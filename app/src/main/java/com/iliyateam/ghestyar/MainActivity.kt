// ═══ MainActivity.kt ═══
package com.iliyateam.ghestyar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.ui.components.PinLockDialog
import com.iliyateam.ghestyar.ui.components.bounceClick
import com.iliyateam.ghestyar.ui.components.tabTransitionSpec
import com.iliyateam.ghestyar.ui.screens.*
import com.iliyateam.ghestyar.ui.theme.*
import com.iliyateam.ghestyar.util.*
import com.iliyateam.ghestyar.widget.GhestYarWidgetProvider
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String, val icon: ImageVector) {
    OVERVIEW("داشبورد", Icons.Rounded.Dashboard),
    INSTALLMENTS("اقساط", Icons.AutoMirrored.Rounded.ReceiptLong),
    CASHFLOW("دخل‌خرج", Icons.Rounded.AccountBalanceWallet),
    GOALS_CHEQUES("قلک و چک", Icons.Rounded.Savings),
    SETTINGS("تنظیمات", Icons.Rounded.Settings)
}

enum class ActiveScreen {
    MAIN,
    ADD_INSTALLMENT,
    ABOUT
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val createCsv =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        Exporter.csv(this@MainActivity, uri, vm.allOnce())
                        Toast.makeText(this@MainActivity, "خروجی اکسل با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "خطا در خروجی فایل: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private val createJson =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        Exporter.jsonBackup(this@MainActivity, uri, vm.allOnce())
                        Toast.makeText(this@MainActivity, "پشتیبان با موفقیت ذخیره شد ☁️", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "خطا در ذخیره فایل پشتیبان: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private val restoreJson =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        val restoredItems = Exporter.jsonRestore(this@MainActivity, uri)
                        vm.restoreBackup(restoredItems) { count ->
                            Toast.makeText(
                                this@MainActivity,
                                "تعداد ${count.faDigits()} قسط با موفقیت بازیابی شد ✅",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "خطا در بازیابی فایل: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            val fontScale by vm.fontScale.collectAsStateWithLifecycle()

            QestYarTheme(themeMode = themeMode, fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AppRoot()
                    }
                }
            }
        }
    }

