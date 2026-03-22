package com.symptomtracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Log : Screen("log", "Log", Icons.Default.Add)
    object Timeline : Screen("timeline", "Timeline", Icons.Default.Timeline)
    object Insights : Screen("insights", "Insights", Icons.Default.AutoAwesome)
    object Report : Screen("report", "Report", Icons.Default.Description)
}

val bottomNavItems = listOf(
    Screen.Log,
    Screen.Timeline,
    Screen.Insights,
    Screen.Report,
)
