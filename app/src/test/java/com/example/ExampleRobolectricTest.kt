package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JanuDatabase
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.ProjectBackupEntity
import com.example.data.repository.JanuRepository
import com.example.service.CodeValidationManager
import com.example.service.DeviceActionsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: JanuDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JanuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JANU AI", appName)
    }

    @Test
    fun testInsertAndRetrieveMemory() = runBlocking {
        val memoryDao = db.memoryDao()
        val testMemory = MemoryEntity(
            key = "user_name",
            value = "Kartik",
            category = "PROFILE"
        )
        memoryDao.insertMemory(testMemory)

        val retrieved = memoryDao.getMemoryByKey("user_name")
        assertNotNull(retrieved)
        assertEquals("Kartik", retrieved?.value)
    }

    @Test
    fun testInsertAndRetrieveChatMessage() = runBlocking {
        val chatDao = db.chatDao()
        val msg = ChatMessageEntity(
            sender = "JANU",
            message = "Hello Kartik! How can I help you today?",
            mode = "ONLINE",
            language = "EN"
        )
        chatDao.insertMessage(msg)

        val messages = chatDao.getAllMessages().first()
        assertTrue(messages.isNotEmpty())
        assertEquals("JANU", messages[0].sender)
    }

    @Test
    fun testAutomationRuleDaoOperations() = runBlocking {
        val automationDao = db.automationDao()
        val rule = AutomationRuleEntity(
            title = "Sleep Focus",
            triggerType = "VOICE_PHRASE",
            triggerValue = "good night",
            actionType = "SPEAK_TEXT",
            actionTarget = "Good night Kartik! Sweet dreams.",
            isEnabled = true
        )
        val id = automationDao.insertRule(rule)
        assertTrue(id > 0)

        val activeRules = automationDao.getActiveRules()
        assertEquals(1, activeRules.size)
        assertEquals("Sleep Focus", activeRules[0].title)

        // Toggle disabled
        automationDao.toggleRuleEnabled(id, false)
        val afterToggle = automationDao.getActiveRules()
        assertEquals(0, afterToggle.size)
    }

    @Test
    fun testBackupDaoOperations() = runBlocking {
        val backupDao = db.backupDao()
        val backup = ProjectBackupEntity(
            versionName = "v1.0.1-test",
            description = "Test backup snapshot",
            fileListSummary = "MainActivity.kt, JanuDatabase.kt",
            backupData = "{\"status\": \"test_ok\"}"
        )
        val id = backupDao.insertBackup(backup)
        assertTrue(id > 0)

        val all = backupDao.getAllBackups().first()
        assertEquals(1, all.size)
        assertEquals("v1.0.1-test", all[0].versionName)
    }

    @Test
    fun testMemoryExportFormatting() = runBlocking {
        val repo = JanuRepository(db.memoryDao(), db.chatDao(), db.automationDao(), db.backupDao())
        repo.saveMemory("fav_color", "Neon Green", "PREFERENCE")
        repo.saveMemory("owner", "Kartik", "PROFILE")

        val exported = repo.exportMemoryText()
        assertTrue(exported.contains("=== JANU AI MEMORY VAULT EXPORT ==="))
        assertTrue(exported.contains("fav_color: Neon Green"))
        assertTrue(exported.contains("owner: Kartik"))
    }

    @Test
    fun testCodeValidatorSyntaxCheck() {
        val validator = CodeValidationManager()
        val validKotlin = """
            package com.example.test
            
            class TestClass {
                fun compute(): Int {
                    return 42
                }
            }
        """.trimIndent()

        val validResult = validator.validateCode(validKotlin, "KOTLIN")
        assertTrue(validResult.isValid)
        assertTrue(validResult.issuesFound.isEmpty())

        val unbalancedKotlin = """
            class Broken {
                fun test() {
            
        """.trimIndent()
        val brokenResult = validator.validateCode(unbalancedKotlin, "KOTLIN")
        assertFalse(brokenResult.isValid)
        assertTrue(brokenResult.issuesFound.any { it.contains("braces", ignoreCase = true) })
    }

    @Test
    fun testDeviceActionsBrightnessClamping() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val actionsManager = DeviceActionsManager(context)

        actionsManager.setBrightnessPercent(150)
        assertEquals(100, actionsManager.brightnessPercent.value)

        actionsManager.setBrightnessPercent(-20)
        assertEquals(5, actionsManager.brightnessPercent.value)

        val stepped = actionsManager.increaseBrightness(20)
        assertEquals(25, stepped)
    }
}
