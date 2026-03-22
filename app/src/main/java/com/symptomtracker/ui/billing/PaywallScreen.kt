package com.symptomtracker.ui.billing

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    billingManager: BillingManager,
) {
    val state by billingManager.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("annual") }

    if (state.isPro) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            item {
                Icon(Icons.Default.AutoAwesome, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("SymptomTracker Pro", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Unlock the full power of AI health tracking",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }

            item {
                // Feature list
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProFeatureRow(Icons.Default.AutoAwesome, "AI Insights", "Full Gemini-powered pattern analysis")
                        ProFeatureRow(Icons.Default.Description, "Doctor Reports", "Export beautiful PDFs for appointments")
                        ProFeatureRow(Icons.Default.CloudUpload, "Encrypted Backup", "Never lose your data")
                        ProFeatureRow(Icons.Default.History, "Unlimited History", "No 30-day cap")
                        ProFeatureRow(Icons.Default.Science, "Correlation Engine", "See how meds affect symptoms")
                    }
                }
            }

            item {
                // Plan selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanCard(
                        selected = selectedPlan == "annual",
                        title = "Annual",
                        price = state.annualPrice + "/year",
                        badge = "Save 37%",
                        onClick = { selectedPlan = "annual" },
                    )
                    PlanCard(
                        selected = selectedPlan == "monthly",
                        title = "Monthly",
                        price = state.monthlyPrice + "/month",
                        badge = null,
                        onClick = { selectedPlan = "monthly" },
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val sku = if (selectedPlan == "annual") SKU_PRO_ANNUAL else SKU_PRO_MONTHLY
                        billingManager.launchBillingFlow(context as Activity, sku)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Start 7-Day Free Trial",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text("Cancel anytime. No charge during trial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ProFeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlanCard(selected: Boolean, title: String, price: String, badge: String?, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().let {
            if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            else it
        },
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(price, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (badge != null) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall) {
                    Text(badge, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
