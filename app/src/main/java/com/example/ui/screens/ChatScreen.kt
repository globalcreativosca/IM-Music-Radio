package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.viewmodel.RadioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatarColor by viewModel.userAvatarColor.collectAsState()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var isSongRequestMode by remember { mutableStateOf(false) }
    var songTitleText by remember { mutableStateOf("") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E17))
    ) {
        // --- Top Bar: Pinned Announcement & User Profile ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141524))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile pill
                Surface(
                    color = Color(0xFF1E2038),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { showEditProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(userAvatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAdminAuthenticated) "Cabina [DJ Admin]" else userName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nombre",
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (isAdminAuthenticated) {
                    Surface(
                        color = Color(0xFFFF2A85).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2A85))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Modo Admin",
                                tint = Color(0xFFFF2A85),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Modo Cabina Activo",
                                color = Color(0xFFFF2A85),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${chatMessages.size} comentarios",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Pinned announcement banner
            if (config.isPinnedAnnouncementActive && config.pinnedAnnouncement.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261D12)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB800).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = config.pinnedAnnouncement,
                            color = Color(0xFFFFE082),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- Chat Messages List ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (!config.chatEnabled && !isAdminAuthenticated) {
                // Paused Chat State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "El chat está pausado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "La cabina de transmisión ha detenido temporalmente los comentarios.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            } else if (chatMessages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Sé el primero en escribir!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Envía saludos en vivo o pide tu canción favorita.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            message = msg,
                            isAdminAuthenticated = isAdminAuthenticated,
                            onReact = { type -> viewModel.reactToChat(msg.id, type) },
                            onDelete = { viewModel.deleteChatMessage(msg.id) }
                        )
                    }
                }
            }
        }

        // --- Bottom Input & Quick Reactions ---
        if (config.chatEnabled || isAdminAuthenticated) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141524))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Song request banner if active
                AnimatedVisibility(
                    visible = isSongRequestMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E1A38))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFFF2A85),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Modo Pedido Musical Activado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF80AB)
                            )
                        }

                        IconButton(
                            onClick = { isSongRequestMode = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar pedido",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isSongRequestMode) {
                    OutlinedTextField(
                        value = songTitleText,
                        onValueChange = { songTitleText = it },
                        placeholder = { Text("Ej: Coldplay - Viva La Vida", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("song_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2A85),
                            unfocusedBorderColor = Color(0xFF38234D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("🔥", "❤️", "🎵", "👏", "📻", "🕺").forEach { emoji ->
                        Surface(
                            color = Color(0xFF1E2138),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .clickable {
                                    viewModel.sendChatMessage(emoji)
                                }
                        ) {
                            Text(
                                text = emoji,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Song request toggle button
                    Surface(
                        color = if (isSongRequestMode) Color(0xFFFF2A85) else Color(0xFF262842),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable { isSongRequestMode = !isSongRequestMode }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Pedir canción",
                                tint = if (isSongRequestMode) Color.Black else Color(0xFFFF2A85),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pedir tema",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSongRequestMode) Color.Black else Color.White
                            )
                        }
                    }
                }

                // Text Input & Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                if (isSongRequestMode) "Mensaje o dedicatoria..." else "Comenta en vivo...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF2B2E4A),
                            focusedContainerColor = Color(0xFF191B2E),
                            unfocusedContainerColor = Color(0xFF191B2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (isSongRequestMode) {
                                if (songTitleText.isNotBlank()) {
                                    viewModel.sendChatMessage(
                                        text = messageText.ifBlank { "¡Saludos a cabina! Me encantaría escuchar este tema 🎵" },
                                        isSongRequest = true,
                                        songTitle = songTitleText
                                    )
                                    messageText = ""
                                    songTitleText = ""
                                    isSongRequestMode = false
                                }
                            } else {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendChatMessage(messageText)
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSongRequestMode) Color(0xFFFF2A85) else Color(0xFF00E5FF)
                            )
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar mensaje",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // --- Profile Editing Dialog ---
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempColor by remember { mutableStateOf(userAvatarColor) }
        val colorOptions = listOf(
            0xFF00E5FF, 0xFFFF2A85, 0xFF00E676, 0xFFFFB800, 0xFF7C4DFF, 0xFFFF5252
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = Color(0xFF191B2E),
            title = {
                Text(
                    text = "Personalizar Perfil",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tu apodo en la transmisión:",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF383C66),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Color de tu avatar:",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorOptions.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(
                                        width = if (tempColor == c) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { tempColor = c }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateListenerProfile(tempName, tempColor)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isAdminAuthenticated: Boolean,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                message.isDjAdmin -> Color(0xFF2C132E)
                message.isSongRequest -> Color(0xFF1E1733)
                else -> Color(0xFF161829)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = when {
            message.isDjAdmin -> androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFF2A85))
            message.isSongRequest -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.7f))
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242742))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Sender, Badge, Timestamp, Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(message.userColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isDjAdmin) Icons.Default.Campaign else Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (message.isDjAdmin) Color(0xFFFF2A85) else Color.White
                    )

                    if (message.isDjAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFF2A85).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CABINA",
                                color = Color(0xFFFF4081),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (isAdminAuthenticated) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar mensaje",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Song Request Box
            if (message.isSongRequest && message.songTitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF331F4D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Canción pedida:",
                                fontSize = 10.sp,
                                color = Color(0xFF80D8FF)
                            )
                            Text(
                                text = message.songTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Message Body
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = if (message.isDjAdmin) Color(0xFFFFE0E9) else Color(0xFFE2E4EC)
            )

            // Reactions Row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionChip(
                    emoji = "🔥",
                    count = message.fireCount,
                    onClick = { onReact("fire") }
                )
                ReactionChip(
                    emoji = "❤️",
                    count = message.heartCount,
                    onClick = { onReact("heart") }
                )
                ReactionChip(
                    emoji = "🎵",
                    count = message.musicCount,
                    onClick = { onReact("music") }
                )
            }
        }
    }
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF22253D),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 12.sp)
            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
