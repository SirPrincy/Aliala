package com.example.util

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HealthEntryEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class WalletBalance(
    val wallet: WalletEntity,
    val currentBalance: Long
)

data class CategoryShare(
    val category: String,
    val amount: Long,
    val percentage: Int,
    val colorHex: String
)

data class MonthBarData(
    val monthLabel: String,
    val expenses: Long,
    val revenues: Long
)

data class FinanceSummary(
    val totalBalance: Long,
    val monthlyRevenues: Long,
    val monthlyExpenses: Long,
    val percentChangeVsPreviousMonth: Int?,
    val budgetAmount: Long,
    val budgetConsumedAmount: Long,
    val budgetPercentage: Int,
    val budgetRemaining: Long,
    val dailyRemaining: Long,
    val walletBalances: List<WalletBalance>,
    val categoryShares: List<CategoryShare>,
    val semesterHistory: List<MonthBarData>
)

data class BusinessSummary(
    val profit: Long,
    val sales: Long,
    val stockCount: Int,
    val pipelineOrdered: Int,
    val pipelineTransit: Int,
    val pipelineArrived: Int,
    val pipelineSold: Int,
    val activeParcel: BusinessOrderEntity?,
    val lowStockCount: Int,
    val clientsToFollowCount: Int
)

data class HealthSummary(
    val waterGlasses: Int,
    val sleepHours: Double,
    val weightKg: Double?,
    val steps: Int,
    val mood: Int,
    val sleepAverage7Days: Double,
    val sleepPoints: List<Float>
)

data class HabitStatus(
    val habit: HabitEntity,
    val streak: Int,
    val isDoneToday: Boolean
)

data class DayWeeklyStatus(
    val dayLetter: String, // "L", "M", "M", "J", "V", "S", "D"
    val isCompleted: Boolean,
    val isToday: Boolean,
    val countDone: Int
)

data class HabitsSummary(
    val completedToday: Int,
    val totalHabits: Int,
    val habits: List<HabitStatus>,
    val weeklyStatus: List<DayWeeklyStatus>,
    val overallStreak: Int = 0
)

data class FeaturedLoan(
    val loan: LoanEntity,
    val remainingAmount: Long,
    val repaidAmount: Long,
    val progressPercent: Int,
    val daysRemaining: Long
)

data class LoansSummary(
    val totalBorrowedRemaining: Long, // Je dois
    val totalLentRemaining: Long, // On me doit
    val featuredLoan: FeaturedLoan?
)

data class DayProgressSummary(
    val tasksDone: Int,
    val totalTasks: Int,
    val habitsDone: Int,
    val totalHabits: Int,
    val waterGlasses: Int,
    val progressPercent: Int,
    val streakDays: Int,
    val level: Int
)

data class UpcomingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val badgeColorHex: String,
    val isUrgent: Boolean,
    val iconType: String
)

object Calculations {

    const val DOMESTIC_PORT_YUAN = 15.0

