package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.viewmodel.JanuUiState

@Composable
fun SettingsScreen(
    uiState: JanuUiState,
    onLanguageSelected: (String) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onUpdateVoiceSettings: (Float, Float) -> Unit,
    onTestVoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var pitch by remember { mutableFloatStateOf(uiState.speechPitch) }
    var rate by remember { mutableFloatStateOf(uiState.speechRate) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JanuBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "JANU AI CONFIGURATION",
            color = JanuNeonGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // 1. LANGUAGE SELECTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice & Text Language",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "JANU AI seamlessly converses in English, Odia, and Hindi across all features.",
                    color = JanuTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                LanguageOptionRow(
                    code = "EN",
                    name = "English (Global)",
                    sample = "Hello! How can I help you today?",
                    isSelected = uiState.selectedLanguage == "EN",
                    onSelect = { onLanguageSelected("EN") }
                )

                LanguageOptionRow(
                    code = "OD",
                    name = "Odia / ଓଡ଼ିଆ (Regional)",
                    sample = "ନମସ୍କାର କାର୍ତ୍ତିକ! ମୁଁ ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?",
                    isSelected = uiState.selectedLanguage == "OD",
                    onSelect = { onLanguageSelected("OD") }
                )

                LanguageOptionRow(
                    code = "HI",
                    name = "Hindi / हिन्दी (National)",
                    sample = "नमस्ते कार्तिक! मैं आपकी कैसे मदद कर सकती हूँ?",
                    isSelected = uiState.selectedLanguage == "HI",
                    onSelect = { onLanguageSelected("HI") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val sampleText = when (uiState.selectedLanguage) {
                            "OD" -> "ନମସ୍କାର! ମୁଁ ଜାନୁ ଆଇ, ଆପଣଙ୍କ ଭଏସ୍ ସହାୟକ।"
                            "HI" -> "नमस्ते! मैं जानू एआई हूँ, आपकी वॉइस असिस्टेंट।"
                            else -> "Hello! I am JANU AI, your personal intelligent assistant."
                        }
                        onTestVoice(sampleText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JanuDarkGreen, contentColor = JanuNeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_language_voice_button")
                ) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Speech in ${uiState.selectedLanguage}", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. WAKE WORD DETECTION ("HEY JANU") CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (uiState.wakeWordActive) JanuNeonCyan else JanuCardBorder,
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.wakeWordActive) JanuNeonCyan else JanuCardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Wake Word",
                                tint = if (uiState.wakeWordActive) Color.Black else JanuTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Wake Word (\"Hey Janu\")",
                                color = JanuTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (uiState.wakeWordActive) "Active (Listening for 'Hey Janu')" else "Disabled (Manual button only)",
                                color = if (uiState.wakeWordActive) JanuNeonCyan else JanuTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Switch(
                        checked = uiState.wakeWordActive,
                        onCheckedChange = { onToggleWakeWord(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = JanuNeonCyan,
                            uncheckedThumbColor = JanuTextSecondary,
                            uncheckedTrackColor = JanuCardBorder
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Explanations for Battery and Microphone permissions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF071116))
                        .border(1.dp, Color(0xFF132A36), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = "Battery info",
                            tint = JanuWarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Battery Usage: Continuous wake word acoustic listening requires background audio DSP sampling (~3-5% battery per hour). On your Oppo F25 5G Pro (Dimensity 7050), APU hardware handles it efficiently, but keeping it disabled when not needed maximizes battery life.",
                            color = JanuTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions info",
                            tint = JanuNeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Microphone Permission: Android mandates runtime RECORD_AUDIO permission. When enabled, JANU monitors audio frames for the 'Hey Janu' phrase. No raw voice is transmitted or saved to external servers.",
                            color = JanuTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 3. AI ENGINE MODE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "AI Mode",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Processing Mode",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleOfflineMode(false) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !uiState.forceOfflineMode,
                        onClick = { onToggleOfflineMode(false) },
                        colors = RadioButtonDefaults.colors(selectedColor = JanuNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Auto Mode (Online Gemini 3.8 + Offline Fallback)",
                            color = JanuTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Uses Gemini 3.8 Flash for rich reasoning; automatically falls back to local core if disconnected.",
                            color = JanuTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleOfflineMode(true) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.forceOfflineMode,
                        onClick = { onToggleOfflineMode(true) },
                        colors = RadioButtonDefaults.colors(selectedColor = JanuNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Strict Offline Mode (Local Core Only)",
                            color = JanuTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Zero internet requests. Executes device commands, flashlight, apps, volume, battery, alarms, and local memory locally.",
                            color = JanuTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. VOICE SPEECH RATE & PITCH
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Settings",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Pitch & Speed Tuning",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Speech Rate: ${String.format("%.1fx", rate)}",
                    color = JanuTextSecondary,
                    fontSize = 12.sp
                )
                Slider(
                    value = rate,
                    onValueChange = {
                        rate = it
                        onUpdateVoiceSettings(pitch, rate)
                    },
                    valueRange = 0.6f..1.8f,
                    colors = SliderDefaults.colors(thumbColor = JanuNeonGreen, activeTrackColor = JanuNeonGreen)
                )

                Text(
                    text = "Voice Pitch: ${String.format("%.1fx", pitch)}",
                    color = JanuTextSecondary,
                    fontSize = 12.sp
                )
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        onUpdateVoiceSettings(pitch, rate)
                    },
                    valueRange = 0.6f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = JanuNeonCyan, activeTrackColor = JanuNeonCyan)
                )
            }
        }

        // 5. ARCHITECTURE & UPGRADE ROADMAP (V1, V2, V3)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Roadmap",
                        tint = JanuNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JANU AI Evolution Roadmap",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                RoadmapItem(
                    version = "V1 (Active)",
                    title = "Hybrid Architecture",
                    desc = "Instant local semantic intent engine + Firebase Gemini 3.8 Flash, Room database memory vault, hardware controls, and multilingual Odia/Hindi/English speech."
                )

                RoadmapItem(
                    version = "V2 (Upcoming)",
                    title = "MediaPipe LLM On-Device",
                    desc = "Integrated Google MediaPipe LLM Inference running quantized Gemma-2B locally on device storage without internet."
                )

                RoadmapItem(
                    version = "V3 (Future)",
                    title = "Dimensity 7050 APU GGUF Engine",
                    desc = "llama.cpp NPU acceleration directly compiling GGUF quantized models for real-time offline Jarvis neural processing."
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    code: String,
    name: String,
    sample: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = JanuNeonGreen)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = name,
                color = if (isSelected) JanuNeonGreen else JanuTextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "\"$sample\"",
                color = JanuTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RoadmapItem(version: String, title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(JanuDarkGreen)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = version,
                    color = JanuNeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = JanuNeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(
            text = desc,
            color = JanuTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
