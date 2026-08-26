// ═══ MainViewModel.kt ═══
package com.iliyateam.ghestyar

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iliyateam.ghestyar.data.*
import com.iliyateam.ghestyar.reminder.ReminderScheduler
import com.iliyateam.ghestyar.util.JalaliDate
import com.iliyateam.ghestyar.util.toJalali
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class VipColorTheme(
    val title: String,
    val hexColor: String
) {
    TEAL_MOSS("سبزآبی کلاسیک", "#006A6E"),
    OBSIDIAN_GOLD("طلایی اشرافی (Obsidian Gold)", "#D4AF37"),
    EMERALD_LUXURY("زمرد سلطنتی (Royal Emerald)", "#059669"),
    ROYAL_AMETHYST("ارغوانی اشرافی (Deep Amethyst)", "#7C3AED"),
    MIDNIGHT_SAPPHIRE("یاقوت کبود (Midnight Sapphire)", "#0284C7")
}

enum class AppFontScale(val title: String, val scale: Float) {
    SMALL("کوچک", 0.90f),
    NORMAL("استاندارد", 1.0f),
    LARGE("بزرگ", 1.15f),
    EXTRA_LARGE("خیلی بزرگ", 1.28f)
}

enum class UrgencyFilter(val title: String) {
    ALL("همه"),
    OVERDUE("دارای تأخیر"),
    DUE_SOON("سررسید این هفته"),
    THIS_MONTH("این ماه")
}

data class FinancialStats(
    val totalActiveDebt: Long = 0L,
    val monthlyCommitment: Long = 0L,
    val totalPaidAllTime: Long = 0L,
    val totalInstallmentsCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0,
    val overallHealthPercentage: Float = 0f,
    val totalMonthlyIncome: Long = 0L,
    val totalMonthlyExpense: Long = 0L,
    val safeLoanCapacity: Long = 0L
)

