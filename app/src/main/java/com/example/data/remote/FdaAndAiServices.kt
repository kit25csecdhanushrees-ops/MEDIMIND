package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FdaDrugSuggestion(
    val brandName: String,
    val genericName: String,
    val dosageForm: String,
    val manufacturer: String,
    val warningsSummary: String = ""
)

object FdaAndAiServices {
    private const val TAG = "FdaAndAiServices"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Queries openFDA Drug Label API for search-as-you-type drug matching.
     */
    suspend fun searchOpenFda(query: String): List<FdaDrugSuggestion> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        val cleanQuery = query.trim().replace("\"", "").replace(" ", "+")
        val url = "https://api.fda.gov/drug/label.json?search=openfda.brand_name:\"$cleanQuery\"+openfda.generic_name:\"$cleanQuery\"&limit=6"

        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext fallbackLocalSearch(query)
            }
            val body = response.body?.string() ?: return@withContext fallbackLocalSearch(query)
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return@withContext fallbackLocalSearch(query)

            val suggestions = mutableListOf<FdaDrugSuggestion>()
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val openFda = item.optJSONObject("openfda")
                val brandNames = openFda?.optJSONArray("brand_name")
                val genericNames = openFda?.optJSONArray("generic_name")
                val forms = openFda?.optJSONArray("dosage_form")
                val manufacturers = openFda?.optJSONArray("manufacturer_name")

                val brand = brandNames?.optString(0) ?: ""
                val generic = genericNames?.optString(0) ?: ""
                val form = forms?.optString(0) ?: "Tablet"
                val manufacturer = manufacturers?.optString(0) ?: "Pharmaceutical"

                val interactions = item.optJSONArray("drug_interactions")?.optString(0) ?: ""
                val summary = if (interactions.length > 200) interactions.substring(0, 200) + "..." else interactions

                if (brand.isNotEmpty() || generic.isNotEmpty()) {
                    suggestions.add(
                        FdaDrugSuggestion(
                            brandName = if (brand.isNotEmpty()) brand else generic,
                            genericName = generic,
                            dosageForm = form.replaceFirstChar { it.uppercase() },
                            manufacturer = manufacturer,
                            warningsSummary = summary
                        )
                    )
                }
            }

            if (suggestions.isEmpty()) fallbackLocalSearch(query) else suggestions
        } catch (e: Exception) {
            Log.w(TAG, "openFDA network search error: ${e.message}, using fallback catalog.")
            fallbackLocalSearch(query)
        }
    }

    private fun fallbackLocalSearch(query: String): List<FdaDrugSuggestion> {
        val catalog = listOf(
            FdaDrugSuggestion("Warfarin (Coumadin)", "warfarin sodium", "Tablet", "Bristol-Myers Squibb", "High bleeding hazard when combined with NSAIDs or antiplatelets."),
            FdaDrugSuggestion("Aspirin (Bayer)", "acetylsalicylic acid", "Tablet", "Bayer Healthcare", "Inhibits platelet aggregation. Exacerbates peptic ulcer disease."),
            FdaDrugSuggestion("Lisinopril (Zestril)", "lisinopril", "Tablet", "AstraZeneca", "ACE inhibitor. Monitor potassium when used with potassium-sparing diuretics."),
            FdaDrugSuggestion("Metformin (Glucophage)", "metformin hydrochloride", "Tablet", "Merck", "First-line biguanide for type 2 diabetes. Risk of lactic acidosis with iodinated contrast."),
            FdaDrugSuggestion("Atorvastatin (Lipitor)", "atorvastatin calcium", "Tablet", "Pfizer", "HMG-CoA reductase inhibitor for lipid management."),
            FdaDrugSuggestion("Amlodipine (Norvasc)", "amlodipine besylate", "Tablet", "Pfizer", "Calcium channel blocker for hypertension and angina."),
            FdaDrugSuggestion("Levothyroxine (Synthroid)", "levothyroxine sodium", "Tablet", "AbbVie", "Thyroid hormone replacement. Absorption reduced by iron/calcium."),
            FdaDrugSuggestion("Omeprazole (Prilosec)", "omeprazole", "Capsule", "Procter & Gamble", "Proton pump inhibitor. Decreases activation of clopidogrel."),
            FdaDrugSuggestion("Ibuprofen (Advil / Motrin)", "ibuprofen", "Tablet", "Haleon", "NSAID pain reliever. Severe interaction with anticoagulants and lithium."),
            FdaDrugSuggestion("Amoxicillin (Amoxil)", "amoxicillin", "Capsule", "GlaxoSmithKline", "Beta-lactam antibiotic. Contraindicated in penicillin-allergic patients."),
            FdaDrugSuggestion("Sildenafil (Viagra)", "sildenafil citrate", "Tablet", "Pfizer", "PDE-5 inhibitor. Fatal hypotension when paired with organic nitrates."),
            FdaDrugSuggestion("Spironolactone (Aldactone)", "spironolactone", "Tablet", "Pfizer", "Potassium-sparing diuretic. Hyperkalemia risk with ACE inhibitors."),
            FdaDrugSuggestion("Citalopram (Celexa)", "citalopram hydrobromide", "Tablet", "Allergan", "SSRI antidepressant. Serotonin syndrome risk with tramadol."),
            FdaDrugSuggestion("Tramadol (Ultram)", "tramadol hydrochloride", "Tablet", "Janssen", "Opioid analgesic with serotonergic activity.")
        )
        val q = query.lowercase().trim()
        return catalog.filter {
            it.brandName.lowercase().contains(q) || it.genericName.lowercase().contains(q)
        }
    }

    /**
     * Uses Gemini to explain a detected interaction in compassionate, patient-friendly terms.
     */
    suspend fun explainInteractionWithGemini(
        drugA: String,
        drugB: String,
        severity: String,
        clinicalSummary: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Clinical Summary:\n$clinicalSummary\n\nActionable Advice:\n- Do not change or discontinue your medication on your own.\n- Inform your pharmacist or doctor that you are taking both $drugA and $drugB.\n- Ask: 'Is there a safer alternative or does my dosage need to be monitored?'"
        }

        val prompt = """
            You are MediMind's Clinical Safety AI. Explain the following drug interaction in clear, calm, and plain language for a patient:
            - Medicine 1: $drugA
            - Medicine 2: $drugB
            - Severity: $severity
            - Clinical context: $clinicalSummary

            Formatting requirements:
            1. What is this interaction? (1-2 clear sentences in simple non-medical terms)
            2. Why it matters to you? (Key symptoms to watch out for)
            3. Exact questions to ask your doctor or pharmacist.
            Keep it under 150 words. Do NOT tell the patient to stop their medicine on their own; always emphasize consulting their doctor.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        try {
            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: ""
                val resJson = JSONObject(resStr)
                val candidates = resJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API interaction explanation error: ${e.message}")
        }

        // Resilient fallback
        return@withContext "Clinical Summary:\n$clinicalSummary\n\nPatient Recommendation:\n- Both $drugA and $drugB can interact to increase side effects.\n- Please consult your physician before your next dose.\n- Never stop prescribed therapies abruptly."
    }

    /**
     * Answers patient medication Q&A with Gemini AI, grounded with their active medications.
     */
    suspend fun askAssistant(
        question: String,
        activeMedicines: List<Medicine>,
        userProfile: UserProfile
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        val medListStr = if (activeMedicines.isEmpty()) "None currently logged" else {
            activeMedicines.joinToString("; ") { "${it.name} (${it.dosage}, ${it.form}, ${it.frequency})" }
        }
        val allergiesStr = if (userProfile.allergies.isEmpty()) "None declared" else userProfile.allergies.joinToString(", ")
        val conditionsStr = if (userProfile.conditions.isEmpty()) "None declared" else userProfile.conditions.joinToString(", ")

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineAssistantAnswer(question, activeMedicines, userProfile)
        }

        val prompt = """
            You are MediMind, a thoughtful, medically responsible, and cautious medication assistant.
            Patient Profile:
            - Active Medications: $medListStr
            - Known Allergies: $allergiesStr
            - Chronic Conditions: $conditionsStr
            
            Patient Question: "$question"

            Guidelines:
            - Provide a clear, safe, and helpful answer grounded in their active medications and allergies.
            - If asking about taking an OTC medicine (like ibuprofen or cold meds) with their current prescriptions, explicitly cross-reference for interactions.
            - Always conclude with a reminder to confirm with their doctor or pharmacist.
            - Keep your answer under 160 words, structured with brief bullet points.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        try {
            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: ""
                val resJson = JSONObject(resStr)
                val candidates = resJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Assistant error: ${e.message}")
        }

        return@withContext generateOfflineAssistantAnswer(question, activeMedicines, userProfile)
    }

    private fun generateOfflineAssistantAnswer(
        question: String,
        activeMedicines: List<Medicine>,
        userProfile: UserProfile
    ): String {
        val q = question.lowercase()
        val hasBloodThinner = activeMedicines.any { it.name.lowercase().contains("warfarin") || it.name.lowercase().contains("coumadin") || it.name.lowercase().contains("aspirin") }
        val hasBpMed = activeMedicines.any { it.name.lowercase().contains("lisinopril") || it.name.lowercase().contains("amlodipine") || it.name.lowercase().contains("losartan") }

        return when {
            q.contains("ibuprofen") && hasBloodThinner -> {
                "Caution: You are taking a blood thinner/anticoagulant (${activeMedicines.firstOrNull { it.name.lowercase().contains("warfarin") || it.name.lowercase().contains("aspirin") }?.name}). Co-administering Ibuprofen or other NSAIDs significantly elevates gastrointestinal bleeding risk. We advise contacting your pharmacist or doctor before taking ibuprofen; acetaminophen (Tylenol) is typically preferred."
            }
            q.contains("ibuprofen") && hasBpMed -> {
                "Note: Regular use of NSAIDs like Ibuprofen can elevate blood pressure and reduce the effectiveness of ACE inhibitors or ARBs. For occasional mild pain, ask your doctor if acetaminophen is suitable."
            }
            q.contains("missed") || q.contains("forget") || q.contains("forgot") -> {
                "If you miss a dose, take it as soon as you remember unless it is almost time for your next scheduled dose. Never take a double dose to make up for a missed one. For anticoagulant medications like Warfarin, contact your clinic for specific missed-dose instructions."
            }
            q.contains("food") || q.contains("empty stomach") -> {
                "Check the meal timing indicator on your medicine card: most medications like NSAIDs or metformin should be taken with or after food to prevent stomach irritation, while thyroid hormones like levothyroxine require an empty stomach with water."
            }
            else -> {
                "Based on your current medications (${activeMedicines.joinToString { it.name }}): Always double-check with your doctor or pharmacist prior to introducing new supplements or OTC drugs. If you experience severe symptoms like dizziness, unusual bruising, or allergic reactions, seek emergency medical care."
            }
        }
    }
}
