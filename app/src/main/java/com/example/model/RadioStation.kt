package com.example.model

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val streamUrl: String,
    val bitrate: String = "128 kbps HD",
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)
