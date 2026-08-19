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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.FinancialStats
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import com.iliyateam.ghestyar.ui.components.AnimatedMoneyText
import com.iliyateam.ghestyar.ui.components.InstallmentCard
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.pulseGlow
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    isPremium: Boolean = false,
    onAdd: () -> Unit,
    onDetail: (Installment) -> Unit,
    onEdit: (Installment) -> Unit,
    onPremium: () -> Unit,
    onExportExcel: () -> Unit = {},
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {}
) {
    val active by vm.active.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by vm.selectedCategoryFilter.collectAsStateWithLifecycle()

    val canAddMore = isPremium || active.size < 5
    var searchExpanded by remember { mutableStateOf(false) }
    var selectedFilterTab by remember { mutableIntStateOf(1) } // 0: همه, 1: فعال, 2: تسویه‌شده
    val isDark = isSystemInDarkTheme()

    val currentList = remember(active, history, selectedFilterTab, searchQuery, selectedCategory) {
        val baseList: List<Installment> = when (selectedFilterTab) {
            0 -> (active + history).sortedBy { it.dueEpochDay }
            1 -> active
            else -> history
        }
        baseList.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.note.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || item.category == selectedCategory
            matchQuery && matchCategory
        }
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
            // ۱. نوار بالا: سلام و خوش‌آمد، تاریخ شمسی، آواتار و دکمه‌های جستجو و VIP
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("ق", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column {
                            Text(
                                "سلام، خوش‌آمدید 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                LocalDate.now().formatJalaliWithWeekday(),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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

                        // دکمه VIP کپسولی
                        Surface(
                            onClick = onPremium,
                            shape = RoundedCornerShape(50),
                            color = if (isPremium) GoldVip.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .pulseGlow(enabled = !isPremium, minScale = 0.95f, maxScale = 1.05f)
                                .bounceClick(minScale = 0.92f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Star,
                                    null,
                                    tint = if (isPremium) GoldVip else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    if (isPremium) "VIP 👑" else "ارتقا",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) GoldVip else MaterialTheme.colorScheme.primary
                                )
                            }
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
                        placeholder = { Text("جستجو در عنوان اقساط...") },
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

            // ۲. کارت برجسته Hero: مانده کل بدهی و شاخص پرداخت
            item {
                HeroDebtSummaryCard(
                    stats = stats,
                    activeCount = active.size,
                    paidCount = history.size
                )
            }

            // ۳. کارت تونال قسط بعدی (Next Due Tonal Card)
            item {
                NextDueTonalCard(active = active)
            }

            // ۴. سوییچر سگمنتی ۳ حالته کپسولی (همه / فعال / تسویه‌شده)
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
                                "همه (${(active.size + history.size).faDigits()})",
                                "فعال (${active.size.faDigits()})",
                                "تسویه‌شده (${history.size.faDigits()})"
                            )

                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedFilterTab == index
                                Surface(
                                    onClick = { selectedFilterTab = index },
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

            // ۵. چیپ‌های فیلتر دسته‌بندی
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

            // ۶. نمایش لیست اقساط فیلترشده
            if (currentList.isEmpty()) {
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
                                "برای ثبت و مدیریت منظم وام‌ها و اقساط خود، دکمه افزودن طرح را بزنید.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(currentList, key = { _, item -> item.id }) { index, item ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        StaggeredItemEntrance(index = index) {
                            InstallmentCard(
                                item = item,
                                onClick = { onDetail(item) },
                                onPaid = { vm.markPaid(item) },
                                onEdit = { onEdit(item) },
                                onDelete = { vm.delete(item) }
                            )
                        }
                    }
                }
            }
        }

        // دکمه شناور FAB با گوشه‌های ۲۸dp
        FloatingActionButton(
            onClick = { if (canAddMore) onAdd() else onPremium() },
            shape = RoundedCornerShape(28.dp),
            containerColor = Moss,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp)
                .size(62.dp)
                .bounceClick(minScale = 0.90f)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "افزودن طرح", modifier = Modifier.size(30.dp))
        }
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
                    "مانده کل بدهی",
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

            // نوار سفید پیشرفت روی ترک تیره با فیزیک فنری
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
                        daysLeft > 1 -> "${daysLeft.toInt().faDigits()} روز مانده تا سررسید"
                        daysLeft == 1L -> "فردا موعد سررسید است 🔔"
                        daysLeft == 0L -> "امروز موعد پرداخت است ⚠️"
                        else -> "${(-daysLeft).toInt().faDigits()} روز تأخیر در پرداخت 🚨"
                    },
                    fontSize = 10.sp,
                    color = if (daysLeft < 0) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}