package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "PREFERENCE", // PROFILE, PREFERENCE, NOTE, FACT
    val timestamp: Long = System.currentTimeMillis()
)
