package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CycleSummaryEntity
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.data.preferences.SessionPreferences
import com.example.data.repository.EndoTrackRepository
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import com.example.ml.RiskTier
import com.example.ml.SymptomCategory
import com.example.util.ClinicianPdfExporter
import com.example.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionStatus {
    data object Loading : SessionStatus
    data object LoggedOut : SessionStatus
    data class LoggedIn(val user: UserEntity) : SessionStatus
}

data class LogDraftState(
    val epochDay: Long = DateUtil.todayEpochDay(),
    val painLevel: Int = 4,
    val hasPeriod: Boolean = false,
    val flowVolume: String = "MEDIUM",
    val tookOtcMedication: Boolean = false,
    val otcMedicationName: String = "",
    val otcRelievedPain: String = "PARTIAL",
    val impactFlags: Set<String> = emptySet(),
    val notes: String = "",
    val selectedSymptoms: Set<String> = emptySet(),
    val searchQuery: String = "",
    val expandedCategories: Set<SymptomCategory> = setOf(SymptomCategory.PELVIC_MENSTRUAL_PAIN),
    val existingLogId: Long? = null,
    val isSaving: Boolean = false,
    val lastSavedRiskResult: MultiCycleRiskResult? = null
)

data class EndoTrackUiState(
    val sessionStatus: SessionStatus = SessionStatus.Loading,
    val hasSeenOnboarding: Boolean = false,
    val authError: String? = null,
    val isAuthBusy: Boolean = false,
    val logs: List<SymptomLogEntity> = emptyList(),
    val cycleSummaries: List<CycleSummaryEntity> = emptyList(),
    val modelWeightsEntity: ModelWeightsEntity? = null,
    val multiCycleRisk: MultiCycleRiskResult = MultiCycleRiskResult(
        aggregatedProbability = 0.02,
        latestDailyProbability = 0.02,
        riskTier = RiskTier.LOW,
        topDrivers = emptyList(),
        totalLogsAnalyzed = 0,
        cyclesAnalyzed = 0,
        recurringSymptomCount = 0,
        isPersonalizedModel = false
    ),
    val timeframeDays: Int = 14,
    val clinicianNote: String = "Primary concern: Recurring severe pelvic pain during menstruation and mid-cycle flares. OTC Ibuprofen (400–600mg) frequently does not relieve peak pain on cycle days 1–3.",
    val statusBannerMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class EndoTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EndoTrackRepository by lazy {
        val db = AppDatabase.getInstance(application)
        val prefs = SessionPreferences(application)
        EndoTrackRepository(db.endoTrackDao(), prefs)
    }

    private val _authError = MutableStateFlow<String?>(null)
    private val _isAuthBusy = MutableStateFlow(false)
    private val _timeframeDays = MutableStateFlow(14)
    private val _clinicianNote = MutableStateFlow(
        "Primary concern: Recurring severe pelvic pain during menstruation and mid-cycle flares. OTC Ibuprofen (400–600mg) frequently does not relieve peak pain on cycle days 1–3."
    )
    private val _statusBanner = MutableStateFlow<String?>(null)

    private val _logDraft = MutableStateFlow(LogDraftState())
    val logDraft: StateFlow<LogDraftState> = _logDraft.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDemoSeeded()
        }
    }

    private val currentUserFlow = repository.loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else repository.observeUser(userId)
    }

    private val userLogsFlow = repository.loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else repository.observeLogs(userId)
    }

    private val cycleSummariesFlow = repository.loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else repository.observeCycleSummaries(userId)
    }

    private val modelWeightsFlow = repository.loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else repository.observeModelWeights(userId)
    }

    private data class CoreBundle(
        val user: UserEntity?,
        val seenOnboarding: Boolean,
        val logs: List<SymptomLogEntity>,
        val summaries: List<CycleSummaryEntity>,
        val weightsEntity: ModelWeightsEntity?
    )

    private val coreBundleFlow = combine(
        currentUserFlow,
        repository.hasSeenOnboardingFlow,
        userLogsFlow,
        cycleSummariesFlow,
        modelWeightsFlow
    ) { user, seenOnboarding, logs, summaries, weightsEntity ->
        CoreBundle(user, seenOnboarding, logs, summaries, weightsEntity)
    }

    val uiState: StateFlow<EndoTrackUiState> = combine(
        coreBundleFlow,
        _authError,
        _isAuthBusy,
        _timeframeDays,
        _clinicianNote,
        _statusBanner
    ) { values ->
        val core = values[0] as CoreBundle
        val authErr = values[1] as String?
        val authBusy = values[2] as Boolean
        val timeframe = values[3] as Int
        val note = values[4] as String
        val banner = values[5] as String?

        val user = core.user
        val sessionStatus = if (user == null) SessionStatus.LoggedOut else SessionStatus.LoggedIn(user)
        val weights = EndoLogisticRegressionModel.weightsFromCsv(core.weightsEntity?.weightsCsv)
        val risk = if (user != null) {
            EndoLogisticRegressionModel.predictMultiCycleRisk(
                baselineHistoryFlags = user.baselineHistorySet(),
                logs = core.logs,
                weights = weights,
                isPersonalized = (core.weightsEntity?.calibrationUpdatesCount ?: 0) > 0
            )
        } else {
            EndoTrackUiState().multiCycleRisk
        }

        EndoTrackUiState(
            sessionStatus = sessionStatus,
            hasSeenOnboarding = core.seenOnboarding,
            authError = authErr,
            isAuthBusy = authBusy,
            logs = core.logs,
            cycleSummaries = core.summaries,
            modelWeightsEntity = core.weightsEntity,
            multiCycleRisk = risk,
            timeframeDays = timeframe,
            clinicianNote = note,
            statusBannerMessage = banner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EndoTrackUiState()
    )

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authError.value = null
            val result = repository.signIn(email, password)
            result.onFailure { err ->
                _authError.value = err.message ?: "Unable to sign in."
            }
            _isAuthBusy.value = false
        }
    }

    fun signInAsDemo() {
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authError.value = null
            val result = repository.signInDemoUser()
            result.onFailure { err ->
                _authError.value = err.message ?: "Unable to load demo account."
            }
            _isAuthBusy.value = false
        }
    }

    fun createAccount(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authError.value = null
            val result = repository.createAccount(fullName, email, password)
            result.onFailure { err ->
                _authError.value = err.message ?: "Unable to create account."
            }
            _isAuthBusy.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _logDraft.value = LogDraftState()
        }
    }

    fun saveClinicalProfile(
        fullName: String,
        ageRange: String,
        ageNumeric: Int,
        cycleLengthDays: Int,
        periodDurationDays: Int,
        journeyReason: String,
        helpGoals: Set<String>,
        baselineHistory: Set<String>,
        lastPeriodStartEpochDay: Long
    ) {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        viewModelScope.launch {
            val updated = currentUser.copy(
                fullName = fullName.trim().ifEmpty { currentUser.fullName },
                ageRange = ageRange,
                ageNumeric = ageNumeric,
                cycleLengthDays = cycleLengthDays.coerceIn(21, 45),
                periodDurationDays = periodDurationDays.coerceIn(2, 10),
                journeyReason = journeyReason,
                helpGoalsCsv = helpGoals.joinToString("|"),
                baselineHistoryCsv = baselineHistory.joinToString("|"),
                lastPeriodStartEpochDay = lastPeriodStartEpochDay,
                hasCompletedProfile = true,
                acceptedDisclaimer = true
            )
            repository.updateClinicalProfile(updated)
            _statusBanner.value = "Clinical profile updated and synced with on-device model."
        }
    }

    fun openLogForDay(epochDay: Long) {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user
        val baselineFlags = currentUser?.baselineHistorySet().orEmpty()

        viewModelScope.launch {
            val existing = if (currentUser != null) {
                repository.getLogForDay(currentUser.id, epochDay)
            } else null

            if (existing != null) {
                val decodedSymptoms = EndoLogisticRegressionModel
                    .decodeSelectedFeatureNames(existing.featureVectorBinary)
                    .toSet()
                _logDraft.value = LogDraftState(
                    epochDay = epochDay,
                    painLevel = existing.painLevel,
                    hasPeriod = existing.hasPeriod,
                    flowVolume = if (existing.flowVolume == "NONE") "MEDIUM" else existing.flowVolume,
                    tookOtcMedication = existing.otcMedicationName.isNotBlank(),
                    otcMedicationName = existing.otcMedicationName,
                    otcRelievedPain = if (existing.otcRelievedPain == "NONE") "PARTIAL" else existing.otcRelievedPain,
                    impactFlags = existing.impactFlagsSet(),
                    notes = existing.notes,
                    selectedSymptoms = decodedSymptoms + baselineFlags,
                    existingLogId = existing.id,
                    expandedCategories = setOf(SymptomCategory.PELVIC_MENSTRUAL_PAIN)
                )
            } else {
                _logDraft.value = LogDraftState(
                    epochDay = epochDay,
                    painLevel = 3,
                    hasPeriod = false,
                    flowVolume = "MEDIUM",
                    tookOtcMedication = false,
                    otcMedicationName = "",
                    otcRelievedPain = "PARTIAL",
                    impactFlags = emptySet(),
                    notes = "",
                    selectedSymptoms = baselineFlags,
                    existingLogId = null,
                    expandedCategories = setOf(SymptomCategory.PELVIC_MENSTRUAL_PAIN)
                )
            }
        }
    }

    fun updatePainLevel(level: Int) {
        _logDraft.update { it.copy(painLevel = level.coerceIn(0, 10)) }
    }

    fun updateHasPeriod(hasPeriod: Boolean) {
        _logDraft.update { it.copy(hasPeriod = hasPeriod) }
    }

    fun updateFlowVolume(volume: String) {
        _logDraft.update { it.copy(flowVolume = volume, hasPeriod = true) }
    }

    fun updateTookOtcMedication(took: Boolean) {
        _logDraft.update {
            it.copy(
                tookOtcMedication = took,
                otcMedicationName = if (took && it.otcMedicationName.isBlank()) "Ibuprofen 400mg" else if (!took) "" else it.otcMedicationName
            )
        }
    }

    fun updateOtcMedicationName(name: String) {
        _logDraft.update { it.copy(otcMedicationName = name) }
    }

    fun updateOtcRelievedPain(relieved: String) {
        _logDraft.update { it.copy(otcRelievedPain = relieved) }
    }

    fun toggleImpactFlag(flag: String) {
        _logDraft.update { draft ->
            val next = if (flag in draft.impactFlags) draft.impactFlags - flag else draft.impactFlags + flag
            draft.copy(impactFlags = next)
        }
    }

    fun updateNotes(notes: String) {
        _logDraft.update { it.copy(notes = notes) }
    }

    fun updateSearchQuery(query: String) {
        _logDraft.update { it.copy(searchQuery = query) }
    }

    fun toggleCategoryExpanded(category: SymptomCategory) {
        _logDraft.update { draft ->
            val next = if (category in draft.expandedCategories) {
                draft.expandedCategories - category
            } else {
                draft.expandedCategories + category
            }
            draft.copy(expandedCategories = next)
        }
    }

    fun toggleSymptom(symptomName: String) {
        _logDraft.update { draft ->
            val next = if (symptomName in draft.selectedSymptoms) {
                draft.selectedSymptoms - symptomName
            } else {
                draft.selectedSymptoms + symptomName
            }
            draft.copy(selectedSymptoms = next)
        }
    }

    fun clearDraft() {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user
        val baselineFlags = currentUser?.baselineHistorySet().orEmpty()
        _logDraft.update {
            LogDraftState(
                epochDay = it.epochDay,
                painLevel = 0,
                selectedSymptoms = baselineFlags,
                expandedCategories = it.expandedCategories
            )
        }
    }

    fun dismissSavedFeedbackDialog() {
        _logDraft.update { it.copy(lastSavedRiskResult = null) }
    }

    fun saveDailyLog() {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        val draft = _logDraft.value

        viewModelScope.launch {
            _logDraft.update { it.copy(isSaving = true) }
            val result = repository.saveSymptomLog(
                user = currentUser,
                epochDay = draft.epochDay,
                painLevel = draft.painLevel,
                hasPeriod = draft.hasPeriod,
                flowVolume = draft.flowVolume,
                otcMedicationName = if (draft.tookOtcMedication) draft.otcMedicationName else "",
                otcRelievedPain = draft.otcRelievedPain,
                impactFlags = draft.impactFlags,
                notes = draft.notes,
                selectedSymptoms = draft.selectedSymptoms
            )
            _logDraft.update {
                it.copy(
                    isSaving = false,
                    lastSavedRiskResult = result
                )
            }
        }
    }

    fun deleteCurrentDraftLog() {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        val logId = _logDraft.value.existingLogId ?: return
        val day = _logDraft.value.epochDay
        viewModelScope.launch {
            repository.deleteSymptomLog(currentUser.id, logId)
            openLogForDay(day)
            _statusBanner.value = "Daily entry removed."
        }
    }

    fun setTimeframeDays(days: Int) {
        _timeframeDays.value = days
    }

    fun updateClinicianNote(note: String) {
        _clinicianNote.value = note
    }

    fun calibrateModelWithOutcome(confirmedEndometriosis: Boolean) {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        viewModelScope.launch {
            val updated = repository.calibrateModelWithOutcome(currentUser, confirmedEndometriosis)
            val pct = (updated.aggregatedProbability * 100.0).toInt()
            val outcomeLabel = if (confirmedEndometriosis) "Confirmed Endometriosis" else "Ruled Out"
            _statusBanner.value = "SGD Calibration applied ($outcomeLabel). Updated score: $pct%."
        }
    }

    fun resetModelToBaseline() {
        val currentUser = (uiState.value.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        viewModelScope.launch {
            val updated = repository.resetModelToBaseline(currentUser)
            val pct = (updated.aggregatedProbability * 100.0).toInt()
            _statusBanner.value = "Reset to 886-patient clinical baseline weights ($pct%)."
        }
    }

    fun exportClinicianPdf(context: Context) {
        val state = uiState.value
        val user = (state.sessionStatus as? SessionStatus.LoggedIn)?.user ?: return
        val result = ClinicianPdfExporter.generateAndShareReport(
            context = context,
            user = user,
            logs = state.logs,
            riskResult = state.multiCycleRisk,
            clinicianNote = state.clinicianNote,
            timeframeDays = state.timeframeDays
        )
        result.onSuccess {
            _statusBanner.value = "Clinician triage PDF generated & ready to share."
        }.onFailure { err ->
            _statusBanner.value = "PDF export failed: ${err.localizedMessage ?: "Unknown error"}"
        }
    }
}
