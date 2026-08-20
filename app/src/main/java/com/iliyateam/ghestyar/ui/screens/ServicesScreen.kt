// ═══ ui/screens/ServicesScreen.kt ═══
package com.iliyateam.ghestyar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iliyateam.ghestyar.MainViewModel
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.SavingsGoal
import java.time.LocalDate
import com.iliyateam.ghestyar.ui.components.ConfettiBurst
import com.iliyateam.ghestyar.ui.components.StaggeredItemEntrance
import com.iliyateam.ghestyar.ui.components.VipGateContainer
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.shimmerBrush
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*

/**
 * پنجره‌های مجزای خدمات و ابزارها
 */
enum class ActiveServiceWindow(val title: String, val emoji: String, val isVip: Boolean) {
    NONE("مرکز خدمات", "🧮", false),
    SAVINGS_GOALS("قلک و اهداف پس‌انداز", "🎯", false),
    LOAN_POOLS("صندوق‌های وام خانوادگی", "👥", true),
    AVALANCHE_SAVER("کاهش سود بانکی (بهمن)", "💸", true),
    CASHFLOW_FORECAST("پیش‌بینی نقدینگی ۶ ماهه", "🔮", true),
    SMS_STUDIO("استودیو و پیامک‌ساز هوشمند", "✉️", true),
    BOOKLET_GENERATOR("دفترچه‌ساز رسمی بانکی PDF", "📑", true),
    HOME_WIDGET("ویجت صفحه اصلی گوشی", "📱", true)
}

