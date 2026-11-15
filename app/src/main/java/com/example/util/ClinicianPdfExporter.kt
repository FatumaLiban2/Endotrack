package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import java.io.File
import java.io.FileOutputStream

object ClinicianPdfExporter {

    data class ReportMetrics(
        val timeframeDays: Int,
        val periodDays: Int,
        val averagePain: Double,
        val peakPain: Int,
        val severeDaysCount: Int,
        val impactDaysCount: Int,
        val otcAttempts: Int,
        val otcNonResponseCount: Int,
        val topSymptoms: List<Pair<String, Int>>
    )

    fun computeMetrics(logs: List<SymptomLogEntity>, timeframeDays: Int): ReportMetrics {
        val today = DateUtil.todayEpochDay()
        val cutoff = today - (timeframeDays - 1)
        val filtered = logs.filter { it.epochDay >= cutoff }
        if (filtered.isEmpty()) {
            return ReportMetrics(
                timeframeDays = timeframeDays,
                periodDays = 0,
                averagePain = 0.0,
                peakPain = 0,
                severeDaysCount = 0,
                impactDaysCount = 0,
                otcAttempts = 0,
                otcNonResponseCount = 0,
                topSymptoms = emptyList()
            )
        }

        val counts = mutableMapOf<String, Int>()
        filtered.forEach { log ->
            EndoLogisticRegressionModel.decodeSelectedFeatureNames(log.featureVectorBinary).forEach { name ->
                counts[name] = (counts[name] ?: 0) + 1
            }
        }

        val otcLogs = filtered.filter { it.otcMedicationName.isNotBlank() }
        val nonResponse = otcLogs.count { it.otcRelievedPain == "NO" || it.otcRelievedPain == "PARTIAL" }

        return ReportMetrics(
            timeframeDays = timeframeDays,
            periodDays = filtered.count { it.hasPeriod },
            averagePain = filtered.map { it.painLevel }.average(),
            peakPain = filtered.maxOf { it.painLevel },
            severeDaysCount = filtered.count { it.painLevel >= 6 },
            impactDaysCount = filtered.count { it.impactFlagsSet().isNotEmpty() },
            otcAttempts = otcLogs.size,
            otcNonResponseCount = nonResponse,
            topSymptoms = counts.entries
                .sortedByDescending { it.value }
                .take(8)
                .map { it.key to it.value }
        )
    }

