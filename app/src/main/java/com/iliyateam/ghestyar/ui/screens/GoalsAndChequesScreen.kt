// ═══ ui/screens/GoalsAndChequesScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.SavingsGoal
import com.iliyateam.ghestyar.ui.components.ConfettiBurst
import com.iliyateam.ghestyar.ui.components.ReceiptShareHelper
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.shimmerBrush
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndChequesScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenPremium: () -> Unit
) {
    val goals by vm.savingsGoals.collectAsStateWithLifecycle()
    val pendingCheques by vm.pendingChequesAndDebts.collectAsStateWithLifecycle()
    val clearedCheques by vm.clearedChequesAndDebts.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: قلک اهداف, 1: چک و قرض
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddChequeDialog by remember { mutableStateOf(false) }
    var selectedChequeForReceipt by remember { mutableStateOf<ChequeOrDebt?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // نوار بالا با statusBarsPadding و دکمه ثبت سریع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldVip,
                    modifier = Modifier.size(38.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (activeTab == 0) "🎯" else "✍️", fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "قلک اهداف و چک‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("مدیریت تعهدات و برنامه‌ریزی", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // دکمه ارگونومیک ثبت در هدر بر اساس تب فعال
            Button(
                onClick = {
                    if (activeTab == 0) showAddGoalDialog = true else showAddChequeDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == 0) GoldVip else Moss),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text(
                    if (activeTab == 0) "هدف جدید" else "ثبت چک / قرض",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        // تب‌های سوییچ M3
        PrimaryTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("قلک و اهداف (${goals.size.faDigits()})", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("چک و قرض‌ها (${pendingCheques.size.faDigits()})", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        when (activeTab) {
            0 -> {
                // تب قلک و اهداف پس‌انداز
                if (goals.isEmpty()) {
                    EmptyStateView(
                        emoji = "🎯",
                        title = "هنوز هدفی تعریف نکردی!",
                        sub = "برای خرید گوشی، سفر، طلا یا خودرو قلک هدف بساز و پس‌اندازت را ثبت کن.",
                        actionText = "ایجاد اولین هدف ✨",
                        onAction = { showAddGoalDialog = true }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(goals, key = { _, goal -> goal.id }) { index, goal ->
                            StaggeredItemEntrance(index = index) {
                                SavingsGoalCard(
                                    goal = goal,
                                    isPrivacy = isPrivacyMode,
                                    onDeposit = { amount -> vm.depositToGoal(goal, amount) },
                                    onDelete = { vm.deleteSavingsGoal(goal) }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // تب چک و قرض‌ها
                if (pendingCheques.isEmpty() && clearedCheques.isEmpty()) {
                    EmptyStateView(
                        emoji = "✍️",
                        title = "هیچ چک یا طلبی ثبت نشده!",
                        sub = "چک‌های صیادی و طلب‌ها یا بدهی‌های شخصی‌ات را اینجا ثبت و رهگیری کن.",
                        actionText = "ثبت چک یا قرض جدید 🤝",
                        onAction = { showAddChequeDialog = true }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(pendingCheques, key = { _, item -> item.id }) { index, item ->
                            StaggeredItemEntrance(index = index) {
                                ChequeRowItem(
                                    item = item,
                                    isPrivacy = isPrivacyMode,
                                    onToggle = { vm.toggleChequeCleared(item) },
                                    onShowReceipt = { selectedChequeForReceipt = item },
                                    onDelete = { vm.deleteChequeOrDebt(item) }
                                )
                            }
                        }

                        if (clearedCheques.isNotEmpty()) {
                            item {
                                Text(
                                    "تسویه‌شده‌ها (${clearedCheques.size.faDigits()})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                                )
                            }
                            itemsIndexed(clearedCheques, key = { _, item -> item.id }) { index, item ->
                                StaggeredItemEntrance(index = index + pendingCheques.size) {
                                    ChequeRowItem(
                                        item = item,
                                        isPrivacy = isPrivacyMode,
                                        onToggle = { vm.toggleChequeCleared(item) },
                                        onShowReceipt = { selectedChequeForReceipt = item },
                                        onDelete = { vm.deleteChequeOrDebt(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, current, due, emoji ->
                vm.addSavingsGoal(title, target, current, due, emoji)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddChequeDialog) {
        AddChequeDialog(
            onDismiss = { showAddChequeDialog = false },
            onSave = { title, person, amount, isCheque, isReceivable, due, chNum, bank ->
                vm.addChequeOrDebt(title, person, amount, isCheque, isReceivable, due, chNum, bank)
                showAddChequeDialog = false
            }
        )
    }

    selectedChequeForReceipt?.let { cheque ->
        com.iliyateam.ghestyar.ui.components.ChequeReceiptCardDialog(
            item = cheque,
            onDismiss = { selectedChequeForReceipt = null }
        )
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    isPrivacy: Boolean,
    onDeposit: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    var celebrating by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val animatedGoalProgress by animateFloatAsState(
        targetValue = goal.progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f),
        label = "GoalAnimatedProgress"
    )

    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
    val percentInt = (goal.progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.5f)
            else if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            else GoldVip.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // سطر اول: آیکون، عنوان هدف و بج وضعیت درصد
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.16f) else GoldVip.copy(alpha = 0.16f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(goal.emoji, fontSize = 24.sp)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (goal.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF10B981).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    "تکمیل شد 🎉",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        if (isPrivacy) "هدف: ••••••" else "هدف نهایی: ${goal.targetAmount.money()} تومان",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.18f) else GoldVip.copy(alpha = 0.18f),
                    border = BorderStroke(
                        1.dp,
                        if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.4f) else GoldVip.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        "${percentInt.faDigits()}٪",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) Color(0xFF10B981) else (if (isDark) GoldLight else Color(0xFFB45309)),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }

            // ۲. نوار پر شدن فوق‌العاده گرافیکی و مدرن (M3 Expressive Stadium Track)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    // نوار گرادیان پرشونده
                    val fillBrush = if (goal.isCompleted) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedGoalProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(fillBrush)
                            .shimmerBrush(durationMillis = 2000)
                    )

                    // نشانگرهای نقطه‌ای تارگت ۲۵٪، ۵۰٪، ۷۵٪
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.25f, 0.50f, 0.75f).forEach { milestone ->
                            Surface(
                                shape = CircleShape,
                                color = if (animatedGoalProgress >= milestone) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(4.dp)
                            ) {}
                        }
                    }
                }

                // اطلاعات واریز شده و باقیمانده
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isPrivacy) "پس‌انداز: ••••••" else "ذخیره: ${goal.currentAmount.money()} ت",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) Color(0xFF10B981) else (if (isDark) MossLight else Moss)
                    )

                    if (!goal.isCompleted) {
                        Text(
                            if (isPrivacy) "مانده: ••••••" else "مانده: ${remainingAmount.money()} ت",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ۳. دکمه واریز سریع با جلوه ذرات جشن
            Box(modifier = Modifier.fillMaxWidth()) {
                ConfettiBurst(
                    active = celebrating,
                    color = GoldVip,
                    modifier = Modifier.align(Alignment.Center).size(100.dp)
                )

                Button(
                    onClick = { showDepositDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (goal.isCompleted) Color(0xFF10B981) else (if (isDark) Color(0xFF282F30) else Moss),
                        contentColor = if (goal.isCompleted) Color.White else (if (isDark) MossLight else Color.White)
                    ),
                    border = if (isDark && !goal.isCompleted) BorderStroke(1.dp, MossLight.copy(alpha = 0.3f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .bounceClick(minScale = 0.95f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        if (goal.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircle,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (goal.isCompleted) "افزایش بیشتر پس‌انداز ✨" else "واریز وجه به قلک 🪙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDepositDialog) {
        DepositGoalDialog(
            goalTitle = goal.title,
            onDismiss = { showDepositDialog = false },
            onConfirm = { amount ->
                onDeposit(amount)
                showDepositDialog = false
            }
        )
    }
}

@Composable
private fun ChequeRowItem(
    item: ChequeOrDebt,
    isPrivacy: Boolean,
    onToggle: () -> Unit,
    onShowReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    val due = LocalDate.ofEpochDay(item.dueEpochDay)

    Card(
        onClick = onShowReceipt,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(minScale = 0.98f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, ChequeBlue.copy(alpha = 0.2f))
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

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (item.isCheque) "✍️" else "🤝", fontSize = 12.sp)
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (item.isReceivable) ChequeBlue.copy(alpha = 0.14f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            if (item.isReceivable) "طلبکاریم" else "بدهکاریم",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isReceivable) ChequeBlue else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "طرف حساب: ${item.personName} • موعد: ${due.formatJalali()}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                if (isPrivacy) "••••••" else "${item.amount.money()} ت",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.isReceivable) ChequeBlue else Coral
            )

            // دکمه باز کردن کارت تصویری چک و طلب
            IconButton(
                onClick = onShowReceipt,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Outlined.Share, "کارت تصویری یادآوری", tint = ChequeBlue, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Delete, "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    emoji: String,
    title: String,
    sub: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 48.sp)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                sub,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(actionText, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Long, currentAmount: Long, due: JalaliDate, emoji: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var targetDigits by rememberSaveable { mutableStateOf("") }
    var currentDigits by rememberSaveable { mutableStateOf("") }
    var due by rememberSaveable { mutableStateOf(JalaliDate.today().plusMonths(6)) }
    var selectedEmoji by rememberSaveable { mutableStateOf("📱") }
    var showDatePicker by remember { mutableStateOf(false) }

    val emojis = listOf("📱", "🚗", "🏠", "✈️", "💍", "💻", "🎓", "🪙", "🏖️", "🎁")
    val target = targetDigits.toLongOrNull() ?: 0L
    val current = currentDigits.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && target > 0

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("ایجاد قلک و هدف پس‌انداز جدید", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            // انتخاب ایموجی هدف
            Text("آیکون هدف", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emojis) { emoji ->
                    Surface(
                        onClick = { selectedEmoji = emoji },
                        shape = CircleShape,
                        color = if (selectedEmoji == emoji) GoldVip.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (selectedEmoji == emoji) androidx.compose.foundation.BorderStroke(2.dp, GoldVip) else null,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان هدف (مثلاً خرید آیفون ۱۶)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (targetDigits.isEmpty()) "" else target.money(),
                onValueChange = { v -> targetDigits = v.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ کل هدف (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (currentDigits.isEmpty()) "" else current.money(),
                onValueChange = { v -> currentDigits = v.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("پس‌انداز اولیه تا الان (تومان - اختیاری)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

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
                    Text("تاریخ هدف برای رسیدن به پس‌انداز:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(due.toLocalDate().formatJalali(), fontWeight = FontWeight.Bold, color = GoldVip)
                }
            }

            Button(
                onClick = {
                    onSave(title, target, current, due, selectedEmoji)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Text("ایجاد قلک هدف 🎯", fontWeight = FontWeight.Bold, color = Color.White)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepositGoalDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountDigits by rememberSaveable { mutableStateOf("") }
    val amount = amountDigits.toLongOrNull() ?: 0L

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("واریز پس‌انداز به «$goalTitle»", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v -> amountDigits = v.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ واریزی به قلک (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // دکمه‌های سریع
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(500_000L, 1_000_000L, 2_000_000L, 5_000_000L).forEach { quickVal ->
                    OutlinedButton(
                        onClick = { amountDigits = quickVal.toString() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("${(quickVal / 1000).faDigits()} هـ", fontSize = 10.sp)
                    }
                }
            }

            Button(
                onClick = { onConfirm(amount) },
                enabled = amount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss)
            ) {
                Text("ثبت واریز ✅", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChequeDialog(
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
    var isCheque by rememberSaveable { mutableStateOf(true) }
    var isReceivable by rememberSaveable { mutableStateOf(false) } // پیش‌فرض: ما باید پرداخت کنیم (بدهکاریم)
    var title by rememberSaveable { mutableStateOf("") }
    var personName by rememberSaveable { mutableStateOf("") }
    var amountDigits by rememberSaveable { mutableStateOf("") }
    var chequeNumber by rememberSaveable { mutableStateOf("") }
    var bankName by rememberSaveable { mutableStateOf("") }
    var due by rememberSaveable { mutableStateOf(JalaliDate.today().plusMonths(1)) }
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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ثبت چک یا بدهی / طلب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            // نوع مورد (چک / قرض)
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
                onValueChange = { v -> amountDigits = v.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

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
                Text("ثبت در سیستم ✅", fontWeight = FontWeight.Bold, color = Color.White)
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
