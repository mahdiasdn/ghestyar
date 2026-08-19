// ═══ ui/screens/AnalyticsTab.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.ProgressRing
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.money

@Composable
fun AnalyticsTab(
    stats: FinancialStats,
    activeInstallments: List<Installment>,
    historyInstallments: List<Installment>,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ۱. کارت شاخص پیشرفت تسویه اقساط
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressRing(
                        progress = stats.overallHealthPercentage / 100f,
                        tint = Moss,
                        strokeWidth = 8.dp,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${stats.overallHealthPercentage.toInt().faDigits()}٪",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Moss
                            )
                            Text("تسویه", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "شاخص تسویه وام‌ها و اقساط",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "تاکنون ${stats.totalPaidAllTime.money()} تومان تسویه شده و ${stats.totalActiveDebt.money()} تومان باقیمانده است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ۲. شبکه ۲×۲ کارت‌های آماری Bento
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "مانده بدهی کل",
                    value = "${stats.totalActiveDebt.money()} ت",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    tint = Coral
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "تعهد ۳۰ روز آینده",
                    value = "${stats.monthlyCommitment.money()} ت",
                    icon = Icons.Rounded.CalendarMonth,
                    tint = GoldVip
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "اقساط فعال",
                    value = "${stats.activeCount.faDigits()} فقره",
                    icon = Icons.Rounded.HourglassTop,
                    tint = Moss
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "وضعیت معوقات",
                    value = if (stats.overdueCount > 0) "${stats.overdueCount.faDigits()} قسط معوق ⚠️" else "فاقد معوقه ✅",
                    icon = Icons.Rounded.WarningAmber,
                    tint = if (stats.overdueCount > 0) Coral else Moss
                )
            }
        }

        // ۳. 👑 مشاور هوشمند استراتژی تسویه بدهی و نجات مالی (Killer VIP Feature)
        item {
            if (isPremium) {
                DebtPayoffStrategySection(
                    activeInstallments = activeInstallments,
                    stats = stats
                )
            } else {
                VipStrategyTeaserCard(onOpenPremium = onOpenPremium)
            }
        }

        // ۴. رادار نسبت بدهی به درآمد (DTI Health Gauge)
        item {
            DebtToIncomeHealthCard(
                stats = stats,
                isPremium = isPremium,
                onOpenPremium = onOpenPremium
            )
        }

        // ۵. تفکیک دسته‌بندی‌ها بر اساس نوع وام
        item {
            val categoryGroups = remember(activeInstallments) {
                activeInstallments.groupBy { it.category }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "تفکیک اقساط فعال بر اساس موضوع",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (categoryGroups.isEmpty()) {
                        Text(
                            "هیچ قسط فعالی برای دسته‌بندی وجود ندارد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categoryGroups.forEach { (catId, items) ->
                            val category = InstallmentCategories.get(catId)
                            val sumAmount = items.sumOf { it.remainingAmount }
                            val ratio = if (stats.totalActiveDebt > 0) {
                                (sumAmount.toFloat() / stats.totalActiveDebt).coerceIn(0f, 1f)
                            } else 0f

                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(category.emoji, fontSize = 12.sp)
                                        Text(
                                            category.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        "${sumAmount.money()} تومان (${((ratio * 100).toInt()).faDigits()}٪)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = Moss,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
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
 * بخش تحلیل هوشمند استراتژی تسویه بدهی‌ها (مخصوص اعضای طلایی)
 */
@Composable
private fun DebtPayoffStrategySection(
    activeInstallments: List<Installment>,
    stats: FinancialStats
) {
    val smallestLoan = remember(activeInstallments) {
        activeInstallments.minByOrNull { it.remainingAmount }
    }

    val maxMonthsRemaining = remember(activeInstallments) {
        activeInstallments.maxOfOrNull { it.totalSessions - it.paidSessions } ?: 0
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
                    Text("🤖", fontSize = 20.sp)
                    Text(
                        "مشاور استراتژی تسویه بدهی (هوش مالی)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldVip
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.15f)) {
                    Text("VIP 👑", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // تارگت تخمینی آزادی مالی
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
                    Text("🎯", fontSize = 24.sp)
                    Column(Modifier.weight(1f)) {
                        Text("تاریخ تخمینی صفر شدن تمام اقساط:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (maxMonthsRemaining > 0) "${maxMonthsRemaining.faDigits()} ماه آینده (طبق برنامه منظم اقساط)"
                            else "تبریک! هیچ قسط فعالی ندارید 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Moss
                        )
                    }
                }
            }

            // استراتژی گلوله برفی (Snowball)
            smallestLoan?.let { loan ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("❄️", fontSize = 14.sp)
                            Text("استراتژی گلوله برفی (پیشنهاد اولویت تسویه):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "وام «${loan.title}» با مانده ${loan.remainingAmount.money()} تومان کمترین بدهی شماست. با تسویه زودتر این وام، ماهانه ${loan.amount.money()} تومان نقدینگی در بودجه شما آزاد می‌شود!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * بنر معرفی و ترغیب به خرید بخش استراتژی برای کاربران رایگان
 */
@Composable
private fun VipStrategyTeaserCard(onOpenPremium: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = GoldVip.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.4f))
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
                    Text("🤖", fontSize = 22.sp)
                    Text("مشاور هوشمند استراتژی تسویه بدهی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Text("🔒 قفل VIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldVip)
            }

            Text(
                "با فعال‌سازی پلن طلایی، سیستم هوشمند قسط‌یار با فرمول‌های Snowball و Avalanche بهترین مسیر ریاضی را برای تسویه زودهنگام وام‌ها و صرفه‌جویی چند ده میلیونی در سود بانکی برایتان محاسبه می‌کند.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onOpenPremium,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("بازگشایی استراتژی تسویه و هوش مالی 👑", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * کارت نسبت بدهی به درآمد (DTI Health Ratio)
 */
@Composable
private fun DebtToIncomeHealthCard(
    stats: FinancialStats,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val dtiRatio = if (stats.totalMonthlyIncome > 0) {
        (stats.monthlyCommitment.toFloat() / stats.totalMonthlyIncome).coerceIn(0f, 1.5f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📊", fontSize = 16.sp)
                    Text("رادار سلامت اعتباری (نسبت اقساط به درآمد)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                if (!isPremium) {
                    Text("👑 نسخه VIP", fontSize = 10.sp, color = GoldVip, fontWeight = FontWeight.Bold)
                }
            }

            if (stats.totalMonthlyIncome > 0) {
                val percentage = (dtiRatio * 100).toInt()
                val statusText = when {
                    percentage <= 35 -> "وضعیت عالی 🟢 (اقساط کمتر از ۳۵٪ درآمد)"
                    percentage <= 50 -> "وضعیت محتاط 🟡 (اقساط ۳۵ تا ۵۰٪ درآمد)"
                    else -> "هشدار کسری بودجه 🔴 (اقساط بیش از ۵۰٪ درآمد)"
                }
                val statusColor = when {
                    percentage <= 35 -> Moss
                    percentage <= 50 -> GoldVip
                    else -> Coral
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        Text("${percentage.faDigits()}٪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                    LinearProgressIndicator(
                        progress = { dtiRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            } else {
                Text(
                    "برای محاسبه دقیق سلامت اعتباری، درآمد ماهانه خود را در بخش دخل‌وخرج وارد کنید.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
