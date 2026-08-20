package fr.quickvpn.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Background,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = Background,
    secondary = GreenDark,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    onError = Color.White,
    outline = Color(0x1FFFFFFF)
)

@Composable
fun QuickVpnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = QuickVpnTypography,
        content = content
    )
}
