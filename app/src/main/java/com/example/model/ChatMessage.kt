package com.example.model

data class ChatMessage(
    val id: String,
    val userName: String,
    val userColor: Long,
    val text: String,
    val timestamp: Long,
    val isDjAdmin: Boolean = false,
    val isSongRequest: Boolean = false,
    val songTitle: String? = null,
    val fireCount: Int = 0,
    val heartCount: Int = 0,
    val musicCount: Int = 0
)
