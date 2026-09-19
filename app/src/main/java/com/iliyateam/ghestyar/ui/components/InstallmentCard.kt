// ═══ ui/components/InstallmentCard.kt ═══
package com.iliyateam.ghestyar.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * کارت قسط نسل جدید با دیزاین رسمی Material 3 Expressive
 * خلوت، خوانا، بدون متون اضافه با تمرکز بر ارقام شاخص، برچسب‌های کپسولی و بازخورد لمسی سریع
 */
@Composable
fun InstallmentCard(
    item: Installment,
    onClick: () -> Unit,
    onPaid: () -> Unit,
    onUnmarkPaid: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val todayJ = remember { JalaliDate.today() }
    val due = remember(item.dueEpochDay) { LocalDate.ofEpochDay(item.dueEpochDay) }
    val dueJ = remember(due) { due.toJalali() }
    val daysLeft = remember(due, today) {
        ChronoUnit.DAYS.between(today, due)
    }
    val category = remember(item.category) { InstallmentCategories.get(item.category) }

    val isPaidThisMonth = remember(item.isPaid, item.paidSessions, item.paidAtEpochDay, item.dueEpochDay) {
        if (item.isPaid) return@remember true
        if (item.paidSessions <= 0) return@remember false
        val lastPaid = item.paidAtEpochDay?.let { LocalDate.ofEpochDay(it).toJalali() }
        val paidInCurrentJalaliMonth = lastPaid != null && lastPaid.jy == todayJ.jy && lastPaid.jm == todayJ.jm
        val nextDueIsInFutureMonth = dueJ.jy > todayJ.jy || (dueJ.jy == todayJ.jy && dueJ.jm > todayJ.jm)
        paidInCurrentJalaliMonth || nextDueIsInFutureMonth
    }

    var celebrating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun triggerPay() {
        if (celebrating) return
        celebrating = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            scale.animateTo(0.85f, spring(stiffness = Spring.StiffnessHigh))
            launch {
                scale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            delay(160)
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            delay(350)
            onPaid()
            celebrating = false
        }
    }

    val isDark = isSystemInDarkTheme()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = when {
            item.isPaid -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLowest else Color(0xFFF7FAF8)
            isPaidThisMonth -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFF5FBF7)
            daysLeft < 0 -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFFFF7F6)
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        border = BorderStroke(
            width = 1.dp,
            color = when {
                item.isPaid -> Moss.copy(alpha = 0.35f)
                isPaidThisMonth -> Moss.copy(alpha = 0.35f)
                daysLeft < 0 -> Coral.copy(alpha = 0.45f)
                daysLeft <= 3 -> GoldVip.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        shadowElevation = if (daysLeft in 0..3 && !isPaidThisMonth) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── ۱. سطر اصلی: آیکون، عنوان، مقصد، مبلغ درشت و منو ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // آیکون اسکوئیرکل دسته
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        item.isPaid -> Moss.copy(alpha = 0.16f)
                        isPaidThisMonth -> Moss.copy(alpha = 0.16f)
                        daysLeft < 0 -> Coral.copy(alpha = 0.16f)
                        else -> if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft.copy(alpha = 0.75f)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(category.emoji, fontSize = 20.sp)
                    }
                }

                // عنوان و مقصد
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.destination.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AccountBalance,
                                contentDescription = null,
                                tint = if (isDark) MossLight else Moss,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.destination,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // مبلغ قسط درشت و شفاف
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = item.amount.money(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = if (isDark) MossLight else Moss
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "ت",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // منوی گزینه‌ها
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "گزینه‌ها",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("مشاهده جزئیات", style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = { Icon(Icons.Outlined.Info, null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ویرایش اطلاعات", style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            }
                        )
                        if (item.paidSessions > 0 && onUnmarkPaid != null) {
                            DropdownMenuItem(
                                text = { Text("بازگردانی پرداخت ↩️", style = MaterialTheme.typography.labelLarge, color = Coral) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Undo, null, tint = Coral, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    menuOpen = false
                                    onUnmarkPaid()
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        DropdownMenuItem(
                            text = { Text("حذف قسط", color = Coral, style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Coral, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            }
                        )
                    }
                }
            }

            // ─── ۲. برچسب وضعیت و موعد کپسولی M3 Expressive (تک‌خطی و فوق‌العاده تمیز) ───
            Surface(
                shape = RoundedCornerShape(50),
                color = when {
                    item.isPaid -> Moss.copy(alpha = 0.14f)
                    isPaidThisMonth -> Moss.copy(alpha = 0.14f)
                    daysLeft < 0 -> Coral.copy(alpha = 0.14f)
                    daysLeft <= 3 -> GoldVip.copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                item.isPaid -> "✨ تسویه کامل"
                                isPaidThisMonth -> "✅ پرداخت این ماه انجام شده"
                                daysLeft < 0 -> "🚨 ${kotlin.math.abs(daysLeft).faDigits()} روز تاخیر"
                                daysLeft == 0L -> "⚡ سررسید امروز"
                                daysLeft <= 3 -> "🔔 ${daysLeft.faDigits()} روز مانده"
                                else -> "🗓️ موعد سررسید"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                item.isPaid -> Moss
                                isPaidThisMonth -> Moss
                                daysLeft < 0 -> Coral
                                daysLeft <= 3 -> if (isDark) GoldVip else Color(0xFFB45309)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    Text(
                        text = if (item.isPaid) "بدون مانده" else due.formatJalali(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── ۳. نوار پیشرفت کپسولی و دکمه اقدام سریع ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // نوار پیشرفت و وضعیت اقساط
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "قسط ${item.paidSessions.faDigits()} از ${item.totalSessions.faDigits()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${((item.overallProgress * 100).toInt()).faDigits()}٪",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MossLight else Moss
                        )
                    }

                    LinearProgressIndicator(
                        progress = { item.overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isDark) MossLight else Moss,
                        trackColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else Color(0xFFE2E8F0)
                    )
                }

                // دکمه اقدام ثبت پرداخت
                if (!item.isPaid) {
                    if (isPaidThisMonth) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Moss.copy(alpha = 0.15f),
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Moss, modifier = Modifier.size(15.dp))
                                Text(
                                    "تسویه شده",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Moss
                                )
                            }
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            ConfettiBurst(
                                active = celebrating,
                                color = Moss,
                                modifier = Modifier.size(60.dp)
                            )

                            Button(
                                onClick = ::triggerPay,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale.value
                                        scaleY = scale.value
                                    }
                                    .bounceClick(minScale = 0.92f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text(
                                        "پرداخت قسط",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "حذف «${item.title}»",
            message = "این قسط و یادآوری‌های آن حذف خواهند شد. آیا مطمئن هستید؟",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}