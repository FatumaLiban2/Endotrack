package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CycleSummaryEntity
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import com.example.ui.components.CenteredMaxWidthColumn
import com.example.ui.components.RiskTierPill
import com.example.ui.components.WarmCard
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PainMildTint
import com.example.ui.theme.PainModerateTint
import com.example.ui.theme.PainSevereTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.RiskLowText
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow
import com.example.util.ClinicianPdfExporter
import com.example.util.DateUtil

@Composable
fun InsightsReportScreen(
    user: UserEntity,
    logs: List<SymptomLogEntity>,
    cycleSummaries: List<CycleSummaryEntity>,
    riskResult: MultiCycleRiskResult,
    weightsEntity: ModelWeightsEntity?,
    timeframeDays: Int,
    clinicianNote: String,
    onSelectTimeframe: (Int) -> Unit,
    onClinicianNoteChange: (String) -> Unit,
    onCalibrateOutcome: (Boolean) -> Unit,
    onResetWeights: () -> Unit,
    onExportPdf: () -> Unit,
    onEditLogDay: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateUtil.todayEpochDay()
    val cutoff = today - (timeframeDays - 1)
    val windowLogs = remember(logs, timeframeDays) {
        logs.filter { it.epochDay >= cutoff }.sortedBy { it.epochDay }
    }
    val metrics = remember(logs, timeframeDays) {
        ClinicianPdfExporter.computeMetrics(logs, timeframeDays)
    }

    val chartDays = remember(today) {
        ((today - 13)..today).toList()
    }
    val logsByDay = remember(logs) { logs.associateBy { it.epochDay } }
    val peakLog = remember(windowLogs) { windowLogs.maxByOrNull { it.painLevel } }
    var selectedChartDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRecentLogs by rememberSaveable { mutableStateOf(false) }
    var showCalibration by rememberSaveable { mutableStateOf(false) }

    val periodLogs = remember(windowLogs) { windowLogs.filter { it.hasPeriod } }
    val nonPeriodLogs = remember(windowLogs) { windowLogs.filter { !it.hasPeriod } }
    val periodPainAvg = if (periodLogs.isEmpty()) 0.0 else periodLogs.map { it.painLevel }.average()
    val nonPeriodPainAvg = if (nonPeriodLogs.isEmpty()) 0.0 else nonPeriodLogs.map { it.painLevel }.average()

    val phaseAverages = remember(logs, user) {
        val grouped = logs.groupBy { log ->
            DateUtil.cyclePhaseForDay(log.cycleDay, user.cycleLengthDays, user.periodDurationDays)
        }
        listOf("Menstrual phase", "Follicular phase", "Ovulatory phase", "Luteal phase").map { phase ->
            val phaseLogs = grouped[phase].orEmpty()
            val avg = if (phaseLogs.isEmpty()) 0.0 else phaseLogs.map { it.painLevel }.average()
            phase.replace(" phase", "") to avg
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBlush)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        CenteredMaxWidthColumn {
            // Timeframe pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(14 to "14 days", 30 to "30 days", 90 to "90 days").forEach { (days, label) ->
                    val selected = timeframeDays == days
                    Surface(
                        onClick = { onSelectTimeframe(days) },
                        shape = CircleShape,
                        color = if (selected) SurfaceCanvasBlush else Color.White,
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) PrimaryCherry else OutlineBorderTint
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timeframe_$days")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selected) SecondaryDeepCherry else OnSurfaceEspresso,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Pain trend chart
            WarmCard {
                Text(
                    text = "Pain trend",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnSurfaceEspresso,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${DateUtil.formatShortDate(cutoff)} – ${DateUtil.formatShortDate(today)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                )

                if (peakLog != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val inspectLog = selectedChartDay?.let { logsByDay[it] } ?: peakLog
                    val inspectSymptoms = EndoLogisticRegressionModel
                        .decodeSelectedFeatureNames(inspectLog.featureVectorBinary)
                        .filterNot { it in user.baselineHistorySet() }
                        .take(3)
                        .joinToString(" • ")
                        .ifEmpty { "No extra symptoms" }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, OutlineBorderTint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryDeepCherry),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = inspectLog.painLevel.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${DateUtil.formatShortDate(inspectLog.epochDay)}: Pain ${inspectLog.painLevel}/10",
                                    style = MaterialTheme.typography.labelLarge.copy(color = SecondaryDeepCherry)
                                )
                                Text(
                                    text = inspectSymptoms,
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(shape = CircleShape, color = Color.White) {
                                Text(
                                    text = if (inspectLog.id == peakLog.id) "Peak day" else "Selected",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val barCount = chartDays.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 22.dp, top = 16.dp)
                    ) {
                        val slotWidth = size.width / barCount.toFloat()
                        val barWidth = (slotWidth * 0.58f).coerceAtMost(28.dp.toPx())
                        val maxBarHeight = size.height

                        chartDays.forEachIndexed { idx, day ->
                            val log = logsByDay[day]
                            val pain = log?.painLevel ?: 0
                            val ratio = (pain / 10f).coerceIn(0.06f, 1f)
                            val barHeight = if (log == null) 6.dp.toPx() else maxBarHeight * ratio
                            val leftX = idx * slotWidth + (slotWidth - barWidth) / 2f
                            val topY = maxBarHeight - barHeight

                            val barColor = when {
                                log == null -> SurfaceCanvasBlush
                                pain >= 7 -> PainSevereTint
                                pain >= 4 -> PainModerateTint
                                else -> PainMildTint
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(leftX, topY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            if (log?.hasPeriod == true) {
                                drawCircle(
                                    color = PrimaryCherry,
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(leftX + barWidth / 2f, topY - 7.dp.toPx())
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chartDays.forEach { day ->
                            val log = logsByDay[day]
                            val isSelected = selectedChartDay == day
                            Text(
                                text = DateUtil.dayOfMonth(day).toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when {
                                        isSelected -> PrimaryCherry
                                        log?.hasPeriod == true -> SecondaryDeepCherry
                                        else -> OnSurfaceVariantMuted
                                    },
                                    fontWeight = if (isSelected || log?.hasPeriod == true) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedChartDay = day }
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = PainMildTint, label = "0–3 Mild")
                    LegendItem(color = PainModerateTint, label = "4–6 Moderate")
                    LegendItem(color = PainSevereTint, label = "7–10 Severe")
                    LegendItem(color = PrimaryCherry, label = "Period")
                }
            }

            // KPI row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WarmCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Average pain",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.1f".format(metrics.averagePain),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = PrimaryCherry,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = " /10",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = "Peak: ${metrics.peakPain}/10",
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                    )
                }

                WarmCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Days logged",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = windowLogs.size.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = " /${timeframeDays}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    val consistency = ((windowLogs.size * 100.0) / timeframeDays).toInt().coerceAtMost(100)
                    Text(
                        text = "$consistency%",
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                    )
                }
            }

            // Top symptom + honest period comparison
            val topSymptom = metrics.topSymptoms.firstOrNull()
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Top symptom",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                        Text(
                            text = topSymptom?.first ?: "Log symptoms to see patterns",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    if (topSymptom != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = topSymptom.second.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PrimaryCherry,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "of ${windowLogs.size} logs",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }
                    }
                }

                if (periodLogs.isNotEmpty() && nonPeriodLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val periodText = "%.1f".format(periodPainAvg)
                    val nonPeriodText = "%.1f".format(nonPeriodPainAvg)
                    val comparison = when {
                        periodPainAvg - nonPeriodPainAvg >= 1.0 ->
                            "Pain is higher on period days ($periodText/10) than other days ($nonPeriodText/10)."
                        nonPeriodPainAvg - periodPainAvg >= 1.0 ->
                            "Pain is higher on non-period days ($nonPeriodText/10) than period days ($periodText/10)."
                        else ->
                            "Pain is similar on period days ($periodText/10) and other days ($nonPeriodText/10)."
                    }
                    Text(
                        text = comparison,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso)
                    )
                }
            }

            // Cycle phase breakdown
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pain by cycle phase",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Text(
                        text = "${cycleSummaries.size} cycle(s)",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                }

                phaseAverages.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { (phase, avg) ->
                            WarmCard(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = phase,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = PrimaryCherry,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Avg ${"%.1f".format(avg)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnSurfaceEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (avg / 10.0).toFloat().coerceIn(0f, 1f) },
                                    color = PrimaryCherry,
                                    trackColor = SurfaceCanvasBlush,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // What is driving the pattern signal (moved here from Home)
            if (riskResult.topDrivers.isNotEmpty()) {
                WarmCard(modifier = Modifier.testTag("insights_drivers_card")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What drives your signal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        RiskTierPill(
                            riskTier = riskResult.riskTier,
                            probability = riskResult.aggregatedProbability
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val maxContribution = riskResult.topDrivers.maxOf { it.contribution }.coerceAtLeast(0.5)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        riskResult.topDrivers.forEach { driver ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = driver.featureName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OnSurfaceEspresso,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+${"%.2f".format(driver.contribution)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = PrimaryCherry)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { (driver.contribution / maxContribution).toFloat().coerceIn(0.1f, 1f) },
                                    color = PrimaryCherry,
                                    trackColor = SurfaceCanvasBlush,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Doctor report
            WarmCard(modifier = Modifier.testTag("insights_doctor_report_card")) {
                Text(
                    text = "Doctor report",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnSurfaceEspresso,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = clinicianNote,
                    onValueChange = onClinicianNoteChange,
                    label = { Text("What do you want to tell your doctor?") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCherry,
                        unfocusedBorderColor = OutlineBorderTint,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Last $timeframeDays days: peak pain ${metrics.peakPain}/10, " +
                            "${metrics.severeDaysCount} days at 6 or above, " +
                            "${metrics.impactDaysCount} days affecting daily life, " +
                            "${metrics.otcNonResponseCount}/${metrics.otcAttempts} pain-relief attempts without relief.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onExportPdf,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryDeepCherry,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("export_doctor_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export / share report (PDF)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Recent daily logs (collapsed by default)
            WarmCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecentLogs = !showRecentLogs },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent logs",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Icon(
                        imageVector = if (showRecentLogs) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle recent logs",
                        tint = SecondaryDeepCherry
                    )
                }

                if (showRecentLogs) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        logs.take(6).forEach { log ->
                            Surface(
                                onClick = { onEditLogDay(log.epochDay) },
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineBorderTint),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (log.painLevel >= 6) PrimaryCherry else SurfaceCanvasBlush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = log.painLevel.toString(),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = if (log.painLevel >= 6) Color.White else SecondaryDeepCherry
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = DateUtil.formatShortDate(log.epochDay),
                                                style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (log.hasPeriod) "• Day ${log.cycleDay} Period" else "• Cycle Day ${log.cycleDay}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (log.hasPeriod) PrimaryCherry else OnSurfaceVariantMuted
                                                )
                                            )
                                        }
                                        Text(
                                            text = log.notes.ifBlank { "Tap to edit" },
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit log",
                                        tint = SecondaryDeepCherry,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Model calibration (advanced, collapsed by default)
            WarmCard(modifier = Modifier.testTag("insights_calibration_card")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalibration = !showCalibration },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = SecondaryDeepCherry,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Advanced: calibrate model",
                            style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                        )
                    }
                    Icon(
                        imageVector = if (showCalibration) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle calibration",
                        tint = SecondaryDeepCherry
                    )
                }

                if (showCalibration) {
                    val updateCount = weightsEntity?.calibrationUpdatesCount ?: 0
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (updateCount > 0) {
                            weightsEntity?.lastCalibrationOutcome ?: "Personalized model"
                        } else {
                            "Using the baseline model"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(color = SecondaryDeepCherry)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "If a doctor has confirmed or ruled out endometriosis for you, record it here to adjust the model to your own data. This stays on your device.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCalibrateOutcome(true) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCherry,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("calibrate_confirmed_button")
                        ) {
                            Text(
                                text = "Confirmed",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        OutlinedButton(
                            onClick = { onCalibrateOutcome(false) },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, OutlineBorderTint),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceContainerLow,
                                contentColor = RiskLowText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("calibrate_ruled_out_button")
                        ) {
                            Text(
                                text = "Ruled out",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    if (updateCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onResetWeights,
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("reset_baseline_weights_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = null,
                                tint = SecondaryDeepCherry,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reset to baseline ($updateCount update${if (updateCount > 1) "s" else ""})",
                                color = SecondaryDeepCherry,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Text(
                text = "Pattern summaries only, not a medical diagnosis.",
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
        )
    }
}