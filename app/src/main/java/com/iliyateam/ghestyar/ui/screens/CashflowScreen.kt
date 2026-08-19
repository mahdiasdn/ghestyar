// ═══ ui/screens/CashflowScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.CashflowSummary
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.Transaction
import com.iliyateam.ghestyar.data.TransactionCategories
import com.iliyateam.ghestyar.ui.components.AnimatedMoneyText
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.pulseGlow
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

enum class CashflowTypeFilter(val title: String) {
    ALL("همه"),
    INCOME("درآمدها 💰"),
    EXPENSE("مخارج 🛒")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashflowScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val cashflow by vm.cashflowSummary.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCategoryFilterSheet by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf(CashflowTypeFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // فیلتر هوشمند تراکنش‌ها
    val filteredTransactions = remember(transactions, selectedTypeFilter, searchQuery, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchType = when (selectedTypeFilter) {
                CashflowTypeFilter.ALL -> true
                CashflowTypeFilter.INCOME -> tx.isIncome
                CashflowTypeFilter.EXPENSE -> !tx.isIncome
            }
            val matchCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
            val matchQuery = searchQuery.isBlank() ||
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true)

            matchType && matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ۱. نوار بالای شیک با هدر و دکمه ثبت سریع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Moss,
                    modifier = Modifier.size(40.dp),
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("مدیریت دخل و خرج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "ماه جاری: ${Jalali.months[JalaliDate.today().jm - 1]} ${JalaliDate.today().jy.faDigits()}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("ثبت دخل/خرج", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ۲. داشبورد کارت فوق‌پیشرفته Bento برای خلاصه دخل و خرج
            item {
                EnhancedCashflowDashboardCard(
                    cashflow = cashflow,
                    isPrivacy = isPrivacyMode
                )
            }

            // ۳. نوار جستجو و فیلترهای مینیمال و آرامش‌بخش
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // سطر فیلترها: سوییچر سه تایی + دکمه فیلتر دسته‌بندی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // سوییچر تمیز ۳ حالته
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CashflowTypeFilter.entries.forEach { filter ->
                                val isSelected = selectedTypeFilter == filter
                                val bgColor by animateColorAsState(
                                    if (isSelected) {
                                        when (filter) {
                                            CashflowTypeFilter.INCOME -> Moss
                                            CashflowTypeFilter.EXPENSE -> Coral
                                            CashflowTypeFilter.ALL -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        }
                                    } else Color.Transparent,
                                    label = "filter_bg"
                                )
                                val textColor by animateColorAsState(
                                    if (isSelected) {
                                        if (filter == CashflowTypeFilter.ALL) MaterialTheme.colorScheme.onSurface else Color.White
                                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                                    label = "filter_text"
                                )

                                Surface(
                                    onClick = {
                                        selectedTypeFilter = filter
                                        selectedCategoryFilter = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = bgColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .bounceClick(minScale = 0.96f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            filter.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }

                        // دکمه کوچک و شیک باز کردن دسته‌بندی‌ها
                        Surface(
                            onClick = { showCategoryFilterSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedCategoryFilter != null) Moss.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (selectedCategoryFilter != null) BorderStroke(1.dp, Moss) else null,
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.FilterList,
                                    contentDescription = "فیلتر دسته",
                                    tint = if (selectedCategoryFilter != null) Moss else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "دسته‌ها",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedCategoryFilter != null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategoryFilter != null) Moss else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // اگر دسته‌بندی فیلتر شده بود، یک چیپ کوچک با دکمه حذف نمایش داده شود
                    selectedCategoryFilter?.let { catId ->
                        val cat = TransactionCategories.expenseCategories.firstOrNull { it.id == catId }
                            ?: TransactionCategories.incomeCategories.firstOrNull { it.id == catId }
                        if (cat != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Moss.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Moss.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(cat.emoji, fontSize = 12.sp)
                                        Text("فیلتر دسته: ${cat.title}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Moss)
                                        IconButton(
                                            onClick = { selectedCategoryFilter = null },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, "حذف فیلتر", tint = Moss, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ۴. فهرست تراکنش‌ها
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(if (selectedCategoryFilter != null) "🔍" else "📊", fontSize = 42.sp)
                            Text(
                                if (selectedCategoryFilter != null) "تراکنشی در این دسته‌بندی یافت نشد"
                                else "هنوز تراکنشی در این ماه ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "برای آگاهی دقیق از مانده دخل و خرج، مبالغ درآمد یا مخارج خود را ثبت نمایید.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Moss)
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("ثبت اولین تراکنش ✨", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ریز تراکنش‌ها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${filteredTransactions.size.faDigits()} تراکنش",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(filteredTransactions, key = { _, tx -> tx.id }) { index, tx ->
                    StaggeredItemEntrance(index = index) {
                        TransactionRowItem(
                            tx = tx,
                            isPrivacy = isPrivacyMode,
                            onDelete = { vm.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // پنجره انتخاب دسته‌بندی جهت فیلتر
    if (showCategoryFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فیلتر بر اساس دسته‌بندی موضوعی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (selectedCategoryFilter != null) {
                        TextButton(onClick = {
                            selectedCategoryFilter = null
                            showCategoryFilterSheet = false
                        }) {
                            Text("پاک کردن فیلتر", color = Coral, fontSize = 11.sp)
                        }
                    }
                }

                val availableCats = when (selectedTypeFilter) {
                    CashflowTypeFilter.INCOME -> TransactionCategories.incomeCategories
                    CashflowTypeFilter.EXPENSE -> TransactionCategories.expenseCategories
                    CashflowTypeFilter.ALL -> TransactionCategories.expenseCategories + TransactionCategories.incomeCategories
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCats) { cat ->
                        val isSelected = selectedCategoryFilter == cat.id
                        Surface(
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else cat.id
                                showCategoryFilterSheet = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) (if (cat.isIncome) Moss else Coral).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(1.5.dp, if (cat.isIncome) Moss else Coral) else null,
                            modifier = Modifier.bounceClick(minScale = 0.94f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(cat.emoji, fontSize = 20.sp)
                                Text(
                                    cat.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) (if (cat.isIncome) Moss else Coral) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, isIncome, category, note ->
                vm.addTransaction(title, amount, isIncome, category, LocalDate.now().toEpochDay(), note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EnhancedCashflowDashboardCard(
    cashflow: CashflowSummary,
    isPrivacy: Boolean
) {
    fun formatMoney(amount: Long): String =
        if (isPrivacy) "••••••" else "${amount.money()} ت"

    val totalFlow = (cashflow.totalIncome + cashflow.totalExpense).coerceAtLeast(1L)
    val incomeRatio = (cashflow.totalIncome.toFloat() / totalFlow).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, CardBorderGradient)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // سطر ۱: مانده خالص دخل و خرج + بج وضعیت تراز
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "مانده نهایی ماه (تراز نقدینگی)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    AnimatedMoneyText(
                        amount = cashflow.netBalance,
                        isPrivacy = isPrivacy,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (cashflow.netBalance >= 0) Moss else Coral
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (cashflow.netBalance >= 0) Moss.copy(alpha = 0.14f) else Coral.copy(alpha = 0.14f),
                    modifier = Modifier.pulseGlow(enabled = true, minScale = 0.96f, maxScale = 1.04f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            if (cashflow.netBalance >= 0) "📈 تراز مثبت" else "📉 کسری بودجه",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashflow.netBalance >= 0) Moss else Coral
                        )
                    }
                }
            }

            // نوار بصری نسبت درآمد به هزینه (Progress ratio)
            if (cashflow.totalIncome > 0 || cashflow.totalExpense > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("درآمد: ${(incomeRatio * 100).toInt().faDigits()}٪", fontSize = 9.sp, color = Moss, fontWeight = FontWeight.Bold)
                        Text("مخارج: ${((1f - incomeRatio) * 100).toInt().faDigits()}٪", fontSize = 9.sp, color = Coral, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Coral.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(incomeRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Moss)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // سطر ۲: سه ستون خلاصه (کل درآمد، کل مخارج، مانده پس از کسر اقساط)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // درآمد
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = CircleShape, color = Moss, modifier = Modifier.size(8.dp)) {}
                        Text("کل دریافتی", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        formatMoney(cashflow.totalIncome),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Moss
                    )
                }

                // مخارج
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = CircleShape, color = Coral, modifier = Modifier.size(8.dp)) {}
                        Text("کل پرداختی", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        formatMoney(cashflow.totalExpense),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Coral
                    )
                }

                // مانده پس از اقساط
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (cashflow.remainingAfterInstallments >= 0) Moss.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "پس از کسر اقساط",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatMoney(cashflow.remainingAfterInstallments),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashflow.remainingAfterInstallments >= 0) Moss else Coral
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    tx: Transaction,
    isPrivacy: Boolean,
    onDelete: () -> Unit
) {
    val category = TransactionCategories.get(tx.category, tx.isIncome)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            if (tx.isIncome) Moss.copy(alpha = 0.2f) else Coral.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // آیکون دسته‌بندی با کانتینر رنگی اختصاصی
            Surface(
                shape = CircleShape,
                color = if (tx.isIncome) Moss.copy(alpha = 0.14f) else Coral.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.emoji, fontSize = 18.sp)
                }
            }

            // اطلاعات تراکنش
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (tx.isIncome) Moss.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f)
                    ) {
                        Text(
                            category.title,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) Moss else Coral,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    "${LocalDate.ofEpochDay(tx.epochDay).formatJalali()}${if (tx.note.isNotBlank()) " • ${tx.note}" else ""}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // مبلغ با بج مثبت یا منفی
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (isPrivacy) "••••••" else "${if (tx.isIncome) "+" else "-"}${tx.amount.money()} ت",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.isIncome) Moss else Coral
                )
            }

            // دکمه حذف
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    "حذف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Long, isIncome: Boolean, category: String, note: String) -> Unit
) {
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var amountDigits by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf(if (isIncome) "salary" else "food") }
    var note by rememberSaveable { mutableStateOf("") }

    val amount = amountDigits.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && amount > 0

    val categories = if (isIncome) TransactionCategories.incomeCategories else TransactionCategories.expenseCategories

    val quickAmounts = listOf(50_000L, 100_000L, 500_000L, 1_000_000L, 5_000_000L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (isIncome) "💰" else "🛒", fontSize = 20.sp)
                Text(
                    if (isIncome) "ثبت درآمد و دریافتی جدید" else "ثبت هزینه و مخارج جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // سوئیچ تب تعاملی بین درآمد و هزینه
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp)
            ) {
                Surface(
                    onClick = {
                        isIncome = false
                        categoryId = "food"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(minScale = 0.96f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isIncome) Coral else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "هزینه و خرج 🛒",
                            fontWeight = FontWeight.Bold,
                            color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    onClick = {
                        isIncome = true
                        categoryId = "salary"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(minScale = 0.96f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isIncome) Moss else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "درآمد و واریزی 💰",
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // فیلد عنوان
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان تراکنش") },
                placeholder = { Text(if (isIncome) "مثلاً حقوق، دستمزد، سود سپرده" else "مثلاً خرید سوپرمارکت، بنزین، اجاره") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // فیلد مبلغ
            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v -> amountDigits = v.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ به تومان") },
                placeholder = { Text("مثلاً ۲۵۰,۰۰۰") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // مبالغ سریع (Quick Amount Chips)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickAmounts) { qAmount ->
                    SuggestionChip(
                        onClick = {
                            val cur = amountDigits.toLongOrNull() ?: 0L
                            amountDigits = (cur + qAmount).toString()
                        },
                        label = { Text("+${qAmount.money()}", fontSize = 10.sp) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // انتخاب دسته‌بندی
            Text("دسته‌بندی موضوعی:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = categoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { categoryId = cat.id },
                        leadingIcon = { Text(cat.emoji, fontSize = 12.sp) },
                        label = { Text(cat.title, fontSize = 11.sp) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isIncome) Moss else Coral,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // فیلد یادداشت اختیاری
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت و توضیحات (اختیاری)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // دکمه ثبت نهایی
            Button(
                onClick = { onSave(title, amount, isIncome, categoryId, note) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) Moss else Coral)
            ) {
                Text(
                    if (isIncome) "ثبت درآمد و افزایش موجودی ✅" else "ثبت هزینه و پرداخت ✅",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}
