package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.EndoBottomNavBar
import com.example.ui.components.EndoRoutes
import com.example.ui.components.EndoTrackTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateProfileScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.InsightsReportScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.SymptomLogScreen
import com.example.ui.theme.EndoTrackTheme
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.viewmodel.EndoTrackViewModel
import com.example.ui.viewmodel.SessionStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EndoTrackTheme {
                EndoTrackRootApp()
            }
        }
    }
}

@Composable
fun EndoTrackRootApp(
    viewModel: EndoTrackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logDraft by viewModel.logDraft.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val session = uiState.sessionStatus) {
        SessionStatus.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceBlush),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryCherry)
            }
        }

        SessionStatus.LoggedOut -> {
            var showAuth by rememberSaveable { mutableStateOf(uiState.hasSeenOnboarding) }
            if (!showAuth) {
                LandingScreen(
                    onGetStarted = { showAuth = true },
                    onDemoQuickStart = { viewModel.signInAsDemo() },
                    isBusy = uiState.isAuthBusy
                )
            } else {
                AuthScreen(
                    authError = uiState.authError,
                    isBusy = uiState.isAuthBusy,
                    onSignIn = { email, password -> viewModel.signIn(email, password) },
                    onCreateAccount = { name, email, password ->
                        viewModel.createAccount(name, email, password)
                    },
                    onSignInDemo = { viewModel.signInAsDemo() },
                    onClearError = { viewModel.clearAuthError() },
                    onBackToLanding = { showAuth = false }
                )
            }
        }

        is SessionStatus.LoggedIn -> {
            val user = session.user
            if (!user.hasCompletedProfile) {
                CreateProfileScreen(
                    user = user,
                    isEditingExisting = false,
                    onSaveProfile = { name, ageRange, ageNum, cycleLen, periodDur, reason, goals, baseline, lastPeriod ->
                        viewModel.saveClinicalProfile(
                            fullName = name,
                            ageRange = ageRange,
                            ageNumeric = ageNum,
                            cycleLengthDays = cycleLen,
                            periodDurationDays = periodDur,
                            journeyReason = reason,
                            helpGoals = goals,
                            baselineHistory = baseline,
                            lastPeriodStartEpochDay = lastPeriod
                        )
                    },
                    onBack = {},
                    onSignOut = { viewModel.signOut() }
                )
            } else {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: EndoRoutes.HOME
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.statusBannerMessage) {
                    val msg = uiState.statusBannerMessage
                    if (!msg.isNullOrBlank()) {
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusBanner()
                    }
                }

                val showChromeBars = currentRoute != EndoRoutes.PROFILE
                val topSubtitle = when (currentRoute) {
                    EndoRoutes.HOME -> "Home"
                    EndoRoutes.LOG -> "Log"
                    EndoRoutes.INSIGHTS -> "Insights"
                    EndoRoutes.LEARN -> "Learn"
                    EndoRoutes.PROFILE -> "Clinical Profile"
                    else -> "Home"
                }

                Scaffold(
                    containerColor = SurfaceBlush,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = SecondaryDeepCherry,
                                contentColor = Color.White
                            )
                        }
                    },
                    topBar = {
                        if (showChromeBars) {
                            EndoTrackTopBar(
                                subtitle = topSubtitle,
                                onProfileClick = {
                                    navController.navigate(EndoRoutes.PROFILE) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (showChromeBars) {
                            EndoBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { target ->
                                    if (target == EndoRoutes.LOG && currentRoute != EndoRoutes.LOG) {
                                        viewModel.openLogForDay(logDraft.epochDay)
                                    }
                                    navController.navigate(target) {
                                        popUpTo(EndoRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = EndoRoutes.HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(EndoRoutes.HOME) {
                            HomeDashboardScreen(
                                user = user,
                                logs = uiState.logs,
                                riskResult = uiState.multiCycleRisk,
                                weightsEntity = uiState.modelWeightsEntity,
                                onStartTodayLog = { epochDay ->
                                    viewModel.openLogForDay(epochDay)
                                    navController.navigate(EndoRoutes.LOG) {
                                        popUpTo(EndoRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                    }
                                },
                                onOpenInsights = {
                                    navController.navigate(EndoRoutes.INSIGHTS) {
                                        popUpTo(EndoRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(EndoRoutes.LOG) {
                            SymptomLogScreen(
                                user = user,
                                logs = uiState.logs,
                                draft = logDraft,
                                onSelectDay = { viewModel.openLogForDay(it) },
                                onPainLevelChange = { viewModel.updatePainLevel(it) },
                                onHasPeriodChange = { viewModel.updateHasPeriod(it) },
                                onFlowVolumeChange = { viewModel.updateFlowVolume(it) },
                                onTookOtcChange = { viewModel.updateTookOtcMedication(it) },
                                onOtcNameChange = { viewModel.updateOtcMedicationName(it) },
                                onOtcRelievedChange = { viewModel.updateOtcRelievedPain(it) },
                                onToggleImpactFlag = { viewModel.toggleImpactFlag(it) },
                                onNotesChange = { viewModel.updateNotes(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onToggleCategory = { viewModel.toggleCategoryExpanded(it) },
                                onToggleSymptom = { viewModel.toggleSymptom(it) },
                                onClearDraft = { viewModel.clearDraft() },
                                onSaveLog = { viewModel.saveDailyLog() },
                                onDeleteLog = { viewModel.deleteCurrentDraftLog() },
                                onDismissFeedback = { viewModel.dismissSavedFeedbackDialog() },
                                onNavigateToInsights = {
                                    navController.navigate(EndoRoutes.INSIGHTS) {
                                        popUpTo(EndoRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(EndoRoutes.INSIGHTS) {
                            InsightsReportScreen(
                                user = user,
                                logs = uiState.logs,
                                cycleSummaries = uiState.cycleSummaries,
                                riskResult = uiState.multiCycleRisk,
                                weightsEntity = uiState.modelWeightsEntity,
                                timeframeDays = uiState.timeframeDays,
                                clinicianNote = uiState.clinicianNote,
                                onSelectTimeframe = { viewModel.setTimeframeDays(it) },
                                onClinicianNoteChange = { viewModel.updateClinicianNote(it) },
                                onCalibrateOutcome = { viewModel.calibrateModelWithOutcome(it) },
                                onResetWeights = { viewModel.resetModelToBaseline() },
                                onExportPdf = { viewModel.exportClinicianPdf(context) },
                                onEditLogDay = { day ->
                                    viewModel.openLogForDay(day)
                                    navController.navigate(EndoRoutes.LOG) {
                                        popUpTo(EndoRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(EndoRoutes.LEARN) {
                            LearnScreen(weightsEntity = uiState.modelWeightsEntity)
                        }

                        composable(EndoRoutes.PROFILE) {
                            CreateProfileScreen(
                                user = user,
                                isEditingExisting = true,
                                onSaveProfile = { name, ageRange, ageNum, cycleLen, periodDur, reason, goals, baseline, lastPeriod ->
                                    viewModel.saveClinicalProfile(
                                        fullName = name,
                                        ageRange = ageRange,
                                        ageNumeric = ageNum,
                                        cycleLengthDays = cycleLen,
                                        periodDurationDays = periodDur,
                                        journeyReason = reason,
                                        helpGoals = goals,
                                        baselineHistory = baseline,
                                        lastPeriodStartEpochDay = lastPeriod
                                    )
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() },
                                onSignOut = { viewModel.signOut() }
                            )
                        }
                    }
                }
            }
        }
    }
}
