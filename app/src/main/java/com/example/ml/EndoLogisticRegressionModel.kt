package com.example.ml

import com.example.data.local.entity.SymptomLogEntity
import kotlin.math.exp

enum class SymptomCategory(
    val id: String,
    val title: String,
    val shortTitle: String,
    val description: String
) {
    PELVIC_MENSTRUAL_PAIN(
        id = "A",
        title = "Category A — Pelvic & Menstrual Pain",
        shortTitle = "Pelvic & Pain",
        description = "Localized cramping, ovulation, and chronic pelvic pain"
    ),
    FLOW_BLEEDING(
        id = "B",
        title = "Category B — Menstrual Flow & Bleeding",
        shortTitle = "Flow & Bleeding",
        description = "Bleeding intensity, clots, cycle length & irregularity"
    ),
    INTIMACY_INTERCOURSE(
        id = "C",
        title = "Category C — Intimacy & Intercourse",
        shortTitle = "Intimacy",
        description = "Pain during or after intercourse and deep cramping"
    ),
    BOWEL_BLADDER_GI(
        id = "D",
        title = "Category D — Bowel, Bladder & GI",
        shortTitle = "Bowel & GI",
        description = "GI distress, bloating, IBS overlap, and urinary pain"
    ),
    FATIGUE_BODY_NEURO(
        id = "E",
        title = "Category E — Fatigue, Body Pain & Neurological",
        shortTitle = "Fatigue & Body",
        description = "Exhaustion, radiating back/hip/leg pain, migraines & nausea"
    ),
    HORMONAL_MOOD_SYSTEMIC(
        id = "F",
        title = "Category F — Hormonal, Mood & Systemic",
        shortTitle = "Hormonal & Mood",
        description = "Clinical history flags, mood, sleep, and systemic markers"
    )
}

enum class RiskTier(val label: String, val clinicalSummary: String) {
    LOW(
        label = "Low Pattern Signal",
        clinicalSummary = "Current logged features show low overlap with high-weight endometriosis clusters."
    ),
    MODERATE(
        label = "Moderate Pattern Flag",
        clinicalSummary = "Recurring multi-system symptom clusters warrant structured cycle monitoring."
    ),
    HIGH(
        label = "High Pattern Flag",
        clinicalSummary = "Strong multi-cycle alignment with clinical endometriosis markers. Consider sharing this summary with an OB-GYN."
    )
}

data class PatternDriver(
    val featureIndex: Int,
    val featureName: String,
    val category: SymptomCategory,
    val weight: Double,
    val activation: Double,
    val contribution: Double,
    val frequencyRatio: Double = 1.0
)

data class DailyRiskResult(
    val probability: Double,
    val riskTier: RiskTier,
    val logit: Double,
    val topDrivers: List<PatternDriver>,
    val activeFeatureCount: Int
)

data class MultiCycleRiskResult(
    val aggregatedProbability: Double,
    val latestDailyProbability: Double,
    val riskTier: RiskTier,
    val topDrivers: List<PatternDriver>,
    val totalLogsAnalyzed: Int,
    val cyclesAnalyzed: Int,
    val recurringSymptomCount: Int,
    val isPersonalizedModel: Boolean
)

/**
 * Embedded Logistic Regression Engine trained on an 886-patient clinical endometriosis dataset.
 * Executes 100% on-device with exact 58-feature vector ordering, multi-cycle recurrence aggregation,
 * and online Stochastic Gradient Descent (SGD) calibration from clinician-verified outcomes.
 */
object EndoLogisticRegressionModel {

    const val INTERCEPT: Double = -3.893212

