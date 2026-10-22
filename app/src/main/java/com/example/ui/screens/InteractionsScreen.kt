package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InteractionResult
import com.example.data.model.InteractionSeverity
import com.example.ui.MediMindViewModel
import com.example.ui.theme.MediTeal
import com.example.ui.theme.SeverityMinor
import com.example.ui.theme.SeverityModerate
import com.example.ui.theme.SeveritySafe
import com.example.ui.theme.SeveritySevere
import com.example.ui.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionsScreen(viewModel: MediMindViewModel) {
    val interactions by viewModel.interactions.collectAsState()
    val manualResult by viewModel.manualCheckResult.collectAsState()
    val explanationText by viewModel.selectedInteractionExplanation.collectAsState()
    val isExplaining by viewModel.isExplaining.collectAsState()
    val language by viewModel.language.collectAsState()

    var manualDrugA by remember { mutableStateOf("Warfarin") }
    var manualDrugB by remember { mutableStateOf("Aspirin") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("interactions_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Mandatory Safety Disclaimer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Medical Disclaimer",
                        tint = MediTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Medical Safety Disclaimer",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Localization.getString("disclaimer", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // 2. Section Header: Active Prescription Interferences
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Medication Interactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${interactions.size} interaction alerts identified across your active medicines",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (interactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SeveritySafe, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "No Known Harmful Interactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SeveritySafe
                            )
                            Text(
                                text = "Your active medicine list currently presents no documented major contraindications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }
        } else {
            items(interactions, key = { it.id }) { item ->
                InteractionCard(
                    item = item,
                    language = language,
                    onExplainWithAi = { viewModel.explainInteraction(item) }
                )
            }
        }

        // 3. Manual Drug Interaction Checker Tool
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_checker_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString("manual_checker", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Check potential interactions between any two prescription or OTC medicines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = manualDrugA,
                        onValueChange = { manualDrugA = it },
                        label = { Text("Medicine 1 (e.g., Warfarin)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_drug_a_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualDrugB,
                        onValueChange = { manualDrugB = it },
                        label = { Text("Medicine 2 (e.g., Aspirin, Ibuprofen, Sildenafil)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_drug_b_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.checkManualPair(manualDrugA, manualDrugB) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_manual_check_button")
                    ) {
                        Text("Check Pair Safety")
                    }

                    // Display Manual Result
                    manualResult?.let { result ->
                        Spacer(modifier = Modifier.height(16.dp))
                        val badgeColor = when (result.severity) {
                            InteractionSeverity.SEVERE -> SeveritySevere
                            InteractionSeverity.MODERATE -> SeverityModerate
                            InteractionSeverity.MINOR -> SeverityMinor
                            InteractionSeverity.SAFE -> SeveritySafe
                        }

                        Surface(
                            color = badgeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${result.drugA} + ${result.drugB}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = badgeColor,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = result.severity.name,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result.summary,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (result.recommendation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Advice: ${result.recommendation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Explanation Dialog
    if (isExplaining || explanationText != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExplanation() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MediTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini AI Clinical Insights", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (isExplaining) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = MediTeal)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Analyzing clinical literature...")
                    }
                } else {
                    Column {
                        Text(
                            text = explanationText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Important: Always verify these points with your doctor or pharmacist prior to changing any schedule.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissExplanation() }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun InteractionCard(
    item: InteractionResult,
    language: String,
    onExplainWithAi: () -> Unit
) {
    val severityColor = when (item.severity) {
        InteractionSeverity.SEVERE -> SeveritySevere
        InteractionSeverity.MODERATE -> SeverityModerate
        InteractionSeverity.MINOR -> SeverityMinor
        InteractionSeverity.SAFE -> SeveritySafe
    }

    val icon = when {
        item.isAllergyConflict -> Icons.Default.Warning
        item.isConditionConflict -> Icons.Default.LocalHospital
        else -> Icons.Default.Info
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(severityColor.copy(alpha = 0.5f))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interaction_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.drugA} ⚡ ${item.drugB}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = severityColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.severity.name,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.mechanism.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mechanism: ${item.mechanism}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Action: ${item.recommendation}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onExplainWithAi,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_explain_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MediTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.getString("explain_gemini", language))
            }
        }
    }
}
