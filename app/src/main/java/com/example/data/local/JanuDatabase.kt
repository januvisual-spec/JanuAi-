package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MemoryEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JanuDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: JanuDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JanuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JanuDatabase::class.java,
                    "janu_ai_vault.db"
                ).addCallback(JanuDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class JanuDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val memoryDao = database.memoryDao()
                    val chatDao = database.chatDao()

                    // Default Profile Memory
                    memoryDao.insertMemory(
                        MemoryEntity(
                            key = "user_name",
                            value = "Kartik",
                            category = "PROFILE"
                        )
                    )
                    memoryDao.insertMemory(
                        MemoryEntity(
                            key = "assistant_name",
                            value = "JANU AI",
                            category = "PROFILE"
                        )
                    )
                    memoryDao.insertMemory(
                        MemoryEntity(
                            key = "device_model",
                            value = "Oppo F25 5G Pro (Dimensity 7050)",
                            category = "SYSTEM"
                        )
                    )
                    memoryDao.insertMemory(
                        MemoryEntity(
                            key = "preferred_languages",
                            value = "English, Odia, Hindi",
                            category = "PREFERENCE"
                        )
                    )

                    // Initial Welcome Message
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            sender = "JANU",
                            message = "Hello Kartik! I am JANU AI, your personal intelligent assistant. How can I help you today?",
                            mode = "ONLINE",
                            language = "EN"
                        )
                    )
                }
            }
        }
    }
}
