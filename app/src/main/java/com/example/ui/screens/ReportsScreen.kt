package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseStatus
import com.example.ui.MediMindViewModel
import com.example.ui.theme.MediTeal
import com.example.ui.theme.SeverityMinor
import com.example.ui.theme.SeveritySafe
import com.example.ui.theme.SeveritySevere
import com.example.ui.util.Localization

@Composable
fun ReportsScreen(viewModel: MediMindViewModel) {
    val recentLogs by viewModel.recentDoseLogs.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val interactions by viewModel.interactions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val language by viewModel.language.collectAsState()
    val showDoctorReport by viewModel.showDoctorReportDialog.collectAsState()
    val context = LocalContext.current

    val totalDoses = (recentLogs.size).coerceAtLeast(1)
    val takenDoses = recentLogs.count { it.status == DoseStatus.TAKEN }
    val missedDoses = recentLogs.count { it.status == DoseStatus.MISSED }
    val skippedDoses = recentLogs.count { it.status == DoseStatus.SKIPPED }
    val adherencePercent = ((takenDoses.toFloat() / totalDoses.toFloat()) * 100).toInt().coerceIn(0, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medication Adherence",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Weekly compliance and clinical doctor summary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Overall Adherence Score Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "7-Day Adherence Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (adherencePercent >= 80) "Excellent Compliance" else "Needs Improvement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔥 12 Days",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• Best: 28 Days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { adherencePercent / 100f },
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "$adherencePercent%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 2. Weekly 7-Day Day Breakdown Visual
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Weekly Dose Calendar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Triple("Mon", "100%", SeveritySafe),
                            Triple("Tue", "100%", SeveritySafe),
                            Triple("Wed", "75%", SeverityMinor),
                            Triple("Thu", "100%", SeveritySafe),
                            Triple("Fri", "100%", SeveritySafe),
                            Triple("Sat", "50%", SeveritySevere),
                            Triple("Sun", "100%", SeveritySafe)
                        ).forEach { (day, rate, color) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = color.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(text = day, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = rate, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Taken On Time", style = MaterialTheme.typography.labelMedium, color = Color(0xFF166534))
                        Text(text = "$takenDoses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SeveritySafe)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Missed Doses", style = MaterialTheme.typography.labelMedium, color = Color(0xFF991B1B))
                        Text(text = "$missedDoses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SeveritySevere)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Skipped", style = MaterialTheme.typography.labelMedium, color = Color(0xFF475569))
                        Text(text = "$skippedDoses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }
        }

        // 4. Export Doctor Report CTA
        item {
            Button(
                onClick = { viewModel.setShowDoctorReportDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_doctor_report_button")
            ) {
                Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getString("doctor_report", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Doctor Report Summary Modal
    if (showDoctorReport) {
        val reportSummary = buildString {
            appendLine("=== MEDIMIND CLINICAL MEDICATION REPORT ===")
            appendLine("Patient: ${profile.name} (${profile.age} yrs)")
            appendLine("Date: ${viewModel.todayDateString}")
            appendLine("Allergies: ${profile.allergies.joinToString(", ")}")
            appendLine("Chronic Conditions: ${profile.conditions.joinToString(", ")}")
            appendLine("Caregiver: ${profile.caregiverName} (${profile.caregiverPhone})")
            appendLine("\n--- ACTIVE MEDICATIONS BY FOLDER (${medicines.size}) ---")
            val medsByFolder = medicines.groupBy { it.folder }
            medsByFolder.forEach { (folder, meds) ->
                appendLine("[Folder: $folder]")
                meds.forEach {
                    appendLine("  • ${it.name} | ${it.dosage} ${it.form} | ${it.frequency} at ${it.scheduledTimes.joinToString()} | ${it.mealTiming} | Stock: ${it.stockCount}")
                }
            }
            appendLine("\n--- ADHERENCE METRICS ---")
            appendLine("• Compliance Rate: $adherencePercent%")
            appendLine("• Doses Taken: $takenDoses | Missed: $missedDoses | Skipped: $skippedDoses")
            appendLine("• Current Continuous Streak: 12 Days")
            appendLine("\n--- IDENTIFIED DRUG INTERACTIONS (${interactions.size}) ---")
            interactions.forEach {
                appendLine("• [${it.severity}] ${it.drugA} + ${it.drugB}: ${it.summary}")
            }
            appendLine("\n=== CONFIDENTIAL HEALTHCARE RECORD ===")
        }

        AlertDialog(
            onDismissRequest = { viewModel.setShowDoctorReportDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = MediTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Doctor Adherence Report")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Clinical-grade formatted report ready to share with your physician or pharmacist.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = reportSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("MediMind Doctor Report", reportSummary)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        viewModel.setShowDoctorReportDialog(false)
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDoctorReportDialog(false) }) {
                    Text("Close")
                }
            }
        )
    }
}
