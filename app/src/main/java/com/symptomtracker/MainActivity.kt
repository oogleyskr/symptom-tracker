package com.symptomtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.symptomtracker.ui.*
import com.symptomtracker.ui.billing.BillingManager
import com.symptomtracker.ui.insights.InsightsScreen
import com.symptomtracker.ui.log.LogScreen
import com.symptomtracker.ui.onboarding.OnboardingPrefs
import com.symptomtracker.ui.onboarding.OnboardingScreen
import com.symptomtracker.ui.report.ReportScreen
import com.symptomtracker.ui.theme.SymptomTrackerTheme
import com.symptomtracker.ui.timeline.TimelineScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingPrefs: OnboardingPrefs
    @Inject lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SymptomTrackerTheme {
                var showOnboarding by remember { mutableStateOf(!onboardingPrefs.hasSeenOnboarding) }

                AnimatedContent(
                    targetState = showOnboarding,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                ) { onboarding ->
                    if (onboarding) {
                        OnboardingScreen(
                            onFinish = {
                                onboardingPrefs.hasSeenOnboarding = true
                                showOnboarding = false
                            }
                        )
                    } else {
                        MainApp(billingManager = billingManager)
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(billingManager: BillingManager) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Log.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Log.route) { LogScreen() }
            composable(Screen.Timeline.route) { TimelineScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Report.route) { ReportScreen() }
        }
    }
}