    val FEATURE_NAMES: List<String> = listOf(
        "Heavy / Extreme menstrual bleeding",
        "Menstrual pain (Dysmenorrhea)",
        "Painful / Burning pain during sex (Dyspareunia)",
        "Pelvic pain",
        "Irregular / Missed periods",
        "Cramping",
        "Abdominal pain / pressure",
        "Back pain",
        "Painful bowel movements",
        "Nausea",
        "Menstrual clots",
        "Infertility",
        "Painful cramps during period",
        "Pain / Chronic pain",
        "Diarrhea",
        "Long menstruation",
        "Constipation / Chronic constipation",
        "Vomiting / constant vomiting",
        "Fatigue / Chronic fatigue",
        "Painful ovulation",
        "Stomach cramping",
        "Migraines",
        "Extreme / Severe pain",
        "Leg pain",
        "Irritable Bowel Syndrome (IBS)",
        "Syncope (fainting, passing out)",
        "Mood swings",
        "Depression",
        "Bleeding",
        "Lower back pain",
        "Fertility Issues",
        "Ovarian cysts",
        "Painful urination",
        "Headaches",
        "Constant bleeding",
        "Pain after Intercourse",
        "Digestive / GI problems",
        "IBS-like symptoms",
        "Excessive bleeding",
        "Anaemia / Iron deficiency",
        "Hip pain",
        "Vaginal Pain/Pressure",
        "Sharp / Stabbing pain",
        "Bowel pain",
        "Anxiety",
        "Cysts (unspecified)",
        "Dizziness",
        "Malaise / Sickness",
        "Abnormal uterine bleeding",
        "Fever",
        "Hormonal problems",
        "Bloating",
        "Feeling sick",
        "Decreased energy / Exhaustion",
        "Abdominal Cramps during Intercourse",
        "Insomnia / Sleeplessness",
        "Acne / pimples",
        "Loss of appetite"
    )

    val BASELINE_WEIGHTS: DoubleArray = doubleArrayOf(
        0.300594, 1.622982, 0.801102, 0.887031, 1.158049, 0.549979, 0.53697, 0.455146,
        1.3171, 0.378872, -0.036302, 1.137858, 1.313789, 0.494088, -0.105146, -0.585763,
        0.759938, -0.890351, 1.751311, -0.903647, 0.057626, -0.153492, 0.747968, -0.195847,
        0.537324, -0.081584, -0.232243, -0.016206, 1.584487, 0.475274, -1.118124, 1.757803,
        0.168983, -0.562574, -1.260077, 0.054397, 0.032629, 1.389936, 0.220111, 0.069915,
        0.162713, -0.850081, 0.130386, -1.503701, 0.407295, 1.235638, -0.524618, -1.183341,
        -1.880808, -1.677725, 0.90951, 0.588219, -0.652598, 0.260948, 0.65612, -0.055391,
        0.103288, -1.095229
    )

    val BASELINE_CLINICAL_HISTORY_FEATURES: List<String> = listOf(
        "Ovarian cysts",
        "Cysts (unspecified)",
        "Infertility",
        "Fertility Issues",
        "Irritable Bowel Syndrome (IBS)",
        "Anaemia / Iron deficiency",
        "Hormonal problems"
    )

    val CATEGORY_FEATURES: Map<SymptomCategory, List<String>> = mapOf(
        SymptomCategory.PELVIC_MENSTRUAL_PAIN to listOf(
            "Menstrual pain (Dysmenorrhea)",
            "Pelvic pain",
            "Painful cramps during period",
            "Cramping",
            "Abdominal pain / pressure",
            "Extreme / Severe pain",
            "Pain / Chronic pain",
            "Sharp / Stabbing pain",
            "Stomach cramping",
            "Painful ovulation",
            "Vaginal Pain/Pressure"
        ),
        SymptomCategory.FLOW_BLEEDING to listOf(
            "Heavy / Extreme menstrual bleeding",
            "Bleeding",
            "Excessive bleeding",
            "Long menstruation",
            "Irregular / Missed periods",
            "Menstrual clots",
            "Constant bleeding",
            "Abnormal uterine bleeding"
        ),
        SymptomCategory.INTIMACY_INTERCOURSE to listOf(
            "Painful / Burning pain during sex (Dyspareunia)",
            "Pain after Intercourse",
            "Abdominal Cramps during Intercourse"
        ),
        SymptomCategory.BOWEL_BLADDER_GI to listOf(
            "Painful bowel movements",
            "Bloating",
            "IBS-like symptoms",
            "Irritable Bowel Syndrome (IBS)",
            "Constipation / Chronic constipation",
            "Diarrhea",
            "Bowel pain",
            "Digestive / GI problems",
            "Painful urination"
        ),
        SymptomCategory.FATIGUE_BODY_NEURO to listOf(
            "Fatigue / Chronic fatigue",
            "Decreased energy / Exhaustion",
            "Back pain",
            "Lower back pain",
            "Hip pain",
            "Leg pain",
            "Headaches",
            "Migraines",
            "Dizziness",
            "Syncope (fainting, passing out)",
            "Nausea",
            "Vomiting / constant vomiting",
            "Feeling sick",
            "Malaise / Sickness"
        ),
        SymptomCategory.HORMONAL_MOOD_SYSTEMIC to listOf(
            "Ovarian cysts",
            "Cysts (unspecified)",
            "Infertility",
            "Fertility Issues",
            "Hormonal problems",
            "Anaemia / Iron deficiency",
            "Mood swings",
            "Anxiety",
            "Depression",
            "Insomnia / Sleeplessness",
            "Loss of appetite",
            "Acne / pimples",
            "Fever"
        )
    )

