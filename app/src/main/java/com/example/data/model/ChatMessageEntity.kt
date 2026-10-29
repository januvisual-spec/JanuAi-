package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "JANU"
    val message: String,
    val mode: String = "ONLINE", // "ONLINE" or "OFFLINE"
    val language: String = "EN", // "EN", "OD", "HI"
    val actionExecuted: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
