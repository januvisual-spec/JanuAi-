package com.example.service

import android.content.Context

/**
 * VoiceAssistantManager:
 * Provides speech-to-text, text-to-speech, push-to-talk, continuous mode,
 * and 'Hey Janu' wake word detection in English, Odia, and Hindi.
 */
class VoiceAssistantManager(
    context: Context,
    onCommandRecognized: (String) -> Unit
) : JanuVoiceManager(context, onCommandRecognized)