    @Composable
    private fun AppRoot() {
        val context = LocalContext.current
        var showSplash by remember { mutableStateOf(true) }

        if (showSplash) {
            SplashScreen(onFinish = { showSplash = false })
            return
        }

        val isPinLockEnabled by vm.isPinLockEnabled.collectAsStateWithLifecycle()
        val pinCode by vm.pinCode.collectAsStateWithLifecycle()
        var isUnlocked by remember { mutableStateOf(!isPinLockEnabled) }

        val mainPagerState = rememberPagerState(initialPage = 0, pageCount = { MainNavTab.entries.size })
        val coroutineScope = rememberCoroutineScope()

        var currentScreen by remember { mutableStateOf(ActiveScreen.MAIN) }
        var editingInstallment by remember { mutableStateOf<Installment?>(null) }
        var detailedInstallment by remember { mutableStateOf<Installment?>(null) }
        var showPremium by remember { mutableStateOf(false) }
        var showLoanCalculator by remember { mutableStateOf(false) }
        var isPremium by remember { mutableStateOf(Premium.isPremium(context)) }

        val activeInstallments by vm.active.collectAsStateWithLifecycle()
        val historyInstallments by vm.history.collectAsStateWithLifecycle()
        val stats by vm.stats.collectAsStateWithLifecycle()

        // ─── مدیریت کلید بازگشت سیستمی (BackHandler) ───
        BackHandler(enabled = currentScreen != ActiveScreen.MAIN) {
            currentScreen = ActiveScreen.MAIN
            editingInstallment = null
        }

        BackHandler(enabled = showPremium) {
            showPremium = false
        }

        BackHandler(enabled = showLoanCalculator) {
            showLoanCalculator = false
        }

        BackHandler(enabled = detailedInstallment != null) {
            detailedInstallment = null
        }

        val switchToTab: (Int) -> Unit = { targetIndex ->
            if (mainPagerState.currentPage != targetIndex) {
                coroutineScope.launch {
                    if (kotlin.math.abs(mainPagerState.currentPage - targetIndex) <= 1) {
                        mainPagerState.animateScrollToPage(
                            targetIndex,
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        )
                    } else {
                        mainPagerState.scrollToPage(targetIndex)
                    }
                }
            }
        }

        BackHandler(
            enabled = currentScreen == ActiveScreen.MAIN && !showPremium && !showLoanCalculator && detailedInstallment == null &&
                    mainPagerState.currentPage != 0
        ) {
            switchToTab(0)
        }

        // قفل پین کد در صورت فعال بودن
        if (isPinLockEnabled && !isUnlocked) {
            PinLockDialog(
                correctPin = pinCode,
                onSuccess = { isUnlocked = true }
            )
            return
        }

        AnimatedContent(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            transitionSpec = {
                when {
                    targetState == ActiveScreen.ADD_INSTALLMENT -> {
                        (slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.93f))
                            .togetherWith(slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut())
                    }
                    initialState == ActiveScreen.ADD_INSTALLMENT -> {
                        (slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn())
                            .togetherWith(slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut() + scaleOut(targetScale = 0.93f))
                    }
                    targetState == ActiveScreen.ABOUT -> {
                        (slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn())
                            .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
                    }
                    else -> {
                        (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                            .togetherWith(slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut())
                    }
                }
            },
            label = "ScreenAnimatedTransition"
        ) { screen ->
            when (screen) {
                ActiveScreen.ABOUT -> {
                    AboutScreen(onBack = { currentScreen = ActiveScreen.MAIN })
                }
                ActiveScreen.ADD_INSTALLMENT -> {
                    val itemToEdit = editingInstallment
                    AddInstallmentScreen(
                        editingItem = itemToEdit,
                        onBack = {
                            currentScreen = ActiveScreen.MAIN
                            editingInstallment = null
                        },
                        onSave = { title, amount, due, sessions, color, category, remind, note ->
                            if (itemToEdit != null) {
                                vm.update(itemToEdit, title, amount, due, sessions, color, category, remind, note)
                                Toast.makeText(context, "تغییرات با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                vm.add(title, amount, due, sessions, color, category, remind, note)
                                Toast.makeText(
                                    context,
                                    if (remind) "قسط ثبت شد و یادآوری فعال است 🔔" else "قسط با موفقیت ثبت شد ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            currentScreen = ActiveScreen.MAIN
                            editingInstallment = null
                        }
                    )
                }
                ActiveScreen.MAIN -> {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                MainNavTab.entries.forEach { tab ->
                                    val isSelected = mainPagerState.currentPage == tab.ordinal
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.18f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.6f,
                                            stiffness = 500f
                                        ),
                                        label = "nav_icon_scale"
                                    )
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { switchToTab(tab.ordinal) },
                                        icon = {
                                            Icon(
                                                tab.icon,
                                                contentDescription = tab.title,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                            )
                                        },
                                        label = {
                                            Text(
                                                tab.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    ) { pad ->
                        HorizontalPager(
                            state = mainPagerState,
                            beyondViewportPageCount = 0,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(pad)
                        ) { page ->
                            when (MainNavTab.entries[page]) {
                                MainNavTab.OVERVIEW -> {
                                    OverviewScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenCalculator = { showLoanCalculator = true },
                                        onAddInstallment = { currentScreen = ActiveScreen.ADD_INSTALLMENT },
                                        onOpenCashflow = { switchToTab(MainNavTab.CASHFLOW.ordinal) },
                                        onOpenGoalsCheques = { switchToTab(MainNavTab.GOALS_CHEQUES.ordinal) },
                                        onDetailInstallment = { item -> detailedInstallment = item }
                                    )
                                }
                                MainNavTab.INSTALLMENTS -> {
                                    HomeScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onAdd = { currentScreen = ActiveScreen.ADD_INSTALLMENT },
                                        onEdit = { item ->
                                            editingInstallment = item
                                            currentScreen = ActiveScreen.ADD_INSTALLMENT
                                        },
                                        onDetail = { item -> detailedInstallment = item },
                                        onPremium = { showPremium = true },
                                        onExportExcel = { createCsv.launch("ghestyar-report.csv") },
                                        onBackup = { createJson.launch("ghestyar-backup.json") },
                                        onRestore = { restoreJson.launch(arrayOf("application/json", "*/*")) }
                                    )
                                }
                                MainNavTab.CASHFLOW -> {
                                    CashflowScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenPremium = { showPremium = true }
                                    )
                                }
                                MainNavTab.GOALS_CHEQUES -> {
                                    GoalsAndChequesScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenPremium = { showPremium = true }
                                    )
                                }
                                MainNavTab.SETTINGS -> {
                                    SettingsScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenCalculator = { showLoanCalculator = true },
                                        onOpenPremium = { showPremium = true },
                                        onOpenAbout = { currentScreen = ActiveScreen.ABOUT },
                                        onExportExcel = { createCsv.launch("ghestyar-report.csv") },
                                        onBackup = { createJson.launch("ghestyar-backup.json") },
                                        onRestore = { restoreJson.launch(arrayOf("application/json", "*/*")) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        detailedInstallment?.let { item ->
            InstallmentDetailSheet(
                item = item,
                onDismiss = { detailedInstallment = null },
                onEdit = {
                    detailedInstallment = null
                    editingInstallment = item
                    currentScreen = ActiveScreen.ADD_INSTALLMENT
                },
                onPayCurrent = {
                    vm.markPaid(item)
                    com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(context)
                    Toast.makeText(context, "قسط پرداخت شد! 🎉", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    vm.delete(item)
                    com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(context)
                    Toast.makeText(context, "قسط حذف شد 🗑️", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showLoanCalculator) {
            LoanCalculatorSheet(
                onDismiss = { showLoanCalculator = false },
                onAddAsInstallment = { title, monthly, sessions ->
                    vm.add(
                        title = title,
                        amount = monthly,
                        due = JalaliDate.today().plusMonths(1),
                        sessions = sessions,
                        colorIndex = 0,
                        category = "bank",
                        remind = true,
                        note = "محاسبه‌شده با ماشین‌حساب وام بانکی"
                    )
                    com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(context)
                    Toast.makeText(context, "قسط «$title» به لیست اقساط اضافه شد ✅", Toast.LENGTH_LONG).show()
                }
            )
        }

        if (showPremium) {
            PremiumSheet(
                isCurrentlyPremium = isPremium,
                onDismiss = { showPremium = false },
                onUnlock = { selectedTier ->
                    Premium.setPremium(context, true, selectedTier)
                    isPremium = true
                    showPremium = false
                    Toast.makeText(
                        context,
                        "اشتراک ${selectedTier.title} با موفقیت فعال شد! ✨👑",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onResetFree = {
                    Premium.setPremium(context, false)
                    isPremium = false
                    showPremium = false
                    Toast.makeText(context, "حالت به نسخه رایگان تغییر یافت", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}