private data class ServiceGridItem(
    val window: ActiveServiceWindow?,
    val title: String,
    val description: String,
    val emoji: String,
    val icon: ImageVector,
    val badge: String,
    val isVip: Boolean,
    val colorPrimary: Color,
    val colorContainer: Color,
    val onCustomClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    vm: MainViewModel,
    isPremium: Boolean,
    onOpenPremium: () -> Unit,
    onOpenCalculator: () -> Unit = {},
    onGenerateBooklet: (Installment) -> Unit = {}
) {
    val goals by vm.savingsGoals.collectAsStateWithLifecycle()
    val pendingCheques by vm.pendingChequesAndDebts.collectAsStateWithLifecycle()
    val loanPoolsWithMembers by vm.loanPoolsWithMembers.collectAsStateWithLifecycle()
    val activeInstallments by vm.active.collectAsStateWithLifecycle()
    val cashflow by vm.cashflowSummary.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val isPrivacyMode by vm.isPrivacyMode.collectAsStateWithLifecycle()

    var activeWindow by rememberSaveable { mutableStateOf(ActiveServiceWindow.NONE) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    // مدیریت دکمه بازگشت فیزیکی گوشی به صفحه اصلی خدمات
    BackHandler(enabled = activeWindow != ActiveServiceWindow.NONE) {
        activeWindow = ActiveServiceWindow.NONE
    }

    AnimatedContent(
        targetState = activeWindow,
        transitionSpec = {
            if (targetState != ActiveServiceWindow.NONE) {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
            } else {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
            }
        },
        label = "ServiceWindowTransition"
    ) { currentWindow ->
        if (currentWindow == ActiveServiceWindow.NONE) {
            // ══════════════════════════════════════════════════════════════
            // ۱. نمای اصلی: گرید مدرن و جذاب مرکز خدمات
            // ══════════════════════════════════════════════════════════════
            ServicesGridHub(
                goalsCount = goals.size,
                loanPoolsCount = loanPoolsWithMembers.size,
                isPremium = isPremium,
                onOpenWindow = { window ->
                    if (window.isVip && !isPremium) {
                        onOpenPremium()
                    } else {
                        activeWindow = window
                    }
                },
                onOpenCalculator = onOpenCalculator
            )
        } else {
            // ══════════════════════════════════════════════════════════════
            // ۲. پنجره اختصاصی و متمرکز خدمت انتخاب‌شده
            // ══════════════════════════════════════════════════════════════
            ServiceDedicatedWindow(
                window = currentWindow,
                onBack = { activeWindow = ActiveServiceWindow.NONE },
                onAddGoal = { showAddGoalDialog = true }
            ) {
                when (currentWindow) {
                    ActiveServiceWindow.SAVINGS_GOALS -> {
                        SavingsGoalsWindowContent(
                            goals = goals,
                            isPrivacy = isPrivacyMode,
                            onAddGoal = { showAddGoalDialog = true },
                            onEdit = { goal -> editingGoal = goal },
                            onDeposit = { goal, amt -> vm.depositToGoal(goal, amt) },
                            onDelete = { goal -> vm.deleteSavingsGoal(goal) }
                        )
                    }
                    ActiveServiceWindow.LOAN_POOLS -> {
                        LoanPoolSection(
                            poolsWithMembers = loanPoolsWithMembers,
                            isPremium = isPremium,
                            onOpenPremium = onOpenPremium,
                            vm = vm
                        )
                    }
                    ActiveServiceWindow.AVALANCHE_SAVER -> {
                        VipGateContainer(
                            isPremium = isPremium,
                            featureTitle = "کاهش سود بانکی (بهمن)",
                            featureDescription = "محاسبه تسویه زودهنگام و صرفه‌جویی چند میلیونی در سود بازپرداخت وام‌ها",
                            onOpenPremium = onOpenPremium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AvalancheInterestSaverCard(
                                    installments = activeInstallments,
                                    isPremium = isPremium,
                                    onOpenPremium = onOpenPremium
                                )
                            }
                        }
                    }
                    ActiveServiceWindow.CASHFLOW_FORECAST -> {
                        VipGateContainer(
                            isPremium = isPremium,
                            featureTitle = "پیش‌بینی نقدینگی ۶ ماهه",
                            featureDescription = "تحلیل آینده‌نگر موجودی، تعهدات و هشدار ماه‌های دارای کسری بودجه",
                            onOpenPremium = onOpenPremium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SixMonthCashflowForecastCard(
                                    installments = activeInstallments,
                                    cheques = pendingCheques,
                                    cashflow = cashflow
                                )
                            }
                        }
                    }
                    ActiveServiceWindow.SMS_STUDIO -> {
                        VipGateContainer(
                            isPremium = isPremium,
                            featureTitle = "استودیو پیامک‌ساز و یادآور",
                            featureDescription = "ارسال پیام‌های رسمی و دوستانه با جزئیات کامل شماره شبا و کارت",
                            onOpenPremium = onOpenPremium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SmartSmsReminderCard(
                                    installments = activeInstallments,
                                    cheques = pendingCheques
                                )
                            }
                        }
                    }
                    ActiveServiceWindow.BOOKLET_GENERATOR -> {
                        VipGateContainer(
                            isPremium = isPremium,
                            featureTitle = "دفترچه‌ساز رسمی بانکی PDF",
                            featureDescription = "تولید سند رسمی دو صفحه‌ای A4 بانکی همراه با جدول ماه به ماه، بارکد و محل امضا",
                            onOpenPremium = onOpenPremium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                LoanBookletGeneratorCard(
                                    installments = activeInstallments,
                                    onGenerateBooklet = onGenerateBooklet
                                )
                            }
                        }
                    }
                    ActiveServiceWindow.HOME_WIDGET -> {
                        VipGateContainer(
                            isPremium = isPremium,
                            featureTitle = "ویجت هوشمند صفحه اصلی",
                            featureDescription = "روزشمار زنده و لحظه‌ای سررسید اقساط روی لانچر و صفحه قفل گوشی",
                            onOpenPremium = onOpenPremium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HomeScreenWidgetCard(
                                    installments = activeInstallments
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddOrEditGoalBottomSheet(
            initialGoal = null,
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, current, due, emoji ->
                vm.addSavingsGoal(title, target, current, due, emoji)
                showAddGoalDialog = false
            }
        )
    }

    editingGoal?.let { oldGoal ->
        AddOrEditGoalBottomSheet(
            initialGoal = oldGoal,
            onDismiss = { editingGoal = null },
            onSave = { title, target, current, due, emoji ->
                vm.updateSavingsGoal(oldGoal, title, target, current, due, emoji, oldGoal.note)
                editingGoal = null
            }
        )
    }
}

/**
 * نمای شبکه کاشی‌بندی (Grid Hub) مرکز خدمات
 */
@Composable
private fun ServicesGridHub(
    goalsCount: Int,
    loanPoolsCount: Int,
    isPremium: Boolean,
    onOpenWindow: (ActiveServiceWindow) -> Unit,
    onOpenCalculator: () -> Unit
) {
    val items = remember(goalsCount, loanPoolsCount, isPremium) {
        listOf(
            ServiceGridItem(
                window = ActiveServiceWindow.SAVINGS_GOALS,
                title = "قلک و اهداف مالی",
                description = "پس‌انداز هدفمند برای خریدهای آینده",
                emoji = "🎯",
                icon = Icons.Rounded.Savings,
                badge = if (goalsCount > 0) "${goalsCount.faDigits()} هدف" else "رایگان",
                isVip = false,
                colorPrimary = Color(0xFFD97706),
                colorContainer = Color(0xFFFEF3C7)
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.LOAN_POOLS,
                title = "صندوق وام خانوادگی",
                description = "قرعه‌کشی گردونه شانس و واریزی‌ها",
                emoji = "👥",
                icon = Icons.Rounded.Groups,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Color(0xFF7C3AED),
                colorContainer = Color(0xFFEDE9FE)
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.AVALANCHE_SAVER,
                title = "کاهش سود بانکی",
                description = "استراتژی تسویه بهمن و صرفه‌جویی",
                emoji = "💸",
                icon = Icons.Rounded.AutoAwesome,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Color(0xFF059669),
                colorContainer = Color(0xFFA7F3D0)
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.CASHFLOW_FORECAST,
                title = "پیش‌بینی نقدینگی",
                description = "تحلیل ۶ ماه آینده و هشدار کسری",
                emoji = "🔮",
                icon = Icons.Rounded.Timeline,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Color(0xFF0284C7),
                colorContainer = Color(0xFFE0F2FE)
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.SMS_STUDIO,
                title = "استودیو پیامک‌ساز",
                description = "یادآور هوشمند با شماره شبا و کارت",
                emoji = "✉️",
                icon = Icons.AutoMirrored.Rounded.Send,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Color(0xFFE11D48),
                colorContainer = Color(0xFFFFE4E6)
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.BOOKLET_GENERATOR,
                title = "دفترچه‌ساز رسمی",
                description = "تولید فایل PDF بانکی A4 با بارکد",
                emoji = "📑",
                icon = Icons.Rounded.PictureAsPdf,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Moss,
                colorContainer = MintSoft
            ),
            ServiceGridItem(
                window = ActiveServiceWindow.HOME_WIDGET,
                title = "ویجت صفحه اصلی",
                description = "روزشمار زنده روی لانچر گوشی",
                emoji = "📱",
                icon = Icons.Rounded.Widgets,
                badge = "⭐ VIP",
                isVip = true,
                colorPrimary = Color(0xFF0D9488),
                colorContainer = Color(0xFFCCFBF1)
            ),
            ServiceGridItem(
                window = null,
                title = "ماشین‌حساب اقساط",
                description = "محاسبه سود وام ۴٪، ۱۸٪ و آزاد",
                emoji = "🧮",
                icon = Icons.Rounded.Calculate,
                badge = "رایگان",
                isVip = false,
                colorPrimary = Color(0xFF475569),
                colorContainer = Color(0xFFF1F5F9),
                onCustomClick = onOpenCalculator
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // هدر بالای مرکز خدمات
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldVip,
                    modifier = Modifier.size(46.dp),
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🧮", fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "مرکز خدمات و ابزارهای مالی",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "جعبه‌ابزار هوشمند، محاسبات VIP و خدمات ویژه قسط‌یار",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // کارت‌های ۲ ستونه شبکه خدمات (2-Column Grid Layout)
        val chunkedItems = items.chunked(2)
        itemsIndexed(chunkedItems) { rowIndex, rowList ->
            StaggeredItemEntrance(index = rowIndex) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowList.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            ServiceGridCard(
                                item = item,
                                isPremium = isPremium,
                                onClick = {
                                    if (item.onCustomClick != null) {
                                        item.onCustomClick.invoke()
                                    } else if (item.window != null) {
                                        onOpenWindow(item.window)
                                    }
                                }
                            )
                        }
                    }
                    if (rowList.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * کارت تکی در گرید خدمات
 */
@Composable
private fun ServiceGridCard(
    item: ServiceGridItem,
    isPremium: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .bounceClick(minScale = 0.96f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            if (item.isVip) GoldVip.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) item.colorPrimary.copy(alpha = 0.22f) else item.colorContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(item.emoji, fontSize = 20.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (item.isVip) GoldVip.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        item.badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isVip) GoldVip else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp,
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ورود",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isVip) GoldVip else Moss
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    null,
                    tint = if (item.isVip) GoldVip else Moss,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * فریم پنجره اختصاصی و متمرکز برای هر خدمت
 */
@Composable
private fun ServiceDedicatedWindow(
    window: ActiveServiceWindow,
    onBack: () -> Unit,
    onAddGoal: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // نوار هدر پنجره اختصاصی
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "بازگشت به مرکز خدمات",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            window.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (window.isVip) {
                            Surface(shape = RoundedCornerShape(50), color = GoldVip.copy(alpha = 0.2f)) {
                                Text("⭐ VIP", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GoldVip, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text("مرکز ابزارها و خدمات ویژه", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (window == ActiveServiceWindow.SAVINGS_GOALS) {
                Button(
                    onClick = onAddGoal,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVip),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.bounceClick(minScale = 0.94f)
                ) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("هدف جدید", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.Black)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

        // محتوای پنجره
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * محتوای پنجره قلک و اهداف پس‌انداز
 */
@Composable
private fun SavingsGoalsWindowContent(
    goals: List<SavingsGoal>,
    isPrivacy: Boolean,
    onAddGoal: () -> Unit,
    onEdit: (SavingsGoal) -> Unit,
    onDeposit: (SavingsGoal, Long) -> Unit,
    onDelete: (SavingsGoal) -> Unit
) {
    if (goals.isEmpty()) {
        EmptyGoalView(onAddGoal = onAddGoal)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(goals, key = { _, goal -> goal.id }) { index, goal ->
                StaggeredItemEntrance(index = index) {
                    GoalCard(
                        goal = goal,
                        isPrivacy = isPrivacy,
                        onEdit = { onEdit(goal) },
                        onDeposit = { amount -> onDeposit(goal, amount) },
                        onDelete = { onDelete(goal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    isPrivacy: Boolean,
    onEdit: () -> Unit,
    onDeposit: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
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
        onClick = onEdit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                // اطلاعات عنوان و مبالغ بدون شکست عمودی
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (goal.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF10B981).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    "تکمیل شد 🎉",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        if (isPrivacy) "هدف: ••••••" else "هدف: ${goal.targetAmount.money()} تومان",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.16f) else GoldVip.copy(alpha = 0.16f)
                ) {
                    Text(
                        "${percentInt.faDigits()}٪",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) Color(0xFF10B981) else GoldVip,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "ویرایش هدف",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = { showConfirmDelete = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "حذف هدف",
                            tint = Coral.copy(alpha = 0.8f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // نوار پیشرفت
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val fillBrush = if (goal.isCompleted) {
                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                } else {
                    Brush.horizontalGradient(listOf(GoldVip, GoldLight))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedGoalProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(fillBrush)
                            .shimmerBrush(durationMillis = 2000)
                    )
                }

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

            // دکمه واریز وجه
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
        DepositBottomSheet(
            goalTitle = goal.title,
            onDismiss = { showDepositDialog = false },
            onConfirm = { amount ->
                onDeposit(amount)
                showDepositDialog = false
            }
        )
    }

    if (showConfirmDelete) {
        com.iliyateam.ghestyar.ui.components.ConfirmDeleteDialog(
            title = "حذف قلک «${goal.title}»",
            message = "آیا از حذف این هدف پس‌انداز با موجودی ${goal.currentAmount.money()} تومان اطمینان دارید؟",
            onConfirm = {
                showConfirmDelete = false
                onDelete()
            },
            onDismiss = { showConfirmDelete = false }
        )
    }
}

@Composable
private fun EmptyGoalView(onAddGoal: () -> Unit) {
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
            Text("🎯", fontSize = 48.sp)
            Text("هنوز قلک هدفی تعریف نکردی!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "برای خرید گوشی، سفر، طلا یا خودرو قلک هدف بساز و پس‌اندازت را ثبت کن.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAddGoal,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip),
                modifier = Modifier.bounceClick(minScale = 0.94f)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text("ایجاد اولین قلک هدف ✨", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepositBottomSheet(
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
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("واریز وجه به «$goalTitle»", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = if (amountDigits.isEmpty()) "" else amount.money(),
                onValueChange = { v -> amountDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ واریزی (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            if (amount > 0L) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Moss.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Moss.copy(alpha = 0.25f)),
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
                            color = Moss
                        )
                    }
                }
            }

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
private fun AddOrEditGoalBottomSheet(
    initialGoal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Long, currentAmount: Long, due: JalaliDate, emoji: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialGoal?.title ?: "") }
    var targetDigits by rememberSaveable { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var currentDigits by rememberSaveable { mutableStateOf(initialGoal?.currentAmount?.toString() ?: "") }
    var due by rememberSaveable {
        mutableStateOf(
            initialGoal?.targetEpochDay?.let { LocalDate.ofEpochDay(it).toJalali() }
                ?: JalaliDate.today().plusMonths(6)
        )
    }
    var selectedEmoji by rememberSaveable { mutableStateOf(initialGoal?.emoji ?: "📱") }

    val emojis = listOf("📱", "🚗", "🏠", "✈️", "💍", "💻", "🎓", "🪙", "🏖️", "🎁", "🛵", "⌚")
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
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (initialGoal != null) "ویرایش قلک و هدف پس‌انداز" else "ایجاد قلک و هدف پس‌انداز جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text("آیکون هدف", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emojis) { emoji ->
                    Surface(
                        onClick = { selectedEmoji = emoji },
                        shape = CircleShape,
                        color = if (selectedEmoji == emoji) GoldVip.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (selectedEmoji == emoji) BorderStroke(2.dp, GoldVip) else null,
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
                onValueChange = { v -> targetDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ کل هدف (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (currentDigits.isEmpty()) "" else current.money(),
                onValueChange = { v -> currentDigits = v.cleanNumericDigits(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("موجودی فعلی پس‌انداز شده (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Button(
                onClick = { onSave(title, target, current, due, selectedEmoji) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldVip)
            ) {
                Text(
                    if (initialGoal != null) "ذخیره تغییرات قلک ✅" else "ایجاد قلک هدف ✨",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
