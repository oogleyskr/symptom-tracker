package com.symptomtracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Purple/teal dark theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9B7DFF),
    onPrimary = Color(0xFF1A0060),
    primaryContainer = Color(0xFF2D0087),
    onPrimaryContainer = Color(0xFFCDBDFF),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00374A),
    secondaryContainer = Color(0xFF004E62),
    onSecondaryContainer = Color(0xFF97ECFF),
    tertiary = Color(0xFFFF8A65),
    background = Color(0xFF0D0D14),
    surface = Color(0xFF13131F),
    surfaceVariant = Color(0xFF1E1E2E),
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFFB0B0C4),
    error = Color(0xFFFF6B6B),
)

@Composable
fun SymptomTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content,
    )
}
