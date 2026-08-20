// ═══ ui/screens/InstallmentDetailSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.ProgressRing
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentDetailSheet(
    item: Installment,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onPayCurrent: () -> Unit,
    onUnmarkPaid: () -> Unit = {},
    onDelete: () -> Unit
) {
    val category = InstallmentCategories.get(item.category)
    val today = LocalDate.now()
    val due = LocalDate.ofEpochDay(item.dueEpochDay)
    val start = LocalDate.ofEpochDay(item.startEpochDay)
    val daysLeft = ChronoUnit.DAYS.between(today, due)
    val tint = urgencyColor(daysLeft)
    val remainingSessions = (item.totalSessions - item.paidSessions).coerceAtLeast(0)

    var showReceiptCard by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ۱. سربرگ عنوان قسط و آیکون دسته‌بندی
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(category.emoji, fontSize = 24.sp)
                        }
                    }

                    Column {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                category.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.destination.isNotBlank()) {
                                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outlineVariant)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft
                                ) {
                                    Text(
                                        "🏦 ${item.destination}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) MossLight else Moss,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, "ویرایش", tint = if (isDark) MossLight else Moss)
                    }
                    IconButton(onClick = { showReceiptCard = true }) {
                        Icon(Icons.Outlined.Share, "اشتراک رسید", tint = if (isDark) MossLight else Moss)
                    }
                }
            }

            // ۲. حلقه پیشرفت گرافیکی به همراه خلاصه مالی
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProgressRing(
                        progress = item.overallProgress,
                        tint = if (isDark) MossLight else Moss,
                        strokeWidth = 8.dp,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${((item.overallProgress * 100).toInt()).faDigits()}٪",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MossLight else Moss
                            )
                            Text("پیشرفت", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("مبلغ هر قسط", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${item.amount.money()} تومان",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MossLight else Moss
                        )
                        Spacer(Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft.copy(alpha = 0.8f)
                        ) {
                            Text(
                                "قسط پرداخت‌شده: ${item.paidSessions.faDigits()} از ${item.totalSessions.faDigits()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) MossLight else MossDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            "شروع: ${start.formatJalali()}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ۳. شبکه ۳ ستونه Bento Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BentoStatPill(
                    modifier = Modifier.weight(1f),
                    label = "مبلغ کل وام",
                    value = "${item.totalAmount.money()} ت"
                )
                BentoStatPill(
                    modifier = Modifier.weight(1f),
                    label = "تعداد اقساط",
                    value = "${item.totalSessions.faDigits()} ماه"
                )
                BentoStatPill(
                    modifier = Modifier.weight(1f),
                    label = "اقساط مانده",
                    value = "${remainingSessions.faDigits()} قسط",
                    valueColor = if (remainingSessions > 0) Coral else (if (isDark) MossLight else Moss)
                )
            }

            // ۴. برنامه اقساط
            Text(
                "برنامه اقساط و سررسیدها",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (s in 1..item.totalSessions) {
                    val isPaid = s <= item.paidSessions
                    val isCurrent = s == item.paidSessions + 1
                    val isLastPaid = s == item.paidSessions && item.paidSessions > 0
                    val sessionDue = start.plusMonths((s - 1).toLong())

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = when {
                            isCurrent -> if (isDark) MaterialTheme.colorScheme.primaryContainer else MintSoft.copy(alpha = 0.85f)
                            isPaid -> MaterialTheme.colorScheme.surfaceContainerLow
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCurrent) (if (isDark) MossLight.copy(alpha = 0.5f) else Moss.copy(alpha = 0.4f))
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isPaid -> if (isDark) MossLight else Moss
                                        isCurrent -> if (isDark) MossLight.copy(alpha = 0.25f) else Moss.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    },
                                    border = if (!isPaid && !isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isPaid) {
                                            Icon(Icons.Rounded.Check, null, tint = if (isDark) Color(0xFF003739) else Color.White, modifier = Modifier.size(16.dp))
                                        } else if (isCurrent) {
                                            Text("${s.faDigits()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) MossLight else Moss)
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "قسط ${s.faDigits()} — ${sessionDue.formatJalali()}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) (if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MossDeep) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isLastPaid) {
                                    Surface(
                                        onClick = {
                                            onUnmarkPaid()
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = Coral.copy(alpha = 0.14f),
                                        border = BorderStroke(0.8.dp, Coral.copy(alpha = 0.4f)),
                                        modifier = Modifier.bounceClick(minScale = 0.92f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null, tint = Coral, modifier = Modifier.size(12.dp))
                                            Text("لغو پرداخت", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral)
                                        }
                                    }
                                }

                                Text(
                                    "${item.amount.money()} ت",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Moss else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ۵. یادداشت و وضعیت یادآوری
            if (item.note.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📝 یادداشت: ${item.note}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ۶. دکمه‌های عملیات ۲۸dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.paidSessions < item.totalSessions) {
                    Button(
                        onClick = {
                            onPayCurrent()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .bounceClick(minScale = 0.96f)
                    ) {
                        Text("پرداخت این قسط ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }

                if (item.paidSessions > 0) {
                    OutlinedButton(
                        onClick = {
                            onUnmarkPaid()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Coral.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral),
                        modifier = Modifier
                            .weight(if (item.paidSessions >= item.totalSessions) 1f else 0.9f)
                            .height(52.dp)
                            .bounceClick(minScale = 0.96f)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, null, modifier = Modifier.size(15.dp), tint = Coral)
                        Spacer(Modifier.width(4.dp))
                        Text("بازگردانی پرداخت ↩️", color = Coral, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                OutlinedButton(
                    onClick = { showConfirmDelete = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral),
                    border = BorderStroke(1.dp, Coral.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(52.dp)
                        .bounceClick(minScale = 0.94f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Delete, "حذف", tint = Coral, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showConfirmDelete) {
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف قسط «${item.title}»",
            message = "آیا از حذف این قسط و سوابق آن اطمینان دارید؟ این عملیات غیرقابل بازگشت است.",
            onConfirm = {
                showConfirmDelete = false
                onDelete()
                onDismiss()
            },
            onDismiss = { showConfirmDelete = false }
        )
    }

    if (showReceiptCard) {
        com.iliyateam.ghestyar.ui.components.InstallmentReceiptCardDialog(
            item = item,
            onDismiss = { showReceiptCard = false }
        )
    }
}

@Composable
private fun BentoStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
