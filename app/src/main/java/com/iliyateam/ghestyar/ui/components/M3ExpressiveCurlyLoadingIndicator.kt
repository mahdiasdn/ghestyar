// ═══ ui/components/M3ExpressiveCurlyLoadingIndicator.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.ui.theme.MossLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * لودینگ دایره‌ای کرلی و موج‌دار رسمی Material 3 Expressive (Curly / Wavy Circular Indicator)
 * پیاده‌سازی امواج سینوسی ارگانیک، چرخش نرم، گرادینت نورانی و خطوط سرگرد
 */
@Composable
fun M3ExpressiveCurlyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    color: Color = Moss,
    secondaryColor: Color = MossLight,
    trackColor: Color = color.copy(alpha = 0.15f),
    strokeWidth: Dp = 5.dp,
    waveCount: Int = 5,
    waveAmplitude: Dp = 4.dp,
    progress: Float? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "M3CurlyLoading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CurlyRotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val strokePx = strokeWidth.toPx()
            val ampPx = waveAmplitude.toPx()
            val baseRadius = (this.size.width - strokePx * 2 - ampPx * 2) / 2f

            // ۱. رسم ترک کرلی پس‌زمینه (Background Curly Track)
            val trackPath = Path()
            val totalPoints = 140
            for (i in 0..totalPoints) {
                val angle = (i.toFloat() / totalPoints) * 2 * PI.toFloat()
                val r = baseRadius + ampPx * sin(waveCount * angle)
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) trackPath.moveTo(x, y) else trackPath.lineTo(x, y)
            }
            trackPath.close()

            drawPath(
                path = trackPath,
                color = trackColor,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // ۲. رسم موج کرلی فعال پیشرفت (Active Wavy Foreground)
            val activePath = Path()
            val sweepRatio = progress ?: 0.72f
            val sweepPoints = (totalPoints * sweepRatio.coerceIn(0.08f, 1f)).toInt()

            rotate(degrees = rotation, pivot = center) {
                for (i in 0..sweepPoints) {
                    val angle = (i.toFloat() / totalPoints) * 2 * PI.toFloat()
                    val r = baseRadius + ampPx * sin(waveCount * angle + wavePhase)
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (i == 0) activePath.moveTo(x, y) else activePath.lineTo(x, y)
                }

                drawPath(
                    path = activePath,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            secondaryColor,
                            color
                        ),
                        center = center
                    ),
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