data class CashflowSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val thisMonthInstallments: Long = 0L,
    val thisMonthPayableCheques: Long = 0L,
    val thisMonthReceivableCheques: Long = 0L,
    val netBalance: Long = 0L,
    val totalMonthlyInflow: Long = 0L,
    val totalMonthlyOutflow: Long = 0L,
    val remainingAfterInstallments: Long = 0L,
    val recurringMonthlyIncome: Long = 0L,
    val recurringMonthlyExpense: Long = 0L
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val installmentDao = db.installmentDao()
    private val transactionDao = db.transactionDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val chequeOrDebtDao = db.chequeOrDebtDao()
    private val userProfileDao = db.userProfileDao()
    private val loanPoolDao = db.loanPoolDao()

    private val prefs = app.getSharedPreferences("ghestyar_settings", Context.MODE_PRIVATE)

    // ─── مدیریت پروفایل‌های چندگانه و ایزوله ───
    val allProfiles: StateFlow<List<UserProfile>> = userProfileDao.observeAll()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeProfileId = MutableStateFlow(prefs.getLong("active_profile_id", 1L))

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val loanPoolsWithMembers: StateFlow<List<LoanPoolWithMembers>> = activeProfileId
        .flatMapLatest { pId -> loanPoolDao.getPoolsWithMembers(pId) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeProfile: StateFlow<UserProfile> = combine(
        allProfiles,
        activeProfileId
    ) { profiles, id ->
        profiles.firstOrNull { it.id == id }
            ?: profiles.firstOrNull { it.isDefault }
            ?: profiles.firstOrNull()
            ?: UserProfile(id = 1L, name = "حساب اصلی", emoji = "👤", colorIndex = 0, isDefault = true)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserProfile(id = 1L, name = "حساب اصلی", emoji = "👤", colorIndex = 0, isDefault = true)
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // اطمینان از وجود پروفایل پیش‌فرض
                val existingProfiles = userProfileDao.getAll()
                if (existingProfiles.isEmpty()) {
                    userProfileDao.insert(
                        UserProfile(id = 1L, name = "حساب اصلی", emoji = "👤", colorIndex = 0, isDefault = true)
                    )
                }

                // زمان‌بندی مجدد و هوشمند تمام اعلان‌ها با درج نام پروفایل
                val profilesMap = userProfileDao.getAll().associateBy { it.id }
                installmentDao.getAll().filter { !it.isPaid && it.remind }.forEach { item ->
                    val pName = profilesMap[item.profileId]?.name.orEmpty()
                    ReminderScheduler.schedule(getApplication(), item, pName)
                }
            } catch (_: Exception) { }
        }
    }

    fun selectProfile(id: Long) {
        activeProfileId.value = id
        prefs.edit().putLong("active_profile_id", id).apply()
    }

    fun addProfile(name: String, emoji: String, colorIndex: Int, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val newProfile = UserProfile(
                name = name.trim().ifBlank { "پروفایل جدید" },
                emoji = emoji.ifBlank { "💼" },
                colorIndex = colorIndex,
                isDefault = false
            )
            val newId = userProfileDao.insert(newProfile)
            selectProfile(newId)
            onDone(newId)
        }
    }

    fun deleteProfile(profile: UserProfile, onDone: () -> Unit = {}) {
        if (profile.isDefault || profile.id == 1L) return
        viewModelScope.launch {
            // حذف کامل اطلاعات ایزوله این پروفایل
            val pId = profile.id
            installmentDao.getAll().filter { it.profileId == pId }.forEach {
                ReminderScheduler.cancel(getApplication(), it)
                installmentDao.delete(it)
            }
            transactionDao.getAll().filter { it.profileId == pId }.forEach { transactionDao.delete(it) }
            savingsGoalDao.getAll().filter { it.profileId == pId }.forEach { savingsGoalDao.delete(it) }
            chequeOrDebtDao.getAll().filter { it.profileId == pId }.forEach { chequeOrDebtDao.delete(it) }

            userProfileDao.delete(profile)

            if (activeProfileId.value == pId) {
                selectProfile(1L)
            }
            onDone()
        }
    }

    // ─── جریان‌های اقساط (کاملاً فیلتر و ایزوله بر اساس پروفایل فعال) ───
    val active: StateFlow<List<Installment>> = combine(
        installmentDao.observeActive().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<Installment>> = combine(
        installmentDao.observeHistory().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val all: StateFlow<List<Installment>> = combine(
        active,
        history
    ) { act, hist ->
        act + hist
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedUrgencyFilter = MutableStateFlow(UrgencyFilter.ALL)

    val filteredActive: StateFlow<List<Installment>> = combine(
        active,
        searchQuery,
        selectedCategoryFilter,
        selectedUrgencyFilter
    ) { list, query, category, urgency ->
        val today = LocalDate.now().toEpochDay()
        val nextWeek = today + 7
        val nextMonth = today + 30

        list.filter { item ->
            val matchQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.destination.contains(query, ignoreCase = true) ||
                    item.note.contains(query, ignoreCase = true)

            val matchCategory = category == null || item.category == category

            val matchUrgency = when (urgency) {
                UrgencyFilter.ALL -> true
                UrgencyFilter.OVERDUE -> item.dueEpochDay < today
                UrgencyFilter.DUE_SOON -> item.dueEpochDay in today..nextWeek
                UrgencyFilter.THIS_MONTH -> item.dueEpochDay <= nextMonth
            }

            matchQuery && matchCategory && matchUrgency
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── جریان‌های دخل و خرج (ایزوله بر اساس پروفایل) ───
    val transactions: StateFlow<List<Transaction>> = combine(
        transactionDao.observeAll().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── جریان‌های قلک و اهداف (ایزوله بر اساس پروفایل) ───
    val savingsGoals: StateFlow<List<SavingsGoal>> = combine(
        savingsGoalDao.observeAll().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── جریان‌های چک و قرض (ایزوله بر اساس پروفایل) ───
    val pendingChequesAndDebts: StateFlow<List<ChequeOrDebt>> = combine(
        chequeOrDebtDao.observePending().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clearedChequesAndDebts: StateFlow<List<ChequeOrDebt>> = combine(
        chequeOrDebtDao.observeCleared().distinctUntilChanged(),
        activeProfileId
    ) { list, pId ->
        list.filter { it.profileId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── خلاصه دخل و خرج ماهانه (مربوط به پروفایل فعال با احتساب اقساط و چک‌ها) ───
    val cashflowSummary: StateFlow<CashflowSummary> = combine(
        transactions,
        active,
        pendingChequesAndDebts
    ) { txList, activeInstallments, chequesList ->
        val today = JalaliDate.today()
        val currentMonth = today.jm
        val currentYear = today.jy

        val thisMonthTx = txList.filter {
            val jDate = LocalDate.ofEpochDay(it.epochDay).toJalali()
            jDate.jy == currentYear && jDate.jm == currentMonth
        }

        val income = thisMonthTx.filter { it.isIncome }.sumOf { it.amount }
        val expense = thisMonthTx.filter { !it.isIncome }.sumOf { it.amount }
        val net = income - expense

        // تعهدات اقساط ماه جاری: مجموع مبالغ ماهانه تمام اقساط فعال
        val monthInstallments = activeInstallments.sumOf { it.amount }

        // چک‌ها و بدهی‌ها/طلب‌های سررسید ماه جاری
        val thisMonthCheques = chequesList.filter {
            val jDate = LocalDate.ofEpochDay(it.dueEpochDay).toJalali()
            jDate.jy == currentYear && jDate.jm == currentMonth
        }
        val payableCheques = thisMonthCheques.filter { !it.isReceivable }.sumOf { it.amount }
        val receivableCheques = thisMonthCheques.filter { it.isReceivable }.sumOf { it.amount }

        val totalInflow = income + receivableCheques
        val totalOutflow = expense + monthInstallments + payableCheques
        val remainingNet = totalInflow - totalOutflow

        // درآمدهای تکرارشونده و ثابت ماهانه در پروفایل فعال (محاسبه ماه جاری یا یکتا برای جلوگیری از دوباره‌شماری ماه‌های قبل)
        val recurringIncome = thisMonthTx.filter { it.isIncome && it.isRecurring }.sumOf { it.amount }
            .takeIf { it > 0 } ?: txList.filter { it.isIncome && it.isRecurring }.distinctBy { it.title.trim().lowercase() }.sumOf { it.amount }
        val recurringExpense = thisMonthTx.filter { !it.isIncome && it.isRecurring }.sumOf { it.amount }
            .takeIf { it > 0 } ?: txList.filter { !it.isIncome && it.isRecurring }.distinctBy { it.title.trim().lowercase() }.sumOf { it.amount }

        CashflowSummary(
            totalIncome = income,
            totalExpense = expense,
            thisMonthInstallments = monthInstallments,
            thisMonthPayableCheques = payableCheques,
            thisMonthReceivableCheques = receivableCheques,
            netBalance = net,
            totalMonthlyInflow = totalInflow,
            totalMonthlyOutflow = totalOutflow,
            remainingAfterInstallments = remainingNet,
            recurringMonthlyIncome = recurringIncome,
            recurringMonthlyExpense = recurringExpense
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CashflowSummary())

    // ─── آمارهای جامع مالی ───
    val stats: StateFlow<FinancialStats> = combine(
        active,
        history,
        cashflowSummary
    ) { activeList, historyList, cashflow ->
        val today = LocalDate.now().toEpochDay()

        val totalActiveDebt = activeList.sumOf { it.remainingAmount }
        val monthlyCommitment = cashflow.thisMonthInstallments
        val totalPaidActive = activeList.sumOf { it.paidAmount }
        val totalPaidHistory = historyList.sumOf { it.totalAmount }
        val totalPaidAllTime = totalPaidActive + totalPaidHistory

        val totalInstallmentsCount = activeList.size + historyList.size
        val activeCount = activeList.size
        val completedCount = historyList.size
        val overdueCount = activeList.count { it.dueEpochDay < today }

        val overdueDebt = activeList.filter { it.dueEpochDay < today }.sumOf { it.remainingAmount }
        val onTimeDebt = (totalActiveDebt - overdueDebt).coerceAtLeast(0L)
        val overallHealthPercentage = if (totalActiveDebt > 0L) {
            val debtRatio = (onTimeDebt.toFloat() / totalActiveDebt).coerceIn(0f, 1f)
            val countRatio = if (activeCount > 0) (activeCount - overdueCount).toFloat() / activeCount else 1f
            ((debtRatio * 0.7f + countRatio * 0.3f) * 100f).coerceIn(0f, 100f)
        } else 100f

        val safeCapacity = if (cashflow.remainingAfterInstallments > 0) {
            (cashflow.remainingAfterInstallments * 0.40f).toLong()
        } else 0L

        FinancialStats(
            totalActiveDebt = totalActiveDebt,
            monthlyCommitment = monthlyCommitment,
            totalPaidAllTime = totalPaidAllTime,
            totalInstallmentsCount = totalInstallmentsCount,
            activeCount = activeCount,
            completedCount = completedCount,
            overdueCount = overdueCount,
            overallHealthPercentage = overallHealthPercentage,
            totalMonthlyIncome = cashflow.totalIncome,
            totalMonthlyExpense = cashflow.totalExpense,
            safeLoanCapacity = safeCapacity
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialStats())

    // ─── تنظیمات کاربری ───
    val themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    )

    val vipColorTheme = MutableStateFlow(
        try {
            VipColorTheme.valueOf(prefs.getString("vip_color_theme", VipColorTheme.TEAL_MOSS.name) ?: VipColorTheme.TEAL_MOSS.name)
        } catch (_: Exception) {
            VipColorTheme.TEAL_MOSS
        }
    )

    val fontScale = MutableStateFlow(
        try {
            AppFontScale.valueOf(prefs.getString("font_scale", AppFontScale.NORMAL.name) ?: AppFontScale.NORMAL.name)
        } catch (_: Exception) {
            AppFontScale.NORMAL
        }
    )

    val isPrivacyMode = MutableStateFlow(prefs.getBoolean("privacy_mode", false))
    val isPinLockEnabled = MutableStateFlow(prefs.getBoolean("pin_enabled", false))
    val pinCode = MutableStateFlow(prefs.getString("pin_code", "") ?: "")
    val notificationHour = MutableStateFlow(prefs.getInt("notif_hour", 9))

    fun setTheme(mode: AppThemeMode) {
        themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setVipTheme(theme: VipColorTheme) {
        vipColorTheme.value = theme
        prefs.edit().putString("vip_color_theme", theme.name).apply()
    }

    fun setFontScale(scale: AppFontScale) {
        fontScale.value = scale
        prefs.edit().putString("font_scale", scale.name).apply()
    }

    fun setPrivacyMode(enabled: Boolean) {
        isPrivacyMode.value = enabled
        prefs.edit().putBoolean("privacy_mode", enabled).apply()
    }

    private fun hashPin(pin: String): String {
        if (pin.isBlank()) return ""
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (_: Exception) { pin }
    }

    fun verifyPin(inputPin: String): Boolean {
        val stored = pinCode.value
        if (stored.isBlank()) return true
        return stored == inputPin || stored == hashPin(inputPin)
    }

    fun setPinLock(enabled: Boolean, code: String = "") {
        val hashed = if (code.isNotBlank()) hashPin(code) else ""
        isPinLockEnabled.value = enabled
        pinCode.value = hashed
        prefs.edit()
            .putBoolean("pin_enabled", enabled)
            .putString("pin_code", hashed)
            .apply()
    }

    fun setNotificationHour(hour: Int) {
        notificationHour.value = hour
        prefs.edit().putInt("notif_hour", hour).apply()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profilesMap = userProfileDao.getAll().associateBy { it.id }
                installmentDao.getAll().filter { !it.isPaid && it.remind }.forEach { item ->
                    val pName = profilesMap[item.profileId]?.name.orEmpty()
                    ReminderScheduler.schedule(getApplication(), item, pName, hour)
                }
            } catch (_: Exception) {}
        }
    }

    // ─── عملیات اقساط ───
    suspend fun allOnce() = installmentDao.getAll().filter { it.profileId == activeProfileId.value }

    fun setSearch(q: String) { searchQuery.value = q }
    fun setCategoryFilter(cat: String?) { selectedCategoryFilter.value = cat }
    fun setUrgencyFilter(filter: UrgencyFilter) { selectedUrgencyFilter.value = filter }

    fun add(
        title: String, amount: Long, due: JalaliDate, sessions: Int,
        colorIndex: Int, category: String, remind: Boolean, note: String,
        destination: String = ""
    ) {
        viewModelScope.launch {
            val pId = activeProfileId.value
            val pName = activeProfile.value.name
            val item = Installment(
                title = title.trim(),
                amount = amount,
                startEpochDay = LocalDate.now().toEpochDay(),
                dueEpochDay = due.toLocalDate().toEpochDay(),
                totalSessions = sessions,
                colorIndex = colorIndex,
                category = category,
                remind = remind,
                note = note.trim(),
                destination = destination.trim(),
                profileId = pId
            )
            val id = installmentDao.insert(item)
            if (remind) ReminderScheduler.schedule(getApplication(), item.copy(id = id), pName)
        }
    }

    fun update(
        item: Installment, title: String, amount: Long, due: JalaliDate,
        sessions: Int, colorIndex: Int, category: String, remind: Boolean, note: String,
        destination: String = ""
    ) {
        viewModelScope.launch {
            val pName = activeProfile.value.name
            val updated = item.copy(
                title = title.trim(),
                amount = amount,
                dueEpochDay = due.toLocalDate().toEpochDay(),
                totalSessions = sessions,
                paidSessions = item.paidSessions.coerceAtMost(sessions),
                colorIndex = colorIndex,
                category = category,
                remind = remind,
                note = note.trim(),
                destination = destination.trim()
            )
            installmentDao.update(updated)
            if (remind) ReminderScheduler.schedule(getApplication(), updated, pName)
            else ReminderScheduler.cancel(getApplication(), updated)
        }
    }

    fun markPaid(item: Installment) {
        viewModelScope.launch {
            if (item.paidSessions >= item.totalSessions) return@launch
            val today = LocalDate.now()
            val pName = activeProfile.value.name
            val newPaidSessions = item.paidSessions + 1
            val isLast = newPaidSessions >= item.totalSessions

            val currentDue = LocalDate.ofEpochDay(item.dueEpochDay).toJalali()
            val nextDueEpoch = if (!isLast) {
                currentDue.plusMonths(1).toLocalDate().toEpochDay()
            } else {
                item.dueEpochDay
            }

            val updated = item.copy(
                paidSessions = newPaidSessions,
                dueEpochDay = nextDueEpoch,
                isPaid = isLast,
                paidAtEpochDay = today.toEpochDay()
            )
            installmentDao.update(updated)
            if (isLast) {
                ReminderScheduler.cancel(getApplication(), item)
            } else if (updated.remind) {
                ReminderScheduler.schedule(getApplication(), updated, pName)
            }
            com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(getApplication())
        }
    }

    fun unmarkPaid(item: Installment) {
        viewModelScope.launch {
            if (item.paidSessions <= 0) return@launch
            val pName = activeProfile.value.name
            val newPaidSessions = item.paidSessions - 1
            val wasPaid = item.isPaid

            val currentDue = LocalDate.ofEpochDay(item.dueEpochDay).toJalali()
            val prevDueEpoch = if (wasPaid) {
                // در صورت بازگردانی از حالت کاملاً تسویه‌شده، تاریخ همان سررسید آخرین قسط است
                item.dueEpochDay
            } else {
                // در حین اقساط جاری، تاریخ سررسید یک ماه به عقب بازمی‌گردد
                currentDue.minusMonths(1).toLocalDate().toEpochDay()
            }

            val updated = item.copy(
                isPaid = false,
                paidSessions = newPaidSessions,
                dueEpochDay = prevDueEpoch,
                paidAtEpochDay = null
            )
            installmentDao.update(updated)
            if (updated.remind) ReminderScheduler.schedule(getApplication(), updated, pName)
            com.iliyateam.ghestyar.widget.GhestYarWidgetProvider.updateAll(getApplication())
        }
    }

    fun delete(item: Installment) {
        viewModelScope.launch {
            ReminderScheduler.cancel(getApplication(), item)
            installmentDao.delete(item)
        }
    }

    fun addTransaction(
        title: String,
        amount: Long,
        isIncome: Boolean,
        category: String,
        epochDay: Long,
        note: String = "",
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            transactionDao.insert(
                Transaction(
                    title = title.trim(),
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    epochDay = epochDay,
                    note = note.trim(),
                    profileId = activeProfileId.value,
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun addTransactionsBatch(list: List<Transaction>, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val pId = activeProfileId.value
            val mapped = list.map { it.copy(profileId = pId) }
            val insertedIds = transactionDao.insertAll(mapped)
            onComplete(insertedIds.size)
        }
    }

    fun updateTransaction(
        tx: Transaction,
        title: String,
        amount: Long,
        isIncome: Boolean,
        category: String,
        epochDay: Long,
        note: String = "",
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            transactionDao.update(
                tx.copy(
                    title = title.trim(),
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    epochDay = epochDay,
                    note = note.trim(),
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { transactionDao.delete(tx) }
    }

    // ─── عملیات قلک و اهداف ───
    fun addSavingsGoal(title: String, targetAmount: Long, currentAmount: Long, due: JalaliDate, emoji: String, note: String = "") {
        viewModelScope.launch {
            savingsGoalDao.insert(
                SavingsGoal(
                    title = title.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetEpochDay = due.toLocalDate().toEpochDay(),
                    emoji = emoji,
                    note = note.trim(),
                    profileId = activeProfileId.value
                )
            )
        }
    }

    fun updateSavingsGoal(
        goal: SavingsGoal,
        title: String,
        targetAmount: Long,
        currentAmount: Long,
        due: JalaliDate,
        emoji: String,
        note: String = ""
    ) {
        viewModelScope.launch {
            savingsGoalDao.update(
                goal.copy(
                    title = title.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetEpochDay = due.toLocalDate().toEpochDay(),
                    emoji = emoji,
                    note = note.trim()
                )
            )
        }
    }

    fun depositToGoal(goal: SavingsGoal, amount: Long) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtLeast(0L))
            savingsGoalDao.update(updated)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch { savingsGoalDao.delete(goal) }
    }

    // ─── عملیات چک و قرض ───
    fun addChequeOrDebt(title: String, personName: String, amount: Long, isCheque: Boolean, isReceivable: Boolean, due: JalaliDate, chequeNumber: String = "", bankName: String = "", note: String = "") {
        viewModelScope.launch {
            chequeOrDebtDao.insert(
                ChequeOrDebt(
                    title = title.trim(),
                    personName = personName.trim(),
                    amount = amount,
                    isCheque = isCheque,
                    isReceivable = isReceivable,
                    dueEpochDay = due.toLocalDate().toEpochDay(),
                    chequeNumber = chequeNumber.trim(),
                    bankName = bankName.trim(),
                    note = note.trim(),
                    profileId = activeProfileId.value
                )
            )
        }
    }

    fun updateChequeOrDebt(
        item: ChequeOrDebt,
        title: String,
        personName: String,
        amount: Long,
        isCheque: Boolean,
        isReceivable: Boolean,
        due: JalaliDate,
        chequeNumber: String = "",
        bankName: String = "",
        note: String = ""
    ) {
        viewModelScope.launch {
            chequeOrDebtDao.update(
                item.copy(
                    title = title.trim(),
                    personName = personName.trim(),
                    amount = amount,
                    isCheque = isCheque,
                    isReceivable = isReceivable,
                    dueEpochDay = due.toLocalDate().toEpochDay(),
                    chequeNumber = chequeNumber.trim(),
                    bankName = bankName.trim(),
                    note = note.trim()
                )
            )
        }
    }

    fun toggleChequeCleared(item: ChequeOrDebt) {
        viewModelScope.launch {
            chequeOrDebtDao.update(item.copy(isCleared = !item.isCleared))
        }
    }

    fun deleteChequeOrDebt(item: ChequeOrDebt) {
        viewModelScope.launch { chequeOrDebtDao.delete(item) }
    }

    // ─── مدیریت صندوق‌های وام و قرعه‌کشی خانوادگی ───
    fun createLoanPool(
        title: String,
        monthlyAmount: Long,
        memberNames: List<String>,
        startEpochDay: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            val total = memberNames.size.coerceAtLeast(2)
            val pool = LoanPool(
                title = title.trim(),
                monthlyAmount = monthlyAmount,
                totalMembers = total,
                startEpochDay = startEpochDay,
                winnerPayout = monthlyAmount * total,
                currentRound = 1,
                note = note.trim(),
                profileId = activeProfileId.value
            )
            val poolId = loanPoolDao.insertPool(pool)
            val members = memberNames.mapIndexed { idx, name ->
                LoanPoolMember(
                    poolId = poolId,
                    name = name.trim(),
                    lotteryPosition = idx + 1,
                    hasWon = false,
                    wonMonth = 0,
                    paidThisMonth = false
                )
            }
            loanPoolDao.insertMembers(members)
        }
    }

    fun toggleMemberPaidThisMonth(member: LoanPoolMember) {
        viewModelScope.launch {
            loanPoolDao.updateMember(member.copy(paidThisMonth = !member.paidThisMonth))
        }
    }

    fun setLotteryOrder(poolId: Long, orderedMembers: List<LoanPoolMember>) {
        viewModelScope.launch {
            val updated = orderedMembers.mapIndexed { idx, m ->
                m.copy(lotteryPosition = idx + 1)
            }
            loanPoolDao.updateMembers(updated)
        }
    }

    fun markPoolWinner(pool: LoanPool, member: LoanPoolMember, month: Int) {
        viewModelScope.launch {
            loanPoolDao.updateMember(member.copy(hasWon = true, wonMonth = month))
            if (month >= pool.currentRound) {
                loanPoolDao.updatePool(pool.copy(currentRound = (month + 1).coerceAtMost(pool.totalMembers)))
            }
        }
    }

    fun advancePoolRoundAndResetPayments(pool: LoanPool, currentMembers: List<LoanPoolMember>) {
        viewModelScope.launch {
            val nextRound = (pool.currentRound + 1).coerceAtMost(pool.totalMembers)
            loanPoolDao.updatePool(pool.copy(currentRound = nextRound))
            val resetMembers = currentMembers.map { it.copy(paidThisMonth = false) }
            loanPoolDao.updateMembers(resetMembers)
        }
    }

    fun updateLoanPool(pool: LoanPool, title: String, monthlyAmount: Long, note: String = "") {
        viewModelScope.launch {
            loanPoolDao.updatePool(
                pool.copy(
                    title = title.trim(),
                    monthlyAmount = monthlyAmount,
                    winnerPayout = monthlyAmount * pool.totalMembers,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteLoanPool(pool: LoanPool) {
        viewModelScope.launch {
            loanPoolDao.deletePool(pool)
        }
    }

    suspend fun getFullBackupData(): com.iliyateam.ghestyar.util.FullBackupData {
        return com.iliyateam.ghestyar.util.FullBackupData(
            version = 4,
            timestamp = System.currentTimeMillis(),
            installments = installmentDao.getAll(),
            transactions = transactionDao.getAll(),
            savingsGoals = savingsGoalDao.getAll(),
            chequesAndDebts = chequeOrDebtDao.getAll(),
            userProfiles = userProfileDao.getAll(),
            loanPools = loanPoolDao.getAllPools(),
            loanPoolMembers = loanPoolDao.getAllMembers()
        )
    }

    fun restoreFullBackup(data: com.iliyateam.ghestyar.util.FullBackupData, onComplete: (Int, Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            var instCount = 0
            var txCount = 0
            var goalCount = 0
            var chequeCount = 0

            if (data.userProfiles.isNotEmpty()) {
                userProfileDao.insertAll(data.userProfiles)
            }

            if (data.installments.isNotEmpty()) {
                installmentDao.insertAll(data.installments)
                instCount = data.installments.size
                val pName = activeProfile.value.name
                data.installments.filter { !it.isPaid && it.remind }.forEach {
                    ReminderScheduler.schedule(getApplication(), it, pName)
                }
            }

            if (data.transactions.isNotEmpty()) {
                transactionDao.insertAll(data.transactions)
                txCount = data.transactions.size
            }

            if (data.savingsGoals.isNotEmpty()) {
                savingsGoalDao.insertAll(data.savingsGoals)
                goalCount = data.savingsGoals.size
            }

            if (data.chequesAndDebts.isNotEmpty()) {
                chequeOrDebtDao.insertAll(data.chequesAndDebts)
                chequeCount = data.chequesAndDebts.size
            }

            if (data.loanPools.isNotEmpty()) {
                loanPoolDao.insertAllPools(data.loanPools)
            }
            if (data.loanPoolMembers.isNotEmpty()) {
                loanPoolDao.insertAllMembers(data.loanPoolMembers)
            }

            onComplete(instCount, txCount, goalCount, chequeCount)
        }
    }

    fun restoreBackup(items: List<Installment>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            if (items.isNotEmpty()) {
                val pId = activeProfileId.value
                val pName = activeProfile.value.name
                val mapped = items.map { it.copy(profileId = pId) }
                installmentDao.insertAll(mapped)
                mapped.filter { !it.isPaid && it.remind }.forEach {
                    ReminderScheduler.schedule(getApplication(), it, pName)
                }
            }
            onComplete(items.size)
        }
    }
}