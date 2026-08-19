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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Check
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val due = remember(item.dueEpochDay) { LocalDate.ofEpochDay(item.dueEpochDay) }
    val daysLeft = remember(due, today) {
        ChronoUnit.DAYS.between(today, due)
    }
    val ringProgress = remember(item.startEpochDay, due, daysLeft) {
        val start = LocalDate.ofEpochDay(item.startEpochDay)
        val totalPeriod = ChronoUnit.DAYS.between(start, due).coerceAtLeast(1)
        ((totalPeriod - daysLeft).toFloat() / totalPeriod).coerceIn(0f, 1f)
    }
    val tint = remember(daysLeft) { urgencyColor(daysLeft) }
    val category = remember(item.category) { InstallmentCategories.get(item.category) }
    val remainingSessions = remember(item.totalSessions, item.paidSessions) {
        (item.totalSessions - item.paidSessions).coerceAtLeast(0)
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
            delay(600)
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (daysLeft < 0) Coral.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون اسکویرکل دسته‌بندی
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft.copy(alpha = 0.7f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(category.emoji, fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.width(12.dp))

                // متن و جزییات عنوان و مبلغ
                Column(Modifier.weight(1f)) {
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
                            overflow = TextOverflow.Ellipsis
                        )

                        // بج کپسولی تعداد اقساط مانده
                        if (item.totalSessions > 1) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    "قسط مانده ${remainingSessions.faDigits()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) MossLight else Moss,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ماهانه ${item.amount.money()} تومان",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MossLight else Moss
                        )

                        Text(
                            due.relativeLabel(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = tint
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // دکمه پرداخت + منوی بیشتر
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ConfettiBurst(
                            active = celebrating,
                            color = Moss,
                            modifier = Modifier.size(60.dp)
                        )

                        Surface(
                            onClick = ::triggerPay,
                            shape = CircleShape,
                            color = if (celebrating) Moss else if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                                .bounceClick(minScale = 0.88f)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "پرداخت",
                                    tint = if (celebrating) Color.White else if (isDark) MossLight else Moss,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { menuOpen = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = "بیشتر",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("مشاهده جزئیات", fontSize = 12.sp) },
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
            }

            // نوار باریک پیشرفت تونال بر اساس M3 Expressive
            if (item.totalSessions > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${((item.overallProgress * 100).toInt()).faDigits()}٪",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MossLight else Moss
                    )

                    LinearProgressIndicator(
                        progress = { item.overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isDark) MossLight else Moss,
                        trackColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                Button(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral)
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("انصراف")
                }
            },
            title = { Text("حذف «${item.title}»", fontWeight = FontWeight.Bold) },
            text = { Text("این قسط و تمام یادآوری‌های آن حذف خواهند شد.") }
        )
    }
}