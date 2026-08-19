// ═══ ui/theme/Theme.kt ═══
package com.iliyateam.ghestyar.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.AppFontScale
import com.iliyateam.ghestyar.AppThemeMode
import com.iliyateam.ghestyar.R

// ══════════════════════════════════════════════════════════════
// 🎨 پالت رنگی اختصاصی Material 3 Expressive (مطابق با کیت طراحی قسط‌یار)
// ══════════════════════════════════════════════════════════════
val Moss            = Color(0xFF006A6E) // Primary (Deep Pine Teal #006A6E)
val MossLight       = Color(0xFF42B3B8)
val MossDeep        = Color(0xFF004D50)
val MintSoft        = Color(0xFFCCE8E9) // Sec Container #CCE8E9
val MintLight       = Color(0xFF8CF1F6) // Prim Container #8CF1F6

val GoldVip         = Color(0xFFF59E0B) // Tertiary / VIP / Goals
val GoldLight       = Color(0xFFFDE68A)
val GoldVipGradient = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24)))

// رنگ متمایز چک‌های صیادی و طلب‌ها (لاجوردی درخشان متمایز از سبز اقساط)
val ChequeBlue      = Color(0xFF2563EB) // Royal Blue for Cheques & Debts
val ChequeBlueSoft  = Color(0xFFDBEAFE)
val ChequePurple    = Color(0xFF7C3AED) // Purple for Receivable

val Coral           = Color(0xFFBA1A1A) // Error #BA1A1A / Overdue / Expense
val CoralSoft       = Color(0xFFFFDAD6)

val Amber           = Color(0xFFD97706) // Warning
val AmberSoft       = Color(0xFFFEF3C7)

val InkSoft         = Color(0xFF4A6364)

// سطوح با کنتراست بالا و عمق رنگی M3 Expressive
val SurfaceLight             = Color(0xFFFFFFFF)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight  = Color(0xFFF0F6F6)
val SurfaceContainerLight    = Color(0xFFE8F1F1)
val SurfaceContainerHighLight = Color(0xFFE0ECEC)
val SurfaceContainerHighestLight = Color(0xFFD8E6E6)
val BackgroundLight          = Color(0xFFF4FAFA)

// پالت تاریک فوق‌العاده شیک و عمیق (Charcoal OLED - بدون اغراق در رنگ سبز)
val SurfaceDark              = Color(0xFF16191A)
val SurfaceContainerLowestDark  = Color(0xFF0F1213)
val SurfaceContainerLowDark   = Color(0xFF1B1F20)
val SurfaceContainerDark     = Color(0xFF212628)
val SurfaceContainerHighDark  = Color(0xFF282E30)
val SurfaceContainerHighestDark = Color(0xFF303739)
val BackgroundDark           = Color(0xFF101314)

// گرادیان‌های مدرن Expressive
val HeroGradientLight = Brush.linearGradient(
    listOf(Color(0xFF006A6E), Color(0xFF00585B))
)

val HeroGradientDark = Brush.linearGradient(
    listOf(Color(0xFF1F2A2B), Color(0xFF192425))
)

val CardBorderGradient = Brush.linearGradient(
    listOf(MintSoft.copy(alpha = 0.8f), Color.Transparent)
)

val installmentPalette = listOf(
    Color(0xFF006A6E), // سبزآبی تیره M3
    Color(0xFF2563EB), // لاجوردی
    Color(0xFF7C3AED), // بنفش
    Color(0xFFEA580C), // کهربایی گرم
    Color(0xFFDB2777), // ارغوانی
    Color(0xFF0891B2), // فیروزه‌ای
    Color(0xFF4B5563)  // متالیک
)

// ══════════════════════════════════════════════════════════════
// ⚡ سیستم انیمیشن و فیزیک فنری Material 3 Expressive Motion
// ══════════════════════════════════════════════════════════════
val ExpressiveSpring = spring<Float>(
    dampingRatio = 0.7f,
    stiffness = 380f
)

val ExpressiveBouncy = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

val SmoothSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

// ══════════════════════════════════════════════════════════════
// 📐 اشکال هندسی با گوشه‌های گرد ۲۸dp - M3 Expressive Shapes
// ══════════════════════════════════════════════════════════════
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

