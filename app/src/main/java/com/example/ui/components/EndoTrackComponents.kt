package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ml.RiskTier
import com.example.ui.theme.OnSurfaceEspresso
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineBorderTint
import com.example.ui.theme.PrimaryCherry
import com.example.ui.theme.RiskHighContainer
import com.example.ui.theme.RiskHighText
import com.example.ui.theme.RiskLowContainer
import com.example.ui.theme.RiskLowText
import com.example.ui.theme.RiskModerateContainer
import com.example.ui.theme.RiskModerateText
import com.example.ui.theme.SecondaryDeepCherry
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceCanvasBlush
import com.example.ui.theme.SurfaceContainerLow

object EndoRoutes {
    const val LANDING = "landing"
    const val AUTH = "auth"
    const val PROFILE = "profile"
    const val HOME = "home"
    const val LOG = "log"
    const val INSIGHTS = "insights"
    const val LEARN = "learn"
}

@Composable
fun EndoTrackTopBar(
    subtitle: String,
    onProfileClick: () -> Unit,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Surface(
        color = SurfaceBlush,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = SecondaryDeepCherry
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Image(
                    painter = painterResource(id = R.drawable.endotrack_app_icon_1794760705877),
                    contentDescription = "EndoTrack Cherry Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, OutlineBorderTint, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EndoTrack",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCherry
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrimaryCherry.copy(alpha = 0.6f))
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariantMuted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SecondaryDeepCherry)
                        .clickable(onClick = onProfileClick)
                        .testTag("top_bar_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Open Clinical Profile",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OutlineBorderTint)
            )
        }
    }
}

@Composable
fun WarmCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    borderColor: Color = OutlineBorderTint,
    cornerRadiusDp: Int = 24,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusDp.dp),
        color = containerColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun MedicalDisclaimerBanner(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceContainerLow
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, OutlineBorderTint)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = "Medical Disclaimer",
                tint = SecondaryDeepCherry,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "RESPONSIBLE AI & MEDICAL DISCLAIMER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SecondaryDeepCherry,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "EndoTrack is a pre-diagnostic screening and pattern-recognition aid. It does not diagnose endometriosis, prescribe medication, or replace a healthcare professional.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceEspresso
                    )
                )
            }
        }
    }
}

@Composable
fun RiskTierPill(
    riskTier: RiskTier,
    probability: Double,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (riskTier) {
        RiskTier.LOW -> RiskLowContainer to RiskLowText
        RiskTier.MODERATE -> RiskModerateContainer to RiskModerateText
        RiskTier.HIGH -> RiskHighContainer to RiskHighText
    }
    val pct = (probability * 100.0).toInt()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.25f))
    ) {
        Text(
            text = "${riskTier.label} • $pct%",
            style = MaterialTheme.typography.labelSmall.copy(
                color = fg,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EndoBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, OutlineBorderTint),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            val items = listOf(
                Quad(EndoRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
                Quad(EndoRoutes.LOG, "Log", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                Quad(EndoRoutes.INSIGHTS, "Insights", Icons.Filled.Insights, Icons.Outlined.Insights),
                Quad(EndoRoutes.LEARN, "Learn", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
            )
            items.forEach { (route, label, filledIcon, outlinedIcon) ->
                val selected = currentRoute == route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    modifier = Modifier.testTag("bottom_nav_$route"),
                    icon = {
                        Icon(
                            imageVector = if (selected) filledIcon else outlinedIcon,
                            contentDescription = label
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryCherry,
                        selectedTextColor = PrimaryCherry,
                        indicatorColor = SurfaceCanvasBlush,
                        unselectedIconColor = OnSurfaceVariantMuted,
                        unselectedTextColor = OnSurfaceVariantMuted
                    )
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun CenteredMaxWidthColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}
