// ═══ ui/components/ExpressiveMotion.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.money

// ══════════════════════════════════════════════════════════════
// ⚡ کلیک ارتجاعی فیزیکی و فوق‌العاده نرم با بازخورد هپتیک (Bounce Interaction)
// ══════════════════════════════════════════════════════════════
@Composable
fun Modifier.bounceClick(
    minScale: Float = 0.94f,
    onClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) minScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 650f
        ),
        label = "BounceAnim"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

// ══════════════════════════════════════════════════════════════
// 🌊 فیزیک فنری پیشرفته هنگام لمس طولانی یا فشرده شدن
// ══════════════════════════════════════════════════════════════
@Composable
fun Modifier.springPress(
    targetScale: Float = 0.92f,
    onClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SpringPress"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

// ══════════════════════════════════════════════════════════════
// ✨ انیمیشن تنفس نوری و پالس زنده (Pulsing Glow) برای سررسیدها و نشان‌ها
// ══════════════════════════════════════════════════════════════
@Composable
fun Modifier.pulseGlow(
    enabled: Boolean = true,
    minScale: Float = 0.96f,
    maxScale: Float = 1.04f,
    durationMillis: Int = 1400
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ══════════════════════════════════════════════════════════════
// 💎 شیمر و افکت براق درخشان (Shimmer Effect)
// ══════════════════════════════════════════════════════════════
@Composable
fun Modifier.shimmerBrush(
    targetValue: Float = 1000f,
    durationMillis: Int = 1200
): Modifier {
    val shimmerColors = remember {
        listOf(
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.1f),
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    return this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 200f, translateAnimation - 200f),
            end = Offset(translateAnimation, translateAnimation)
        )
        drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
    }
}

// ══════════════════════════════════════════════════════════════
// 🔢 انیمیشن چرخش و تغییر نرم ارقام و مبالغ (Animated Rolling Numbers)
// ══════════════════════════════════════════════════════════════
@Composable
fun AnimatedMoneyText(
    amount: Long,
    isPrivacy: Boolean = false,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    suffix: String = "ت"
) {
    if (isPrivacy) {
        Text("••••••", style = style, fontWeight = fontWeight, color = color, modifier = modifier)
    } else {
        val animatedAmount by animateFloatAsState(
            targetValue = amount.toFloat(),
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 280f),
            label = "MoneyRollingAnim"
        )

        Row(modifier = modifier) {
            Text(
                text = "${animatedAmount.toLong().money()} $suffix",
                style = style,
                fontWeight = fontWeight,
                color = color
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
// 🌊 ورود آبشاری و پلکانی فوق‌العاده نرم با فیزیک جهشی (Staggered Entrance)
// ══════════════════════════════════════════════════════════════
@Composable
fun StaggeredItemEntrance(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val staggerDelay = ((index % 10) * 40).coerceIn(0, 360)

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 240, delayMillis = staggerDelay)
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 360f
            )
        ) + scaleIn(
            initialScale = 0.93f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 360f
            )
        ),
        exit = fadeOut(animationSpec = tween(120))
    ) {
        content()
    }
}

// ══════════════════════════════════════════════════════════════
// 🔄 ترنزیشن روان و هماهنگ با چیدمان راست‌به‌چپ (RTL) بین تب‌ها
// ══════════════════════════════════════════════════════════════
fun tabTransitionSpec(targetIndex: Int, initialIndex: Int): ContentTransform {
    val isMovingToLeftTab = targetIndex >= initialIndex
    return (
        slideInHorizontally(
            initialOffsetX = { if (isMovingToLeftTab) -it / 4 else it / 4 },
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 220)
        )
    ).togetherWith(
        slideOutHorizontally(
            targetOffsetX = { if (isMovingToLeftTab) it / 4 else -it / 4 },
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 160)
        )
    )
}
