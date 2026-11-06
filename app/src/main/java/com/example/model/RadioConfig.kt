package com.example.model

data class RadioConfig(
    val stationName: String = "Radio Mix Digital",
    val slogan: String = "¡Tu música en vivo las 24 horas!",
    val genre: String = "Pop, Urbano & Top Hits",
    val frequency: String = "98.5 FM & Digital",
    val primaryStreamUrl: String = "https://streams.ilovemusic.de/iloveradio1.mp3",
    val secondaryStreamUrl: String = "https://ice2.somafm.com/groovesalad-128-mp3",
    val logoUrl: String = "",
    val whatsappNumber: String = "+1234567890",
    val instagramUrl: String = "https://instagram.com",
    val facebookUrl: String = "https://facebook.com",
    val websiteUrl: String = "https://radiomix.live",
    val phoneHotline: String = "+1 800-555-0199",
    val pinnedAnnouncement: String = "🔥 ¡Bienvenidos al show en vivo! Escribe en el chat para interactuar con nosotros.",
    val isPinnedAnnouncementActive: Boolean = true,
    val adminPassword: String = "admin123",
    val chatEnabled: Boolean = true,
    val autoPlayOnStart: Boolean = false,
    val defaultVolume: Float = 0.85f
)
