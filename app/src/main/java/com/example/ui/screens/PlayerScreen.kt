package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.PlaybackState
import com.example.model.RadioStation
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.SleepTimerSheet
import com.example.ui.components.VisualizerView
import com.example.ui.components.VolumeControlSheet
import com.example.viewmodel.RadioNavScreen
import com.example.viewmodel.RadioViewModel
import kotlin.math.roundToInt

@Composable
fun PlayerScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val equalizerPreset by viewModel.equalizerPreset.collectAsState()
    val sleepTimerSeconds by viewModel.sleepTimerSeconds.collectAsState()
    val visualizerBars by viewModel.visualizerBars.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showVolumeSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

    val isPlaying = playbackState == PlaybackState.PLAYING
    val isBuffering = playbackState == PlaybackState.BUFFERING

    // Pulsing live indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E17))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar: Station Info & Live Badge ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = config.stationName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = config.frequency,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Live Indicator Badge
            Surface(
                color = if (isPlaying) Color(0xFFFF1744).copy(alpha = 0.2f) else Color(0xFF23253B),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPlaying) Color(0xFFFF1744) else Color(0xFF383B58)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(if (isPlaying) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFFFF1744) else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "EN VIVO" else if (isBuffering) "CONECTANDO" else "EN ESPERA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color(0xFFFF5252) else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Pinned Announcement Banner (if enabled) ---
        AnimatedVisibility(
            visible = config.isPinnedAnnouncementActive && config.pinnedAnnouncement.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261E14)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB800).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Anuncio",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = config.pinnedAnnouncement,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFFE082),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- Hero Artwork Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF2F3252), RoundedCornerShape(20.dp))
        ) {
            if (config.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = config.logoUrl,
                    contentDescription = "Logo de la emisora",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.banner_radio_live),
                    contentDescription = "Cabina de radio en vivo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark gradient overlay on bottom for text clarity
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC0D0E17)),
                            startY = 180f
                        )
                    )
            )

            // Station dial badge in hero corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentStation?.bitrate ?: "128 kbps HD",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Station slogan in hero bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = currentStation?.name ?: config.stationName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = config.slogan,
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Visualizer Spectrum Bars ---
        VisualizerView(
            amplitudes = visualizerBars,
            isPlaying = isPlaying,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Integrated Volume Control Bar ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161828)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF262842), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.testTag("player_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted || volume < 0.02f) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                        contentDescription = "Silenciar / Activar sonido",
                        tint = if (isMuted) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = { viewModel.setVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF2D304E)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("player_volume_slider")
                )

                Text(
                    text = if (isMuted) "0%" else "${(volume * 100).roundToInt()}%",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 2.dp)
                        .clickable { showVolumeSheet = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Error Banner if Stream Failed ---
        if (errorMessage != null && playbackState == PlaybackState.ERROR) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1215)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errorMessage ?: "Error al reproducir stream",
                        color = Color(0xFFFFCDD2),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.togglePlayPause() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reintentar",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // --- Main Player Controls Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous preset
            IconButton(
                onClick = {
                    val currentIndex = stations.indexOfFirst { it.id == currentStation?.id }
                    if (currentIndex > 0) {
                        viewModel.playStation(stations[currentIndex - 1])
                    } else if (stations.isNotEmpty()) {
                        viewModel.playStation(stations.last())
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C2C))
                    .testTag("prev_station_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Emisora anterior",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Circular Play/Pause/Buffer Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isPlaying) 1.02f else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF2A85),
                                Color(0xFF7C4DFF)
                            )
                        )
                    )
                    .clickable { viewModel.togglePlayPause() }
                    .testTag("main_play_button")
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Stop Button
            IconButton(
                onClick = { viewModel.playerManager.stop() },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C2C))
                    .testTag("stop_player_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Detener transmisión",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Next preset
            IconButton(
                onClick = {
                    val currentIndex = stations.indexOfFirst { it.id == currentStation?.id }
                    if (currentIndex >= 0 && currentIndex < stations.size - 1) {
                        viewModel.playStation(stations[currentIndex + 1])
                    } else if (stations.isNotEmpty()) {
                        viewModel.playStation(stations.first())
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C2C))
                    .testTag("next_station_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente emisora",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Quick Utility Buttons: Equalizer, Sleep Timer, Share, WhatsApp Hotline ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Equalizer button
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161829)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showEqualizerSheet = true }
                    .border(1.dp, Color(0xFF262945), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Ecualizador",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = equalizerPreset.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Sleep Timer button
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (sleepTimerSeconds > 0) Color(0xFF2B1C4B) else Color(0xFF161829)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showSleepTimerSheet = true }
                    .border(
                        1.dp,
                        if (sleepTimerSeconds > 0) Color(0xFF7C4DFF) else Color(0xFF262945),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Temporizador",
                        tint = if (sleepTimerSeconds > 0) Color(0xFFFFB800) else Color(0xFF7C4DFF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (sleepTimerSeconds > 0) "%d min".format(sleepTimerSeconds / 60) else "Timer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            // Share Stream button
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161829)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "¡Estoy escuchando ${config.stationName} (${config.frequency}) en vivo! Sintoniza tú también: ${config.primaryStreamUrl}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir Radio en Vivo"))
                    }
                    .border(1.dp, Color(0xFF262945), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compartir",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Station Presets Carousel ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emisoras y Canales",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "${stations.size} disponibles",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stations) { st ->
                val isSelected = st.id == currentStation?.id
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.playStation(st) }
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF25273F),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("station_card_${st.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E213D) else Color(0xFF151624)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF2B2E4A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = st.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.White
                            )
                            Text(
                                text = st.genre,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Direct Contact with Studio / DJ Cabina ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121422)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23253B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Contacto Directo con Cabina",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Envía notas de voz y saludos en vivo al DJ",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // WhatsApp button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3828)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val cleanNum = config.whatsappNumber.replace(Regex("[^0-9]"), "")
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=¡Hola! Los estoy escuchando en vivo en la radio 📻🎶")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp DJ",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Go to Live Chat button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D163D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(RadioNavScreen.CHAT) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Chat",
                                tint = Color(0xFFFF2A85),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Entrar al Chat",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Bottom Sheets
    if (showVolumeSheet) {
        VolumeControlSheet(
            volume = volume,
            isMuted = isMuted,
            onVolumeChange = { viewModel.setVolume(it) },
            onToggleMute = { viewModel.toggleMute() },
            onDismiss = { showVolumeSheet = false }
        )
    }

    if (showEqualizerSheet) {
        EqualizerSheet(
            currentPreset = equalizerPreset,
            onSelectPreset = {
                viewModel.setEqualizerPreset(it)
                showEqualizerSheet = false
            },
            onDismiss = { showEqualizerSheet = false }
        )
    }

    if (showSleepTimerSheet) {
        SleepTimerSheet(
            remainingSeconds = sleepTimerSeconds,
            onSetTimer = { viewModel.setSleepTimer(it) },
            onDismiss = { showSleepTimerSheet = false }
        )
    }
}
