package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    secondary = SpotifyGreenBright,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextLight,
    surface = SurfaceGrey,
    onSurface = TextLight,
    surfaceVariant = ActivePill,
    onSurfaceVariant = TextGrey
)

// Music player experiences are primarily beautiful in complete dark mode
private val LightColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    secondary = SpotifyGreenBright,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextLight,
    surface = SurfaceGrey,
    onSurface = TextLight,
    surfaceVariant = ActivePill,
    onSurfaceVariant = TextGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the premium music experience
    dynamicColor: Boolean = false, // Disable dynamic colors to keep Spotify visual consistency
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
