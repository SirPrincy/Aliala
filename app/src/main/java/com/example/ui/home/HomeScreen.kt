package com.example.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionEntity
import com.example.ui.habits.CreateHabitSheet
import com.example.ui.home.components.AddLoanSheet
import com.example.ui.home.components.AddOrderSheet
import com.example.ui.home.components.AddTaskSheet
import com.example.ui.home.components.AddTransactionSheet
import com.example.ui.home.components.BackupAlertSection
import com.example.ui.home.components.BusinessSection
import com.example.ui.home.components.DailyInsightSection
import com.example.ui.home.components.FinanceSection
import com.example.ui.home.components.GoalsSection
import com.example.ui.home.components.HabitsSection
import com.example.ui.home.components.HealthSection
import com.example.ui.home.components.HeroSection
import com.example.ui.home.components.HomeHeader
import com.example.ui.home.components.LanguageSection
import com.example.ui.home.components.LoansSection
import com.example.ui.home.components.ModulesGridSection
import com.example.ui.home.components.MonthlyBreakdownSection
import com.example.ui.home.components.GermanSheet
import com.example.ui.home.components.GoalsSheet
import com.example.ui.home.components.HealthSheet
import com.example.ui.home.components.MoreModulesSheet
import com.example.ui.home.components.QuickActionsSection
import com.example.ui.home.components.RecentTransactionsSection
import com.example.ui.home.components.SetBudgetDialog
import com.example.ui.home.components.SettingsDemoSheet
import com.example.ui.home.components.TasksSection
import com.example.ui.home.components.UpcomingDeadlinesSection
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToHabits: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToBusiness: () -> Unit = {},
    onNavigateToCatalogue: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Dialog & Sheet States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var transactionSheetInitialType by remember { mutableStateOf("EXPENSE") }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var showAddHabitSheet by remember { mutableStateOf(false) }
    var showAddLoanSheet by remember { mutableStateOf(false) }
    var showAddOrderSheet by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showSettingsDemoSheet by remember { mutableStateOf(false) }
    var showMoreModulesSheet by remember { mutableStateOf(false) }
    var showGoalsSheet by remember { mutableStateOf(false) }
    var showGermanSheet by remember { mutableStateOf(false) }
    var showHealthSheet by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf("") }

    // Snackbar listener for deleted transaction with Undo action
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            if (msg == "Transaction supprimée") {
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Annuler",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDeleteTransaction()
                } else {
                    viewModel.clearSnackbar()
                }
            } else {
                snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
                viewModel.clearSnackbar()
            }
        }
    }

    LaunchedEffect(uiState.activeTab) {
        if (uiState.activeTab == 4) {
            showMoreModulesSheet = true
        } else if (uiState.activeTab == 3) {
            onNavigateToBusiness()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(
                selectedTab = uiState.activeTab,
                onTabSelected = { tab ->
                    if (tab == 3) {
                        onNavigateToBusiness()
                    } else {
                        viewModel.onSelectTab(tab)
                        if (tab == 4) {
                            showMoreModulesSheet = true
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionSheetInitialType = "EXPENSE"
                    editingTransaction = null
                    showAddTransactionSheet = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(56.dp)
                    .testTag("home_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // 1. Header
            HomeHeader(
                userName = uiState.userName,
                dateGreeting = uiState.todayGreeting,
                onOpenSettings = { showSettingsDemoSheet = true },
                onOpenSearch = { Toast.makeText(context, "Recherche globale", Toast.LENGTH_SHORT).show() }
            )

            // 2. Hero "Ta journée"
            HeroSection(
                progress = uiState.dayProgress,
                onWaterClick = { viewModel.incrementWater() },
                onHabitsClick = onNavigateToHabits,
                onTasksClick = { viewModel.onSelectTab(1) }
            )

            // 3. Quick Actions Row
            QuickActionsSection(
                onAddExpense = {
                    transactionSheetInitialType = "EXPENSE"
                    editingTransaction = null
                    showAddTransactionSheet = true
                },
                onAddIncome = {
                    transactionSheetInitialType = "INCOME"
                    editingTransaction = null
                    showAddTransactionSheet = true
                },
                onAddTask = { showAddTaskSheet = true },
                onAddOrder = { showAddOrderSheet = true },
                onAddLoan = { showAddLoanSheet = true },
                onAddHabit = { showAddHabitSheet = true }
            )

            val primaryGoal = uiState.goals.firstOrNull()
            val primaryGoalTitle = primaryGoal?.name ?: "Aucun objectif"
            val primaryGoalPercent = if (primaryGoal != null && primaryGoal.targetAmount > 0) {
                ((primaryGoal.savedAmount.toDouble() / primaryGoal.targetAmount.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0
            val activeLoansCount = if (uiState.loansSummary.totalBorrowedRemaining > 0 || uiState.loansSummary.totalLentRemaining > 0) 1 else 0

            // 4. Modules Grid (8 actifs)
            ModulesGridSection(
                tasksCount = uiState.todayTasks.count { !it.isCompleted },
                habitStreak = uiState.dayProgress.streakDays,
                financeTotal = uiState.financeSummary.totalBalance,
                loansCount = activeLoansCount,
                ordersCount = uiState.businessSummary.pipelineTransit + uiState.businessSummary.pipelineOrdered,
                sleepHours = uiState.healthSummary.sleepHours,
                goalTitle = primaryGoalTitle,
                goalPercent = primaryGoalPercent,
                onModuleClick = { module ->
                    when (module) {
                        "habitudes" -> onNavigateToHabits()
                        "finances" -> viewModel.onSelectTab(2)
                        "taches" -> viewModel.onSelectTab(1)
                        "business" -> onNavigateToBusiness()
                        "allemand" -> showGermanSheet = true
                        "objectifs" -> showGoalsSheet = true
                        "sante" -> onNavigateToHealth()
                        "emprunts" -> showAddLoanSheet = true
                        else -> Toast.makeText(context, "Module: $module", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // 5. Tâches du jour
            TasksSection(
                tasks = uiState.todayTasks,
                onToggleTask = { task -> viewModel.toggleTask(task) },
                onAddTask = { showAddTaskSheet = true },
                onViewAll = { viewModel.onSelectTab(1) }
            )

            // 6. Habitudes du jour
            HabitsSection(
                habitsSummary = uiState.habitsSummary,
                onToggleHabit = { habitId -> viewModel.toggleHabitToday(habitId) },
                onOpenHabits = onNavigateToHabits,
                onAddHabit = { showAddHabitSheet = true }
            )

            // 7. Solde du mois (Finances)
            FinanceSection(
                summary = uiState.financeSummary,
                monthDisplay = uiState.selectedMonthDisplay,
                canGoNext = uiState.canGoToNextMonth,
                hideAmounts = uiState.hideAmounts,
                onPreviousMonth = { viewModel.onPreviousMonth() },
                onNextMonth = { viewModel.onNextMonth() },
                onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                onSetBudget = { showSetBudgetDialog = true }
            )

            // 8. Business Chine
            BusinessSection(
                summary = uiState.businessSummary,
                onViewAll = { onNavigateToBusiness() }
            )

            // 9. Emprunts & Dettes
            LoansSection(
                summary = uiState.loansSummary,
                onViewAll = { showAddLoanSheet = true }
            )

            // 10. Santé & Bien-être
            HealthSection(
                summary = uiState.healthSummary,
                onWaterClick = { viewModel.incrementWater() },
                onMoodSelect = { mood -> viewModel.setMood(mood) },
                onOpenHealth = onNavigateToHealth
            )

            // 11. Allemand
            LanguageSection(
                onReviewClick = { showGermanSheet = true }
            )

            // 12. Grands Objectifs
            GoalsSection(
                goals = uiState.goals,
                onViewAll = { showGoalsSheet = true }
            )

            // 13. Insight du jour
            DailyInsightSection(
                insightText = uiState.dailyInsightText
            )

            // 14. Répartition mensuelle
            MonthlyBreakdownSection(
                summary = uiState.financeSummary
            )

            // 15. À venir (Échéances)
            UpcomingDeadlinesSection(
                items = uiState.upcomingItems
            )

            // 16. Dernières transactions
            RecentTransactionsSection(
                transactions = uiState.recentTransactions,
                onEditTransaction = { tx ->
                    editingTransaction = tx
                    showAddTransactionSheet = true
                },
                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                onViewAll = { viewModel.onSelectTab(2) }
            )

            // 17. Sauvegarde locale
            BackupAlertSection(
                lastExportDate = uiState.lastExportDate,
                onExportJson = {
                    viewModel.exportBackup { json ->
                        Toast.makeText(context, "Sauvegarde exportée dans les fichiers locaux", Toast.LENGTH_LONG).show()
                    }
                },
                onImportJson = {
                    showImportConfirmDialog = true
                }
            )

            // Clearance for bottom floating elements
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Modal Bottom Sheets
    if (showAddTransactionSheet) {
        AddTransactionSheet(
            initialType = transactionSheetInitialType,
            existingTransaction = editingTransaction,
            wallets = uiState.financeSummary.walletBalances.map { it.wallet },
            onDismiss = {
                showAddTransactionSheet = false
                editingTransaction = null
            },
            onSave = { title, amount, type, walletId, category ->
                val current = editingTransaction
                if (current != null) {
                    viewModel.updateTransaction(
                        current.copy(title = title, amount = amount, type = type, walletId = walletId, category = category)
                    )
                } else {
                    viewModel.addTransaction(title, amount, type, walletId, category)
                }
                editingTransaction = null
            }
        )
    }

    if (showAddTaskSheet) {
        AddTaskSheet(
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, dueTime, colorHex ->
                viewModel.addTask(title, dueTime, colorHex)
            }
        )
    }

    if (showAddHabitSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddHabitSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            CreateHabitSheet(
                onDismiss = { showAddHabitSheet = false },
                onConfirm = { name, category, iconName, targetDays, habitType, targetCount, unit, colorHex, reminderTimes, note ->
                    viewModel.createHabit(
                        name, category, iconName, targetDays, habitType, targetCount, unit, colorHex, reminderTimes, note
                    )
                    showAddHabitSheet = false
                    Toast.makeText(context, "Habitude '$name' créée !", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showAddLoanSheet) {
        AddLoanSheet(
            onDismiss = { showAddLoanSheet = false },
            onSave = { direction, counterparty, name, amount, interest, dueDate ->
                viewModel.addLoan(direction, counterparty, name, amount, interest, dueDate)
            }
        )
    }

    if (showAddOrderSheet) {
        AddOrderSheet(
            onDismiss = { showAddOrderSheet = false },
            onSave = { product, supplier, quantity, priceYuan, resalePrice, status ->
                viewModel.addBusinessOrder(product, supplier, quantity, priceYuan, resalePrice, status)
            }
        )
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            currentAmount = uiState.financeSummary.budgetAmount,
            onDismiss = { showSetBudgetDialog = false },
            onConfirm = { newAmount -> viewModel.setBudget(newAmount) }
        )
    }

    if (showMoreModulesSheet) {
        MoreModulesSheet(
            onDismiss = {
                showMoreModulesSheet = false
                if (uiState.activeTab == 4) {
                    viewModel.onSelectTab(0)
                }
            },
            onOpenGoals = { showGoalsSheet = true },
            onOpenGerman = { showGermanSheet = true },
            onOpenHealth = {
                showMoreModulesSheet = false
                onNavigateToHealth()
            },
            onOpenLoans = { showAddLoanSheet = true },
            onOpenHabits = {
                showMoreModulesSheet = false
                onNavigateToHabits()
            },
            onOpenBusiness = {
                showMoreModulesSheet = false
                onNavigateToBusiness()
            },
            onOpenCatalogue = {
                showMoreModulesSheet = false
                onNavigateToCatalogue()
            },
            catalogueItemCount = uiState.businessSummary.pipelineTransit + uiState.businessSummary.pipelineOrdered + 20,
            onOpenSettings = { showSettingsDemoSheet = true }
        )
    }

    if (showGoalsSheet) {
        GoalsSheet(
            goals = uiState.goals,
            onDismiss = { showGoalsSheet = false },
            onAddGoal = { name, target, saved, category, deadline ->
                viewModel.addGoal(name, target, saved, category, deadline)
            }
        )
    }

    if (showGermanSheet) {
        GermanSheet(
            onDismiss = { showGermanSheet = false },
            onCompleteSession = {
                viewModel.completeGermanReview()
            }
        )
    }

    if (showHealthSheet) {
        HealthSheet(
            summary = uiState.healthSummary,
            onDismiss = { showHealthSheet = false },
            onWaterClick = { viewModel.incrementWater() },
            onMoodSelect = { mood -> viewModel.setMood(mood) }
        )
    }

    if (showSettingsDemoSheet) {
        SettingsDemoSheet(
            themeMode = uiState.themeMode,
            onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
            onDismiss = { showSettingsDemoSheet = false },
            onLoadDemo = { viewModel.loadDemoData() },
            onClearDemo = { viewModel.clearDemoData() },
            onExportJson = {
                viewModel.exportBackup {
                    Toast.makeText(context, "Sauvegarde exportée avec succès", Toast.LENGTH_SHORT).show()
                }
            },
            onImportJson = { showImportConfirmDialog = true },
            onWipeAllData = { showWipeConfirmDialog = true }
        )
    }

    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = {
                Text(
                    text = "Réinitialiser toutes les données ?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Attention : cette action est irréversible.\n\n" +
                    "Toutes vos données (habitudes, finances, tâches, historique de santé) seront définitivement effacées. " +
                    "Votre niveau et vos points d'expérience (XP) seront remis à zéro."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWipeConfirmDialog = false
                        viewModel.wipeAllData()
                        Toast.makeText(context, "Toutes les données ont été réinitialisées", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Tout effacer", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("Importer la sauvegarde locale") },
            text = { Text("Voulez-vous restaurer les données depuis la dernière sauvegarde locale ? Les données actuelles seront conservées et fusionnées.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        val file = java.io.File(context.filesDir, "ma_vie_backup.json")
                        if (file.exists()) {
                            viewModel.importBackup(file.readText()) { success ->
                                Toast.makeText(context, if (success) "Sauvegarde restaurée" else "Erreur de restauration", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Aucun fichier de sauvegarde trouvé", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Restaurer", color = CyanPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
