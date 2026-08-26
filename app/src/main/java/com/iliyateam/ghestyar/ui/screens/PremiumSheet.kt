// ═══ ui/screens/PremiumSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.SubscriptionTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSheet(
    isCurrentlyPremium: Boolean,
    onDismiss: () -> Unit,
    onUnlock: (SubscriptionTier) -> Unit,
    onResetFree: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(SubscriptionTier.YEARLY) }

    // انیمیشن تابش نور طلایی تاج VIP
    val infiniteTransition = rememberInfiniteTransition(label = "vip_crown")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crown_shimmer"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // آیکون تاج طلایی متحرک
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GoldVipGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = "قسط‌یار طلایی",
                    tint = Color.White,
                    modifier = Modifier.size((40 * shimmer).dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "قسط‌یار طلایی (VIP) 👑",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "آرامش خاطر مالی، کنترل ۱۰۰٪ بر بدهی‌ها و صرفه‌جویی در سود وام‌ها",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // کارت پلن‌های اشتراک
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubscriptionTier.entries.forEach { tier ->
                    val isSelected = selectedTier == tier
                    Card(
                        onClick = { selectedTier = tier },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(minScale = 0.98f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GoldVip.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GoldVip else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTier = tier },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldVip)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        tier.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    tier.discountBadge?.let { badge ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = GoldVip.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                badge,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = GoldVip,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    tier.durationText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${tier.priceFormatted} ت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GoldVip else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // لیست مزایای ارزشمند اشتراک (Key Value Drivers)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "امکانات و ارزش‌های ویژه حساب طلایی:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                VipFeature(
                    icon = Icons.Rounded.Groups,
                    title = "مدیریت صندوق‌های وام خانوادگی و گردونه قرعه‌کشی",
                    desc = "مدیریت واریزی اعضا، گردونه شانس متحرک ماهانه و گزارش‌گیری پیام‌رسان‌ها"
                )
                VipFeature(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "بهینه‌ساز استراتژی تسویه و کاهش سود بانکی (بهمن)",
                    desc = "محاسبه دقیق صرفه‌جویی چند میلیونی در پرداخت سود بانکی با اولویت‌بندی هوشمند"
                )
                VipFeature(
                    icon = Icons.Rounded.Widgets,
                    title = "ویجت‌های تعاملی و زنده صفحه اصلی گوشی",
                    desc = "روزشمار زنده اقساط، مانده بدهی و دسترسی فوری بدون نیاز به باز کردن برنامه"
                )
                VipFeature(
                    icon = Icons.Rounded.Palette,
                    title = "شخصی‌سازی اشرافی با تم‌های لوکس VIP",
                    desc = "دسترسی به تم‌های طلایی اشرافی (Obsidian Gold)، زمردی و یاقوتی اختصاصی"
                )
                VipFeature(
                    icon = Icons.Rounded.PictureAsPdf,
                    title = "تولید دفترچه رسمی بانکی اقساط (PDF A4) و اکسل",
                    desc = "صدور دفترچه رسمی بانکی با جدول سررسید، بارکد و محل امضا جهت پرینت"
                )
                VipFeature(
                    icon = Icons.Rounded.Timeline,
                    title = "پیش‌بینی هوشمند جریان نقدینگی ۶ ماه آینده",
                    desc = "تحلیل آینده‌نگر موجودی با احتساب تمام اقساط و سررسید چک‌ها"
                )
                VipFeature(
                    icon = Icons.AutoMirrored.Rounded.Send,
                    title = "پیامک‌ساز و یادآور هوشمند تعهدات به بدهکاران و ضامنین",
                    desc = "تولید متن‌های رسمی و دوستانه آماده ارسال با اطلاعات قسط و شماره کارت"
                )
                VipFeature(
                    icon = Icons.Rounded.AllInclusive,
                    title = "ثبت نامحدود اقساط، وام‌ها، قلک‌ها و چک‌های صیادی",
                    desc = "مدیریت بدون سقف تمام تعهدات مالی (در نسخه رایگان تا ۴ قسط)"
                )
                VipFeature(
                    icon = Icons.Rounded.SupervisorAccount,
                    title = "پروفایل‌های مالی چندگانه و کاملاً ایزوله",
                    desc = "تفکیک کامل حساب‌های شخصی، خانوادگی، شرکت و فروشگاه"
                )
                VipFeature(
                    icon = Icons.Rounded.FileDownload,
                    title = "پشتیبان‌گیری آفلاین و بازیابی ایمن تمام اطلاعات",
                    desc = "ذخیره فایل پشتیبان جامع با یک لمس و بازیابی کامل در تمام دستگاه‌ها"
                )
                VipFeature(
                    icon = Icons.Rounded.Block,
                    title = "حذف کامل تبلیغات و تجربه بدون وقفه",
                    desc = "محیطی تمیز، آرامش‌بخش و فوق‌العاده سریع"
                )
            }

            Spacer(Modifier.height(4.dp))

            // دکمه خرید اصلی
            Button(
                onClick = { onUnlock(selectedTier) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color.Black)
                    Text(
                        "خرید و فعال‌سازی فوری (${selectedTier.priceFormatted} تومان)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // گزینه‌های دمو / تست
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onUnlock(selectedTier) }) {
                    Text("فعال‌سازی آنی (حالت دمو/تست)", style = MaterialTheme.typography.labelMedium, color = Moss)
                }

                if (isCurrentlyPremium) {
                    TextButton(onClick = onResetFree) {
                        Text("بازگشت به نسخه رایگان", style = MaterialTheme.typography.labelMedium, color = Coral)
                    }
                }
            }
        }
    }
}

@Composable
private fun VipFeature(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = GoldVip.copy(alpha = 0.16f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = GoldVip,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.Star, null, tint = GoldVip, modifier = Modifier.size(13.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}