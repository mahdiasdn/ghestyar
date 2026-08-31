// ═══ ui/screens/SplashScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.ui.components.AppLogo
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.shimmerBrush
import com.iliyateam.ghestyar.ui.theme.GoldVip
import com.iliyateam.ghestyar.ui.theme.MintSoft
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.ui.theme.MossLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var logoAppeared by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val logoScale by animateFloatAsState(
        targetValue = if (logoAppeared) 1.0f else 0.4f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 320f
        ),
        label = "LogoScaleAnim"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (logoAppeared) 1.0f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "LogoAlphaAnim"
    )

    // انیمیشن پالس و هاله نوری لوگو
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "ProgressAnim"
    )

    LaunchedEffect(Unit) {
        logoAppeared = true
        delay(300)
        progress = 1f
        delay(2000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // لوگوی متحرک با فیزیک جهشی
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
            ) {
                // هاله نوری ضربان‌دار
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                            alpha = glowAlpha
                        }
                        .clip(CircleShape)
                        .background(MintSoft)
                )

                // لوگوی اصلی قسط‌یار با وکتور بومی و پرسرعت
                AppLogo(
                    modifier = Modifier
                        .size(118.dp)
                        .bounceClick(minScale = 0.95f)
                )
            }

            Spacer(Modifier.height(18.dp))

            // نام برنامه و شعار با ورود نرم
            Text(
                "قسط‌یار",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "دستیار هوشمند اقساط و آرامش مالی",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(42.dp))

            // لودینگ دایره‌ای کرلی و موج‌دار متریال ۳ اکسپرسیو (Wavy / Curly Circular Indicator)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                com.iliyateam.ghestyar.ui.components.M3ExpressiveCurlyLoadingIndicator(
                    size = 58.dp,
                    color = Moss,
                    secondaryColor = MossLight,
                    waveCount = 6,
                    waveAmplitude = 4.5.dp,
                    strokeWidth = 5.dp,
                    progress = animatedProgress
                )

                Text(
                    text = "در حال بارگذاری اطلاعات...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // اطلاعات نسخه در پایین صفحه
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                shadowElevation = 1.dp
            ) {
                Text(
                    "نسخه ۱.۰",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Moss,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
        }
    }
}
