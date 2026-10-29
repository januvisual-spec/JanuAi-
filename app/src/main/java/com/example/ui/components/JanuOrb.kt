package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JanuNeonCyan
import com.example.ui.theme.JanuNeonGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JanuOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")

    // Slow continuous outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    // Reverse middle rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Core pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening || isSpeaking) 600 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.width / 2f - 16.dp.toPx()

            // Dynamic reactive amplification
            val reactionMultiplier = if (isListening || isSpeaking) (1f + audioLevel * 0.4f) else 1f
            val dynamicCoreRadius = (baseRadius * 0.52f * pulseScale * reactionMultiplier)

            // 1. Holographic Outer Dashed Ring
            rotate(outerRotation, pivot = center) {
                drawCircle(
                    color = JanuNeonCyan.copy(alpha = 0.35f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 2. Middle Quantum Ring with Arc Segments
            rotate(innerRotation, pivot = center) {
                drawCircle(
                    color = JanuNeonGreen.copy(alpha = 0.55f),
                    radius = baseRadius * 0.82f,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 30f), 0f),
                        cap = StrokeCap.Round
                    )
                )

                // 4 Orbiting Satellite Nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90.0))
                    val nodeX = center.x + (baseRadius * 0.82f * cos(angle)).toFloat()
                    val nodeY = center.y + (baseRadius * 0.82f * sin(angle)).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 3. Ambient Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isListening) JanuNeonCyan else JanuNeonGreen).copy(alpha = 0.45f),
                        JanuNeonGreen.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicCoreRadius * 1.5f
                ),
                radius = dynamicCoreRadius * 1.5f,
                center = center
            )

            // 4. Inner Luminous Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        if (isListening) JanuNeonCyan else JanuNeonGreen,
                        if (isSpeaking) JanuNeonCyan else Color(0xFF042F24)
                    ),
                    center = center,
                    radius = dynamicCoreRadius
                ),
                radius = dynamicCoreRadius,
                center = center
            )

            // 5. Tech Center Crosshair / Pulse Target
            val crosshairLen = 14.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(center.x - crosshairLen, center.y),
                end = Offset(center.x + crosshairLen, center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(center.x, center.y - crosshairLen),
                end = Offset(center.x, center.y + crosshairLen),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
