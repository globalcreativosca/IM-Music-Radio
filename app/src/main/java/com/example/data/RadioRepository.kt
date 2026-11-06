package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ChatMessage
import com.example.model.RadioConfig
import com.example.model.RadioProgram
import com.example.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RadioRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("radio_prefs_data", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<RadioConfig> = _config.asStateFlow()

    private val _stations = MutableStateFlow(loadStations())
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _programs = MutableStateFlow(loadPrograms())
    val programs: StateFlow<List<RadioProgram>> = _programs.asStateFlow()

    private val _chatMessages = MutableStateFlow(loadChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("listener_name", "Oyente_${(100..999).random()}") ?: "Oyente")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatarColor = MutableStateFlow(prefs.getLong("listener_color", 0xFF00E5FF))
    val userAvatarColor: StateFlow<Long> = _userAvatarColor.asStateFlow()

    // --- Config Management ---
    private fun loadConfig(): RadioConfig {
        val name = prefs.getString("cfg_name", "Radio Mix Digital") ?: "Radio Mix Digital"
        val slogan = prefs.getString("cfg_slogan", "¡Tu música en vivo las 24 horas!") ?: "¡Tu música en vivo las 24 horas!"
        val genre = prefs.getString("cfg_genre", "Pop, Urbano & Top Hits") ?: "Pop, Urbano & Top Hits"
        val frequency = prefs.getString("cfg_frequency", "98.5 FM & Digital") ?: "98.5 FM & Digital"
        val primaryStream = prefs.getString("cfg_primary_stream", "https://streams.ilovemusic.de/iloveradio1.mp3")
            ?: "https://streams.ilovemusic.de/iloveradio1.mp3"
        val secondaryStream = prefs.getString("cfg_secondary_stream", "https://ice2.somafm.com/groovesalad-128-mp3")
            ?: "https://ice2.somafm.com/groovesalad-128-mp3"
        val logoUrl = prefs.getString("cfg_logo_url", "") ?: ""
        val whatsapp = prefs.getString("cfg_whatsapp", "+1234567890") ?: "+1234567890"
        val instagram = prefs.getString("cfg_instagram", "https://instagram.com") ?: "https://instagram.com"
        val facebook = prefs.getString("cfg_facebook", "https://facebook.com") ?: "https://facebook.com"
        val website = prefs.getString("cfg_website", "https://radiomix.live") ?: "https://radiomix.live"
        val phone = prefs.getString("cfg_phone", "+1 800-555-0199") ?: "+1 800-555-0199"
        val announcement = prefs.getString("cfg_announcement", "🔥 ¡Bienvenidos al show en vivo! Escribe en el chat para interactuar con nosotros.")
            ?: "🔥 ¡Bienvenidos al show en vivo! Escribe en el chat para interactuar con nosotros."
        val announcementActive = prefs.getBoolean("cfg_announcement_active", true)
        val adminPassword = prefs.getString("cfg_admin_pwd", "admin123") ?: "admin123"
        val chatEnabled = prefs.getBoolean("cfg_chat_enabled", true)
        val autoPlay = prefs.getBoolean("cfg_autoplay", false)
        val defaultVol = prefs.getFloat("cfg_default_vol", 0.85f)

        return RadioConfig(
            stationName = name,
            slogan = slogan,
            genre = genre,
            frequency = frequency,
            primaryStreamUrl = primaryStream,
            secondaryStreamUrl = secondaryStream,
            logoUrl = logoUrl,
            whatsappNumber = whatsapp,
            instagramUrl = instagram,
            facebookUrl = facebook,
            websiteUrl = website,
            phoneHotline = phone,
            pinnedAnnouncement = announcement,
            isPinnedAnnouncementActive = announcementActive,
            adminPassword = adminPassword,
            chatEnabled = chatEnabled,
            autoPlayOnStart = autoPlay,
            defaultVolume = defaultVol
        )
    }

    fun updateConfig(newConfig: RadioConfig) {
        prefs.edit().apply {
            putString("cfg_name", newConfig.stationName)
            putString("cfg_slogan", newConfig.slogan)
            putString("cfg_genre", newConfig.genre)
            putString("cfg_frequency", newConfig.frequency)
            putString("cfg_primary_stream", newConfig.primaryStreamUrl)
            putString("cfg_secondary_stream", newConfig.secondaryStreamUrl)
            putString("cfg_logo_url", newConfig.logoUrl)
            putString("cfg_whatsapp", newConfig.whatsappNumber)
            putString("cfg_instagram", newConfig.instagramUrl)
            putString("cfg_facebook", newConfig.facebookUrl)
            putString("cfg_website", newConfig.websiteUrl)
            putString("cfg_phone", newConfig.phoneHotline)
            putString("cfg_announcement", newConfig.pinnedAnnouncement)
            putBoolean("cfg_announcement_active", newConfig.isPinnedAnnouncementActive)
            putString("cfg_admin_pwd", newConfig.adminPassword)
            putBoolean("cfg_chat_enabled", newConfig.chatEnabled)
            putBoolean("cfg_autoplay", newConfig.autoPlayOnStart)
            putFloat("cfg_default_vol", newConfig.defaultVolume)
            apply()
        }
        _config.value = newConfig

        // Also update the primary station item in stations list
        val current = _stations.value.toMutableList()
        val index = current.indexOfFirst { it.id == "primary_admin_station" }
        if (index != -1) {
            current[index] = current[index].copy(
                name = newConfig.stationName,
                genre = newConfig.genre,
                streamUrl = newConfig.primaryStreamUrl
            )
            _stations.value = current
            saveStations(current)
        }
    }

    fun verifyAdminPassword(password: String): Boolean {
        return password.trim() == _config.value.adminPassword.trim()
    }

    fun updateAdminPassword(newPassword: String): Boolean {
        if (newPassword.isBlank()) return false
        val updated = _config.value.copy(adminPassword = newPassword)
        updateConfig(updated)
        return true
    }

    fun setChatEnabled(enabled: Boolean) {
        updateConfig(_config.value.copy(chatEnabled = enabled))
    }

    fun setPinnedAnnouncement(text: String, active: Boolean) {
        updateConfig(_config.value.copy(pinnedAnnouncement = text, isPinnedAnnouncementActive = active))
    }

    // --- Stations Management ---
    private fun loadStations(): List<RadioStation> {
        val json = prefs.getString("stations_json", null)
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<RadioStation>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RadioStation(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            genre = obj.getString("genre"),
                            streamUrl = obj.getString("streamUrl"),
                            bitrate = obj.optString("bitrate", "128 kbps HD"),
                            isCustom = obj.optBoolean("isCustom", false),
                            isFavorite = obj.optBoolean("isFavorite", false)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        // Default stations
        return listOf(
            RadioStation(
                id = "primary_admin_station",
                name = "Radio Mix Digital (Principal)",
                genre = "Pop, Urbano & Hits",
                streamUrl = "https://streams.ilovemusic.de/iloveradio1.mp3",
                bitrate = "320 kbps HD",
                isCustom = false,
                isFavorite = true
            ),
            RadioStation(
                id = "station_lofi",
                name = "Chillout & Lofi Beats",
                genre = "Lofi / Relax",
                streamUrl = "https://stream.laut.fm/lofi",
                bitrate = "128 kbps",
                isCustom = false,
                isFavorite = true
            ),
            RadioStation(
                id = "station_groove",
                name = "Groove Salad Lounge",
                genre = "Ambient & Downtempo",
                streamUrl = "https://ice2.somafm.com/groovesalad-128-mp3",
                bitrate = "128 kbps",
                isCustom = false,
                isFavorite = false
            ),
            RadioStation(
                id = "station_rock",
                name = "Classic Rock Radio",
                genre = "Rock / Clásicos",
                streamUrl = "https://streams.ilovemusic.de/iloveradio2.mp3",
                bitrate = "192 kbps",
                isCustom = false,
                isFavorite = false
            ),
            RadioStation(
                id = "station_electro",
                name = "DefCon Electronic",
                genre = "EDM / Synthwave",
                streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
                bitrate = "128 kbps",
                isCustom = false,
                isFavorite = false
            )
        )
    }

    private fun saveStations(list: List<RadioStation>) {
        try {
            val array = JSONArray()
            list.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("genre", it.genre)
                obj.put("streamUrl", it.streamUrl)
                obj.put("bitrate", it.bitrate)
                obj.put("isCustom", it.isCustom)
                obj.put("isFavorite", it.isFavorite)
                array.put(obj)
            }
            prefs.edit().putString("stations_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addStation(station: RadioStation) {
        val updated = _stations.value + station
        _stations.value = updated
        saveStations(updated)
    }

    fun removeStation(id: String) {
        if (id == "primary_admin_station") return // protected
        val updated = _stations.value.filter { it.id != id }
        _stations.value = updated
        saveStations(updated)
    }

    fun toggleFavoriteStation(id: String) {
        val updated = _stations.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _stations.value = updated
        saveStations(updated)
    }

    // --- Programs / Schedule Management ---
    private fun loadPrograms(): List<RadioProgram> {
        val json = prefs.getString("programs_json", null)
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<RadioProgram>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RadioProgram(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            host = obj.getString("host"),
                            timeSlot = obj.getString("timeSlot"),
                            days = obj.getString("days"),
                            description = obj.getString("description"),
                            isLiveNow = obj.optBoolean("isLiveNow", false)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        return listOf(
            RadioProgram(
                id = "prog_1",
                title = "El Despertador Mix",
                host = "DJ Alex & Laura",
                timeSlot = "07:00 - 11:00",
                days = "Lunes a Viernes",
                description = "El mejor inicio de día con noticias de música, interacción en vivo y la selección más energética.",
                isLiveNow = true
            ),
            RadioProgram(
                id = "prog_2",
                title = "Top 20 Hit Parade",
                host = "Marcos Rivera",
                timeSlot = "11:00 - 14:00",
                days = "Lunes a Sábado",
                description = "Las canciones más votadas en el ranking global con estrenos exclusivos.",
                isLiveNow = false
            ),
            RadioProgram(
                id = "prog_3",
                title = "La Tarde en Conexión",
                host = "Sofía Méndez",
                timeSlot = "14:00 - 18:00",
                days = "Lunes a Viernes",
                description = "Música para acompañar tu jornada laboral y complacencias pedidas por chat.",
                isLiveNow = false
            ),
            RadioProgram(
                id = "prog_4",
                title = "Zona Urbana & Club Session",
                host = "DJ Fader",
                timeSlot = "18:00 - 22:00",
                days = "Viernes y Sábados",
                description = "Mezclas en vivo, trap, reggaeton, pop latino y remixes de club.",
                isLiveNow = false
            ),
            RadioProgram(
                id = "prog_5",
                title = "Noche Acústica & Chill",
                host = "Cabina Automática",
                timeSlot = "22:00 - 07:00",
                days = "Todos los días",
                description = "Música relajante sin pausas publicitarias para descansar y estudiar.",
                isLiveNow = false
            )
        )
    }

    private fun savePrograms(list: List<RadioProgram>) {
        try {
            val array = JSONArray()
            list.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("title", it.title)
                obj.put("host", it.host)
                obj.put("timeSlot", it.timeSlot)
                obj.put("days", it.days)
                obj.put("description", it.description)
                obj.put("isLiveNow", it.isLiveNow)
                array.put(obj)
            }
            prefs.edit().putString("programs_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addProgram(program: RadioProgram) {
        val updated = _programs.value + program
        _programs.value = updated
        savePrograms(updated)
    }

    fun updateProgram(program: RadioProgram) {
        val updated = _programs.value.map { if (it.id == program.id) program else it }
        _programs.value = updated
        savePrograms(updated)
    }

    fun removeProgram(id: String) {
        val updated = _programs.value.filter { it.id != id }
        _programs.value = updated
        savePrograms(updated)
    }

    // --- Chat Management ---
    private fun loadChatMessages(): List<ChatMessage> {
        val json = prefs.getString("chat_messages_json", null)
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<ChatMessage>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ChatMessage(
                            id = obj.getString("id"),
                            userName = obj.getString("userName"),
                            userColor = obj.getLong("userColor"),
                            text = obj.getString("text"),
                            timestamp = obj.getLong("timestamp"),
                            isDjAdmin = obj.optBoolean("isDjAdmin", false),
                            isSongRequest = obj.optBoolean("isSongRequest", false),
                            songTitle = if (obj.has("songTitle")) obj.getString("songTitle") else null,
                            fireCount = obj.optInt("fireCount", 0),
                            heartCount = obj.optInt("heartCount", 0),
                            musicCount = obj.optInt("musicCount", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        val now = System.currentTimeMillis()
        return listOf(
            ChatMessage(
                id = "msg_welcome",
                userName = "Radio Mix Oficial [DJ]",
                userColor = 0xFFFF2A85,
                text = "¡Bienvenidos a la transmisión en vivo! Escriban desde dónde nos sintonizan 📻✨",
                timestamp = now - 1200000,
                isDjAdmin = true,
                fireCount = 14,
                heartCount = 22,
                musicCount = 18
            ),
            ChatMessage(
                id = "msg_1",
                userName = "Carlos M.",
                userColor = 0xFF00E5FF,
                text = "¡Saludos desde Madrid! Sonando con excelente fidelidad sonora 🔥🎶",
                timestamp = now - 900000,
                fireCount = 5,
                heartCount = 3
            ),
            ChatMessage(
                id = "msg_2",
                userName = "Mariana G.",
                userColor = 0xFF00E676,
                text = "¡Arriba la buena música! Los escucho todos los días en el trabajo.",
                timestamp = now - 450000,
                heartCount = 8
            ),
            ChatMessage(
                id = "msg_3",
                userName = "Diego R.",
                userColor = 0xFFFFB800,
                text = "¿Pueden poner el nuevo tema de Dua Lipa?",
                timestamp = now - 150000,
                isSongRequest = true,
                songTitle = "Dua Lipa - Training Season",
                musicCount = 7
            )
        )
    }

    private fun saveChatMessages(list: List<ChatMessage>) {
        try {
            val array = JSONArray()
            list.takeLast(100).forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("userName", it.userName)
                obj.put("userColor", it.userColor)
                obj.put("text", it.text)
                obj.put("timestamp", it.timestamp)
                obj.put("isDjAdmin", it.isDjAdmin)
                obj.put("isSongRequest", it.isSongRequest)
                if (it.songTitle != null) obj.put("songTitle", it.songTitle)
                obj.put("fireCount", it.fireCount)
                obj.put("heartCount", it.heartCount)
                obj.put("musicCount", it.musicCount)
                array.put(obj)
            }
            prefs.edit().putString("chat_messages_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun sendChatMessage(
        text: String,
        isSongRequest: Boolean = false,
        songTitle: String? = null,
        isDjAdmin: Boolean = false
    ) {
        val sender = if (isDjAdmin) "Cabina [DJ Admin]" else _userName.value
        val color = if (isDjAdmin) 0xFFFF2A85 else _userAvatarColor.value
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            userName = sender,
            userColor = color,
            text = text,
            timestamp = System.currentTimeMillis(),
            isDjAdmin = isDjAdmin,
            isSongRequest = isSongRequest,
            songTitle = songTitle
        )
        val updated = _chatMessages.value + newMsg
        _chatMessages.value = updated
        saveChatMessages(updated)
    }

    fun deleteChatMessage(id: String) {
        val updated = _chatMessages.value.filter { it.id != id }
        _chatMessages.value = updated
        saveChatMessages(updated)
    }

    fun clearAllChat() {
        val welcome = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                userName = "Cabina [DJ Admin]",
                userColor = 0xFFFF2A85,
                text = "El chat ha sido reiniciado por el moderador. ¡Comienza una nueva sesión en vivo!",
                timestamp = System.currentTimeMillis(),
                isDjAdmin = true
            )
        )
        _chatMessages.value = welcome
        saveChatMessages(welcome)
    }

    fun reactToMessage(id: String, reactionType: String) {
        val updated = _chatMessages.value.map { msg ->
            if (msg.id == id) {
                when (reactionType) {
                    "fire" -> msg.copy(fireCount = msg.fireCount + 1)
                    "heart" -> msg.copy(heartCount = msg.heartCount + 1)
                    "music" -> msg.copy(musicCount = msg.musicCount + 1)
                    else -> msg
                }
            } else msg
        }
        _chatMessages.value = updated
        saveChatMessages(updated)
    }

    fun updateUserName(name: String) {
        val clean = name.trim().ifEmpty { "Oyente" }
        prefs.edit().putString("listener_name", clean).apply()
        _userName.value = clean
    }

    fun updateUserColor(color: Long) {
        prefs.edit().putLong("listener_color", color).apply()
        _userAvatarColor.value = color
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _config.value = loadConfig()
        _stations.value = loadStations()
        _programs.value = loadPrograms()
        _chatMessages.value = loadChatMessages()
    }
}
