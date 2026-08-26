// ═══ ui/screens/OverviewScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Brush
import com.iliyateam.ghestyar.CashflowSummary
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.SavingsGoal
import com.iliyateam.ghestyar.ui.components.AnimatedMoneyText
import com.iliyateam.ghestyar.ui.components.ReceiptShareHelper
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.pulseGlow
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun OverviewScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenCalculator: () -> Unit,
    onAddInstallment: () -> Unit,
    onOpenCashflow: () -> Unit,
    onDetailInstallment: (Installment) -> Unit,
    onPremium: () -> Unit = {},
    selectedDashboardTab: Int = 0,
    onDashboardTabChange: (Int) -> Unit = {}
) {
    val activeInstallments by vm.active.collectAsStateWithLifecycle()
    val historyInstallments by vm.history.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val cashflow by vm.cashflowSummary.collectAsStateWithLifecycle()
    val pendingCheques by vm.pendingChequesAndDebts.collectAsStateWithLifecycle()
    val goals by vm.savingsGoals.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    var calendarMonth by remember { mutableStateOf(JalaliDate.today()) }
    var selectedDay by remember { mutableStateOf(JalaliDate.today()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ۱. نوار هدر بالا با statusBarsPadding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Moss,
                    modifier = Modifier.size(38.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (selectedDashboardTab == 0) Icons.Rounded.Dashboard else Icons.Rounded.Analytics,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (selectedDashboardTab == 0) "داشبورد و تقویم مالی" else "آمار و تحلیل هوشمند",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(LocalDate.now().formatJalaliWithWeekday(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // دکمه ماشین حساب سریع وام
            Button(
                onClick = onOpenCalculator,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Calculate, null, tint = Moss, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ماشین‌حساب وام", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // ۲. سوییچر ۲ تبه بین تقویم/داشبورد و آمار/تحلیل جامع
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    0 to ("تقویم و داشبورد" to Icons.Rounded.CalendarMonth),
                    1 to ("آمار و نمودارها" to Icons.Rounded.Analytics)
                )
                tabs.forEach { (index, data) ->
                    val (title, icon) = data
                    val isSelected = selectedDashboardTab == index
                    Surface(
                        onClick = { onDashboardTabChange(index) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Moss else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(minScale = 0.96f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (selectedDashboardTab == 1) {
            AnalyticsTab(
                stats = stats,
                activeInstallments = activeInstallments,
                historyInstallments = historyInstallments,
                isPremium = isPremium,
                onOpenPremium = onPremium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ۱. مرکز جامع تراز مالی و تفکیک جریان نقدینگی ماهانه
                item {
                    DashboardFinancialSummaryHub(
                        cashflow = cashflow,
                        activeInstallmentsCount = activeInstallments.size,
                        pendingChequesCount = pendingCheques.size,
                        isPrivacy = isPrivacyMode,
                        onOpenCashflow = onOpenCashflow
                    )
                }

        // ۴. تقویم ماهانه هوشمند شمسی با رنگ‌بندی کاملاً تفکیک‌شده
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // هدر ماه تقویم با کلیدهای قبلی / بعدی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { calendarMonth = calendarMonth.minusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.ChevronRight, "ماه قبل", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Text(
                            "${Jalali.months[calendarMonth.jm - 1]} ${calendarMonth.jy.faDigits()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { calendarMonth = calendarMonth.plusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, "ماه بعد", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // سربرگ روزهای هفته
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { wd ->
                            Text(
                                wd,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (wd == "ج") Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // گرید روزهای ماه
                    JalaliMonthCalendarGrid(
                        year = calendarMonth.jy,
                        month = calendarMonth.jm,
                        selectedDay = selectedDay,
                        activeInstallments = activeInstallments,
                        pendingCheques = pendingCheques,
                        goals = goals,
                        onDayClick = { selectedDay = it }
                    )

                    // راهنمای شفاف نشانگرهای رنگی
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DotLegend(color = Moss, label = "اقساط (سبز)")
                        DotLegend(color = ChequeBlue, label = "چک / طلب (آبی)")
                        DotLegend(color = GoldVip, label = "قلک هدف (طلایی)")
                    }
                }
            }
        }

        // ۵. بخش رویدادهای روز انتخابی تقویم
        item {
            val selectedLocalDate = selectedDay.toLocalDate()
            val selectedEpochDay = selectedLocalDate.toEpochDay()

            val dayInstallments = activeInstallments.filter { it.dueEpochDay == selectedEpochDay }
            val dayCheques = pendingCheques.filter { it.dueEpochDay == selectedEpochDay }
            val dayGoals = goals.filter { it.targetEpochDay == selectedEpochDay }
            val hasEvents = dayInstallments.isNotEmpty() || dayCheques.isNotEmpty() || dayGoals.isNotEmpty()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "سررسیدهای ${selectedDay.format()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (hasEvents) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Moss.copy(alpha = 0.14f)
                        ) {
                            Text(
                                "${(dayInstallments.size + dayCheques.size + dayGoals.size).faDigits()} مورد",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Moss,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!hasEvents) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("✨", fontSize = 22.sp)
                            Column {
                                Text("هیچ سررسیدی در این تاریخ ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("برای این روز پرداختی یا موعد چکی نداری.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    // لیست اقساط با تم سبز زمردی
                    dayInstallments.forEachIndexed { idx, item ->
                        StaggeredItemEntrance(index = idx) {
                            Card(
                                onClick = { onDetailInstallment(item) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                border = BorderStroke(1.dp, Moss.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick(minScale = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(shape = CircleShape, color = Moss.copy(alpha = 0.14f), modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Rounded.ReceiptLong, null, tint = Moss, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Surface(shape = RoundedCornerShape(50), color = Moss.copy(alpha = 0.12f)) {
                                                Text("قسط", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Moss, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text("قسط ${(item.paidSessions + 1).faDigits()} از ${item.totalSessions.faDigits()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(if (isPrivacyMode) "••••••" else "${item.amount.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Moss)
                                }
                            }
                        }
                    }

                    // لیست چک‌ها و طلب‌ها با تم لاجوردی آبی درخشان متمایز
                    dayCheques.forEachIndexed { idx, item ->
                        StaggeredItemEntrance(index = idx + dayInstallments.size) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick(minScale = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(shape = CircleShape, color = ChequeBlue.copy(alpha = 0.14f), modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (item.isCheque) "✍️" else "🤝", fontSize = 16.sp)
                                        }
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Surface(shape = RoundedCornerShape(50), color = ChequeBlue.copy(alpha = 0.12f)) {
                                                Text(if (item.isCheque) "چک صیادی" else "قرض/طلب", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ChequeBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text("طرف حساب: ${item.personName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(if (isPrivacyMode) "••••••" else "${item.amount.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (item.isReceivable) ChequeBlue else Coral)
                                }
                            }
                        }
                    }

                    // لیست قلک‌ها و اهداف با تم طلایی
                    dayGoals.forEachIndexed { idx, item ->
                        StaggeredItemEntrance(index = idx + dayInstallments.size + dayCheques.size) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick(minScale = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(shape = CircleShape, color = GoldVip.copy(alpha = 0.14f), modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🎯", fontSize = 16.sp)
                                        }
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.12f)) {
                                                Text("هدف پس‌انداز", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = GoldVip, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text("مبلغ هدف: ${item.targetAmount.money()} تومان", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${item.currentAmount.money()} ت", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GoldVip)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
    }
}

@Composable
private fun DashboardFinancialSummaryHub(
    cashflow: CashflowSummary,
    activeInstallmentsCount: Int,
    pendingChequesCount: Int,
    isPrivacy: Boolean,
    onOpenCashflow: () -> Unit
) {
    val isPositive = cashflow.remainingAfterInstallments >= 0
    val totalInflow = cashflow.totalMonthlyInflow
    val totalOutflow = cashflow.totalMonthlyOutflow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ۱. کارت اصلی هیرو تراز نهایی ماهانه (Hero Net Balance Hub)
        Card(
            onClick = onOpenCashflow,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(minScale = 0.98f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(
                1.dp,
                if (isPositive) Moss.copy(alpha = 0.35f) else Coral.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (if (isPositive) Moss else Coral).copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // سطر بالا: عنوان تراز و بج وضعیت
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = (if (isPositive) Moss else Coral).copy(alpha = 0.18f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPositive) Moss else Coral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "مانده خالص نقدینگی ماه",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "کسر تمام مخارج، اقساط و چک‌ها از درآمدها",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = (if (isPositive) Moss else Coral).copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, (if (isPositive) Moss else Coral).copy(alpha = 0.4f))
                        ) {
                            Text(
                                if (isPositive) "تراز مثبت ماه 📈" else "کسری بودجه 📉",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isPositive) Moss else Coral,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // عدد مانده نهایی
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedMoneyText(
                            amount = cashflow.remainingAfterInstallments,
                            isPrivacy = isPrivacy,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) Moss else Coral
                        )
                        if (!isPrivacy) {
                            Text(
                                "تومان",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // نوار نمایش شفاف فرمول تفکیکی
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPrivacy) "ورودی‌ها: ••••••" else "کل ورودی: ${totalInflow.money()} ت",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Moss
                            )
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isPrivacy) "تعهدات: ••••••" else "کل خروجی: ${totalOutflow.money()} ت",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Coral
                            )
                        }
                    }
                }
            }
        }

        // ۲. کارت‌های تفکیک‌شده Bento چهارگانه
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ۱. کل درآمدها
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                title = "درآمدهای ماه",
                value = if (isPrivacy) "••••••" else "${cashflow.totalIncome.money()} ت",
                icon = Icons.Rounded.ArrowDownward,
                tint = Moss,
                subText = if (cashflow.thisMonthReceivableCheques > 0) "+${cashflow.thisMonthReceivableCheques.money()} طلب" else "درآمد جاری"
            )

            // ۲. هزینه‌ها و مخارج
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                title = "مخارج و هزینه‌ها",
                value = if (isPrivacy) "••••••" else "${cashflow.totalExpense.money()} ت",
                icon = Icons.Rounded.ArrowUpward,
                tint = Coral,
                subText = "هزینه‌های ثبت‌شده"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ۳. اقساط وام‌ها
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                title = "اقساط این ماه",
                value = if (isPrivacy) "••••••" else "${cashflow.thisMonthInstallments.money()} ت",
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                tint = Color(0xFF0D9488),
                subText = "${activeInstallmentsCount.faDigits()} قسط فعال"
            )

            // ۴. چک‌ها و بدهی‌ها
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                title = "چک‌ها و بدهی‌ها",
                value = if (isPrivacy) "••••••" else "${cashflow.thisMonthPayableCheques.money()} ت",
                icon = Icons.Rounded.HistoryEdu,
                tint = ChequeBlue,
                subText = if (cashflow.thisMonthReceivableCheques > 0) "${pendingChequesCount.faDigits()} چک • طلب دار" else "${pendingChequesCount.faDigits()} چک در انتظار"
            )
        }
    }
}

@Composable
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    subText: String
) {
    Card(
        modifier = modifier
            .bounceClick(minScale = 0.96f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = tint.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                    }
                }
                Text(subText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tint)
            }

            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun JalaliMonthCalendarGrid(
    year: Int,
    month: Int,
    selectedDay: JalaliDate,
    activeInstallments: List<Installment>,
    pendingCheques: List<ChequeOrDebt>,
    goals: List<SavingsGoal>,
    onDayClick: (JalaliDate) -> Unit
) {
    val totalDays = remember(year, month) { Jalali.monthLength(year, month) }
    val firstDayOfWeekIndex = remember(year, month) {
        val firstDayGregorian = Jalali.toGregorian(year, month, 1)
        when (firstDayGregorian.dayOfWeek.toString()) {
            "SATURDAY" -> 0
            "SUNDAY" -> 1
            "MONDAY" -> 2
            "TUESDAY" -> 3
            "WEDNESDAY" -> 4
            "THURSDAY" -> 5
            else -> 6 // FRIDAY
        }
    }

    val today = remember { JalaliDate.today() }

    val installmentEpochDays = remember(activeInstallments) {
        activeInstallments.map { it.dueEpochDay }.toSet()
    }
    val chequeEpochDays = remember(pendingCheques) {
        pendingCheques.map { it.dueEpochDay }.toSet()
    }
    val goalEpochDays = remember(goals) {
        goals.map { it.targetEpochDay }.toSet()
    }

    // پیش‌محاسبه داده‌های روزهای ماه برای صفر شدن لگ حین اسکرول
    val monthDayInfos = remember(year, month, installmentEpochDays, chequeEpochDays, goalEpochDays) {
        (1..totalDays).map { d ->
            val date = JalaliDate(year, month, d)
            val epochDay = date.toLocalDate().toEpochDay()
            Triple(
                date,
                epochDay,
                Triple(
                    epochDay in installmentEpochDays,
                    epochDay in chequeEpochDays,
                    epochDay in goalEpochDays
                )
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var dayCounter = 1
        val totalCells = firstDayOfWeekIndex + totalDays
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < firstDayOfWeekIndex || dayCounter > totalDays) {
                        Spacer(Modifier.size(36.dp))
                    } else {
                        val d = dayCounter
                        val (thisDate, _, events) = monthDayInfos[d - 1]
                        val isSelected = selectedDay.jy == year && selectedDay.jm == month && selectedDay.jd == d
                        val isToday = today.jy == year && today.jm == month && today.jd == d
                        val (hasInstallment, hasCheque, hasGoal) = events

                        CalendarDayCell(
                            day = d,
                            isSelected = isSelected,
                            isToday = isToday,
                            isFriday = col == 6,
                            hasInstallment = hasInstallment,
                            hasCheque = hasCheque,
                            hasGoal = hasGoal,
                            onClick = { onDayClick(thisDate) }
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isFriday: Boolean,
    hasInstallment: Boolean,
    hasCheque: Boolean,
    hasGoal: Boolean,
    onClick: () -> Unit
) {
    val cellScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "cell_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else -> Color.Transparent
        },
        border = if (isToday && !isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = cellScale
                scaleY = cellScale
            }
            .bounceClick(minScale = 0.88f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                day.faDigits(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isFriday -> Coral
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // نشانگرهای نقطه‌ای رویدادهای مالی با رنگ‌های کاملاً تفکیک‌شده
            if (hasInstallment || hasCheque || hasGoal) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    if (hasInstallment) {
                        // سبز زمردی برای اقساط
                        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else Moss))
                    }
                    if (hasCheque) {
                        // آبی لاجوردی درخشان برای چک‌ها و طلب‌ها
                        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else ChequeBlue))
                    }
                    if (hasGoal) {
                        // طلایی کهربایی برای قلک‌های هدف
                        Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else GoldVip))
                    }
                }
            }
        }
    }
}

@Composable
private fun DotLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


