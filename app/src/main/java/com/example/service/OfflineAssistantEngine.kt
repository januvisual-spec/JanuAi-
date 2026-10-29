package com.example.service

import com.example.data.model.MemoryEntity
import com.example.data.repository.JanuRepository
import java.util.Locale

class OfflineAssistantEngine(
    private val androidControl: AndroidControlManager,
    private val repository: JanuRepository,
    private val automationManager: AutomationManager,
    private val codeGenManager: CodeGenerationManager
) {

    suspend fun processOfflineCommand(
        query: String,
        language: String,
        memories: List<MemoryEntity>
    ): AiResponse {
        val trimmed = query.trim()
        val lower = trimmed.lowercase(Locale.ROOT)
        val userName = memories.find { it.key == "user_name" }?.value ?: "Kartik"

        // 1. Check Automation Rule Triggers first
        val autoMatch = automationManager.checkVoiceTrigger(query, language)
        if (autoMatch.isMatched && autoMatch.responseText != null) {
            return AiResponse.Text(
                spokenText = autoMatch.responseText,
                displayText = "[Automation Triggered: ${autoMatch.ruleTitle}]\n${autoMatch.responseText}",
                actionExecuted = "AUTOMATION_${autoMatch.actionType}",
                modeUsed = "OFFLINE"
            )
        }

        // 2. Code Assistant / Code Generation Voice Commands
        if (lower.contains("code") || lower.contains("calculator app") || lower.contains("app bana") ||
            lower.contains("code lekha") || lower.contains("କୋଡ୍") || lower.contains("कोड लिखो")
        ) {
            val templateType = when {
                lower.contains("calculator") || lower.contains("କାଲକୁଲେଟର") || lower.contains("कैलकुलेटर") -> "CALCULATOR"
                lower.contains("torch") || lower.contains("flashlight") || lower.contains("ଫ୍ଲାସଲାଇଟ୍") -> "FLASHLIGHT"
                lower.contains("database") || lower.contains("room") || lower.contains("ନୋଟ୍") -> "ROOM_DB"
                lower.contains("python") || lower.contains("ପାଇଥନ୍") || lower.contains("पायथन") -> "PYTHON_SCRIPT"
                lower.contains("manifest") || lower.contains("permission") -> "MANIFEST_PERMISSIONS"
                else -> "CUSTOM"
            }

            val codeResult = codeGenManager.generateTemplate(templateType, query)
            val spokenReply = when (language.uppercase()) {
                "OD" -> "ମୁଁ ଆପଣଙ୍କ ପାଇଁ ${codeResult.title} କୋଡ୍ ପ୍ରସ୍ତୁତ କରିଛି। କୋଡ୍ ଷ୍ଟୁଡିଓ ଟ୍ୟାବ୍ ରେ ଦେଖନ୍ତୁ।"
                "HI" -> "मैंने आपके लिए ${codeResult.title} कोड तैयार कर दिया है। कोड स्टूडियो टैब में देखें।"
                else -> "I have generated the ${codeResult.title} code module for you. Check the Code Studio tab."
            }

            return AiResponse.Text(
                spokenText = spokenReply,
                displayText = "${codeResult.title}\nFile: ${codeResult.targetFileName}\n\n${codeResult.codeSnippet.take(280)}...\n\n(Full code available in Code Studio tab)",
                actionExecuted = "GENERATE_CODE:$templateType",
                modeUsed = "OFFLINE"
            )
        }

        // 3. Hardware Flashlight
        if (lower.contains("flashlight on") || lower.contains("torch on") ||
            lower.contains("light on") || lower.contains("ଫ୍ଲାସଲାଇଟ ଅନ୍") ||
            lower.contains("ଟର୍ଚ ଅନ୍") || lower.contains("flashlight on kara") ||
            lower.contains("टॉर्च चालू") || lower.contains("फ्लैशलाइट ऑन")
        ) {
            val result = androidControl.setFlashlight(true)
            val text = when (language.uppercase()) {
                "OD" -> if (result.isSuccess) "ଫ୍ଲାସଲାଇଟ୍ ଅନ୍ କରାଗଲା, $userName!" else "ଫ୍ଲାସଲାଇଟ୍ ଖୋଲିବାରେ ତ୍ରୁଟି।"
                "HI" -> if (result.isSuccess) "फ्लैशलाइट चालू कर दी गई है, $userName!" else "फ्लैशलाइट चालू करने में समस्या आई।"
                else -> if (result.isSuccess) "Flashlight activated, $userName." else "Could not access the flashlight."
            }
            return AiResponse.Text(text, text, actionExecuted = "FLASHLIGHT_ON", modeUsed = "OFFLINE")
        }

        if (lower.contains("flashlight off") || lower.contains("torch off") ||
            lower.contains("light off") || lower.contains("ଫ୍ଲାସଲାଇଟ ବନ୍ଦ") ||
            lower.contains("ଟର୍ଚ ବନ୍ଦ") || lower.contains("टॉर्च बंद") || lower.contains("फ्लैशलाइट बंद")
        ) {
            val result = androidControl.setFlashlight(false)
            val text = when (language.uppercase()) {
                "OD" -> if (result.isSuccess) "ଫ୍ଲାସଲାଇଟ୍ ବନ୍ଦ କରାଗଲା, $userName!" else "ଫ୍ଲାସଲାଇଟ୍ ବନ୍ଦ କରିବାରେ ତ୍ରୁଟି।"
                "HI" -> if (result.isSuccess) "फ्लैशलाइट बंद कर दी गई है, $userName!" else "फ्लैशलाइट बंद करने में त्रुटि।"
                else -> if (result.isSuccess) "Flashlight turned off, $userName." else "Could not turn off flashlight."
            }
            return AiResponse.Text(text, text, actionExecuted = "FLASHLIGHT_OFF", modeUsed = "OFFLINE")
        }

        // 4. App Launching (WhatsApp, YouTube, Chrome, Camera, Music, Settings)
        if (lower.contains("whatsapp") || lower.contains("ହ୍ୱାଟ୍ସଆପ୍") || lower.contains("व्हाट्सएप")) {
            val res = androidControl.launchWhatsApp()
            val text = when (language.uppercase()) {
                "OD" -> "ହ୍ୱାଟ୍ସଆପ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "व्हाट्सएप खोला जा रहा है।"
                else -> res.userMessage
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_WHATSAPP", modeUsed = "OFFLINE")
        }

        if (lower.contains("youtube") || lower.contains("ୟୁଟ୍ୟୁବ୍") || lower.contains("यूट्यूब")) {
            val res = androidControl.launchYouTube()
            val text = when (language.uppercase()) {
                "OD" -> "ୟୁଟ୍ୟୁବ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "यूट्यूब खोला जा रहा है।"
                else -> res.userMessage
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_YOUTUBE", modeUsed = "OFFLINE")
        }

        if (lower.contains("chrome") || lower.contains("browser") || lower.contains("ବ୍ରାଉଜର୍") || lower.contains("क्रोम")) {
            val res = androidControl.launchChrome()
            return AiResponse.Text(res.userMessage, res.userMessage, actionExecuted = "LAUNCH_CHROME", modeUsed = "OFFLINE")
        }

        if (lower.contains("camera") || lower.contains("କ୍ୟାମେରା") || lower.contains("कैमरा")) {
            val res = androidControl.launchCamera()
            val text = when (language.uppercase()) {
                "OD" -> "କ୍ୟାମେରା ଖୋଲାଯାଉଛି।"
                "HI" -> "कैमरा खोला जा रहा है।"
                else -> res.userMessage
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_CAMERA", modeUsed = "OFFLINE")
        }

        if (lower.contains("music") || lower.contains("play music") || lower.contains("open music") ||
            lower.contains("ଗୀତ") || lower.contains("ସଙ୍ଗୀତ") || lower.contains("म्यूजिक") || lower.contains("गाना")
        ) {
            val res = androidControl.launchMusicPlayer()
            val text = when (language.uppercase()) {
                "OD" -> "ମ୍ୟୁଜିକ୍ ପ୍ଲେୟାର୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "म्यूजिक प्लेयर खोला जा रहा है।"
                else -> res.userMessage
            }
            return AiResponse.Text(text, text, actionExecuted = "LAUNCH_MUSIC", modeUsed = "OFFLINE")
        }

        if (lower.contains("settings") || lower.contains("ସେଟିଙ୍ଗସ୍") || lower.contains("सेटिंग्स")) {
            val res = androidControl.launchSettings()
            return AiResponse.Text(res.userMessage, res.userMessage, actionExecuted = "LAUNCH_SETTINGS", modeUsed = "OFFLINE")
        }

        // 5. Wi-Fi & Bluetooth Panels
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ୱାଇଫାଇ") || lower.contains("वाईफाई")) {
            androidControl.actionsManager.openWifiPanel()
            val text = when (language.uppercase()) {
                "OD" -> "ୱାଇଫାଇ ପ୍ୟାନେଲ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "वाई-फाई नियंत्रण पैनल खोला जा रहा है।"
                else -> "Opening Wi-Fi control panel."
            }
            return AiResponse.Text(text, text, actionExecuted = "OPEN_WIFI", modeUsed = "OFFLINE")
        }

        if (lower.contains("bluetooth") || lower.contains("ବ୍ଲୁଟୁଥ୍") || lower.contains("ब्लूटूथ")) {
            androidControl.actionsManager.openBluetoothSettings()
            val text = when (language.uppercase()) {
                "OD" -> "ବ୍ଲୁଟୁଥ୍ ସେଟିଙ୍ଗସ୍ ଖୋଲାଯାଉଛି।"
                "HI" -> "ब्लूटूथ सेटिंग्स खोली जा रही है।"
                else -> "Opening Bluetooth settings."
            }
            return AiResponse.Text(text, text, actionExecuted = "OPEN_BLUETOOTH", modeUsed = "OFFLINE")
        }

        // 6. Battery Telemetry
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("ବ୍ୟାଟେରୀ") || lower.contains("बैटरी")) {
            val (level, charging) = androidControl.refreshBatteryInfo()
            val chargeStateEn = if (charging) "and currently charging" else "on battery power"
            val text = when (language.uppercase()) {
                "OD" -> "ବ୍ୟାଟେରୀ ସ୍ତର $level% ଅଛି " + (if (charging) "ଏବଂ ଚାର୍ଜିଂ ହେଉଛି।" else "।")
                "HI" -> "बैटरी का स्तर $level% है " + (if (charging) "और चार्ज हो रहा है।" else "।")
                else -> "Battery is at $level% $chargeStateEn."
            }
            return AiResponse.Text(text, text, actionExecuted = "CHECK_BATTERY", modeUsed = "OFFLINE")
        }

        // 7. Date and Time
        if (lower.contains("time") || lower.contains("clock") || lower.contains("ସମୟ") || lower.contains("समय")) {
            val timeText = androidControl.getCurrentTimeString(language)
            return AiResponse.Text(timeText, timeText, actionExecuted = "CHECK_TIME", modeUsed = "OFFLINE")
        }

        if (lower.contains("date") || lower.contains("today") || lower.contains("ତାରିଖ") || lower.contains("तारीख")) {
            val dateText = androidControl.getCurrentDateString(language)
            return AiResponse.Text(dateText, dateText, actionExecuted = "CHECK_DATE", modeUsed = "OFFLINE")
        }

        // 8. Offline Mode Explicit Command: "Janu, internet nahi, offline mode re kama kara"
        if (lower.contains("offline") || lower.contains("internet nahi") || lower.contains("ଅଫଲାଇନ୍") || lower.contains("ऑफलाइन")) {
            val text = when (language.uppercase()) {
                "OD" -> "ଆଜ୍ଞା କାର୍ତ୍ତିକ, ମୁଁ ସମ୍ପୂର୍ଣ୍ଣ ଅଫଲାଇନ୍ ମୋଡ୍ ରେ ପ୍ରସ୍ତୁତ। ସମସ୍ତ ଡିଭାଇସ୍ ନିୟନ୍ତ୍ରଣ ଓ କୋଡ୍ ଟେମ୍ପଲେଟ୍ ଉପଲବ୍ଧ ଅଛି।"
                "HI" -> "जी कार्तिक, मैं पूरी तरह से ऑफलाइन मोड में सक्रिय हूँ। सभी डिवाइस नियंत्रण और कोड टेम्प्लेट उपलब्ध हैं।"
                else -> "Understood Kartik. Offline mode is active. All local device controls, memories, and code templates are fully operational."
            }
            return AiResponse.Text(text, text, actionExecuted = "OFFLINE_CONFIRMED", modeUsed = "OFFLINE")
        }

        // 9. Self-Improvement / New Feature Query: "Janu, nua feature add kara"
        if (lower.contains("nua feature") || lower.contains("new feature") || lower.contains("feature add")) {
            val text = when (language.uppercase()) {
                "OD" -> "ନୂଆ ଫିଚର୍ ଯୋଡ଼ିବା ପାଇଁ ମୁଁ ପ୍ରସ୍ତୁତ। ଦୟାକରି କୋଡ୍ ଷ୍ଟୁଡିଓ ଟ୍ୟାବ୍ ଖୋଲନ୍ତୁ କିମ୍ବା ଆପଣଙ୍କ ଫିଚର୍ ବର୍ଣ୍ଣନା କରନ୍ତୁ।"
                "HI" -> "नया फीचर जोड़ने के लिए मैं तैयार हूँ। कृपया कोड स्टूडियो खोलें या अपने फीचर का विवरण दें।"
                else -> "Ready for self-improvement. Open the Code Studio tab or describe the feature you want to engineer."
            }
            return AiResponse.Text(text, text, actionExecuted = "FEATURE_REQUEST", modeUsed = "OFFLINE")
        }

        // 10. General Conversational Chit-chat
        if (lower.contains("hello") || lower.contains("hi janu") || lower.contains("hey janu") ||
            lower.contains("ନମସ୍କାର") || lower.contains("କେମିତି ଅଛ") || lower.contains("नमस्ते") || lower.contains("कैसी हो")
        ) {
            val text = when (language.uppercase()) {
                "OD" -> "ନମସ୍କାର $userName! ମୁଁ ଜାନୁ ଆଇ, ମୁଁ ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?"
                "HI" -> "नमस्ते $userName! मैं जानू एआई हूँ। मैं आज आपकी क्या मदद कर सकती हूँ?"
                else -> "Hello $userName! I am JANU AI, your personal Jarvis assistant. How can I help you today?"
            }
            return AiResponse.Text(text, text, modeUsed = "OFFLINE")
        }

        // Default local response
        val fallbackText = when (language.uppercase()) {
            "OD" -> "ମୁଁ ଅଫଲାଇନ୍ ମୋଡ୍ ରେ କାର୍ଯ୍ୟ କରୁଛି। ଆପଣ କୋଡ୍, ଫ୍ଲାସଲାଇଟ୍, ଆପ୍, ସମୟ, ବ୍ୟାଟେରୀ କିମ୍ବା ଅଟୋମେସନ୍ ବିଷୟରେ କହିପାରିବେ।"
            "HI" -> "मैं ऑफलाइन मोड में सक्रिय हूँ। आप कोड, फ्लैशलाइट, ऐप्स, समय, बैटरी या ऑटोमेशन के लिए निर्देश दे सकते हैं।"
            else -> "Offline Core Active. You can ask me to write code, open apps, toggle flashlight, check battery, time, or trigger automations."
        }
        return AiResponse.Text(fallbackText, fallbackText, modeUsed = "OFFLINE")
    }
}
