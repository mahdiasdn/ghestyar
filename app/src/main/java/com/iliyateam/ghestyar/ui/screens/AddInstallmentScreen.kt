// ═══ ui/screens/AddInstallmentScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentScreen(
    editingItem: Installment? = null,
    onBack: () -> Unit,
    onSave: (
        title: String,
        amount: Long,
        due: JalaliDate,
        sessions: Int,
        colorIndex: Int,
        category: String,
        remind: Boolean,
        note: String
    ) -> Unit
) {
    BackHandler(onBack = onBack)

    var title by rememberSaveable {
        mutableStateOf(editingItem?.title ?: "")
    }
    var amountDigits by rememberSaveable {
        mutableStateOf(editingItem?.amount?.toString() ?: "")
    }
    var due by rememberSaveable {
        mutableStateOf(
            editingItem?.let { LocalDate.ofEpochDay(it.dueEpochDay).toJalali() }
                ?: JalaliDate.today().plusMonths(1)
        )
    }
    var sessions by rememberSaveable {
        mutableIntStateOf(editingItem?.totalSessions ?: 12)
    }
    var category by rememberSaveable {
        mutableStateOf(editingItem?.category ?: "bank")
    }
    var remind by rememberSaveable {
        mutableStateOf(editingItem?.remind ?: true)
    }
    var note by rememberSaveable {
        mutableStateOf(editingItem?.note ?: "")
    }
    var selectedCycle by rememberSaveable { mutableStateOf("monthly") }

    var showPicker by remember { mutableStateOf(false) }

    val amount = amountDigits.toLongOrNull() ?: 0L
    val isEditMode = editingItem != null
    val isValid = title.isNotBlank() && amount > 0L && sessions > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // نوار بالا با دکمه بستن مطابق عکس ۳
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                if (isEditMode) "ویرایش طرح قسط" else "افزودن طرح جدید",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.size(36.dp))
        }

        // بدنه فرم اسکرول‌شونده تمیز و مدرن
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ۱. فیلد عنوان طرح
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان طرح") },
                placeholder = { Text("مثلاً لپ‌تاپ ایسوس، قسط خودرو...") },
                leadingIcon = {
                    Icon(Icons.Outlined.Tag, null, tint = Moss, modifier = Modifier.size(20.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ۲. انتخاب دسته‌بندی با چیپ‌های کپسولی
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "دسته‌بندی موضوعی",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(InstallmentCategories.list) { cat ->
                        val isSelected = category == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat.id },
                            leadingIcon = { Text(cat.emoji, fontSize = 13.sp) },
                            label = { Text(cat.title, fontSize = 11.sp) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Moss,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ۳. فیلد مبلغ هر قسط
            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v ->
                    amountDigits = v.filter { it.isDigit() }.take(12)
                },
                label = { Text("مبلغ هر قسط (تومان)") },
                placeholder = { Text("مثلاً ۲,۰۴۰,۰۰۰") },
                leadingIcon = {
                    Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Moss, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ۴. تعداد اقساط
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = CircleShape, color = MintSoft, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#", fontWeight = FontWeight.Bold, color = Moss, fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text("تعداد اقساط", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sessions.faDigits()} قسط", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Moss)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepperBtn(
                            icon = Icons.Rounded.Remove,
                            enabled = sessions > 1,
                            onClick = { sessions-- }
                        )
                        StepperBtn(
                            icon = Icons.Rounded.Add,
                            enabled = sessions < 360,
                            onClick = { sessions++ }
                        )
                    }
                }
            }

            // ۵. تاریخ اولین قسط (مطابق عکس ۳)
            Card(
                onClick = { showPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null, tint = Moss, modifier = Modifier.size(22.dp))
                        Column {
                            Text("تاریخ اولین قسط", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                due.toLocalDate().formatJalali(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MintSoft.copy(alpha = 0.8f)
                    ) {
                        Text(
                            "تغییر تاریخ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Moss,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ۶. سوییچر دوره پرداخت (ماهانه / هفتگی / سفارشی - عکس ۳)
            val isDark = isSystemInDarkTheme()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("دوره پرداخت", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val cycles = listOf("monthly" to "ماهانه", "weekly" to "هفتگی", "custom" to "سفارشی")
                        cycles.forEach { (id, label) ->
                            val isSelected = selectedCycle == id
                            Surface(
                                onClick = { selectedCycle = id },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) (if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MintSoft) else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, if (isDark) MossLight.copy(alpha = 0.5f) else Moss.copy(alpha = 0.3f)) else null,
                                modifier = Modifier.weight(1f).bounceClick(minScale = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Rounded.Check, null, tint = if (isDark) MossLight else Moss, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (isDark) MossLight else Moss) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ۷. کادر محاسبات زنده مبلغ ماهانه
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFDAE2FF).copy(alpha = 0.65f),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f) else Color(0xFFB8C6EA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("قسط ماهانه شما:", fontSize = 11.sp, color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF22304C), fontWeight = FontWeight.Medium)
                    Text(
                        "${amount.money()} تومان",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MossLight else Color(0xFF0E1A36)
                    )
                    Text(
                        "مبلغ کل در ${sessions.faDigits()} قسط: ${(amount * sessions).money()} تومان",
                        fontSize = 10.sp,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF3A4764)
                    )
                }
            }

            // ۸. سوییچ وضعیت یادآوری
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "یادآوری خودکار با اعلان 🔔",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "اعلان در ۳ روز قبل، ۱ روز قبل و صبح روز سررسید",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = remind,
                        onCheckedChange = { remind = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Moss)
                    )
                }
            }

            // ۹. یادداشت اختیاری
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("یادداشت یا شماره قرارداد (اختیاری)") },
                placeholder = { Text("مثلاً شماره کارت، کد پیگیری...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
        }

        // دکمه‌های پایینی ۲۸dp (ذخیره طرح + انصراف - مطابق عکس ۳)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onSave(title, amount, due, sessions, 0, category, remind, note)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text(
                    if (isEditMode) "ذخیره تغییرات" else "ذخیره طرح",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Text("انصراف", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showPicker) {
        JalaliDatePickerSheet(
            initial = due,
            onDismiss = { showPicker = false },
            onConfirm = {
                due = it
                showPicker = false
            }
        )
    }
}

@Composable
private fun StepperBtn(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) MintSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) Moss else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}