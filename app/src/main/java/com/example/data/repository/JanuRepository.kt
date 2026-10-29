package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.MemoryDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.flow.Flow

class JanuRepository(
    private val memoryDao: MemoryDao,
    private val chatDao: ChatDao
) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

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
}
