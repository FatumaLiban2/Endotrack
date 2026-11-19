package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
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

@OptIn(ExperimentalLayoutApi::class)
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

    val chartDays = remember(timeframeDays, today) {
        val displaySpan = when (timeframeDays) {
            14 -> 14
            30 -> 14
            else -> 14
        }
        ((today - (displaySpan - 1))..today).toList()
    }
    val logsByDay = remember(logs) { logs.associateBy { it.epochDay } }
    val peakLog = remember(windowLogs) { windowLogs.maxByOrNull { it.painLevel } }
    var selectedChartDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRecentLogs by rememberSaveable { mutableStateOf(true) }

    val periodPainAvg = remember(windowLogs) {
        val p = windowLogs.filter { it.hasPeriod }
        if (p.isEmpty()) 0.0 else p.map { it.painLevel }.average()
    }
    val nonPeriodPainAvg = remember(windowLogs) {
        val np = windowLogs.filter { !it.hasPeriod }
        if (np.isEmpty()) 0.0 else np.map { it.painLevel }.average()
    }

    val phaseAverages = remember(logs, user) {
        val grouped = logs.groupBy { log ->
            DateUtil.cyclePhaseForDay(log.cycleDay, user.cycleLengthDays, user.periodDurationDays)
        }
        listOf(
            Triple("Menstrual phase", "Days 1–${user.periodDurationDays}", grouped["Menstrual phase"].orEmpty()),
            Triple("Follicular phase", "Days ${user.periodDurationDays + 1}–${(user.cycleLengthDays / 2) - 1}", grouped["Follicular phase"].orEmpty()),
            Triple("Ovulatory phase", "Mid-cycle", grouped["Ovulatory phase"].orEmpty()),
            Triple("Luteal phase", "Pre-period", grouped["Luteal phase"].orEmpty())
        ).map { (phase, rangeLabel, phaseLogs) ->
            val avg = if (phaseLogs.isEmpty()) 0.0 else phaseLogs.map { it.painLevel }.average()
            Triple(phase.replace(" phase", ""), rangeLabel, avg)
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
            // Timeframe Header & Selector Pills matching Stitch Insights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timeframe",
                    style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                )
                Text(
                    text = "${DateUtil.formatShortDate(cutoff)} – ${DateUtil.formatShortDate(today)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryCherry)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(14 to "Last 14 Days", 30 to "Last 30 Days", 90 to "Last 90 Days").forEach { (days, label) ->
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

            // Longitudinal Pain Trend Chart Card matching Stitch Insights
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${timeframeDays}-Day Pain Trend",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Scale 0 (None) to 10 (Incapacitating)",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                    Surface(shape = CircleShape, color = SurfaceContainerLow) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = null,
                                tint = PrimaryCherry,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Flow marker",
                                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                            )
                        }
                    }
                }

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
                                    text = if (inspectLog.id == peakLog.id) "Peak Day" else "Selected",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Compose Canvas Bar Chart with flow droplet indicators
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
                }
            }

            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WarmCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Average Pain",
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
                        text = "Logged Days",
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
                        text = "$consistency% consistency",
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                    )
                }
            }

            // Top Reported Symptom banner
            val topSymptom = metrics.topSymptoms.firstOrNull()
            WarmCard(containerColor = Color.White) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvasBlush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = PrimaryCherry
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOP REPORTED SYMPTOM",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                        Text(
                            text = topSymptom?.first ?: "Log symptoms to surface recurring patterns",
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
            }

            // Cycle Pattern Identified Card
            WarmCard(containerColor = SurfaceContainerLow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryDeepCherry),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cycle Pattern Identified",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(shape = CircleShape, color = Color.White) {
                        Text(
                            text = "Entry Trend",
                            style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Your pain is significantly higher during menstrual bleeding days (averaging ${"%.1f".format(periodPainAvg)}/10 vs non-period days average ${"%.1f".format(nonPeriodPainAvg)}/10).",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = OnSurfaceVariantMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Summarizes your personal entries and does not constitute a medical diagnosis.",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                }
            }

            // Cycle Phase Breakdown (4 cards)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cycle Phase Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Text(
                        text = "${cycleSummaries.size} cycle(s) tracked",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    phaseAverages.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { (phase, range, avg) ->
                                WarmCard(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = phase,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = PrimaryCherry,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = range,
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "Avg ${"%.1f".format(avg)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = OnSurfaceEspresso,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = " /10",
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                                        )
                                    }
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
            }

            // On-Device Logistic Regression & SGD Incremental Calibration Card
            WarmCard(modifier = Modifier.testTag("insights_calibration_card")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceCanvasBlush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null,
                                tint = SecondaryDeepCherry
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "On-Device SGD Calibration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnSurfaceEspresso,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = weightsEntity?.lastCalibrationOutcome ?: "Clinical Baseline (886-Patient Model)",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }
                    }
                    RiskTierPill(
                        riskTier = riskResult.riskTier,
                        probability = riskResult.aggregatedProbability
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Record a clinician-verified diagnostic outcome to locally fine-tune your 58 model weights via Stochastic Gradient Descent (w[i] -= lr × (ŷ - y) × x[i]).",
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
                            text = "Confirmed Endo (+1)",
                            style = MaterialTheme.typography.labelSmall
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
                            text = "Ruled Out (0)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                val updateCount = weightsEntity?.calibrationUpdatesCount ?: 0
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
                            text = "Reset to Clinical Baseline Weights ($updateCount update${if (updateCount > 1) "s" else ""})",
                            color = SecondaryDeepCherry,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Clinician-Facing Appointment Triage Summary & PDF Export Card matching Stitch Insights
            WarmCard(modifier = Modifier.testTag("insights_doctor_report_card")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SecondaryDeepCherry),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Appointment Triage Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnSurfaceEspresso,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Clinician-ready PDF export",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }
                    }
                    Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecondaryDeepCherry,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "1. What do you want to tell your doctor?",
                    style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = clinicianNote,
                    onValueChange = onClinicianNoteChange,
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

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "2. Top Priority Drivers for Visit",
                    style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val topics = riskResult.topDrivers.map { it.featureName }
                        .ifEmpty { listOf("Cycle Pain Pattern", "NSAID Response", "Baseline History") }
                    topics.forEach { topic ->
                        Surface(
                            shape = CircleShape,
                            color = SurfaceCanvasBlush,
                            border = BorderStroke(1.dp, OutlineBorderTint)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryCherry,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${timeframeDays}-Day Clinical Summary",
                                style = MaterialTheme.typography.titleMedium.copy(color = PrimaryCherry)
                            )
                            Text(
                                text = "${metrics.otcNonResponseCount}/${metrics.otcAttempts} OTC unrelieved",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Peak Pain Reported",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                    )
                                    Text(
                                        text = "${metrics.peakPain} / 10",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = PrimaryCherry,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${metrics.severeDaysCount} days pain ≥ 6",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Quality of Life Impact",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                    )
                                    Text(
                                        text = "${metrics.impactDaysCount} Days",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = OnSurfaceEspresso,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${metrics.periodDays} bleeding days",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                    )
                                }
                            }
                        }

                        if (metrics.topSymptoms.isNotEmpty()) {
                            Text(
                                text = "Symptom Distribution (${timeframeDays} days)",
                                style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceEspresso)
                            )
                            val maxCount = metrics.topSymptoms.maxOf { it.second }.coerceAtLeast(1)
                            metrics.topSymptoms.take(4).forEach { (symptom, count) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = symptom,
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "$count logs",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { (count.toFloat() / maxCount.toFloat()).coerceIn(0.1f, 1f) },
                                        color = PrimaryCherry,
                                        trackColor = SurfaceCanvasBlush,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        text = "Export / Share Doctor Report (PDF)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onExportPdf,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = SecondaryDeepCherry
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Android Share Sheet",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Recent Daily Logs expandable list
            WarmCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecentLogs = !showRecentLogs },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recent Daily Logs",
                            style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                        )
                        Text(
                            text = "Tap any day to review or edit its 58 symptoms",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
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
                                            if (log.hasPeriod) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• Day ${log.cycleDay} Period",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryCherry)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• Cycle Day ${log.cycleDay}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                                )
                                            }
                                        }
                                        Text(
                                            text = log.notes.ifBlank { "Tap to edit logged symptoms" },
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
