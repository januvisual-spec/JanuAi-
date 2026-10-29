package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JanuOrb
import com.example.ui.components.VoiceWaveform
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
fun AssistantScreen(
    uiState: JanuUiState,
    isListening: Boolean,
    isSpeaking: Boolean,
    audioWaveformLevel: Float,
    partialSpeech: String,
    onMicToggle: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSubmitQuery: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    onQuickCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Confirmation Safeguard Dialog
    if (uiState.pendingConfirmation != null) {
        AlertDialog(
            onDismissRequest = onDismissAction,
            containerColor = JanuCardBg,
            title = {
                Text(
                    text = "Security Confirmation",
                    color = JanuWarningAmber,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = uiState.pendingConfirmation.first,
                    color = JanuTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmAction,
                    colors = ButtonDefaults.buttonColors(containerColor = JanuNeonGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_action_button")
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissAction,
                    modifier = Modifier.testTag("cancel_action_button")
                ) {
                    Text("Cancel", color = JanuTextSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JanuBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Badge
        val statusText = when {
            isListening -> "LISTENING TO VOICE..."
            uiState.status == "PROCESSING" -> "PROCESSING QUERY..."
            isSpeaking -> "JANU SPEAKING..."
            uiState.wakeWordActive -> "HEY JANU LISTENING..."
            else -> "JANU READY"
        }

        val statusColor = when {
            isListening -> JanuNeonCyan
            uiState.status == "PROCESSING" -> JanuWarningAmber
            isSpeaking -> JanuNeonGreen
            else -> JanuTextSecondary
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF091410))
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Animated Orb (Jarvis AI Core)
        JanuOrb(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioLevel = audioWaveformLevel,
            size = 200.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Voice Equalizer Waveform
        VoiceWaveform(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioLevel = audioWaveformLevel,
            height = 36.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Transcription / Response Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isListening && partialSpeech.isNotBlank()) {
                    Text(
                        text = "You: $partialSpeech",
                        color = JanuNeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                } else if (uiState.activeQuery.isNotBlank()) {
                    Text(
                        text = "You: \"${uiState.activeQuery}\"",
                        color = JanuNeonCyan.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = uiState.lastResponse,
                        color = JanuTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSpeaking) {
                        IconButton(
                            onClick = onStopSpeaking,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B1212))
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speaking",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Glow Microphone Action Button
        val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mic_pulse"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(JanuNeonCyan.copy(alpha = 0.25f))
                )
            }

            IconButton(
                onClick = onMicToggle,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isListening) listOf(JanuNeonCyan, Color(0xFF075985))
                            else listOf(JanuNeonGreen, Color(0xFF065F46))
                        )
                    )
                    .border(
                        2.dp,
                        if (isListening) JanuNeonCyan else JanuNeonGreen,
                        CircleShape
                    )
                    .testTag("voice_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Text(
            text = if (isListening) "Tap to pause listening" else "Tap to talk with JANU",
            color = JanuTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Command Action Chips
        val chipCommands = listOf(
            "💡 Turn on flashlight" to "Turn on flashlight",
            "💡 Turn off flashlight" to "Turn off flashlight",
            "📶 Wi-Fi Panel" to "Open Wi-Fi",
            "🔵 Bluetooth Settings" to "Open Bluetooth",
            "☀️ Brightness Up" to "Increase brightness",
            "🌙 Dim Screen" to "Decrease brightness",
            "🔋 Battery status" to "What is my battery level?",
            "🕒 Current time" to "What time is it?",
            "📅 Today's date" to "What is today's date?",
            "▶️ Open YouTube" to "Open YouTube",
            "💬 Open WhatsApp" to "Open WhatsApp",
            "📷 Open Camera" to "Open Camera",
            "🎵 Play music" to "Play music",
            "🔊 Volume Up" to "Increase volume",
            "⏰ Set Alarm" to "Set alarm",
            "ଓଡ଼ିଆ: ନମସ୍କାର" to "ନମସ୍କାର ଜାନୁ",
            "हिन्दी: नमस्ते" to "नमस्ते जानू"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chipCommands.forEach { (label, command) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JanuCardBg)
                        .border(1.dp, JanuCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("quick_chip_${label.take(8)}")
                ) {
                    TextButton(
                        onClick = { onSubmitQuery(command) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = label,
                            color = JanuNeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Text Query Bar for Hybrid Voice/Text Conversation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(JanuCardBg)
                .border(1.dp, JanuCardBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Type command or message...",
                        color = JanuTextMuted,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("text_query_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = JanuTextPrimary,
                    unfocusedTextColor = JanuTextPrimary,
                    cursorColor = JanuNeonGreen
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSubmitQuery(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(JanuDarkGreen)
                    .testTag("send_query_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = JanuNeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
