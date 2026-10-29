package com.example.service

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ControlActionResult(
    val isSuccess: Boolean,
    val userMessage: String,
    val actionCode: String
)

class AndroidControlManager(val context: Context) {

    private val TAG = "AndroidControlManager"

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val actionsManager = DeviceActionsManager(context)

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _batteryLevel = MutableStateFlow(getInitialBatteryLevel())
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(getInitialIsCharging())
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        findCameraWithFlash()
        registerTorchCallback()
    }

    private fun findCameraWithFlash() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdWithFlash = id
                    return
                }
            }
            if (cameraIdWithFlash == null && cameraManager?.cameraIdList?.isNotEmpty() == true) {
                cameraIdWithFlash = cameraManager.cameraIdList[0]
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering camera with flash", e)
        }
    }

    private fun registerTorchCallback() {
        try {
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == cameraIdWithFlash || cameraIdWithFlash == null) {
                        _isFlashlightOn.value = enabled
                    }
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering torch callback", e)
        }
    }

    // 1. Flashlight Control
    fun setFlashlight(enabled: Boolean): ControlActionResult {
        val id = cameraIdWithFlash ?: cameraManager?.cameraIdList?.firstOrNull()
        if (id == null) {
            return ControlActionResult(false, "Flashlight hardware is unavailable on this device.", "FLASHLIGHT_ERROR")
        }
        return try {
            cameraManager?.setTorchMode(id, enabled)
            _isFlashlightOn.value = enabled
            ControlActionResult(
                true,
                if (enabled) "Flashlight activated successfully." else "Flashlight turned off.",
                if (enabled) "FLASHLIGHT_ON" else "FLASHLIGHT_OFF"
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "CameraAccessException toggling torch", e)
            ControlActionResult(false, "Cannot access camera flash: ${e.message}", "FLASHLIGHT_ACCESS_DENIED")
        } catch (e: Exception) {
            Log.e(TAG, "Exception toggling torch", e)
            ControlActionResult(false, "Flashlight toggle failed: ${e.localizedMessage}", "FLASHLIGHT_FAILED")
        }
    }

    fun toggleFlashlight(): ControlActionResult = setFlashlight(!_isFlashlightOn.value)

    // 2. Volume & Audio Control
    fun increaseVolume(): Int {
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
        return getCurrentVolumePercent()
    }

    fun decreaseVolume(): Int {
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        return getCurrentVolumePercent()
    }

    fun setVolumePercent(percent: Int): Int {
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val clamped = percent.coerceIn(0, 100)
        val target = ((clamped / 100f) * maxVolume).toInt()
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        return getCurrentVolumePercent()
    }

    fun getCurrentVolumePercent(): Int {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        return if (max > 0) (current * 100 / max) else 50
    }

    // 3. Battery Telemetry
    private fun getInitialBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    }

    private fun getInitialIsCharging(): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun refreshBatteryInfo(): Pair<Int, Boolean> {
        val level = getInitialBatteryLevel()
        val charging = getInitialIsCharging()
        _batteryLevel.value = level
        _isCharging.value = charging
        return Pair(level, charging)
    }

    // 4. Connectivity Diagnostics
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun getNetworkStatusText(): String {
        val network = connectivityManager?.activeNetwork ?: return "Offline (No Network)"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Offline"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Online (Wi-Fi 5G)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Online (5G Mobile)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Online (Ethernet)"
            else -> "Connected"
        }
    }

    // 5. Date & Time
    fun getCurrentTimeString(language: String = "EN"): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formatted = timeFormat.format(Date())
        return when (language.uppercase()) {
            "OD" -> "ବର୍ତ୍ତମାନ ସମୟ ହେଉଛି $formatted"
            "HI" -> "वर्तमान समय $formatted है"
            else -> "The current time is $formatted"
        }
    }

    fun getCurrentDateString(language: String = "EN"): String {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val formatted = dateFormat.format(Date())
        return when (language.uppercase()) {
            "OD" -> "ଆଜିର ତାରିଖ ହେଉଛି $formatted"
            "HI" -> "आज की तारीख $formatted है"
            else -> "Today is $formatted"
        }
    }

    // 6. Application Launching
    fun launchWhatsApp(): ControlActionResult =
        launchPackageOrWeb("com.whatsapp", "https://web.whatsapp.com", "WhatsApp")

    fun launchYouTube(): ControlActionResult =
        launchPackageOrWeb("com.google.android.youtube", "https://www.youtube.com", "YouTube")

    fun launchChrome(): ControlActionResult =
        launchPackageOrWeb("com.android.chrome", "https://www.google.com", "Google Chrome")

    fun launchCamera(): ControlActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ControlActionResult(true, "Camera opened successfully.", "LAUNCH_CAMERA")
        } catch (e: Exception) {
            ControlActionResult(false, "Could not open camera: ${e.message}", "CAMERA_FAILED")
        }
    }

    fun launchMusicPlayer(): ControlActionResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ControlActionResult(true, "Music player launched.", "LAUNCH_MUSIC")
        } catch (e: Exception) {
            launchPackageOrWeb("com.spotify.music", "https://open.spotify.com", "Music Player")
        }
    }

    fun launchSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_SETTINGS), "Settings")

    fun openWifiSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_WIFI_SETTINGS), "Wi-Fi Settings")

    fun openBluetoothSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Bluetooth Settings")

    fun openLocationSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), "Location Settings")

    fun openDisplaySettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_DISPLAY_SETTINGS), "Display Settings")

    fun openSoundSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_SOUND_SETTINGS), "Sound Settings")

    fun openNotificationAccessSettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), "Notification Access")

    fun openAccessibilitySettings(): ControlActionResult =
        launchIntentSafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), "Accessibility Settings")

    fun setAlarm(hour: Int = 7, minute: Int = 0, message: String = "JANU Alarm"): ControlActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ControlActionResult(true, "Alarm set for $hour:${String.format("%02d", minute)}", "SET_ALARM")
        } catch (e: Exception) {
            launchIntentSafely(Intent(AlarmClock.ACTION_SHOW_ALARMS), "Alarm Clock")
        }
    }

    fun launchIntentSafely(intent: Intent, targetName: String): ControlActionResult {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ControlActionResult(true, "Opening $targetName.", "OPEN_${targetName.uppercase().replace(" ", "_")}")
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch intent for $targetName", e)
            ControlActionResult(false, "Unable to open $targetName on this device.", "OPEN_FAILED")
        }
    }

    fun launchPackageOrWeb(packageName: String, webFallback: String, appName: String): ControlActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                ControlActionResult(true, "Opening $appName.", "LAUNCH_$appName")
            } catch (e: Exception) {
                ControlActionResult(false, "Failed to launch $appName: ${e.message}", "LAUNCH_FAILED")
            }
        } else {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ControlActionResult(true, "$appName app is not installed. Opening in browser.", "LAUNCH_WEB")
            } catch (e: Exception) {
                ControlActionResult(false, "Could not open $appName.", "APP_NOT_FOUND")
            }
        }
    }
}