    private val FEATURE_INDEX_MAP: Map<String, Int> =
        FEATURE_NAMES.withIndex().associate { (idx, name) -> name to idx }

    private val FEATURE_CATEGORY_MAP: Map<String, SymptomCategory> = buildMap {
        CATEGORY_FEATURES.forEach { (category, names) ->
            names.forEach { put(it, category) }
        }
    }

    init {
        require(FEATURE_NAMES.size == 58) { "Model must contain exactly 58 features." }
        require(BASELINE_WEIGHTS.size == 58) { "Model must contain exactly 58 weights." }
        require(FEATURE_CATEGORY_MAP.size == 58) { "All 58 features must belong to a category." }
    }

    fun indexOf(featureName: String): Int = FEATURE_INDEX_MAP[featureName] ?: -1

    fun categoryOf(featureName: String): SymptomCategory =
        FEATURE_CATEGORY_MAP[featureName] ?: SymptomCategory.HORMONAL_MOOD_SYSTEMIC

    fun sigmoid(logit: Double): Double = 1.0 / (1.0 + exp(-logit))

    fun riskTierFor(probability: Double): RiskTier = when {
        probability >= 0.65 -> RiskTier.HIGH
        probability >= 0.35 -> RiskTier.MODERATE
        else -> RiskTier.LOW
    }

    fun encodeBinaryVector(
        selectedSymptoms: Set<String>,
        baselineHistoryFlags: Set<String> = emptySet()
    ): DoubleArray {
        val vector = DoubleArray(58)
        val combined = selectedSymptoms + baselineHistoryFlags
        combined.forEach { name ->
            val idx = FEATURE_INDEX_MAP[name]
            if (idx != null && idx in 0 until 58) {
                vector[idx] = 1.0
            }
        }
        return vector
    }

    fun encodeBinaryString(vector: DoubleArray): String {
        require(vector.size == 58)
        return buildString(58) {
            for (i in 0 until 58) {
                append(if (vector[i] >= 0.5) '1' else '0')
            }
        }
    }

    fun decodeBinaryString(binary: String): DoubleArray {
        val vector = DoubleArray(58)
        val len = minOf(binary.length, 58)
        for (i in 0 until len) {
            vector[i] = if (binary[i] == '1') 1.0 else 0.0
        }
        return vector
    }

    fun decodeSelectedFeatureNames(binary: String): List<String> {
        val result = mutableListOf<String>()
        val len = minOf(binary.length, 58)
        for (i in 0 until len) {
            if (binary[i] == '1') {
                result.add(FEATURE_NAMES[i])
            }
        }
        return result
    }

    fun weightsToCsv(weights: DoubleArray): String = weights.joinToString(",")

    fun weightsFromCsv(csv: String?): DoubleArray {
        if (csv.isNullOrBlank()) return BASELINE_WEIGHTS.copyOf()
        val parsed = csv.split(",").mapNotNull { it.trim().toDoubleOrNull() }
        return if (parsed.size == 58) parsed.toDoubleArray() else BASELINE_WEIGHTS.copyOf()
    }

