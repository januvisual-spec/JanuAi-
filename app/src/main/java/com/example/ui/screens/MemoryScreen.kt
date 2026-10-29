package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryEntity
import com.example.ui.theme.JanuBlack
import com.example.ui.theme.JanuCardBg
import com.example.ui.theme.JanuCardBorder
import com.example.ui.theme.JanuDarkGreen
import com.example.ui.theme.JanuNeonCyan
import com.example.ui.theme.JanuNeonGreen
import com.example.ui.theme.JanuTextMuted
import com.example.ui.theme.JanuTextPrimary
import com.example.ui.theme.JanuTextSecondary
import com.example.ui.theme.JanuWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryScreen(
    memories: List<MemoryEntity>,
    onAddMemory: (String, String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit,
    onExportMemories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("PREFERENCE") }

    val filteredMemories = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) memories
        else memories.filter {
            it.key.contains(searchQuery, ignoreCase = true) ||
                    it.value.contains(searchQuery, ignoreCase = true)
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = JanuCardBg,
            title = {
                Text(
                    text = "Add Personal Memory",
                    color = JanuNeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Store a personal fact, nickname, or preference for JANU AI to remember.",
                        color = JanuTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Topic / Key (e.g. favorite_food)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JanuNeonGreen,
                            unfocusedBorderColor = JanuCardBorder,
                            focusedTextColor = JanuTextPrimary,
                            unfocusedTextColor = JanuTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.testTag("memory_key_input")
                    )

                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Details / Value (e.g. Odia Dalma)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JanuNeonGreen,
                            unfocusedBorderColor = JanuCardBorder,
                            focusedTextColor = JanuTextPrimary,
                            unfocusedTextColor = JanuTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.testTag("memory_value_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKey.isNotBlank() && newValue.isNotBlank()) {
                            onAddMemory(newKey.trim(), newValue.trim(), newCategory)
                            newKey = ""
                            newValue = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JanuNeonGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_memory_button")
                ) {
                    Text("Save Memory", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = JanuTextSecondary)
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = JanuCardBg,
            title = {
                Text("Erase All Memories", color = JanuWarningAmber, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete all personal memories? JANU AI will forget your preferences.", color = JanuTextPrimary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllMemories()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Erase All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = JanuTextSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JanuBlack)
            .padding(16.dp)
    ) {
        // User Identity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091F18)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(JanuDarkGreen)
                            .border(1.dp, JanuNeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Memory Vault",
                            tint = JanuNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "PERSONAL MEMORY VAULT",
                            color = JanuNeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Encrypted local SQLite storage via Room",
                            color = JanuTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { onExportMemories() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JanuDarkGreen)
                        .border(1.dp, JanuNeonGreen, CircleShape)
                        .testTag("export_memory_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Memories",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search & Add Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search memories...", color = JanuTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("memory_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JanuNeonGreen,
                    unfocusedBorderColor = JanuCardBorder,
                    focusedTextColor = JanuTextPrimary,
                    unfocusedTextColor = JanuTextPrimary,
                    focusedContainerColor = JanuCardBg,
                    unfocusedContainerColor = JanuCardBg
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JanuDarkGreen)
                    .border(1.dp, JanuNeonGreen, RoundedCornerShape(12.dp))
                    .testTag("add_memory_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Memory",
                    tint = JanuNeonGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No memories match \"$searchQuery\"." else "Memory vault is empty.\nTap + to teach JANU something about you!",
                    color = JanuTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        } else {
            val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMemories, key = { it.id }) { mem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, JanuCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = JanuCardBg),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(JanuDarkGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = mem.category,
                                            color = JanuNeonGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = mem.key,
                                        color = JanuNeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = mem.value,
                                    color = JanuTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Updated: ${dateFormatter.format(Date(mem.timestamp))}",
                                    color = JanuTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = { onDeleteMemory(mem.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_memory_${mem.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Memory",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
