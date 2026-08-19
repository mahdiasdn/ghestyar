// ═══ ui/components/ProgressRing.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressRing(
    progress: Float,          // 0..1 چقدر از دوره گذشته
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    isOverdue: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ring_progress"
    )

    val pulse = if (isOverdue) {
        val pulseTransition = rememberInfiniteTransition(label = "pulse_trans")
        pulseTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_val"
        ).value
    } else {
        0.3f
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (size.minDimension - stroke) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // در حالت تأخیر، هاله چشمک‌زن قرمز/اخطار رسم می‌شود
            if (isOverdue) {
                drawCircle(
                    color = tint.copy(alpha = pulse * 0.35f),
                    radius = radius + stroke * 0.8f,
                    center = center,
                    style = Stroke(width = stroke * 1.8f)
                )
            }

            // مسیر پایه (Track)
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // کمان پیشرفت (Progress Arc)
            if (animatedProgress > 0f) {
                drawArc(
                    color = tint,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}