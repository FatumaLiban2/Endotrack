package com.example.data.local

import com.example.data.local.dao.EndoTrackDao
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.util.DateUtil
import com.example.util.PasswordHasher

object DemoDataSeeder {

    const val DEMO_EMAIL = "elena@endotrack.app"
    const val DEMO_PASSWORD = "endotrack123"

    suspend fun seedDemoUserIfNeeded(dao: EndoTrackDao): Long {
        val existing = dao.getUserByEmail(DEMO_EMAIL)
        if (existing != null) {
            return existing.id
        }

        val today = DateUtil.todayEpochDay()
        // Set cycle day so today is Cycle Day 14 (Ovulatory phase) matching clinical demo flow
        val lastPeriodStart = today - 13L
        val baselineFlags = setOf(
            "Ovarian cysts",
            "Irritable Bowel Syndrome (IBS)"
        )

        val demoUser = UserEntity(
            email = DEMO_EMAIL,
            passwordHash = PasswordHasher.hash(DEMO_PASSWORD, DEMO_EMAIL),
            fullName = "Elena",
            ageRange = "25–34",
            ageNumeric = 29,
            cycleLengthDays = 28,
            periodDurationDays = 5,
            journeyReason = "I'm waiting for a diagnosis",
            helpGoalsCsv = listOf(
                "Track my symptoms day by day",
                "Spot patterns across my cycle",
                "Prepare for doctor's appointments"
            ).joinToString("|"),
            baselineHistoryCsv = baselineFlags.joinToString("|"),
            lastPeriodStartEpochDay = lastPeriodStart,
            hasCompletedProfile = true,
            acceptedDisclaimer = true
        )

        val userId = dao.insertUser(demoUser)

        dao.upsertWeights(
            ModelWeightsEntity(
                userId = userId,
                weightsCsv = EndoLogisticRegressionModel.weightsToCsv(
                    EndoLogisticRegressionModel.BASELINE_WEIGHTS
                ),
                calibrationUpdatesCount = 0,
                lastCalibrationOutcome = "Clinical Baseline (886-Patient Model)"
            )
        )

        // Seed 35 days spanning Cycle 1 and Cycle 2, leaving Today unlogged for immediate interactive entry
        data class SeedDaySpec(
            val offsetFromToday: Int,
            val pain: Int,
            val hasPeriod: Boolean,
            val flow: String,
            val med: String,
            val relieved: String,
            val impact: List<String>,
            val symptoms: Set<String>,
            val notes: String
        )

        val specs = listOf(
            // Cycle 1 (days -41 to -14)
            SeedDaySpec(-41, 8, true, "HEAVY", "Ibuprofen 600mg", "NO", listOf("Missed school/work", "Poor sleep", "Hard to move"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Heavy / Extreme menstrual bleeding", "Painful cramps during period", "Fatigue / Chronic fatigue", "Lower back pain", "Nausea", "Menstrual clots"),
                "Severe cramping woke me up at 4am; Ibuprofen 600mg did not relieve pelvic pain."),
            SeedDaySpec(-40, 9, true, "HEAVY", "Ibuprofen 600mg", "NO", listOf("Missed school/work", "Hard to move", "Poor sleep"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Sharp / Stabbing pain", "Extreme / Severe pain", "Heavy / Extreme menstrual bleeding", "Fatigue / Chronic fatigue", "Painful bowel movements", "Bloating", "Dizziness"),
                "Peak flare day. Stabbing lower abdominal pain and painful bowel movements."),
            SeedDaySpec(-39, 7, true, "MEDIUM", "Naproxen 500mg", "PARTIAL", listOf("Hard to move"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Painful cramps during period", "Fatigue / Chronic fatigue", "Lower back pain", "Bloating"),
                "Heating pad + Naproxen took edge off slightly, still fatigued."),
            SeedDaySpec(-38, 5, true, "LIGHT", "Ibuprofen 400mg", "PARTIAL", listOf("Poor sleep"),
                setOf("Pelvic pain", "Cramping", "Fatigue / Chronic fatigue", "Bleeding", "Bloating"),
                "Flow tapering, persistent dull pelvic ache."),
            SeedDaySpec(-37, 3, true, "SPOTTING", "", "NONE", emptyList(),
                setOf("Cramping", "Decreased energy / Exhaustion"),
                "Spotting only, mild fatigue."),
            SeedDaySpec(-34, 2, false, "NONE", "", "NONE", emptyList(),
                setOf("Bloating", "IBS-like symptoms"),
                "Mild GI bloating after lunch."),
            SeedDaySpec(-31, 2, false, "NONE", "", "NONE", emptyList(),
                setOf("Fatigue / Chronic fatigue"),
                "Low energy morning."),
            SeedDaySpec(-28, 5, false, "NONE", "Ibuprofen 400mg", "PARTIAL", listOf("Poor sleep"),
                setOf("Pelvic pain", "Painful ovulation", "Painful / Burning pain during sex (Dyspareunia)", "Abdominal Cramps during Intercourse", "Bloating"),
                "Mid-cycle sharp pain and discomfort during intimacy."),
            SeedDaySpec(-26, 4, false, "NONE", "", "NONE", emptyList(),
                setOf("Pelvic pain", "Pain after Intercourse", "Lower back pain"),
                "Lingering pelvic soreness."),
            SeedDaySpec(-23, 3, false, "NONE", "", "NONE", emptyList(),
                setOf("Bloating", "Constipation / Chronic constipation", "Fatigue / Chronic fatigue"),
                "Constipation and abdominal fullness."),
            SeedDaySpec(-20, 4, false, "NONE", "", "NONE", listOf("Poor sleep"),
                setOf("Lower back pain", "Hip pain", "Insomnia / Sleeplessness", "Mood swings"),
                "Restless sleep, aching left hip."),
            SeedDaySpec(-17, 6, false, "NONE", "Ibuprofen 400mg", "NO", listOf("Poor sleep"),
                setOf("Pelvic pain", "Cramping", "Painful bowel movements", "IBS-like symptoms", "Fatigue / Chronic fatigue", "Migraines"),
                "Pre-menstrual escalation; NSAIDs ineffective."),
            SeedDaySpec(-15, 6, false, "NONE", "Ibuprofen 400mg", "NO", listOf("Hard to move"),
                setOf("Pelvic pain", "Abdominal pain / pressure", "Lower back pain", "Bloating", "Nausea"),
                "Strong pre-period pelvic pressure."),

            // Cycle 2 (lastPeriodStart = -13)
            SeedDaySpec(-13, 8, true, "HEAVY", "Ibuprofen 600mg", "NO", listOf("Missed school/work", "Hard to move", "Poor sleep"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Heavy / Extreme menstrual bleeding", "Painful cramps during period", "Sharp / Stabbing pain", "Fatigue / Chronic fatigue", "Nausea"),
                "Cycle Day 1 flare: sharp stabbing cramps, Ibuprofen 600mg failed to reduce pain."),
            SeedDaySpec(-12, 8, true, "HEAVY", "Ibuprofen 600mg", "NO", listOf("Missed school/work", "Hard to move", "Poor sleep"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Heavy / Extreme menstrual bleeding", "Excessive bleeding", "Painful bowel movements", "Fatigue / Chronic fatigue", "Lower back pain", "Painful urination"),
                "Heavy bleeding with clots, painful urination & bowel pain."),
            SeedDaySpec(-11, 7, true, "MEDIUM", "Naproxen 500mg", "PARTIAL", listOf("Hard to move"),
                setOf("Menstrual pain (Dysmenorrhea)", "Pelvic pain", "Bleeding", "Painful cramps during period", "Lower back pain", "Leg pain", "Fatigue / Chronic fatigue"),
                "Radiating ache down thighs; used heating pad 3 hours."),
            SeedDaySpec(-10, 6, true, "MEDIUM", "Ibuprofen 400mg", "PARTIAL", listOf("Poor sleep"),
                setOf("Pelvic pain", "Bleeding", "Cramping", "Bloating", "IBS-like symptoms", "Decreased energy / Exhaustion"),
                "Moderate cramps and persistent bloating."),
            SeedDaySpec(-9, 4, true, "LIGHT", "", "NONE", emptyList(),
                setOf("Pelvic pain", "Bleeding", "Fatigue / Chronic fatigue"),
                "Lighter flow, lingering pelvic heaviness."),
            SeedDaySpec(-7, 2, false, "NONE", "", "NONE", emptyList(),
                setOf("Fatigue / Chronic fatigue", "Headaches"),
                "Recovering energy."),
            SeedDaySpec(-5, 3, false, "NONE", "", "NONE", emptyList(),
                setOf("Pelvic pain", "Bloating", "Digestive / GI problems"),
                "Mild abdominal pressure."),
            SeedDaySpec(-4, 4, false, "NONE", "Acetaminophen 500mg", "YES", emptyList(),
                setOf("Pelvic pain", "Lower back pain", "Fatigue / Chronic fatigue"),
                "Lower back ache eased after rest."),
            // Day -3 omitted intentionally for realistic 5-of-7 streak like Stitch dashboard!
            SeedDaySpec(-3, 6, false, "NONE", "Ibuprofen 400mg", "NO", listOf("Poor sleep"),
                setOf("Pelvic pain", "Painful / Burning pain during sex (Dyspareunia)", "Sharp / Stabbing pain", "Fatigue / Chronic fatigue", "Bloating"),
                "Dyspareunia flare and bloating."),
            SeedDaySpec(-2, 3, false, "NONE", "", "NONE", emptyList(),
                setOf("Cramping", "Lower back pain", "Fatigue / Chronic fatigue"),
                "Morning stiffness, eased midday."),
            SeedDaySpec(-1, 5, true, "SPOTTING", "Ibuprofen 400mg", "PARTIAL", listOf("Hard to move"),
                setOf("Pelvic pain", "Fatigue / Chronic fatigue", "Lower back pain", "Bloating", "Painful cramps during period"),
                "Mid-cycle spotting and pelvic ache; took Ibuprofen 400mg at 8pm.")
        )

        val currentWeights = EndoLogisticRegressionModel.BASELINE_WEIGHTS
        specs.forEach { spec ->
            val epochDay = today + spec.offsetFromToday
            val cycleNumber = DateUtil.calculateCycleNumber(epochDay, lastPeriodStart, 28)
            val cycleDay = DateUtil.calculateCycleDay(epochDay, lastPeriodStart, 28)
            val vec = EndoLogisticRegressionModel.encodeBinaryVector(spec.symptoms, baselineFlags)
            val prediction = EndoLogisticRegressionModel.predictDailyRisk(vec, currentWeights)

            dao.upsertLog(
                SymptomLogEntity(
                    userId = userId,
                    epochDay = epochDay,
                    cycleNumber = cycleNumber,
                    cycleDay = cycleDay,
                    painLevel = spec.pain,
                    hasPeriod = spec.hasPeriod,
                    flowVolume = spec.flow,
                    otcMedicationName = spec.med,
                    otcRelievedPain = spec.relieved,
                    impactFlagsCsv = spec.impact.joinToString("|"),
                    notes = spec.notes,
                    featureVectorBinary = EndoLogisticRegressionModel.encodeBinaryString(vec),
                    dailyRiskProbability = prediction.probability,
                    createdAtMillis = System.currentTimeMillis() + (spec.offsetFromToday * 86_400_000L)
                )
            )
        }

        return userId
    }
}
