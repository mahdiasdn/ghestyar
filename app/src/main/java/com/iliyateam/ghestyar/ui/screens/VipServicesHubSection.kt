// ═══ ui/screens/VipServicesHubSection.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iliyateam.ghestyar.CashflowSummary
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.ui.components.AnimatedMoneyText
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

@Composable
fun VipServicesHubSection(
    installments: List<Installment>,
    cheques: List<ChequeOrDebt>,
    cashflow: CashflowSummary,
    stats: FinancialStats,
    isPremium: Boolean,
    onOpenPremium: () -> Unit,
    onGenerateBooklet: (Installment) -> Unit
) {
    val context = LocalContext.current
    var selectedToolTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // سوییچر ابزارهای ۴ گانه
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tools = listOf(
                    0 to ("بهینه‌ساز سود (بهمن) 💸" to Icons.AutoMirrored.Rounded.TrendingDown),
                    1 to ("پیش‌بینی نقدینگی ۶ ماهه 🔮" to Icons.Rounded.AutoAwesome),
                    2 to ("پیامک‌ساز و یادآور هوشمند ✉️" to Icons.AutoMirrored.Rounded.Send),
                    3 to ("دفترچه‌ساز رسمی بانکی 📑" to Icons.Rounded.PictureAsPdf),
                    4 to ("ویجت صفحه اصلی 📱" to Icons.Rounded.Widgets)
                )
                items(tools) { (index, data) ->
                    val (title, icon) = data
                    val isSelected = selectedToolTab == index
                    Surface(
                        onClick = { selectedToolTab = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GoldVip else Color.Transparent,
                        modifier = Modifier.bounceClick(minScale = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        when (selectedToolTab) {
            0 -> AvalancheInterestSaverCard(installments, isPremium, onOpenPremium)
            1 -> SixMonthCashflowForecastCard(installments, cheques, cashflow)
            2 -> SmartSmsReminderCard(installments, cheques)
            3 -> LoanBookletGeneratorCard(installments, onGenerateBooklet)
            4 -> HomeScreenWidgetCard(installments)
        }
    }
}

/**
 * ۱. بهینه‌ساز تسویه بهمن (Avalanche) و محاسبه سود صرفه‌جویی‌شده
 */
@Composable
internal fun AvalancheInterestSaverCard(
    installments: List<Installment>,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val activeLoans = remember(installments) { installments.filter { !it.isPaid && it.remainingAmount > 0 } }
    var extraMonthlyPay by remember { mutableFloatStateOf(500000f) }

    // محاسبه سود تقریبی وام‌ها (فرض میانگین نرخ سود ۱۸٪ برای وام‌های بانکی و ۲۳٪ برای BNPL)
    val estimatedInterestSaved = remember(extraMonthlyPay, activeLoans) {
        val totalDebt = activeLoans.sumOf { it.remainingAmount }
        if (totalDebt <= 0) 0L
        else {
            val monthsSaved = (extraMonthlyPay / (activeLoans.sumOf { it.amount }.coerceAtLeast(100_000L)) * 4f).coerceIn(1f, 18f)
            ((totalDebt * 0.18f / 12f) * monthsSaved).toLong()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💸", fontSize = 20.sp)
                    Text("بهینه‌ساز هوشمند سود بانکی (روش بهمن)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.16f)) {
                    Text("⭐ VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text(
                "استراتژی بهمن (Avalanche) با تسویه سریع‌تر وام‌های پربهره، بیشترین میزان سود بانکی را به جیب شما برمی‌گرداند:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // مبلغ سود صرفه‌جویی شده
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Moss.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Moss.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💰", fontSize = 26.sp)
                    Column {
                        Text("میزان سود صرفه‌جویی‌شده برای شما:", fontSize = 11.sp, color = Moss)
                        Text(
                            "+${estimatedInterestSaved.money()} تومان",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Moss
                        )
                        Text("سود کمتری که به بانک‌ها و شرکت‌های واسط پرداخت خواهید کرد.", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // اسلایدر مازاد پرداختی ماهانه
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("توان پرداخت مازاد ماهانه:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+${extraMonthlyPay.toLong().money()} تومان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Slider(
                    value = extraMonthlyPay,
                    onValueChange = { extraMonthlyPay = it },
                    valueRange = 200000f..5000000f,
                    steps = 24,
                    colors = SliderDefaults.colors(thumbColor = GoldVip, activeTrackColor = GoldVip)
                )
            }
        }
    }
}

/**
 * ۲. پیش‌بینی جریان نقدینگی ۶ ماه آینده
 */
@Composable
internal fun SixMonthCashflowForecastCard(
    installments: List<Installment>,
    cheques: List<ChequeOrDebt>,
    cashflow: CashflowSummary
) {
    val currentJalali = JalaliDate.today()

    val forecastList = remember(installments, cheques, cashflow) {
        val baseFutureIncome = if (cashflow.recurringMonthlyIncome > 0L) {
            cashflow.recurringMonthlyIncome
        } else {
            cashflow.totalIncome
        }

        (0 until 6).map { offset ->
            val targetJalali = currentJalali.plusMonths(offset)
            val monthName = Jalali.months[targetJalali.jm - 1]

            // درآمد ماه: ماه جاری کل درآمد، ماه‌های بعد درآمد ثابت/تکرارشونده
            val monthIncome = if (offset == 0) cashflow.totalIncome else baseFutureIncome

            // اقساط فعال که در ماه مورد نظر جلسه پرداخت دارند
            val monthInstallments = installments.filter { inst ->
                if (inst.isPaid || inst.paidSessions >= inst.totalSessions) false
                else {
                    val startJ = LocalDate.ofEpochDay(inst.startEpochDay).toJalali()
                    val remainingStartSession = inst.paidSessions + 1
                    (remainingStartSession..inst.totalSessions).any { s ->
                        val sessionDue = startJ.plusMonths(s - 1)
                        sessionDue.jy == targetJalali.jy && sessionDue.jm == targetJalali.jm
                    }
                }
            }.sumOf { it.amount }

            // چک‌های سررسید آن ماه (طلب‌ها مثبت و بدهی‌ها منفی)
            val monthCheques = cheques.filter { ch ->
                if (ch.isCleared) false
                else {
                    val chJalali = LocalDate.ofEpochDay(ch.dueEpochDay).toJalali()
                    chJalali.jy == targetJalali.jy && chJalali.jm == targetJalali.jm
                }
            }.sumOf { ch -> if (ch.isReceivable) -ch.amount else ch.amount }

            val netProjected = monthIncome - monthInstallments - monthCheques
            val hasDeficit = netProjected < 0
            Triple(monthName, netProjected, hasDeficit)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔮", fontSize = 20.sp)
                    Text("پیش‌بینی نقدینگی ۶ ماه آینده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.16f)) {
                    Text("هوشمند ⭐", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text(
                "پیش‌بینی تراز موجودی شما پس از کسر تمام اقساط و سررسید چک‌های ۶ ماه آینده:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // نمودار ۶ ماهه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                forecastList.forEach { (monthName, projected, isDeficit) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDeficit) Coral.copy(alpha = 0.12f) else Moss.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, if (isDeficit) Coral.copy(alpha = 0.35f) else Moss.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(monthName, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (isDeficit) "⚠️ کسری" else "🟢 مثبت",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDeficit) Coral else Moss
                            )
                            Text(
                                "${(projected / 1000).money()} ه.ت",
                                fontSize = 8.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ۳. استودیو و ادیتور هوشمند پیامک و پیام یادآور اقساط و بدهی
 */
@Composable
internal fun SmartSmsReminderCard(
    installments: List<Installment>,
    cheques: List<ChequeOrDebt>
) {
    val context = LocalContext.current
    var selectedTone by remember { mutableIntStateOf(0) }
    var inputMode by remember { mutableIntStateOf(0) } // 0: اقساط, 1: چک و طلب, 2: دستی

    // فیلدهای قابل ویرایش توسط کاربر
    var title by remember { mutableStateOf(installments.firstOrNull()?.title ?: "قسط وام") }
    var recipientName by remember { mutableStateOf("") }
    var amountDigits by remember { mutableStateOf(installments.firstOrNull()?.amount?.toString() ?: "1000000") }
    var dueDateStr by remember {
        val firstDate = installments.firstOrNull()?.let { LocalDate.ofEpochDay(it.dueEpochDay).toJalali() } ?: JalaliDate.today().plusMonths(1)
        mutableStateOf("${firstDate.jy.faDigits()}/${firstDate.jm.faDigits().padStart(2, '۰')}/${firstDate.jd.faDigits().padStart(2, '۰')}")
    }
    var cardOrShaba by remember { mutableStateOf("۶۰۳۷-۹۹۷۵-****-****") }
    var customMessageText by remember { mutableStateOf("") }
    var isUserManuallyEditingText by remember { mutableStateOf(false) }

    val tones = listOf("محترمانه و اداری", "دوستانه و صمیمی", "رسمی و حقوقی", "یادآوری به ضامن 🛡️")

    val amountLong = amountDigits.toLongOrNull() ?: 0L

    // تولید خودکار متن در صورت عدم ویرایش دستی مستقیم
    val autoGeneratedText = remember(selectedTone, title, recipientName, amountLong, dueDateStr, cardOrShaba) {
        val targetPerson = if (recipientName.isNotBlank()) " جناب/سرکار $recipientName" else ""
        val bankInfo = if (cardOrShaba.isNotBlank()) " شماره کارت/شبا: $cardOrShaba" else ""
        when (selectedTone) {
            0 -> "با سلام و احترام$targetPerson، بدین‌وسیله یادآوری می‌گردد موعد سررسید «$title» به مبلغ ${amountLong.money()} تومان در تاریخ $dueDateStr می‌باشد.$bankInfo با تشکر فراوان."
            1 -> "سلام و ارادت$targetPerson، موعد پرداخت «$title» به مبلغ ${amountLong.money()} تومان برای تاریخ $dueDateStr هست. محبت کنید واریز بفرمایید.$bankInfo مخلصم 🙏"
            2 -> "پیرو تعهدات مالی فیمابین، سررسید پرداخت «$title» به مبلغ ${amountLong.money()} تومان مورخ $dueDateStr اعلام می‌گردد.$bankInfo سامانه مدیریت مالی قسط‌یار."
            else -> "با سلام و احترام$targetPerson (ضامن محترم)، بدین‌وسیله موعد سررسید قسط وام «$title» به مبلغ ${amountLong.money()} تومان در تاریخ $dueDateStr یادآوری می‌گردد.$bankInfo لطفاً پیگیری لازم را مبذول فرمایید."
        }
    }

    LaunchedEffect(autoGeneratedText) {
        if (!isUserManuallyEditingText) {
            customMessageText = autoGeneratedText
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
                    Text("✉️", fontSize = 20.sp)
                    Text("استودیو و پیامک‌ساز هوشمند", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.16f)) {
                    Text("⭐ VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            // سوییچ منبع داده: اقساط قبلی / چک و طلب / ورود دستی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modes = listOf("از اقساط 📑", "از چک و طلب ✍️", "ورود دستی ✏️")
                modes.forEachIndexed { index, label ->
                    val isSel = inputMode == index
                    Surface(
                        onClick = {
                            inputMode = index
                            isUserManuallyEditingText = false
                            if (index == 0 && installments.isNotEmpty()) {
                                val inst = installments.first()
                                title = inst.title
                                amountDigits = inst.amount.toString()
                                val j = LocalDate.ofEpochDay(inst.dueEpochDay).toJalali()
                                dueDateStr = "${j.jy.faDigits()}/${j.jm.faDigits().padStart(2, '۰')}/${j.jd.faDigits().padStart(2, '۰')}"
                                cardOrShaba = inst.destination.ifBlank { "۶۰۳۷-۹۹۷۵-****-****" }
                            } else if (index == 1 && cheques.isNotEmpty()) {
                                val ch = cheques.first()
                                title = ch.title
                                recipientName = ch.personName
                                amountDigits = ch.amount.toString()
                                val j = LocalDate.ofEpochDay(ch.dueEpochDay).toJalali()
                                dueDateStr = "${j.jy.faDigits()}/${j.jm.faDigits().padStart(2, '۰')}/${j.jd.faDigits().padStart(2, '۰')}"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) GoldVip else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.weight(1f).bounceClick(minScale = 0.94f)
                    ) {
                        Text(
                            label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // چیپ‌های انتخاب سریع از موارد موجود
            if (inputMode == 0 && installments.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(installments) { inst ->
                        FilterChip(
                            selected = title == inst.title,
                            onClick = {
                                title = inst.title
                                amountDigits = inst.amount.toString()
                                val j = LocalDate.ofEpochDay(inst.dueEpochDay).toJalali()
                                dueDateStr = "${j.jy.faDigits()}/${j.jm.faDigits().padStart(2, '۰')}/${j.jd.faDigits().padStart(2, '۰')}"
                                cardOrShaba = inst.destination.ifBlank { cardOrShaba }
                                isUserManuallyEditingText = false
                            },
                            label = { Text(inst.title, fontSize = 10.5.sp) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            } else if (inputMode == 1 && cheques.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cheques) { ch ->
                        FilterChip(
                            selected = title == ch.title,
                            onClick = {
                                title = ch.title
                                recipientName = ch.personName
                                amountDigits = ch.amount.toString()
                                val j = LocalDate.ofEpochDay(ch.dueEpochDay).toJalali()
                                dueDateStr = "${j.jy.faDigits()}/${j.jm.faDigits().padStart(2, '۰')}/${j.jd.faDigits().padStart(2, '۰')}"
                                isUserManuallyEditingText = false
                            },
                            label = { Text("${ch.title} (${ch.personName})", fontSize = 10.5.sp) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // فیلدهای ویرایش دستی مشخصات
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; isUserManuallyEditingText = false },
                    label = { Text("عنوان قسط / تعهد", fontSize = 10.5.sp) },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it; isUserManuallyEditingText = false },
                    label = { Text("نام مخاطب", fontSize = 10.5.sp) },
                    placeholder = { Text("اختیاری", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (amountDigits.isEmpty()) "" else amountLong.money(),
                    onValueChange = { v -> amountDigits = v.cleanNumericDigits(12); isUserManuallyEditingText = false },
                    label = { Text("مبلغ (تومان)", fontSize = 10.5.sp) },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDateStr,
                    onValueChange = { dueDateStr = it; isUserManuallyEditingText = false },
                    label = { Text("تاریخ سررسید", fontSize = 10.5.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = cardOrShaba,
                onValueChange = { cardOrShaba = it; isUserManuallyEditingText = false },
                label = { Text("شماره کارت، حساب یا شبا جهت واریز", fontSize = 10.5.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // انتخاب لحن پیام
            Text("لحن پیام:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tones.forEachIndexed { index, tone ->
                    val isSel = selectedTone == index
                    Surface(
                        onClick = { selectedTone = index; isUserManuallyEditingText = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Moss else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.weight(1f).bounceClick(minScale = 0.94f)
                    ) {
                        Text(
                            tone,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // باکس پیش‌نمایش و ادیتور مستقیم متن نهایی
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("متن کامل پیام (قابل ویرایش مستقیم):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isUserManuallyEditingText) {
                        TextButton(onClick = { isUserManuallyEditingText = false; customMessageText = autoGeneratedText }) {
                            Text("بازنشانی متن 🔄", fontSize = 10.sp, color = GoldVip)
                        }
                    }
                }

                OutlinedTextField(
                    value = customMessageText,
                    onValueChange = {
                        customMessageText = it
                        isUserManuallyEditingText = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                )
            }

            // دکمه‌های عملیات: ارسال پیامک، کپی و اشتراک گذاری
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ارسال مستقیم با پیامک
                Button(
                    onClick = {
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:")
                            putExtra("sms_body", customMessageText)
                        }
                        try {
                            context.startActivity(smsIntent)
                        } catch (_: Exception) {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, customMessageText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(share, "ارسال پیام یادآوری"))
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .bounceClick(minScale = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ارسال پیامک 📲", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // اشتراک گذاری در پیام‌رسان‌ها
                FilledTonalButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, customMessageText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری در پیام‌رسان‌ها"))
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(46.dp)
                        .bounceClick(minScale = 0.95f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("اشتراک‌گذاری", fontSize = 10.5.sp)
                }

                // کپی متن
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("قسط‌یار", customMessageText)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "متن پیام کپی شد 📋", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, "کپی متن", tint = Moss)
                }
            }
        }
    }
}

/**
 * ۴. دفترچه‌ساز رسمی بانکی PDF
 */
@Composable
internal fun LoanBookletGeneratorCard(
    installments: List<Installment>,
    onGenerateBooklet: (Installment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.35f))
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
                    Text("📑", fontSize = 20.sp)
                    Text("تولید دفترچه رسمی اقساط بانکی (A4)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.16f)) {
                    Text("⭐ VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text(
                "برای هر وام یک سند رسمی دو صفحه‌ای بانکی همراه با جدول ماه به ماه، بارکد و محل امضا جهت پرینت تولید کنید:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (installments.isEmpty()) {
                Text("هنوز وامی برای صدور دفترچه ثبت نشده است.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                installments.forEach { inst ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inst.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${inst.totalSessions.faDigits()} قسط • ${inst.amount.money()} تومان", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = { onGenerateBooklet(inst) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.bounceClick(minScale = 0.94f)
                            ) {
                                Text("صدور دفترچه PDF 🖨️", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ۵. ویجت هوشمند و زنده صفحه اصلی گوشی
 */
@Composable
internal fun HomeScreenWidgetCard(
    installments: List<Installment>
) {
    val context = LocalContext.current
    val nextItem = remember(installments) { installments.filter { !it.isPaid }.minByOrNull { it.dueEpochDay } }
    val monthlyTotal = remember(installments) {
        val today = LocalDate.now().toEpochDay()
        installments.filter { !it.isPaid && it.dueEpochDay <= today + 30 }.sumOf { it.amount }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, GoldVip.copy(alpha = 0.35f))
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
                    Text("📱", fontSize = 20.sp)
                    Text("ویجت تعاملی صفحه اصلی گوشی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldVip)
                }
                Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.16f)) {
                    Text("⭐ VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text(
                "پیش‌نمایش زنده ویجت قسط‌یار بر روی صفحه اصلی دستگاه شما:",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // شبیه‌سازی زنده ویجت با داده‌های واقعی کاربر
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F2628),
                border = BorderStroke(1.2.dp, Color(0xFF34D399).copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("قسط‌یار • نزدیک‌ترین سررسید", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0x33F59E0B)
                        ) {
                            val daysLabel = nextItem?.let {
                                LocalDate.ofEpochDay(it.dueEpochDay).relativeLabel()
                            } ?: "تکمیل"
                            Text(daysLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text(nextItem?.title ?: "خیالت آسوده ✨", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        if (nextItem != null) "${nextItem.amount.money()} تومان" else "همه اقساط تسویه شده است",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    Text(
                        "تعهد این ماه: ${monthlyTotal.money()} تومان (${installments.filter { !it.isPaid }.size.faDigits()} قسط)",
                        fontSize = 10.5.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Button(
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                        val myProvider = android.content.ComponentName(context, com.iliyateam.ghestyar.widget.GhestYarWidgetProvider::class.java)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            appWidgetManager.requestPinAppWidget(myProvider, null, null)
                        } else {
                            android.widget.Toast.makeText(context, "انگشت خود را روی صفحه اصلی نگه داشته و ویجت قسط‌یار را اضافه کنید 📱", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "انگشت خود را روی صفحه اصلی نگه داشته و ویجت قسط‌یار را اضافه کنید 📱", android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Icon(Icons.Rounded.Widgets, null, tint = Color.Black, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("افزودن ویجت به صفحه اصلی گوشی 📱", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
