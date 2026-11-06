package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    remainingSeconds: Int,
    onSetTimer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val minutesLeft = remainingSeconds / 60
    val secondsLeft = remainingSeconds % 60

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141524),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Temporizador de Reposo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (remainingSeconds > 0) {
                            "Apagado automático en %02d:%02d min".format(minutesLeft, secondsLeft)
                        } else {
                            "Programa el apagado automático de la música"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (remainingSeconds > 0) Color(0xFF00E5FF) else Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val timerOptions = listOf(
                15 to "15 minutos",
                30 to "30 minutos",
                45 to "45 minutos",
                60 to "60 minutos (1 hora)",
                90 to "90 minutos"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                timerOptions.forEach { (mins, label) ->
                    val isApproxActive = remainingSeconds > (mins - 1) * 60 && remainingSeconds <= mins * 60
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSetTimer(mins)
                                onDismiss()
                            }
                            .border(
                                width = if (isApproxActive) 1.5.dp else 0.dp,
                                color = if (isApproxActive) Color(0xFF7C4DFF) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isApproxActive) Color(0xFF261D4A) else Color(0xFF1E2038)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isApproxActive) Color(0xFF7C4DFF) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontWeight = if (isApproxActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            if (remainingSeconds > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = {
                        onSetTimer(0)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFF5252).copy(alpha = 0.2f),
                        contentColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar Temporizador", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
