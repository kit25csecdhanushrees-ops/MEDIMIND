package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.InteractionSeverity
import com.example.ui.MediMindViewModel
import com.example.ui.NavTab
import com.example.ui.theme.MediTeal
import com.example.ui.theme.SeverityMinor
import com.example.ui.theme.SeverityModerate
import com.example.ui.theme.SeveritySafe
import com.example.ui.theme.SeveritySevere
import com.example.ui.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: MediMindViewModel) {
    val doseLogs by viewModel.todayDoseLogs.collectAsState()
    val activeAlarm by viewModel.activeAlarmDose.collectAsState()
    val interactions by viewModel.interactions.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val language by viewModel.language.collectAsState()

    var showLangMenu by remember { mutableStateOf(false) }

    val severeInteractions = interactions.filter { it.severity == InteractionSeverity.SEVERE }
    val lowStockMeds = medicines.filter { it.stockCount <= it.lowStockThreshold }

    val takenCount = doseLogs.count { it.status == DoseStatus.TAKEN }
    val totalCount = doseLogs.size
    val progress = if (totalCount > 0) takenCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Profile & Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${Localization.getString("good_day", language)}, ${profile.name}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${profile.age} yrs • Primary Patient",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Language Selector
                            Box {
                                IconButton(
                                    onClick = { showLangMenu = true },
                                    modifier = Modifier.testTag("lang_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Change Language",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                DropdownMenu(
                                    expanded = showLangMenu,
                                    onDismissRequest = { showLangMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("English (EN)") },
                                        onClick = {
                                            viewModel.setLanguage("en")
                                            showLangMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("हिंदी (Hindi)") },
                                        onClick = {
                                            viewModel.setLanguage("hi")
                                            showLangMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("தமிழ் (Tamil)") },
                                        onClick = {
                                            viewModel.setLanguage("ta")
                                            showLangMenu = false
                                        }
                                    )
                                }
                            }

                            // Theme Toggle
                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adherence Progress & Streak Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MediTeal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 12",
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.getString("streak_days", language),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "$takenCount / $totalCount ${Localization.getString("doses_progress", language)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // 2. Active Urgent Alarm Banner (with Sound & Haptics)
        item {
            AnimatedVisibility(visible = activeAlarm != null) {
                activeAlarm?.let { dueDose ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD97706))),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_alarm_banner")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Alarm Active",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Localization.getString("due_alarm", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.dismissAlarm() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF92400E)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${dueDose.medName} • ${dueDose.dosage}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "Scheduled for ${dueDose.scheduledTime}. Please take with water.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF92400E)
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.takeDose(dueDose.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("alarm_take_button")
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Localization.getString("take", language))
                                }

                                OutlinedButton(
                                    onClick = { viewModel.snoozeDose(dueDose.id, 10) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("alarm_snooze_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("10m")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.skipDose(dueDose.id) },
                                    modifier = Modifier.testTag("alarm_skip_button")
                                ) {
                                    Text(Localization.getString("skip", language))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Critical Drug Interaction Alert Banner
        item {
            AnimatedVisibility(visible = severeInteractions.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SeveritySevere)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTab(NavTab.INTERACTIONS) }
                        .testTag("severe_interaction_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SeveritySevere.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Severe Alert",
                                    tint = SeveritySevere,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.getString("critical_alert", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SeveritySevere
                            )
                            Text(
                                text = "${severeInteractions.size} severe interaction detected (${severeInteractions.firstOrNull()?.drugA} + ${severeInteractions.firstOrNull()?.drugB}). Tap to review doctor guidance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }
        }

        // 4. Low Stock Alert
        item {
            AnimatedVisibility(visible = lowStockMeds.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = "Low Stock",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${Localization.getString("low_stock", language)}: ${lowStockMeds.joinToString { "${it.name} (${it.stockCount} left)" }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // 5. Section Title: Today's Schedule Timeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.getString("doses_today", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = viewModel.todayDateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6. Dose Logs List
        if (doseLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "All done",
                            tint = MediTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No doses scheduled for today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Add medicines in the Medicines tab to set up your schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(doseLogs, key = { it.id }) { log ->
                DoseTimelineCard(
                    log = log,
                    language = language,
                    onTake = { viewModel.takeDose(log.id) },
                    onSnooze = { mins -> viewModel.snoozeDose(log.id, mins) },
                    onSkip = { viewModel.skipDose(log.id) },
                    onMiss = { viewModel.missDose(log.id) }
                )
            }
        }
    }
}

@Composable
fun DoseTimelineCard(
    log: DoseLog,
    language: String,
    onTake: () -> Unit,
    onSnooze: (Int) -> Unit,
    onSkip: () -> Unit,
    onMiss: () -> Unit
) {
    var showSnoozeMenu by remember { mutableStateOf(false) }

    val statusColor = when (log.status) {
        DoseStatus.TAKEN -> SeveritySafe
        DoseStatus.DUE -> MediTeal
        DoseStatus.SNOOZED -> SeverityMinor
        DoseStatus.SKIPPED -> Color.Gray
        DoseStatus.MISSED -> SeveritySevere
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dose_card_${log.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.medName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = log.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MediTeal.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📁 ${log.folder}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MediTeal,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = log.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.scheduledTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Meal",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "After food",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (log.actualTime != null) {
                    Text(
                        text = "Taken at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(log.actualTime))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SeveritySafe,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick action buttons if not already taken
            if (log.status != DoseStatus.TAKEN) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTake,
                        colors = ButtonDefaults.buttonColors(containerColor = SeveritySafe),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dose_take_${log.id}")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString("take", language))
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showSnoozeMenu = true },
                            modifier = Modifier.testTag("dose_snooze_${log.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.getString("snooze", language))
                        }

                        DropdownMenu(
                            expanded = showSnoozeMenu,
                            onDismissRequest = { showSnoozeMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("5 minutes") }, onClick = { onSnooze(5); showSnoozeMenu = false })
                            DropdownMenuItem(text = { Text("10 minutes") }, onClick = { onSnooze(10); showSnoozeMenu = false })
                            DropdownMenuItem(text = { Text("15 minutes") }, onClick = { onSnooze(15); showSnoozeMenu = false })
                        }
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("dose_skip_${log.id}")
                    ) {
                        Text(Localization.getString("skip", language))
                    }

                    // Test Missed button (triggers caregiver notification)
                    OutlinedButton(
                        onClick = onMiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SeveritySevere),
                        modifier = Modifier.testTag("dose_miss_${log.id}")
                    ) {
                        Text(Localization.getString("missed", language))
                    }
                }
            }
        }
    }
}
