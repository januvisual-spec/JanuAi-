package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_backups")
data class ProjectBackupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val versionName: String, // e.g. "v1.2.0-pre-feature"
    val description: String,
    val fileListSummary: String,
    val backupData: String, // JSON / code snapshot
    val timestamp: Long = System.currentTimeMillis()
)