    /**
     * Exact Logistic Regression daily inference:
     * probability = 1.0 / (1.0 + exp(-(INTERCEPT + sum(weights[i] * features[i]))))
     */
    fun predictDailyRisk(
        features: DoubleArray,
        weights: DoubleArray = BASELINE_WEIGHTS
    ): DailyRiskResult {
        require(features.size == 58) { "Feature vector must have 58 elements." }
        require(weights.size == 58) { "Weights vector must have 58 elements." }

        var logit = INTERCEPT
        val drivers = mutableListOf<PatternDriver>()
        var activeCount = 0

        for (i in 0 until 58) {
            val x = features[i]
            val w = weights[i]
            val contribution = w * x
            logit += contribution
            if (x > 0.0) {
                activeCount++
                if (contribution > 0.0) {
                    val name = FEATURE_NAMES[i]
                    drivers.add(
                        PatternDriver(
                            featureIndex = i,
                            featureName = name,
                            category = categoryOf(name),
                            weight = w,
                            activation = x,
                            contribution = contribution,
                            frequencyRatio = x
                        )
                    )
                }
            }
        }

        val probability = sigmoid(logit)
        val topDrivers = drivers.sortedByDescending { it.contribution }.take(4)

        return DailyRiskResult(
            probability = probability,
            riskTier = riskTierFor(probability),
            logit = logit,
            topDrivers = topDrivers,
            activeFeatureCount = activeCount
        )
    }

    /**
     * Mode 1: Longitudinal Multi-Cycle Pattern Aggregation.
     * Combines persistent baseline clinical history flags from the user's profile with
     * symptom recurrence frequency across logged cycles and days, then runs sigmoid inference
     * and extracts the top 4 positive w[i] * x[i] contributors as Key Pattern Drivers.
     */
    fun predictMultiCycleRisk(
        baselineHistoryFlags: Set<String>,
        logs: List<SymptomLogEntity>,
        weights: DoubleArray = BASELINE_WEIGHTS,
        isPersonalized: Boolean = false
    ): MultiCycleRiskResult {
        if (logs.isEmpty()) {
            val baselineVec = encodeBinaryVector(emptySet(), baselineHistoryFlags)
            val baselinePrediction = predictDailyRisk(baselineVec, weights)
            return MultiCycleRiskResult(
                aggregatedProbability = baselinePrediction.probability,
                latestDailyProbability = baselinePrediction.probability,
                riskTier = baselinePrediction.riskTier,
                topDrivers = baselinePrediction.topDrivers,
                totalLogsAnalyzed = 0,
                cyclesAnalyzed = 0,
                recurringSymptomCount = baselineHistoryFlags.size,
                isPersonalizedModel = isPersonalized
            )
        }

        val totalLogs = logs.size.toDouble()
        val cycles = logs.map { it.cycleNumber }.distinct()
        val cycleCount = maxOf(1, cycles.size)

        val logCountsPerFeature = IntArray(58)
        val cycleSetsPerFeature = Array(58) { mutableSetOf<Int>() }

        logs.forEach { log ->
            val vec = decodeBinaryString(log.featureVectorBinary)
            for (i in 0 until 58) {
                if (vec[i] >= 0.5) {
                    logCountsPerFeature[i]++
                    cycleSetsPerFeature[i].add(log.cycleNumber)
                }
            }
        }

        val aggregatedVector = DoubleArray(58)
        val frequencyRatios = DoubleArray(58)
        var recurringCount = 0

        for (i in 0 until 58) {
            val featureName = FEATURE_NAMES[i]
            val isBaselineHistory = featureName in baselineHistoryFlags
            val freqRatio = logCountsPerFeature[i] / totalLogs
            val cycleCoverage = cycleSetsPerFeature[i].size.toDouble() / cycleCount.toDouble()
            frequencyRatios[i] = if (isBaselineHistory) 1.0 else freqRatio

            if (isBaselineHistory) {
                aggregatedVector[i] = 1.0
                recurringCount++
            } else if (logCountsPerFeature[i] > 0) {
                // Symptoms recurring across cycles or >= 20% of days activate full pattern signal;
                // sporadic symptoms contribute proportionally so multi-cycle tracking feels responsive.
                val activation = when {
                    cycleCount >= 2 && cycleSetsPerFeature[i].size >= 2 -> 1.0
                    freqRatio >= 0.25 -> 1.0
                    else -> (0.55 * freqRatio + 0.45 * cycleCoverage).coerceIn(0.25, 1.0)
                }
                aggregatedVector[i] = activation
                if (activation >= 0.7) recurringCount++
            }
        }

        var logit = INTERCEPT
        val positiveDrivers = mutableListOf<PatternDriver>()
        for (i in 0 until 58) {
            val x = aggregatedVector[i]
            val w = weights[i]
            val contribution = w * x
            logit += contribution
            if (x > 0.0 && contribution > 0.0) {
                val name = FEATURE_NAMES[i]
                positiveDrivers.add(
                    PatternDriver(
                        featureIndex = i,
                        featureName = name,
                        category = categoryOf(name),
                        weight = w,
                        activation = x,
                        contribution = contribution,
                        frequencyRatio = frequencyRatios[i]
                    )
                )
            }
        }

        val aggregatedProbability = sigmoid(logit)
        val latestLog = logs.maxByOrNull { it.epochDay }
        val latestDailyProb = if (latestLog != null) {
            val combinedVec = decodeBinaryString(latestLog.featureVectorBinary)
            baselineHistoryFlags.forEach { flag ->
                val idx = indexOf(flag)
                if (idx >= 0) combinedVec[idx] = 1.0
            }
            predictDailyRisk(combinedVec, weights).probability
        } else {
            aggregatedProbability
        }

        return MultiCycleRiskResult(
            aggregatedProbability = aggregatedProbability,
            latestDailyProbability = latestDailyProb,
            riskTier = riskTierFor(aggregatedProbability),
            topDrivers = positiveDrivers.sortedByDescending { it.contribution }.take(4),
            totalLogsAnalyzed = logs.size,
            cyclesAnalyzed = cycleCount,
            recurringSymptomCount = recurringCount,
            isPersonalizedModel = isPersonalized
        )
    }

