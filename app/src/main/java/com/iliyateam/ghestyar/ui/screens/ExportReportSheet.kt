// ═══ ui/screens/ExportReportSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

enum class ExportTimeFilter(val title: String, val emoji: String) {
    ALL("همه اقساط", "🌟"),
    THIS_MONTH("ماه جاری", "🗓️"),
    THIS_YEAR("سال جاری", "📆"),
    ACTIVE_ONLY("فقط فعال", "⏳"),
    COMPLETED_ONLY("فقط تسویه‌شده", "✅")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportSheet(
    allInstallments: List<Installment>,
    onDismiss: () -> Unit,
    onSaveDocument: (isPdf: Boolean, items: List<Installment>, titleScope: String) -> Unit
) {
    val context = LocalContext.current
    var isPdfFormat by remember { mutableStateOf(true) } // true: PDF, false: Excel
    var selectedFilter by remember { mutableStateOf(ExportTimeFilter.ALL) }

    val todayJalali = remember { JalaliDate.today() }
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    // فیلتر کردن اقساط بر اساس بازه انتخابی
    val filteredItems = remember(allInstallments, selectedFilter) {
        when (selectedFilter) {
            ExportTimeFilter.ALL -> allInstallments.sortedBy { it.dueEpochDay }
            ExportTimeFilter.THIS_MONTH -> {
                allInstallments.filter { item ->
                    val jDate = LocalDate.ofEpochDay(item.dueEpochDay).toJalali()
                    jDate.jy == todayJalali.jy && jDate.jm == todayJalali.jm
                }.sortedBy { it.dueEpochDay }
            }
            ExportTimeFilter.THIS_YEAR -> {
                allInstallments.filter { item ->
                    val jDate = LocalDate.ofEpochDay(item.dueEpochDay).toJalali()
                    jDate.jy == todayJalali.jy
                }.sortedBy { it.dueEpochDay }
            }
            ExportTimeFilter.ACTIVE_ONLY -> allInstallments.filter { !it.isPaid }.sortedBy { it.dueEpochDay }
            ExportTimeFilter.COMPLETED_ONLY -> allInstallments.filter { it.isPaid }.sortedBy { it.dueEpochDay }
        }
    }

    val totalSum = remember(filteredItems) { filteredItems.sumOf { it.totalAmount } }
    val paidSum = remember(filteredItems) { filteredItems.sumOf { it.paidAmount } }
    val remainingSum = remember(filteredItems) { filteredItems.sumOf { it.remainingAmount } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ۱. سربرگ شیت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = CircleShape, color = Moss.copy(alpha = 0.14f), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📑", fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text("خروجی و گزارش‌گیری رسمی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("خروجی تفکیک‌شده در قالب PDF و اکسل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "بستن")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))

            // ۲. انتخاب فرمت فایل خروجی (PDF یا Excel)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("۱. قالب فایل خروجی را انتخاب کنید:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // کارت PDF رسمی
                    Surface(
                        onClick = { isPdfFormat = true },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isPdfFormat) Moss.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (isPdfFormat) BorderStroke(1.5.dp, Moss) else null,
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(minScale = 0.96f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📑", fontSize = 28.sp)
                            Text(
                                "سند رسمی PDF",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPdfFormat) Moss else MaterialTheme.colorScheme.onSurface
                            )
                            Text("A4 مصور با جدول و سربرگ", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // کارت اکسل رسمی XLSX
                    Surface(
                        onClick = { isPdfFormat = false },
                        shape = RoundedCornerShape(18.dp),
                        color = if (!isPdfFormat) GoldVip.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (!isPdfFormat) BorderStroke(1.5.dp, GoldVip) else null,
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(minScale = 0.96f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📊", fontSize = 28.sp)
                            Text(
                                "اکسل رسمی (.xlsx) ⭐",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isPdfFormat) GoldVip else MaterialTheme.colorScheme.onSurface
                            )
                            Text("فرمت مایکروسافت اکسل، رنگی و فرمول‌دار", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ۳. فیلتر بازه زمانی و دسته‌بندی
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("۲. بازه زمانی و فیلتر اقساط:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(ExportTimeFilter.entries) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(filter.emoji, fontSize = 13.sp)
                                Text(
                                    filter.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ۴. کارت پیش‌نمایش زنده اقساط انتخاب‌شده
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("پیش‌نمایش داده‌های گزارش:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(shape = RoundedCornerShape(50), color = Moss.copy(alpha = 0.12f)) {
                            Text("${filteredItems.size.faDigits()} قسط منتخب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Moss, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("مجموع تعهدات:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${totalSum.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("پرداخت‌شده:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${paidSum.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Moss)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("مانده بدهی:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${remainingSum.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Coral)
                        }
                    }
                }
            }

            // ۵. دکمه‌های اقدام و صدور گزارش
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // دکمه اشتراک‌گذاری مستقیم
                OutlinedButton(
                    onClick = {
                        if (filteredItems.isEmpty()) {
                            Toast.makeText(context, "هیچ قسطی در این فیلتر وجود ندارد!", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        try {
                            Exporter.shareReportFile(context, filteredItems, isPdfFormat, selectedFilter.title)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .bounceClick(minScale = 0.96f)
                ) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("اشتراک مستقیم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // دکمه ذخیره فایل در حافظه
                Button(
                    onClick = {
                        if (filteredItems.isEmpty()) {
                            Toast.makeText(context, "هیچ قسطی در این فیلتر وجود ندارد!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSaveDocument(isPdfFormat, filteredItems, selectedFilter.title)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPdfFormat) Moss else GoldVip),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .bounceClick(minScale = 0.96f)
                ) {
                    Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp), tint = if (isPdfFormat) Color.White else Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "ذخیره در حافظه",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPdfFormat) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
