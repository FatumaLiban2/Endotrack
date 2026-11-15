package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val WarmClinicalSolaceColorScheme = lightColorScheme(
    primary = PrimaryCherry,
    onPrimary = OnPrimaryWhite,
    primaryContainer = SurfaceCanvasBlush,
    onPrimaryContainer = SecondaryDeepCherry,
    inversePrimary = InversePrimaryRose,
    secondary = SecondaryDeepCherry,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = OnSecondaryContainerDeep,
    tertiary = TertiaryWarm,
    onTertiary = OnPrimaryWhite,
    tertiaryContainer = OnTertiaryContainerWarm,
    onTertiaryContainer = OnSurfaceDark,
    background = SurfaceBlush,
    onBackground = OnSurfaceEspresso,
    surface = SurfaceBlush,
    onSurface = OnSurfaceEspresso,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariantMuted,
    surfaceTint = PrimaryCherry,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = ErrorRed,
    onError = OnPrimaryWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerDeep,
    outline = OutlineWarm,
    outlineVariant = OutlineBorderTint,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBlush
)

val EndoTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun EndoTrackTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = WarmClinicalSolaceColorScheme,
        typography = EndoTrackTypography,
        shapes = EndoTrackShapes,
        content = content
    )
}
