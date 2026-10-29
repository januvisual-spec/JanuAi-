package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerType: String, // "VOICE_PHRASE", "BATTERY_LOW", "BLUETOOTH_CONNECT", "TIME_EVENT"
    val triggerValue: String, // e.g. "good morning", "gaming mode", "battery_low_20"
    val actionType: String, // "SPEAK_TEXT", "LAUNCH_APP", "TOGGLE_FLASHLIGHT", "OPEN_PANEL"
    val actionTarget: String, // text to speak or package name or device command
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
