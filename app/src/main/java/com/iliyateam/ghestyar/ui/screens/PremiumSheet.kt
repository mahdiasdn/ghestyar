// ═══ ui/screens/PremiumSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.data.SubscriptionTier
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*

/**
 * شیت اشتراک ویژه طلایی با معماری طراحی نسل جدید Material 3 Expressive
 * شامل انیمیشن‌های فیزیکال فنری، کانتینرهای اسکوئیرکل، کارت‌های بنتو گرید و دکمه کپسولی درخشان
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSheet(
    isCurrentlyPremium: Boolean,
    onDismiss: () -> Unit,
    onUnlock: (SubscriptionTier) -> Unit,
    onResetFree: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(SubscriptionTier.YEARLY) }
    val isDark = isSystemInDarkTheme()

    // ۱. انیمیشن درخشش و ضربان هاله طلایی هیرو (Physics-based Spring Motion)
    val infiniteTransition = rememberInfiniteTransition(label = "m3_expressive_vip")
    val heroPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_pulse"
    )

    // ۲. انیمیشن خط بازتاب نور (Shimmer Sweep) روی دکمه اصلی
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btn_shimmer"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 44.dp, height = 5.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            ) {}
        },
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 34.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ─── ۱. هیرو سکشن لوکس و چشم‌نواز با هاله‌های نوری چندگانه ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // هاله‌های نور پس‌زمینه
                Box(
                    modifier = Modifier
                        .size((110 * heroPulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldVip.copy(alpha = if (isDark) 0.35f else 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // کانتینر اسکوئیرکل تاج طلایی
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.5.dp, GoldVip.copy(alpha = 0.6f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(80.dp)
                        .bounceClick(minScale = 0.92f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        GoldVip.copy(alpha = 0.25f),
                                        GoldVip.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = "قسط‌یار طلایی",
                            tint = GoldVip,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            // متن‌ها و برچسب کپسولی VIP
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldVip.copy(alpha = if (isDark) 0.2f else 0.14f),
                    border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = GoldVip,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "VIP PRO • تجربه بدون محدودیت",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isDark) GoldVip else Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    "قسط‌یار طلایی؛ دستیار هوشمند مالی",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    "صرفه‌جویی میلیونی در سود وام‌ها، کنترل کامل چک‌ها و مدیریت صندوق‌ها",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // ─── ۲. انتخابگر پلن‌های اشتراک به سبک کارت‌های تعاملی M3 Expressive ───
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubscriptionTier.entries.forEach { tier ->
                    val isSelected = selectedTier == tier
                    val animBorderColor by animateColorAsState(
                        targetValue = if (isSelected) GoldVip else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "tier_border"
                    )
                    val animBgColor by animateColorAsState(
                        targetValue = when {
                            isSelected && isDark -> Color(0xFF2A2415)
                            isSelected -> Color(0xFFFFFBEB)
                            isDark -> MaterialTheme.colorScheme.surfaceContainerLow
                            else -> MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "tier_bg"
                    )

                    Surface(
                        onClick = { selectedTier = tier },
                        shape = RoundedCornerShape(22.dp),
                        color = animBgColor,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, animBorderColor),
                        shadowElevation = if (isSelected) 4.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(minScale = 0.97f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // چک‌باکس اسکوئیرکل مدرن متریال ۳ به جای رادیوباتن معمولی
                            M3ExpressiveSelectionIndicator(isSelected = isSelected)

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = tier.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = if (isSelected && !isDark) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                                    )

                                    tier.discountBadge?.let { badge ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isSelected) GoldVip else GoldVip.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.Black else (if (isDark) GoldVip else Color(0xFFB45309)),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = tier.durationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // قیمت با تایپوگرافی شاخص بولد
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = tier.priceFormatted,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 19.sp
                                        ),
                                        color = if (isSelected) (if (isDark) GoldVip else Color(0xFFB45309)) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "تومان",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── ۳. کارت‌های مزایا به سبک Bento Grid پیشرفته متریال ۳ ───
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "امکانات ویژه اشتراک طلایی",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "۱۱ قابلیت برتر ⚡",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // ستون کارت‌های تفکیک‌شده بنتو
                val features = listOf(
                    Triple(
                        Icons.Rounded.Groups,
                        "صندوق‌های وام و قرعه‌کشی خانوادگی",
                        "مدیریت واریزی اعضا، گردونه شانس متحرک و گزارش پیام‌رسان‌ها"
                    ),
                    Triple(
                        Icons.Rounded.AutoAwesome,
                        "بهینه‌ساز استراتژی تسویه بدهی (بهمن)",
                        "صرفه‌جویی چند میلیونی در سود بانکی با اولویت‌بندی هوشمند"
                    ),
                    Triple(
                        Icons.Rounded.Widgets,
                        "ویجت‌های زنده و تعاملی صفحه اصلی",
                        "روزشمار زنده اقساط و مانده بدهی بدون نیاز به باز کردن برنامه"
                    ),
                    Triple(
                        Icons.Rounded.Palette,
                        "تم‌های اشرافی VIP (Obsidian Gold)",
                        "شخصی‌سازی لوکس با پوسته‌های طلایی، زمردی و یاقوتی اختصاصی"
                    ),
                    Triple(
                        Icons.Rounded.PictureAsPdf,
                        "دفترچه رسمی بانکی اقساط (PDF & Excel)",
                        "صدور دفترچه رسمی بانکی A4 با جدول سررسید، بارکد و محل امضا"
                    ),
                    Triple(
                        Icons.Rounded.Timeline,
                        "پیش‌بینی جریان نقدینگی ۶ ماه آینده",
                        "تحلیل آینده‌نگر موجودی با احتساب تمام اقساط و سررسید چک‌ها"
                    ),
                    Triple(
                        Icons.AutoMirrored.Rounded.Send,
                        "پیامک‌ساز و یادآور هوشمند تعهدات",
                        "تولید متن‌های رسمی و آماده ارسال با اطلاعات قسط و شماره کارت"
                    ),
                    Triple(
                        Icons.Rounded.AllInclusive,
                        "ثبت نامحدود اقساط، وام‌ها و چک‌ها",
                        "مدیریت بدون سقف تمام تعهدات مالی (رایگان: حداکثر ۴ قسط)"
                    ),
                    Triple(
                        Icons.Rounded.SupervisorAccount,
                        "پروفایل‌های مالی ایزوله و چندگانه",
                        "تفکیک کامل حساب‌های شخصی، خانوادگی، شرکت و فروشگاه"
                    ),
                    Triple(
                        Icons.Rounded.FileDownload,
                        "پشتیبان‌گیری آفلاین و بازیابی ایمن",
                        "ذخیره فایل پشتیبان جامع با یک لمس و بازیابی کامل در تمام دستگاه‌ها"
                    ),
                    Triple(
                        Icons.Rounded.Block,
                        "حذف کامل تبلیغات و تجربه روان",
                        "محیطی کاملاً تمیز، آرامش‌بخش و فوق‌العاده سریع"
                    )
                )

                features.forEach { (icon, title, desc) ->
                    ExpressiveBentoFeatureCard(icon = icon, title = title, desc = desc, isDark = isDark)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ─── ۴. دکمه اقدام کپسولی معلق با شیمر متحرک (Expressive Hero Action) ───
            Button(
                onClick = { onUnlock(selectedTier) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .bounceClick(minScale = 0.95f)
                    .drawWithContent {
                        drawContent()
                        // افکت نوری شیمر سراسری روی دکمه
                        val sweepWidth = size.width * 0.35f
                        val startX = size.width * shimmerOffset - sweepWidth
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startX = startX,
                                endX = startX + sweepWidth
                            )
                        )
                    },
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldVip,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "خرید و فعال‌سازی آنی (${selectedTier.priceFormatted} تومان)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                }
            }

            // گزینه‌های دمو و تست با فیدبک ارتجاعی نرم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onUnlock(selectedTier) },
                    modifier = Modifier.bounceClick(minScale = 0.94f)
                ) {
                    Text(
                        "فعال‌سازی آنی (حالت دمو/تست) ⚡",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Moss
                    )
                }

                if (isCurrentlyPremium) {
                    TextButton(
                        onClick = onResetFree,
                        modifier = Modifier.bounceClick(minScale = 0.94f)
                    ) {
                        Text(
                            "بازگشت به نسخه رایگان",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Coral
                        )
                    }
                }
            }
        }
    }
}

/**
 * نشانگر انتخابی اسکوئیرکل M3 Expressive با ترنزیشن فیزیکال
 */
@Composable
private fun M3ExpressiveSelectionIndicator(isSelected: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_scale"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) GoldVip else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 0.dp else 1.5.dp,
            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * کارت ویژگی‌های بنتو گرید با کانتینر اسکوئیرکل و هایلایت بصری
 */
@Composable
private fun ExpressiveBentoFeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GoldVip.copy(alpha = if (isDark) 0.2f else 0.14f),
                border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.35f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark) GoldVip else Color(0xFFB45309),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}