package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.clinical.ClinicalRules
import com.example.data.local.DoseLogEntity
import com.example.data.local.MediMindDatabase
import com.example.data.local.MedicineEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.CaregiverAlert
import com.example.data.model.ChatMessage
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.InteractionResult
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import com.example.data.remote.FdaAndAiServices
import com.example.util.AlarmAndSoundHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MediMindRepository(private val context: Context) {

    private val db: MediMindDatabase = Room.databaseBuilder(
        context.applicationContext,
        MediMindDatabase::class.java,
        "medimind_db"
    ).fallbackToDestructiveMigration(true).build()

    private val dao = db.dao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _interactions = MutableStateFlow<List<InteractionResult>>(emptyList())
    val interactions: Flow<List<InteractionResult>> = _interactions.asStateFlow()

    private val _caregiverAlerts = MutableStateFlow<List<CaregiverAlert>>(emptyList())
    val caregiverAlerts: Flow<List<CaregiverAlert>> = _caregiverAlerts.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello Alex! I am your MediMind Clinical AI Assistant. I have your active medicines and health profile loaded. How can I help you today?",
                isUser = false
            )
        )
    )
    val chatMessages: Flow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activeAlarmDose = MutableStateFlow<DoseLog?>(null)
    val activeAlarmDose: Flow<DoseLog?> = _activeAlarmDose.asStateFlow()

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val medicinesFlow: Flow<List<Medicine>> = dao.getAllMedicines().map { list ->
        list.map { it.toModel() }
    }

    val todayDoseLogsFlow: Flow<List<DoseLog>> = dao.getDoseLogsByDate(todayDateString).map { list ->
        list.map { it.toModel() }
    }

    val recentDoseLogsFlow: Flow<List<DoseLog>> = dao.getRecentDoseLogs().map { list ->
        list.map { it.toModel() }
    }

    val userProfileFlow: Flow<UserProfile> = dao.getUserProfile().map { entity ->
        entity?.toModel() ?: UserProfile()
    }

    init {
        scope.launch {
            seedInitialDataIfEmpty()
            recalculateInteractions()
            checkDueDoses()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllMedicines().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultProfile = UserProfile()
            dao.saveUserProfile(UserProfileEntity.fromModel(defaultProfile))

            val warfarin = Medicine(
                id = "med_warfarin",
                name = "Warfarin (Coumadin)",
                genericName = "warfarin sodium",
                dosage = "5 mg",
                form = "Tablet",
                frequency = "Daily",
                scheduledTimes = listOf("08:00", "20:00"),
                mealTiming = "After food",
                stockCount = 28,
                lowStockThreshold = 5,
                notes = "Anticoagulant blood thinner. Regular INR checks required.",
                colorHex = 0xFFDC2626,
                folder = "Cardiovascular"
            )

            val aspirin = Medicine(
                id = "med_aspirin",
                name = "Aspirin (Bayer)",
                genericName = "acetylsalicylic acid",
                dosage = "81 mg",
                form = "Tablet",
                frequency = "Daily",
                scheduledTimes = listOf("08:00"),
                mealTiming = "With food",
                stockCount = 14,
                lowStockThreshold = 5,
                notes = "Cardioprotective low-dose antiplatelet.",
                colorHex = 0xFFEA580C,
                folder = "Cardiovascular"
            )

            val metformin = Medicine(
                id = "med_metformin",
                name = "Metformin (Glucophage)",
                genericName = "metformin hydrochloride",
                dosage = "500 mg",
                form = "Tablet",
                frequency = "Twice daily",
                scheduledTimes = listOf("09:00", "21:00"),
                mealTiming = "With food",
                stockCount = 42,
                lowStockThreshold = 7,
                notes = "For glycemic management. Take with breakfast & dinner.",
                colorHex = 0xFF0284C7,
                folder = "Diabetes & Metabolic"
            )

            val atorvastatin = Medicine(
                id = "med_atorvastatin",
                name = "Atorvastatin (Lipitor)",
                genericName = "atorvastatin calcium",
                dosage = "20 mg",
                form = "Tablet",
                frequency = "Daily",
                scheduledTimes = listOf("21:00"),
                mealTiming = "Before bed",
                stockCount = 3, // Low stock triggers warning banner!
                lowStockThreshold = 5,
                notes = "Cholesterol management. Refill needed soon.",
                colorHex = 0xFF7C3AED,
                folder = "Lipids & Heart"
            )

            dao.insertMedicines(
                listOf(
                    MedicineEntity.fromModel(warfarin),
                    MedicineEntity.fromModel(aspirin),
                    MedicineEntity.fromModel(metformin),
                    MedicineEntity.fromModel(atorvastatin)
                )
            )

            // Seed today's dose logs
            val today = todayDateString
            val logs = listOf(
                DoseLog(
                    id = "log_1",
                    medId = warfarin.id,
                    medName = warfarin.name,
                    dosage = warfarin.dosage,
                    scheduledTime = "08:00",
                    scheduledDate = today,
                    status = DoseStatus.TAKEN,
                    actualTime = System.currentTimeMillis() - 7200000,
                    folder = "Cardiovascular"
                ),
                DoseLog(
                    id = "log_2",
                    medId = aspirin.id,
                    medName = aspirin.name,
                    dosage = aspirin.dosage,
                    scheduledTime = "08:00",
                    scheduledDate = today,
                    status = DoseStatus.DUE,
                    folder = "Cardiovascular"
                ),
                DoseLog(
                    id = "log_3",
                    medId = metformin.id,
                    medName = metformin.name,
                    dosage = metformin.dosage,
                    scheduledTime = "09:00",
                    scheduledDate = today,
                    status = DoseStatus.DUE,
                    folder = "Diabetes & Metabolic"
                ),
                DoseLog(
                    id = "log_4",
                    medId = warfarin.id,
                    medName = warfarin.name,
                    dosage = warfarin.dosage,
                    scheduledTime = "20:00",
                    scheduledDate = today,
                    status = DoseStatus.DUE,
                    folder = "Cardiovascular"
                ),
                DoseLog(
                    id = "log_5",
                    medId = atorvastatin.id,
                    medName = atorvastatin.name,
                    dosage = atorvastatin.dosage,
                    scheduledTime = "21:00",
                    scheduledDate = today,
                    status = DoseStatus.DUE,
                    folder = "Lipids & Heart"
                )
            )
            dao.insertDoseLogs(logs.map { DoseLogEntity.fromModel(it) })
        }
    }

    private suspend fun checkDueDoses() {
        val today = todayDateString
        val logs = dao.getDoseLogsByDate(today).firstOrNull() ?: emptyList()
        val dueLog = logs.firstOrNull { it.status == DoseStatus.DUE.name }
        if (dueLog != null) {
            _activeAlarmDose.value = dueLog.toModel()
        }
    }

    suspend fun recalculateInteractions() {
        val meds = dao.getAllMedicines().firstOrNull()?.map { it.toModel() } ?: emptyList()
        val profile = dao.getUserProfile().firstOrNull()?.toModel() ?: UserProfile()
        val results = ClinicalRules.evaluateInteractions(meds, profile)
        _interactions.value = results
    }

    suspend fun addMedicine(med: Medicine) {
        dao.insertMedicine(MedicineEntity.fromModel(med))
        // Generate today's logs for this new medicine
        val today = todayDateString
        val newLogs = med.scheduledTimes.map { time ->
            DoseLog(
                id = UUID.randomUUID().toString(),
                medId = med.id,
                medName = med.name,
                dosage = med.dosage,
                scheduledTime = time,
                scheduledDate = today,
                status = DoseStatus.DUE,
                folder = med.folder
            )
        }
        dao.insertDoseLogs(newLogs.map { DoseLogEntity.fromModel(it) })
        recalculateInteractions()
    }

    suspend fun updateMedicine(med: Medicine) {
        dao.updateMedicine(MedicineEntity.fromModel(med))
        recalculateInteractions()
    }

    suspend fun deleteMedicine(id: String) {
        dao.deleteMedicineById(id)
        recalculateInteractions()
    }

    suspend fun markDose(logId: String, status: DoseStatus) {
        val todayLogs = dao.getDoseLogsByDate(todayDateString).firstOrNull() ?: emptyList()
        val target = todayLogs.firstOrNull { it.id == logId } ?: return
        val updated = target.copy(
            status = status.name,
            actualTime = if (status == DoseStatus.TAKEN) System.currentTimeMillis() else null
        )
        dao.updateDoseLog(updated)

        // Decrement stock if taken
        if (status == DoseStatus.TAKEN) {
            val meds = dao.getAllMedicines().firstOrNull() ?: emptyList()
            val medEntity = meds.firstOrNull { it.id == target.medId }
            if (medEntity != null && medEntity.stockCount > 0) {
                dao.updateMedicine(medEntity.copy(stockCount = medEntity.stockCount - 1))
            }
            AlarmAndSoundHelper.playMedicationChime()
            AlarmAndSoundHelper.triggerVibration(context, isLong = false)
        }

        // If missed, trigger caregiver alert
        if (status == DoseStatus.MISSED) {
            val profile = dao.getUserProfile().firstOrNull()?.toModel() ?: UserProfile()
            val alert = CaregiverAlert(
                patientName = profile.name,
                medicineName = target.medName,
                scheduledTime = target.scheduledTime,
                message = "Alert sent to ${profile.caregiverName}: Dose of ${target.medName} (${target.dosage}) missed at ${target.scheduledTime}.",
                isCritical = target.medName.contains("Warfarin", ignoreCase = true)
            )
            _caregiverAlerts.value = listOf(alert) + _caregiverAlerts.value
        }

        if (_activeAlarmDose.value?.id == logId) {
            _activeAlarmDose.value = null
        }
    }

    suspend fun snoozeDose(logId: String, minutes: Int) {
        val todayLogs = dao.getDoseLogsByDate(todayDateString).firstOrNull() ?: emptyList()
        val target = todayLogs.firstOrNull { it.id == logId } ?: return
        val updated = target.copy(
            status = DoseStatus.SNOOZED.name,
            notes = "Snoozed for $minutes mins"
        )
        dao.updateDoseLog(updated)
        if (_activeAlarmDose.value?.id == logId) {
            _activeAlarmDose.value = null
        }
    }

    fun dismissAlarm() {
        _activeAlarmDose.value = null
    }

    suspend fun updateProfile(profile: UserProfile) {
        dao.saveUserProfile(UserProfileEntity.fromModel(profile))
        recalculateInteractions()
    }

    suspend fun explainInteraction(interaction: InteractionResult): String {
        return FdaAndAiServices.explainInteractionWithGemini(
            drugA = interaction.drugA,
            drugB = interaction.drugB,
            severity = interaction.severity.name,
            clinicalSummary = interaction.summary
        )
    }

    suspend fun sendChatMessage(userText: String) {
        val userMsg = ChatMessage(text = userText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        val meds = dao.getAllMedicines().firstOrNull()?.map { it.toModel() } ?: emptyList()
        val profile = dao.getUserProfile().firstOrNull()?.toModel() ?: UserProfile()

        val answer = FdaAndAiServices.askAssistant(userText, meds, profile)
        val assistantMsg = ChatMessage(text = answer, isUser = false)
        _chatMessages.value = _chatMessages.value + assistantMsg
    }
}
