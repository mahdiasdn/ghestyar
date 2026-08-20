// ═══ ui/screens/AnalyticsTab.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.ProgressRing
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun AnalyticsTab(
    stats: FinancialStats,
    activeInstallments: List<Installment>,
    historyInstallments: List<Installment>,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val context = LocalContext.current

    // محاسبه امتیاز سلامت اعتباری (Score 100..1000)
    val healthScore = remember(stats, activeInstallments, historyInstallments) {
        var score = 500

        // ۱. نمره خوش‌حسابی بر اساس اقساط به موقع
        if (stats.totalInstallmentsCount > 0) {
            val onTimeRatio = (stats.totalInstallmentsCount - stats.overdueCount).toFloat() / stats.totalInstallmentsCount
            score += (onTimeRatio * 250).toInt()
        } else {
            score += 250
        }

        // ۲. جریمه معوقات
        score -= (stats.overdueCount * 90)

        // ۳. نمره شاخص DTI (نسبت اقساط به درآمد)
        if (stats.totalMonthlyIncome > 0) {
            val dti = stats.monthlyCommitment.toFloat() / stats.totalMonthlyIncome
            when {
                dti <= 0.30f -> score += 150
                dti <= 0.50f -> score += 60
                else -> score -= 80
            }
        } else {
            score += 60
        }

        // ۴. نمره پیشرفت تسویه بدهی
        val totalAmountAll = stats.totalPaidAllTime + stats.totalActiveDebt
        if (totalAmountAll > 0) {
            val paidRatio = stats.totalPaidAllTime.toFloat() / totalAmountAll
            score += (paidRatio * 100).toInt()
        }

        score.coerceIn(150, 1000)
    }

    val scoreGrade = remember(healthScore) {
        when {
            healthScore >= 850 -> "A+ (ممتاز 🌟)" to Moss
            healthScore >= 750 -> "A (خیلی خوب ✨)" to Moss
            healthScore >= 620 -> "B (متوسط ⚡)" to GoldVip
            else -> "C (نیازمند توجه ⚠️)" to Coral
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ۱. 🏆 کارنامه هوش مالی و امتیاز اعتباری (Smart Financial Health & Credit Score)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = scoreGrade.second.copy(alpha = 0.14f), modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🏆", fontSize = 16.sp)
                                }
                            }
                            Column {
                                Text("کارنامه سلامت مالی و رتبه اعتباری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("محاسبه هوشمند بر اساس رفتار تسویه", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // دکمه اشتراک‌گذاری کارنامه
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    appendLine("📊 کارنامه سلامت مالی من در قسط‌یار:")
                                    appendLine("🏆 امتیاز اعتباری: ${healthScore.faDigits()} از ۱۰۰۰ (${scoreGrade.first})")
                                    appendLine("💰 کل تسویه شده: ${stats.totalPaidAllTime.money()} تومان")
                                    appendLine("🎯 تعهد ماهانه: ${stats.monthlyCommitment.money()} تومان")
                                    appendLine("✅ تعداد اقساط موفق: ${stats.completedCount.faDigits()} قسط")
                                    appendLine("📱 مدیریت هوشمند اقساط با اپلیکیشن قسط‌یار")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری کارنامه مالی"))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Share, "اشتراک‌گذاری", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // گیج متر امتیاز
                        ProgressRing(
                            progress = healthScore / 1000f,
                            tint = scoreGrade.second,
                            strokeWidth = 9.dp,
                            modifier = Modifier.size(86.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    healthScore.faDigits(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreGrade.second
                                )
                                Text("از ۱۰۰۰", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("رتبه اعتباری:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(scoreGrade.first, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scoreGrade.second)
                            }
                            Text(
                                when {
                                    healthScore >= 850 -> "انضباط مالی شما عالی است. جریان نقدینگی و پرداخت‌هایتان در بالاترین سطح امنیت قرار دارد."
                                    healthScore >= 750 -> "وضعیت پرداخت‌ها بسیار مناسب است. با حفظ این روند به رتبه ممتاز خواهید رسید."
                                    healthScore >= 620 -> "وضعیت تعهدات شما تحت کنترل است، اما مراقب افزایش همزمان اقساط باشید."
                                    else -> "اقساط معوق یا بار تعهدات سنگین است. پیشنهاد می‌شود از استراتژی تسویه زودهنگام استفاده کنید."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // چیپ‌های ۴ گانه وضعیت
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HealthMetricChip(
                            modifier = Modifier.weight(1f),
                            title = "تسویه به موقع",
                            value = "${stats.overallHealthPercentage.toInt().faDigits()}٪",
                            isGood = stats.overdueCount == 0
                        )
                        HealthMetricChip(
                            modifier = Modifier.weight(1f),
                            title = "معوقات",
                            value = "${stats.overdueCount.faDigits()} مورد",
                            isGood = stats.overdueCount == 0
                        )
                        HealthMetricChip(
                            modifier = Modifier.weight(1f),
                            title = "تسویه‌شده‌ها",
                            value = "${stats.completedCount.faDigits()} وام",
                            isGood = stats.completedCount > 0
                        )
                    }
                }
            }
        }

        // ۲. ⏳ روزشمار و تایمر آزادی مالی (Debt-Free Freedom Countdown)
        item {
            val furthestEpochDay = remember(activeInstallments) {
                if (activeInstallments.isEmpty()) null
                else {
                    activeInstallments.maxOfOrNull { item ->
                        val remainingMonths = (item.totalSessions - item.paidSessions).coerceAtLeast(1)
                        item.dueEpochDay + (remainingMonths - 1) * 30L
                    }
                }
            }

            val daysUntilFreedom = remember(furthestEpochDay) {
                furthestEpochDay?.let { targetDay ->
                    val today = LocalDate.now().toEpochDay()
                    (targetDay - today).coerceAtLeast(0L)
                } ?: 0L
            }

            val freedomDateJalali = remember(furthestEpochDay) {
                furthestEpochDay?.let { LocalDate.ofEpochDay(it).toJalali().format() } ?: "هم‌اکنون"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, Moss.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⏳", fontSize = 20.sp)
                            Text("روزشمار آزادی مالی و تسویه کامل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Moss)
                        }
                        Surface(shape = RoundedCornerShape(50), color = Moss.copy(alpha = 0.15f)) {
                            Text(
                                if (activeInstallments.isEmpty()) "آزاد و رها ✨" else "${daysUntilFreedom.faDigits()} روز دیگر",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Moss,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (activeInstallments.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Moss.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🎉", fontSize = 28.sp)
                                Column {
                                    Text("تبریک! شما در حال حاضر هیچ قسط فعالی ندارید.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Moss)
                                    Text("۱۰۰٪ درآمدهای شما بدون تعهد بدهی در اختیار خودتان است.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("تاریخ پیش‌بینی اتمام تمام اقساط:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(freedomDateJalali, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("مانده کل تعهدات:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${stats.totalActiveDebt.money()} تومان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Coral)
                            }
                        }

                        // نوار پیشرفت تسویه
                        val totalPaid = stats.totalPaidAllTime
                        val totalAll = totalPaid + stats.totalActiveDebt
                        val progressRatio = if (totalAll > 0) (totalPaid.toFloat() / totalAll).coerceIn(0f, 1f) else 1f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("پیشرفت مسیر تسویه کل وام‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(progressRatio * 100).toInt().faDigits()}٪ تسویه شده", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Moss)
                            }
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Moss,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ۳. 📊 نمودار پیش‌بینی بار تعهدات ۶ ماه آینده (Future 6-Month Commitment Projection)
        item {
            FutureCommitmentProjectionSection(activeInstallments = activeInstallments)
        }

        // ۴. ⚡ شبیه‌ساز تسویه زودهنگام و ماشین زمان اقساط (Early Payoff & Snowball Simulator)
        item {
            EarlyPayoffSimulatorSection(
                activeInstallments = activeInstallments,
                stats = stats,
                isPremium = isPremium,
                onOpenPremium = onOpenPremium
            )
        }

        // ۵. 🏦 تفکیک پیشرفته مقاصد پرداخت (بانک‌ها vs اسنپ‌پی/BNPL vs خریدهای اقساطی)
        item {
            ProviderDistributionSection(
                activeInstallments = activeInstallments,
                stats = stats
            )
        }

        // ۶. 🎖️ تالار افتخارات و نشان‌های انضباط مالی (Gamification Badges)
        item {
            FinancialBadgesSection(
                stats = stats,
                healthScore = healthScore,
                historyCount = historyInstallments.size
            )
        }
    }
}

/**
 * چیپ نمایش یک متریک نمره سلامت
 */
@Composable
private fun HealthMetricChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isGood: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isGood) Moss.copy(alpha = 0.10f) else Coral.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGood) Moss else Coral)
        }
    }
}

/**
 * نمودار پیش‌بینی بار تعهدات ۶ ماه آینده
 */
@Composable
private fun FutureCommitmentProjectionSection(activeInstallments: List<Installment>) {
    val monthsProjection = remember(activeInstallments) {
        val today = JalaliDate.today()
        (0..5).map { offset ->
            var monthNum = today.jm + offset
            var yearNum = today.jy
            while (monthNum > 12) {
                monthNum -= 12
                yearNum += 1
            }
            val monthName = Jalali.months[monthNum - 1]

            // محاسبه مبلغ تعهد در این ماه
            val monthAmount = activeInstallments.filter { item ->
                val remainingSessions = item.totalSessions - item.paidSessions
                offset < remainingSessions
            }.sumOf { it.amount }

            Triple(monthName, monthAmount, offset == 0)
        }
    }

    val maxAmount = remember(monthsProjection) {
        monthsProjection.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊", fontSize = 18.sp)
                    Text("پیش‌بینی بار تعهدات ۶ ماه آینده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("تومان", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (activeInstallments.isEmpty()) {
                Text("اقساط فعالی برای پیش‌بینی ماه‌های آینده وجود ندارد.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthsProjection.forEach { (monthName, amount, isCurrentMonth) ->
                        val barRatio = (amount.toFloat() / maxAmount).coerceIn(0.12f, 1f)
                        val animatedBarHeight by animateFloatAsState(
                            targetValue = barRatio,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                            label = "BarHeightAnim"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (amount > 0) amount.compactMoney() else "-",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentMonth) Moss else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight(animatedBarHeight * 0.75f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (isCurrentMonth) Moss
                                        else if (amount == maxAmount) GoldVip
                                        else MaterialTheme.colorScheme.primaryContainer
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                monthName,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentMonth) Moss else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * شبیه‌ساز زنده تسویه زودهنگام و استراتژی گلوله برفی
 */
@Composable
private fun EarlyPayoffSimulatorSection(
    activeInstallments: List<Installment>,
    stats: FinancialStats,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val validLoans = remember(activeInstallments) {
        activeInstallments.filter { (it.totalSessions - it.paidSessions) > 0 }
    }

    val totalMonthlyCommitment = remember(validLoans) {
        validLoans.sumOf { it.amount }.coerceAtLeast(100_000L)
    }

    var extraMonthlyPay by remember(totalMonthlyCommitment) {
        mutableFloatStateOf((totalMonthlyCommitment * 0.25f).coerceIn(100_000f, 2_000_000f))
    }

    val maxSliderValue = remember(totalMonthlyCommitment) {
        (totalMonthlyCommitment * 2.5f).coerceIn(1_000_000f, 20_000_000f)
    }

    data class PayoffSimResult(
        val normalMonths: Int,
        val acceleratedMonths: Int,
        val savedMonths: Int,
        val normalDateText: String,
        val acceleratedDateText: String
    )

    val simulationResult = remember(extraMonthlyPay, validLoans) {
        if (validLoans.isEmpty()) {
            PayoffSimResult(0, 0, 0, "-", "-")
        } else {
            // ۱. زمان تسویه استاندارد بدون مازاد (بیشترین تعداد اقساط باقیمانده)
            val normalMonths = validLoans.maxOfOrNull {
                (it.totalSessions - it.paidSessions).coerceAtLeast(1)
            } ?: 1

            // ۲. شبیه‌سازی گام‌به‌گام استراتژی گلوله برفی (Snowball Acceleration)
            class DebtLoan(var remainingDebt: Long, val originalMonthly: Long)
            val simLoans = validLoans
                .map { DebtLoan(it.remainingAmount, it.amount) }
                .sortedBy { it.remainingDebt } // اولویت تسویه سریع‌تر با وام‌های سبک‌تر (Snowball)
                .toMutableList()

            var monthsCount = 0
            val extraPay = extraMonthlyPay.toLong()
            val totalMonthlyBudget = validLoans.sumOf { it.amount } + extraPay

            while (simLoans.any { it.remainingDebt > 0 } && monthsCount < 360) {
                monthsCount++
                var availableBudget = totalMonthlyBudget

                // الف: ابتدا پرداخت حداقل قسط ماهانه برای وام‌های باز
                for (loan in simLoans) {
                    if (loan.remainingDebt > 0) {
                        val minDeduct = loan.originalMonthly.coerceAtMost(loan.remainingDebt).coerceAtMost(availableBudget)
                        loan.remainingDebt -= minDeduct
                        availableBudget -= minDeduct
                    }
                }

                // ب: تزریق گلوله برفی کل بودجه مازاد به سبک‌ترین وام فعال تا صفر شود
                for (loan in simLoans) {
                    if (loan.remainingDebt > 0 && availableBudget > 0) {
                        val extraDeduct = availableBudget.coerceAtMost(loan.remainingDebt)
                        loan.remainingDebt -= extraDeduct
                        availableBudget -= extraDeduct
                    }
                }
            }

            val saved = (normalMonths - monthsCount).coerceAtLeast(0)
            val today = JalaliDate.today()
            val normalDate = today.plusMonths(normalMonths)
            val accelDate = today.plusMonths(monthsCount)
            val normalText = "${Jalali.months[normalDate.jm - 1]} ${normalDate.jy.faDigits()}"
            val accelText = "${Jalali.months[accelDate.jm - 1]} ${accelDate.jy.faDigits()}"

            PayoffSimResult(normalMonths, monthsCount, saved, normalText, accelText)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡", fontSize = 20.sp)
                    Text("شبیه‌ساز هوشمند تسویه زودهنگام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldVip.copy(alpha = 0.16f),
                    border = BorderStroke(0.8.dp, GoldVip.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Rounded.Star, null, tint = GoldVip, modifier = Modifier.size(11.dp))
                        Text("گلوله برفی ⭐ VIP", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GoldVip)
                    }
                }
            }

            Text(
                "با پرداخت ماهیانه مبلغی مازاد، استراتژی گلوله برفی زمان تسویه تمام وام‌های شما را کوتاه‌تر می‌کند:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (validLoans.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "در حال حاضر قسط فعالی برای محاسبه شبیه‌ساز ثبت نشده است.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                // اسلایدر مبلغ مازاد
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مبلغ مازاد ماهیانه:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${extraMonthlyPay.toLong().money()} تومان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldVip)
                    }

                    Slider(
                        value = extraMonthlyPay,
                        onValueChange = { extraMonthlyPay = it },
                        valueRange = 100000f..maxSliderValue,
                        steps = 38,
                        colors = SliderDefaults.colors(thumbColor = GoldVip, activeTrackColor = GoldVip)
                    )

                    // چیپ‌های انتخاب سریع مبلغ مازاد
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(200_000f, 500_000f, 1_000_000f, 2_000_000f).forEach { chipAmt ->
                            if (chipAmt <= maxSliderValue) {
                                val isSel = (extraMonthlyPay - chipAmt).let { it >= -20000 && it <= 20000 }
                                Surface(
                                    onClick = { extraMonthlyPay = chipAmt },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) GoldVip.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (isSel) BorderStroke(1.dp, GoldVip) else null,
                                    modifier = Modifier.weight(1f).bounceClick(minScale = 0.94f)
                                ) {
                                    Text(
                                        "+${chipAmt.toLong().compactMoney()} ت",
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) GoldVip else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // نتیجه شبیه‌سازی
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (simulationResult.savedMonths > 0) Moss.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (simulationResult.savedMonths > 0) Moss.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(if (simulationResult.savedMonths > 0) "🚀" else "💡", fontSize = 26.sp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (simulationResult.savedMonths > 0)
                                    "اقساط شما ${simulationResult.savedMonths.faDigits()} ماه زودتر به پایان می‌رسد! 🎉"
                                else "برنامه تسویه فعلی استاندارد است.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (simulationResult.savedMonths > 0) Moss else MaterialTheme.colorScheme.onSurface
                            )
                            if (simulationResult.savedMonths > 0) {
                                Text(
                                    "تاریخ آزادی مالی: ${simulationResult.acceleratedDateText} (به جای ${simulationResult.normalDateText})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldVip
                                )
                                Text(
                                    "کل بدهی‌ها به جای ${simulationResult.normalMonths.faDigits()} ماه، ظرف مدت ${simulationResult.acceleratedMonths.faDigits()} ماه به طور کامل صفر می‌شوند.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "پایان تسویه اقساط: ${simulationResult.normalDateText} (${simulationResult.normalMonths.faDigits()} ماه دیگر)",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * تفکیک پیشرفته مقاصد پرداخت (بانک‌ها و سرویس‌های BNPL)
 */
@Composable
private fun ProviderDistributionSection(
    activeInstallments: List<Installment>,
    stats: FinancialStats
) {
    val providerGroups = remember(activeInstallments) {
        activeInstallments.groupBy { item ->
            item.destination.trim().ifBlank {
                InstallmentCategories.get(item.category).title
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏦", fontSize = 18.sp)
                    Text("تفکیک تعهدات بر اساس مقصد و بانک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("${providerGroups.size.faDigits()} مقصد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (providerGroups.isEmpty()) {
                Text("هیچ قسط فعالی برای تفکیک وجود ندارد.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                providerGroups.forEach { (providerName, items) ->
                    val totalSum = items.sumOf { it.remainingAmount }
                    val ratio = if (stats.totalActiveDebt > 0) {
                        (totalSum.toFloat() / stats.totalActiveDebt).coerceIn(0f, 1f)
                    } else 0f

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(providerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${totalSum.money()} ت (${(ratio * 100).toInt().faDigits()}٪)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * تالار افتخارات و مدال‌های انضباط مالی (Gamification Badges)
 */
@Composable
private fun FinancialBadgesSection(
    stats: FinancialStats,
    healthScore: Int,
    historyCount: Int
) {
    data class BadgeItem(
        val emoji: String,
        val title: String,
        val desc: String,
        val isUnlocked: Boolean
    )

    val badges = listOf(
        BadgeItem("🏅", "خوش‌حساب اول", "ثبت و پرداخت اولین قسط", stats.totalInstallmentsCount > 0),
        BadgeItem("🛡️", "سپر اعتباری", "کنترل اقساط بدون هیچ معوقه", stats.overdueCount == 0 && stats.activeCount > 0),
        BadgeItem("🚀", "فرار از بدهی", "تسویه بیش از ۵۰٪ کل مبالغ", stats.overallHealthPercentage >= 50f),
        BadgeItem("🏆", "تسویه‌گر طلایی", "تسویه کامل حداقل ۱ فقره وام", historyCount > 0),
        BadgeItem("👑", "نخبه مالی", "امتیاز سلامت بالای ۸۰۰", healthScore >= 800)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎖️", fontSize = 18.sp)
                    Text("نشان‌ها و دستاوردهای انضباط مالی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("${badges.count { it.isUnlocked }.faDigits()} از ${badges.size.faDigits()} آنلاک", fontSize = 10.sp, color = GoldVip, fontWeight = FontWeight.Bold)
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(badges) { badge ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (badge.isUnlocked) GoldVip.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = if (badge.isUnlocked) BorderStroke(1.dp, GoldVip.copy(alpha = 0.4f)) else null,
                        modifier = Modifier
                            .width(130.dp)
                            .bounceClick(minScale = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(badge.emoji, fontSize = 26.sp)
                            Text(
                                badge.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                badge.desc,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
