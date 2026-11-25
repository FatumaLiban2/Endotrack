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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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

    val last7Days = (6 downTo 0).map { offset -> today - offset }
    val logsByDay = logs.associateBy { it.epochDay }
    val loggedCountLast7 = last7Days.count { it in logsByDay }
    val recentWeekLogs = last7Days.mapNotNull { logsByDay[it] }
    val avgPainWeek = if (recentWeekLogs.isNotEmpty()) {
        recentWeekLogs.map { it.painLevel }.average()
    } else 0.0

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
            // Greeting + cycle day/phase on one line
            Column {
                Text(
                    text = "Hello, ${user.fullName}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = OnSurfaceEspresso,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Day $cycleDay • $cyclePhase",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryDeepCherry),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Single log-today button
            Surface(
                shape = RoundedCornerShape(24.dp),
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
                        .padding(16.dp)
                ) {
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
                            text = if (today in logsByDay) "Edit today's entry" else "Log today's symptoms",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Pattern signal summary
            MultiCycleRiskCard(
                riskResult = riskResult,
                onViewInsights = onOpenInsights
            )

            // Last 7 days
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                // Average pain (compact)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Avg pain this week",
                        style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceVariantMuted)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.1f".format(avgPainWeek),
                            style = MaterialTheme.typography.titleLarge.copy(
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

            // Latest entry (compact)
            if (latestLog != null) {
                val decoded = EndoLogisticRegressionModel.decodeSelectedFeatureNames(latestLog.featureVectorBinary)
                    .filterNot { it in user.baselineHistorySet() }
                WarmCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Latest entry",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                            Text(
                                text = DateUtil.formatShortDate(latestLog.epochDay),
                                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
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
                        decoded.take(3).forEach { symptom ->
                            Surface(shape = CircleShape, color = SurfaceContainerLow) {
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceEspresso),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                        if (decoded.size > 3) {
                            Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                                Text(
                                    text = "+${decoded.size - 3} more",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Doctor report: one button
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Doctor report (PDF)",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Short disclaimer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = SecondaryDeepCherry,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your data stays on your device. EndoTrack doesn't diagnose.",
                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                )
            }
        }
    }
}

@Composable
private fun MultiCycleRiskCard(
    riskResult: MultiCycleRiskResult,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(78.dp)) {
                    drawArc(
                        color = SurfaceCanvasBlush,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProb,
                        useCenter = false,
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.titleLarge.copy(
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pattern Signal",
                    style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                )
                RiskTierPill(
                    riskTier = riskResult.riskTier,
                    probability = riskResult.aggregatedProbability
                )
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
                text = "View insights",
                style = MaterialTheme.typography.labelMedium.copy(color = SecondaryDeepCherry)
            )
        }
    }
}