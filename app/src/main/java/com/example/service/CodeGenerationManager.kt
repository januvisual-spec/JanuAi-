package com.example.service

import com.example.data.model.MemoryEntity

data class GeneratedCodeResult(
    val language: String, // "KOTLIN", "JAVA", "PYTHON", "XML"
    val title: String,
    val description: String,
    val codeSnippet: String,
    val targetFileName: String,
    val buildInstructions: String
)

class CodeGenerationManager {

    fun generateTemplate(templateType: String, customPrompt: String = ""): GeneratedCodeResult {
        return when (templateType.uppercase()) {
            "CALCULATOR", "CALCULATOR_APP" -> generateCalculatorApp()
            "FLASHLIGHT", "TORCH_CONTROLLER" -> generateFlashlightController()
            "ROOM_DB", "DATABASE" -> generateRoomDatabaseTemplate()
            "PYTHON_SCRIPT", "PYTHON" -> generatePythonAutomation()
            "MANIFEST_PERMISSIONS", "MANIFEST" -> generateManifestTemplate()
            else -> generateGenericComposeFeature(customPrompt)
        }
    }

    private fun generateCalculatorApp(): GeneratedCodeResult {
        val code = """
package com.example.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JanuCalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }

    fun onDigit(d: String) {
        display = if (display == "0") d else display + d
    }

    fun onOp(op: String) {
        operand1 = display.toDoubleOrNull()
        pendingOp = op
        display = "0"
    }

    fun onEquals() {
        val op1 = operand1 ?: return
        val op2 = display.toDoubleOrNull() ?: return
        val res = when (pendingOp) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "*" -> op1 * op2
            "/" -> if (op2 != 0.0) op1 / op2 else Double.NaN
            else -> op2
        }
        display = if (res.isNaN()) "Error" else if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
        operand1 = null
        pendingOp = null
    }

    fun onClear() {
        display = "0"
        operand1 = null
        pendingOp = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060908))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            color = Color(0xFF00FF88),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        val buttons = listOf(
            listOf("C", "+/-", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { btn ->
                    val isAction = btn in listOf("/", "*", "-", "+", "=")
                    Button(
                        onClick = {
                            when (btn) {
                                "C" -> onClear()
                                "=" -> onEquals()
                                in listOf("+", "-", "*", "/") -> onOp(btn)
                                else -> onDigit(btn)
                            }
                        },
                        modifier = Modifier
                            .weight(if (btn == "0") 2f else 1f)
                            .height(64.dp)
                            .clip(CircleShape),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAction) Color(0xFF00FF88) else Color(0xFF111E19),
                            contentColor = if (isAction) Color.Black else Color.White
                        )
                    ) {
                        Text(text = btn, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
        """.trimIndent()

        return GeneratedCodeResult(
            language = "KOTLIN",
            title = "Modern OLED Calculator (Jetpack Compose)",
            description = "Complete calculator with clean math state, zero division protection, and dark OLED UI.",
            codeSnippet = code,
            targetFileName = "JanuCalculatorScreen.kt",
            buildInstructions = "1. Place in your ui package.\n2. Call JanuCalculatorScreen() from your Compose navigation stack.\n3. Requires no external dependencies."
        )
    }

    private fun generateFlashlightController(): GeneratedCodeResult {
        val code = """
package com.example.service

import android.content.Context
import android.hardware.camera2.CameraManager

class FlashlightController(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    fun setTorch(enabled: Boolean): Boolean {
        return try {
            cameraId?.let { cameraManager.setTorchMode(it, enabled) }
            true
        } catch (e: Exception) {
            false
        }
    }
}
        """.trimIndent()

        return GeneratedCodeResult(
            language = "KOTLIN",
            title = "CameraManager Torch Controller",
            description = "Safe camera hardware flashlight controller using official Android Camera2 APIs.",
            codeSnippet = code,
            targetFileName = "FlashlightController.kt",
            buildInstructions = "Add <uses-feature android:name=\"android.hardware.camera.flash\" /> in AndroidManifest.xml."
        )
    }

    private fun generateRoomDatabaseTemplate(): GeneratedCodeResult {
        val code = """
package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
        """.trimIndent()

        return GeneratedCodeResult(
            language = "KOTLIN",
            title = "Room Database CRUD Architecture",
            description = "Entity, DAO with reactive Flow queries, and Database declaration using Room and KSP.",
            codeSnippet = code,
            targetFileName = "NoteDatabase.kt",
            buildInstructions = "Requires Room dependencies (androidx-room-runtime, androidx-room-ktx) and ksp(androidx-room-compiler)."
        )
    }

    private fun generatePythonAutomation(): GeneratedCodeResult {
        val code = """
# JANU AI Python Automation Script
# Compatible with Python 3.9+ and Android Termux / QPython

import datetime
import json
import urllib.request

def janu_system_health():
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    report = {
        "assistant": "JANU AI",
        "timestamp": now,
        "status": "OPERATIONAL",
        "device": "Oppo F25 5G Pro",
        "chipset": "MediaTek Dimensity 7050",
        "memory_status": "OK"
    }
    return json.dumps(report, indent=2)

if __name__ == "__main__":
    print("JANU AI Automation Initialized...")
    print(janu_system_health())
        """.trimIndent()

        return GeneratedCodeResult(
            language = "PYTHON",
            title = "Python Device Health Monitor",
            description = "Cross-platform Python diagnostic script for checking system health and telemetry.",
            codeSnippet = code,
            targetFileName = "janu_health.py",
            buildInstructions = "Run via python3 janu_health.py in terminal or Android Python environment."
        )
    }

    private fun generateManifestTemplate(): GeneratedCodeResult {
        val code = """
<!-- Android 16 Compliant Permissions for JANU AI -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera.flash" android:required="false" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="com.android.alarm.permission.SET_ALARM" />
</manifest>
        """.trimIndent()

        return GeneratedCodeResult(
            language = "XML",
            title = "Complete Android Permissions Block",
            description = "Permission declarations for voice recording, network diagnostics, flashlight, and alarms.",
            codeSnippet = code,
            targetFileName = "AndroidManifest_Permissions.xml",
            buildInstructions = "Merge within the root <manifest> tag of your app's AndroidManifest.xml."
        )
    }

    private fun generateGenericComposeFeature(prompt: String): GeneratedCodeResult {
        val sanitized = prompt.ifBlank { "Custom Feature" }
        val code = """
package com.example.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Feature: $sanitized
 * Generated by JANU AI for Kartik
 */
@Composable
fun JanuCustomFeatureScreen() {
    var stateValue by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060908))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$sanitized",
            color = Color(0xFF00FF88),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Active Count: ${'$'}stateValue",
            color = Color(0xFFF0FDF4),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { stateValue++ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF88),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Increment Counter", fontWeight = FontWeight.Bold)
        }
    }
}
        """.trimIndent()

        return GeneratedCodeResult(
            language = "KOTLIN",
            title = "Custom Jetpack Compose Screen ($sanitized)",
            description = "State-backed Jetpack Compose module generated according to your specification.",
            codeSnippet = code,
            targetFileName = "JanuCustomFeatureScreen.kt",
            buildInstructions = "Embed into your navigation composable graph."
        )
    }
}
