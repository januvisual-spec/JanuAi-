package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JanuNeonCyan
import com.example.ui.theme.JanuNeonGreen
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(modifier = modifier.fillMaxWidth().height(height)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = (canvasWidth / (barCount * 1.6f)).coerceIn(3f, 10f)
            val spacing = (canvasWidth - (barWidth * barCount)) / (barCount - 1)

            val isActive = isListening || isSpeaking
            val effectiveLevel = if (isActive) audioLevel.coerceIn(0.15f, 1f) else 0.05f

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                // Sine wave variation across bars
                val waveOffset = sin((i.toFloat() / barCount * 3.5f * Math.PI + phase).toDouble()).toFloat()
                val normalizedHeight = if (isActive) {
                    ((waveOffset + 1.2f) * 0.4f * effectiveLevel + 0.15f).coerceIn(0.1f, 1f)
                } else {
                    0.08f + (sin((i + phase).toDouble()).toFloat() * 0.03f)
                }

                val currentBarHeight = canvasHeight * normalizedHeight
                val y = (canvasHeight - currentBarHeight) / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isListening) JanuNeonCyan else JanuNeonGreen,
                            if (isSpeaking) JanuNeonCyan else Color(0xFF024329)
                        )
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
