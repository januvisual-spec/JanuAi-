package com.example.service

import com.example.data.model.AutomationRuleEntity
import com.example.data.repository.JanuRepository

data class AutomationMatchResult(
    val isMatched: Boolean,
    val ruleTitle: String? = null,
    val actionType: String? = null,
    val actionTarget: String? = null,
    val responseText: String? = null
)

class AutomationManager(
    private val repository: JanuRepository,
    private val androidControl: AndroidControlManager
) {

    suspend fun checkVoiceTrigger(voiceInput: String, language: String): AutomationMatchResult {
        val rules = repository.getActiveAutomationRules()
        val lower = voiceInput.lowercase().trim()

        for (rule in rules) {
            if (rule.triggerType == "VOICE_PHRASE") {
                val triggerPhrase = rule.triggerValue.lowercase().trim()
                if (lower.contains(triggerPhrase)) {
                    val response = executeAction(rule, language)
                    return AutomationMatchResult(
                        isMatched = true,
                        ruleTitle = rule.title,
                        actionType = rule.actionType,
                        actionTarget = rule.actionTarget,
                        responseText = response
                    )
                }
            }
        }

        return AutomationMatchResult(isMatched = false)
    }

    suspend fun checkBatteryTrigger(batteryLevel: Int, isCharging: Boolean): AutomationMatchResult {
        if (isCharging) return AutomationMatchResult(isMatched = false)

        val rules = repository.getActiveAutomationRules()
        for (rule in rules) {
            if (rule.triggerType == "BATTERY_LOW") {
                val threshold = rule.triggerValue.toIntOrNull() ?: 20
                if (batteryLevel <= threshold) {
                    val message = "Battery Alert: Your phone is at $batteryLevel%. Please plug in your charger."
                    return AutomationMatchResult(
                        isMatched = true,
                        ruleTitle = rule.title,
                        actionType = "SPEAK_TEXT",
                        actionTarget = message,
                        responseText = message
                    )
                }
            }
        }

        return AutomationMatchResult(isMatched = false)
    }

    private fun executeAction(rule: AutomationRuleEntity, language: String): String {
        return when (rule.actionType) {
            "SPEAK_TEXT" -> {
                rule.actionTarget
            }
            "LAUNCH_APP" -> {
                when (rule.actionTarget.lowercase()) {
                    "music" -> {
                        androidControl.launchMusicPlayer()
                        "Launching music player as configured by automation rule: ${rule.title}."
                    }
                    else -> {
                        androidControl.launchPackageOrWeb(
                            rule.actionTarget,
                            "https://play.google.com/store/apps/details?id=${rule.actionTarget}",
                            rule.title
                        )
                        "Launching application for rule: ${rule.title}."
                    }
                }
            }
            "TOGGLE_FLASHLIGHT" -> {
                androidControl.toggleFlashlight()
                "Flashlight toggled by automation rule: ${rule.title}."
            }
            "OPEN_PANEL" -> {
                if (rule.actionTarget.contains("wifi", ignoreCase = true)) {
                    androidControl.actionsManager.openWifiPanel()
                    "Opening Wi-Fi control panel."
                } else {
                    androidControl.actionsManager.openBluetoothSettings()
                    "Opening Bluetooth settings."
                }
            }
            else -> {
                "Executed automation: ${rule.title}."
            }
        }
    }
}
