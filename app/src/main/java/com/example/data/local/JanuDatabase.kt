package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.ProjectBackupEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        ChatMessageEntity::class,
        AutomationRuleEntity::class,
        ProjectBackupEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class JanuDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao
    abstract fun automationDao(): AutomationDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var INSTANCE: JanuDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JanuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JanuDatabase::class.java,
                    "janu_ai_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(JanuDatabaseCallback(scope))
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
                    val automationDao = database.automationDao()
                    val backupDao = database.backupDao()

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
                            message = "Hello Kartik! I am JANU AI, your upgraded personal Jarvis assistant. How can I assist you with your device, code, or automations today?",
                            mode = "ONLINE",
                            language = "EN"
                        )
                    )

                    // Seed Default Automation Rules
                    automationDao.insertRule(
                        AutomationRuleEntity(
                            title = "Good Morning Briefing",
                            triggerType = "VOICE_PHRASE",
                            triggerValue = "good morning",
                            actionType = "SPEAK_TEXT",
                            actionTarget = "Good morning Kartik! All systems nominal. Checking today's date and device status for you."
                        )
                    )
                    automationDao.insertRule(
                        AutomationRuleEntity(
                            title = "Gaming Mode Trigger",
                            triggerType = "VOICE_PHRASE",
                            triggerValue = "gaming mode",
                            actionType = "LAUNCH_APP",
                            actionTarget = "com.dts.freefireth"
                        )
                    )
                    automationDao.insertRule(
                        AutomationRuleEntity(
                            title = "Quick Music Playback",
                            triggerType = "VOICE_PHRASE",
                            triggerValue = "open music",
                            actionType = "LAUNCH_APP",
                            actionTarget = "music"
                        )
                    )
                    automationDao.insertRule(
                        AutomationRuleEntity(
                            title = "Low Battery Voice Alert",
                            triggerType = "BATTERY_LOW",
                            triggerValue = "20",
                            actionType = "SPEAK_TEXT",
                            actionTarget = "Attention Kartik: Battery level is below 20%. Please connect your charger."
                        )
                    )

                    // Initial Baseline Project Backup
                    backupDao.insertBackup(
                        ProjectBackupEntity(
                            versionName = "v1.0.0-baseline",
                            description = "Initial production baseline backup with voice, device controls, and memory.",
                            fileListSummary = "MainActivity, AndroidControlManager, JanuVoiceManager, JanuAiEngine, Database",
                            backupData = "{\"status\": \"baseline_verified\", \"target\": \"Oppo F25 5G Pro\", \"android\": 16}"
                        )
                    )
                }
            }
        }
    }
}
