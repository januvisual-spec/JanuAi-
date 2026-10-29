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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun DeviceControlScreen(
    uiState: JanuUiState,
    onToggleFlashlight: () -> Unit,
    onSetVolume: (Int) -> Unit,
    onIncreaseVolume: () -> Unit,
    onDecreaseVolume: () -> Unit,
    onOpenWifiPanel: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onSetBrightness: (Int) -> Unit,
    onIncreaseBrightness: () -> Unit,
    onDecreaseBrightness: () -> Unit,
    onRequestWriteSettings: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onRefreshTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JanuBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Text(
            text = "HARDWARE & DEVICE ACTIONS",
            color = JanuNeonGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // 1. SCREEN BRIGHTNESS ACTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
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
                                .background(Color(0xFF26230B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = "Screen Brightness",
                                tint = JanuWarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Screen Brightness",
                                color = JanuTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Live display intensity",
                                color = JanuTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "${uiState.brightnessPercent}%",
                        color = JanuWarningAmber,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseBrightness,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-", color = JanuTextSecondary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = uiState.brightnessPercent.toFloat(),
                        onValueChange = { onSetBrightness(it.toInt()) },
                        valueRange = 5f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JanuWarningAmber,
                            activeTrackColor = JanuWarningAmber,
                            inactiveTrackColor = JanuCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("brightness_slider")
                    )

                    IconButton(
                        onClick = onIncreaseBrightness,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+", color = JanuWarningAmber, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Preset Buttons (25%, 50%, 75%, 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 50, 75, 100).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.brightnessPercent == preset) JanuWarningAmber else Color(0xFF161F1B))
                                .clickable { onSetBrightness(preset) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset%",
                                color = if (uiState.brightnessPercent == preset) Color.Black else JanuTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. WI-FI & BLUETOOTH ACTIONS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Wi-Fi Action Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = JanuCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isWifiEnabled) Color(0xFF042F2E) else JanuCardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi",
                                tint = if (uiState.isWifiEnabled) JanuNeonCyan else JanuTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (uiState.isWifiEnabled) Color(0xFF03332A) else Color(0xFF241515))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (uiState.isWifiEnabled) "ACTIVE" else "OFF",
                                color = if (uiState.isWifiEnabled) JanuNeonGreen else Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Wi-Fi Panel",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Toggle Wi-Fi",
                        color = JanuTextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenWifiPanel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF042F2E),
                            contentColor = JanuNeonCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_wifi_panel_button")
                    ) {
                        Text("Toggle Wi-Fi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bluetooth Action Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = JanuCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isBluetoothEnabled) Color(0xFF0C2433) else JanuCardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth",
                                tint = if (uiState.isBluetoothEnabled) Color(0xFF38BDF8) else JanuTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (uiState.isBluetoothEnabled) Color(0xFF063337) else Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (uiState.isBluetoothEnabled) "ON" else "OFF",
                                color = if (uiState.isBluetoothEnabled) Color(0xFF38BDF8) else JanuTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bluetooth",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Pair & Connect",
                        color = JanuTextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenBluetoothSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A2230),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_bluetooth_settings_button")
                    ) {
                        Text("Configure BT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. FLASHLIGHT ACTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (uiState.isFlashlightOn) JanuNeonGreen else JanuCardBorder,
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isFlashlightOn) Color(0xFF09291D) else JanuCardBg
            ),
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isFlashlightOn) JanuNeonGreen else JanuCardBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight",
                            tint = if (uiState.isFlashlightOn) Color.Black else JanuTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Flashlight / Torch",
                            color = JanuTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (uiState.isFlashlightOn) "State: ACTIVATED" else "State: OFF",
                            color = if (uiState.isFlashlightOn) JanuNeonGreen else JanuTextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = uiState.isFlashlightOn,
                    onCheckedChange = { onToggleFlashlight() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = JanuNeonGreen,
                        uncheckedThumbColor = JanuTextSecondary,
                        uncheckedTrackColor = JanuCardBorder
                    ),
                    modifier = Modifier.testTag("flashlight_switch")
                )
            }
        }

        // 4. SYSTEM MEDIA VOLUME SLIDER CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Media Volume",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${uiState.volumePercent}%",
                        color = JanuNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseVolume,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Decrease Volume",
                            tint = JanuTextSecondary
                        )
                    }

                    Slider(
                        value = uiState.volumePercent.toFloat(),
                        onValueChange = { onSetVolume(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JanuNeonCyan,
                            activeTrackColor = JanuNeonCyan,
                            inactiveTrackColor = JanuCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("system_volume_slider")
                    )

                    IconButton(
                        onClick = onIncreaseVolume,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Increase Volume",
                            tint = JanuNeonCyan
                        )
                    }
                }
            }
        }

        // 5. DEVICE TELEMETRY CARD (Oppo F25 5G Pro)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JanuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = JanuCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device Specs & Status",
                        color = JanuTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Oppo F25 5G Pro",
                        color = JanuNeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TelemetryRow(label = "Processor (SoC)", value = "MediaTek Dimensity 7050 5G")
                TelemetryRow(label = "RAM / Storage", value = "8 GB RAM / 256 GB ROM")
                TelemetryRow(label = "Operating System", value = "Android 16 (API 36)")
                TelemetryRow(
                    label = "Battery Health & Level",
                    value = "${uiState.batteryLevel}% " + (if (uiState.isCharging) "(Charging)" else "(Discharging)")
                )
                TelemetryRow(label = "Network Telemetry", value = uiState.networkStatus)
            }
        }

        // 6. APPLICATION LAUNCHPAD
        Text(
            text = "APPLICATION LAUNCHPAD",
            color = JanuNeonGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        val apps = listOf(
            AppLauncherItem("YouTube", Icons.Default.PlayArrow, Color(0xFFEF4444)) { onLaunchApp("youtube") },
            AppLauncherItem("WhatsApp", Icons.Default.Search, Color(0xFF22C55E)) { onLaunchApp("whatsapp") },
            AppLauncherItem("Camera", Icons.Default.CameraAlt, JanuNeonCyan) { onLaunchApp("camera") },
            AppLauncherItem("Music Player", Icons.Default.MusicNote, Color(0xFFA855F7)) { onLaunchApp("music") },
            AppLauncherItem("Alarm & Clock", Icons.Default.Alarm, JanuWarningAmber) { onLaunchApp("alarm") },
            AppLauncherItem("Settings", Icons.Default.Settings, JanuTextSecondary) { onLaunchApp("settings") }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            apps.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, JanuCardBorder, RoundedCornerShape(12.dp))
                                .clickable { item.onClick() }
                                .testTag("launch_app_${item.title.take(4)}"),
                            colors = CardDefaults.cardColors(containerColor = JanuCardBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.tint,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    color = JanuTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AppLauncherItem(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = JanuTextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = JanuTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
