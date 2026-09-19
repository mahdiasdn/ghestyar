// ═══ ui/screens/JalaliDatePickerSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliDatePickerSheet(
    initial: JalaliDate,
    onConfirm: (JalaliDate) -> Unit,
    onDismiss: () -> Unit
) {
    var jy by remember { mutableIntStateOf(initial.jy.coerceIn(1390, 1420)) }
    var jm by remember { mutableIntStateOf(initial.jm.coerceIn(1, 12)) }
    var jd by remember { mutableIntStateOf(initial.jd.coerceIn(1, 31)) }

    val maxDay = Jalali.monthLength(jy, jm)
    val safeDay = jd.coerceIn(1, maxDay)
    if (safeDay != jd) {
        jd = safeDay
    }

    val selected = remember(jy, jm, jd) { JalaliDate(jy, jm, jd) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // هدر پیش‌نمایش تاریخ
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "انتخاب تاریخ سررسید",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    selected.toLocalDate().formatJalaliWithWeekday(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Moss
                )
            }

            // میانبرهای سریع و خلوت
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SuggestionChip(
                        onClick = {
                            val next = JalaliDate.startOfNextMonth()
                            jy = next.jy
                            jm = next.jm
                            jd = next.jd
                        },
                        label = { Text("سر ماه آینده", style = MaterialTheme.typography.labelMedium) }
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val next = JalaliDate.today().plusDays(15)
                            jy = next.jy
                            jm = next.jm
                            jd = next.jd
                        },
                        label = { Text("۱۵ روز بعد", style = MaterialTheme.typography.labelMedium) }
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val next = JalaliDate.today().plusMonths(1)
                            jy = next.jy
                            jm = next.jm
                            jd = next.jd
                        },
                        label = { Text("یک ماه بعد", style = MaterialTheme.typography.labelMedium) }
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val today = JalaliDate.today()
                            jy = today.jy
                            jm = today.jm
                            jd = today.jd
                        },
                        label = { Text("امروز", style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // کنترلرهای روان ۳ ستونه: روز | ماه | سال
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ستون روز
                DateColumnSpinner(
                    label = "روز",
                    valueText = jd.faDigits(),
                    onIncrement = {
                        jd = if (jd < maxDay) jd + 1 else 1
                    },
                    onDecrement = {
                        jd = if (jd > 1) jd - 1 else maxDay
                    }
                )

                // ستون ماه
                DateColumnSpinner(
                    label = "ماه",
                    valueText = Jalali.months[jm - 1],
                    onIncrement = {
                        jm = if (jm < 12) jm + 1 else 1
                    },
                    onDecrement = {
                        jm = if (jm > 1) jm - 1 else 12
                    }
                )

                // ستون سال
                DateColumnSpinner(
                    label = "سال",
                    valueText = jy.faDigits(),
                    onIncrement = {
                        if (jy < 1420) jy++
                    },
                    onDecrement = {
                        if (jy > 1390) jy--
                    }
                )
            }

            Spacer(Modifier.height(6.dp))

            // دکمه تایید
            Button(
                onClick = { onConfirm(selected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text(
                    "تأیید سررسید: ${selected.format()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DateColumnSpinner(
    label: String,
    valueText: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "افزایش", tint = Moss)
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.widthIn(min = 80.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    valueText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = "کاهش", tint = Moss)
        }
    }
}