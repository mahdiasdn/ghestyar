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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var txToDelete by remember { mutableStateOf<Transaction?>(null) }
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
                            onEdit = { editingTransaction = tx },
                            onDelete = { txToDelete = tx }
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
        AddOrEditTransactionDialog(
            initialTx = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, isIncome, category, note, isRecurring ->
                vm.addTransaction(title, amount, isIncome, category, LocalDate.now().toEpochDay(), note, isRecurring)
                showAddDialog = false
            }
        )
    }

    editingTransaction?.let { oldTx ->
        AddOrEditTransactionDialog(
            initialTx = oldTx,
            onDismiss = { editingTransaction = null },
            onSave = { title, amount, isIncome, category, note, isRecurring ->
                vm.updateTransaction(oldTx, title, amount, isIncome, category, oldTx.epochDay, note, isRecurring)
                editingTransaction = null
            }
        )
    }

    txToDelete?.let { tx ->
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف تراکنش «${tx.title}»",
            message = "آیا از حذف این ${if (tx.isIncome) "درآمد" else "هزینه"} به مبلغ ${tx.amount.money()} تومان اطمینان دارید؟",
            onConfirm = {
                vm.deleteTransaction(tx)
                txToDelete = null
            },
            onDismiss = { txToDelete = null }
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

    val isPositive = cashflow.remainingAfterInstallments >= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ۱. کارت اصلی هیرو تراز نقدینگی خالص آزاد ماه (Hero Balance Hub)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(minScale = 0.98f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(
                1.dp,
                if (isPositive) Moss.copy(alpha = 0.35f) else Coral.copy(alpha = 0.35f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isPositive) Moss.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // سطر ۱: آیکون + عنوان تراز + بج وضعیت زنده
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
                                color = if (isPositive) Moss.copy(alpha = 0.18f) else Coral.copy(alpha = 0.18f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPositive) Moss else Coral,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "نقدینگی خالص آزاد این ماه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "پس از کسر هزینه‌ها و اقساط ماه جاری",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isPositive) Moss.copy(alpha = 0.15f) else Coral.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, if (isPositive) Moss.copy(alpha = 0.4f) else Coral.copy(alpha = 0.4f))
                        ) {
                            Text(
                                if (isPositive) "تراز مثبت ماه 📈" else "کسری بودجه 📉",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) Moss else Coral,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    // سطر ۲: عدد بزرگ نقدینگی خالص پس از کسر اقساط
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedMoneyText(
                            amount = cashflow.remainingAfterInstallments,
                            isPrivacy = isPrivacy,
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (isPositive) Moss else Coral
                        )
                        if (!isPrivacy) {
                            Text(
                                "تومان",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ۲. ردیف کارت‌های چهارگانه Bento (دریافتی‌ها، مخارج جاری، اقساط این ماه، چک‌ها و بدهی‌ها)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // کارت دریافتی‌ها
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, Moss.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = Moss.copy(alpha = 0.15f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = Moss, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("کل دریافتی", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(
                        formatMoney(cashflow.totalIncome),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Moss,
                        maxLines = 1
                    )
                }
            }

            // کارت پرداختی‌ها / مخارج جاری
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, Coral.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = Coral.copy(alpha = 0.15f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = Coral, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("مخارج جاری", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(
                        formatMoney(cashflow.totalExpense),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Coral,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // کارت اقساط این ماه
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = Color(0xFF0D9488).copy(alpha = 0.15f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("اقساط ماه", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(
                        formatMoney(cashflow.thisMonthInstallments),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488),
                        maxLines = 1
                    )
                }
            }

            // کارت چک‌ها و بدهی‌های پرداختی این ماه
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick(minScale = 0.96f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = ChequeBlue.copy(alpha = 0.15f), modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.HistoryEdu, contentDescription = null, tint = ChequeBlue, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("چک‌ها و بدهی", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(
                        formatMoney(cashflow.thisMonthPayableCheques),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChequeBlue,
                        maxLines = 1
                    )
                }
            }
        }

        // ۳. کارت تفکیک سهم کسری و مصارف ماه (مخارج + اقساط + چک‌ها)
        val totalOutflow = cashflow.totalMonthlyOutflow
        val isDeficit = cashflow.remainingAfterInstallments < 0
        val expenseRatio = if (totalOutflow > 0) (cashflow.totalExpense.toFloat() / totalOutflow).coerceIn(0f, 1f) else 0.33f
        val installmentRatio = if (totalOutflow > 0) (cashflow.thisMonthInstallments.toFloat() / totalOutflow).coerceIn(0f, 1f) else 0.33f
        val chequeRatio = if (totalOutflow > 0) (cashflow.thisMonthPayableCheques.toFloat() / totalOutflow).coerceIn(0f, 1f) else 0.33f
        val expensePercent = (expenseRatio * 100).toInt()
        val installmentPercent = (installmentRatio * 100).toInt()
        val chequePercent = (100 - expensePercent - installmentPercent).coerceAtLeast(0)

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                if (isDeficit) Coral.copy(alpha = 0.35f) else Moss.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(if (isDeficit) "⚠️" else "📊", fontSize = 14.sp)
                        Text(
                            if (isDeficit) "تفکیک و سهم کسری بودجه ماه" else "سهم‌بندی کل مصارف این ماه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "کل خروجی: ${if (isPrivacy) "••••••" else "${totalOutflow.money()} ت"}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // نوار گرافیکی چندقسمتی درصد مخارج vs اقساط vs چک‌ها
                if (totalOutflow > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (cashflow.totalExpense > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(expenseRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Coral)
                                )
                            }
                            if (cashflow.thisMonthInstallments > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(installmentRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Color(0xFF0D9488))
                                )
                            }
                            if (cashflow.thisMonthPayableCheques > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(chequeRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(ChequeBlue)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Coral)
                                )
                                Text(
                                    "مخارج: ${expensePercent.faDigits()}٪",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Coral
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D9488))
                                )
                                Text(
                                    "اقساط: ${installmentPercent.faDigits()}٪",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0D9488)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ChequeBlue)
                                )
                                Text(
                                    "چک‌ها: ${chequePercent.faDigits()}٪",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ChequeBlue
                                )
                            }
                        }
                    }
                }

                // متن توضیحی هوشمند
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isDeficit) Coral else Moss).copy(alpha = 0.08f)
                ) {
                    Text(
                        text = if (isDeficit) {
                            "کسری خالص ماه: ${kotlin.math.abs(cashflow.remainingAfterInstallments).money()} تومان است. مجموع تعهدات خروجی شامل ${cashflow.totalExpense.money()} تومان مخارج، ${cashflow.thisMonthInstallments.money()} تومان اقساط و ${cashflow.thisMonthPayableCheques.money()} تومان چک‌های این ماه از کل ورودی بیشتر است."
                        } else {
                            "پس از کسر تمام ${cashflow.totalExpense.money()} تومان مخارج، ${cashflow.thisMonthInstallments.money()} تومان اقساط و ${cashflow.thisMonthPayableCheques.money()} تومان چک‌های ماه، مبلغ ${cashflow.remainingAfterInstallments.money()} تومان نقدینگی خالص برای شما آزاد می‌ماند."
                        },
                        fontSize = 10.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDeficit) Coral else Moss,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    tx: Transaction,
    isPrivacy: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = TransactionCategories.get(tx.category, tx.isIncome)

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            if (tx.isIncome) Moss.copy(alpha = 0.25f) else Coral.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            // اطلاعات تراکنش (کاملاً مقاوم در برابر کرش و شکست عمودی متن)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (tx.isIncome) Moss.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = category.title,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) Moss else Coral,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (tx.isRecurring) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "🔄 ماهانه",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${LocalDate.ofEpochDay(tx.epochDay).formatJalali()}${if (tx.note.isNotBlank()) " • ${tx.note}" else ""}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // مبلغ با بج مثبت یا منفی
            Text(
                text = if (isPrivacy) "••••••" else "${if (tx.isIncome) "+" else "-"}${tx.amount.money()} ت",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.isIncome) Moss else Coral
            )

            // دکمه‌های ویرایش و حذف
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        "ویرایش",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        "حذف",
                        tint = Coral.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditTransactionDialog(
    initialTx: Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Long, isIncome: Boolean, category: String, note: String, isRecurring: Boolean) -> Unit
) {
    var isIncome by rememberSaveable { mutableStateOf(initialTx?.isIncome ?: false) }
    var title by rememberSaveable { mutableStateOf(initialTx?.title ?: "") }
    var amountDigits by rememberSaveable { mutableStateOf(initialTx?.amount?.toString() ?: "") }
    var categoryId by rememberSaveable { mutableStateOf(initialTx?.category ?: if (isIncome) "salary" else "food") }
    var note by rememberSaveable { mutableStateOf(initialTx?.note ?: "") }
    var isRecurring by rememberSaveable { mutableStateOf(initialTx?.isRecurring ?: false) }

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
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (isIncome) "💰" else "🛒", fontSize = 20.sp)
                Text(
                    if (initialTx != null) "ویرایش تراکنش دخل و خرج" else if (isIncome) "ثبت درآمد و دریافتی جدید" else "ثبت هزینه و مخارج جدید",
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
                onValueChange = { v -> amountDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ به تومان") },
                placeholder = { Text("مثلاً ۲۵۰,۰۰۰") },
                trailingIcon = {
                    if (amountDigits.isNotEmpty()) {
                        IconButton(onClick = { amountDigits = "" }) {
                            Icon(Icons.Rounded.Close, "پاک کردن", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // نمایش زنده مبلغ به حروف
            if (amount > 0L) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isIncome) Moss else Coral).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, (if (isIncome) Moss else Coral).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✍️", fontSize = 12.sp)
                        Text(
                            "معادل: ${amount.toPersianWords("تومان")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isIncome) Moss else Coral
                        )
                    }
                }
            }

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

            // انتخاب نوع تکرار (ثابت ماهانه یا موردی)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = (if (isIncome) Moss else Coral).copy(alpha = 0.16f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isRecurring) "🔄" else "✨", fontSize = 16.sp)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (isIncome) (if (isRecurring) "درآمد ثابت ماهانه" else "درآمد موردی و یک‌باره")
                                else (if (isRecurring) "هزینه ثابت ماهانه" else "هزینه موردی و یک‌باره"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isIncome) (if (isRecurring) "محاسبه در پیش‌بینی ۶ ماه آینده (مثل حقوق)" else "فقط مربوط به دخل همین ماه (مثل پاداش)")
                                else (if (isRecurring) "تکرار مستمر در هر ماه (مثل اجاره یا شارژ)" else "صرفاً خرج همین ماه (مثل خرید یا سفر)"),
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isIncome) Moss else Coral
                        )
                    )
                }
            }

            // دکمه ثبت نهایی
            Button(
                onClick = { onSave(title, amount, isIncome, categoryId, note, isRecurring) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) Moss else Coral)
            ) {
                Text(
                    if (initialTx != null) "ذخیره تغییرات تراکنش ✅" else if (isIncome) "ثبت درآمد و افزایش موجودی ✅" else "ثبت هزینه و پرداخت ✅",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}
