package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.clinical.ClinicalRules
import com.example.data.model.CaregiverAlert
import com.example.data.model.ChatMessage
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.InteractionResult
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import com.example.data.remote.FdaAndAiServices
import com.example.data.remote.FdaDrugSuggestion
import com.example.data.repository.MediMindRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    TODAY,
    MEDICINES,
    INTERACTIONS,
    REPORTS,
    ASSISTANT
}

class MediMindViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediMindRepository(application)

    val medicines: StateFlow<List<Medicine>> = repository.medicinesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDoseLogs: StateFlow<List<DoseLog>> = repository.todayDoseLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDoseLogs: StateFlow<List<DoseLog>> = repository.recentDoseLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val interactions: StateFlow<List<InteractionResult>> = repository.interactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caregiverAlerts: StateFlow<List<CaregiverAlert>> = repository.caregiverAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlarmDose: StateFlow<DoseLog?> = repository.activeAlarmDose
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayDateString: String
        get() = repository.todayDateString

    private val _currentTab = MutableStateFlow(NavTab.TODAY)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _fdaSearchResults = MutableStateFlow<List<FdaDrugSuggestion>>(emptyList())
    val fdaSearchResults: StateFlow<List<FdaDrugSuggestion>> = _fdaSearchResults.asStateFlow()

    private val _isSearchingFda = MutableStateFlow(false)
    val isSearchingFda: StateFlow<Boolean> = _isSearchingFda.asStateFlow()

    private val _selectedInteractionExplanation = MutableStateFlow<String?>(null)
    val selectedInteractionExplanation: StateFlow<String?> = _selectedInteractionExplanation.asStateFlow()

    private val _isExplaining = MutableStateFlow(false)
    val isExplaining: StateFlow<Boolean> = _isExplaining.asStateFlow()

    private val _manualCheckResult = MutableStateFlow<InteractionResult?>(null)
    val manualCheckResult: StateFlow<InteractionResult?> = _manualCheckResult.asStateFlow()

    private val _showAddMedicineDialog = MutableStateFlow(false)
    val showAddMedicineDialog: StateFlow<Boolean> = _showAddMedicineDialog.asStateFlow()

    private val _showDoctorReportDialog = MutableStateFlow(false)
    val showDoctorReportDialog: StateFlow<Boolean> = _showDoctorReportDialog.asStateFlow()

    private val _showConsentDialog = MutableStateFlow(false)
    val showConsentDialog: StateFlow<Boolean> = _showConsentDialog.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _largeTextEnabled = MutableStateFlow(false)
    val largeTextEnabled: StateFlow<Boolean> = _largeTextEnabled.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun setShowAddMedicineDialog(show: Boolean) {
        _showAddMedicineDialog.value = show
        if (!show) {
            _fdaSearchResults.value = emptyList()
        }
    }

    fun setShowDoctorReportDialog(show: Boolean) {
        _showDoctorReportDialog.value = show
    }

    fun setShowConsentDialog(show: Boolean) {
        _showConsentDialog.value = show
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleLargeText() {
        _largeTextEnabled.value = !_largeTextEnabled.value
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun takeDose(logId: String) {
        viewModelScope.launch {
            repository.markDose(logId, DoseStatus.TAKEN)
        }
    }

    fun snoozeDose(logId: String, minutes: Int) {
        viewModelScope.launch {
            repository.snoozeDose(logId, minutes)
        }
    }

    fun skipDose(logId: String) {
        viewModelScope.launch {
            repository.markDose(logId, DoseStatus.SKIPPED)
        }
    }

    fun missDose(logId: String) {
        viewModelScope.launch {
            repository.markDose(logId, DoseStatus.MISSED)
        }
    }

    fun dismissAlarm() {
        repository.dismissAlarm()
    }

    fun searchFda(query: String) {
        viewModelScope.launch {
            if (query.trim().length < 2) {
                _fdaSearchResults.value = emptyList()
                return@launch
            }
            _isSearchingFda.value = true
            val results = FdaAndAiServices.searchOpenFda(query)
            _fdaSearchResults.value = results
            _isSearchingFda.value = false
        }
    }

    private val _selectedFolder = MutableStateFlow("All")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    private val _customFolders = MutableStateFlow<List<String>>(
        listOf(
            "Cardiovascular",
            "Diabetes & Metabolic",
            "Lipids & Heart",
            "Daily Essentials",
            "Vitamins & Supplements",
            "Pain Relief",
            "As-Needed / SOS"
        )
    )
    val customFolders: StateFlow<List<String>> = _customFolders.asStateFlow()

    fun setSelectedFolder(folder: String) {
        _selectedFolder.value = folder
    }

    fun addCustomFolder(folderName: String) {
        val trimmed = folderName.trim()
        if (trimmed.isNotBlank() && !_customFolders.value.contains(trimmed)) {
            _customFolders.value = _customFolders.value + trimmed
            _selectedFolder.value = trimmed
        }
    }

    fun addMedicine(
        name: String,
        genericName: String,
        dosage: String,
        form: String,
        frequency: String,
        times: List<String>,
        mealTiming: String,
        stockCount: Int,
        lowStockThreshold: Int,
        notes: String,
        folder: String = "Daily Essentials"
    ) {
        viewModelScope.launch {
            val med = Medicine(
                name = name,
                genericName = genericName,
                dosage = dosage,
                form = form,
                frequency = frequency,
                scheduledTimes = if (times.isEmpty()) listOf("08:00") else times,
                mealTiming = mealTiming,
                stockCount = stockCount,
                lowStockThreshold = lowStockThreshold,
                notes = notes,
                colorHex = 0xFF0D9488,
                folder = folder
            )
            repository.addMedicine(med)
            _showAddMedicineDialog.value = false
        }
    }

    fun deleteMedicine(id: String) {
        viewModelScope.launch {
            repository.deleteMedicine(id)
        }
    }

    fun checkManualPair(drugA: String, drugB: String) {
        val result = ClinicalRules.checkPair(drugA, drugB)
        _manualCheckResult.value = result ?: InteractionResult(
            drugA = drugA,
            drugB = drugB,
            severity = com.example.data.model.InteractionSeverity.SAFE,
            summary = "No major interaction reported between $drugA and $drugB in the standard clinical database. Always confirm with your pharmacist.",
            source = "Clinical Rules Engine"
        )
    }

    fun clearManualCheck() {
        _manualCheckResult.value = null
    }

    fun explainInteraction(interaction: InteractionResult) {
        viewModelScope.launch {
            _isExplaining.value = true
            _selectedInteractionExplanation.value = null
            val explanation = repository.explainInteraction(interaction)
            _selectedInteractionExplanation.value = explanation
            _isExplaining.value = false
        }
    }

    fun dismissExplanation() {
        _selectedInteractionExplanation.value = null
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(text)
        }
    }

    fun updateProfile(
        name: String,
        age: Int,
        allergies: List<String>,
        conditions: List<String>,
        caregiverName: String,
        caregiverPhone: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                age = age,
                allergies = allergies,
                conditions = conditions,
                caregiverName = caregiverName,
                caregiverPhone = caregiverPhone
            )
            repository.updateProfile(updated)
        }
    }
}
