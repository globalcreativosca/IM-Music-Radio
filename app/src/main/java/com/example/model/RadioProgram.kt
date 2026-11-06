package com.example.model

data class RadioProgram(
    val id: String,
    val title: String,
    val host: String,
    val timeSlot: String,
    val days: String,
    val description: String,
    val isLiveNow: Boolean = false
)
