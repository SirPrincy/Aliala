package com.example.data.backup

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HealthEntryEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    suspend fun exportToJson(context: Context, db: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("app", "Ma vie")

        // Wallets
        val walletsArray = JSONArray()
        db.walletDao().getAllWalletsSnapshot().forEach { w ->
            walletsArray.put(JSONObject().apply {
                put("id", w.id)
                put("name", w.name)
                put("iconName", w.iconName)
                put("initialBalance", w.initialBalance)
                put("isDemo", w.isDemo)
            })
        }
        root.put("wallets", walletsArray)

        // Transactions
        val txArray = JSONArray()
        db.transactionDao().getAllTransactionsSnapshot().forEach { t ->
            txArray.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("walletId", t.walletId)
                put("date", t.date)
                put("amount", t.amount)
                put("type", t.type)
                put("category", t.category)
                put("isDemo", t.isDemo)
            })
        }
        root.put("transactions", txArray)

        // Budgets
        val budgetArray = JSONArray()
        db.budgetDao().getAllBudgetsSnapshot().forEach { b ->
            budgetArray.put(JSONObject().apply {
                put("monthKey", b.monthKey)
                put("amount", b.amount)
                put("isDemo", b.isDemo)
            })
        }
        root.put("budgets", budgetArray)

        // Tasks
        val taskArray = JSONArray()
        db.taskDao().getAllTasksSnapshot().forEach { t ->
            taskArray.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("dueDate", t.dueDate)
                put("categoryColorHex", t.categoryColorHex)
                put("isCompleted", t.isCompleted)
                put("isDemo", t.isDemo)
            })
        }
        root.put("tasks", taskArray)

        // Habits
        val habitArray = JSONArray()
        db.habitDao().getAllHabitsSnapshot().forEach { h ->
            habitArray.put(JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("iconName", h.iconName)
                put("targetDaysPerWeek", h.targetDaysPerWeek)
                put("createdAt", h.createdAt)
                put("isDemo", h.isDemo)
                put("habitType", h.habitType)
                put("targetCount", h.targetCount)
                put("unit", h.unit)
                put("moment", h.moment)
                put("colorHex", h.colorHex)
                put("reminderTimes", h.reminderTimes)
                put("note", h.note)
            })
        }
        root.put("habits", habitArray)

        // Habit Logs
        val logArray = JSONArray()
        db.habitDao().getAllLogsSnapshot().forEach { l ->
            logArray.put(JSONObject().apply {
                put("id", l.id)
                put("habitId", l.habitId)
                put("date", l.date)
                put("isDemo", l.isDemo)
            })
        }
        root.put("habitLogs", logArray)

        // Loans
        val loanArray = JSONArray()
        db.loanDao().getAllLoansSnapshot().forEach { l ->
            loanArray.put(JSONObject().apply {
                put("id", l.id)
                put("direction", l.direction)
                put("counterpartyType", l.counterpartyType)
                put("name", l.name)
                put("amount", l.amount)
                put("interestPercent", l.interestPercent ?: JSONObject.NULL)
                put("dueDate", l.dueDate)
                put("createdAt", l.createdAt)
                put("isDemo", l.isDemo)
            })
        }
        root.put("loans", loanArray)

        // Loan Payments
        val paymentArray = JSONArray()
        db.loanDao().getAllPaymentsSnapshot().forEach { p ->
            paymentArray.put(JSONObject().apply {
                put("id", p.id)
                put("loanId", p.loanId)
                put("amount", p.amount)
                put("date", p.date)
                put("isDemo", p.isDemo)
            })
        }
        root.put("loanPayments", paymentArray)

        // Business Orders
        val orderArray = JSONArray()
        db.businessOrderDao().getAllOrdersSnapshot().forEach { o ->
            orderArray.put(JSONObject().apply {
                put("id", o.id)
                put("product", o.product)
                put("supplier", o.supplier)
                put("quantityBought", o.quantityBought)
                put("priceYuan", o.priceYuan)
                put("exchangeRate", o.exchangeRate)
                put("resalePriceAr", o.resalePriceAr)
                put("status", o.status)
                put("orderDate", o.orderDate)
                put("estimatedArrival", o.estimatedArrival)
                put("soldDate", o.soldDate ?: JSONObject.NULL)
                put("isDemo", o.isDemo)
            })
        }
        root.put("businessOrders", orderArray)

        // Health Entries
        val healthArray = JSONArray()
        db.healthDao().getAllEntriesSnapshot().forEach { he ->
            healthArray.put(JSONObject().apply {
                put("id", he.id)
                put("date", he.date)
                put("waterGlasses", he.waterGlasses)
                put("sleepHours", he.sleepHours)
                put("weightKg", he.weightKg ?: JSONObject.NULL)
                put("steps", he.steps)
                put("mood", he.mood)
                put("isDemo", he.isDemo)
            })
        }
        root.put("healthEntries", healthArray)

        // Goals
        val goalArray = JSONArray()
        db.goalDao().getAllGoalsSnapshot().forEach { g ->
            goalArray.put(JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("targetAmount", g.targetAmount)
                put("savedAmount", g.savedAmount)
                put("deadline", g.deadline)
                put("category", g.category)
                put("isDemo", g.isDemo)
            })
        }
        root.put("goals", goalArray)

        val jsonString = root.toString(2)
        // Also write to local app file
        val file = File(context.filesDir, "ma_vie_backup.json")
        file.writeText(jsonString)
        jsonString
    }

    suspend fun importFromJson(jsonString: String, db: AppDatabase): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "Ma vie") {
                return@withContext false
            }

            // Parse wallets
            val wallets = mutableListOf<WalletEntity>()
            if (root.has("wallets")) {
                val array = root.getJSONArray("wallets")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    wallets.add(
                        WalletEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            iconName = obj.optString("iconName", "wallet"),
                            initialBalance = obj.optLong("initialBalance", 0L),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse transactions
            val transactions = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    transactions.add(
                        TransactionEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            walletId = obj.optLong("walletId", 1L),
                            date = obj.getLong("date"),
                            amount = obj.getLong("amount"),
                            type = obj.getString("type"),
                            category = obj.getString("category"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse budgets
            val budgets = mutableListOf<BudgetEntity>()
            if (root.has("budgets")) {
                val array = root.getJSONArray("budgets")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    budgets.add(
                        BudgetEntity(
                            monthKey = obj.getString("monthKey"),
                            amount = obj.getLong("amount"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse tasks
            val tasks = mutableListOf<TaskEntity>()
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    tasks.add(
                        TaskEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            dueDate = obj.getLong("dueDate"),
                            categoryColorHex = obj.optString("categoryColorHex", "#EF4444"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse habits
            val habits = mutableListOf<HabitEntity>()
            if (root.has("habits")) {
                val array = root.getJSONArray("habits")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    habits.add(
                        HabitEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            iconName = obj.optString("iconName", "water"),
                            targetDaysPerWeek = obj.optInt("targetDaysPerWeek", 7),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            isDemo = obj.optBoolean("isDemo", false),
                            habitType = obj.optString("habitType", "BOOLEAN"),
                            targetCount = obj.optInt("targetCount", 1),
                            unit = obj.optString("unit", "fois"),
                            moment = obj.optString("moment", "Matin"),
                            colorHex = obj.optString("colorHex", "#06B6D4"),
                            reminderTimes = obj.optString("reminderTimes", ""),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // Parse habit logs
            val habitLogs = mutableListOf<HabitLogEntity>()
            if (root.has("habitLogs")) {
                val array = root.getJSONArray("habitLogs")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    habitLogs.add(
                        HabitLogEntity(
                            id = obj.optLong("id", 0L),
                            habitId = obj.getLong("habitId"),
                            date = obj.getString("date"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse loans
            val loans = mutableListOf<LoanEntity>()
            if (root.has("loans")) {
                val array = root.getJSONArray("loans")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loans.add(
                        LoanEntity(
                            id = obj.optLong("id", 0L),
                            direction = obj.getString("direction"),
                            counterpartyType = obj.getString("counterpartyType"),
                            name = obj.getString("name"),
                            amount = obj.getLong("amount"),
                            interestPercent = if (obj.isNull("interestPercent")) null else obj.getDouble("interestPercent"),
                            dueDate = obj.getLong("dueDate"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse payments
            val payments = mutableListOf<LoanPaymentEntity>()
            if (root.has("loanPayments")) {
                val array = root.getJSONArray("loanPayments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    payments.add(
                        LoanPaymentEntity(
                            id = obj.optLong("id", 0L),
                            loanId = obj.getLong("loanId"),
                            amount = obj.getLong("amount"),
                            date = obj.getLong("date"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse orders
            val orders = mutableListOf<BusinessOrderEntity>()
            if (root.has("businessOrders")) {
                val array = root.getJSONArray("businessOrders")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    orders.add(
                        BusinessOrderEntity(
                            id = obj.optLong("id", 0L),
                            product = obj.getString("product"),
                            supplier = obj.optString("supplier", "Guangzhou"),
                            quantityBought = obj.optInt("quantityBought", 1),
                            priceYuan = obj.optDouble("priceYuan", 0.0),
                            exchangeRate = obj.optDouble("exchangeRate", 625.0),
                            resalePriceAr = obj.optLong("resalePriceAr", 0L),
                            status = obj.optString("status", "ORDERED"),
                            orderDate = obj.optLong("orderDate", System.currentTimeMillis()),
                            estimatedArrival = obj.optLong("estimatedArrival", System.currentTimeMillis()),
                            soldDate = if (obj.isNull("soldDate")) null else obj.getLong("soldDate"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse health
            val healthEntries = mutableListOf<HealthEntryEntity>()
            if (root.has("healthEntries")) {
                val array = root.getJSONArray("healthEntries")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    healthEntries.add(
                        HealthEntryEntity(
                            id = obj.optLong("id", 0L),
                            date = obj.getString("date"),
                            waterGlasses = obj.optInt("waterGlasses", 0),
                            sleepHours = obj.optDouble("sleepHours", 7.5),
                            weightKg = if (obj.isNull("weightKg")) null else obj.getDouble("weightKg"),
                            steps = obj.optInt("steps", 0),
                            mood = obj.optInt("mood", 1),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse goals
            val goals = mutableListOf<GoalEntity>()
            if (root.has("goals")) {
                val array = root.getJSONArray("goals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    goals.add(
                        GoalEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            targetAmount = obj.getLong("targetAmount"),
                            savedAmount = obj.getLong("savedAmount"),
                            deadline = obj.getLong("deadline"),
                            category = obj.optString("category", "Épargne Projet"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Insert into Database
            if (wallets.isNotEmpty()) db.walletDao().insertWallets(wallets)
            if (transactions.isNotEmpty()) db.transactionDao().insertTransactions(transactions)
            if (budgets.isNotEmpty()) db.budgetDao().insertBudgets(budgets)
            if (tasks.isNotEmpty()) db.taskDao().insertTasks(tasks)
            if (habits.isNotEmpty()) db.habitDao().insertHabits(habits)
            if (habitLogs.isNotEmpty()) db.habitDao().insertLogs(habitLogs)
            if (loans.isNotEmpty()) db.loanDao().insertLoans(loans)
            if (payments.isNotEmpty()) db.loanDao().insertPayments(payments)
            if (orders.isNotEmpty()) db.businessOrderDao().insertOrders(orders)
            if (healthEntries.isNotEmpty()) db.healthDao().insertEntries(healthEntries)
            if (goals.isNotEmpty()) db.goalDao().insertGoals(goals)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
