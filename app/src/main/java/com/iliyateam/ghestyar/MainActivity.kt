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
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.time.LocalDate

enum class MainNavTab(val title: String, val icon: ImageVector) {
    OVERVIEW("داشبورد", Icons.Rounded.Dashboard),
    INSTALLMENTS("اقساط", Icons.AutoMirrored.Rounded.ReceiptLong),
    CASHFLOW("دخل‌خرج", Icons.Rounded.AccountBalanceWallet),
    SERVICES("خدمات", Icons.Rounded.Widgets),
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

    private var pendingExportItems: List<Installment> = emptyList()
    private var pendingExportScope: String = "همه اقساط"
    private var pendingExportIsPdf: Boolean = true

    private val createExportDoc =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        if (pendingExportIsPdf) {
                            Exporter.pdf(this@MainActivity, uri, pendingExportItems, pendingExportScope)
                            Toast.makeText(this@MainActivity, "گزارش PDF با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                        } else {
                            Exporter.excelXlsx(this@MainActivity, uri, pendingExportItems, pendingExportScope)
                            Toast.makeText(this@MainActivity, "فایل اکسل (.xlsx) با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "خطا در ذخیره فایل: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private var pendingBookletInstallment: Installment? = null
    private val createBookletDoc =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            if (uri != null && pendingBookletInstallment != null) {
                lifecycleScope.launch {
                    try {
                        Exporter.loanBookletPdf(this@MainActivity, uri, pendingBookletInstallment!!)
                        Toast.makeText(this@MainActivity, "دفترچه رسمی PDF با موفقیت صادر شد 🖨️", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "خطا در ایجاد دفترچه: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private val createJson =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        val backupData = vm.getFullBackupData()
                        Exporter.jsonBackup(this@MainActivity, uri, backupData)
                        Toast.makeText(this@MainActivity, "پشتیبان جامع تمام اطلاعات با موفقیت ذخیره شد 💾", Toast.LENGTH_SHORT).show()
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
                        val restoredData = Exporter.jsonRestore(this@MainActivity, uri)
                        vm.restoreFullBackup(restoredData) { iCount, tCount, gCount, cCount ->
                            val parts = mutableListOf<String>()
                            if (iCount > 0) parts.add("${iCount.faDigits()} قسط")
                            if (tCount > 0) parts.add("${tCount.faDigits()} تراکنش")
                            if (gCount > 0) parts.add("${gCount.faDigits()} قلک")
                            if (cCount > 0) parts.add("${cCount.faDigits()} چک و طلب")
                            val summary = if (parts.isEmpty()) "اطلاعاتی یافت نشد" else parts.joinToString("، ")
                            Toast.makeText(
                                this@MainActivity,
                                "بازیابی کامل با موفقیت انجام شد ✅ ($summary)",
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
            val vipTheme by vm.vipColorTheme.collectAsStateWithLifecycle()

            QestYarTheme(themeMode = themeMode, fontScale = fontScale, vipTheme = vipTheme) {
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

    companion object {
        var hasShownSplash: Boolean = false
    }

    @Composable
    private fun AppRoot() {
        val context = LocalContext.current
        var showSplash by rememberSaveable { mutableStateOf(!hasShownSplash) }

        if (showSplash) {
            SplashScreen(onFinish = {
                hasShownSplash = true
                showSplash = false
            })
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
        var showExportReports by rememberSaveable { mutableStateOf(false) }
        var isPremium by remember { mutableStateOf(Premium.isPremium(context)) }
        var overviewSubTab by rememberSaveable { mutableIntStateOf(0) }

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

        BackHandler(enabled = showExportReports) {
            showExportReports = false
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
                        onSave = { title, amount, due, sessions, color, category, remind, note, destination ->
                            if (itemToEdit != null) {
                                vm.update(itemToEdit, title, amount, due, sessions, color, category, remind, note, destination)
                                Toast.makeText(context, "تغییرات با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                vm.add(title, amount, due, sessions, color, category, remind, note, destination)
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
                        topBar = {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                ) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        },
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
                                        onDetailInstallment = { item -> detailedInstallment = item },
                                        onPremium = { showPremium = true },
                                        selectedDashboardTab = overviewSubTab,
                                        onDashboardTabChange = { overviewSubTab = it }
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
                                        onOpenAnalytics = {
                                            overviewSubTab = 1
                                            switchToTab(MainNavTab.OVERVIEW.ordinal)
                                        },
                                        onExportExcel = { showExportReports = true },
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
                                MainNavTab.SERVICES -> {
                                    ServicesScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenPremium = { showPremium = true },
                                        onOpenCalculator = { showLoanCalculator = true },
                                        onGenerateBooklet = { inst ->
                                            pendingBookletInstallment = inst
                                            createBookletDoc.launch("دفترچه_اقساط_${inst.title}.pdf")
                                        }
                                    )
                                }
                                MainNavTab.SETTINGS -> {
                                    SettingsScreen(
                                        vm = vm,
                                        isPremium = isPremium,
                                        onOpenCalculator = { showLoanCalculator = true },
                                        onOpenPremium = { showPremium = true },
                                        onOpenAbout = { currentScreen = ActiveScreen.ABOUT },
                                        onExportExcel = { showExportReports = true },
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
                onUnmarkPaid = {
                    vm.unmarkPaid(item)
                    com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(context)
                    Toast.makeText(context, "پرداخت قسط بازگردانی شد ↩️", Toast.LENGTH_SHORT).show()
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

        if (showExportReports) {
            val allList by vm.all.collectAsStateWithLifecycle()
            ExportReportSheet(
                allInstallments = allList,
                onDismiss = { showExportReports = false },
                onSaveDocument = { isPdf, items, titleScope ->
                    pendingExportIsPdf = isPdf
                    pendingExportItems = items
                    pendingExportScope = titleScope
                    val ext = if (isPdf) "pdf" else "xlsx"
                    val defaultName = "ghestyar-report-${LocalDate.now().formatJalali().replace("/", "-")}.$ext"
                    createExportDoc.launch(defaultName)
                }
            )
        }
    }
}