    fun computeFinanceSummary(
        wallets: List<WalletEntity>,
        transactions: List<TransactionEntity>,
        selectedMonthKey: String,
        budget: BudgetEntity?
    ): FinanceSummary {
        // Calculate per-wallet balances: initialBalance + sum(income/expense)
        val walletBalances = wallets.map { wallet ->
            val txForWallet = transactions.filter { it.walletId == wallet.id }
            val net = txForWallet.sumOf { tx ->
                when (tx.type) {
                    "INCOME" -> tx.amount
                    "EXPENSE" -> -tx.amount
                    else -> 0L
                }
            }
            WalletBalance(wallet, wallet.initialBalance + net)
        }

        val totalBalance = walletBalances.sumOf { it.currentBalance }

        // Filter transactions for selected month
        val ym = try { YearMonth.parse(selectedMonthKey) } catch (e: Exception) { YearMonth.now() }
        val startMonthMillis = ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMonthMillis = ym.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val monthTransactions = transactions.filter { it.date in startMonthMillis..endMonthMillis }

        val monthlyRevenues = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val monthlyExpenses = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // Compare with previous month
        val prevYm = ym.minusMonths(1)
        val prevStart = prevYm.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val prevEnd = prevYm.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val prevExpenses = transactions.filter { it.date in prevStart..prevEnd && it.type == "EXPENSE" }.sumOf { it.amount }

        val percentChange = if (prevExpenses > 0) {
            (((monthlyExpenses - prevExpenses).toDouble() / prevExpenses.toDouble()) * 100).roundToInt()
        } else {
            12 // reasonable default indicator
        }

        val budgetAmount = budget?.amount ?: 1_000_000L
        val budgetPercentage = if (budgetAmount > 0) {
            ((monthlyExpenses.toDouble() / budgetAmount.toDouble()) * 100).roundToInt()
        } else 0
        val budgetRemaining = budgetAmount - monthlyExpenses

        val today = LocalDate.now()
        val daysInMonth = ym.lengthOfMonth()
        val daysLeft = if (ym == YearMonth.now()) {
            (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)
        } else {
            daysInMonth
        }
        val dailyRemaining = (budgetRemaining / daysLeft).coerceAtLeast(0L)

        // Category shares
        val categories = listOf("Nourriture", "Business", "Loisirs", "Transport", "Autres")
        val categoryColors = mapOf(
            "Nourriture" to "#06B6D4",
            "Business" to "#3B82F6",
            "Loisirs" to "#7C3AED",
            "Transport" to "#22C55E",
            "Autres" to "#94A3B8"
        )
        val expensesByCategory = monthTransactions.filter { it.type == "EXPENSE" }.groupBy { it.category }
        val totalExp = monthlyExpenses.coerceAtLeast(1L)
        val categoryShares = categories.map { cat ->
            val sum = expensesByCategory[cat]?.sumOf { it.amount } ?: 0L
            val pct = ((sum.toDouble() / totalExp.toDouble()) * 100).roundToInt()
            CategoryShare(cat, sum, pct, categoryColors[cat] ?: "#94A3B8")
        }

        // Semester history (last 6 months)
        val semesterHistory = (5 downTo 0).map { i ->
            val mYm = YearMonth.now().minusMonths(i.toLong())
            val mStart = mYm.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val mEnd = mYm.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val mTx = transactions.filter { it.date in mStart..mEnd }
            val exp = mTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val rev = mTx.filter { it.type == "INCOME" }.sumOf { it.amount }
            val label = mYm.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH)
                .replaceFirstChar { it.uppercase() }.take(4)
            MonthBarData(label, exp, rev)
        }

