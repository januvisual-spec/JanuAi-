package com.example.service

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
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

class DeviceControlManager(private val context: Context) {

    private val TAG = "DeviceControlManager"

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _batteryLevel = MutableStateFlow(getInitialBatteryLevel())
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(getInitialIsCharging())
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    val actionsManager = DeviceActionsManager(context)

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
            Log.e(TAG, "Error finding flash camera", e)
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
    fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val id = cameraIdWithFlash ?: cameraManager?.cameraIdList?.firstOrNull()
            if (id != null) {
                cameraManager?.setTorchMode(id, enabled)
                _isFlashlightOn.value = enabled
                true
            } else {
                false
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Camera access error toggling flashlight", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "General error toggling flashlight", e)
            false
        }
    }

    fun toggleFlashlight(): Boolean {
        val target = !_isFlashlightOn.value
        return setFlashlight(target)
    }

    // 2. Battery Telemetry
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

    // 3. Audio & Volume Control
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

    fun setVolumePercent(percent: Int) {
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val target = ((percent.coerceIn(0, 100) / 100f) * maxVolume).toInt()
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
    }

    fun getCurrentVolumePercent(): Int {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        return if (max > 0) (current * 100 / max) else 50
    }

    // 4. Connectivity & WiFi
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun getNetworkStatusText(): String {
        val network = connectivityManager?.activeNetwork
        if (network == null) return "Offline (No Network)"
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
        return when (language.uppercase())
        {
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

    // 6. App Launching & System Intents
    fun launchYouTube(): Boolean {
        return launchPackageOrWeb("com.google.android.youtube", "https://www.youtube.com")
    }

    fun launchWhatsApp(): Boolean {
        return launchPackageOrWeb("com.whatsapp", "https://web.whatsapp.com")
    }

    fun launchCamera(): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                Log.e(TAG, "Cannot launch camera", ex)
                false
            }
        }
    }

    fun launchSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open settings", e)
            false
        }
    }

    fun launchMusic(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            launchPackageOrWeb("com.spotify.music", "https://open.spotify.com")
        }
    }

    fun setAlarm(hour: Int = 7, minute: Int = 0, message: String = "JANU Alarm"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                Log.e(TAG, "Cannot open alarm", ex)
                false
            }
        }
    }

    fun launchWebSearch(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                true
            } catch (ex: Exception) {
                Log.e(TAG, "Web search failed", ex)
                false
            }
        }
    }

    fun launchPackageOrWeb(packageName: String, webFallback: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch package or web: $packageName", e)
                false
            }
        }
    }
}
