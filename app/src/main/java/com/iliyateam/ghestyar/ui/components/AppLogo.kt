// ═══ ui/components/AppLogo.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * کامپوننت بومی و فوق‌سریع لوگوی قسط‌یار با رندر مستقیم Canvas بدون خطای کرش XML
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val w = size.width
        val scale = w / 512f

        fun sx(v: Float) = v * scale
        fun sy(v: Float) = v * scale

        // ۱. هاله‌های نوری چندگانه متریال ۳
        drawCircle(
            color = Color(0x1A4F46E5),
            radius = sx(232f),
            center = Offset(sx(256f), sy(256f))
        )
        drawCircle(
            color = Color(0x246366F1),
            radius = sx(222f),
            center = Offset(sx(256f), sy(256f))
        )
        drawCircle(
            color = Color(0x337C3AED),
            radius = sx(210f),
            center = Offset(sx(256f), sy(256f))
        )

        // ۲. گوی اصلی نیلی-سرمه‌ای
        val orbBrush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF6366F1),
                0.38f to Color(0xFF4338CA),
                0.72f to Color(0xFF1E1B4B),
                1.0f to Color(0xFF0F172A)
            ),
            center = Offset(sx(175f), sy(145f)),
            radius = sx(390f)
        )
        drawCircle(
            brush = orbBrush,
            radius = sx(196f),
            center = Offset(sx(256f), sy(256f))
        )

        // ۳. قوس نور بالایی
        val arcPath = Path().apply {
            moveTo(sx(108f), sy(180f))
            cubicTo(sx(130f), sy(120f), sx(220f), sy(84f), sx(330f), sy(84f))
        }
        drawPath(
            path = arcPath,
            color = Color.White.copy(alpha = 0.28f),
            style = Stroke(width = sx(3.5f), cap = StrokeCap.Round)
        )

        val ty = sy(-36f)

        // ۴. سایه داینامیک زیر سکه‌ها
        drawOval(
            color = Color(0xFF090D16).copy(alpha = 0.38f),
            topLeft = Offset(sx(258f - 110f), sy(350f - 20f) + ty),
            size = Size(sx(220f), sy(40f))
        )

        // ۵. سکه اول (پایینی)
        drawOval(
            color = Color(0xFFB45309),
            topLeft = Offset(sx(270f - 96f), sy(344f - 17f) + ty),
            size = Size(sx(192f), sy(34f))
        )
        val coin1Brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFBEB),
                0.30f to Color(0xFFFBBF24),
                0.75f to Color(0xFFF59E0B),
                1.0f to Color(0xFFD97706)
            ),
            start = Offset(sx(212.4f), sy(300.4f) + ty),
            end = Offset(sx(337.2f), sy(354.8f) + ty)
        )
        drawOval(
            brush = coin1Brush,
            topLeft = Offset(sx(270f - 96f), sy(326f - 32f) + ty),
            size = Size(sx(192f), sy(64f))
        )

        // ۶. سکه دوم (وسطی)
        drawOval(
            color = Color(0xFFC2410C),
            topLeft = Offset(sx(250f - 92f), sy(309f - 16f) + ty),
            size = Size(sx(184f), sy(32f))
        )
        val coin2Brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFBEB),
                0.30f to Color(0xFFFDE047),
                0.70f to Color(0xFFF59E0B),
                1.0f to Color(0xFFD97706)
            ),
            start = Offset(sx(194.8f), sy(268f) + ty),
            end = Offset(sx(314.4f), sy(319f) + ty)
        )
        drawOval(
            brush = coin2Brush,
            topLeft = Offset(sx(250f - 92f), sy(292f - 30f) + ty),
            size = Size(sx(184f), sy(60f))
        )

        // ۷. سکه سوم (تارگت اصلی / بالایی)
        drawOval(
            color = Color(0xFFD97706),
            topLeft = Offset(sx(260f - 88f), sy(272f - 16f) + ty),
            size = Size(sx(176f), sy(32f))
        )
        val coin3Brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFEF0),
                0.25f to Color(0xFFFEF08A),
                0.65f to Color(0xFFFBBF24),
                1.0f to Color(0xFFEA580C)
            ),
            start = Offset(sx(207.2f), sy(232.8f) + ty),
            end = Offset(sx(321.6f), sy(282.1f) + ty)
        )
        drawOval(
            brush = coin3Brush,
            topLeft = Offset(sx(260f - 88f), sy(256f - 29f) + ty),
            size = Size(sx(176f), sy(58f))
        )

        // خط برجستگی لبه داخلی سکه
        drawOval(
            color = Color(0xFFB45309).copy(alpha = 0.40f),
            topLeft = Offset(sx(260f - 72f), sy(256f - 20f) + ty),
            size = Size(sx(144f), sy(40f)),
            style = Stroke(width = sx(2.2f))
        )

        // بازتاب درخشان کریستالی
        rotate(degrees = -14f, pivot = Offset(sx(228f), sy(245f) + ty)) {
            drawOval(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset(sx(228f - 26f), sy(245f - 9f) + ty),
                size = Size(sx(52f), sy(18f))
            )
        }
    }
}
