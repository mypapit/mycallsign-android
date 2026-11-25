package net.mypapit.mobile.callsignview.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val MYCallsignDarkColorScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = OnOrangeAccent,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OnOrangeContainer,
    secondary = SteelSecondary,
    onSecondary = OnSteelSecondary,
    secondaryContainer = SteelContainer,
    onSecondaryContainer = OnSteelContainer,
    tertiary = RadioCyan,
    onTertiary = OnRadioCyan,
    tertiaryContainer = RadioCyanContainer,
    onTertiaryContainer = RadioCyan,
    background = CharcoalBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = SurfaceLowest,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceDefault,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    outline = BorderOutline,
    outlineVariant = BorderSubtle,
    error = SignalRed,
    onError = OnOrangeAccent,
    errorContainer = SignalRedContainer,
    onErrorContainer = OnSignalRedContainer
)

private val MYCallsignShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MYCallsignTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MYCallsignDarkColorScheme,
        typography = MYCallsignTypography,
        shapes = MYCallsignShapes,
        content = content
    )
}
