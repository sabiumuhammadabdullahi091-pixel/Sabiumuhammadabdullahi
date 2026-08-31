package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BackupCategory
import com.example.data.BackupFileRecord
import com.example.data.BackupVaultManager
import com.example.data.BackupVaultMetrics
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun FileManagementScreen(
    vaultManager: BackupVaultManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val files by vaultManager.files.collectAsState()
    val metrics by vaultManager.vaultMetrics.collectAsState()
    val lastRestoredMessage by vaultManager.lastRestoredMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BackupCategory.ALL) }
    var activeRestoringId by remember { mutableStateOf<String?>(null) }
    var showNewBackupDialog by remember { mutableStateOf(false) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    // Dialog State for adding a new backup snapshot
    var newBackupName by remember { mutableStateOf("") }
    var newBackupCategory by remember { mutableStateOf(BackupCategory.ARCHIVE) }
    var newBackupSizeFormatted by remember { mutableStateOf("12.5 GB") }

    val filteredFiles = remember(files, searchQuery, selectedCategory) {
        files.filter { file ->
            val matchesCategory = (selectedCategory == BackupCategory.ALL || file.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    file.fileName.contains(searchQuery, ignoreCase = true) ||
                    file.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
    }

    fun triggerRestore(file: BackupFileRecord) {
        scope.launch {
            activeRestoringId = file.id
            delay(1200L) // Simulate ultra-fast retrieval and decompression
            vaultManager.restoreFile(file.id) { restored ->
                actionToast = "✅ Restored ${restored.fileName} (${restored.formattedSize}) to /sdcard/Download/Restored/"
            }
            activeRestoringId = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "5TB BACKUP & FILE VAULT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "High-Speed Storage, Fast Recovery & File Management",
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberGreen.copy(alpha = 0.2f))
                    .border(1.dp, CyberGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "5.0 TB ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. 5TB Vault Storage Quota Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CyberCyan.copy(alpha = 0.22f),
                            CyberSurfaceDark
                        )
                    )
                )
                .border(1.5.dp, CyberCyan.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("storage_quota_vault_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CLOUD VAULT ALLOCATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${metrics.totalFilesCount} Backed-up files • 256-bit Encrypted",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${metrics.usedFormatted} / ${metrics.totalQuotaFormatted}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${metrics.freeFormatted} Free",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { metrics.usagePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberCyan,
                    trackColor = CyberSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showNewBackupDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00272B)),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("backup_new_snapshot_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Backup Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                vaultManager.restoreAllFiles()
                                actionToast = "All backed-up files restored to device storage!"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("restore_all_files_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Restore All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search backed-up files...", color = TextMutedDark, fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CyberSurfaceDark,
                unfocusedContainerColor = CyberSurfaceDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("backup_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BackupCategory.values()) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                        selectedLabelColor = CyberCyan,
                        containerColor = CyberSurfaceDark,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CyberCardBorder,
                        selectedBorderColor = CyberCyan
                    )
                )
            }
        }

        if (actionToast != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = actionToast!!,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Backed-up Files Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BACKED-UP FILES (${filteredFiles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tap 'Restore' to recover",
                fontSize = 10.sp,
                color = CyberAmber
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Backed-Up Files List with 'Restore' Buttons
        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurfaceDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Backed-up Files Found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap 'Backup Now' above to add your first 5TB backup snapshot.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    val isRestoring = activeRestoringId == file.id
                    BackupFileItemCard(
                        file = file,
                        isRestoring = isRestoring,
                        onRestore = { triggerRestore(file) },
                        onDelete = { vaultManager.deleteBackup(file.id) },
                        onCopyChecksum = { copyToClipboard(file.checksum, "Checksum") }
                    )
                }
            }
        }
    }

    // Modal Dialog to Create a New Backup Snapshot
    if (showNewBackupDialog) {
        AlertDialog(
            onDismissRequest = { showNewBackupDialog = false },
            containerColor = CyberSurfaceDark,
            title = {
                Text(
                    text = "Create 5TB Backup Snapshot",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Add a file or system state to your 5TB cloud vault with zero-loss encryption.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Backup File Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newBackupName,
                        onValueChange = { newBackupName = it },
                        placeholder = { Text("e.g. reels_monetization_archive_2026.zip", color = TextMutedDark, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Category", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(BackupCategory.MEDIA, BackupCategory.DATABASE, BackupCategory.ARCHIVE, BackupCategory.APK_APP).forEach { cat ->
                            val isSelected = newBackupCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else CyberSurfaceVariant)
                                    .border(1.dp, if (isSelected) CyberCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { newBackupCategory = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.displayName.take(8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyberCyan else TextSecondaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Size Estimate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newBackupSizeFormatted,
                        onValueChange = { newBackupSizeFormatted = it },
                        placeholder = { Text("e.g. 85.0 GB", color = TextMutedDark, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newBackupName.trim().ifEmpty { "creator_data_backup_${System.currentTimeMillis().toString().takeLast(6)}.zip" }
                        vaultManager.createBackupSnapshot(
                            name = name,
                            category = newBackupCategory,
                            sizeBytes = 25_000_000_000L,
                            formattedSize = newBackupSizeFormatted.trim().ifEmpty { "25.0 GB" },
                            description = "User created encrypted 5TB backup"
                        )
                        showNewBackupDialog = false
                        newBackupName = ""
                        actionToast = "✅ Added $name to 5TB Vault!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00272B))
                ) {
                    Text("Create Backup", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBackupDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
fun BackupFileItemCard(
    file: BackupFileRecord,
    isRestoring: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onCopyChecksum: () -> Unit
) {
    val categoryIcon: ImageVector = when (file.category) {
        BackupCategory.MEDIA -> Icons.Default.VideoLibrary
        BackupCategory.DATABASE -> Icons.Default.Storage
        BackupCategory.ARCHIVE -> Icons.Default.FolderZip
        BackupCategory.APK_APP -> Icons.Default.Android
        BackupCategory.DOCUMENT -> Icons.Default.Description
        BackupCategory.ALL -> Icons.Default.Storage
    }

    val categoryColor: Color = when (file.category) {
        BackupCategory.MEDIA -> CyberPurple
        BackupCategory.DATABASE -> CyberAmber
        BackupCategory.ARCHIVE -> CyberCyan
        BackupCategory.APK_APP -> CyberGreen
        BackupCategory.DOCUMENT -> Color(0xFF4285F4)
        BackupCategory.ALL -> CyberCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_file_card_${file.fileName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (file.isRestored) CyberGreen.copy(alpha = 0.5f) else CyberCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.18f))
                            .border(1.dp, categoryColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = file.fileName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${file.category.displayName} • ${file.formattedDate}",
                            fontSize = 10.sp,
                            color = TextMutedDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // File Size Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = file.formattedSize,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // File description and status
            Text(
                text = file.description,
                fontSize = 11.sp,
                color = TextSecondaryDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location, Checksum & Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (file.isRestored) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (file.isRestored) CyberGreen else CyberAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (file.isRestored) "RESTORED TO DEVICE" else "5TB VAULT (ENCRYPTED)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (file.isRestored) CyberGreen else CyberAmber
                    )
                }

                Text(
                    text = file.checksum,
                    fontSize = 9.sp,
                    color = TextMutedDark,
                    modifier = Modifier
                        .clickable { onCopyChecksum() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RESTORE Action Button & Management Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Backup",
                        tint = TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onRestore,
                    enabled = !isRestoring,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (file.isRestored) CyberGreen else CyberCyan,
                        contentColor = if (file.isRestored) Color(0xFF003919) else Color(0xFF00272B)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("restore_button_${file.fileName}")
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Restoring...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (file.isRestored) Icons.Default.CheckCircle else Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (file.isRestored) "RESTORE AGAIN" else "RESTORE FILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
