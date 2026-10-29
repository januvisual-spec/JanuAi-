package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JanuDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.repository.JanuRepository
import com.example.service.AiResponse
import com.example.service.DeviceControlManager
import com.example.service.JanuAiEngine
import com.example.service.JanuVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JanuUiState(
    val status: String = "READY", // READY, LISTENING, PROCESSING, SPEAKING, ERROR
    val activeQuery: String = "",
    val lastResponse: String = "Hello Kartik! I am JANU AI. Tap the microphone or say a command.",
    val selectedLanguage: String = "EN", // EN, OD, HI
    val forceOfflineMode: Boolean = false,
    val networkStatus: String = "Online",
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val volumePercent: Int = 50,
    val brightnessPercent: Int = 50,
    val isWifiEnabled: Boolean = true,
    val isBluetoothEnabled: Boolean = false,
    val canWriteSystemSettings: Boolean = false,
    val pendingConfirmation: Pair<String, String>? = null, // prompt, action
    val speechPitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val wakeWordActive: Boolean = false
)

class JanuViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JanuDatabase.getDatabase(application, viewModelScope)
    private val repository = JanuRepository(database.memoryDao(), database.chatDao())
    val deviceControl = DeviceControlManager(application)

    private val voiceManager = JanuVoiceManager(application) { recognizedText ->
        onVoiceInputReceived(recognizedText)
    }

    private val aiEngine = JanuAiEngine(deviceControl, repository)

    private val _uiState = MutableStateFlow(JanuUiState())
    val uiState: StateFlow<JanuUiState> = _uiState.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val audioWaveformLevel: StateFlow<Float> = voiceManager.audioWaveformLevel
    val partialSpeech: StateFlow<String> = voiceManager.partialText

    init {
        refreshDeviceTelemetry()
        observeDeviceStates()
    }

    private fun observeDeviceStates() {
        viewModelScope.launch {
            deviceControl.isFlashlightOn.collect { on ->
                _uiState.update { it.copy(isFlashlightOn = on) }
            }
        }
        viewModelScope.launch {
            deviceControl.batteryLevel.collect { level ->
                _uiState.update { it.copy(batteryLevel = level) }
            }
        }
        viewModelScope.launch {
            deviceControl.isCharging.collect { charging ->
                _uiState.update { it.copy(isCharging = charging) }
            }
        }
        viewModelScope.launch {
            deviceControl.actionsManager.brightnessPercent.collect { br ->
                _uiState.update { it.copy(brightnessPercent = br) }
            }
        }
        viewModelScope.launch {
            deviceControl.actionsManager.isWifiEnabled.collect { wifi ->
                _uiState.update { it.copy(isWifiEnabled = wifi) }
            }
        }
        viewModelScope.launch {
            deviceControl.actionsManager.isBluetoothEnabled.collect { bt ->
                _uiState.update { it.copy(isBluetoothEnabled = bt) }
            }
        }
        viewModelScope.launch {
            deviceControl.actionsManager.canWriteSystemSettings.collect { canWrite ->
                _uiState.update { it.copy(canWriteSystemSettings = canWrite) }
            }
        }
    }

    fun attachActivity(activity: android.app.Activity) {
        deviceControl.actionsManager.currentActivity = activity
    }

    fun refreshDeviceTelemetry() {
        val (level, charging) = deviceControl.refreshBatteryInfo()
        val networkText = deviceControl.getNetworkStatusText()
        val volume = deviceControl.getCurrentVolumePercent()
        deviceControl.actionsManager.refreshStates()

        _uiState.update {
            it.copy(
                batteryLevel = level,
                isCharging = charging,
                networkStatus = networkText,
                volumePercent = volume,
                brightnessPercent = deviceControl.actionsManager.brightnessPercent.value,
                isWifiEnabled = deviceControl.actionsManager.isWifiEnabled.value,
                isBluetoothEnabled = deviceControl.actionsManager.isBluetoothEnabled.value,
                canWriteSystemSettings = deviceControl.actionsManager.canWriteSystemSettings.value
            )
        }
    }

    fun toggleListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
            _uiState.update { it.copy(status = "READY") }
        } else {
            voiceManager.stopSpeaking()
            _uiState.update { it.copy(status = "LISTENING", activeQuery = "") }
            voiceManager.startListening(_uiState.value.selectedLanguage)
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _uiState.update { it.copy(status = "READY") }
    }

    private fun onVoiceInputReceived(text: String) {
        if (text.isNotBlank()) {
            _uiState.update { it.copy(activeQuery = text) }
            submitQuery(text)
        } else {
            _uiState.update { it.copy(status = "READY") }
        }
    }

    fun submitQuery(userQuery: String) {
        if (userQuery.isBlank()) return

        val trimmed = userQuery.trim()
        _uiState.update { it.copy(status = "PROCESSING", activeQuery = trimmed) }

        viewModelScope.launch {
            // Save user message to database
            repository.saveChatMessage(
                sender = "USER",
                message = trimmed,
                mode = if (_uiState.value.forceOfflineMode) "OFFLINE" else "ONLINE",
                language = _uiState.value.selectedLanguage
            )

            val currentMemories = memories.value
            val response = aiEngine.processQuery(
                query = trimmed,
                preferredLanguage = _uiState.value.selectedLanguage,
                forceOffline = _uiState.value.forceOfflineMode,
                memories = currentMemories
            )

            when (response) {
                is AiResponse.Text -> {
                    _uiState.update {
                        it.copy(
                            status = "SPEAKING",
                            lastResponse = response.displayText
                        )
                    }

                    // Save AI response to database
                    repository.saveChatMessage(
                        sender = "JANU",
                        message = response.displayText,
                        mode = response.modeUsed,
                        language = _uiState.value.selectedLanguage,
                        actionExecuted = response.actionExecuted
                    )

                    // Speak aloud via TTS
                    voiceManager.speak(response.spokenText, _uiState.value.selectedLanguage)
                }

                is AiResponse.ConfirmationRequired -> {
                    _uiState.update {
                        it.copy(
                            status = "READY",
                            lastResponse = response.spokenText,
                            pendingConfirmation = Pair(response.confirmationPrompt, response.pendingAction)
                        )
                    }
                    voiceManager.speak(response.spokenText, _uiState.value.selectedLanguage)
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        viewModelScope.launch {
            when (pending.second) {
                "CLEAR_ALL_MEMORIES" -> {
                    repository.clearMemories()
                    val feedback = "All memories have been permanently cleared from the vault."
                    _uiState.update { it.copy(pendingConfirmation = null, lastResponse = feedback) }
                    voiceManager.speak(feedback, _uiState.value.selectedLanguage)
                }
                else -> {
                    _uiState.update { it.copy(pendingConfirmation = null) }
                }
            }
        }
    }

    fun dismissPendingAction() {
        _uiState.update { it.copy(pendingConfirmation = null) }
    }

    fun setLanguage(langCode: String) {
        _uiState.update { it.copy(selectedLanguage = langCode) }
        viewModelScope.launch {
            repository.saveMemory("preferred_language", langCode, "PREFERENCE")
        }
    }

    fun toggleOfflineMode(force: Boolean) {
        _uiState.update { it.copy(forceOfflineMode = force) }
    }

    fun toggleFlashlight() {
        deviceControl.toggleFlashlight()
    }

    fun setVolume(percent: Int) {
        deviceControl.setVolumePercent(percent)
        _uiState.update { it.copy(volumePercent = percent) }
    }

    fun increaseVolume() {
        val newVol = deviceControl.increaseVolume()
        _uiState.update { it.copy(volumePercent = newVol) }
    }

    fun decreaseVolume() {
        val newVol = deviceControl.decreaseVolume()
        _uiState.update { it.copy(volumePercent = newVol) }
    }

    fun openWifiPanel() {
        deviceControl.actionsManager.openWifiPanel()
    }

    fun openBluetoothSettings() {
        deviceControl.actionsManager.openBluetoothSettings()
    }

    fun setBrightness(percent: Int) {
        deviceControl.actionsManager.setBrightnessPercent(percent)
        _uiState.update { it.copy(brightnessPercent = percent) }
    }

    fun increaseBrightness() {
        val newBr = deviceControl.actionsManager.increaseBrightness(15)
        _uiState.update { it.copy(brightnessPercent = newBr) }
    }

    fun decreaseBrightness() {
        val newBr = deviceControl.actionsManager.decreaseBrightness(15)
        _uiState.update { it.copy(brightnessPercent = newBr) }
    }

    fun requestWriteSettingsPermission() {
        deviceControl.actionsManager.openWriteSettingsPermissionScreen()
    }

    fun launchAppByName(name: String) {
        when (name.lowercase()) {
            "youtube" -> deviceControl.launchYouTube()
            "whatsapp" -> deviceControl.launchWhatsApp()
            "camera" -> deviceControl.launchCamera()
            "settings" -> deviceControl.launchSettings()
            "music" -> deviceControl.launchMusic()
            "alarm" -> deviceControl.setAlarm(7, 0, "JANU Alarm")
        }
    }

    fun addCustomMemory(key: String, value: String, category: String = "NOTE") {
        viewModelScope.launch {
            repository.saveMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }

    fun updateVoiceSettings(pitch: Float, rate: Float) {
        voiceManager.pitch = pitch
        voiceManager.speechRate = rate
        _uiState.update { it.copy(speechPitch = pitch, speechRate = rate) }
    }

    fun toggleWakeWord(active: Boolean) {
        voiceManager.isWakeWordActive = active
        _uiState.update { it.copy(wakeWordActive = active) }
    }

    fun speakText(text: String) {
        voiceManager.speak(text, _uiState.value.selectedLanguage)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