        return FinanceSummary(
            totalBalance = totalBalance,
            monthlyRevenues = monthlyRevenues,
            monthlyExpenses = monthlyExpenses,
            percentChangeVsPreviousMonth = percentChange,
            budgetAmount = budgetAmount,
            budgetConsumedAmount = monthlyExpenses,
            budgetPercentage = budgetPercentage,
            budgetRemaining = budgetRemaining,
            dailyRemaining = dailyRemaining,
            walletBalances = walletBalances,
            categoryShares = categoryShares,
            semesterHistory = semesterHistory
        )
    }

    fun computeBusinessSummary(
        orders: List<BusinessOrderEntity>,
        salesList: List<com.example.data.local.entity.BusinessSaleEntity> = emptyList()
    ): BusinessSummary {
        val soldOrders = orders.filter { it.status == "SOLD" }
        val salesFromOrders = soldOrders.sumOf { it.resalePriceAr }
        val salesFromDirectSales = salesList.sumOf { it.totalPriceAr }
        val totalSales = salesFromOrders + salesFromDirectSales

        val profitFromOrders = soldOrders.sumOf { (it.resalePriceAr - (it.priceYuan * it.exchangeRate).toLong()) }
        val profitFromDirectSales = salesList.sumOf { (it.totalPriceAr - (it.unitCostAr * it.quantity)) }
        val totalProfit = profitFromOrders + profitFromDirectSales

        val arrivedOrders = orders.filter { it.status == "ARRIVED" }
        val stockCount = arrivedOrders.sumOf { if (it.stockCount > 0) it.stockCount else it.quantityBought }

        val pipelineOrdered = orders.count { it.status == "ORDERED" }
        val pipelineTransit = orders.count { it.status == "IN_TRANSIT" }
        val pipelineArrived = orders.count { it.status == "ARRIVED" }
        val pipelineSold = orders.count { it.status == "SOLD" } + salesList.size

        val activeParcel = orders.filter { it.status == "IN_TRANSIT" }.minByOrNull { it.estimatedArrival }
        
        val lowStockCount = orders.count { 
            (it.status == "ARRIVED" && (it.restockAlert || (if (it.stockCount > 0) it.stockCount else it.quantityBought) <= 2)) 
        }

        val unpaidSalesCount = salesList.count { !it.isFullyPaid }
        val pendingDeliveryClientsCount = orders.count { it.status == "ARRIVED" && it.assignedClient.isNotBlank() }
        val clientsToFollowCount = unpaidSalesCount + pendingDeliveryClientsCount

        return BusinessSummary(
            profit = totalProfit,
            sales = totalSales,
            stockCount = stockCount,
            pipelineOrdered = pipelineOrdered,
            pipelineTransit = pipelineTransit,
            pipelineArrived = pipelineArrived,
            pipelineSold = pipelineSold,
            activeParcel = activeParcel,
            lowStockCount = lowStockCount,
            clientsToFollowCount = clientsToFollowCount
        )
    }

    fun computeHealthSummary(entries: List<HealthEntryEntity>): HealthSummary {
        val todayStr = DateUtils.getTodayDateString()
        val todayEntry = entries.find { it.date == todayStr }

        // Last 7 days
        val today = LocalDate.now()
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }
        val last7Entries = last7Days.map { d -> entries.find { it.date == d } }

        val sleepPoints = last7Entries.map { (it?.sleepHours ?: 0.0).toFloat() }
        val sleepDaysWithData = last7Entries.filterNotNull().filter { it.sleepHours > 0.0 }
        val sleepAverage7Days = if (sleepDaysWithData.isNotEmpty()) sleepDaysWithData.map { it.sleepHours }.average() else 0.0

        val weightEntries = entries.filter { it.weightKg != null && it.weightKg > 0.0 }.sortedBy { it.date }
        val currentWeightKg = todayEntry?.weightKg ?: weightEntries.lastOrNull()?.weightKg

        return HealthSummary(
            waterGlasses = todayEntry?.waterGlasses ?: 0,
            sleepHours = todayEntry?.sleepHours ?: 0.0,
            weightKg = currentWeightKg,
            steps = todayEntry?.steps ?: 0,
            mood = todayEntry?.mood ?: -1,
            sleepAverage7Days = sleepAverage7Days,
            sleepPoints = sleepPoints
        )
    }

    fun computeHabitsSummary(habits: List<HabitEntity>, logs: List<HabitLogEntity>): HabitsSummary {
        val todayStr = DateUtils.getTodayDateString()
        val todayLogs = logs.filter { it.date == todayStr }
        val completedToday = habits.count { h -> todayLogs.any { it.habitId == h.id } }

        val habitsWithStatus = habits.map { h ->
            val hLogs = logs.filter { it.habitId == h.id }.map { it.date }.toSet()
            val isDoneToday = hLogs.contains(todayStr)

            // Compute consecutive streak ending today or yesterday
            var streak = 0
            var checkDate = LocalDate.now()
            if (!hLogs.contains(checkDate.toString())) {
                checkDate = checkDate.minusDays(1)
            }
            while (hLogs.contains(checkDate.toString())) {
                streak++
                checkDate = checkDate.minusDays(1)
            }

            HabitStatus(h, streak, isDoneToday)
        }

        // Weekly days L, M, M, J, V, S, D
        val now = LocalDate.now()
        val monday = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val dayLetters = listOf("L", "M", "M", "J", "V", "S", "D")

        val weeklyStatus = (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateStr = date.toString()
            val doneCount = logs.count { it.date == dateStr }
            DayWeeklyStatus(
                dayLetter = dayLetters[i],
                isCompleted = doneCount >= habits.size.coerceAtLeast(1) && doneCount > 0,
                isToday = date == now,
                countDone = doneCount
            )
        }

        // Compute overall streak across all habit logs
        val datesWithLogs = logs.map { it.date }.toSet()
        val todayStrFormatted = now.toString()
        val yesterdayStr = now.minusDays(1).toString()
        var currDate = if (datesWithLogs.contains(todayStrFormatted)) now else if (datesWithLogs.contains(yesterdayStr)) now.minusDays(1) else null
        var overallStreak = 0
        while (currDate != null && datesWithLogs.contains(currDate.toString())) {
            overallStreak++
            currDate = currDate.minusDays(1)
        }

        return HabitsSummary(
            completedToday = completedToday,
            totalHabits = habits.size,
            habits = habitsWithStatus,
            weeklyStatus = weeklyStatus,
            overallStreak = overallStreak
        )
    }

    fun computeLoansSummary(loans: List<LoanEntity>, payments: List<LoanPaymentEntity>): LoansSummary {
        val paymentsByLoan = payments.groupBy { it.loanId }

        val borrowedLoans = loans.filter { it.direction == "BORROWED" }
        val lentLoans = loans.filter { it.direction == "LENT" }

        val totalBorrowedRemaining = borrowedLoans.sumOf { loan ->
            val repaid = paymentsByLoan[loan.id]?.sumOf { it.amount } ?: 0L
            (loan.amount - repaid).coerceAtLeast(0L)
        }

        val totalLentRemaining = lentLoans.sumOf { loan ->
            val repaid = paymentsByLoan[loan.id]?.sumOf { it.amount } ?: 0L
            (loan.amount - repaid).coerceAtLeast(0L)
        }

        val featured = borrowedLoans.minByOrNull { it.dueDate }?.let { loan ->
            val repaid = paymentsByLoan[loan.id]?.sumOf { it.amount } ?: 0L
            val remaining = (loan.amount - repaid).coerceAtLeast(0L)
            val pct = if (loan.amount > 0) ((repaid.toDouble() / loan.amount.toDouble()) * 100).roundToInt() else 0
            val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofInstant(java.time.Instant.ofEpochMilli(loan.dueDate), ZoneId.systemDefault()))
            FeaturedLoan(loan, remaining, repaid, pct, days)
        }

        return LoansSummary(
            totalBorrowedRemaining = totalBorrowedRemaining,
            totalLentRemaining = totalLentRemaining,
            featuredLoan = featured
        )
    }

    fun computeDayProgress(
        tasks: List<TaskEntity>,
        habitsSummary: HabitsSummary,
        healthSummary: HealthSummary,
        totalXp: Long = 0L
    ): DayProgressSummary {
        val tasksDone = tasks.count { it.isCompleted }
        val totalTasks = tasks.size.coerceAtLeast(1)
        val taskRatio = tasksDone.toDouble() / totalTasks

        val habitsDone = habitsSummary.completedToday
        val totalHabits = habitsSummary.totalHabits.coerceAtLeast(1)
        val habitRatio = habitsDone.toDouble() / totalHabits

        val waterGlasses = healthSummary.waterGlasses
        val waterRatio = (waterGlasses.toDouble() / 8.0).coerceAtMost(1.0)

        val combinedPercent = (((taskRatio + habitRatio + waterRatio) / 3.0) * 100).roundToInt().coerceIn(0, 100)
        val streak = if (habitsSummary.overallStreak > 0) habitsSummary.overallStreak else (habitsSummary.habits.maxOfOrNull { it.streak } ?: 0)
        val level = ((totalXp / 500) + 1).toInt()

        return DayProgressSummary(
            tasksDone = tasksDone,
            totalTasks = tasks.size,
            habitsDone = habitsDone,
            totalHabits = habitsSummary.totalHabits,
            waterGlasses = waterGlasses,
            progressPercent = if (tasks.isEmpty() && habitsSummary.totalHabits == 0) 0 else combinedPercent,
            streakDays = streak,
            level = level
        )
    }

    fun computeUpcomingItems(
        tasks: List<TaskEntity>,
        loans: List<LoanEntity>,
        orders: List<BusinessOrderEntity>
    ): List<UpcomingItem> {
        val items = mutableListOf<UpcomingItem>()

        // Non-completed tasks with upcoming or overdue dates
        tasks.filter { !it.isCompleted }.take(2).forEach { t ->
            val relText = DateUtils.formatRelativeDays(t.dueDate)
            items.add(
                UpcomingItem(
                    id = "task_${t.id}",
                    title = t.title,
                    subtitle = "Tâche à réaliser",
                    badgeText = relText,
                    badgeColorHex = if (relText == "En retard") "#FEE2E2" else "#EEF2FF",
                    isUrgent = relText == "En retard" || relText == "Aujourd'hui",
                    iconType = "task"
                )
            )
        }

        // Active borrowed loans
        loans.filter { it.direction == "BORROWED" }.take(2).forEach { l ->
            val relText = DateUtils.formatRelativeDays(l.dueDate)
            items.add(
                UpcomingItem(
                    id = "loan_${l.id}",
                    title = "Remboursement emprunt",
                    subtitle = "${l.name} · ${CurrencyFormatter.formatAriary(l.amount)}",
                    badgeText = relText,
                    badgeColorHex = if (relText == "En retard") "#FEE2E2" else "#F5F3FF",
                    isUrgent = relText == "En retard",
                    iconType = "loan"
                )
            )
        }

        // Active parcels in transit
        orders.filter { it.status == "IN_TRANSIT" }.take(2).forEach { o ->
            val relText = DateUtils.formatRelativeDays(o.estimatedArrival)
            items.add(
                UpcomingItem(
                    id = "order_${o.id}",
                    title = "Colis ${o.product}",
                    subtitle = if (o.supplier.isNotBlank()) "Fournisseur: ${o.supplier}" else "Arrivée estimée à Tana",
                    badgeText = relText,
                    badgeColorHex = "#EFF6FF",
                    isUrgent = false,
                    iconType = "parcel"
                )
            )
        }

        return items
    }
}
