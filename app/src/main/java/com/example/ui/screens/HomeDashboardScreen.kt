package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import com.example.ml.RiskTier
import com.example.ui.components.CenteredMaxWidthColumn
import com.example.ui.components.RiskTierPill
import com.example.ui.components.WarmCard
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PainMildTint
import com.example.ui.theme.PainModerateTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.PrimaryDeep
import com.example.ui.theme.RiskHighText
import com.example.ui.theme.RiskLowText
import com.example.ui.theme.RiskModerateText
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow
import com.example.util.DateUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    user: UserEntity,
    logs: List<SymptomLogEntity>,
    riskResult: MultiCycleRiskResult,
    weightsEntity: ModelWeightsEntity?,
    onStartTodayLog: (Long) -> Unit,
    onOpenInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateUtil.todayEpochDay()
    val cycleDay = DateUtil.calculateCycleDay(today, user.lastPeriodStartEpochDay, user.cycleLengthDays)
    val cyclePhase = DateUtil.cyclePhaseForDay(cycleDay, user.cycleLengthDays, user.periodDurationDays)
    val todayFormatted = DateUtil.formatDayHeader(today)

    val last7Days = (6 downTo 0).map { offset -> today - offset }
    val logsByDay = logs.associateBy { it.epochDay }
    val loggedCountLast7 = last7Days.count { it in logsByDay }
    val recentWeekLogs = last7Days.mapNotNull { logsByDay[it] }
    val avgPainWeek = if (recentWeekLogs.isNotEmpty()) {
        recentWeekLogs.map { it.painLevel }.average()
    } else 0.0

    val primarySymptomWeek = recentWeekLogs
        .flatMap { EndoLogisticRegressionModel.decodeSelectedFeatureNames(it.featureVectorBinary) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "No flare logged"

    val latestLog = logs.maxByOrNull { it.epochDay }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBlush)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        CenteredMaxWidthColumn {
            // Cycle Day & Phase pills matching Stitch Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = SecondaryDeepCherry,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$todayFormatted • Cycle Day $cycleDay",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceEspresso),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = SurfaceCanvasBlush,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    modifier = Modifier.weight(0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(PrimaryCherry)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cyclePhase,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecondaryDeepCherry,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Personalized Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${user.fullName}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = OnSurfaceEspresso,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "How is your body feeling right now?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariantMuted)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCanvasBlush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = null,
                        tint = PrimaryCherry,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Hero Cherry-Red CTA Card matching Stitch Home
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = PrimaryCherry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryCherry, PrimaryDeep)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Today's Check-in",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = if (today in logsByDay) "Update today's symptoms" else "Log today's symptoms",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Even pain-free or gentle days provide vital patterns for the 58-symptom model across your cycles.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )

                        Button(
                            onClick = { onStartTodayLog(today) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = SecondaryDeepCherry
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("home_log_today_cta")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (today in logsByDay) "Edit Today's Entry" else "Start Today's Entry",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Takes under 90 seconds • Runs on-device inference immediately",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }
            }

            // Live Multi-Cycle AI Pattern Risk Card
            MultiCycleRiskCard(
                riskResult = riskResult,
                calibrationCount = weightsEntity?.calibrationUpdatesCount ?: 0,
                onViewInsights = onOpenInsights
            )

            // Last 7 Days Streak Card matching Stitch Home
            WarmCard {
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
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryCherry,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Last 7 Days",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                            Text(
                                text = if (loggedCountLast7 >= 5) "Consistent tracking unlocked" else "Tap any day to log",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = SurfaceCanvasBlush
                    ) {
                        Text(
                            text = "$loggedCountLast7 of 7 logged",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecondaryDeepCherry,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        last7Days.forEach { day ->
                            val log = logsByDay[day]
                            val isToday = day == today
                            val circleColor = when {
                                log == null -> SurfaceCanvasBlush
                                log.painLevel >= 6 -> SecondaryDeepCherry
                                log.painLevel >= 3 -> PrimaryCherry
                                else -> PainModerateTint
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onStartTodayLog(day) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = DateUtil.dayOfWeekInitial(day),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isToday) PrimaryCherry else OnSurfaceVariantMuted,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(circleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = log?.painLevel?.toString() ?: if (isToday) "?" else "•",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (log != null) Color.White else SecondaryDeepCherry,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (log?.hasPeriod == true) PrimaryCherry
                                            else if (log != null) PainModerateTint.copy(alpha = 0.4f)
                                            else Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Avg pain this week",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.1f".format(avgPainWeek),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = PrimaryCherry,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = " /10",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (avgPainWeek / 10.0).toFloat().coerceIn(0f, 1f) },
                                color = PrimaryCherry,
                                trackColor = SurfaceCanvasBlush,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Top weekly factor",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = PrimaryCherry,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = primarySymptomWeek,
                                    style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Latest Entry Card
            if (latestLog != null) {
                val decoded = EndoLogisticRegressionModel.decodeSelectedFeatureNames(latestLog.featureVectorBinary)
                    .filterNot { it in user.baselineHistorySet() }
                WarmCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = SecondaryDeepCherry,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Latest Entry",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                        }
                        Text(
                            text = DateUtil.formatShortDate(latestLog.epochDay),
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SurfaceCanvasBlush
                        ) {
                            Text(
                                text = "Pain ${latestLog.painLevel}/10",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = SecondaryDeepCherry,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        if (latestLog.hasPeriod) {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineBorderTint)
                            ) {
                                Text(
                                    text = "Period (${latestLog.flowVolume.lowercase().replaceFirstChar { it.uppercase() }})",
                                    style = MaterialTheme.typography.labelMedium.copy(color = PrimaryCherry),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (decoded.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Recorded Symptoms:",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            decoded.take(5).forEach { symptom ->
                                Surface(
                                    shape = CircleShape,
                                    color = SurfaceContainerLow
                                ) {
                                    Text(
                                        text = symptom,
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceEspresso),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            if (decoded.size > 5) {
                                Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                                    Text(
                                        text = "+${decoded.size - 5} more",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Medication,
                                contentDescription = null,
                                tint = SecondaryDeepCherry,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = latestLog.otcMedicationName.ifBlank { "No OTC medication logged" },
                                    style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceEspresso)
                                )
                                val impactText = latestLog.impactFlagsSet().joinToString(" • ")
                                    .ifEmpty { "No functional impact flagged" }
                                Text(
                                    text = impactText,
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = { onStartTodayLog(latestLog.epochDay) },
                                shape = CircleShape,
                                border = BorderStroke(1.dp, OutlineBorderTint),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Entry",
                                    tint = SecondaryDeepCherry,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                                )
                            }
                        }
                    }
                }
            }

            // Doctor's Visit Prep Card matching Stitch Home
            WarmCard(containerColor = SurfaceContainerLow) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryCherry),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Doctor's Visit Prep",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                                Text(
                                    text = "PDF Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryCherry,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate a structured 14, 30, or 90-day clinical triage PDF report with your top pattern drivers for your OB-GYN.",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onOpenInsights,
                    shape = CircleShape,
                    border = BorderStroke(1.2.dp, PrimaryCherry),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = SecondaryDeepCherry
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_generate_report_button")
                ) {
                    Text(
                        text = "Open Insights & Provider Report",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Privacy footer matching Stitch Home
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, OutlineBorderTint),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = SecondaryDeepCherry,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your health data stays 100% private on your device in local Room storage. EndoTrack tracks & informs — it never replaces professional medical diagnosis.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiCycleRiskCard(
    riskResult: MultiCycleRiskResult,
    calibrationCount: Int,
    onViewInsights: () -> Unit
) {
    val animatedProb by animateFloatAsState(
        targetValue = riskResult.aggregatedProbability.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "risk_arc"
    )
    val gaugeColor = when (riskResult.riskTier) {
        RiskTier.LOW -> RiskLowText
        RiskTier.MODERATE -> RiskModerateText
        RiskTier.HIGH -> RiskHighText
    }
    val pct = (riskResult.aggregatedProbability * 100.0).toInt()

    WarmCard(modifier = Modifier.testTag("home_ai_risk_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = PrimaryCherry,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Multi-Cycle Pattern Signal",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Text(
                        text = if (calibrationCount > 0) {
                            "Personalized SGD Model ($calibrationCount update${if (calibrationCount > 1) "s" else ""})"
                        } else {
                            "886-Patient Logistic Regression"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                }
            }
            RiskTierPill(
                riskTier = riskResult.riskTier,
                probability = riskResult.aggregatedProbability
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(86.dp)) {
                    drawArc(
                        color = SurfaceCanvasBlush,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProb,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = OnSurfaceEspresso,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${riskResult.cyclesAnalyzed} cycle${if (riskResult.cyclesAnalyzed == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = riskResult.riskTier.clinicalSummary,
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${riskResult.totalLogsAnalyzed} logs analyzed • ${riskResult.recurringSymptomCount} recurring markers",
                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry)
                )
            }
        }

        if (riskResult.topDrivers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Top Contributing Pattern Drivers (wᵢ × xᵢ):",
                style = MaterialTheme.typography.labelMedium.copy(color = SecondaryDeepCherry)
            )
            Spacer(modifier = Modifier.height(8.dp))
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

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onViewInsights,
            shape = CircleShape,
            border = BorderStroke(1.dp, OutlineBorderTint),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Explore Longitudinal Charts & Calibrate Model",
                style = MaterialTheme.typography.labelMedium.copy(color = SecondaryDeepCherry)
            )
        }
    }
}
