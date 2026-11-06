package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VisualizerView(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val count = amplitudes.size.coerceAtLeast(1)
        val availableWidth = size.width
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (count - 1)
        val barWidth = ((availableWidth - totalSpacing) / count).coerceAtLeast(3.dp.toPx())
        val maxHeight = size.height

        for (i in 0 until count) {
            val amp = amplitudes.getOrElse(i) { 0.1f }.coerceIn(0.08f, 1.0f)
            val barHeight = (maxHeight * amp).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + barSpacing)
            val y = maxHeight - barHeight

            val gradient = if (isPlaying) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF2A85), // Neon magenta at top
                        Color(0xFF00E5FF), // Neon cyan in middle
                        Color(0xFF7C4DFF)  // Deep violet at bottom
                    ),
                    startY = y,
                    endY = maxHeight
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A4B65),
                        Color(0xFF26273B)
                    ),
                    startY = y,
                    endY = maxHeight
                )
            }

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
