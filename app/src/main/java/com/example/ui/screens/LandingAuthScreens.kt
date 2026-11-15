package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DemoDataSeeder
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.WarmCard
import com.example.ui.theme.ErrorContainerLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainerDeep
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onDemoQuickStart: () -> Unit,
    isBusy: Boolean
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceBlush, SurfaceContainerLow, SurfaceBlush)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top badge pill matching Stitch landing
            Surface(
                shape = CircleShape,
                color = SurfaceCanvasBlush,
                border = BorderStroke(1.dp, OutlineBorderTint)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryCherry)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMPASSIONATE HEALTH TRACKER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SecondaryDeepCherry,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cherry motif hero logo
            Surface(
                modifier = Modifier.size(104.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, OutlineBorderTint),
                shadowElevation = 4.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.endotrack_app_icon_1794760705877),
                    contentDescription = "EndoTrack Cherry Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "EndoTrack",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = PrimaryCherry,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Track. Understand. Be heard.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = OnSurfaceEspresso,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Designed to help shorten the 5–7 year endometriosis diagnostic delay by turning daily pain & 58 clinical symptoms into structured evidence for your OB-GYN.",
                style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariantMuted),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            LandingFeaturePill(
                icon = Icons.Filled.Lock,
                text = "100% On-device privacy & local storage"
            )
            Spacer(modifier = Modifier.height(10.dp))
            LandingFeaturePill(
                icon = Icons.Filled.Description,
                text = "Clinician-ready PDF triage report export"
            )
            Spacer(modifier = Modifier.height(10.dp))
            LandingFeaturePill(
                icon = Icons.Filled.Analytics,
                text = "886-patient Logistic Regression pattern aid"
            )
            Spacer(modifier = Modifier.height(10.dp))
            LandingFeaturePill(
                icon = Icons.Filled.FavoriteBorder,
                text = "Evidence-based multi-cycle flare tracking"
            )

            Spacer(modifier = Modifier.height(20.dp))

            MedicalDisclaimerBanner()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGetStarted,
                enabled = !isBusy,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryCherry,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("landing_get_started_button")
            ) {
                Text(
                    text = "Get started",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDemoQuickStart,
                enabled = !isBusy,
                shape = CircleShape,
                border = BorderStroke(1.2.dp, OutlineBorderTint),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = SecondaryDeepCherry
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("landing_demo_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryCherry
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Explore Demo Patient (Elena)",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "On-device encryption • No cloud tracking required",
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LandingFeaturePill(
    icon: ImageVector,
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, OutlineBorderTint)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryDeepCherry,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceEspresso)
            )
        }
    }
}

@Composable
fun AuthScreen(
    authError: String?,
    isBusy: Boolean,
    onSignIn: (String, String) -> Unit,
    onCreateAccount: (String, String, String) -> Unit,
    onSignInDemo: () -> Unit,
    onClearError: () -> Unit,
    onBackToLanding: () -> Unit
) {
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBlush)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.endotrack_app_icon_1794760705877),
                    contentDescription = "EndoTrack Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, OutlineBorderTint, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EndoTrack",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = PrimaryCherry,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Private Local Account & Clinical Journal",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                    )
                }
                TextButton(onClick = onBackToLanding) {
                    Text("Overview", color = SecondaryDeepCherry)
                }
            }

            // Mode toggle pill
            Surface(
                shape = CircleShape,
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, OutlineBorderTint),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    listOf(false to "Sign In", true to "Create Account").forEach { (signUp, title) ->
                        val active = isSignUpMode == signUp
                        Surface(
                            onClick = {
                                isSignUpMode = signUp
                                onClearError()
                            },
                            shape = CircleShape,
                            color = if (active) PrimaryCherry else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag(if (signUp) "auth_tab_signup" else "auth_tab_signin")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (active) Color.White else SecondaryDeepCherry
                                    )
                                )
                            }
                        }
                    }
                }
            }

            WarmCard {
                Text(
                    text = if (isSignUpMode) "Create your private journal" else "Welcome back",
                    style = MaterialTheme.typography.headlineSmall.copy(color = OnSurfaceEspresso)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSignUpMode) {
                        "Your account and 58-symptom history stay encrypted on this device."
                    } else {
                        "Sign in to access your cycle timeline, Logistic Regression insights, and clinician reports."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!authError.isNullOrBlank()) {
                    Surface(
                        color = ErrorContainerLight,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authError,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnErrorContainerDeep),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCherry,
                    unfocusedBorderColor = OutlineBorderTint,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            onClearError()
                        },
                        label = { Text("First name or nickname") },
                        placeholder = { Text("e.g., Elena") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        onClearError()
                    },
                    label = { Text("Email address") },
                    placeholder = { Text("name@example.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onClearError()
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Minimum 6 characters") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            onCreateAccount(fullName, email, password)
                        } else {
                            onSignIn(email, password)
                        }
                    },
                    enabled = !isBusy,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCherry,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isSignUpMode) "Create Account & Continue" else "Sign In",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Demo account quick login card
            WarmCard(containerColor = SurfaceContainerLow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PersonOutline,
                        contentDescription = null,
                        tint = SecondaryDeepCherry,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Instant Clinical Demo (Pre-Seeded Cycles)",
                        style = MaterialTheme.typography.labelLarge.copy(color = SecondaryDeepCherry)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in as Elena (${DemoDataSeeder.DEMO_EMAIL}) with 2 pre-logged menstrual cycles, longitudinal pain flares, and PDF report data ready.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariantMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSignInDemo,
                    enabled = !isBusy,
                    shape = CircleShape,
                    border = BorderStroke(1.2.dp, PrimaryCherry),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryCherry
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_demo_signin_button")
                ) {
                    Text(
                        text = "Sign In as Demo Patient (Elena)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            MedicalDisclaimerBanner()
        }
    }
}
