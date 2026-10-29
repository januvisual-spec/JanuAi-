package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JanuVoiceManager(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "JanuVoiceManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioWaveformLevel = MutableStateFlow(0f)
    val audioWaveformLevel: StateFlow<Float> = _audioWaveformLevel.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _wakeWordTriggered = MutableStateFlow(false)
    val wakeWordTriggered: StateFlow<Boolean> = _wakeWordTriggered.asStateFlow()

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setSpeechRate(value)
        }

    var pitch: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setPitch(value)
        }

    var isContinuousListening: Boolean = false
    var isWakeWordActive: Boolean = false
    var activeLanguageCode: String = "EN"

    var onWakeWordDetectedCallback: (() -> Unit)? = null

    init {
        initTts()
        initRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.language = Locale.ENGLISH
            textToSpeech?.setSpeechRate(speechRate)
            textToSpeech?.setPitch(pitch)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveformLevel.value = 0f
                    if (isWakeWordActive || isContinuousListening) {
                        startListening(activeLanguageCode)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveformLevel.value = 0f
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _audioWaveformLevel.value = 0f
                }
            })
            Log.d(TAG, "TTS Initialized successfully.")
        } else {
            Log.e(TAG, "TTS Initialization failed: $status")
        }
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioWaveformLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioWaveformLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioWaveformLevel.value = 0f
                        Log.w(TAG, "Speech recognition error: $error")
                        // If wake word is active and we hit silence/timeout, restart passive listening loop
                        if (isWakeWordActive && error != SpeechRecognizer.ERROR_CLIENT) {
                            try {
                                startListening(activeLanguageCode)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error restarting wake listener", e)
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioWaveformLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            _partialText.value = recognized

                            if (isWakeWordActive && checkWakeWordMatch(recognized)) {
                                _wakeWordTriggered.value = true
                                onWakeWordDetectedCallback?.invoke()
                                val queryAfterWake = stripWakeWord(recognized)
                                if (queryAfterWake.isNotBlank()) {
                                    onCommandRecognized(queryAfterWake)
                                } else {
                                    onCommandRecognized(recognized)
                                }
                            } else {
                                onCommandRecognized(recognized)
                            }
                        }

                        if (isWakeWordActive && !_isSpeaking.value) {
                            startListening(activeLanguageCode)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partial = matches[0]
                            _partialText.value = partial

                            if (isWakeWordActive && checkWakeWordMatch(partial)) {
                                _wakeWordTriggered.value = true
                                onWakeWordDetectedCallback?.invoke()
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun checkWakeWordMatch(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return lower.contains("hey janu") || lower.contains("hey jaanu") ||
                lower.contains("hi janu") || lower.contains("hello janu") ||
                lower.contains("janu") || lower.contains("ହେ ଜାନୁ") ||
                lower.contains("ଜାନୁ") || lower.contains("हे जानू") ||
                lower.contains("जानू")
    }

    private fun stripWakeWord(text: String): String {
        return text
            .replace("hey janu", "", ignoreCase = true)
            .replace("hey jaanu", "", ignoreCase = true)
            .replace("hi janu", "", ignoreCase = true)
            .replace("hello janu", "", ignoreCase = true)
            .replace("ହେ ଜାନୁ", "", ignoreCase = true)
            .replace("हे जानू", "", ignoreCase = true)
            .trim()
    }

    fun startListening(languageCode: String = activeLanguageCode) {
        activeLanguageCode = languageCode
        if (_isSpeaking.value) {
            stopSpeaking()
        }

        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            val localeTag = when (languageCode.uppercase()) {
                "OD", "OR" -> "or-IN"
                "HI" -> "hi-IN"
                else -> "en-IN"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener", e)
        } finally {
            _isListening.value = false
            _audioWaveformLevel.value = 0f
        }
    }

    fun speak(text: String, languageCode: String = activeLanguageCode) {
        if (!isTtsInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not ready to speak")
            return
        }

        val targetLocale = when (languageCode.uppercase()) {
            "OD", "OR" -> Locale("or", "IN")
            "HI" -> Locale("hi", "IN")
            else -> Locale.ENGLISH
        }

        val result = textToSpeech?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech?.setLanguage(Locale.ENGLISH)
        }

        val utteranceId = "janu_utt_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
            _audioWaveformLevel.value = 0f
        }
    }

    fun shutdown() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null

        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
