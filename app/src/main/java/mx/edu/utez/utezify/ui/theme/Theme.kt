package mx.edu.utez.utezify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = SpotifyBlack,
    primaryContainer = SpotifyDarkGreen,
    onPrimaryContainer = SpotifyWhite,
    secondary = SpotifyGreen,
    onSecondary = SpotifyBlack,
    background = SpotifyBlack,
    onBackground = SpotifyWhite,
    surface = SpotifyDarkGrey,
    onSurface = SpotifyWhite,
    surfaceVariant = SpotifyMediumGrey,
    onSurfaceVariant = SpotifyLightGrey,
    error = SpotifyRed,
    onError = SpotifyWhite
)

@Composable
fun UTEZifyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
