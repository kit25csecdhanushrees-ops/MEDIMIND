package com.example.data.clinical

import com.example.data.model.InteractionResult
import com.example.data.model.InteractionSeverity
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import java.util.Locale

data class ClinicalRule(
    val drugAKeyword: String,
    val drugBKeyword: String,
    val severity: InteractionSeverity,
    val summary: String,
    val mechanism: String,
    val recommendation: String
)

object ClinicalRules {

    private val rules = listOf(
        ClinicalRule(
            drugAKeyword = "warfarin",
            drugBKeyword = "aspirin",
            severity = InteractionSeverity.SEVERE,
            summary = "High risk of life-threatening gastrointestinal or intracranial bleeding.",
            mechanism = "Warfarin inhibits vitamin K-dependent clotting factors while aspirin irreversibly inhibits platelet aggregation. Combined use severely impairs hemostasis.",
            recommendation = "Contact your doctor immediately. Do not co-administer without specific dual-therapy cardiology monitoring and gastric protection (e.g., PPI)."
        ),
        ClinicalRule(
            drugAKeyword = "warfarin",
            drugBKeyword = "ibuprofen",
            severity = InteractionSeverity.SEVERE,
            summary = "Substantial elevation of bleeding risk and gastric mucosal damage.",
            mechanism = "NSAIDs displace warfarin from albumin, prolong prothrombin time, and erode gastric mucosa.",
            recommendation = "Avoid combination. Use acetaminophen for mild analgesia after consulting your physician."
        ),
        ClinicalRule(
            drugAKeyword = "sildenafil",
            drugBKeyword = "nitroglycerin",
            severity = InteractionSeverity.SEVERE,
            summary = "Profound, life-threatening hypotension and syncope.",
            mechanism = "PDE-5 inhibitors potentiate the hypotensive effects of organic nitrates via amplified cGMP accumulation.",
            recommendation = "Absolute contraindication. Nitrates must not be administered within 24 hours of sildenafil."
        ),
        ClinicalRule(
            drugAKeyword = "lisinopril",
            drugBKeyword = "spironolactone",
            severity = InteractionSeverity.SEVERE,
            summary = "Risk of severe hyperkalemia leading to cardiac arrhythmias.",
            mechanism = "Both agents decrease renal potassium excretion through renin-angiotensin-aldosterone system inhibition.",
            recommendation = "Requires frequent serum potassium and renal function monitoring (BUN/creatinine)."
        ),
        ClinicalRule(
            drugAKeyword = "simvastatin",
            drugBKeyword = "clarithromycin",
            severity = InteractionSeverity.SEVERE,
            summary = "Markedly increased simvastatin plasma concentrations causing rhabdomyolysis.",
            mechanism = "Clarithromycin is a potent CYP3A4 inhibitor, blocking statin metabolic clearance.",
            recommendation = "Temporarily suspend simvastatin therapy during macrolide antibiotic course."
        ),
        ClinicalRule(
            drugAKeyword = "methotrexate",
            drugBKeyword = "ibuprofen",
            severity = InteractionSeverity.SEVERE,
            summary = "Elevated methotrexate toxicity, pancytopenia, and severe bone marrow suppression.",
            mechanism = "NSAIDs reduce renal perfusion and compete for renal organic anion transport.",
            recommendation = "Avoid high-dose methotrexate co-administration with NSAIDs without strict hospital oncologic oversight."
        ),
        ClinicalRule(
            drugAKeyword = "citalopram",
            drugBKeyword = "tramadol",
            severity = InteractionSeverity.SEVERE,
            summary = "Serotonin syndrome risk and lowered seizure threshold.",
            mechanism = "Additive serotonergic reuptake inhibition and tramadol seizure-precipitating properties.",
            recommendation = "Monitor for tremor, hyperreflexia, autonomic instability, or select alternative analgesic."
        ),
        ClinicalRule(
            drugAKeyword = "digoxin",
            drugBKeyword = "amiodarone",
            severity = InteractionSeverity.SEVERE,
            summary = "Digoxin toxicity with severe bradycardia and AV nodal block.",
            mechanism = "Amiodarone inhibits P-glycoprotein-mediated renal and biliary secretion of digoxin.",
            recommendation = "Reduce digoxin dose by 50% upon starting amiodarone and monitor serum levels."
        ),
        ClinicalRule(
            drugAKeyword = "clopidogrel",
            drugBKeyword = "omeprazole",
            severity = InteractionSeverity.MODERATE,
            summary = "Reduced antiplatelet efficacy of clopidogrel.",
            mechanism = "Omeprazole inhibits CYP2C19, which is required to convert clopidogrel to its active metabolite.",
            recommendation = "Consider pantoprazole or famotidine as alternative gastric protective agents."
        ),
        ClinicalRule(
            drugAKeyword = "levothyroxine",
            drugBKeyword = "calcium",
            severity = InteractionSeverity.MODERATE,
            summary = "Decreased levothyroxine absorption leading to hypothyroidism symptoms.",
            mechanism = "Calcium carbonate forms insoluble chelation complexes with thyroid hormone in the gut.",
            recommendation = "Separate administration times by at least 4 hours."
        ),
        ClinicalRule(
            drugAKeyword = "ciprofloxacin",
            drugBKeyword = "antacid",
            severity = InteractionSeverity.MODERATE,
            summary = "Significant decrease in fluoroquinolone bioavailability.",
            mechanism = "Divalent and trivalent cations (aluminum, magnesium, calcium) chelate ciprofloxacin.",
            recommendation = "Take ciprofloxacin 2 hours before or 6 hours after cation-containing antacids."
        ),
        ClinicalRule(
            drugAKeyword = "metformin",
            drugBKeyword = "contrast",
            severity = InteractionSeverity.SEVERE,
            summary = "Risk of contrast-induced nephropathy and fatal lactic acidosis.",
            mechanism = "Iodinated radiocontrast can precipitate acute renal impairment, causing metformin accumulation.",
            recommendation = "Discontinue metformin 48 hours prior to intravascular contrast procedures."
        ),
        ClinicalRule(
            drugAKeyword = "lithium",
            drugBKeyword = "ibuprofen",
            severity = InteractionSeverity.SEVERE,
            summary = "Elevated lithium levels resulting in tremor, ataxia, and renal toxicity.",
            mechanism = "NSAIDs reduce renal prostaglandin synthesis, lowering renal clearance of lithium.",
            recommendation = "Monitor lithium blood levels closely or switch to alternative analgesics."
        ),
        ClinicalRule(
            drugAKeyword = "atorvastatin",
            drugBKeyword = "gemfibrozil",
            severity = InteractionSeverity.SEVERE,
            summary = "High risk of myopathy and rhabdomyolysis.",
            mechanism = "Inhibition of statin glucuronidation and OATP1B1 hepatic uptake.",
            recommendation = "Avoid combination; fenofibrate is safer if fibrate therapy is strictly indicated."
        )
    )

