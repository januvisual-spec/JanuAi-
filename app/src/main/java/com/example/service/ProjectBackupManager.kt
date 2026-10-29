package com.example.service

import com.example.data.model.ProjectBackupEntity
import com.example.data.repository.JanuRepository
import kotlinx.coroutines.flow.Flow

class ProjectBackupManager(private val repository: JanuRepository) {

    val allBackups: Flow<List<ProjectBackupEntity>> = repository.allBackups

    suspend fun createVersionedBackup(
        versionName: String,
        description: String,
        fileListSummary: String,
        backupSnapshot: String
    ): Long {
        return repository.createBackup(
            versionName = versionName,
            description = description,
            fileList = fileListSummary,
            data = backupSnapshot
        )
    }

    suspend fun deleteBackup(backupId: Long) {
        repository.deleteBackup(backupId)
    }
}
