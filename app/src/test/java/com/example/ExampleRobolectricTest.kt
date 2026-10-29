package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JanuDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.service.DeviceActionsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
