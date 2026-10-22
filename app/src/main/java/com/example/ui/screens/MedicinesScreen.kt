package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.clinical.ClinicalRules
import com.example.data.model.Medicine
import com.example.ui.MediMindViewModel
import com.example.ui.theme.MediTeal
import com.example.ui.theme.SeverityMinor
import com.example.ui.theme.SeveritySevere
import com.example.ui.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(viewModel: MediMindViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val showAddDialog by viewModel.showAddMedicineDialog.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val customFolders by viewModel.customFolders.collectAsState()
    val language by viewModel.language.collectAsState()

    var medToDelete by remember { mutableStateOf<Medicine?>(null) }
    var medToMove by remember { mutableStateOf<Medicine?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }
    var groupByFolders by remember { mutableStateOf(true) }

    // Derived list of distinct folders that have medicines or exist in customFolders
    val allFolderNames = remember(medicines, customFolders) {
        val set = linkedSetOf<String>()
        medicines.forEach { set.add(it.folder) }
        customFolders.forEach { set.add(it) }
        set.toList()
    }

    val filteredMeds = remember(medicines, selectedFolder) {
        if (selectedFolder == "All") medicines else medicines.filter { it.folder == selectedFolder }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowAddMedicineDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_medicine_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("medicines_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.getString("nav_medicines", language),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${medicines.size} medications across ${allFolderNames.size} folders",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // View toggle: Group by Folders or Flat
                    IconButton(
                        onClick = { groupByFolders = !groupByFolders },
                        modifier = Modifier.testTag("toggle_folder_view_button")
                    ) {
                        Icon(
                            imageVector = if (groupByFolders) Icons.Default.FolderOpen else Icons.Default.ViewAgenda,
                            contentDescription = "Toggle folder grouping",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Folder Shelf Filter Bar
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Medicine Folders & Cabinets",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = { showNewFolderDialog = true },
                            modifier = Modifier.testTag("create_folder_text_button")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ New Folder", fontSize = 12.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("folder_shelf_row")
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFolder == "All",
                                onClick = { viewModel.setSelectedFolder("All") },
                                label = { Text("📁 All (${medicines.size})") },
                                modifier = Modifier.testTag("folder_chip_all")
                            )
                        }

                        items(allFolderNames) { folderName ->
                            val count = medicines.count { it.folder == folderName }
                            FilterChip(
                                selected = selectedFolder == folderName,
                                onClick = { viewModel.setSelectedFolder(folderName) },
                                label = { Text("📁 $folderName ($count)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (selectedFolder == folderName) Icons.Default.FolderOpen else Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedFolder == folderName) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                modifier = Modifier.testTag("folder_chip_$folderName")
                            )
                        }
                    }
                }
            }

            if (medicines.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = MediTeal,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No medicines in this folder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap the '+' button below to add a prescription to this folder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (groupByFolders && selectedFolder == "All") {
                // Folder Cabinets Grouped View
                val foldersWithMeds = allFolderNames.filter { folder ->
                    medicines.any { it.folder == folder }
                }

                items(foldersWithMeds, key = { it }) { folderName ->
                    val medsInFolder = medicines.filter { it.folder == folderName }
                    FolderCabinetSection(
                        folderName = folderName,
                        medicines = medsInFolder,
                        language = language,
                        onDeleteMed = { medToDelete = it },
                        onMoveMed = { medToMove = it }
                    )
                }
            } else {
                // Filtered List View
                items(filteredMeds, key = { it.id }) { med ->
                    MedicineCard(
                        medicine = med,
                        language = language,
                        onDelete = { medToDelete = med },
                        onMove = { medToMove = med }
                    )
                }
            }
        }
    }

    // Add Medicine Dialog
    if (showAddDialog) {
        AddMedicineDialog(
            viewModel = viewModel,
            foldersList = allFolderNames,
            initialFolder = if (selectedFolder != "All") selectedFolder else "Daily Essentials",
            onDismiss = { viewModel.setShowAddMedicineDialog(false) }
        )
    }

    // Create New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, tint = MediTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Medicine Folder", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Organize medications by condition, regimen, or family member (e.g. 'Cardiovascular', 'Pain Management', 'Morning Pack').",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        label = { Text("Folder Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_folder_name_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderNameInput.isNotBlank()) {
                            viewModel.addCustomFolder(newFolderNameInput)
                            newFolderNameInput = ""
                            showNewFolderDialog = false
                        }
                    },
                    enabled = newFolderNameInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Create Folder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move Medicine to Another Folder Dialog
    medToMove?.let { med ->
        var targetFolder by remember { mutableStateOf(med.folder) }
        AlertDialog(
            onDismissRequest = { medToMove = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = null, tint = MediTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Move to Folder", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Move '${med.name}' to another folder:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(allFolderNames) { folder ->
                            Surface(
                                color = if (targetFolder == folder) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetFolder = folder }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MediTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = folder, fontWeight = if (targetFolder == folder) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addMedicine(
                            name = med.name,
                            genericName = med.genericName,
                            dosage = med.dosage,
                            form = med.form,
                            frequency = med.frequency,
                            times = med.scheduledTimes,
                            mealTiming = med.mealTiming,
                            stockCount = med.stockCount,
                            lowStockThreshold = med.lowStockThreshold,
                            notes = med.notes,
                            folder = targetFolder
                        )
                        viewModel.deleteMedicine(med.id)
                        medToMove = null
                    }
                ) {
                    Text("Move")
                }
            },
            dismissButton = {
                TextButton(onClick = { medToMove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    medToDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medToDelete = null },
            title = { Text("Delete ${med.name}?") },
            text = { Text("Are you sure you want to remove this medication from folder '${med.folder}'? This will also remove its scheduled doses.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMedicine(med.id)
                        medToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeveritySevere)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { medToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FolderCabinetSection(
    folderName: String,
    medicines: List<Medicine>,
    language: String,
    onDeleteMed: (Medicine) -> Unit,
    onMoveMed: (Medicine) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val totalPills = medicines.sumOf { it.stockCount }
    val hasLowStock = medicines.any { it.stockCount <= it.lowStockThreshold }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("folder_cabinet_$folderName")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Folder Banner Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MediTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                contentDescription = null,
                                tint = MediTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${medicines.size} medications • $totalPills pills total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasLowStock) {
                        Surface(
                            color = SeveritySevere.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "LOW STOCK",
                                color = SeveritySevere,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    medicines.forEach { med ->
                        MedicineCard(
                            medicine = med,
                            language = language,
                            onDelete = { onDeleteMed(med) },
                            onMove = { onMoveMed(med) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCard(
    medicine: Medicine,
    language: String,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    val isLowStock = medicine.stockCount <= medicine.lowStockThreshold
    val stockRatio = (medicine.stockCount.toFloat() / 30f).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(medicine.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = Color(medicine.colorHex),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (medicine.genericName.isNotEmpty()) {
                            Text(
                                text = medicine.genericName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFileMove,
                            contentDescription = "Move to folder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Medicine",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Chips Row including Folder Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MediTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(12.dp), tint = MediTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = medicine.folder,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MediTeal
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${medicine.dosage} • ${medicine.form}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = medicine.mealTiming,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pill Stock Bar & Low Stock Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Stock: ${medicine.stockCount} pills remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLowStock) SeveritySevere else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                )

                if (isLowStock) {
                    Surface(
                        color = SeveritySevere.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LOW STOCK",
                            color = SeveritySevere,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { stockRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isLowStock) SeveritySevere else MediTeal,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (medicine.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Note: ${medicine.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    viewModel: MediMindViewModel,
    foldersList: List<String>,
    initialFolder: String,
    onDismiss: () -> Unit
) {
    val searchResults by viewModel.fdaSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingFda.collectAsState()
    val existingMeds by viewModel.medicines.collectAsState()

    var name by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 tablet") }
    var selectedForm by remember { mutableStateOf("Tablet") }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    var mealTiming by remember { mutableStateOf("After food") }
    var stockCount by remember { mutableIntStateOf(30) }
    var selectedFolder by remember { mutableStateOf(initialFolder) }
    var notes by remember { mutableStateOf("") }
    val scheduledTimes = remember { mutableStateListOf("08:00") }

    // Voice Input Launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                name = spokenText
                viewModel.searchFda(spokenText)
            }
        }
    }

    // Check interaction preview with existing medicines in real time!
    val potentialInteraction = remember(name, genericName, existingMeds) {
        if (name.length > 2) {
            existingMeds.firstNotNullOfOrNull { existing ->
                ClinicalRules.checkPair(name, existing.name)
            }
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Medicine to Folder",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Folder Selection Chips
                item {
                    Text(
                        text = "Assign to Folder / Cabinet:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MediTeal
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(foldersList) { folder ->
                            FilterChip(
                                selected = selectedFolder == folder,
                                onClick = { selectedFolder = folder },
                                label = { Text("📁 $folder") }
                            )
                        }
                    }
                }

                // Search as you type with openFDA
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.searchFda(it)
                        },
                        label = { Text("Medicine Name (openFDA search)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak medicine name")
                                        }
                                        runCatching { voiceLauncher.launch(intent) }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = MediTeal)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_med_name_input"),
                        singleLine = true
                    )
                }

                // Autocomplete results chips
                if (searchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "openFDA Suggestions:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MediTeal
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(searchResults) { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        name = suggestion.brandName
                                        genericName = suggestion.genericName
                                        selectedForm = suggestion.dosageForm
                                        if (suggestion.warningsSummary.isNotEmpty()) {
                                            notes = suggestion.warningsSummary
                                        }
                                    },
                                    label = { Text(suggestion.brandName) }
                                )
                            }
                        }
                    }
                }

                // Real-time interaction preview banner!
                if (potentialInteraction != null) {
                    item {
                        Surface(
                            color = SeveritySevere.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SeveritySevere),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SeveritySevere)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Warning: ${potentialInteraction.severity} interaction with existing meds!",
                                        fontWeight = FontWeight.Bold,
                                        color = SeveritySevere,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = potentialInteraction.summary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Dosage & Form
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("Dosage") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_med_dosage_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = selectedForm,
                            onValueChange = { selectedForm = it },
                            label = { Text("Form") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_med_form_input"),
                            singleLine = true
                        )
                    }
                }

                // Frequency & Meal Timing
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedFrequency,
                            onValueChange = { selectedFrequency = it },
                            label = { Text("Frequency") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_med_freq_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = mealTiming,
                            onValueChange = { mealTiming = it },
                            label = { Text("Meal Timing") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_med_meal_input"),
                            singleLine = true
                        )
                    }
                }

                // Scheduled Times
                item {
                    Text(
                        text = "Scheduled Times",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("08:00", "13:00", "20:00").forEach { time ->
                            val selected = scheduledTimes.contains(time)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) {
                                        if (scheduledTimes.size > 1) scheduledTimes.remove(time)
                                    } else {
                                        scheduledTimes.add(time)
                                    }
                                },
                                label = { Text(time) }
                            )
                        }
                    }
                }

                // Stock Count
                item {
                    OutlinedTextField(
                        value = stockCount.toString(),
                        onValueChange = { stockCount = it.toIntOrNull() ?: 30 },
                        label = { Text("Pill Count Stock (Total)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_med_stock_input"),
                        singleLine = true
                    )
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Instructions / Doctor Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addMedicine(
                            name = name,
                            genericName = genericName,
                            dosage = dosage,
                            form = selectedForm,
                            frequency = selectedFrequency,
                            times = scheduledTimes.toList(),
                            mealTiming = mealTiming,
                            stockCount = stockCount,
                            lowStockThreshold = 5,
                            notes = notes,
                            folder = selectedFolder
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_medicine_button")
            ) {
                Text("Save to Folder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
