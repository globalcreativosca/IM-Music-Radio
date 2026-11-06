package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RadioRepository
import com.example.model.ChatMessage
import com.example.model.EqualizerPreset
import com.example.model.PlaybackState
import com.example.model.RadioConfig
import com.example.model.RadioProgram
import com.example.model.RadioStation
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class RadioNavScreen(val title: String) {
    PLAYER("En Vivo"),
    CHAT("Chat"),
    SCHEDULE("Programas"),
    ADMIN("Admin")
}

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    val repository = RadioRepository(application)
    val playerManager = RadioPlayerManager(application)

    val config: StateFlow<RadioConfig> = repository.config
    val stations: StateFlow<List<RadioStation>> = repository.stations
    val programs: StateFlow<List<RadioProgram>> = repository.programs
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val userName: StateFlow<String> = repository.userName
    val userAvatarColor: StateFlow<Long> = repository.userAvatarColor

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState
    val currentStation: StateFlow<RadioStation?> = playerManager.currentStation
    val volume: StateFlow<Float> = playerManager.volume
    val isMuted: StateFlow<Boolean> = playerManager.isMuted
    val equalizerPreset: StateFlow<EqualizerPreset> = playerManager.equalizerPreset
    val sleepTimerSeconds: StateFlow<Int> = playerManager.sleepTimerSeconds
    val visualizerBars: StateFlow<List<Float>> = playerManager.visualizerBars
    val errorMessage: StateFlow<String?> = playerManager.errorMessage

    private val _currentScreen = MutableStateFlow(RadioNavScreen.PLAYER)
    val currentScreen: StateFlow<RadioNavScreen> = _currentScreen.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminPasswordError = MutableStateFlow<String?>(null)
    val adminPasswordError: StateFlow<String?> = _adminPasswordError.asStateFlow()

    private val _adminActionFeedback = MutableStateFlow<String?>(null)
    val adminActionFeedback: StateFlow<String?> = _adminActionFeedback.asStateFlow()

    private val _streamTestResult = MutableStateFlow<String?>(null)
    val streamTestResult: StateFlow<String?> = _streamTestResult.asStateFlow()

    private val _isTestingStream = MutableStateFlow(false)
    val isTestingStream: StateFlow<Boolean> = _isTestingStream.asStateFlow()

    init {
        // Set initial volume from repository config
        playerManager.setVolume(config.value.defaultVolume)

        // Select primary station initially
        val primary = stations.value.firstOrNull()
        if (primary != null) {
            // If configured to autoplay, play immediately
            if (config.value.autoPlayOnStart) {
                playerManager.play(primary)
            } else {
                // Pre-select station
                playerManager.play(primary)
                playerManager.pause()
            }
        }
    }

    fun navigateTo(screen: RadioNavScreen) {
        _currentScreen.value = screen
    }

    // --- Player Actions ---
    fun playStation(station: RadioStation) {
        playerManager.play(station)
    }

    fun togglePlayPause() {
        val curr = currentStation.value
        if (curr == null) {
            val first = stations.value.firstOrNull()
            if (first != null) playerManager.play(first)
        } else {
            playerManager.togglePlayPause()
        }
    }

    fun setVolume(vol: Float) {
        playerManager.setVolume(vol)
    }

    fun toggleMute() {
        playerManager.toggleMute()
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        playerManager.setEqualizerPreset(preset)
    }

    fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimer(minutes)
    }

    fun toggleFavorite(stationId: String) {
        repository.toggleFavoriteStation(stationId)
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String, isSongRequest: Boolean = false, songTitle: String? = null) {
        if (text.isBlank() && (!isSongRequest || songTitle.isNullOrBlank())) return
        val asAdmin = _isAdminAuthenticated.value
        repository.sendChatMessage(
            text = text.trim(),
            isSongRequest = isSongRequest,
            songTitle = songTitle?.trim(),
            isDjAdmin = asAdmin
        )
    }

    fun reactToChat(id: String, type: String) {
        repository.reactToMessage(id, type)
    }

    fun updateListenerProfile(name: String, color: Long) {
        repository.updateUserName(name)
        repository.updateUserColor(color)
    }

    // --- Admin Actions ---
    fun loginAdmin(passwordInput: String): Boolean {
        _adminPasswordError.value = null
        val ok = repository.verifyAdminPassword(passwordInput)
        if (ok) {
            _isAdminAuthenticated.value = true
            _adminPasswordError.value = null
            _adminActionFeedback.value = "Sesión de administrador iniciada con éxito"
            return true
        } else {
            _adminPasswordError.value = "Contraseña incorrecta. Intente nuevamente."
            return false
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _adminPasswordError.value = null
    }

    fun clearAdminFeedback() {
        _adminActionFeedback.value = null
    }

    fun saveRadioConfig(newConfig: RadioConfig) {
        repository.updateConfig(newConfig)
        _adminActionFeedback.value = "Configuración de la radio guardada correctamente"

        // If the current playing station was the primary admin station, update playback stream
        if (currentStation.value?.id == "primary_admin_station") {
            val updatedStation = RadioStation(
                id = "primary_admin_station",
                name = newConfig.stationName,
                genre = newConfig.genre,
                streamUrl = newConfig.primaryStreamUrl,
                bitrate = "320 kbps HD"
            )
            if (playbackState.value == PlaybackState.PLAYING) {
                playerManager.play(updatedStation)
            }
        }
    }

    fun changeAdminPassword(oldPwd: String, newPwd: String, confirmPwd: String): Boolean {
        if (!repository.verifyAdminPassword(oldPwd)) {
            _adminPasswordError.value = "La contraseña actual es incorrecta"
            return false
        }
        if (newPwd.length < 4) {
            _adminPasswordError.value = "La nueva contraseña debe tener al menos 4 caracteres"
            return false
        }
        if (newPwd != confirmPwd) {
            _adminPasswordError.value = "Las contraseñas no coinciden"
            return false
        }

        repository.updateAdminPassword(newPwd)
        _adminPasswordError.value = null
        _adminActionFeedback.value = "¡Contraseña actualizada con éxito!"
        return true
    }

    fun setChatEnabled(enabled: Boolean) {
        repository.setChatEnabled(enabled)
        _adminActionFeedback.value = if (enabled) "Chat activado para los oyentes" else "Chat pausado por moderación"
    }

    fun setPinnedAnnouncement(text: String, active: Boolean) {
        repository.setPinnedAnnouncement(text, active)
        _adminActionFeedback.value = "Anuncio de cabina actualizado"
    }

    fun deleteChatMessage(id: String) {
        repository.deleteChatMessage(id)
        _adminActionFeedback.value = "Mensaje eliminado"
    }

    fun clearAllChat() {
        repository.clearAllChat()
        _adminActionFeedback.value = "Historial del chat vaciado"
    }

    fun addProgram(title: String, host: String, timeSlot: String, days: String, description: String) {
        val newProg = RadioProgram(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            host = host.trim(),
            timeSlot = timeSlot.trim(),
            days = days.trim(),
            description = description.trim(),
            isLiveNow = false
        )
        repository.addProgram(newProg)
        _adminActionFeedback.value = "Programa añadido a la parrilla"
    }

    fun updateProgram(program: RadioProgram) {
        repository.updateProgram(program)
        _adminActionFeedback.value = "Programa actualizado"
    }

    fun deleteProgram(id: String) {
        repository.removeProgram(id)
        _adminActionFeedback.value = "Programa eliminado"
    }

    fun addCustomStation(name: String, genre: String, url: String) {
        val station = RadioStation(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            genre = genre.trim(),
            streamUrl = url.trim(),
            bitrate = "128 kbps",
            isCustom = true
        )
        repository.addStation(station)
        _adminActionFeedback.value = "Emisora añadida a la lista"
    }

    fun removeStation(id: String) {
        repository.removeStation(id)
        _adminActionFeedback.value = "Emisora eliminada"
    }

    fun testStreamUrl(streamUrl: String) {
        _isTestingStream.value = true
        _streamTestResult.value = "Verificando conexión con el servidor de streaming..."
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL(streamUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 7000
                    connection.readTimeout = 7000
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("User-Agent", "RadioOnlineApp/1.0")
                    connection.connect()

                    val code = connection.responseCode
                    val contentType = connection.contentType ?: ""
                    connection.disconnect()

                    if (code in 200..399) {
                        "Conexión exitosa (HTTP $code). Tipo de audio: ${contentType.ifEmpty { "stream de audio activo" }}"
                    } else {
                        "El servidor respondió con código HTTP $code"
                    }
                } catch (e: Exception) {
                    "Fallo de conexión: ${e.localizedMessage ?: "URL inaccesible o fuera de línea"}"
                }
            }
            _streamTestResult.value = result
            _isTestingStream.value = false
        }
    }

    fun resetToDefaults() {
        repository.resetToDefaults()
        playerManager.setVolume(config.value.defaultVolume)
        _adminActionFeedback.value = "Valores de fábrica restaurados"
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
