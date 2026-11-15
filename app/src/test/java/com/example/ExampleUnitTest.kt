package com.example

import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.RiskTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp

class ExampleUnitTest {

    @Test
    fun modelHasExact58FeaturesAndWeights() {
        assertEquals(58, EndoLogisticRegressionModel.FEATURE_NAMES.size)
        assertEquals(58, EndoLogisticRegressionModel.BASELINE_WEIGHTS.size)
        val totalCategoryFeatures = EndoLogisticRegressionModel.CATEGORY_FEATURES.values.sumOf { it.size }
        assertEquals(58, totalCategoryFeatures)
    }

    @Test
    fun zeroVectorProducesBaselineInterceptProbability() {
        val zeros = DoubleArray(58)
        val result = EndoLogisticRegressionModel.predictDailyRisk(zeros)
        val expected = 1.0 / (1.0 + exp(-EndoLogisticRegressionModel.INTERCEPT))
        assertEquals(expected, result.probability, 1e-6)
        assertEquals(RiskTier.LOW, result.riskTier)
        assertEquals(0, result.activeFeatureCount)
    }

    @Test
    fun highRiskClusterProducesHighProbabilityAndTopDrivers() {
        val symptoms = setOf(
            "Menstrual pain (Dysmenorrhea)",
            "Pelvic pain",
            "Fatigue / Chronic fatigue",
            "Ovarian cysts",
            "Painful bowel movements",
            "Painful / Burning pain during sex (Dyspareunia)"
        )
        val vec = EndoLogisticRegressionModel.encodeBinaryVector(symptoms)
        val result = EndoLogisticRegressionModel.predictDailyRisk(vec)
        assertTrue(result.probability > 0.80)
        assertEquals(RiskTier.HIGH, result.riskTier)
        assertEquals(4, result.topDrivers.size)
        assertEquals("Ovarian cysts", result.topDrivers.first().featureName)
    }

    @Test
    fun sgdWeightUpdateMovesPredictionTowardLabel() {
        val symptoms = setOf("Pelvic pain", "Cramping", "Bloating")
        val vec = EndoLogisticRegressionModel.encodeBinaryVector(symptoms)
        val initialProb = EndoLogisticRegressionModel.predictDailyRisk(vec).probability

        val updatedWeights = EndoLogisticRegressionModel.updateWeightsWithFeedback(
            currentWeights = EndoLogisticRegressionModel.BASELINE_WEIGHTS,
            features = vec,
            actualLabel = 1.0,
            learningRate = 0.2
        )
        val updatedProb = EndoLogisticRegressionModel.predictDailyRisk(vec, updatedWeights).probability
        assertTrue(updatedProb > initialProb)
    }
}