fun createQestTypography(scale: Float = 1.0f): Typography {
    fun fam(size: Int, weight: FontWeight = FontWeight.Normal, lineHeight: Int = size + 7) =
        TextStyle(
            fontFamily = Vazirmatn,
            fontSize = (size * scale).sp,
            fontWeight = weight,
            lineHeight = ((lineHeight * scale).toInt()).sp
        )

    return Typography(
        headlineLarge  = fam(28, FontWeight.Bold, 36),
        headlineMedium = fam(22, FontWeight.Bold, 30),
        headlineSmall  = fam(19, FontWeight.Bold, 26),
        titleLarge     = fam(17, FontWeight.Bold, 24),
        titleMedium    = fam(15, FontWeight.Medium, 22),
        titleSmall     = fam(13, FontWeight.Medium, 18),
        bodyLarge      = fam(14, FontWeight.Normal, 22),
        bodyMedium     = fam(13, FontWeight.Normal, 20),
        bodySmall      = fam(11, FontWeight.Normal, 16),
        labelLarge     = fam(12, FontWeight.Medium, 18),
        labelMedium    = fam(11, FontWeight.Medium, 16),
        labelSmall     = fam(9, FontWeight.Medium, 14)
    )
}

val QestTypography = createQestTypography(1.0f)

@Composable
fun QestYarTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    fontScale: AppFontScale = AppFontScale.NORMAL,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val scheme = if (isDark) {
        darkColorScheme(
            primary = MossLight,
            onPrimary = Color(0xFF003739),
            primaryContainer = Color(0xFF1B2F30),
            onPrimaryContainer = Color(0xFF8CF1F6),
            secondary = Color(0xFFBAC3C3),
            onSecondary = Color(0xFF243132),
            secondaryContainer = Color(0xFF282F30),
            onSecondaryContainer = Color(0xFFDEE4E4),
            tertiary = Color(0xFFB8C6EA),
            onTertiary = Color(0xFF22304C),
            tertiaryContainer = Color(0xFF2D3545),
            onTertiaryContainer = Color(0xFFDAE2FF),
            background = BackgroundDark,
            onBackground = Color(0xFFE2E6E6),
            surface = SurfaceDark,
            onSurface = Color(0xFFE2E6E6),
            surfaceVariant = SurfaceContainerDark,
            onSurfaceVariant = Color(0xFF9EAAAB),
            surfaceContainerLowest = SurfaceContainerLowestDark,
            surfaceContainerLow = SurfaceContainerLowDark,
            surfaceContainer = SurfaceContainerDark,
            surfaceContainerHigh = SurfaceContainerHighDark,
            surfaceContainerHighest = SurfaceContainerHighestDark,
            outline = Color(0xFF4C5556),
            outlineVariant = Color(0xFF313839),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005)
        )
    } else {
        lightColorScheme(
            primary = Moss,
            onPrimary = Color.White,
            primaryContainer = MintSoft,
            onPrimaryContainer = Color(0xFF002022),
            secondary = Color(0xFF4A6364),
            onSecondary = Color.White,
            secondaryContainer = MintSoft,
            onSecondaryContainer = Color(0xFF051F20),
            tertiary = Color(0xFF525E7D),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDAE2FF),
            onTertiaryContainer = Color(0xFF0E1A36),
            background = BackgroundLight,
            onBackground = Color(0xFF161D1D),
            surface = SurfaceLight,
            onSurface = Color(0xFF161D1D),
            surfaceVariant = SurfaceContainerLight,
            onSurfaceVariant = Color(0xFF3F4949),
            surfaceContainerLowest = SurfaceContainerLowestLight,
            surfaceContainerLow = SurfaceContainerLowLight,
            surfaceContainer = SurfaceContainerLight,
            surfaceContainerHigh = SurfaceContainerHighLight,
            surfaceContainerHighest = SurfaceContainerHighestLight,
            outline = Color(0xFF6F7979),
            outlineVariant = Color(0xFFBFC9C9),
            error = Coral,
            onError = Color.White
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(scheme.background.toArgb()))
            val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = createQestTypography(fontScale.scale),
        shapes = ExpressiveShapes,
        content = content
    )
}

fun urgencyColor(daysLeft: Long): Color = when {
    daysLeft < 0   -> Coral
    daysLeft == 0L -> Color(0xFFE11D48)
    daysLeft <= 2  -> Color(0xFFEA580C)
    daysLeft <= 5  -> GoldVip
    daysLeft <= 10 -> Color(0xFF65A30D)
    else           -> Moss
}