package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProjectBackupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {
    @Query("SELECT * FROM project_backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<ProjectBackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: ProjectBackupEntity): Long

    @Query("DELETE FROM project_backups WHERE id = :id")
    suspend fun deleteBackupById(id: Long)

    @Delete
    suspend fun deleteBackup(backup: ProjectBackupEntity)
}
