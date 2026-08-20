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
    val remainingSessions = remember(item.totalSessions, item.paidSessions) {
        (item.totalSessions - item.paidSessions).coerceAtLeast(0)
    }

    // تشخیص دقیق اینکه آیا قسط دوره/ماه جاری پرداخت شده است یا خیر
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
            scale.animateTo(0.82f, spring(stiffness = Spring.StiffnessHigh))
            launch {
                scale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            delay(180)
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            delay(400)
            onPaid()
            celebrating = false
        }
    }

    val isDark = isSystemInDarkTheme()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.isPaid -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLowest else Color(0xFFF6FAF7)
                isPaidThisMonth -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFF4FBF7)
                daysLeft < 0 -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFFFF7F6)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                item.isPaid -> Moss.copy(alpha = 0.35f)
                isPaidThisMonth -> Moss.copy(alpha = 0.38f)
                daysLeft < 0 -> Coral.copy(alpha = 0.45f)
                daysLeft <= 3 -> GoldVip.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ۱. سطر بالا: آیکون دسته‌بندی + عنوان + بج مقصد + منوی ۳ نقطه
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // آیکون دسته‌بندی با کانتینر رنگی
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

                // عنوان و تگ مقصد
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // بج کپسولی شماره قسط
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft.copy(alpha = 0.5f)
                        ) {
                            Text(
                                if (item.isPaid) "تسویه شده ✨"
                                else "قسط ${item.paidSessions.faDigits()} از ${item.totalSessions.faDigits()}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MossLight else MossDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (item.destination.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else Color(0xFFEBF1F6)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isDark) MossLight else Moss,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        item.destination,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // منوی ۳ نقطه
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(32.dp)
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
                            text = { Text("مشاهده جزئیات کامل", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Info, null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ویرایش قسط", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            }
                        )
                        if (item.paidSessions > 0 && onUnmarkPaid != null) {
                            DropdownMenuItem(
                                text = { Text("بازگردانی آخرین پرداخت ↩️", fontSize = 12.sp, color = Coral) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Undo, null, tint = Coral, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    menuOpen = false
                                    onUnmarkPaid()
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        DropdownMenuItem(
                            text = { Text("حذف قسط", color = Coral, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Coral, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            }
                        )
                    }
                }
            }

            // ۲. سطر مبلغ ماهانه و باقیمانده کل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "مبلغ هر قسط:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${item.amount.money()} تومان",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MossLight else Moss
                    )
                }

                if (item.totalSessions > 1) {
                    Text(
                        "مانده کل: ${item.remainingAmount.money()} ت",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ۳. بنر اختصاصی و شفاف وضعیت ماه و موعد سررسید
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    item.isPaid -> Moss.copy(alpha = 0.12f)
                    isPaidThisMonth -> Moss.copy(alpha = 0.12f)
                    daysLeft < 0 -> Coral.copy(alpha = 0.12f)
                    daysLeft <= 3 -> GoldVip.copy(alpha = 0.15f)
                    else -> if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft.copy(alpha = 0.45f)
                },
                border = BorderStroke(
                    0.7.dp,
                    when {
                        item.isPaid -> Moss.copy(alpha = 0.35f)
                        isPaidThisMonth -> Moss.copy(alpha = 0.35f)
                        daysLeft < 0 -> Coral.copy(alpha = 0.35f)
                        daysLeft <= 3 -> GoldVip.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isPaid) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✨", fontSize = 13.sp)
                            Text(
                                "تمام اقساط این وام با موفقیت پرداخت و تسویه شد",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Moss
                            )
                        }
                    } else if (isPaidThisMonth) {
                        // وضعیت پرداخت این ماه با ذکر تاریخ دقیق قسط بعدی
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = CircleShape, color = Moss.copy(alpha = 0.2f), modifier = Modifier.size(20.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Check, null, tint = Moss, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    "این ماه پرداخت شده ✅",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Moss
                                )
                            }

                            Text(
                                "قسط بعدی: ${due.formatJalali()} (${daysLeft.faDigits()} روز دیگر)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // وضعیت سررسید جاری یا معوق
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = (if (daysLeft < 0) Coral else if (daysLeft <= 3) GoldVip else Moss).copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (daysLeft < 0) Icons.Rounded.Warning else Icons.Rounded.Schedule,
                                            null,
                                            tint = if (daysLeft < 0) Coral else if (daysLeft <= 3) GoldVip else Moss,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Text(
                                    when {
                                        daysLeft < 0 -> "${kotlin.math.abs(daysLeft).faDigits()} روز تاخیر در پرداخت!"
                                        daysLeft == 0L -> "سررسید قسط امروز است ⚡"
                                        else -> "${daysLeft.faDigits()} روز تا سررسید قسط ${(item.paidSessions + 1).faDigits()}"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysLeft < 0) Coral else if (daysLeft <= 3) GoldVip else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                "سررسید: ${due.formatJalali()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (daysLeft < 0) Coral else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ۴. سطر پایین: نوار پیشرفت تسویه وام + دکمه پرداخت یا تایید
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // نوار پیشرفت
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${((item.overallProgress * 100).toInt()).faDigits()}٪ تسویه شده",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MossLight else Moss
                        )
                        Text(
                            "${remainingSessions.faDigits()} قسط مانده",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { item.overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isDark) MossLight else Moss,
                        trackColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft.copy(alpha = 0.45f)
                    )
                }

                // دکمه اقدام
                if (!item.isPaid) {
                    if (isPaidThisMonth) {
                        Surface(
                            onClick = onClick,
                            shape = RoundedCornerShape(50),
                            color = Moss.copy(alpha = 0.16f),
                            border = BorderStroke(0.8.dp, Moss.copy(alpha = 0.4f)),
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Moss, modifier = Modifier.size(15.dp))
                                Text("پرداخت شده", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Moss)
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
                                    .bounceClick(minScale = 0.90f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("پرداخت این قسط", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            title = "حذف قسط «${item.title}»",
            message = "این قسط و تمام یادآوری‌های مرتبط با آن حذف خواهند شد. آیا مطمئن هستید؟",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}