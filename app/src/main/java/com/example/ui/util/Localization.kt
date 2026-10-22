package com.example.ui.util

object Localization {
    fun getString(key: String, lang: String): String {
        return when (lang) {
            "hi" -> hindiStrings[key] ?: englishStrings[key] ?: key
            "ta" -> tamilStrings[key] ?: englishStrings[key] ?: key
            else -> englishStrings[key] ?: key
        }
    }

    private val englishStrings = mapOf(
        "app_title" to "MediMind",
        "nav_today" to "Today",
        "nav_medicines" to "Medicines",
        "nav_interactions" to "Interactions",
        "nav_reports" to "Reports",
        "nav_assistant" to "Assistant",
        "good_day" to "Good day",
        "streak_days" to "Days Streak",
        "doses_today" to "Today's Schedule",
        "doses_progress" to "doses logged",
        "critical_alert" to "Critical Interaction Alert",
        "critical_alert_desc" to "Severe drug interaction detected in your active medicines list. Consult your doctor.",
        "due_alarm" to "Medication Due Now",
        "take" to "Take",
        "taken" to "Taken",
        "snooze" to "Snooze",
        "skip" to "Skip",
        "missed" to "Missed",
        "low_stock" to "Low Stock Alert",
        "add_medicine" to "Add Medicine",
        "search_fda" to "Search medicine name (openFDA)...",
        "disclaimer" to "This app provides information only and is not a substitute for professional medical advice. Always consult your doctor or pharmacist before changing any medication.",
        "manual_checker" to "Manual Drug Checker",
        "explain_gemini" to "Explain with Gemini AI",
        "doctor_report" to "Export Doctor Report",
        "caregiver_mode" to "Caregiver Monitoring"
    )

    private val hindiStrings = mapOf(
        "app_title" to "मेडिमाइंड (MediMind)",
        "nav_today" to "आज (Today)",
        "nav_medicines" to "दवाइयाँ",
        "nav_interactions" to "इंटरैक्शन",
        "nav_reports" to "रिपोर्ट",
        "nav_assistant" to "सहायक",
        "good_day" to "नमस्ते",
        "streak_days" to "दिनों का स्ट्रीक",
        "doses_today" to "आज का शेड्यूल",
        "doses_progress" to "खुराकें ली गईं",
        "critical_alert" to "गंभीर दवा चेतावनी",
        "critical_alert_desc" to "आपकी सक्रिय दवाओं में गंभीर इंटरैक्शन पाया गया है। डॉक्टर से संपर्क करें।",
        "due_alarm" to "दवा लेने का समय हो गया",
        "take" to "लें",
        "taken" to "ले ली",
        "snooze" to "स्नूज़",
        "skip" to "छोड़ें",
        "missed" to "छूट गई",
        "low_stock" to "दवा खत्म होने वाली है",
        "add_medicine" to "दवा जोड़ें",
        "search_fda" to "दवा खोजें...",
        "disclaimer" to "यह ऐप केवल जानकारी प्रदान करता है और पेशेवर चिकित्सा सलाह का विकल्प नहीं है। दवा बदलने से पहले हमेशा डॉक्टर से सलाह लें।",
        "manual_checker" to "दवा इंटरैक्शन जांच",
        "explain_gemini" to "Gemini AI से समझें",
        "doctor_report" to "डॉक्टर रिपोर्ट निर्यात करें",
        "caregiver_mode" to "देखभालकर्ता मोड"
    )

    private val tamilStrings = mapOf(
        "app_title" to "மெடிமைண்ட் (MediMind)",
        "nav_today" to "இன்று",
        "nav_medicines" to "மருந்துகள்",
        "nav_interactions" to "எதிர்வினைகள்",
        "nav_reports" to "அறிக்கைகள்",
        "nav_assistant" to "உதவியாளர்",
        "good_day" to "வணக்கம்",
        "streak_days" to "நாட்கள் தொடர்ச்சி",
        "doses_today" to "இன்றைய அட்டவணை",
        "doses_progress" to "மருந்துகள் எடுக்கப்பட்டன",
        "critical_alert" to "தீவிர மருந்து எச்சரிக்கை",
        "critical_alert_desc" to "உங்கள் மருந்துகளில் தீவிர பக்கவிளைவு கண்டறியப்பட்டுள்ளது. மருத்துவரை அணுகவும்.",
        "due_alarm" to "மருந்து எடுத்துக்கொள்ளும் நேரம்",
        "take" to "எடு",
        "taken" to "எடுக்கப்பட்டது",
        "snooze" to "தாமதி",
        "skip" to "தவிர்",
        "missed" to "தவறியது",
        "low_stock" to "குறைந்த இருப்பு எச்சரிக்கை",
        "add_medicine" to "மருந்து சேர்க்கவும்",
        "search_fda" to "மருந்து தேடவும்...",
        "disclaimer" to "இந்த பயன்பாடு தகவலுக்கு மட்டுமே, மருத்துவ ஆலோசனைக்கு மாற்றாகாது. மருத்துவரை அணுகவும்.",
        "manual_checker" to "மருந்து சரிபார்ப்பு",
        "explain_gemini" to "Gemini AI விளக்கம்",
        "doctor_report" to "மருத்துவர் அறிக்கை",
        "caregiver_mode" to "பராமரிப்பாளர் பயன்முறை"
    )
}
