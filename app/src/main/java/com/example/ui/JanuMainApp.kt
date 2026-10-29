package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StatusTopBar
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.CodeStudioScreen
import com.example.ui.screens.DeviceControlScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JanuBlack
import com.example.ui.theme.JanuCardBg
import com.example.ui.theme.JanuCardBorder
import com.example.ui.theme.JanuDarkSurface
import com.example.ui.theme.JanuNeonCyan
import com.example.ui.theme.JanuNeonGreen
import com.example.ui.theme.JanuTextPrimary
import com.example.ui.theme.JanuTextSecondary
import com.example.ui.viewmodel.JanuViewModel

enum class JanuScreen(val title: String, val icon: ImageVector, val testTag: String) {
    ASSISTANT("Assistant", Icons.Default.GraphicEq, "nav_assistant"),
    CODE("Code", Icons.Default.Code, "nav_code"),
    AUTOMATIONS("Rules", Icons.Default.Bolt, "nav_automations"),
    DEVICE("Controls", Icons.Default.Devices, "nav_controls"),
    MEMORY("Memory", Icons.Default.Memory, "nav_memory"),
    CHAT("Logs", Icons.Default.Chat, "nav_chat"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun JanuMainApp(
    viewModel: JanuViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(JanuScreen.ASSISTANT) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val automationRules by viewModel.allRules.collectAsStateWithLifecycle()

    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val audioWaveformLevel by viewModel.audioWaveformLevel.collectAsStateWithLifecycle()
    val partialSpeech by viewModel.partialSpeech.collectAsStateWithLifecycle()

    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            showPermissionRationale = true
        }
    }

    fun handleMicClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun exportMemoryVault() {
        viewModel.exportMemorySnapshot { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("JANU AI Memory Vault", text))
            Toast.makeText(context, "Memory Vault copied to clipboard!", Toast.LENGTH_SHORT).show()

            // Also open Android Share Sheet for easy export
            try {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export JANU AI Memory Vault")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                // Clipboard fallback succeeded
            }
        }
    }

    // Permission Rationale Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            containerColor = JanuCardBg,
            title = {
                Text("Microphone Permission Required", color = JanuNeonGreen)
            },
            text = {
                Text(
                    "JANU AI requires microphone access to hear and process your spoken voice commands. Please grant the permission to enable voice conversation.",
                    color = JanuTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JanuNeonGreen, contentColor = Color.Black)
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Dismiss", color = JanuTextSecondary)
                }
            }
        )
    }

    // Predictive Back Handling
    if (currentScreen != JanuScreen.ASSISTANT) {
        BackHandler {
            currentScreen = JanuScreen.ASSISTANT
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JanuBlack),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            StatusTopBar(
                isOnline = viewModel.androidControl.isNetworkAvailable(),
                forceOffline = uiState.forceOfflineMode,
                batteryLevel = uiState.batteryLevel,
                isCharging = uiState.isCharging,
                networkStatus = uiState.networkStatus,
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = { lang -> viewModel.setLanguage(lang) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .border(1.dp, JanuCardBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = JanuDarkSurface,
                tonalElevation = 8.dp
            ) {
                JanuScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 9.sp,
                                maxLines = 1,
                                color = if (isSelected) JanuNeonGreen else JanuTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = JanuNeonGreen,
                            indicatorColor = JanuNeonGreen,
                            unselectedIconColor = JanuTextSecondary,
                            unselectedTextColor = JanuTextSecondary
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JanuBlack)
        ) {
            when (currentScreen) {
                JanuScreen.ASSISTANT -> AssistantScreen(
                    uiState = uiState,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    audioWaveformLevel = audioWaveformLevel,
                    partialSpeech = partialSpeech,
                    onMicToggle = { handleMicClick() },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onSubmitQuery = { query -> viewModel.submitQuery(query) },
                    onConfirmAction = { viewModel.confirmPendingAction() },
                    onDismissAction = { viewModel.dismissPendingAction() },
                    onQuickCommand = { cmd -> viewModel.submitQuery(cmd) }
                )

                JanuScreen.CODE -> CodeStudioScreen(
                    onSaveToBackups = { version, desc, file, data ->
                        viewModel.saveProjectBackup(version, desc, file, data)
                    }
                )

                JanuScreen.AUTOMATIONS -> AutomationsScreen(
                    rules = automationRules,
                    onToggleRule = { id, enabled -> viewModel.toggleAutomationRule(id, enabled) },
                    onDeleteRule = { id -> viewModel.deleteAutomationRule(id) },
                    onAddRule = { rule -> viewModel.addAutomationRule(rule) },
                    onTestRule = { rule -> viewModel.testAutomationRule(rule) }
                )

                JanuScreen.DEVICE -> DeviceControlScreen(
                    uiState = uiState,
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    onSetVolume = { vol -> viewModel.setVolume(vol) },
                    onIncreaseVolume = { viewModel.increaseVolume() },
                    onDecreaseVolume = { viewModel.decreaseVolume() },
                    onOpenWifiPanel = { viewModel.openWifiPanel() },
                    onOpenBluetoothSettings = { viewModel.openBluetoothSettings() },
                    onSetBrightness = { br -> viewModel.setBrightness(br) },
                    onIncreaseBrightness = { viewModel.increaseBrightness() },
                    onDecreaseBrightness = { viewModel.decreaseBrightness() },
                    onRequestWriteSettings = { viewModel.requestWriteSettingsPermission() },
                    onLaunchApp = { app -> viewModel.launchAppByName(app) },
                    onRefreshTelemetry = { viewModel.refreshDeviceTelemetry() }
                )

                JanuScreen.MEMORY -> MemoryScreen(
                    memories = memories,
                    onAddMemory = { k, v, c -> viewModel.addCustomMemory(k, v, c) },
                    onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                    onClearAllMemories = { viewModel.confirmPendingAction() },
                    onExportMemories = { exportMemoryVault() }
                )

                JanuScreen.CHAT -> ChatHistoryScreen(
                    messages = chatMessages,
                    onSpeakMessage = { text -> viewModel.speakText(text) },
                    onClearChat = { viewModel.clearChatHistory() }
                )

                JanuScreen.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                    onToggleOfflineMode = { offline -> viewModel.toggleOfflineMode(offline) },
                    onToggleWakeWord = { active -> viewModel.toggleWakeWord(active) },
                    onUpdateVoiceSettings = { pitch, rate -> viewModel.updateVoiceSettings(pitch, rate) },
                    onTestVoice = { sample -> viewModel.speakText(sample) }
                )
            }
        }
    }
}
