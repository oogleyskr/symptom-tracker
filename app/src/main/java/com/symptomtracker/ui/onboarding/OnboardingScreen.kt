package com.symptomtracker.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accentColor: androidx.compose.ui.graphics.Color,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.HealthAndSafety,
            title = "Track Your Health",
            subtitle = "Log symptoms, medications, and side effects in seconds. No complexity, just data.",
            accentColor = androidx.compose.ui.graphics.Color(0xFF9B7DFF),
        ),
        OnboardingPage(
            icon = Icons.Default.AutoAwesome,
            title = "AI Spots the Patterns",
            subtitle = "Gemini AI analyzes your logs and surfaces what's actually going on with your health.",
            accentColor = androidx.compose.ui.graphics.Color(0xFF4DD0E1),
        ),
        OnboardingPage(
            icon = Icons.Default.Description,
            title = "Share With Your Doctor",
            subtitle = "Generate a clean PDF summary for your next appointment. One tap, done.",
            accentColor = androidx.compose.ui.graphics.Color(0xFFFF8A65),
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Skip button — always visible
        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        ) {
            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            OnboardingPage(page = pages[page])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val isLast = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    if (isLast) "Get Started" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = page.accentColor.copy(alpha = 0.15f),
            modifier = Modifier.size(140.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = page.accentColor,
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 26.sp,
        )
    }
}
