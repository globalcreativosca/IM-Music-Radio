package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ScheduleScreen
import com.example.viewmodel.RadioNavScreen
import com.example.viewmodel.RadioViewModel

@Composable
fun RadioMainScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val config by viewModel.config.collectAsState()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()

    // Handle back button to return to Player screen from secondary tabs
    if (currentScreen != RadioNavScreen.PLAYER) {
        BackHandler {
            viewModel.navigateTo(RadioNavScreen.PLAYER)
        }
    }

    val isPlaying = playbackState == PlaybackState.PLAYING
    val isBuffering = playbackState == PlaybackState.BUFFERING

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E17)),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111322))
                    .navigationBarsPadding()
            ) {
                // --- Mini Floating Player when browsing other screens ---
                AnimatedVisibility(
                    visible = currentScreen != RadioNavScreen.PLAYER && (isPlaying || isBuffering || playbackState == PlaybackState.PAUSED),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        color = Color(0xFF1E2038),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF32365A), RoundedCornerShape(14.dp))
                            .clickable { viewModel.navigateTo(RadioNavScreen.PLAYER) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) Color(0xFFFF2A85) else Color(0xFF2E3152)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = currentStation?.name ?: config.stationName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isPlaying) "Transmitiendo en vivo" else if (isBuffering) "Cargando audio..." else "En pausa",
                                        color = if (isPlaying) Color(0xFF00E5FF) else Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                ) {
                                    if (isBuffering) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- Navigation Bar ---
                NavigationBar(
                    containerColor = Color(0xFF111322),
                    contentColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == RadioNavScreen.PLAYER,
                        onClick = { viewModel.navigateTo(RadioNavScreen.PLAYER) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == RadioNavScreen.PLAYER) Icons.Filled.Radio else Icons.Outlined.Radio,
                                contentDescription = "En Vivo"
                            )
                        },
                        label = { Text("En Vivo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_player")
                    )

                    NavigationBarItem(
                        selected = currentScreen == RadioNavScreen.CHAT,
                        onClick = { viewModel.navigateTo(RadioNavScreen.CHAT) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == RadioNavScreen.CHAT) Icons.Filled.Chat else Icons.Outlined.Chat,
                                contentDescription = "Chat"
                            )
                        },
                        label = { Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF2A85),
                            selectedTextColor = Color(0xFFFF2A85),
                            indicatorColor = Color(0xFFFF2A85).copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    NavigationBarItem(
                        selected = currentScreen == RadioNavScreen.SCHEDULE,
                        onClick = { viewModel.navigateTo(RadioNavScreen.SCHEDULE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == RadioNavScreen.SCHEDULE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Programas"
                            )
                        },
                        label = { Text("Programas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFB800),
                            selectedTextColor = Color(0xFFFFB800),
                            indicatorColor = Color(0xFFFFB800).copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_schedule")
                    )

                    NavigationBarItem(
                        selected = currentScreen == RadioNavScreen.ADMIN,
                        onClick = { viewModel.navigateTo(RadioNavScreen.ADMIN) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == RadioNavScreen.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Admin"
                            )
                        },
                        label = {
                            Text(
                                if (isAdminAuthenticated) "Admin •" else "Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E676),
                            selectedTextColor = Color(0xFF00E676),
                            indicatorColor = Color(0xFF00E676).copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                RadioNavScreen.PLAYER -> PlayerScreen(viewModel = viewModel)
                RadioNavScreen.CHAT -> ChatScreen(viewModel = viewModel)
                RadioNavScreen.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                RadioNavScreen.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
