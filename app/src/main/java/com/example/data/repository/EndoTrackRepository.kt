package com.example.data.repository

import com.example.data.local.DemoDataSeeder
import com.example.data.local.dao.EndoTrackDao
import com.example.data.local.entity.CycleSummaryEntity
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.data.preferences.SessionPreferences
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import com.example.util.DateUtil
import com.example.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Auth readiness contract allowing local offline Room+DataStore authentication
 * with optional future Firebase Auth drop-in adapter.
 */
interface EndoAuthGateway {
    suspend fun signIn(email: String, password: String): Result<UserEntity>
    suspend fun createAccount(fullName: String, email: String, password: String): Result<UserEntity>
    suspend fun signOut()
}

class EndoTrackRepository(
    private val dao: EndoTrackDao,
    private val prefs: SessionPreferences
) : EndoAuthGateway {

    val loggedInUserIdFlow: Flow<Long?> = prefs.loggedInUserIdFlow
    val hasSeenOnboardingFlow: Flow<Boolean> = prefs.hasSeenOnboardingFlow

    suspend fun ensureDemoSeeded() {
        val seeded = prefs.isDemoSeededFlow.first()
        if (!seeded) {
            val demoUserId = DemoDataSeeder.seedDemoUserIfNeeded(dao)
            rebuildCycleSummaries(demoUserId)
            prefs.setDemoSeeded(true)
        }
    }

    suspend fun markOnboardingSeen() {
        prefs.setHasSeenOnboarding(true)
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.getUserByIdFlow(userId)

    fun observeLogs(userId: Long): Flow<List<SymptomLogEntity>> = dao.getLogsForUserFlow(userId)

    fun observeCycleSummaries(userId: Long): Flow<List<CycleSummaryEntity>> =
        dao.getCycleSummariesFlow(userId)

    fun observeModelWeights(userId: Long): Flow<ModelWeightsEntity?> =
        dao.getWeightsForUserFlow(userId)

    fun observeWeightsArray(userId: Long): Flow<DoubleArray> =
        dao.getWeightsForUserFlow(userId).map { entity ->
            EndoLogisticRegressionModel.weightsFromCsv(entity?.weightsCsv)
        }

    override suspend fun signIn(email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your email and password."))
        }
        val user = dao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for $cleanEmail."))
        if (!PasswordHasher.verify(password, cleanEmail, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        prefs.setLoggedInUserId(user.id)
        prefs.setHasSeenOnboarding(true)
        return Result.success(user)
    }

    suspend fun signInDemoUser(): Result<UserEntity> {
        val userId = DemoDataSeeder.seedDemoUserIfNeeded(dao)
        rebuildCycleSummaries(userId)
        val user = dao.getUserByIdOnce(userId)
            ?: return Result.failure(IllegalStateException("Demo profile unavailable."))
        prefs.setDemoSeeded(true)
        prefs.setLoggedInUserId(user.id)
        prefs.setHasSeenOnboarding(true)
        return Result.success(user)
    }

    override suspend fun createAccount(
        fullName: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your name."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (dao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        val today = DateUtil.todayEpochDay()
        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = PasswordHasher.hash(password, cleanEmail),
            fullName = cleanName,
            lastPeriodStartEpochDay = today - 6L,
            hasCompletedProfile = false
        )
        val newId = dao.insertUser(newUser)
        dao.upsertWeights(
            ModelWeightsEntity(
                userId = newId,
                weightsCsv = EndoLogisticRegressionModel.weightsToCsv(
                    EndoLogisticRegressionModel.BASELINE_WEIGHTS
                ),
                calibrationUpdatesCount = 0,
                lastCalibrationOutcome = "Clinical Baseline (886-Patient Model)"
            )
        )
        prefs.setLoggedInUserId(newId)
        prefs.setHasSeenOnboarding(true)
        return Result.success(newUser.copy(id = newId))
    }

    override suspend fun signOut() {
        prefs.setLoggedInUserId(null)
    }

    suspend fun updateClinicalProfile(updatedUser: UserEntity) {
        dao.updateUser(updatedUser.copy(hasCompletedProfile = true))
        rebuildCycleSummaries(updatedUser.id)
    }

    suspend fun getLogForDay(userId: Long, epochDay: Long): SymptomLogEntity? =
        dao.getLogForDay(userId, epochDay)

    suspend fun saveSymptomLog(
        user: UserEntity,
        epochDay: Long,
        painLevel: Int,
        hasPeriod: Boolean,
        flowVolume: String,
        otcMedicationName: String,
        otcRelievedPain: String,
        impactFlags: Set<String>,
        notes: String,
        selectedSymptoms: Set<String>
    ): MultiCycleRiskResult {
        val weightsEntity = dao.getWeightsForUserOnce(user.id)
        val weights = EndoLogisticRegressionModel.weightsFromCsv(weightsEntity?.weightsCsv)
        val baselineFlags = user.baselineHistorySet()

        val featureVector = EndoLogisticRegressionModel.encodeBinaryVector(
            selectedSymptoms = selectedSymptoms,
            baselineHistoryFlags = baselineFlags
        )
        val dailyPrediction = EndoLogisticRegressionModel.predictDailyRisk(featureVector, weights)

        val cycleDay = DateUtil.calculateCycleDay(
            epochDay = epochDay,
            lastPeriodStartEpochDay = user.lastPeriodStartEpochDay,
            cycleLengthDays = user.cycleLengthDays
        )
        val cycleNumber = DateUtil.calculateCycleNumber(
            epochDay = epochDay,
            lastPeriodStartEpochDay = user.lastPeriodStartEpochDay,
            cycleLengthDays = user.cycleLengthDays
        )

        val existing = dao.getLogForDay(user.id, epochDay)
        val logEntity = SymptomLogEntity(
            id = existing?.id ?: 0L,
            userId = user.id,
            epochDay = epochDay,
            cycleNumber = cycleNumber,
            cycleDay = cycleDay,
            painLevel = painLevel,
            hasPeriod = hasPeriod,
            flowVolume = if (hasPeriod) flowVolume else "NONE",
            otcMedicationName = otcMedicationName.trim(),
            otcRelievedPain = if (otcMedicationName.isBlank()) "NONE" else otcRelievedPain,
            impactFlagsCsv = impactFlags.joinToString("|"),
            notes = notes.trim(),
            featureVectorBinary = EndoLogisticRegressionModel.encodeBinaryString(featureVector),
            dailyRiskProbability = dailyPrediction.probability,
            createdAtMillis = System.currentTimeMillis()
        )
        dao.upsertLog(logEntity)
        rebuildCycleSummaries(user.id)

        val allLogs = dao.getLogsForUserOnce(user.id)
        return EndoLogisticRegressionModel.predictMultiCycleRisk(
            baselineHistoryFlags = baselineFlags,
            logs = allLogs,
            weights = weights,
            isPersonalized = (weightsEntity?.calibrationUpdatesCount ?: 0) > 0
        )
    }

    suspend fun deleteSymptomLog(userId: Long, logId: Long) {
        dao.deleteLogById(logId)
        rebuildCycleSummaries(userId)
    }

    suspend fun calibrateModelWithOutcome(
        user: UserEntity,
        confirmedEndometriosis: Boolean
    ): MultiCycleRiskResult {
        val logs = dao.getLogsForUserOnce(user.id)
        val weightsEntity = dao.getWeightsForUserOnce(user.id)
        val currentWeights = EndoLogisticRegressionModel.weightsFromCsv(weightsEntity?.weightsCsv)
        val baselineFlags = user.baselineHistorySet()

        val calibrationVec = EndoLogisticRegressionModel.buildCalibrationFeatureVector(
            baselineHistoryFlags = baselineFlags,
            logs = logs
        )
        val label = if (confirmedEndometriosis) 1.0 else 0.0
        val updatedWeights = EndoLogisticRegressionModel.updateWeightsWithFeedback(
            currentWeights = currentWeights,
            features = calibrationVec,
            actualLabel = label
        )

        val newCount = (weightsEntity?.calibrationUpdatesCount ?: 0) + 1
        val outcomeLabel = if (confirmedEndometriosis) {
            "Confirmed Endometriosis (+SGD)"
        } else {
            "Clinically Ruled Out (-SGD)"
        }

        dao.upsertWeights(
            ModelWeightsEntity(
                userId = user.id,
                weightsCsv = EndoLogisticRegressionModel.weightsToCsv(updatedWeights),
                calibrationUpdatesCount = newCount,
                lastCalibrationOutcome = outcomeLabel,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        )

        // Update stored daily probabilities with personalized weights
        logs.forEach { log ->
            val vec = EndoLogisticRegressionModel.decodeBinaryString(log.featureVectorBinary)
            val newDailyProb = EndoLogisticRegressionModel.predictDailyRisk(vec, updatedWeights).probability
            dao.upsertLog(log.copy(dailyRiskProbability = newDailyProb))
        }
        rebuildCycleSummaries(user.id)

        return EndoLogisticRegressionModel.predictMultiCycleRisk(
            baselineHistoryFlags = baselineFlags,
            logs = dao.getLogsForUserOnce(user.id),
            weights = updatedWeights,
            isPersonalized = true
        )
    }

    suspend fun resetModelToBaseline(user: UserEntity): MultiCycleRiskResult {
        val baselineWeights = EndoLogisticRegressionModel.BASELINE_WEIGHTS
        dao.upsertWeights(
            ModelWeightsEntity(
                userId = user.id,
                weightsCsv = EndoLogisticRegressionModel.weightsToCsv(baselineWeights),
                calibrationUpdatesCount = 0,
                lastCalibrationOutcome = "Clinical Baseline (886-Patient Model)",
                lastUpdatedMillis = System.currentTimeMillis()
            )
        )
        val logs = dao.getLogsForUserOnce(user.id)
        logs.forEach { log ->
            val vec = EndoLogisticRegressionModel.decodeBinaryString(log.featureVectorBinary)
            val dailyProb = EndoLogisticRegressionModel.predictDailyRisk(vec, baselineWeights).probability
            dao.upsertLog(log.copy(dailyRiskProbability = dailyProb))
        }
        rebuildCycleSummaries(user.id)
        return EndoLogisticRegressionModel.predictMultiCycleRisk(
            baselineHistoryFlags = user.baselineHistorySet(),
            logs = dao.getLogsForUserOnce(user.id),
            weights = baselineWeights,
            isPersonalized = false
        )
    }

    private suspend fun rebuildCycleSummaries(userId: Long) {
        val user = dao.getUserByIdOnce(userId) ?: return
        val logs = dao.getLogsForUserOnce(userId)
        val weightsEntity = dao.getWeightsForUserOnce(userId)
        val weights = EndoLogisticRegressionModel.weightsFromCsv(weightsEntity?.weightsCsv)
        val baselineFlags = user.baselineHistorySet()

        dao.deleteCycleSummariesForUser(userId)
        if (logs.isEmpty()) return

        logs.groupBy { it.cycleNumber }.forEach { (cycleNumber, cycleLogs) ->
            val sorted = cycleLogs.sortedBy { it.epochDay }
            val avgPain = sorted.map { it.painLevel }.average()
            val peakPain = sorted.maxOf { it.painLevel }
            val bleedingDays = sorted.count { it.hasPeriod }
            val risk = EndoLogisticRegressionModel.predictMultiCycleRisk(
                baselineHistoryFlags = baselineFlags,
                logs = sorted,
                weights = weights,
                isPersonalized = (weightsEntity?.calibrationUpdatesCount ?: 0) > 0
            )
            val topDriverNames = risk.topDrivers.take(3).joinToString(", ") { it.featureName }
            dao.upsertCycleSummary(
                CycleSummaryEntity(
                    userId = userId,
                    cycleNumber = cycleNumber,
                    startEpochDay = sorted.first().epochDay,
                    endEpochDay = sorted.last().epochDay,
                    lengthDays = user.cycleLengthDays,
                    averagePain = avgPain,
                    peakPain = peakPain,
                    bleedingDays = bleedingDays,
                    aggregatedRiskProbability = risk.aggregatedProbability,
                    topDriverSummary = topDriverNames.ifEmpty { "None flagged" }
                )
            )
        }
    }
}
