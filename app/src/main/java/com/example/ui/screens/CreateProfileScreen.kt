package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.UserEntity
import com.example.ml.EndoLogisticRegressionModel
import com.example.ui.components.CenteredMaxWidthColumn
import com.example.ui.components.EndoTrackTopBar
import com.example.ui.components.WarmCard
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow
import com.example.util.DateUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProfileScreen(
    user: UserEntity,
    isEditingExisting: Boolean,
    onSaveProfile: (
        fullName: String,
        ageRange: String,
        ageNumeric: Int,
        cycleLengthDays: Int,
        periodDurationDays: Int,
        journeyReason: String,
        helpGoals: Set<String>,
        baselineHistory: Set<String>,
        lastPeriodStartEpochDay: Long
    ) -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    if (isEditingExisting) {
        BackHandler(onBack = onBack)
    }

    var fullName by rememberSaveable(user.id) { mutableStateOf(user.fullName) }
    var journeyReason by rememberSaveable(user.id) { mutableStateOf(user.journeyReason) }
    var helpGoals by remember(user.id) { mutableStateOf(user.helpGoalsSet()) }
    var ageRange by rememberSaveable(user.id) { mutableStateOf(user.ageRange) }
    var ageNumeric by rememberSaveable(user.id) { mutableIntStateOf(user.ageNumeric) }
    var cycleLength by rememberSaveable(user.id) { mutableIntStateOf(user.cycleLengthDays) }
    var periodDuration by rememberSaveable(user.id) { mutableIntStateOf(user.periodDurationDays) }
    var baselineHistory by remember(user.id) { mutableStateOf(user.baselineHistorySet()) }
    var lastPeriodOffsetDays by rememberSaveable(user.id) {
        mutableIntStateOf((DateUtil.todayEpochDay() - user.lastPeriodStartEpochDay).toInt().coerceIn(0, 45))
    }
    var acceptedTransparency by rememberSaveable(user.id) {
        mutableStateOf(user.hasCompletedProfile || user.acceptedDisclaimer)
    }

    val scrollState = rememberScrollState()
    val canSubmit = fullName.isNotBlank() && acceptedTransparency

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBlush)
    ) {
        EndoTrackTopBar(
            subtitle = if (isEditingExisting) "Clinical Profile" else "Profile Creation",
            onProfileClick = { if (isEditingExisting) onBack() },
            showBack = isEditingExisting,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp)
        ) {
            CenteredMaxWidthColumn {
                // Hero card matching Stitch endotrack_create_profile
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.2.dp, OutlineBorderTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (isEditingExisting) "Edit clinical profile" else "Create your profile",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = PrimaryCherry,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Baseline cycle & medical history feed directly into your on-device Logistic Regression model.",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                            )
                        }
                    }
                }

                // Name field
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What should we call you? *",
                            style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                        )
                        Text(
                            text = "Required",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        singleLine = true,
                        trailingIcon = {
                            if (fullName.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Valid name",
                                    tint = PrimaryCherry
                                )
                            }
                        },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCherry,
                            unfocusedBorderColor = OutlineBorderTint,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                }

                // What brings you to EndoTrack?
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What brings you to EndoTrack?",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Text(
                        text = "There are no wrong answers.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )

                    val reasons = listOf(
                        "I've been diagnosed with endometriosis",
                        "I'm waiting for a diagnosis",
                        "I have symptoms and I'm not sure why",
                        "I'm getting ready for a doctor's visit",
                        "I'm supporting someone I care about",
                        "I'm just here to learn"
                    )
                    reasons.forEach { option ->
                        val selected = journeyReason == option
                        Surface(
                            onClick = { journeyReason = option },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) SurfaceCanvasBlush else Color.White,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) PrimaryCherry else OutlineBorderTint
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnSurfaceEspresso,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryDeepCherry),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // What would you like help with?
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What would you like help with?",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                    )
                    Text(
                        text = "Pick as many as you like.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )

                    val goals: List<Pair<String, ImageVector>> = listOf(
                        "Track my symptoms day by day" to Icons.Filled.CalendarToday,
                        "Spot patterns across my cycle" to Icons.AutoMirrored.Filled.ShowChart,
                        "Prepare for doctor's appointments" to Icons.Filled.MedicalServices,
                        "Understand endometriosis better" to Icons.AutoMirrored.Filled.MenuBook,
                        "See how pain affects my daily life" to Icons.Filled.SentimentDissatisfied
                    )
                    goals.forEach { (goal, icon) ->
                        val selected = goal in helpGoals
                        Surface(
                            onClick = {
                                helpGoals = if (selected) helpGoals - goal else helpGoals + goal
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) SurfaceCanvasBlush else Color.White,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) PrimaryCherry else OutlineBorderTint
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) SecondaryDeepCherry else OnSurfaceVariantMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = goal,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnSurfaceEspresso,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryDeepCherry),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Age range & numeric age
                WarmCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Age range",
                            style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceEspresso)
                        )
                        Text(
                            text = "Age: $ageNumeric yrs",
                            style = MaterialTheme.typography.labelMedium.copy(color = SecondaryDeepCherry)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val ageOptions = listOf(
                        "Under 18" to 17,
                        "18–24" to 21,
                        "25–34" to 29,
                        "35–44" to 39,
                        "45+" to 47
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ageOptions.forEach { (label, defaultNum) ->
                            val selected = ageRange == label
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    ageRange = label
                                    ageNumeric = defaultNum
                                },
                                label = { Text(label) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
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

                    Spacer(modifier = Modifier.height(16.dp))

                    StepperRow(
                        title = "Usual cycle length in days",
                        subtitle = "Standard range: 21–45 days",
                        valueText = "$cycleLength days",
                        onDecrement = { if (cycleLength > 21) cycleLength-- },
                        onIncrement = { if (cycleLength < 45) cycleLength++ }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StepperRow(
                        title = "Typical period duration",
                        subtitle = "Active bleeding days per cycle",
                        valueText = "$periodDuration days",
                        onDecrement = { if (periodDuration > 2) periodDuration-- },
                        onIncrement = { if (periodDuration < 10) periodDuration++ }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StepperRow(
                        title = "Days since last period started",
                        subtitle = "Sets your current Cycle Day (${lastPeriodOffsetDays + 1})",
                        valueText = "$lastPeriodOffsetDays d ago",
                        onDecrement = { if (lastPeriodOffsetDays > 0) lastPeriodOffsetDays-- },
                        onIncrement = { if (lastPeriodOffsetDays < 44) lastPeriodOffsetDays++ }
                    )
                }

                // Baseline Clinical History (persistent model features!)
                WarmCard(containerColor = SurfaceContainerLow) {
                    Text(
                        text = "Baseline Clinical History (Auto-Fed to AI)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SecondaryDeepCherry,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select any persistent history diagnosed or noted by a clinician. These flags automatically feed into the 58-feature Logistic Regression model so you don't need to re-log them daily.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EndoLogisticRegressionModel.BASELINE_CLINICAL_HISTORY_FEATURES.forEach { feature ->
                            val selected = feature in baselineHistory
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    baselineHistory = if (selected) {
                                        baselineHistory - feature
                                    } else {
                                        baselineHistory + feature
                                    }
                                },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                label = { Text(feature) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    selectedContainerColor = PrimaryCherry,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = OutlineBorderTint,
                                    selectedBorderColor = PrimaryCherry
                                ),
                                modifier = Modifier.testTag("baseline_chip_${feature.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Clinical Transparency checkbox
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineBorderTint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { acceptedTransparency = !acceptedTransparency }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = acceptedTransparency,
                            onCheckedChange = { acceptedTransparency = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryCherry,
                                uncheckedColor = SecondaryDeepCherry
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "CLINICAL TRANSPARENCY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SecondaryDeepCherry,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "I understand EndoTrack tracks my symptoms and shares statistical pattern insights. It does not diagnose endometriosis or replace a licensed doctor.",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceEspresso)
                            )
                        }
                    }
                }

                if (isEditingExisting) {
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, OutlineBorderTint),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = SecondaryDeepCherry
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Account")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom CTA bar
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, OutlineBorderTint),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!canSubmit) {
                    Text(
                        text = "Add your name and tick the clinical transparency box to continue.",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Button(
                    onClick = {
                        val lastPeriodStart = DateUtil.todayEpochDay() - lastPeriodOffsetDays
                        onSaveProfile(
                            fullName,
                            ageRange,
                            ageNumeric,
                            cycleLength,
                            periodDuration,
                            journeyReason,
                            helpGoals,
                            baselineHistory,
                            lastPeriodStart
                        )
                    },
                    enabled = canSubmit,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCherry,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_profile_button")
                ) {
                    Text(
                        text = if (isEditingExisting) "Save Clinical Profile" else "Create profile",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    subtitle: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(color = OnSurfaceEspresso)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, OutlineBorderTint)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge.copy(color = PrimaryCherry),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceCanvasBlush)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease $title",
                    tint = SecondaryDeepCherry
                )
            }
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceCanvasBlush)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase $title",
                    tint = SecondaryDeepCherry
                )
            }
        }
    }
}
