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
import com.iliyateam.ghestyar.VipColorTheme
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
    vipTheme: VipColorTheme = VipColorTheme.TEAL_MOSS,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val primaryLight = when (vipTheme) {
        VipColorTheme.TEAL_MOSS -> Moss
        VipColorTheme.OBSIDIAN_GOLD -> Color(0xFFD4AF37)
        VipColorTheme.EMERALD_LUXURY -> Color(0xFF059669)
        VipColorTheme.ROYAL_AMETHYST -> Color(0xFF7C3AED)
        VipColorTheme.MIDNIGHT_SAPPHIRE -> Color(0xFF0284C7)
    }
    val primaryDark = when (vipTheme) {
        VipColorTheme.TEAL_MOSS -> MossLight
        VipColorTheme.OBSIDIAN_GOLD -> Color(0xFFFBBF24)
        VipColorTheme.EMERALD_LUXURY -> Color(0xFF34D399)
        VipColorTheme.ROYAL_AMETHYST -> Color(0xFFA78BFA)
        VipColorTheme.MIDNIGHT_SAPPHIRE -> Color(0xFF38BDF8)
    }

    val scheme = if (isDark) {
        when (vipTheme) {
            VipColorTheme.OBSIDIAN_GOLD -> darkColorScheme(
                primary = Color(0xFFF59E0B),
                onPrimary = Color(0xFF1C1202),
                primaryContainer = Color(0xFF3B2807),
                onPrimaryContainer = Color(0xFFFDE68A),
                secondary = Color(0xFFD4AF37),
                onSecondary = Color(0xFF1C1402),
                secondaryContainer = Color(0xFF2A200B),
                onSecondaryContainer = Color(0xFFFEF3C7),
                tertiary = Color(0xFFFBBF24),
                onTertiary = Color(0xFF221601),
                tertiaryContainer = Color(0xFF382504),
                onTertiaryContainer = Color(0xFFFFFBEB),
                background = Color(0xFF090A0C),
                onBackground = Color(0xFFF3F4F6),
                surface = Color(0xFF111317),
                onSurface = Color(0xFFF3F4F6),
                surfaceVariant = Color(0xFF1A1D23),
                onSurfaceVariant = Color(0xFFD1A94D),
                surfaceContainerLowest = Color(0xFF07080A),
                surfaceContainerLow = Color(0xFF14171C),
                surfaceContainer = Color(0xFF1A1D24),
                surfaceContainerHigh = Color(0xFF22262F),
                surfaceContainerHighest = Color(0xFF2B313C),
                outline = Color(0xFF785B17),
                outlineVariant = Color(0xFF45350F),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            VipColorTheme.EMERALD_LUXURY -> darkColorScheme(
                primary = Color(0xFF10B981),
                onPrimary = Color(0xFF003822),
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF34D399),
                onSecondary = Color(0xFF003822),
                secondaryContainer = Color(0xFF0B3324),
                onSecondaryContainer = Color(0xFFD1FAE5),
                tertiary = Color(0xFF6EE7B7),
                onTertiary = Color(0xFF022C1B),
                tertiaryContainer = Color(0xFF0E3F2D),
                onTertiaryContainer = Color(0xFFECFDF5),
                background = Color(0xFF05100B),
                onBackground = Color(0xFFECFDF5),
                surface = Color(0xFF0B1B13),
                onSurface = Color(0xFFECFDF5),
                surfaceVariant = Color(0xFF12281D),
                onSurfaceVariant = Color(0xFF6EE7B7),
                surfaceContainerLowest = Color(0xFF030A07),
                surfaceContainerLow = Color(0xFF0E2218),
                surfaceContainer = Color(0xFF132A1F),
                surfaceContainerHigh = Color(0xFF1A3628),
                surfaceContainerHighest = Color(0xFF234433),
                outline = Color(0xFF1B6B4C),
                outlineVariant = Color(0xFF11422F),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            VipColorTheme.ROYAL_AMETHYST -> darkColorScheme(
                primary = Color(0xFFA855F7),
                onPrimary = Color(0xFF2E004F),
                primaryContainer = Color(0xFF581C87),
                onPrimaryContainer = Color(0xFFE9D5FF),
                secondary = Color(0xFFC084FC),
                onSecondary = Color(0xFF2E004F),
                secondaryContainer = Color(0xFF3B1556),
                onSecondaryContainer = Color(0xFFF3E8FF),
                tertiary = Color(0xFFDDD6FE),
                onTertiary = Color(0xFF240E3E),
                tertiaryContainer = Color(0xFF451E6B),
                onTertiaryContainer = Color(0xFFFAF5FF),
                background = Color(0xFF0A0614),
                onBackground = Color(0xFFF5F3FF),
                surface = Color(0xFF130D24),
                onSurface = Color(0xFFF5F3FF),
                surfaceVariant = Color(0xFF1C1434),
                onSurfaceVariant = Color(0xFFC084FC),
                surfaceContainerLowest = Color(0xFF06030D),
                surfaceContainerLow = Color(0xFF18112C),
                surfaceContainer = Color(0xFF1F1638),
                surfaceContainerHigh = Color(0xFF271D44),
                surfaceContainerHighest = Color(0xFF312554),
                outline = Color(0xFF6B21A8),
                outlineVariant = Color(0xFF3B125C),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            VipColorTheme.MIDNIGHT_SAPPHIRE -> darkColorScheme(
                primary = Color(0xFF0EA5E9),
                onPrimary = Color(0xFF003554),
                primaryContainer = Color(0xFF0369A1),
                onPrimaryContainer = Color(0xFFBAE6FD),
                secondary = Color(0xFF38BDF8),
                onSecondary = Color(0xFF003554),
                secondaryContainer = Color(0xFF0C3450),
                onSecondaryContainer = Color(0xFFE0F2FE),
                tertiary = Color(0xFF7DD3FC),
                onTertiary = Color(0xFF04283F),
                tertiaryContainer = Color(0xFF104468),
                onTertiaryContainer = Color(0xFFF0F9FF),
                background = Color(0xFF050E18),
                onBackground = Color(0xFFF0F9FF),
                surface = Color(0xFF0A1827),
                onSurface = Color(0xFFF0F9FF),
                surfaceVariant = Color(0xFF112338),
                onSurfaceVariant = Color(0xFF7DD3FC),
                surfaceContainerLowest = Color(0xFF03080F),
                surfaceContainerLow = Color(0xFF0E2033),
                surfaceContainer = Color(0xFF13283F),
                surfaceContainerHigh = Color(0xFF19324D),
                surfaceContainerHighest = Color(0xFF203E5E),
                outline = Color(0xFF0284C7),
                outlineVariant = Color(0xFF075985),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            VipColorTheme.TEAL_MOSS -> darkColorScheme(
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
        }
    } else {
        when (vipTheme) {
            VipColorTheme.OBSIDIAN_GOLD -> lightColorScheme(
                primary = Color(0xFFB45309),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFEF3C7),
                onPrimaryContainer = Color(0xFF451A03),
                secondary = Color(0xFF78350F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFDE68A),
                onSecondaryContainer = Color(0xFF290E00),
                tertiary = Color(0xFFD97706),
                background = Color(0xFFFFFDF8),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFF7EED9),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFFAF4E6),
                surfaceContainer = Color(0xFFF3E9D0),
                surfaceContainerHigh = Color(0xFFEADDBB),
                surfaceContainerHighest = Color(0xFFDFCE9F),
                outline = Color(0xFFB45309).copy(alpha = 0.5f),
                outlineVariant = Color(0xFFD97706).copy(alpha = 0.3f),
                error = Coral,
                onError = Color.White
            )
            VipColorTheme.EMERALD_LUXURY -> lightColorScheme(
                primary = Color(0xFF047857),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1FAE5),
                onPrimaryContainer = Color(0xFF022C1B),
                secondary = Color(0xFF065F46),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFA7F3D0),
                onSecondaryContainer = Color(0xFF012013),
                tertiary = Color(0xFF059669),
                background = Color(0xFFF4FDF9),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFDEF5EB),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFE7F9F1),
                surfaceContainer = Color(0xFFD8F2E6),
                surfaceContainerHigh = Color(0xFFC7EADB),
                surfaceContainerHighest = Color(0xFFB2DEC9),
                outline = Color(0xFF047857).copy(alpha = 0.5f),
                outlineVariant = Color(0xFF059669).copy(alpha = 0.3f),
                error = Coral,
                onError = Color.White
            )
            VipColorTheme.ROYAL_AMETHYST -> lightColorScheme(
                primary = Color(0xFF7E22CE),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFF3E8FF),
                onPrimaryContainer = Color(0xFF3B0764),
                secondary = Color(0xFF6B21A8),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE9D5FF),
                onSecondaryContainer = Color(0xFF280344),
                tertiary = Color(0xFF9333EA),
                background = Color(0xFFFAF7FF),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFEFE4FF),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFF4EBFD),
                surfaceContainer = Color(0xFFEAD9FB),
                surfaceContainerHigh = Color(0xFFDFC6F8),
                surfaceContainerHighest = Color(0xFFD0AEF4),
                outline = Color(0xFF7E22CE).copy(alpha = 0.5f),
                outlineVariant = Color(0xFF9333EA).copy(alpha = 0.3f),
                error = Coral,
                onError = Color.White
            )
            VipColorTheme.MIDNIGHT_SAPPHIRE -> lightColorScheme(
                primary = Color(0xFF0284C7),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE0F2FE),
                onPrimaryContainer = Color(0xFF082F49),
                secondary = Color(0xFF0369A1),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFBAE6FD),
                onSecondaryContainer = Color(0xFF041E30),
                tertiary = Color(0xFF0EA5E9),
                background = Color(0xFFF3F9FD),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFDCEDF9),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFE4F2FC),
                surfaceContainer = Color(0xFFD3E7F8),
                surfaceContainerHigh = Color(0xFFC0DBF2),
                surfaceContainerHighest = Color(0xFFA8CBEC),
                outline = Color(0xFF0284C7).copy(alpha = 0.5f),
                outlineVariant = Color(0xFF0EA5E9).copy(alpha = 0.3f),
                error = Coral,
                onError = Color.White
            )
            VipColorTheme.TEAL_MOSS -> lightColorScheme(
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
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
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