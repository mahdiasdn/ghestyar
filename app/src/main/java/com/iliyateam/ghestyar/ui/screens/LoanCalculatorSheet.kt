// ═══ ui/screens/LoanCalculatorSheet.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.Coral
import com.iliyateam.ghestyar.ui.theme.GoldVip
import com.iliyateam.ghestyar.ui.theme.Moss
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.money
import kotlin.math.pow

data class LoanRatePreset(
    val title: String,
    val rate: Double,
    val description: String
)

val commonLoanRates = listOf(
    LoanRatePreset("۴٪ قرض‌الحسنه", 4.0, "وام‌های ازدواج، اشتغال و رسالت"),
    LoanRatePreset("۱۸٪ بانکی قدیم", 18.0, "تسهیلات مصوب قبلی"),
    LoanRatePreset("۲۳٪ مصوب جدید", 23.0, "تسهیلات بانکی و خرید کالا"),
    LoanRatePreset("دلخواه", 0.0, "نرخ سفارشی")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorSheet(
    onDismiss: () -> Unit,
    onAddAsInstallment: (title: String, monthlyPayment: Long, sessions: Int) -> Unit
) {
    var loanAmountDigits by rememberSaveable { mutableStateOf("100000000") } // 100 میلیون پیش‌فرض
    var selectedPreset by remember { mutableStateOf(commonLoanRates[2]) } // ۲۳٪
    var customRateText by rememberSaveable { mutableStateOf("23") }
    var monthsText by rememberSaveable { mutableStateOf("36") }
    var title by rememberSaveable { mutableStateOf("وام بانکی") }

    val principal = loanAmountDigits.toLongOrNull() ?: 0L
    val months = monthsText.toIntOrNull()?.coerceIn(1, 360) ?: 12
    val rate = if (selectedPreset.rate > 0.0) selectedPreset.rate else (customRateText.toDoubleOrNull() ?: 0.0)

    // محاسبه بر اساس فرمول بانک مرکزی ایران
    val calculation = remember(principal, months, rate) {
        calculateBankLoan(principal, rate, months)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // هدر ماشین‌حساب
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Moss.copy(alpha = 0.14f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Calculate, null, tint = Moss, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Text(
                        "ماشین‌حساب جامع اقساط و سود وام",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "محاسبه بر اساس فرمول رسمی بانک مرکزی ایران",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ۱. مبلغ کل وام
            OutlinedTextField(
                value = if (loanAmountDigits.isEmpty()) "" else principal.money(),
                onValueChange = { v -> loanAmountDigits = v.filter { it.isDigit() }.take(14) },
                label = { Text("مبلغ کل وام درخواستی (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // کلیدهای سریع مبلغ
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(50_000_000L, 100_000_000L, 200_000_000L, 300_000_000L, 500_000_000L)) { quickVal ->
                    Surface(
                        onClick = { loanAmountDigits = quickVal.toString() },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bounceClick(minScale = 0.94f)
                    ) {
                        Text(
                            "${(quickVal / 1_000_000).faDigits()} م تومان",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ۲. انتخاب نرخ سود
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "نرخ سود سالانه وام",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonLoanRates) { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset.title, fontSize = 11.sp) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Moss,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (selectedPreset.rate == 0.0) {
                    OutlinedTextField(
                        value = customRateText,
                        onValueChange = { customRateText = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                        label = { Text("درصد سود دلخواه (مثلاً ۲۱.۵)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ۳. مدت بازپرداخت (ماه)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("تعداد ماه بازپرداخت") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام وام") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // ۴. کارت نتایج محاسبه Bento
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, Moss.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
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
                        Text("مبلغ هر قسط ماهانه:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${calculation.monthlyPayment.money()} تومان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Moss
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کل سود پرداختی به بانک:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${calculation.totalInterest.money()} تومان", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع کل بازپرداخت (اصل + سود):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${calculation.totalRepayment.money()} تومان", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldVip)
                    }
                }
            }

            // ۵. دکمه افزودن مستقیم به اقساط من
            Button(
                onClick = {
                    if (calculation.monthlyPayment > 0) {
                        onAddAsInstallment(title.ifBlank { "وام $rate٪" }, calculation.monthlyPayment, months)
                        onDismiss()
                    }
                },
                enabled = calculation.monthlyPayment > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .bounceClick(minScale = 0.94f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Icon(Icons.Rounded.AddCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("افزودن این قسط به لیست اقساط من ✅", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

data class LoanCalculationResult(
    val monthlyPayment: Long,
    val totalInterest: Long,
    val totalRepayment: Long
)

fun calculateBankLoan(principal: Long, annualRatePercent: Double, months: Int): LoanCalculationResult {
    if (principal <= 0L || months <= 0) return LoanCalculationResult(0L, 0L, 0L)
    if (annualRatePercent <= 0.0) {
        val monthly = principal / months
        return LoanCalculationResult(monthly, 0L, principal)
    }

    // فرمول رسمی بانک مرکزی ایران
    val r = (annualRatePercent / 100.0) / 12.0
    val power = (1.0 + r).pow(months.toDouble())
    val monthlyPayment = (principal * r * power) / (power - 1.0)
    val monthlyLong = monthlyPayment.toLong()
    val totalRepayment = monthlyLong * months
    val totalInterest = (totalRepayment - principal).coerceAtLeast(0L)

    return LoanCalculationResult(
        monthlyPayment = monthlyLong,
        totalInterest = totalInterest,
        totalRepayment = totalRepayment
    )
}
