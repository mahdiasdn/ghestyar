// ═══ ui/components/Celebration.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.iliyateam.ghestyar.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PaidCheck(modifier: Modifier = Modifier) {
    val pop = remember { Animatable(0f) }
    val draw = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        pop.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(100)
        draw.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
    }

    Canvas(modifier.graphicsLayer {
        val s = pop.value.coerceAtLeast(0.001f)
        scaleX = s
        scaleY = s
    }) {
        drawCircle(Moss)
        val half = size.minDimension / 2
        fun p(x: Float, y: Float) = center + Offset(x * half, y * half)
        val a = p(-0.38f, 0.02f)
        val b = p(-0.08f, 0.30f)
        val c = p(0.42f, -0.26f)

        val t = draw.value
        val s1 = (t / 0.4f).coerceIn(0f, 1f)
        val s2 = ((t - 0.4f) / 0.6f).coerceIn(0f, 1f)
        val w = size.minDimension * 0.13f

        drawLine(Color.White, a, a + (b - a) * s1, w, cap = StrokeCap.Round)
        if (s2 > 0f) {
            drawLine(Color.White, b, b + (c - b) * s2, w, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun ConfettiBurst(active: Boolean, color: Color = Moss, modifier: Modifier = Modifier) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            t.snapTo(0f)
            t.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
    }
    if (!active) return

    val density = LocalDensity.current
    val palette = listOf(color, GoldVip, Coral, ChequeBlue, Color(0xFF10B981), Color(0xFF8B5CF6))

    Canvas(modifier) {
        val dotRadius = with(density) { 4.dp.toPx() }
        val particleCount = 24

        for (i in 0 until particleCount) {
            val angle = (i * (360f / particleCount) + (i * 7f)) * PI.toFloat() / 180f
            val distanceFactor = if (i % 2 == 0) 0.85f else 0.55f
            val dist = size.minDimension * distanceFactor * t.value
            val pos = center + Offset(cos(angle) * dist, sin(angle) * dist)

            val particleColor = palette[i % palette.size]
            val alpha = (1f - t.value).coerceIn(0f, 1f)
            val currentRadius = dotRadius * (1f - t.value * 0.3f)

            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = currentRadius,
                center = pos
            )
        }
    }
}

/**
 * پرتاب تمام‌صفحه ذرات جشن و موفقیت (Full-Screen Golden Confetti Fountain)
 */
@Composable
fun FullScreenCelebration(
    active: Boolean,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!active) return

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(active) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            animationSpec = tween(1500, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    val particleList = remember {
        List(40) {
            ParticleData(
                startX = Random.nextFloat(),
                speedX = (Random.nextFloat() - 0.5f) * 0.4f,
                speedY = Random.nextFloat() * 0.7f + 0.5f,
                color = listOf(GoldVip, Moss, ChequeBlue, Coral, Color(0xFFFBBF24), Color(0xFF34D399))[it % 6],
                size = Random.nextFloat() * 10f + 6f,
                shape = it % 3 // 0: دایره, 1: مستطیل/پولک, 2: ستاره
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animProgress.value
        val alpha = (1f - progress * 0.8f).coerceIn(0f, 1f)

        particleList.forEach { p ->
            val x = (p.startX + p.speedX * progress) * size.width
            val y = (progress * p.speedY) * size.height

            when (p.shape) {
                0 -> {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size,
                        center = Offset(x, y)
                    )
                }
                1 -> {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size, y - p.size / 2),
                        size = androidx.compose.ui.geometry.Size(p.size * 2, p.size)
                    )
                }
                else -> {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * 0.8f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private data class ParticleData(
    val startX: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val shape: Int
)