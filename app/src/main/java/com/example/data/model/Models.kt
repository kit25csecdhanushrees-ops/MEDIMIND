package com.example.data.model

import java.util.UUID

enum class DoseStatus {
    DUE,
    TAKEN,
    SNOOZED,
    SKIPPED,
    MISSED
}

enum class InteractionSeverity {
    SEVERE,
    MODERATE,
    MINOR,
    SAFE
}

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val genericName: String = "",
    val dosage: String = "1 tablet",
    val form: String = "Tablet", // Tablet, Capsule, Syrup, Injection, Inhaler, Drops
    val frequency: String = "Daily", // Daily, Twice daily, Every 8 hours, As needed
    val scheduledTimes: List<String> = listOf("08:00"),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val mealTiming: String = "After food", // Before food, After food, With food, Empty stomach
    val stockCount: Int = 30,
    val lowStockThreshold: Int = 5,
    val notes: String = "",
    val colorHex: Long = 0xFF0D9488,
    val folder: String = "Daily Essentials", // Folder / Category: e.g. "Cardiovascular", "Daily Essentials", "Vitamins & Supplements", "Pain Relief"
    val active: Boolean = true
)

data class DoseLog(
    val id: String = UUID.randomUUID().toString(),
    val medId: String,
    val medName: String,
    val dosage: String,
    val scheduledTime: String, // e.g., "08:00"
    val scheduledDate: String, // e.g., "2026-10-22"
    val status: DoseStatus = DoseStatus.DUE,
    val actualTime: Long? = null,
    val notes: String? = null,
    val folder: String = "Daily Essentials"
)

data class InteractionResult(
    val id: String = UUID.randomUUID().toString(),
    val drugA: String,
    val drugB: String,
    val severity: InteractionSeverity,
    val summary: String,
    val mechanism: String = "",
    val recommendation: String = "",
    val source: String = "Clinical DB", // Clinical DB, openFDA, Gemini AI
    val detectedAt: Long = System.currentTimeMillis(),
    val isAllergyConflict: Boolean = false,
    val isConditionConflict: Boolean = false,
    val acknowledged: Boolean = false
)

data class UserProfile(
    val id: String = "primary_user",
    val name: String = "Alex Rivera",
    val age: Int = 58,
    val allergies: List<String> = listOf("Penicillin", "NSAIDs / Aspirin"),
    val conditions: List<String> = listOf("Hypertension", "Peptic Ulcer"),
    val caregiverName: String = "Maria Rivera",
    val caregiverPhone: String = "+1 (555) 234-5678",
    val caregiverInviteCode: String = "MIND-9842-CARE",
    val caregiverLinked: Boolean = true,
    val timeZone: String = "UTC-7",
    val largeTextEnabled: Boolean = false,
    val language: String = "en", // "en", "hi", "ta"
    val onboardingCompleted: Boolean = true
)

data class CaregiverAlert(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val patientName: String,
    val medicineName: String,
    val scheduledTime: String,
    val message: String,
    val isCritical: Boolean = false
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isDisclaimed: Boolean = true
)
