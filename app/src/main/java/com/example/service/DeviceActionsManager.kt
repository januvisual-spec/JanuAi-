package com.example.service

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceActionsManager(private val context: Context) {

    private val TAG = "DeviceActionsManager"

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _isWifiEnabled = MutableStateFlow(checkWifiEnabled())
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(checkBluetoothEnabled())
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _brightnessPercent = MutableStateFlow(getInitialBrightnessPercent())
    val brightnessPercent: StateFlow<Int> = _brightnessPercent.asStateFlow()

    private val _canWriteSystemSettings = MutableStateFlow(Settings.System.canWrite(context))
    val canWriteSystemSettings: StateFlow<Boolean> = _canWriteSystemSettings.asStateFlow()

    // Activity reference for live window brightness changes
    var currentActivity: Activity? = null

    // 1. Wi-Fi Actions
    fun checkWifiEnabled(): Boolean {
        return try {
            wifiManager?.isWifiEnabled == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking wifi status", e)
            false
        }
    }

    fun openWifiPanel(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(panelIntent)
                true
            } else {
                openWifiSettings()
            }
        } catch (e: Exception) {
            openWifiSettings()
        }
    }

    fun openWifiSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open wifi settings", e)
            false
        }
    }

    // 2. Bluetooth Actions
    fun checkBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.w(TAG, "Bluetooth connect permission not yet granted")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking bluetooth", e)
            false
        }
    }

    fun openBluetoothSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open bluetooth settings", e)
            false
        }
    }

    // 3. Screen Brightness Actions
    private fun getInitialBrightnessPercent(): Int {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            (brightness * 100 / 255).coerceIn(0, 100)
        } catch (e: Exception) {
            50
        }
    }

    fun setBrightnessPercent(percent: Int): Boolean {
        val clamped = percent.coerceIn(5, 100)
        _brightnessPercent.value = clamped

        // 1. Immediately apply to window if activity is attached
        currentActivity?.let { activity ->
            activity.runOnUiThread {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = clamped / 100f
                activity.window.attributes = layoutParams
            }
        }

        // 2. Attempt system-wide persistent brightness if WRITE_SETTINGS is granted
        return if (Settings.System.canWrite(context)) {
            _canWriteSystemSettings.value = true
            try {
                val rawVal = (clamped * 255 / 100).coerceIn(1, 255)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    rawVal
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write system brightness", e)
                false
            }
        } else {
            _canWriteSystemSettings.value = false
            // Successfully changed window brightness
            true
        }
    }

    fun increaseBrightness(step: Int = 15): Int {
        val next = (_brightnessPercent.value + step).coerceAtMost(100)
        setBrightnessPercent(next)
        return next
    }

    fun decreaseBrightness(step: Int = 15): Int {
        val next = (_brightnessPercent.value - step).coerceAtLeast(10)
        setBrightnessPercent(next)
        return next
    }

    fun openWriteSettingsPermissionScreen() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open write settings permission page", e)
        }
    }

    fun refreshStates() {
        _isWifiEnabled.value = checkWifiEnabled()
        _isBluetoothEnabled.value = checkBluetoothEnabled()
        _canWriteSystemSettings.value = Settings.System.canWrite(context)
        _brightnessPercent.value = getInitialBrightnessPercent()
    }
}
