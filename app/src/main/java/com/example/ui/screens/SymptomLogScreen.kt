package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ml.MultiCycleRiskResult
import com.example.ml.SymptomCategory
import com.example.ui.components.CenteredMaxWidthColumn
import com.example.ui.components.RiskTierPill
import com.example.ui.components.WarmCard
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.viewmodel.LogDraftState
import com.example.util.DateUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomLogScreen(
    user: UserEntity,
    logs: List<SymptomLogEntity>,
    draft: LogDraftState,
    onSelectDay: (Long) -> Unit,
    onPainLevelChange: (Int) -> Unit,
    onHasPeriodChange: (Boolean) -> Unit,
    onFlowVolumeChange: (String) -> Unit,
    onTookOtcChange: (Boolean) -> Unit,
    onOtcNameChange: (String) -> Unit,
    onOtcRelievedChange: (String) -> Unit,
    onToggleImpactFlag: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleCategory: (SymptomCategory) -> Unit,
    onToggleSymptom: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSaveLog: () -> Unit,
    onDeleteLog: () -> Unit,
    onDismissFeedback: () -> Unit,
    onNavigateToInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateUtil.todayEpochDay()
    val logsByDay = remember(logs) { logs.associateBy { it.epochDay } }
    val baselineHistory = remember(user.baselineHistoryCsv) { user.baselineHistorySet() }

    val frequentSymptoms = remember(logs) {
        logs.flatMap { EndoLogisticRegressionModel.decodeSelectedFeatureNames(it.featureVectorBinary) }
            .filterNot { it in baselineHistory }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key to it.value }
    }

    val weekDays = remember(draft.epochDay) {
        val anchor = draft.epochDay
        (-3..3).map { anchor + it }
    }

    val scrollState = rememberScrollState()

    if (draft.lastSavedRiskResult != null) {
        SavedInferenceFeedbackDialog(
            riskResult = draft.lastSavedRiskResult,
            onDismiss = onDismissFeedback,
            onViewInsights = {
                onDismissFeedback()
                onNavigateToInsights()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBlush)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        CenteredMaxWidthColumn {
            // Calendar Week Strip Card matching Stitch Log
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSelectDay(draft.epochDay - 7) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Previous week",
                            tint = SecondaryDeepCherry
                        )
                    }
                    Text(
                        text = DateUtil.formatMonthYear(draft.epochDay),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnSurfaceEspresso,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = {
                        val next = (draft.epochDay + 7).coerceAtMost(today)
                        onSelectDay(next)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Next week",
                            tint = SecondaryDeepCherry
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { day ->
                        val selected = day == draft.epochDay
                        val dayLog = logsByDay[day]
                        val isFuture = day > today
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) SecondaryDeepCherry else Color.Transparent)
                                .clickable(enabled = !isFuture) { onSelectDay(day) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = DateUtil.dayOfWeekInitial(day),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) Color.White.copy(alpha = 0.85f)
                                    else if (isFuture) OnSurfaceVariantMuted.copy(alpha = 0.4f)
                                    else OnSurfaceVariantMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = DateUtil.dayOfMonth(day).toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (selected) Color.White
                                    else if (isFuture) OnSurfaceVariantMuted.copy(alpha = 0.4f)
                                    else OnSurfaceEspresso,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                if (dayLog?.hasPeriod == true) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) Color.White else PrimaryCherry)
                                    )
                                }
                                if (dayLog != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (selected) Color.White.copy(alpha = 0.7f) else SecondaryDeepCherry)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryCherry,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val suffix = if (draft.epochDay == today) " (Today)" else ""
                            Text(
                                text = DateUtil.formatDayHeader(draft.epochDay) + suffix,
                                style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
                            )
                        }
                        Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                            Text(
                                text = if (draft.existingLogId != null) "Editing" else "New Entry",
                                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryDeepCherry),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Overall Pain Level Card
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pain level",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val severityTitle = when {
                            draft.painLevel == 0 -> "No pain"
                            draft.painLevel <= 3 -> "Mild"
                            draft.painLevel <= 6 -> "Moderate"
                            else -> "Severe"
                        }
                        Text(
                            text = severityTitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryDeepCherry)
                        )
                    }
                    Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                        Text(
                            text = "${draft.painLevel} / 10",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = SecondaryDeepCherry,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (0..10).forEach { level ->
                        val active = draft.painLevel == level
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (active) PrimaryCherry else Color.White)
                                .border(
                                    width = 1.2.dp,
                                    color = if (active) PrimaryCherry else OutlineBorderTint,
                                    shape = CircleShape
                                )
                                .clickable { onPainLevelChange(level) }
                                .testTag("pain_node_$level"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (active) Color.White else OnSurfaceEspresso,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Period Today & Flow Volume Selector
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
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = null,
                                tint = PrimaryCherry
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Period today?",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                        }
                    }
                    Switch(
                        checked = draft.hasPeriod,
                        onCheckedChange = onHasPeriodChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SecondaryDeepCherry,
                            uncheckedThumbColor = SecondaryDeepCherry,
                            uncheckedTrackColor = SurfaceCanvasBlush
                        ),
                        modifier = Modifier.testTag("period_today_switch")
                    )
                }

                AnimatedVisibility(visible = draft.hasPeriod) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SPOTTING" to "Spotting", "LIGHT" to "Light", "MEDIUM" to "Medium", "HEAVY" to "Heavy").forEach { (key, title) ->
                                val selected = draft.flowVolume == key
                                Surface(
                                    onClick = { onFlowVolumeChange(key) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) SecondaryDeepCherry else Color.White,
                                    border = BorderStroke(
                                        width = 1.2.dp,
                                        color = if (selected) SecondaryDeepCherry else OutlineBorderTint
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flow_tile_${key.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.WaterDrop,
                                            contentDescription = title,
                                            tint = if (selected) Color.White else PrimaryCherry,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (selected) Color.White else OnSurfaceEspresso,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Over-the-Counter (OTC) Pain Medication Tracker
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceCanvasBlush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Medication,
                                contentDescription = null,
                                tint = SecondaryDeepCherry
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OTC Pain Medication",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                            )
                        }
                    }
                    Switch(
                        checked = draft.tookOtcMedication,
                        onCheckedChange = onTookOtcChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SecondaryDeepCherry,
                            uncheckedThumbColor = SecondaryDeepCherry,
                            uncheckedTrackColor = SurfaceCanvasBlush
                        ),
                        modifier = Modifier.testTag("otc_switch")
                    )
                }

                AnimatedVisibility(visible = draft.tookOtcMedication) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Ibuprofen 400mg", "Ibuprofen 600mg", "Naproxen 500mg", "Acetaminophen 500mg", "Mefenamic acid").forEach { med ->
                                val selected = draft.otcMedicationName == med
                                FilterChip(
                                    selected = selected,
                                    onClick = { onOtcNameChange(med) },
                                    label = { Text(med) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SurfaceCanvasBlush,
                                        selectedLabelColor = SecondaryDeepCherry
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draft.otcMedicationName,
                            onValueChange = onOtcNameChange,
                            label = { Text("Medication & dose") },
                            placeholder = { Text("e.g., Ibuprofen 600mg") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCherry,
                                unfocusedBorderColor = OutlineBorderTint
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Did it relieve your pain?",
                            style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceEspresso)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("YES" to "Yes", "PARTIAL" to "Partially", "NO" to "No relief").forEach { (key, label) ->
                                val selected = draft.otcRelievedPain == key
                                Surface(
                                    onClick = { onOtcRelievedChange(key) },
                                    shape = CircleShape,
                                    color = if (selected) PrimaryCherry else SurfaceContainerLow,
                                    border = BorderStroke(1.dp, if (selected) PrimaryCherry else OutlineBorderTint),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (selected) Color.White else OnSurfaceEspresso
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Symptoms Card
            WarmCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Symptoms",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnSurfaceEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Surface(shape = CircleShape, color = SurfaceCanvasBlush) {
                        Text(
                            text = "${draft.selectedSymptoms.size} selected",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryDeepCherry,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draft.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search pelvic pain, cramps, bloating, fatigue...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search symptoms",
                            tint = OnSurfaceVariantMuted
                        )
                    },
                    trailingIcon = {
                        if (draft.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCherry,
                        unfocusedBorderColor = OutlineBorderTint,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("symptom_search_input")
                )

                if (frequentSymptoms.isNotEmpty() && draft.searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = OnSurfaceVariantMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Your usual symptoms",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequentSymptoms.forEach { (name, count) ->
                            val selected = name in draft.selectedSymptoms
                            SymptomToggleChip(
                                name = "$name ($count)",
                                selected = selected,
                                isProfileHistory = false,
                                onClick = { onToggleSymptom(name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6 Expandable Category Cards (A through F)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SymptomCategory.entries.forEach { category ->
                        val allInCat = EndoLogisticRegressionModel.CATEGORY_FEATURES[category].orEmpty()
                        val filteredInCat = if (draft.searchQuery.isBlank()) {
                            allInCat
                        } else {
                            allInCat.filter { it.contains(draft.searchQuery.trim(), ignoreCase = true) }
                        }

                        if (filteredInCat.isNotEmpty()) {
                            val selectedCount = allInCat.count { it in draft.selectedSymptoms }
                            val isExpanded = category in draft.expandedCategories || draft.searchQuery.isNotBlank()

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(
                                    width = if (isExpanded) 1.2.dp else 1.dp,
                                    color = if (isExpanded) PrimaryCherry.copy(alpha = 0.5f) else OutlineBorderTint
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleCategory(category) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                            .testTag("category_header_${category.id}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = categoryIcon(category),
                                                contentDescription = null,
                                                tint = SecondaryDeepCherry,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = category.title,
                                                    style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
                                                )
                                                Text(
                                                    text = "${allInCat.size} symptoms",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedCount > 0) {
                                                Surface(shape = CircleShape, color = SecondaryDeepCherry) {
                                                    Text(
                                                        text = selectedCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                                tint = SecondaryDeepCherry
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        FlowRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            filteredInCat.forEach { symptomName ->
                                                val selected = symptomName in draft.selectedSymptoms
                                                val isBaseline = symptomName in baselineHistory
                                                SymptomToggleChip(
                                                    name = symptomName,
                                                    selected = selected,
                                                    isProfileHistory = isBaseline,
                                                    onClick = { onToggleSymptom(symptomName) }
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

            // Impact on Your Day Card
            WarmCard {
                Text(
                    text = "Impact on your day",
                    style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Missed school/work", "Poor sleep", "Hard to move", "Used heating pad").forEach { flag ->
                        val selected = flag in draft.impactFlags
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleImpactFlag(flag) },
                            label = { Text(flag) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceContainerLow,
                                selectedContainerColor = SurfaceCanvasBlush,
                                selectedLabelColor = SecondaryDeepCherry
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = OutlineBorderTint,
                                selectedBorderColor = PrimaryCherry
                            )
                        )
                    }
                }
            }

            // Daily Notes & Triggers
            WarmCard {
                Text(
                    text = "Notes & triggers",
                    style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChange,
                    placeholder = { Text("E.g., Used heating pad for 2 hours, pain spiked after walking, Ibuprofen 600mg didn't help...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCherry,
                        unfocusedBorderColor = OutlineBorderTint,
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save / Clear Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearDraft,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = OnSurfaceEspresso
                    ),
                    modifier = Modifier
                        .weight(0.38f)
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }

                Button(
                    onClick = onSaveLog,
                    enabled = !draft.isSaving,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryDeepCherry,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(0.62f)
                        .height(52.dp)
                        .testTag("save_daily_log_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (draft.isSaving) "Saving..." else "Save Log",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (draft.existingLogId != null) {
                TextButton(onClick = onDeleteLog) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = OnSurfaceVariantMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete this entry", color = OnSurfaceVariantMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SymptomToggleChip(
    name: String,
    selected: Boolean,
    isProfileHistory: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) SurfaceCanvasBlush else Color.White,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) PrimaryCherry else OutlineBorderTint
        ),
        modifier = Modifier.testTag("symptom_chip_${name.take(24).lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = null,
                tint = if (selected) SecondaryDeepCherry else OnSurfaceVariantMuted,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isProfileHistory) "$name • History" else name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) SecondaryDeepCherry else OnSurfaceEspresso,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun SavedInferenceFeedbackDialog(
    riskResult: MultiCycleRiskResult,
    onDismiss: () -> Unit,
    onViewInsights: () -> Unit
) {
    val multiPct = (riskResult.aggregatedProbability * 100.0).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Log saved",
                style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RiskTierPill(
                    riskTier = riskResult.riskTier,
                    probability = riskResult.aggregatedProbability
                )
                Text(
                    text = "Pattern score: $multiPct% from ${riskResult.totalLogsAnalyzed} logs.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso)
                )
                Text(
                    text = "Pattern screening only, not a medical diagnosis.",
                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewInsights,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCherry)
            ) {
                Text("View Insights")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = SecondaryDeepCherry)
            }
        }
    )
}

private fun categoryIcon(category: SymptomCategory): ImageVector = when (category) {
    SymptomCategory.PELVIC_MENSTRUAL_PAIN -> Icons.Filled.WarningAmber
    SymptomCategory.FLOW_BLEEDING -> Icons.Filled.WaterDrop
    SymptomCategory.INTIMACY_INTERCOURSE -> Icons.Filled.FavoriteBorder
    SymptomCategory.BOWEL_BLADDER_GI -> Icons.Filled.LocalHospital
    SymptomCategory.FATIGUE_BODY_NEURO -> Icons.Filled.Spa
    SymptomCategory.HORMONAL_MOOD_SYSTEMIC -> Icons.Filled.Psychology
}