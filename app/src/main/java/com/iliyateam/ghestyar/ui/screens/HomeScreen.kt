// ═══ ui/screens/HomeScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.*
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * صفحه اصلی جامع اقساط و چک‌ها
 * ادغام کامل مدیریت اقساط وام‌ها، چک‌های صیادی و قرض‌های شخصی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    isPremium: Boolean = false,
    onAdd: () -> Unit,
    onDetail: (Installment) -> Unit,
    onEdit: (Installment) -> Unit,
    onPremium: () -> Unit,
    onOpenAnalytics: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {}
) {
    val activeInstallments by vm.active.collectAsStateWithLifecycle()
    val historyInstallments by vm.history.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by vm.selectedCategoryFilter.collectAsStateWithLifecycle()

    val pendingCheques by vm.pendingChequesAndDebts.collectAsStateWithLifecycle()
    val clearedCheques by vm.clearedChequesAndDebts.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    val canAddMore = isPremium || activeInstallments.size < 5
    var searchExpanded by remember { mutableStateOf(false) }
    var mainSectionTab by rememberSaveable { mutableIntStateOf(0) } // 0: اقساط و وام‌ها, 1: چک‌ها و مطالبات
    var installmentFilterTab by rememberSaveable { mutableIntStateOf(1) } // 0: همه, 1: فعال, 2: تسویه‌شده
    var chequeFilterType by rememberSaveable { mutableIntStateOf(0) } // 0: همه, 1: چک‌های صیادی, 2: طلب و بدهی

    var showAddChequeDialog by remember { mutableStateOf(false) }
    var editingCheque by remember { mutableStateOf<ChequeOrDebt?>(null) }
    var selectedChequeForReceipt by remember { mutableStateOf<ChequeOrDebt?>(null) }
    var chequeToDelete by remember { mutableStateOf<ChequeOrDebt?>(null) }

    val isDark = isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // فیلتر هوشمند اقساط
    val currentInstallments = remember(activeInstallments, historyInstallments, installmentFilterTab, searchQuery, selectedCategory) {
        val baseList: List<Installment> = when (installmentFilterTab) {
            0 -> (activeInstallments + historyInstallments).sortedBy { it.dueEpochDay }
            1 -> activeInstallments
            else -> historyInstallments
        }
        baseList.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.note.contains(searchQuery, ignoreCase = true) ||
                    item.destination.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || item.category == selectedCategory
            matchQuery && matchCategory
        }
    }

    // فیلتر هوشمند چک‌ها
    val filteredPendingCheques = remember(pendingCheques, chequeFilterType, searchQuery) {
        val typeFiltered = when (chequeFilterType) {
            1 -> pendingCheques.filter { it.isCheque }
            2 -> pendingCheques.filter { !it.isCheque }
            else -> pendingCheques
        }
        if (searchQuery.isBlank()) typeFiltered
        else typeFiltered.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.personName.contains(searchQuery, ignoreCase = true) ||
                    it.bankName.contains(searchQuery, ignoreCase = true) ||
                    it.chequeNumber.contains(searchQuery, ignoreCase = true) ||
                    it.note.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredClearedCheques = remember(clearedCheques, chequeFilterType, searchQuery) {
        val typeFiltered = when (chequeFilterType) {
            1 -> clearedCheques.filter { it.isCheque }
            2 -> clearedCheques.filter { !it.isCheque }
            else -> clearedCheques
        }
        if (searchQuery.isBlank()) typeFiltered
        else typeFiltered.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.personName.contains(searchQuery, ignoreCase = true) ||
                    it.bankName.contains(searchQuery, ignoreCase = true) ||
                    it.chequeNumber.contains(searchQuery, ignoreCase = true) ||
                    it.note.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalPayableCheques = remember(pendingCheques) {
        pendingCheques.filter { !it.isReceivable }.sumOf { it.amount }
    }
    val totalReceivableCheques = remember(pendingCheques) {
        pendingCheques.filter { it.isReceivable }.sumOf { it.amount }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ۱. نوار بالا: لوگو، عنوان، تاریخ شمسی و دکمه‌های اقدام سریع
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppLogo(
                            modifier = Modifier
                                .size(44.dp)
                                .bounceClick(minScale = 0.92f)
                        )

                        Column {
                            Text(
                                "مدیریت اقساط و چک‌ها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${activeInstallments.size.faDigits()} قسط • ${pendingCheques.size.faDigits()} چک در انتظار • ${Jalali.months[JalaliDate.today().jm - 1]} ${JalaliDate.today().jy.faDigits()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onExportExcel,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Rounded.PictureAsPdf,
                                contentDescription = "گزارش‌گیری PDF و اکسل",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onOpenAnalytics,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Analytics,
                                contentDescription = "آمار و تحلیل",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { searchExpanded = !searchExpanded },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                if (searchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = "جستجو",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // فیلد بازشونده جستجو
            item {
                AnimatedVisibility(
                    visible = searchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { vm.setSearch(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        placeholder = {
                            Text(if (mainSectionTab == 0) "جستجو در عنوان و مقصد اقساط..." else "جستجو در چک‌ها، نام طرف حساب و بانک...")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { vm.setSearch("") }) {
                                    Icon(Icons.Rounded.Clear, null)
                                }
                            }
                        }
                    )
                }
            }

            // ۲. سوئیچر سگمنتی اصلی (اقساط و وام‌ها / چک‌ها و مطالبات)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val mainTabs = listOf(
                            0 to ("اقساط و وام‌ها (${activeInstallments.size.faDigits()})" to Icons.AutoMirrored.Rounded.ReceiptLong),
                            1 to ("چک‌ها و مطالبات (${pendingCheques.size.faDigits()})" to Icons.Rounded.HistoryEdu)
                        )

                        mainTabs.forEach { (index, data) ->
                            val (title, icon) = data
                            val isSelected = mainSectionTab == index
                            Surface(
                                onClick = { mainSectionTab = index },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Moss else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick(minScale = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ۳. محتوای تب انتخاب‌شده (اقساط یا چک‌ها)
            if (mainSectionTab == 0) {
                // ══════════════════════════════════════════════════════════════
                // الف) بخش اقساط و وام‌ها
                // ══════════════════════════════════════════════════════════════
                item {
                    HeroDebtSummaryCard(
                        stats = stats,
                        activeCount = activeInstallments.size,
                        paidCount = historyInstallments.size
                    )
                }

                item {
                    NextDueTonalCard(active = activeInstallments)
                }

                // سوییچر ۳ حالته وضعیت اقساط (همه / فعال / تسویه‌شده)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val tabs = listOf(
                                    "همه (${(activeInstallments.size + historyInstallments.size).faDigits()})",
                                    "فعال (${activeInstallments.size.faDigits()})",
                                    "تسویه‌شده (${historyInstallments.size.faDigits()})"
                                )

                                tabs.forEachIndexed { index, title ->
                                    val isSelected = installmentFilterTab == index
                                    Surface(
                                        onClick = { installmentFilterTab = index },
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) (if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else Moss) else Color.Transparent,
                                        border = if (isSelected && isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick(minScale = 0.96f)
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) (if (isDark) MossLight else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // چیپ‌های فیلتر دسته‌بندی اقساط
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { vm.setCategoryFilter(null) },
                                label = { Text("همه دسته‌ها", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Moss,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        items(InstallmentCategories.list) { cat ->
                            val isSelected = selectedCategory == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.setCategoryFilter(if (isSelected) null else cat.id) },
                                label = { Text("${cat.emoji} ${cat.title}", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Moss,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // نمایش لیست اقساط فیلترشده
                if (currentInstallments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("✨", fontSize = 44.sp)
                                Text(
                                    "هیچ قسطی در این بخش یافت نشد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "برای ثبت و مدیریت منظم وام‌ها و اقساط خود، دکمه افزودن را بزنید.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(currentInstallments, key = { _, item -> item.id }) { index, item ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            StaggeredItemEntrance(index = index) {
                                InstallmentCard(
                                    item = item,
                                    onClick = { onDetail(item) },
                                    onPaid = {
                                        vm.markPaid(item)
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            val result = snackbarHostState.showSnackbar(
                                                message = "قسط «${item.title}» پرداخت شد ✅",
                                                actionLabel = "بازگردانی ↩️",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                vm.unmarkPaid(item)
                                            }
                                        }
                                    },
                                    onUnmarkPaid = { vm.unmarkPaid(item) },
                                    onEdit = { onEdit(item) },
                                    onDelete = { vm.delete(item) }
                                )
                            }
                        }
                    }
                }
            } else {
                // ══════════════════════════════════════════════════════════════
                // ب) بخش چک‌ها و مطالبات
                // ══════════════════════════════════════════════════════════════
                // کارت‌های خلاصه بدهی و طلب Bento
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(minScale = 0.96f),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, Coral.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("مجموع بدهی‌های چکی", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (isPrivacyMode) "••••••" else "${totalPayableCheques.money()} ت",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Coral
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(minScale = 0.96f),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("مجموع مطالبات و طلب‌ها", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (isPrivacyMode) "••••••" else "${totalReceivableCheques.money()} ت",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ChequeBlue
                                )
                            }
                        }
                    }
                }

                // فیلترهای نوع چک (همه، چک صیادی، قرض و طلب)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("همه موارد", "چک‌های صیادی ✍️", "قرض و طلب 🤝").forEachIndexed { idx, label ->
                            val selected = chequeFilterType == idx
                            FilterChip(
                                selected = selected,
                                onClick = { chequeFilterType = idx },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (idx == 1) ChequeBlue else Moss,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (filteredPendingCheques.isEmpty() && filteredClearedCheques.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(if (chequeFilterType == 1) "✍️" else "🤝", fontSize = 44.sp)
                                Text(
                                    if (chequeFilterType == 1) "هیچ چک صیادی یافت نشد" else "هیچ چک یا طلب و بدهی ثبت نشده",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "چک‌های صیادی و طلب‌ها یا بدهی‌های شخصی‌ات را اینجا ثبت و رهگیری کن.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { showAddChequeDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Moss),
                                    modifier = Modifier.bounceClick(minScale = 0.94f)
                                ) {
                                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text("ثبت چک یا طلب جدید", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    if (filteredPendingCheques.isNotEmpty()) {
                        item {
                            Text(
                                "در انتظار سررسید (${filteredPendingCheques.size.faDigits()})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                        itemsIndexed(filteredPendingCheques, key = { _, item -> item.id }) { index, item ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                StaggeredItemEntrance(index = index) {
                                    ChequeRowItem(
                                        item = item,
                                        isPrivacy = isPrivacyMode,
                                        onToggle = { vm.toggleChequeCleared(item) },
                                        onEdit = { editingCheque = item },
                                        onShowReceipt = { selectedChequeForReceipt = item },
                                        onDelete = { chequeToDelete = item }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredClearedCheques.isNotEmpty()) {
                        item {
                            Text(
                                "تسویه‌شده‌ها (${filteredClearedCheques.size.faDigits()})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp, start = 24.dp, end = 24.dp)
                            )
                        }
                        itemsIndexed(filteredClearedCheques, key = { _, item -> item.id }) { index, item ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                StaggeredItemEntrance(index = index + filteredPendingCheques.size) {
                                    ChequeRowItem(
                                        item = item,
                                        isPrivacy = isPrivacyMode,
                                        onToggle = { vm.toggleChequeCleared(item) },
                                        onEdit = { editingCheque = item },
                                        onShowReceipt = { selectedChequeForReceipt = item },
                                        onDelete = { chequeToDelete = item }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
        )

        // دکمه شناور FAB هوشمند با گوشه‌های ۲۸dp
        FloatingActionButton(
            onClick = {
                if (mainSectionTab == 0) {
                    if (canAddMore) onAdd() else onPremium()
                } else {
                    showAddChequeDialog = true
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Moss,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp)
                .size(62.dp)
                .bounceClick(minScale = 0.90f)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "افزودن", modifier = Modifier.size(30.dp))
        }
    }

    // دیالوگ‌های اختصاصی چک و طلب
    if (showAddChequeDialog) {
        AddOrEditChequeDialog(
            initialItem = null,
            onDismiss = { showAddChequeDialog = false },
            onSave = { title, person, amount, isCheque, isReceivable, due, chNum, bank ->
                vm.addChequeOrDebt(title, person, amount, isCheque, isReceivable, due, chNum, bank)
                showAddChequeDialog = false
            }
        )
    }

    editingCheque?.let { oldCheque ->
        AddOrEditChequeDialog(
            initialItem = oldCheque,
            onDismiss = { editingCheque = null },
            onSave = { title, person, amount, isCheque, isReceivable, due, chNum, bank ->
                vm.updateChequeOrDebt(oldCheque, title, person, amount, isCheque, isReceivable, due, chNum, bank, oldCheque.note)
                editingCheque = null
            }
        )
    }

    selectedChequeForReceipt?.let { cheque ->
        ChequeReceiptCardDialog(
            item = cheque,
            onDismiss = { selectedChequeForReceipt = null }
        )
    }

    chequeToDelete?.let { cheque ->
        ConfirmDeleteDialog(
            title = "حذف «${cheque.title}»",
            message = "آیا از حذف این ${if (cheque.isCheque) "چک صیادی" else "قرض/طلب"} به مبلغ ${cheque.amount.money()} تومان اطمینان دارید؟",
            onConfirm = {
                vm.deleteChequeOrDebt(cheque)
                chequeToDelete = null
            },
            onDismiss = { chequeToDelete = null }
        )
    }
}

/**
 * ردیف کارت نمایش هر چک یا طلب
 */
@Composable
private fun ChequeRowItem(
    item: ChequeOrDebt,
    isPrivacy: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onShowReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    val due = LocalDate.ofEpochDay(item.dueEpochDay)

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = item.isCleared,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = ChequeBlue)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
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
                    Text(if (item.isCheque) "✍️" else "🤝", fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (item.isReceivable) ChequeBlue.copy(alpha = 0.14f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (item.isReceivable) "طلبکاریم" else "بدهکاریم",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isReceivable) ChequeBlue else MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "طرف: ${item.personName} • موعد: ${due.formatJalali()}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (isPrivacy) "••••••" else "${item.amount.money()} ت",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.isReceivable) ChequeBlue else Coral
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onShowReceipt,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Share, "کارت تصویری یادآوری", tint = ChequeBlue, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Edit, "ویرایش", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Delete, "حذف", tint = Coral.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * باتم‌شیت ثبت و ویرایش چک یا بدهی/طلب
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditChequeDialog(
    initialItem: ChequeOrDebt? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        personName: String,
        amount: Long,
        isCheque: Boolean,
        isReceivable: Boolean,
        due: JalaliDate,
        chequeNumber: String,
        bankName: String
    ) -> Unit
) {
    var isCheque by rememberSaveable { mutableStateOf(initialItem?.isCheque ?: true) }
    var isReceivable by rememberSaveable { mutableStateOf(initialItem?.isReceivable ?: false) }
    var title by rememberSaveable { mutableStateOf(initialItem?.title ?: "") }
    var personName by rememberSaveable { mutableStateOf(initialItem?.personName ?: "") }
    var amountDigits by rememberSaveable { mutableStateOf(initialItem?.amount?.toString() ?: "") }
    var chequeNumber by rememberSaveable { mutableStateOf(initialItem?.chequeNumber ?: "") }
    var bankName by rememberSaveable { mutableStateOf(initialItem?.bankName ?: "") }
    var due by rememberSaveable {
        mutableStateOf(
            initialItem?.dueEpochDay?.let { LocalDate.ofEpochDay(it).toJalali() }
                ?: JalaliDate.today().plusMonths(1)
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val amount = amountDigits.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && personName.isNotBlank() && amount > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initialItem != null) "ویرایش چک یا بدهی / طلب" else "ثبت چک یا بدهی / طلب",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // نوع مورد (چک صیادی / قرض شخصی)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(3.dp)
            ) {
                Surface(
                    onClick = { isCheque = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCheque) Moss else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text("چک صیادی ✍️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCheque) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Surface(
                    onClick = { isCheque = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isCheque) Moss else Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text("قرض شخصی 🤝", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isCheque) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // طلب یا بدهی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isReceivable,
                    onClick = { isReceivable = false },
                    label = { Text("بدهکاریم (پرداختی)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isReceivable,
                    onClick = { isReceivable = true },
                    label = { Text("طلبکاریم (دریافتی)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("بابت چه چیزی؟") },
                placeholder = { Text("مثلاً خرید لپ‌تاپ یا قرض رفاقتی") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام طرف حساب (شخص / شرکت)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v -> amountDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ (تومان)") },
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

            if (amount > 0L) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isReceivable) Moss else Coral).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, (if (isReceivable) Moss else Coral).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✍️", fontSize = 12.sp)
                        Text(
                            "معادل: ${amount.toPersianWords("تومان")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isReceivable) Moss else Coral
                        )
                    }
                }
            }

            Card(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تاریخ سررسید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(due.toLocalDate().formatJalali(), fontWeight = FontWeight.Bold, color = Moss)
                }
            }

            Button(
                onClick = { onSave(title, personName, amount, isCheque, isReceivable, due, chequeNumber, bankName) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text(
                    if (initialItem != null) "ذخیره تغییرات ✅" else "ثبت در سیستم ✅",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerSheet(
            initial = due,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                due = it
                showDatePicker = false
            }
        )
    }
}

/**
 * کارت برجسته Hero: مانده کل بدهی با پیشرفت پرداخت
 */
@Composable
private fun HeroDebtSummaryCard(
    stats: FinancialStats,
    activeCount: Int,
    paidCount: Int
) {
    val totalCount = activeCount + paidCount
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1F2527) else Color(0xFF006A6E)),
        border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    "مانده کل بدهی اقساط",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                AnimatedMoneyText(
                    amount = stats.totalActiveDebt,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    suffix = "تومان"
                )
            }

            val animatedHeroProgress by animateFloatAsState(
                targetValue = (stats.overallHealthPercentage / 100f).coerceIn(0f, 1f),
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
                label = "HeroDebtProgressAnim"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedHeroProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (isDark) MossLight else Color.White,
                    trackColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.22f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "پرداخت شده ${stats.overallHealthPercentage.toInt().faDigits()}٪",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (totalCount > 0) {
                        Text(
                            "قسط ${paidCount.faDigits()} از ${totalCount.faDigits()}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * کارت تونال قسط بعدی (Next Due Tonal Card)
 */
@Composable
private fun NextDueTonalCard(active: List<Installment>) {
    val next = active.firstOrNull() ?: return
    val due = LocalDate.ofEpochDay(next.dueEpochDay)
    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due)
    val category = InstallmentCategories.get(next.category)
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MintSoft.copy(alpha = 0.85f)
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MintSoft
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else Color.White,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.emoji, fontSize = 24.sp)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "قسط بعدی — ${next.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                AnimatedMoneyText(
                    amount = next.amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MossLight else Moss,
                    suffix = "تومان"
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        daysLeft > 1 -> "${due.formatJalali()} (${daysLeft.toInt().faDigits()} روز دیگر)"
                        daysLeft == 1L -> "فردا (${due.formatJalali()}) 🔔"
                        daysLeft == 0L -> "امروز موعد پرداخت است (${due.formatJalali()}) ⚠️"
                        else -> "${(-daysLeft).toInt().faDigits()} روز تاخیر! (${due.formatJalali()}) 🚨"
                    },
                    fontSize = 10.5.sp,
                    color = if (daysLeft < 0) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}