package com.example.model

enum class EqualizerPreset(
    val displayName: String,
    val description: String,
    val bassFactor: Float,
    val trebleFactor: Float
) {
    NORMAL("Normal", "Sonido balanceado estándar", 1.0f, 1.0f),
    BASS_BOOST("Mega Bass", "Refuerzo potente de graves", 1.4f, 0.9f),
    POP("Pop & Hits", "Graves y agudos realzados", 1.2f, 1.2f),
    ROCK("Rock & Metal", "Energía y presencia de guitarras", 1.3f, 1.3f),
    VOCAL("Voz / DJ Host", "Claridad para locución y podcast", 0.9f, 1.2f),
    ELECTRONIC("Dance & Electro", "Bajos profundos y agudos nítidos", 1.5f, 1.3f),
    ACOUSTIC("Acústico / Chill", "Tonos cálidos y suaves", 1.1f, 1.1f)
}
