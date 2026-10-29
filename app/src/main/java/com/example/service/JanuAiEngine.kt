package com.example.service

import android.util.Log
import com.example.data.model.MemoryEntity
import com.example.data.repository.JanuRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale

sealed class AiResponse {
    data class Text(
        val spokenText: String,
        val displayText: String,
        val actionExecuted: String? = null,
        val modeUsed: String = "OFFLINE"
    ) : AiResponse()

    data class ConfirmationRequired(
        val confirmationPrompt: String,
        val pendingAction: String,
        val spokenText: String
    ) : AiResponse()
}

class JanuAiEngine(
    private val deviceControl: DeviceControlManager,
    private val repository: JanuRepository
) {
    private val TAG = "JanuAiEngine"

    // Online Gemini model initialized lazily per Firebase AI Logic skill
    private val onlineModel: GenerativeModel? by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI model initialization deferred", e)
            null
        }
    }

    suspend fun processQuery(
        query: String,
        preferredLanguage: String,
        forceOffline: Boolean,
        memories: List<MemoryEntity>
    ): AiResponse = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for immediate device hardware & system commands (Runs Offline instantly without latency!)
        val localCommandResult = checkLocalDeviceCommands(lower, trimmed, preferredLanguage, memories)
        if (localCommandResult != null) {
            return@withContext localCommandResult
        }

        // 2. If online and not forced offline, attempt Online AI (Gemini 3.8 Flash via Firebase AI Logic)
        val isNetworkAvailable = deviceControl.isNetworkAvailable()
        if (isNetworkAvailable && !forceOffline) {
            try {
                return@withContext processOnlineAi(trimmed, preferredLanguage, memories)
            } catch (e: Exception) {
                Log.w(TAG, "Online AI query failed, falling back to Local AI Core", e)
                // Graceful fallback to offline local intelligence
                return@withContext processOfflineNlp(lower, trimmed, preferredLanguage, memories, wasFallback = true)
            }
        }

        // 3. Pure Offline Local AI Core
        return@withContext processOfflineNlp(lower, trimmed, preferredLanguage, memories, wasFallback = false)
    }

    private suspend fun checkLocalDeviceCommands(
        lower: String,
        rawQuery: String,
        language: String,
        memories: List<MemoryEntity>
    ): AiResponse? {
        val userName = memories.find { it.key == "user_name" }?.value ?: "Kartik"

        // FLASHLIGHT COMMANDS
        if (lower.contains("flashlight on") || lower.contains("turn on flashlight") ||
            lower.contains("torch on") || lower.contains("turn on torch") ||
            lower.contains("light on") || lower.contains("ଫ୍ଲାସଲାଇଟ ଅନ୍") ||
            lower.contains("ଟର୍ଚ ଅନ୍") || lower.contains("टॉर्च चालू") || lower.contains("फ्लैशलाइट ऑन")
        ) {
            val success = deviceControl.setFlashlight(true)
            val text = when (language.uppercase()) {
                "OD" -> if (success) "ଫ୍ଲାସଲାଇଟ୍ ଅନ୍ କରାଗଲା, $userName!" else "ଫ୍ଲାସଲାଇଟ୍ ଖୋଲିବାରେ ସମସ୍ୟା ହେଲା।"
                "HI" -> if (success) "फ्लैशलाइट चालू कर दिया गया है, $userName!" else "फ्लैशलाइट चालू करने में समस्या आई।"
                else -> if (success) "Flashlight activated, $userName." else "Could not access the flashlight."
            }
            return AiResponse.Text(text, text, actionExecuted = "FLASHLIGHT_ON", modeUsed = "OFFLINE")
        }

        if (lower.contains("flashlight off") || lower.contains("turn off flashlight") ||
            lower.contains("torch off") || lower.contains("turn off torch") ||
            lower.contains("light off") || lower.contains("ଫ୍ଲାସଲାଇଟ ବନ୍ଦ") ||
            lower.contains("ଟର୍ଚ ବନ୍ଦ") || lower.contains("टॉर्च बंद") || lower.contains("फ्लैशलाइट बंद")
        ) {
            val success = deviceControl.setFlashlight(false)
            val text = when (language.uppercase()) {
                "OD" -> if (success) "ଫ୍ଲାସଲାଇଟ୍ ବନ୍ଦ କରାଗଲା, $userName!" else "ଫ୍ଲାସଲାଇଟ୍ ବନ୍ଦ କରିବାରେ ତ୍ରୁଟି।"
                "HI" -> if (success) "फ्लैशलाइट बंद कर दी गई है, $userName!" else "फ्लैशलाइट बंद करने में त्रुटि।"
                else -> if (success) "Flashlight turned off, $userName." else "Could not turn off flashlight."
            }
            return AiResponse.Text(text, text, actionExecuted = "FLASHLIGHT_OFF", modeUsed = "OFFLINE")
        }

        // APP LAUNCHING
        if (lower.contains("open youtube") || lower.contains("launch youtube") ||
            lower.contains("ୟୁଟ୍ୟୁବ୍ ଖୋଲ") || lower.contains("यूट्यूब खोलो")
        ) {
            deviceControl.launchYouTube()
            val text = when (language.uppercase()) {
                "OD" -> "ୟୁଟ୍ୟୁବ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "यूट्यूब खोला जा रहा है।"
                else -> "Opening YouTube for you, $userName."
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_APP:YouTube", modeUsed = "OFFLINE")
        }

        if (lower.contains("open whatsapp") || lower.contains("launch whatsapp") ||
            lower.contains("ହ୍ୱାଟ୍ସଆପ୍ ଖୋଲ") || lower.contains("व्हाट्सएप खोलो")
        ) {
            deviceControl.launchWhatsApp()
            val text = when (language.uppercase()) {
                "OD" -> "ହ୍ୱାଟ୍ସଆପ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "व्हाट्सएप खोला जा रहा है।"
                else -> "Opening WhatsApp, $userName."
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_APP:WhatsApp", modeUsed = "OFFLINE")
        }

        if (lower.contains("open camera") || lower.contains("launch camera") || lower.contains("take photo") ||
            lower.contains("କ୍ୟାମେରା ଖୋଲ") || lower.contains("कैमरा खोलो")
        ) {
            deviceControl.launchCamera()
            val text = when (language.uppercase()) {
                "OD" -> "କ୍ୟାମେରା ଖୋଲାଯାଉଛି।"
                "HI" -> "कैमरा खोला जा रहा है।"
                else -> "Opening camera, $userName."
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_APP:Camera", modeUsed = "OFFLINE")
        }

        if (lower.contains("open settings") || lower.contains("phone settings") ||
            lower.contains("ସେଟିଙ୍ଗସ୍ ଖୋଲ") || lower.contains("सेटिंग्स खोलो")
        ) {
            deviceControl.launchSettings()
            val text = when (language.uppercase()) {
                "OD" -> "ଡିଭାଇସ୍ ସେଟିଙ୍ଗସ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "डिवाइस सेटिंग्स खोली जा रही है।"
                else -> "Opening device settings."
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_APP:Settings", modeUsed = "OFFLINE")
        }

        if (lower.contains("open music") || lower.contains("play music") || lower.contains("start music") ||
            lower.contains("ଗୀତ ବଜାଅ") || lower.contains("ସଙ୍ଗୀତ ଖୋଲ") || lower.contains("म्यूजिक चलाओ") || lower.contains("गाना बजाओ")
        ) {
            deviceControl.launchMusic()
            val text = when (language.uppercase()) {
                "OD" -> "ମ୍ୟୁଜିକ୍ ପ୍ଲେୟାର୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "म्यूजिक प्लेयर खोला जा रहा है।"
                else -> "Opening your music player, $userName."
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_APP:Music", modeUsed = "OFFLINE")
        }

        // BATTERY TELEMETRY
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("ବ୍ୟାଟେରୀ") || lower.contains("बैटरी")) {
            val (level, charging) = deviceControl.refreshBatteryInfo()
            val chargeStateEn = if (charging) "and currently charging" else "on battery power"
            val text = when (language.uppercase()) {
                "OD" -> "ବ୍ୟାଟେରୀ ସ୍ତର $level% ଅଛି " + (if (charging) "ଏବଂ ଚାର୍ଜିଂ ହେଉଛି।" else "।")
                "HI" -> "बैटरी का स्तर $level% है " + (if (charging) "और चार्ज हो रहा है।" else "।")
                else -> "Battery is at $level% $chargeStateEn."
            }
            return AiResponse.Text(text, text, actionExecuted = "CHECK_BATTERY", modeUsed = "OFFLINE")
        }

        // DATE AND TIME
        if (lower.contains("time") || lower.contains("current time") || lower.contains("clock") ||
            lower.contains("ସମୟ") || lower.contains("କେତେ ବାଜିଛି") || lower.contains("समय")
        ) {
            val timeText = deviceControl.getCurrentTimeString(language)
            return AiResponse.Text(timeText, timeText, actionExecuted = "CHECK_TIME", modeUsed = "OFFLINE")
        }

        if (lower.contains("date") || lower.contains("today's date") || lower.contains("which day") ||
            lower.contains("ତାରିଖ") || lower.contains("ଆଜି କି ବାର") || lower.contains("तारीख") || lower.contains("दिन")
        ) {
            val dateText = deviceControl.getCurrentDateString(language)
            return AiResponse.Text(dateText, dateText, actionExecuted = "CHECK_DATE", modeUsed = "OFFLINE")
        }

        // VOLUME
        if (lower.contains("volume up") || lower.contains("increase volume") || lower.contains("ଭଲ୍ୟୁମ୍ ବଢାଅ") || lower.contains("आवाज बढ़ाओ")) {
            val vol = deviceControl.increaseVolume()
            val text = when (language.uppercase()) {
                "OD" -> "ଭଲ୍ୟୁମ୍ ବଢ଼ାଗଲା ($vol%)।"
                "HI" -> "आवाज़ बढ़ा दी गई है ($vol%)।"
                else -> "Volume increased to $vol%."
            }
            return AiResponse.Text(text, text, actionExecuted = "VOLUME_UP", modeUsed = "OFFLINE")
        }

        if (lower.contains("volume down") || lower.contains("decrease volume") || lower.contains("ଭଲ୍ୟୁମ୍ କମାଅ") || lower.contains("आवाज कम करो")) {
            val vol = deviceControl.decreaseVolume()
            val text = when (language.uppercase()) {
                "OD" -> "ଭଲ୍ୟୁମ୍ କମାଗଲା ($vol%)।"
                "HI" -> "आवाज़ कम कर दी गई है ($vol%)।"
                else -> "Volume decreased to $vol%."
            }
            return AiResponse.Text(text, text, actionExecuted = "VOLUME_DOWN", modeUsed = "OFFLINE")
        }

        // WI-FI ACTIONS
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ୱାଇଫାଇ") || lower.contains("वाई-फाई") || lower.contains("वाईफाई")) {
            deviceControl.actionsManager.openWifiPanel()
            val text = when (language.uppercase()) {
                "OD" -> "ୱାଇଫାଇ ନିୟନ୍ତ୍ରଣ ପ୍ୟାନେଲ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "वाई-फाई नियंत्रण पैनल खोला जा रहा है।"
                else -> "Opening Wi-Fi control panel."
            }
            return AiResponse.Text(text, text, actionExecuted = "TOGGLE_WIFI", modeUsed = "OFFLINE")
        }

        // BLUETOOTH ACTIONS
        if (lower.contains("bluetooth") || lower.contains("ବ୍ଲୁଟୁଥ୍") || lower.contains("ब्लूटूथ")) {
            deviceControl.actionsManager.openBluetoothSettings()
            val text = when (language.uppercase()) {
                "OD" -> "ବ୍ଲୁଟୁଥ୍ ସେଟିଙ୍ଗସ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "ब्लूटूथ सेटिंग्स खोली जा रही है।"
                else -> "Opening Bluetooth settings."
            }
            return AiResponse.Text(text, text, actionExecuted = "TOGGLE_BLUETOOTH", modeUsed = "OFFLINE")
        }

        // SCREEN BRIGHTNESS ACTIONS
        if (lower.contains("brightness") || lower.contains("bright") || lower.contains("dim screen") ||
            lower.contains("ବ୍ରାଇଟନେସ୍") || lower.contains("ଚମକ") || lower.contains("ब्राइटनेस")
        ) {
            val isIncrease = lower.contains("up") || lower.contains("increase") || lower.contains("more") ||
                    lower.contains("high") || lower.contains("ବଢାଅ") || lower.contains("बढ़ाओ")
            val isDecrease = lower.contains("down") || lower.contains("decrease") || lower.contains("low") ||
                    lower.contains("dim") || lower.contains("କମାଅ") || lower.contains("कम")

            val newLevel = when {
                isIncrease -> deviceControl.actionsManager.increaseBrightness(20)
                isDecrease -> deviceControl.actionsManager.decreaseBrightness(20)
                else -> {
                    val numberMatch = Regex("\\d+").find(lower)?.value?.toIntOrNull()
                    if (numberMatch != null) {
                        deviceControl.actionsManager.setBrightnessPercent(numberMatch)
                        numberMatch.coerceIn(5, 100)
                    } else {
                        deviceControl.actionsManager.increaseBrightness(15)
                    }
                }
            }

            val text = when (language.uppercase()) {
                "OD" -> "ସ୍କ୍ରିନ୍ ବ୍ରାଇଟନେସ୍ $newLevel% କରାଗଲା।"
                "HI" -> "स्क्रीन ब्राइटनेस $newLevel% कर दी गई है।"
                else -> "Screen brightness adjusted to $newLevel%."
            }
            return AiResponse.Text(text, text, actionExecuted = "SET_BRIGHTNESS:$newLevel", modeUsed = "OFFLINE")
        }

        // ALARM
        if (lower.contains("set alarm") || lower.contains("alarm set") || lower.contains("ଆଲାର୍ମ") || lower.contains("अलार्म")) {
            deviceControl.setAlarm(7, 0, "JANU Morning Alarm")
            val text = when (language.uppercase()) {
                "OD" -> "ଆଲାର୍ମ ସେଟ୍ କରାଗଲା।"
                "HI" -> "अलार्म सेट कर दिया गया है।"
                else -> "Alarm application opened with preset."
            }
            return AiResponse.Text(text, text, actionExecuted = "SET_ALARM", modeUsed = "OFFLINE")
        }

        // MEMORY INTENT: "remember that ...", "my name is ..."
        if (lower.startsWith("remember that ") || lower.startsWith("remember ") || lower.contains("ମନେରଖ") || lower.contains("याद रखो")) {
            val factToSave = rawQuery
                .replace("remember that", "", ignoreCase = true)
                .replace("remember", "", ignoreCase = true)
                .replace("ମନେରଖ", "", ignoreCase = true)
                .replace("याद रखो", "", ignoreCase = true)
                .trim()

            if (factToSave.isNotBlank()) {
                val key = "memory_${System.currentTimeMillis() % 10000}"
                repository.saveMemory(key, factToSave, "USER_NOTE")
                val text = when (language.uppercase()) {
                    "OD" -> "ମୁଁ ମନେ ରଖିଲି: \"$factToSave\""
                    "HI" -> "मैंने याद रख लिया: \"$factToSave\""
                    else -> "Saved to your personal memory vault: \"$factToSave\""
                }
                return AiResponse.Text(text, text, actionExecuted = "SAVE_MEMORY", modeUsed = "OFFLINE")
            }
        }

        // CONFIRMATION SAFEGUARD CHECK: Deletion of memories
        if (lower.contains("delete all memories") || lower.contains("clear memory") || lower.contains("reset memories")) {
            return AiResponse.ConfirmationRequired(
                confirmationPrompt = "Are you sure you want to permanently erase all saved memories in the vault?",
                pendingAction = "CLEAR_ALL_MEMORIES",
                spokenText = "This action will delete all stored personal memories. Please confirm on screen to proceed."
            )
        }

        return null
    }

    private suspend fun processOnlineAi(
        query: String,
        preferredLanguage: String,
        memories: List<MemoryEntity>
    ): AiResponse {
        val userName = memories.find { it.key == "user_name" }?.value ?: "Kartik"
        val memoryContext = memories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val systemPrompt = """
            You are JANU AI, a personal Jarvis-style AI voice assistant for $userName on his Oppo F25 5G Pro Android device.
            Your persona: ultra-smart, loyal, futuristic, concise, polite, and helpful.
            Current user language preference: $preferredLanguage.
            Respond in natural $preferredLanguage (support English, Odia - ଓଡ଼ିଆ, or Hindi - हिन्दी as appropriate).
            Always address the user politely.
            Keep spoken voice answers concise (1 to 3 sentences) unless the user specifically asks for in-depth information.
            
            Known user personal memory & context:
            $memoryContext
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nUser: $query\nJANU AI:"

        // Mandated 30-60s coroutine timeout per firebase-ai-logic-android skill
        val model = onlineModel ?: throw IllegalStateException("Firebase AI model not initialized")
        val response: GenerateContentResponse = withTimeout(40_000L) {
            model.generateContent(fullPrompt)
        }

        val text = response.text?.trim()
        if (text.isNullOrBlank()) {
            throw IllegalStateException("Empty response from Gemini")
        }

        return AiResponse.Text(
            spokenText = text,
            displayText = text,
            modeUsed = "ONLINE"
        )
    }

    private fun processOfflineNlp(
        lower: String,
        rawQuery: String,
        language: String,
        memories: List<MemoryEntity>,
        wasFallback: Boolean
    ): AiResponse {
        val userName = memories.find { it.key == "user_name" }?.value ?: "Kartik"
        val fallbackPrefix = if (wasFallback) {
            when (language.uppercase()) {
                "OD" -> "[ଅଫଲାଇନ୍ ମୋଡ୍] "
                "HI" -> "[ऑफलाइन मोड] "
                else -> "[Offline Core] "
            }
        } else ""

        // Greetings & Identity
        if (lower.contains("hello") || lower.contains("hi janu") || lower.contains("hey janu") ||
            lower.contains("ନମସ୍କାର") || lower.contains("କେମିତି ଅଛ") || lower.contains("नमस्ते") || lower.contains("कैसी हो")
        ) {
            val text = when (language.uppercase()) {
                "OD" -> "ନମସ୍କାର $userName! ମୁଁ ଜାନୁ ଆଇ, ମୁଁ ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?"
                "HI" -> "नमस्ते $userName! मैं जानू एआई हूँ। मैं आज आपकी क्या मदद कर सकती हूँ?"
                else -> "Hello $userName! I am JANU AI. How can I assist you today?"
            }
            return AiResponse.Text(text, "$fallbackPrefix$text", modeUsed = "OFFLINE")
        }

        if (lower.contains("who are you") || lower.contains("who made you") || lower.contains("your creator") ||
            lower.contains("ତୁମେ କିଏ") || lower.contains("तुम कौन हो")
        ) {
            val text = when (language.uppercase()) {
                "OD" -> "ମୁଁ ଜାନୁ ଆଇ (JANU AI), ଆପଣଙ୍କ ବ୍ୟକ୍ତିଗତ ବୁଦ୍ଧିମାନ ସହାୟକ। ମୁଁ ଆପଣଙ୍କ Oppo F25 5G Pro ରେ ସମ୍ପୂର୍ଣ୍ଣ ସୁରକ୍ଷିତ ଭାବେ କାମ କରେ।"
                "HI" -> "मैं जानू एआई (JANU AI) हूँ, आपकी पर्सनल इंटेलिजेंट असिस्टेंट। मैं आपके Oppo F25 5G Pro पर सुरक्षित रूप से काम करती हूँ।"
                else -> "I am JANU AI, your personal Jarvis-style intelligent assistant optimized for your Oppo F25 5G Pro."
            }
            return AiResponse.Text(text, "$fallbackPrefix$text", modeUsed = "OFFLINE")
        }

        if (lower.contains("my name") || lower.contains("who am i") || lower.contains("ମୋ ନାମ") || lower.contains("मेरा नाम")) {
            val text = when (language.uppercase()) {
                "OD" -> "ଆପଣଙ୍କ ନାମ $userName।"
                "HI" -> "आपका नाम $userName है।"
                else -> "Your name is $userName, my esteemed user."
            }
            return AiResponse.Text(text, "$fallbackPrefix$text", modeUsed = "OFFLINE")
        }

        if (lower.contains("phone") || lower.contains("oppo") || lower.contains("spec") || lower.contains("device") || lower.contains("dimensity")) {
            val text = "Your device is the Oppo F25 5G Pro powered by the MediaTek Dimensity 7050 octa-core chipset, with 8GB RAM, 256GB storage, and Android 16."
            return AiResponse.Text(text, "$fallbackPrefix$text", modeUsed = "OFFLINE")
        }

        if (lower.contains("joke") || lower.contains("ମଜାଳିଆ") || lower.contains("चुटकुला")) {
            val text = when (language.uppercase()) {
                "OD" -> "ଗୋଟିଏ କମ୍ପ୍ୟୁଟର ଅନ୍ୟ କମ୍ପ୍ୟୁଟରକୁ କହିଲା: ମୁଁ ତୁମକୁ ଭଲ ପାଏ, କିନ୍ତୁ ମୋ ମା' କହୁଛନ୍ତି ଆମ ମଧ୍ୟରେ କୌଣସି ୱାଇ-ଫାଇ ସଂଯୋଗ ନାହିଁ!"
                "HI" -> "एक कंप्यूटर ने दूसरे कंप्यूटर से कहा: मुझे तुमसे प्यार है, पर माँ कह रही हैं हमारे बीच कोई वाई-फाई कनेक्शन नहीं है!"
                else -> "Why do programmers prefer dark mode? Because light attracts bugs!"
            }
            return AiResponse.Text(text, "$fallbackPrefix$text", modeUsed = "OFFLINE")
        }

        // Default offline conversational answer
        val defaultText = when (language.uppercase()) {
            "OD" -> "ମୁଁ ଅଫଲାଇନ୍ ମୋଡ୍ ରେ କାର୍ଯ୍ୟ କରୁଛି। ଆପଣ ଫ୍ଲାସଲାଇଟ୍, ଆପ୍, ସମୟ, ବ୍ୟାଟେରୀ କିମ୍ବା ମେମୋରୀ ବିଷୟରେ ନିର୍ଦ୍ଦେଶ ଦେଇପାରିବେ।"
            "HI" -> "मैं ऑफलाइन मोड में सक्रिय हूँ। आप फ्लैशलाइट, ऐप्स, समय, बैटरी या मेमोरी के लिए निर्देश दे सकते हैं।"
            else -> "I processed your request in local offline mode. You can ask me to open apps, toggle flashlight, check battery, time, or review your memory."
        }
        return AiResponse.Text(defaultText, "$fallbackPrefix$defaultText", modeUsed = "OFFLINE")
    }
}
