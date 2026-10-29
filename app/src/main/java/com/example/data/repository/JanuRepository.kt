package com.example.data.repository

import com.example.data.local.AutomationDao
import com.example.data.local.BackupDao
import com.example.data.local.ChatDao
import com.example.data.local.MemoryDao
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.ProjectBackupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JanuRepository(
    private val memoryDao: MemoryDao,
    private val chatDao: ChatDao,
    private val automationDao: AutomationDao,
    private val backupDao: BackupDao
) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allRules: Flow<List<AutomationRuleEntity>> = automationDao.getAllRules()
    val allBackups: Flow<List<ProjectBackupEntity>> = backupDao.getAllBackups()

    fun searchMemories(query: String): Flow<List<MemoryEntity>> =
        memoryDao.searchMemories(query)

    suspend fun getMemoryByKey(key: String): MemoryEntity? =
        memoryDao.getMemoryByKey(key)

    suspend fun saveMemory(key: String, value: String, category: String = "PREFERENCE") {
        val existing = memoryDao.getMemoryByKey(key)
        if (existing != null) {
            memoryDao.updateMemory(existing.copy(value = value, timestamp = System.currentTimeMillis()))
        } else {
            memoryDao.insertMemory(
                MemoryEntity(
                    key = key,
                    value = value,
                    category = category,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearMemories() {
        memoryDao.clearAllMemories()
    }

    suspend fun exportMemoryText(): String {
        val memories = allMemories.first()
        val sb = StringBuilder()
        sb.append("=== JANU AI MEMORY VAULT EXPORT ===\n")
        sb.append("Timestamp: ${System.currentTimeMillis()}\n\n")
        for (m in memories) {
            sb.append("[${m.category}] ${m.key}: ${m.value}\n")
        }
        return sb.toString()
    }

    suspend fun saveChatMessage(
        sender: String,
        message: String,
        mode: String,
        language: String,
        actionExecuted: String? = null
    ): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                message = message,
                mode = mode,
                language = language,
                actionExecuted = actionExecuted,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearChatMessages() {
        chatDao.clearAllMessages()
    }

    // Automation Rules
    suspend fun getActiveAutomationRules(): List<AutomationRuleEntity> =
        automationDao.getActiveRules()

    suspend fun saveAutomationRule(rule: AutomationRuleEntity): Long =
        automationDao.insertRule(rule)

    suspend fun toggleAutomationRule(id: Long, enabled: Boolean) =
        automationDao.toggleRuleEnabled(id, enabled)

    suspend fun deleteAutomationRule(id: Long) =
        automationDao.deleteRuleById(id)

    // Project Backups
    suspend fun createBackup(versionName: String, description: String, fileList: String, data: String): Long {
        return backupDao.insertBackup(
            ProjectBackupEntity(
                versionName = versionName,
                description = description,
                fileListSummary = fileList,
                backupData = data,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBackup(id: Long) =
        backupDao.deleteBackupById(id)
}
