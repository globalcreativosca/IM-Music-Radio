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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioConfig
import com.example.model.RadioProgram
import com.example.viewmodel.RadioViewModel
import kotlin.math.roundToInt

@Composable
fun AdminDashboardScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val adminPasswordError by viewModel.adminPasswordError.collectAsState()
    val adminActionFeedback by viewModel.adminActionFeedback.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Clear feedback when leaving
    LaunchedEffect(Unit) {
        viewModel.clearAdminFeedback()
    }

    if (!isAdminAuthenticated) {
        // --- Admin Password Gate Screen ---
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0D0E17))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151728)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2E3152)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF2A85), Color(0xFF7C4DFF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Panel de Administración",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Configuración total de la emisora, streaming, anuncios, chat y seguridad.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Contraseña de Administrador", color = Color.Gray) },
                        placeholder = { Text("Ingrese su clave", color = Color.Gray) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = Color.LightGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF2C2F4D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    if (adminPasswordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = adminPasswordError ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🔑 Clave de fábrica predeterminada: admin123",
                        fontSize = 11.sp,
                        color = Color(0xFF00E5FF)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val success = viewModel.loginAdmin(passwordInput)
                            if (success) {
                                passwordInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Sesión de Administrador", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        // --- Authenticated Admin Dashboard Panel ---
        AdminDashboardContent(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@Composable
fun AdminDashboardContent(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val programs by viewModel.programs.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val adminActionFeedback by viewModel.adminActionFeedback.collectAsState()
    val streamTestResult by viewModel.streamTestResult.collectAsState()
    val isTestingStream by viewModel.isTestingStream.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Emisora", "Anuncio", "Chat & Moderación", "Programación", "Redes", "Seguridad")

    // Local form state initialized from config
    var stationName by remember(config) { mutableStateOf(config.stationName) }
    var slogan by remember(config) { mutableStateOf(config.slogan) }
    var genre by remember(config) { mutableStateOf(config.genre) }
    var frequency by remember(config) { mutableStateOf(config.frequency) }
    var primaryStreamUrl by remember(config) { mutableStateOf(config.primaryStreamUrl) }
    var secondaryStreamUrl by remember(config) { mutableStateOf(config.secondaryStreamUrl) }
    var logoUrl by remember(config) { mutableStateOf(config.logoUrl) }
    var defaultVolume by remember(config) { mutableStateOf(config.defaultVolume) }
    var autoPlayOnStart by remember(config) { mutableStateOf(config.autoPlayOnStart) }

    var announcementText by remember(config) { mutableStateOf(config.pinnedAnnouncement) }
    var isAnnouncementActive by remember(config) { mutableStateOf(config.isPinnedAnnouncementActive) }

    var chatEnabled by remember(config) { mutableStateOf(config.chatEnabled) }

    var whatsappNumber by remember(config) { mutableStateOf(config.whatsappNumber) }
    var instagramUrl by remember(config) { mutableStateOf(config.instagramUrl) }
    var facebookUrl by remember(config) { mutableStateOf(config.facebookUrl) }
    var websiteUrl by remember(config) { mutableStateOf(config.websiteUrl) }
    var phoneHotline by remember(config) { mutableStateOf(config.phoneHotline) }

    // Password change fields
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Add program fields
    var newProgTitle by remember { mutableStateOf("") }
    var newProgHost by remember { mutableStateOf("") }
    var newProgTime by remember { mutableStateOf("") }
    var newProgDays by remember { mutableStateOf("") }
    var newProgDesc by remember { mutableStateOf("") }

    // Dialogs
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E17))
    ) {
        // --- Header Bar ---
        Surface(
            color = Color(0xFF141524),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A85).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFFF2A85),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Panel de Control Admin",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Configuración activa",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00E676))
                        )
                    }
                }

                Button(
                    onClick = { viewModel.logoutAdmin() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF262842),
                        contentColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Salir",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Action Feedback Notification ---
        AnimatedVisibility(
            visible = adminActionFeedback != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = Color(0xFF1B3D2B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = adminActionFeedback ?: "",
                        color = Color(0xFFB9F6CA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearAdminFeedback() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Cerrar",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // --- Tabs Navigation ---
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF141524),
            contentColor = Color(0xFF00E5FF),
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF00E5FF),
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) Color(0xFF00E5FF) else Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // --- Tab Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: EMISORA Y STREAMING
                    Text(
                        text = "Configuración de la Emisora",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Ajusta la señal en vivo, nombres, dial y parámetros de audio.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AdminTextField(label = "Nombre de la Emisora", value = stationName, onValueChange = { stationName = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Eslogan / Lema", value = slogan, onValueChange = { slogan = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Género Musical", value = genre, onValueChange = { genre = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Frecuencia / Dial (Ej: 98.5 FM & Digital)", value = frequency, onValueChange = { frequency = it })

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF262842))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Servidor de Streaming (Audio en Vivo)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Soporta Icecast, Shoutcast, HLS o enlaces directos MP3/AAC.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    AdminTextField(
                        label = "URL Principal de Streaming",
                        value = primaryStreamUrl,
                        onValueChange = { primaryStreamUrl = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test stream URL button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.testStreamUrl(primaryStreamUrl) },
                            enabled = !isTestingStream,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF262845),
                                contentColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isTestingStream) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verificando...")
                            } else {
                                Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Probar Conexión de URL")
                            }
                        }
                    }

                    if (streamTestResult != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = streamTestResult ?: "",
                            fontSize = 12.sp,
                            color = if (streamTestResult?.contains("exitosa") == true) Color(0xFF00E676) else Color(0xFFFFB800)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(
                        label = "URL de Streaming Secundaria / Respaldo (Opcional)",
                        value = secondaryStreamUrl,
                        onValueChange = { secondaryStreamUrl = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(
                        label = "URL del Logo o Banner (Opcional)",
                        value = logoUrl,
                        onValueChange = { logoUrl = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto Play & Default Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Reproducir automáticamente al abrir la app", color = Color.White, fontSize = 13.sp)
                            Text("Inicia el stream tan pronto se abra el reproductor", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoPlayOnStart,
                            onCheckedChange = { autoPlayOnStart = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Volumen Inicial Predeterminado: ${(defaultVolume * 100).roundToInt()}%",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = defaultVolume,
                        onValueChange = { defaultVolume = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0xFF262842)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.saveRadioConfig(
                                config.copy(
                                    stationName = stationName,
                                    slogan = slogan,
                                    genre = genre,
                                    frequency = frequency,
                                    primaryStreamUrl = primaryStreamUrl,
                                    secondaryStreamUrl = secondaryStreamUrl,
                                    logoUrl = logoUrl,
                                    defaultVolume = defaultVolume,
                                    autoPlayOnStart = autoPlayOnStart
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_station_config_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Configuración de Emisora", fontWeight = FontWeight.Bold)
                    }
                }

                1 -> {
                    // TAB 1: ANUNCIO FIJADO DE CABINA
                    Text(
                        text = "Anuncio Fijado en Vivo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Muestra una cinta destacada en la parte superior del reproductor y del chat.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrar anuncio en pantalla",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isAnnouncementActive,
                            onCheckedChange = { isAnnouncementActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB800),
                                checkedTrackColor = Color(0xFFFFB800).copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        label = { Text("Texto del Anuncio", color = Color.Gray) },
                        placeholder = { Text("Ej: ¡Especial de rock a las 20:00! Participa por premios.", color = Color.Gray) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB800),
                            unfocusedBorderColor = Color(0xFF2D304E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.setPinnedAnnouncement(announcementText, isAnnouncementActive)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar Anuncio al Aire", fontWeight = FontWeight.Bold)
                    }
                }

                2 -> {
                    // TAB 2: CHAT Y MODERACIÓN
                    Text(
                        text = "Moderación y Control de Chat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Gestiona comentarios, elimina mensajes o pausa la sala de chat.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181A2D)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Chat de Oyentes Activo", fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    if (chatEnabled) "Los oyentes pueden enviar mensajes y pedidos" else "El chat está bloqueado temporalmente",
                                    color = if (chatEnabled) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = chatEnabled,
                                onCheckedChange = {
                                    chatEnabled = it
                                    viewModel.setChatEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00E5FF),
                                    checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear Chat Button
                    Button(
                        onClick = { showClearChatDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF381B22),
                            contentColor = Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vaciar Todo el Historial del Chat")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Mensajes Recientes (${chatMessages.size}):",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chatMessages.takeLast(10).reversed().forEach { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${msg.userName}:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(msg.userColor)
                                        )
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            maxLines = 2
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteChatMessage(msg.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Borrar",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: PROGRAMACIÓN Y PARRILLA
                    Text(
                        text = "Gestión de la Parrilla de Programas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Añade nuevos programas y configura la guía semanal de la radio.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF17192C)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Añadir Nuevo Programa", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.height(12.dp))

                            AdminTextField(label = "Título del Show", value = newProgTitle, onValueChange = { newProgTitle = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            AdminTextField(label = "Conductor / DJ", value = newProgHost, onValueChange = { newProgHost = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            AdminTextField(label = "Horario (Ej: 16:00 - 19:00)", value = newProgTime, onValueChange = { newProgTime = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            AdminTextField(label = "Días de Emisión (Ej: Lunes a Viernes)", value = newProgDays, onValueChange = { newProgDays = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            AdminTextField(label = "Descripción del Programa", value = newProgDesc, onValueChange = { newProgDesc = it })

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (newProgTitle.isNotBlank() && newProgTime.isNotBlank()) {
                                        viewModel.addProgram(
                                            title = newProgTitle,
                                            host = newProgHost.ifBlank { "DJ de Cabina" },
                                            timeSlot = newProgTime,
                                            days = newProgDays.ifBlank { "Todos los días" },
                                            description = newProgDesc.ifBlank { "Música en vivo y entretenimiento." }
                                        )
                                        newProgTitle = ""
                                        newProgHost = ""
                                        newProgTime = ""
                                        newProgDays = ""
                                        newProgDesc = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir Programa a la Parrilla", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Programas Actuales (${programs.size}):",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        programs.forEach { prog ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prog.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${prog.timeSlot} • ${prog.days}", color = Color(0xFF00E5FF), fontSize = 12.sp)
                                        Text("DJ: ${prog.host}", color = Color.LightGray, fontSize = 11.sp)
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteProgram(prog.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFFF5252)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: REDES SOCIALES Y CONTACTO
                    Text(
                        text = "Redes Sociales y Enlaces de Contacto",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Permite que los oyentes se conecten con la cabina por WhatsApp y redes.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AdminTextField(label = "Número de WhatsApp de Cabina (con código país)", value = whatsappNumber, onValueChange = { whatsappNumber = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Instagram (Ej: https://instagram.com/tu_radio)", value = instagramUrl, onValueChange = { instagramUrl = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Facebook (Ej: https://facebook.com/tu_radio)", value = facebookUrl, onValueChange = { facebookUrl = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Sitio Web Oficial", value = websiteUrl, onValueChange = { websiteUrl = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminTextField(label = "Teléfono de Cabina / Hotline", value = phoneHotline, onValueChange = { phoneHotline = it })

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.saveRadioConfig(
                                config.copy(
                                    whatsappNumber = whatsappNumber,
                                    instagramUrl = instagramUrl,
                                    facebookUrl = facebookUrl,
                                    websiteUrl = websiteUrl,
                                    phoneHotline = phoneHotline
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Enlaces y Redes Sociales", fontWeight = FontWeight.Bold)
                    }
                }

                5 -> {
                    // TAB 5: SEGURIDAD Y AJUSTES
                    Text(
                        text = "Seguridad y Restablecimiento",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Cambia la contraseña maestra de acceso o reinicia a los valores iniciales.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF17192C)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cambiar Contraseña de Administrador", fontWeight = FontWeight.Bold, color = Color(0xFFFF2A85))
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Contraseña Actual", color = Color.Gray) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF2A85),
                                    unfocusedBorderColor = Color(0xFF2E3150),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva Contraseña", color = Color.Gray) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF2A85),
                                    unfocusedBorderColor = Color(0xFF2E3150),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar Nueva Contraseña", color = Color.Gray) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF2A85),
                                    unfocusedBorderColor = Color(0xFF2E3150),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val ok = viewModel.changeAdminPassword(oldPassword, newPassword, confirmPassword)
                                    if (ok) {
                                        oldPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A85), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Actualizar Contraseña Maestra", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281318)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Restablecer Valores de Fábrica", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Restaura la emisora, programación y ajustes iniciales. La contraseña volverá a ser 'admin123'.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showResetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restablecer Todo a Valores de Fábrica", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Confirmation dialog for Reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = Color(0xFF191B2E),
            title = { Text("¿Restablecer configuración?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Se restaurarán todas las emisoras, contraseña y programas a los valores por defecto.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                ) {
                    Text("Sí, Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // Confirmation dialog for Clear Chat
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            containerColor = Color(0xFF191B2E),
            title = { Text("¿Vaciar todo el chat?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Se borrarán todos los comentarios y pedidos musicales de los oyentes.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChat()
                        showClearChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                ) {
                    Text("Sí, Vaciar Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray, fontSize = 13.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0xFF2C2F4D),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
