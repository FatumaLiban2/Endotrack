package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ModelWeightsEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ui.components.CenteredMaxWidthColumn
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.WarmCard
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow

@Composable
fun LearnScreen(
    weightsEntity: ModelWeightsEntity?,
    modifier: Modifier = Modifier
) {
    val currentWeights = remember(weightsEntity) {
        EndoLogisticRegressionModel.weightsFromCsv(weightsEntity?.weightsCsv)
    }
    val topPositiveWeights = remember(currentWeights) {
        EndoLogisticRegressionModel.FEATURE_NAMES
            .mapIndexed { idx, name -> name to currentWeights[idx] }
            .sortedByDescending { it.second }
            .take(8)
    }

    var checklistState by remember {
        mutableStateOf(
            setOf("Export 30–90 day EndoTrack PDF report")
        )
    }
    val prepChecklist = listOf(
        "Export 30–90 day EndoTrack PDF report",
        "Note OTC medications that failed to relieve peak pain",
        "Highlight cycle days with missed work or sleep disruption",
        "Ask about specialized pelvic MRI / deep endo ultrasound",
        "Confirm whether GI/IBS flares sync with menstrual days"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBlush)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        CenteredMaxWidthColumn {
            WarmCard(containerColor = SurfaceContainerLow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvasBlush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = PrimaryCherry
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Closing the 5–7 Year Diagnostic Delay",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PrimaryCherry,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Evidence-based clinical literacy & AI transparency",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Endometriosis affects ~1 in 10 women and people assigned female at birth, yet diagnosis often takes 5–7 years because symptoms span gynecological, bowel, urinary, and neurological systems—and are frequently masked by OTC painkillers.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso)
                )
            }

            // Transparent Logistic Regression Feature Weights Inspector
            WarmCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = SecondaryDeepCherry
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Transparent 58-Feature Logistic Model",
                            style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                        )
                        Text(
                            text = "Intercept = ${EndoLogisticRegressionModel.INTERCEPT} • 886-patient dataset",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Unlike black-box models, every symptom in EndoTrack has an explicit mathematical weight. Below are the highest positive predictors currently active in your model:",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topPositiveWeights.forEach { (feature, weight) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerLow,
                            border = BorderStroke(1.dp, OutlineBorderTint),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceEspresso,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                                    Text(
                                        text = "w = +${"%.3f".format(weight)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SecondaryDeepCherry,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // OB-GYN Appointment Prep Checklist
            WarmCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = null,
                        tint = PrimaryCherry
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OB-GYN Consultation Checklist",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    prepChecklist.forEach { item ->
                        val checked = item in checklistState
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (checked) SurfaceCanvasBlush else SurfaceContainerLow,
                            border = BorderStroke(1.dp, if (checked) PrimaryCherry else OutlineBorderTint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    checklistState = if (checked) checklistState - item else checklistState + item
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (checked) PrimaryCherry else OnSurfaceVariantMuted
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceEspresso,
                                        fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Symptom Clusters Educational Guide
            WarmCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = SecondaryDeepCherry
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why Multi-System Tracking Matters",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ClusterGuideItem(
                    title = "Pelvic & Intimacy Pain",
                    desc = "Cyclic dysmenorrhea, sharp pelvic cramps, and deep dyspareunia often cluster together during menstrual and ovulatory phases."
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClusterGuideItem(
                    title = "Bowel & Bladder Overlap",
                    desc = "Painful bowel movements, cyclical bloating ('endo belly'), and IBS-like symptoms frequently lead to misdiagnosis unless tracked against cycle day."
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClusterGuideItem(
                    title = "Systemic Fatigue & Masked Pain",
                    desc = "Chronic inflammation drives persistent exhaustion and migraines. Logging OTC medication non-response proves pain severity to clinicians."
                )
            }

            MedicalDisclaimerBanner()
        }
    }
}

@Composable
private fun ClusterGuideItem(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(color = SecondaryDeepCherry)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
            )
        }
    }
}