    /**
     * Mode 2: On-Device Incremental Calibration via Online Stochastic Gradient Descent (SGD).
     * Updates weights locally whenever a clinician-verified diagnostic outcome is recorded:
     *   w[i] -= learningRate * (predictedProb - actualLabel) * features[i]
     * where actualLabel = 1.0 (Endometriosis Confirmed) or 0.0 (Ruled Out).
     */
    fun updateWeightsWithFeedback(
        currentWeights: DoubleArray,
        features: DoubleArray,
        actualLabel: Double,
        learningRate: Double = 0.08
    ): DoubleArray {
        require(currentWeights.size == 58) { "Weights must contain 58 elements." }
        require(features.size == 58) { "Features must contain 58 elements." }
        require(actualLabel == 0.0 || actualLabel == 1.0) { "Actual label must be 0.0 or 1.0." }

        val predictedProb = predictDailyRisk(features, currentWeights).probability
        val error = predictedProb - actualLabel
        val updated = currentWeights.copyOf()

        for (i in 0 until 58) {
            if (features[i] != 0.0) {
                updated[i] = updated[i] - learningRate * error * features[i]
            }
        }
        return updated
    }

    /**
     * Aggregates the user's multi-cycle symptom history into a normalized 58-feature vector
     * suitable for on-device SGD calibration.
     */
    fun buildCalibrationFeatureVector(
        baselineHistoryFlags: Set<String>,
        logs: List<SymptomLogEntity>
    ): DoubleArray {
        val vector = DoubleArray(58)
        baselineHistoryFlags.forEach { flag ->
            val idx = indexOf(flag)
            if (idx >= 0) vector[idx] = 1.0
        }
        if (logs.isEmpty()) return vector

        val counts = IntArray(58)
        logs.forEach { log ->
            val vec = decodeBinaryString(log.featureVectorBinary)
            for (i in 0 until 58) {
                if (vec[i] >= 0.5) counts[i]++
            }
        }
        val threshold = maxOf(1, (logs.size * 0.2).toInt())
        for (i in 0 until 58) {
            if (counts[i] >= threshold) {
                vector[i] = 1.0
            }
        }
        return vector
    }
}
