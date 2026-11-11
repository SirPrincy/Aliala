package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.AppRepository
import com.example.util.Calculations
import com.example.util.DateUtils
import com.example.util.DayProgressSummary
import com.example.util.FinanceSummary
import com.example.util.HabitsSummary
import com.example.util.HealthSummary
import com.example.util.LoansSummary
import com.example.util.UpcomingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Princy",
    val todayGreeting: String = DateUtils.getTodayGreetingDate(),
    val selectedMonthKey: String = DateUtils.getCurrentMonthKey(),
    val selectedMonthDisplay: String = DateUtils.formatMonthKeyDisplay(DateUtils.getCurrentMonthKey()),
    val canGoToNextMonth: Boolean = false,
    val hideAmounts: Boolean = false,
    val lastExportDate: Long? = null,
    val shouldShowBackupAlert: Boolean = true,
    val dayProgress: DayProgressSummary = DayProgressSummary(0, 0, 0, 0, 0, 0, 0, 1),
    val financeSummary: FinanceSummary = Calculations.computeFinanceSummary(emptyList(), emptyList(), DateUtils.getCurrentMonthKey(), null),
    val businessSummary: com.example.util.BusinessSummary = Calculations.computeBusinessSummary(emptyList()),
    val healthSummary: HealthSummary = Calculations.computeHealthSummary(emptyList()),
    val habitsSummary: HabitsSummary = Calculations.computeHabitsSummary(emptyList(), emptyList()),
    val loansSummary: LoansSummary = Calculations.computeLoansSummary(emptyList(), emptyList()),
    val todayTasks: List<TaskEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val upcomingItems: List<UpcomingItem> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val activeTab: Int = 0,
    val themeMode: String = "SYSTEM",
    val dailyInsightText: String = "",
    val snackbarMessage: String? = null,
    val deletedTransactionForUndo: TransactionEntity? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsDataStore = SettingsDataStore(application)
    val repository = AppRepository(application, db, settingsDataStore)

    private val selectedMonthKeyFlow = MutableStateFlow(DateUtils.getCurrentMonthKey())
    private val activeTabFlow = MutableStateFlow(0)
    private val snackbarMessageFlow = MutableStateFlow<String?>(null)
    private val undoTransactionFlow = MutableStateFlow<TransactionEntity?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            repository.wallets,
            repository.transactions,
            repository.tasks,
            repository.habits,
            repository.habitLogs
        ) { wallets, txs, tasks, habits, logs ->
            FiveCore(wallets, txs, tasks, habits, logs)
        },
        combine(
            repository.loans,
            repository.loanPayments,
            combine(repository.businessOrders, repository.businessSales) { orders, sales -> orders to sales },
            repository.healthEntries,
            repository.goals
        ) { loans, payments, (orders, sales), health, goals ->
            FiveSecondary(loans, payments, orders, sales, health, goals)
        },
        combine(
            combine(selectedMonthKeyFlow, repository.hideAmounts, repository.lastExportDate) { m, h, l -> Triple(m, h, l) },
            combine(repository.userName, repository.themeMode, activeTabFlow) { n, th, tab -> Triple(n, th, tab) },
            repository.totalXp
        ) { (monthKey, hideAm, lastExport), (name, theme, tab), xp ->
            SettingsState(monthKey, hideAm, lastExport, name, theme, tab, xp)
        },
        combine(
            snackbarMessageFlow,
            undoTransactionFlow
        ) { msg, undoTx ->
            msg to undoTx
        }
    ) { core, secondary, settings, snackUndo ->
        val monthKey = settings.monthKey
        val budget = BudgetEntity(monthKey = monthKey, amount = 1_000_000L) // Default budget per month

        val finSummary = Calculations.computeFinanceSummary(core.wallets, core.transactions, monthKey, budget)
        val bizSummary = Calculations.computeBusinessSummary(secondary.orders, secondary.sales)
        val hlthSummary = Calculations.computeHealthSummary(secondary.health)
        val habSummary = Calculations.computeHabitsSummary(core.habits, core.habitLogs)
        val lnsSummary = Calculations.computeLoansSummary(secondary.loans, secondary.payments)
        val dayProg = Calculations.computeDayProgress(core.tasks, habSummary, hlthSummary, settings.totalXp)
        val upcoming = Calculations.computeUpcomingItems(core.tasks, secondary.loans, secondary.orders)

        val daysSinceExport = if (settings.lastExport != null) {
            (System.currentTimeMillis() - settings.lastExport) / (86400_000L)
        } else null
        val showBackupAlert = daysSinceExport == null || daysSinceExport >= 7

        val activeParcel = bizSummary.activeParcel
        val insight = when {
            finSummary.budgetPercentage > 85 -> "💡 Attention : tu as consommé ${finSummary.budgetPercentage}% de ton budget ce mois-ci."
            activeParcel != null -> "💡 Ton colis '${activeParcel.product}' est en transit. Arrivée estimée : ${DateUtils.formatRelativeDays(activeParcel.estimatedArrival)}."
            finSummary.percentChangeVsPreviousMonth != null && finSummary.percentChangeVsPreviousMonth > 10 -> "💡 Dépenses mensuelles en hausse de ${finSummary.percentChangeVsPreviousMonth}% par rapport au mois dernier."
            dayProg.streakDays > 1 -> "💡 Belle régularité ! Série de ${dayProg.streakDays} jours d'habitudes."
            else -> "💡 Suis tes finances, tes tâches et ton business au quotidien."
        }

        HomeUiState(
            userName = settings.userName,
            todayGreeting = DateUtils.getTodayGreetingDate(),
            selectedMonthKey = monthKey,
            selectedMonthDisplay = DateUtils.formatMonthKeyDisplay(monthKey),
            canGoToNextMonth = !DateUtils.isCurrentOrFutureMonth(monthKey),
            hideAmounts = settings.hideAmounts,
            lastExportDate = settings.lastExport,
            shouldShowBackupAlert = showBackupAlert,
            dayProgress = dayProg,
            financeSummary = finSummary,
            businessSummary = bizSummary,
            healthSummary = hlthSummary,
            habitsSummary = habSummary,
            loansSummary = lnsSummary,
            todayTasks = core.tasks.take(4),
            goals = secondary.goals,
            upcomingItems = upcoming,
            recentTransactions = core.transactions.take(5),
            activeTab = settings.activeTab,
            themeMode = settings.themeMode,
            dailyInsightText = insight,
            snackbarMessage = snackUndo.first,
            deletedTransactionForUndo = snackUndo.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSelectTab(tab: Int) {
        activeTabFlow.value = tab
    }

    fun onPreviousMonth() {
        selectedMonthKeyFlow.value = DateUtils.shiftMonthKey(selectedMonthKeyFlow.value, -1)
    }

    fun onNextMonth() {
        if (!DateUtils.isCurrentOrFutureMonth(selectedMonthKeyFlow.value)) {
            selectedMonthKeyFlow.value = DateUtils.shiftMonthKey(selectedMonthKeyFlow.value, 1)
        }
    }

    fun toggleHideAmounts() {
        viewModelScope.launch {
            repository.setHideAmounts(!uiState.value.hideAmounts)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun addTask(title: String, dueDateMillis: Long, colorHex: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(title = title, dueDate = dueDateMillis, categoryColorHex = colorHex, isCompleted = false)
            )
        }
    }

    fun addTransaction(
        title: String,
        amount: Long,
        type: String,
        walletId: Long,
        category: String,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    walletId = walletId,
                    category = category,
                    date = date
                )
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            undoTransactionFlow.value = transaction
            repository.deleteTransaction(transaction.id)
            snackbarMessageFlow.value = "Transaction supprimée"
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            val tx = undoTransactionFlow.value
            if (tx != null) {
                repository.insertTransaction(tx)
                undoTransactionFlow.value = null
                snackbarMessageFlow.value = null
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessageFlow.value = null
    }

    fun setBudget(amount: Long) {
        viewModelScope.launch {
            repository.setBudget(selectedMonthKeyFlow.value, amount)
        }
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitToday(habitId)
        }
    }

    fun createHabit(
        name: String,
        category: String,
        iconName: String,
        targetDays: Int,
        habitType: String = "BOOLEAN",
        targetCount: Int = 1,
        unit: String = "fois",
        colorHex: String = "#06B6D4",
        reminderTimes: String = "",
        note: String = ""
    ) {
        if (name.isBlank()) return
        val clampedDays = targetDays.coerceIn(1, 7)
        viewModelScope.launch {
            val newId = repository.insertHabit(
                HabitEntity(
                    name = name.trim(),
                    iconName = iconName,
                    targetDaysPerWeek = clampedDays,
                    isDemo = false,
                    habitType = habitType,
                    targetCount = targetCount,
                    unit = unit,
                    moment = category,
                    colorHex = colorHex,
                    reminderTimes = reminderTimes,
                    note = note
                )
            )
            repository.addXp(20L)
            val todayStr = DateUtils.getTodayDateString()
            if (name.contains("eau", ignoreCase = true) || unit == "verres" || iconName == "💧" || iconName == "water") {
                val healthEntries = repository.getAllHealthEntriesSnapshot()
                val todayHealth = healthEntries.firstOrNull { it.date == todayStr }
                if (todayHealth != null && todayHealth.waterGlasses >= targetCount) {
                    repository.toggleHabitForDate(newId, todayStr)
                }
            }
        }
    }

    fun incrementWater() {
        viewModelScope.launch {
            repository.incrementWater()
        }
    }

    fun setMood(mood: Int) {
        viewModelScope.launch {
            repository.setMood(mood)
        }
    }

    fun addLoan(
        direction: String,
        counterpartyType: String,
        name: String,
        amount: Long,
        interestPercent: Double?,
        dueDate: Long
    ) {
        viewModelScope.launch {
            repository.insertLoan(
                LoanEntity(
                    direction = direction,
                    counterpartyType = counterpartyType,
                    name = name,
                    amount = amount,
                    interestPercent = interestPercent,
                    dueDate = dueDate
                )
            )
        }
    }

    fun addGoal(
        name: String,
        targetAmount: Long,
        savedAmount: Long,
        category: String,
        deadlineMillis: Long
    ) {
        viewModelScope.launch {
            repository.insertGoal(
                com.example.data.local.entity.GoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    category = category,
                    deadline = deadlineMillis
                )
            )
            repository.addXp(30)
            snackbarMessageFlow.value = "Objectif ajouté (+30 XP)"
        }
    }

    fun completeGermanReview() {
        viewModelScope.launch {
            repository.addXp(25)
            snackbarMessageFlow.value = "Session Panorama A2 validée (+25 XP)"
        }
    }

    fun addBusinessOrder(
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resalePriceAr: Long,
        status: String
    ) {
        viewModelScope.launch {
            repository.insertBusinessOrder(
                BusinessOrderEntity(
                    product = product,
                    supplier = supplier,
                    quantityBought = quantity,
                    priceYuan = priceYuan,
                    resalePriceAr = resalePriceAr,
                    status = status
                )
            )
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            snackbarMessageFlow.value = "Données de démo chargées"
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            repository.clearDemoData()
            snackbarMessageFlow.value = "Données de démo supprimées"
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            repository.wipeAllData()
            snackbarMessageFlow.value = "Toutes les données ont été réinitialisées"
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackup()
            snackbarMessageFlow.value = "Sauvegarde exportée avec succès"
            onResult(json)
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackup(jsonString)
            if (success) {
                snackbarMessageFlow.value = "Sauvegarde importée avec succès"
            } else {
                snackbarMessageFlow.value = "Erreur: format de sauvegarde invalide"
            }
            onResult(success)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }
}

private data class FiveCore(
    val wallets: List<com.example.data.local.entity.WalletEntity>,
    val transactions: List<com.example.data.local.entity.TransactionEntity>,
    val tasks: List<com.example.data.local.entity.TaskEntity>,
    val habits: List<com.example.data.local.entity.HabitEntity>,
    val habitLogs: List<com.example.data.local.entity.HabitLogEntity>
)

private data class FiveSecondary(
    val loans: List<com.example.data.local.entity.LoanEntity>,
    val payments: List<com.example.data.local.entity.LoanPaymentEntity>,
    val orders: List<com.example.data.local.entity.BusinessOrderEntity>,
    val sales: List<com.example.data.local.entity.BusinessSaleEntity>,
    val health: List<com.example.data.local.entity.HealthEntryEntity>,
    val goals: List<com.example.data.local.entity.GoalEntity>
)

private data class SettingsState(
    val monthKey: String,
    val hideAmounts: Boolean,
    val lastExport: Long?,
    val userName: String,
    val themeMode: String,
    val activeTab: Int,
    val totalXp: Long
)
