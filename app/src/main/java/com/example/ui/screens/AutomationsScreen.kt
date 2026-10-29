package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.AutomationRuleEntity
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

@Composable
fun AutomationsScreen(
    rules: List<AutomationRuleEntity>,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onAddRule: (AutomationRuleEntity) -> Unit,
    onTestRule: (AutomationRuleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    var ruleTitle by remember { mutableStateOf("") }
    var selectedTriggerType by remember { mutableStateOf("VOICE_PHRASE") }
    var triggerValue by remember { mutableStateOf("") }
    var selectedActionType by remember { mutableStateOf("SPEAK_TEXT") }
    var actionTarget by remember { mutableStateOf("") }

    // Dialog for Creating Rule
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = JanuCardBg,
            title = {
                Text(
                    text = "New Automation Rule",
                    color = JanuNeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Configure trigger-action automation for JANU AI.",
                        color = JanuTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = ruleTitle,
                        onValueChange = { ruleTitle = it },
                        label = { Text("Rule Title (e.g. Night Routine)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JanuNeonGreen,
                            unfocusedBorderColor = JanuCardBorder,
                            focusedTextColor = JanuTextPrimary,
                            unfocusedTextColor = JanuTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("automation_title_input")
                    )

                    // Trigger Type selector
                    Text("Trigger Type", color = JanuNeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("VOICE_PHRASE" to "Voice Phrase", "BATTERY_LOW" to "Battery Low").forEach { (type, label) ->
                            FilterChip(
                                selected = selectedTriggerType == type,
                                onClick = { selectedTriggerType = type },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JanuDarkGreen,
                                    selectedLabelColor = JanuNeonGreen,
                                    containerColor = Color.Transparent,
                                    labelColor = JanuTextSecondary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = triggerValue,
                        onValueChange = { triggerValue = it },
                        label = {
                            Text(
                                if (selectedTriggerType == "VOICE_PHRASE") "Trigger Phrase (e.g. good night)"
                                else "Battery Threshold (e.g. 15)"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JanuNeonGreen,
                            unfocusedBorderColor = JanuCardBorder,
                            focusedTextColor = JanuTextPrimary,
                            unfocusedTextColor = JanuTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("automation_trigger_input")
                    )

                    // Action Type selector
                    Text("Action Type", color = JanuNeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "SPEAK_TEXT" to "Speak Text",
                            "LAUNCH_APP" to "Launch App",
                            "TOGGLE_FLASHLIGHT" to "Torch",
                            "OPEN_PANEL" to "Settings Panel"
                        ).forEach { (action, label) ->
                            FilterChip(
                                selected = selectedActionType == action,
                                onClick = { selectedActionType = action },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JanuDarkGreen,
                                    selectedLabelColor = JanuNeonGreen,
                                    containerColor = Color.Transparent,
                                    labelColor = JanuTextSecondary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = actionTarget,
                        onValueChange = { actionTarget = it },
                        label = {
                            Text(
                                when (selectedActionType) {
                                    "SPEAK_TEXT" -> "Voice Response Text"
                                    "LAUNCH_APP" -> "Package (e.g. music or com.whatsapp)"
                                    "OPEN_PANEL" -> "Panel (e.g. wifi or bluetooth)"
                                    else -> "Target / Payload"
                                }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JanuNeonGreen,
                            unfocusedBorderColor = JanuCardBorder,
                            focusedTextColor = JanuTextPrimary,
                            unfocusedTextColor = JanuTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("automation_target_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ruleTitle.isNotBlank() && triggerValue.isNotBlank()) {
                            onAddRule(
                                AutomationRuleEntity(
                                    title = ruleTitle.trim(),
                                    triggerType = selectedTriggerType,
                                    triggerValue = triggerValue.trim(),
                                    actionType = selectedActionType,
                                    actionTarget = actionTarget.trim()
                                )
                            )
                            ruleTitle = ""
                            triggerValue = ""
                            actionTarget = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JanuNeonGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_rule_button")
                ) {
                    Text("Save Rule", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
        // Top Automation Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuNeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF071B14)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(JanuDarkGreen)
                            .border(1.dp, JanuNeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Automation Engine",
                            tint = JanuNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "ANDROID AUTOMATION ENGINE",
                            color = JanuNeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${rules.count { it.isEnabled }} of ${rules.size} triggers active",
                            color = JanuTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(JanuDarkGreen)
                        .border(1.dp, JanuNeonGreen, CircleShape)
                        .testTag("add_rule_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Rule",
                        tint = JanuNeonGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (rules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No automation rules configured.\nTap + to add voice triggers or battery alerts.",
                    color = JanuTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (rule.isEnabled) JanuCardBorder else JanuCardBorder.copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = JanuCardBg),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (rule.isEnabled) JanuDarkGreen else Color(0xFF1E293B))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = rule.triggerType,
                                                color = if (rule.isEnabled) JanuNeonGreen else JanuTextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = rule.title,
                                            color = if (rule.isEnabled) JanuTextPrimary else JanuTextMuted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = rule.isEnabled,
                                    onCheckedChange = { onToggleRule(rule.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JanuNeonGreen,
                                        checkedTrackColor = JanuDarkGreen,
                                        uncheckedThumbColor = JanuTextMuted,
                                        uncheckedTrackColor = JanuCardBg
                                    ),
                                    modifier = Modifier.testTag("toggle_rule_${rule.id}")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Trigger and Action Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TRIGGER", color = JanuTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(
                                        text = rule.triggerValue,
                                        color = JanuNeonCyan,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ACTION (${rule.actionType})", color = JanuTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(
                                        text = rule.actionTarget.ifBlank { "Execute" },
                                        color = JanuWarningAmber,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Test Trigger Button
                                TextButton(
                                    onClick = { onTestRule(rule) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = JanuNeonGreen)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Test Trigger",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Trigger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Delete Rule
                                IconButton(
                                    onClick = { onDeleteRule(rule.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("delete_rule_${rule.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Rule",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