    fun generateAndShareReport(
        context: Context,
        user: UserEntity,
        logs: List<SymptomLogEntity>,
        riskResult: MultiCycleRiskResult,
        clinicianNote: String,
        timeframeDays: Int
    ): Result<File> {
        return runCatching {
            val metrics = computeMetrics(logs, timeframeDays)
            val doc = PdfDocument()
            val pageWidth = 595 // A4 width in points (72 dpi)
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val cherryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#9E001F")
            }
            val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFF0F0")
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F5D0D6")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitleWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDAD8")
                textSize = 10f
            }
            val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8E0B23")
                textSize = 12.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2B1215")
                textSize = 10f
            }
            val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2B1215")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6D5558")
                textSize = 9f
            }

            // Header Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 88f, cherryPaint)
            canvas.drawText("EndoTrack — Clinician Triage Summary", 36f, 38f, titlePaint)
            val todayStr = DateUtil.formatFullDate(DateUtil.todayEpochDay())
            canvas.drawText(
                "Patient: ${user.fullName}  |  Age: ${user.ageRange}  |  Cycle: ${user.cycleLengthDays}d (Period ${user.periodDurationDays}d)  |  Generated: $todayStr",
                36f,
                58f,
                subtitleWhitePaint
            )
            canvas.drawText(
                "Window: Last ${metrics.timeframeDays} Days  |  Logs Analyzed: ${riskResult.totalLogsAnalyzed} across ${riskResult.cyclesAnalyzed} cycle(s)",
                36f,
                73f,
                subtitleWhitePaint
            )

            var y = 112f
            val left = 36f
            val right = pageWidth - 36f

            // Section 1: Patient Note & Baseline History
            canvas.drawText("1. PATIENT CONCERN & BASELINE CLINICAL HISTORY", left, y, sectionHeaderPaint)
            y += 10f
            val noteBox = RectF(left, y, right, y + 72f)
            canvas.drawRoundRect(noteBox, 10f, 10f, blushPaint)
            canvas.drawRoundRect(noteBox, 10f, 10f, borderPaint)
            var noteY = y + 18f
            wrapText(clinicianNote, 92).take(3).forEach { line ->
                canvas.drawText(line, left + 12f, noteY, bodyPaint)
                noteY += 13f
            }
            val baselineText = user.baselineHistorySet().ifEmpty { setOf("None reported") }.joinToString(", ")
            canvas.drawText("Baseline History Flags: $baselineText", left + 12f, y + 62f, boldBodyPaint)
            y += 90f

            // Section 2: On-Device AI Pattern Summary
            canvas.drawText("2. ON-DEVICE LOGISTIC REGRESSION PATTERN SIGNAL", left, y, sectionHeaderPaint)
            y += 10f
            val aiBox = RectF(left, y, right, y + 104f)
            canvas.drawRoundRect(aiBox, 10f, 10f, blushPaint)
            canvas.drawRoundRect(aiBox, 10f, 10f, borderPaint)

            val pct = (riskResult.aggregatedProbability * 100.0).toInt()
            val modelMode = if (riskResult.isPersonalizedModel) "Personalized SGD Calibrated" else "886-Patient Clinical Baseline"
            canvas.drawText(
                "Multi-Cycle Pattern Score: $pct% (${riskResult.riskTier.label})  —  Model: $modelMode",
                left + 12f,
                y + 20f,
                boldBodyPaint
            )
            canvas.drawText("Top Positive Pattern Drivers (Weight x Recurrence):", left + 12f, y + 38f, mutedPaint)
            var driverY = y + 54f
            riskResult.topDrivers.take(4).forEachIndexed { idx, driver ->
                val freqPct = (driver.frequencyRatio * 100.0).toInt()
                val text = "${idx + 1}. ${driver.featureName}  (w = ${"%.2f".format(driver.weight)}, contribution = +${"%.2f".format(driver.contribution)}, $freqPct% freq)"
                canvas.drawText(text, left + 16f, driverY, bodyPaint)
                driverY += 13f
            }
            y += 122f

            // Section 3: Clinical Metrics Grid
            canvas.drawText("3. LONGITUDINAL PAIN, BLEEDING & MEDICATION SUMMARY", left, y, sectionHeaderPaint)
            y += 12f
            val cardW = (right - left - 16f) / 3f
            val kpis = listOf(
                Triple("Average / Peak Pain", "${"%.1f".format(metrics.averagePain)} / 10", "Peak: ${metrics.peakPain}/10 (${metrics.severeDaysCount} severe days)"),
                Triple("Functional Impact", "${metrics.impactDaysCount} Days", "Bleeding days logged: ${metrics.periodDays}"),
                Triple("OTC Analgesics", "${metrics.otcAttempts} Logged", "Unrelieved/Partial: ${metrics.otcNonResponseCount}x")
            )
            kpis.forEachIndexed { idx, (label, value, sub) ->
                val x1 = left + idx * (cardW + 8f)
                val rect = RectF(x1, y, x1 + cardW, y + 60f)
                canvas.drawRoundRect(rect, 8f, 8f, blushPaint)
                canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
                canvas.drawText(label, x1 + 10f, y + 16f, mutedPaint)
                canvas.drawText(value, x1 + 10f, y + 35f, sectionHeaderPaint)
                canvas.drawText(sub, x1 + 10f, y + 50f, mutedPaint)
            }
            y += 80f

            // Section 4: Top Recurring Symptoms
            canvas.drawText("4. TOP RECURRING SYMPTOMS (${metrics.timeframeDays}-DAY WINDOW)", left, y, sectionHeaderPaint)
            y += 14f
            if (metrics.topSymptoms.isEmpty()) {
                canvas.drawText("No symptoms recorded in the selected window.", left, y, bodyPaint)
                y += 18f
            } else {
                metrics.topSymptoms.take(6).forEach { (symptom, count) ->
                    canvas.drawText("• $symptom", left + 8f, y, bodyPaint)
                    canvas.drawText("$count log(s)", right - 54f, y, boldBodyPaint)
                    y += 15f
                }
            }
            y += 10f

            // Section 5: Consultation Discussion Prompts
            canvas.drawText("5. SUGGESTED QUESTIONS FOR OB-GYN CONSULTATION", left, y, sectionHeaderPaint)
            y += 15f
            val questions = listOf(
                "• Given recurring pelvic pain and NSAID non-response, could endometriosis be contributing?",
                "• Would a specialized pelvic ultrasound (deep endo protocol) or pelvic MRI be appropriate?",
                "• What hormonal or multidisciplinary pain management options fit my symptom profile?"
            )
            questions.forEach { q ->
                canvas.drawText(q, left + 6f, y, bodyPaint)
                y += 15f
            }

            // Footer Disclaimer
            val footerBox = RectF(left, pageHeight - 64f, right, pageHeight - 20f)
            canvas.drawRoundRect(footerBox, 8f, 8f, blushPaint)
            canvas.drawRoundRect(footerBox, 8f, 8f, borderPaint)
            canvas.drawText(
                "CLINICAL DISCLAIMER: EndoTrack is a pre-diagnostic screening and pattern-recognition aid.",
                left + 10f,
                pageHeight - 46f,
                boldBodyPaint
            )
            canvas.drawText(
                "It does not diagnose endometriosis, prescribe medication, or replace a licensed healthcare professional.",
                left + 10f,
                pageHeight - 32f,
                mutedPaint
            )

            doc.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val pdfFile = File(reportsDir, "EndoTrack_Clinician_Report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfFile).use { out ->
                doc.writeTo(out)
            }
            doc.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "EndoTrack Clinician Triage Summary — ${user.fullName}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is ${user.fullName}'s EndoTrack longitudinal symptom & pattern triage report for consultation review."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Export / Share Clinician Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            pdfFile
        }
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        if (text.isBlank()) return listOf("No custom patient note provided.")
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        words.forEach { word ->
            if (current.length + word.length + 1 > maxCharsPerLine) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                if (current.isNotEmpty()) current.append(" ")
                current.append(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
