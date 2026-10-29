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
    private val androidControl: AndroidControlManager,
    private val repository: JanuRepository,
    private val automationManager: AutomationManager,
    private val codeGenManager: CodeGenerationManager
) {
    private val TAG = "JanuAiEngine"

    val offlineEngine = OfflineAssistantEngine(
        androidControl = androidControl,
        repository = repository,
        automationManager = automationManager,
        codeGenManager = codeGenManager
    )

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

        // 1. If online and not forced offline, attempt Online AI (Gemini 3.8 Flash via Firebase AI Logic)
        val isNetworkAvailable = androidControl.isNetworkAvailable()
        if (isNetworkAvailable && !forceOffline) {
            try {
                // If it's an immediate local hardware toggle or automation match, handle quickly
                val autoMatch = automationManager.checkVoiceTrigger(trimmed, preferredLanguage)
                if (autoMatch.isMatched && autoMatch.responseText != null) {
                    return@withContext AiResponse.Text(
                        spokenText = autoMatch.responseText,
                        displayText = "[Automation Triggered: ${autoMatch.ruleTitle}]\n${autoMatch.responseText}",
                        actionExecuted = "AUTOMATION_${autoMatch.actionType}",
                        modeUsed = "ONLINE"
                    )
                }

                return@withContext processOnlineAi(trimmed, preferredLanguage, memories)
            } catch (e: Exception) {
                Log.w(TAG, "Online AI query failed, falling back to Local Offline Core", e)
                return@withContext offlineEngine.processOfflineCommand(trimmed, preferredLanguage, memories)
            }
        }

        // 2. Pure Offline Local AI Engine
        return@withContext offlineEngine.processOfflineCommand(trimmed, preferredLanguage, memories)
    }

    private suspend fun processOnlineAi(
        query: String,
        preferredLanguage: String,
        memories: List<MemoryEntity>
    ): AiResponse {
        val userName = memories.find { it.key == "user_name" }?.value ?: "Kartik"
        val memoryContext = memories.joinToString("\n") { "- ${it.key}: ${it.value}" }

        val systemPrompt = """
            You are JANU AI, an advanced Jarvis-style AI voice assistant and expert Android/Python code engineer for $userName on his Oppo F25 5G Pro (Dimensity 7050, Android 16).
            Persona: loyal, ultra-intelligent, concise, and helpful.
            Current user language preference: $preferredLanguage.
            Support English, Odia (ଓଡ଼ିଆ), and Hindi (हिन्दी) natively.
            You can write complete Kotlin/Java/Python/XML code, plan automations, explain bugs, and control device functions.
            Keep spoken voice answers concise (1 to 3 sentences) unless the user specifically asks for full code or detailed explanation.
            
            Known user personal memory & context:
            $memoryContext
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nUser: $query\nJANU AI:"

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
}