    fun checkPair(nameA: String, nameB: String): InteractionResult? {
        val a = nameA.lowercase(Locale.ROOT)
        val b = nameB.lowercase(Locale.ROOT)

        for (rule in rules) {
            val matchesOrder1 = a.contains(rule.drugAKeyword) && b.contains(rule.drugBKeyword)
            val matchesOrder2 = b.contains(rule.drugAKeyword) && a.contains(rule.drugBKeyword)
            if (matchesOrder1 || matchesOrder2) {
                return InteractionResult(
                    drugA = nameA,
                    drugB = nameB,
                    severity = rule.severity,
                    summary = rule.summary,
                    mechanism = rule.mechanism,
                    recommendation = rule.recommendation,
                    source = "Clinical Knowledge Base"
                )
            }
        }
        return null
    }

    fun evaluateInteractions(medicines: List<Medicine>, profile: UserProfile): List<InteractionResult> {
        val results = mutableListOf<InteractionResult>()
        val activeMeds = medicines.filter { it.active }

        // 1. Drug-Drug Interactions
        for (i in 0 until activeMeds.size) {
            for (j in i + 1 until activeMeds.size) {
                val medA = activeMeds[i]
                val medB = activeMeds[j]
                val match = checkPair("${medA.name} ${medA.genericName}", "${medB.name} ${medB.genericName}")
                if (match != null) {
                    results.add(
                        match.copy(
                            drugA = "${medA.name} (${medA.dosage})",
                            drugB = "${medB.name} (${medB.dosage})"
                        )
                    )
                }
            }
        }

        // 2. Allergy Contraindications
        for (med in activeMeds) {
            val medText = "${med.name} ${med.genericName}".lowercase(Locale.ROOT)
            for (allergy in profile.allergies) {
                val allergyLower = allergy.lowercase(Locale.ROOT)
                if (allergyLower.contains("penicillin") && (medText.contains("penicillin") || medText.contains("amoxicillin") || medText.contains("ampicillin"))) {
                    results.add(
                        InteractionResult(
                            drugA = med.name,
                            drugB = "Known Allergy: $allergy",
                            severity = InteractionSeverity.SEVERE,
                            summary = "Direct allergen conflict: Patient has documented penicillin allergy.",
                            mechanism = "Beta-lactam ring cross-reactivity triggers IgE-mediated anaphylaxis.",
                            recommendation = "DO NOT ADMINISTER. Inform prescribing physician immediately for cephalosporin or non-beta-lactam alternative.",
                            source = "Patient Health Profile",
                            isAllergyConflict = true
                        )
                    )
                }
                if ((allergyLower.contains("aspirin") || allergyLower.contains("nsaid")) && (medText.contains("aspirin") || medText.contains("ibuprofen") || medText.contains("naproxen"))) {
                    results.add(
                        InteractionResult(
                            drugA = med.name,
                            drugB = "Known Allergy: $allergy",
                            severity = InteractionSeverity.SEVERE,
                            summary = "Allergen conflict: Patient has documented NSAID hypersensitivity.",
                            mechanism = "COX-1 inhibition can induce bronchospasm, urticaria, or angioedema.",
                            recommendation = "Stop medication. Consult doctor for acetaminophen or selective alternatives.",
                            source = "Patient Health Profile",
                            isAllergyConflict = true
                        )
                    )
                }
            }

            // 3. Chronic Condition Conflicts
            for (condition in profile.conditions) {
                val conditionLower = condition.lowercase(Locale.ROOT)
                if (conditionLower.contains("ulcer") && (medText.contains("aspirin") || medText.contains("ibuprofen") || medText.contains("naproxen"))) {
                    results.add(
                        InteractionResult(
                            drugA = med.name,
                            drugB = "Chronic Condition: $condition",
                            severity = InteractionSeverity.MODERATE,
                            summary = "Disease conflict: NSAIDs and aspirin irritate gastric lining and exacerbate peptic ulcers.",
                            mechanism = "Suppression of gastroprotective prostaglandins E2 and I2 promotes mucosal perforation and bleeding.",
                            recommendation = "Ask your doctor if gastric co-protection (PPI) or a stomach-sparing pain medication is advised.",
                            source = "Clinical Disease Safety Rules",
                            isConditionConflict = true
                        )
                    )
                }
                if (conditionLower.contains("hypertension") && (medText.contains("pseudoephedrine") || medText.contains("phenylephrine"))) {
                    results.add(
                        InteractionResult(
                            drugA = med.name,
                            drugB = "Chronic Condition: $condition",
                            severity = InteractionSeverity.MODERATE,
                            summary = "Sympathomimetic decongestants can dangerously elevate blood pressure.",
                            mechanism = "Alpha-1 adrenergic receptor stimulation causes peripheral vasoconstriction.",
                            recommendation = "Use non-decongestant saline sprays or consult your doctor.",
                            source = "Clinical Disease Safety Rules",
                            isConditionConflict = true
                        )
                    )
                }
            }
        }

        return results
    }
}
