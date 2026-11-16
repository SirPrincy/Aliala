package com.example.data.backup

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.CatalogueFamilyEntity
import com.example.data.local.entity.CataloguePlatformEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HealthEntryEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.SalePaymentEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletEntity
import com.example.data.local.entity.YuanTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    suspend fun exportToJson(context: Context, db: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
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

        // Business Orders (all 51 columns)
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
                put("category", o.category)
                put("carrier", o.carrier)
                put("variants", o.variants)
                put("freightCostAr", o.freightCostAr)
                put("isFreightPaid", o.isFreightPaid)
                put("freightDueDate", o.freightDueDate ?: JSONObject.NULL)
                put("imageUrl", o.imageUrl)
                put("stockCount", o.stockCount)
                put("unitsSold", o.unitsSold)
                put("marginPercent", o.marginPercent)
                put("restockAlert", o.restockAlert)
                put("referenceCode", o.referenceCode)
                put("platform", o.platform)
                put("weightKg", o.weightKg ?: JSONObject.NULL)
                put("lotNumber", o.lotNumber)
                put("assignedClient", o.assignedClient)
                put("family", o.family)
                put("reservedCount", o.reservedCount)
                put("defectiveCount", o.defectiveCount)
                put("isHidden", o.isHidden)
                put("isDemo", o.isDemo)
                put("freightMode", o.freightMode)
                put("volumeM3", o.volumeM3 ?: JSONObject.NULL)
                put("receptionNotes", o.receptionNotes)
                put("trackingNumbers", o.trackingNumbers)
                put("deliveredTrackingNumbers", o.deliveredTrackingNumbers)
                put("receivedParcelsCount", o.receivedParcelsCount)
                put("receivedUnitsCount", o.receivedUnitsCount)
                put("incidentReport", o.incidentReport)
                put("inspectionResults", o.inspectionResults)
                put("yuanDebited", o.yuanDebited)
                put("yuanDebitedAmount", o.yuanDebitedAmount)
                put("customsTaxAr", o.customsTaxAr)
                put("refundYuan", o.refundYuan)
                put("lostCount", o.lostCount)
                put("unitCostAr", o.unitCostAr)
                put("domesticShippingYuan", o.domesticShippingYuan)
                put("familyId", o.familyId)
                put("supplierDispatchDelay", o.supplierDispatchDelay)
                put("parcelDetailsJson", o.parcelDetailsJson)
            })
        }
        root.put("businessOrders", orderArray)

        // Yuan Transactions
        val yuanTxArray = JSONArray()
        db.yuanTransactionDao().getAllTransactionsSnapshot().forEach { y ->
            yuanTxArray.put(JSONObject().apply {
                put("id", y.id)
                put("type", y.type)
                put("amountYuan", y.amountYuan)
                put("exchangeRate", y.exchangeRate)
                put("amountAr", y.amountAr)
                put("platform", y.platform)
                put("date", y.date)
                put("note", y.note)
                put("orderId", y.orderId ?: JSONObject.NULL)
                put("isDemo", y.isDemo)
            })
        }
        root.put("yuanTransactions", yuanTxArray)

        // Clients
        val clientArray = JSONArray()
        db.clientDao().getAllClientsSnapshot().forEach { c ->
            clientArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("category", c.category)
                put("avatarType", c.avatarType)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
                put("isDemo", c.isDemo)
            })
        }
        root.put("clients", clientArray)

        // Business Sales
        val salesArray = JSONArray()
        db.businessSaleDao().getAllSalesSnapshot().forEach { s ->
            salesArray.put(JSONObject().apply {
                put("id", s.id)
                put("productName", s.productName)
                put("clientName", s.clientName)
                put("clientPhone", s.clientPhone)
                put("quantity", s.quantity)
                put("unitResalePriceAr", s.unitResalePriceAr)
                put("unitCostAr", s.unitCostAr)
                put("totalPriceAr", s.totalPriceAr)
                put("paidAmountAr", s.paidAmountAr)
                put("isFullyPaid", s.isFullyPaid)
                put("saleDate", s.saleDate)
                put("dueDate", s.dueDate)
                put("invoiceNumber", s.invoiceNumber)
                put("saleSource", s.saleSource)
                put("domain", s.domain)
                put("linkedOrderId", s.linkedOrderId ?: JSONObject.NULL)
                put("clientId", s.clientId ?: JSONObject.NULL)
                put("isDemo", s.isDemo)
            })
        }
        root.put("businessSales", salesArray)

        // Sale Payments
        val salePaymentsArray = JSONArray()
        db.salePaymentDao().getAllPaymentsSnapshot().forEach { p ->
            salePaymentsArray.put(JSONObject().apply {
                put("id", p.id)
                put("saleId", p.saleId)
                put("amountAr", p.amountAr)
                put("paymentDate", p.paymentDate)
                put("paymentMethod", p.paymentMethod)
                put("note", p.note)
            })
        }
        root.put("salePayments", salePaymentsArray)

        // Catalogue Families
        val familyArray = JSONArray()
        db.catalogueFamilyDao().getAllFamiliesSnapshot().forEach { f ->
            familyArray.put(JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("emoji", f.emoji)
                put("colorHex", f.colorHex)
                put("displayOrder", f.displayOrder)
                put("isDemo", f.isDemo)
            })
        }
        root.put("catalogueFamilies", familyArray)

        // Catalogue Platforms
        val platformArray = JSONArray()
        db.cataloguePlatformDao().getAllPlatformsSnapshot().forEach { p ->
            platformArray.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("aliasKey", p.aliasKey)
                put("subtitle", p.subtitle)
                put("badgeText", p.badgeText)
                put("isVerified", p.isVerified)
                put("iconType", p.iconType)
                put("colorHex", p.colorHex)
                put("shippingTag", p.shippingTag)
                put("shippingTagIcon", p.shippingTagIcon)
                put("secondaryTag", p.secondaryTag)
                put("isActive", p.isActive)
                put("displayOrder", p.displayOrder)
                put("defaultYuanSpent", p.defaultYuanSpent)
                put("defaultOrdersCount", p.defaultOrdersCount)
                put("defaultLastDate", p.defaultLastDate)
                put("transitNotes", p.transitNotes)
                put("isDemo", p.isDemo)
            })
        }
        root.put("cataloguePlatforms", platformArray)

        // Catalogue Transitaires
        val transitaireArray = JSONArray()
        db.catalogueTransitaireDao().getAllTransitairesSnapshot().forEach { t ->
            transitaireArray.put(JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("contact", t.contact)
                put("address", t.address)
                put("defaultTariffPerKg", t.defaultTariffPerKg)
                put("notes", t.notes)
                put("airNormalRateAr", t.airNormalRateAr)
                put("airFastRateAr", t.airFastRateAr)
                put("seaRateAr", t.seaRateAr)
                put("airNormalLeadDays", t.airNormalLeadDays)
                put("airFastLeadDays", t.airFastLeadDays)
                put("seaNormalLeadDays", t.seaNormalLeadDays)
                put("goodsCategoryDelays", t.goodsCategoryDelays)
                put("rateApplicationDate", t.rateApplicationDate)
            })
        }
        root.put("catalogueTransitaires", transitaireArray)

        // Business Accounts
        val accountArray = JSONArray()
        db.businessAccountDao().getAllAccountsIncludeInactiveSnapshot().forEach { a ->
            accountArray.put(JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("subtitle", a.subtitle)
                put("shortCode", a.shortCode)
                put("balanceAr", a.balanceAr)
                put("isYuanAccount", a.isYuanAccount)
                put("balanceYuan", a.balanceYuan)
                put("isActive", a.isActive)
            })
        }
        root.put("businessAccounts", accountArray)

        // Business Transactions
        val bizTxArray = JSONArray()
        db.businessTransactionDao().getAllTransactionsSnapshot().forEach { bt ->
            bizTxArray.put(JSONObject().apply {
                put("id", bt.id)
                put("title", bt.title)
                put("type", bt.type)
                put("amountAr", bt.amountAr)
                put("accountName", bt.accountName)
                put("destinationAccount", bt.destinationAccount)
                put("yuanTransactionId", bt.yuanTransactionId ?: JSONObject.NULL)
                put("referenceTag", bt.referenceTag)
                put("dateMillis", bt.dateMillis)
            })
        }
        root.put("businessTransactions", bizTxArray)

        // Business Investments
        val investArray = JSONArray()
        db.businessInvestmentDao().getAllInvestmentsSnapshot().forEach { inv ->
            investArray.put(JSONObject().apply {
                put("id", inv.id)
                put("title", inv.title)
                put("category", inv.category)
                put("amountAr", inv.amountAr)
                put("estimatedValueAr", inv.estimatedValueAr)
                put("generatedReturnAr", inv.generatedReturnAr)
                put("status", inv.status)
                put("accountName", inv.accountName)
                put("linkedTransactionId", inv.linkedTransactionId ?: JSONObject.NULL)
                put("dateMillis", inv.dateMillis)
                put("notes", inv.notes)
            })
        }
        root.put("businessInvestments", investArray)

        // Business Debts
        val debtArray = JSONArray()
        db.businessDebtDao().getAllDebtsSnapshot().forEach { d ->
            debtArray.put(JSONObject().apply {
                put("id", d.id)
                put("title", d.title)
                put("direction", d.direction)
                put("counterparty", d.counterparty)
                put("principalAr", d.principalAr)
                put("paidAr", d.paidAr)
                put("interestPercent", d.interestPercent)
                put("dueDateMillis", d.dueDateMillis ?: JSONObject.NULL)
                put("createdAtMillis", d.createdAtMillis)
                put("repaymentFrequency", d.repaymentFrequency)
                put("accountName", d.accountName)
                put("linkedTransactionId", d.linkedTransactionId ?: JSONObject.NULL)
                put("notes", d.notes)
            })
        }
        root.put("businessDebts", debtArray)

        // Business Debt Payments
        val debtPaymentArray = JSONArray()
        db.businessDebtDao().getAllDebtPaymentsSnapshot().forEach { dp ->
            debtPaymentArray.put(JSONObject().apply {
                put("id", dp.id)
                put("debtId", dp.debtId)
                put("amountAr", dp.amountAr)
                put("accountName", dp.accountName)
                put("linkedTransactionId", dp.linkedTransactionId ?: JSONObject.NULL)
                put("dateMillis", dp.dateMillis)
                put("note", dp.note)
            })
        }
        root.put("businessDebtPayments", debtPaymentArray)

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
                            interestPercent = if (!obj.has("interestPercent") || obj.isNull("interestPercent")) null else obj.getDouble("interestPercent"),
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

            // Parse orders (full 51-column restore, backward-compatible with v1)
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
                            exchangeRate = obj.optDouble("exchangeRate", 650.0),
                            resalePriceAr = obj.optLong("resalePriceAr", 0L),
                            status = obj.optString("status", "ORDERED"),
                            orderDate = obj.optLong("orderDate", System.currentTimeMillis()),
                            estimatedArrival = obj.optLong("estimatedArrival", System.currentTimeMillis()),
                            soldDate = if (!obj.has("soldDate") || obj.isNull("soldDate")) null else obj.getLong("soldDate"),
                            category = obj.optString("category", "BUSINESS"),
                            carrier = obj.optString("carrier", ""),
                            variants = obj.optString("variants", ""),
                            freightCostAr = obj.optLong("freightCostAr", 0L),
                            isFreightPaid = obj.optBoolean("isFreightPaid", true),
                            freightDueDate = if (!obj.has("freightDueDate") || obj.isNull("freightDueDate")) null else obj.getLong("freightDueDate"),
                            imageUrl = obj.optString("imageUrl", ""),
                            stockCount = obj.optInt("stockCount", 0),
                            unitsSold = obj.optInt("unitsSold", 0),
                            marginPercent = obj.optInt("marginPercent", 40),
                            restockAlert = obj.optBoolean("restockAlert", false),
                            referenceCode = obj.optString("referenceCode", ""),
                            platform = obj.optString("platform", "1688"),
                            weightKg = if (!obj.has("weightKg") || obj.isNull("weightKg")) null else obj.getDouble("weightKg"),
                            lotNumber = obj.optString("lotNumber", ""),
                            assignedClient = obj.optString("assignedClient", ""),
                            family = obj.optString("family", "Électronique"),
                            reservedCount = obj.optInt("reservedCount", 0),
                            defectiveCount = obj.optInt("defectiveCount", 0),
                            isHidden = obj.optBoolean("isHidden", false),
                            isDemo = obj.optBoolean("isDemo", false),
                            freightMode = obj.optString("freightMode", "AÉRIEN"),
                            volumeM3 = if (!obj.has("volumeM3") || obj.isNull("volumeM3")) null else obj.getDouble("volumeM3"),
                            receptionNotes = obj.optString("receptionNotes", ""),
                            trackingNumbers = obj.optString("trackingNumbers", ""),
                            deliveredTrackingNumbers = obj.optString("deliveredTrackingNumbers", ""),
                            receivedParcelsCount = obj.optInt("receivedParcelsCount", 0),
                            receivedUnitsCount = obj.optInt("receivedUnitsCount", 0),
                            incidentReport = obj.optString("incidentReport", ""),
                            inspectionResults = obj.optString("inspectionResults", ""),
                            yuanDebited = obj.optBoolean("yuanDebited", false),
                            yuanDebitedAmount = obj.optDouble("yuanDebitedAmount", 0.0),
                            customsTaxAr = obj.optLong("customsTaxAr", 0L),
                            refundYuan = obj.optDouble("refundYuan", 0.0),
                            lostCount = obj.optInt("lostCount", 0),
                            unitCostAr = obj.optLong("unitCostAr", 0L),
                            domesticShippingYuan = obj.optDouble("domesticShippingYuan", 0.0),
                            familyId = obj.optLong("familyId", 0L),
                            supplierDispatchDelay = obj.optString("supplierDispatchDelay", ""),
                            parcelDetailsJson = obj.optString("parcelDetailsJson", "")
                        )
                    )
                }
            }

            // Parse yuan transactions
            val yuanTxs = mutableListOf<YuanTransactionEntity>()
            if (root.has("yuanTransactions")) {
                val array = root.getJSONArray("yuanTransactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    yuanTxs.add(
                        YuanTransactionEntity(
                            id = obj.optLong("id", 0L),
                            type = obj.optString("type", "BUY"),
                            amountYuan = obj.optDouble("amountYuan", 0.0),
                            exchangeRate = obj.optDouble("exchangeRate", 650.0),
                            amountAr = obj.optLong("amountAr", 0L),
                            platform = obj.optString("platform", "Alipay"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            note = obj.optString("note", ""),
                            orderId = if (!obj.has("orderId") || obj.isNull("orderId")) null else obj.getLong("orderId"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse clients
            val clients = mutableListOf<ClientEntity>()
            if (root.has("clients")) {
                val array = root.getJSONArray("clients")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    clients.add(
                        ClientEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            category = obj.optString("category", "Business"),
                            avatarType = obj.optString("avatarType", "CYAN"),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse business sales
            val sales = mutableListOf<BusinessSaleEntity>()
            if (root.has("businessSales")) {
                val array = root.getJSONArray("businessSales")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    sales.add(
                        BusinessSaleEntity(
                            id = obj.optLong("id", 0L),
                            productName = obj.optString("productName", ""),
                            clientName = obj.optString("clientName", ""),
                            clientPhone = obj.optString("clientPhone", ""),
                            quantity = obj.optInt("quantity", 1),
                            unitResalePriceAr = obj.optLong("unitResalePriceAr", 0L),
                            unitCostAr = obj.optLong("unitCostAr", 0L),
                            totalPriceAr = obj.optLong("totalPriceAr", 0L),
                            paidAmountAr = obj.optLong("paidAmountAr", 0L),
                            isFullyPaid = obj.optBoolean("isFullyPaid", true),
                            saleDate = obj.optLong("saleDate", System.currentTimeMillis()),
                            dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                            invoiceNumber = obj.optString("invoiceNumber", ""),
                            saleSource = obj.optString("saleSource", "STOCK"),
                            domain = obj.optString("domain", "BUSINESS"),
                            linkedOrderId = if (!obj.has("linkedOrderId") || obj.isNull("linkedOrderId")) null else obj.getLong("linkedOrderId"),
                            clientId = if (!obj.has("clientId") || obj.isNull("clientId")) null else obj.getLong("clientId"),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse sale payments
            val salePayments = mutableListOf<SalePaymentEntity>()
            if (root.has("salePayments")) {
                val array = root.getJSONArray("salePayments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    salePayments.add(
                        SalePaymentEntity(
                            id = obj.optLong("id", 0L),
                            saleId = obj.getLong("saleId"),
                            amountAr = obj.optLong("amountAr", 0L),
                            paymentDate = obj.optLong("paymentDate", System.currentTimeMillis()),
                            paymentMethod = obj.optString("paymentMethod", "ESPECES"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // Parse catalogue families
            val families = mutableListOf<CatalogueFamilyEntity>()
            if (root.has("catalogueFamilies")) {
                val array = root.getJSONArray("catalogueFamilies")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    families.add(
                        CatalogueFamilyEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            emoji = obj.optString("emoji", "🎧"),
                            colorHex = obj.optString("colorHex", "#06B6D4"),
                            displayOrder = obj.optInt("displayOrder", 0),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse catalogue platforms
            val platforms = mutableListOf<CataloguePlatformEntity>()
            if (root.has("cataloguePlatforms")) {
                val array = root.getJSONArray("cataloguePlatforms")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    platforms.add(
                        CataloguePlatformEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            aliasKey = obj.optString("aliasKey", ""),
                            subtitle = obj.optString("subtitle", ""),
                            badgeText = obj.optString("badgeText", ""),
                            isVerified = obj.optBoolean("isVerified", false),
                            iconType = obj.optString("iconType", "STORE"),
                            colorHex = obj.optString("colorHex", "#712ae2"),
                            shippingTag = obj.optString("shippingTag", "Port Payant (¥ 10 - ¥ 25)"),
                            shippingTagIcon = obj.optString("shippingTagIcon", "local_shipping"),
                            secondaryTag = obj.optString("secondaryTag", "Min. commande : 2-5 pcs"),
                            isActive = obj.optBoolean("isActive", true),
                            displayOrder = obj.optInt("displayOrder", 0),
                            defaultYuanSpent = obj.optDouble("defaultYuanSpent", 0.0),
                            defaultOrdersCount = obj.optInt("defaultOrdersCount", 0),
                            defaultLastDate = obj.optString("defaultLastDate", ""),
                            transitNotes = obj.optString("transitNotes", ""),
                            isDemo = obj.optBoolean("isDemo", false)
                        )
                    )
                }
            }

            // Parse catalogue transitaires
            val transitaires = mutableListOf<CatalogueTransitaireEntity>()
            if (root.has("catalogueTransitaires")) {
                val array = root.getJSONArray("catalogueTransitaires")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    transitaires.add(
                        CatalogueTransitaireEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            contact = obj.optString("contact", ""),
                            address = obj.optString("address", ""),
                            defaultTariffPerKg = obj.optLong("defaultTariffPerKg", 0L),
                            notes = obj.optString("notes", ""),
                            airNormalRateAr = obj.optLong("airNormalRateAr", 45000L),
                            airFastRateAr = obj.optLong("airFastRateAr", 60000L),
                            seaRateAr = obj.optLong("seaRateAr", 18000L),
                            airNormalLeadDays = obj.optString("airNormalLeadDays", "7 à 10 jours"),
                            airFastLeadDays = obj.optString("airFastLeadDays", "3 à 5 jours"),
                            seaNormalLeadDays = obj.optString("seaNormalLeadDays", "30 à 45 jours"),
                            goodsCategoryDelays = obj.optString("goodsCategoryDelays", ""),
                            rateApplicationDate = obj.optString("rateApplicationDate", "")
                        )
                    )
                }
            }

            // Parse business accounts
            val businessAccounts = mutableListOf<BusinessAccountEntity>()
            if (root.has("businessAccounts")) {
                val array = root.getJSONArray("businessAccounts")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    businessAccounts.add(
                        BusinessAccountEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            subtitle = obj.optString("subtitle", "Compte"),
                            shortCode = obj.optString("shortCode", "CPT"),
                            balanceAr = obj.optLong("balanceAr", 0L),
                            isYuanAccount = obj.optBoolean("isYuanAccount", false),
                            balanceYuan = obj.optDouble("balanceYuan", 0.0),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }

            // Parse business transactions
            val businessTransactions = mutableListOf<BusinessTransactionEntity>()
            if (root.has("businessTransactions")) {
                val array = root.getJSONArray("businessTransactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    businessTransactions.add(
                        BusinessTransactionEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            type = obj.getString("type"),
                            amountAr = obj.optLong("amountAr", 0L),
                            accountName = obj.optString("accountName", ""),
                            destinationAccount = obj.optString("destinationAccount", ""),
                            yuanTransactionId = if (!obj.has("yuanTransactionId") || obj.isNull("yuanTransactionId")) null else obj.getLong("yuanTransactionId"),
                            referenceTag = obj.optString("referenceTag", ""),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Parse business investments
            val businessInvestments = mutableListOf<BusinessInvestmentEntity>()
            if (root.has("businessInvestments")) {
                val array = root.getJSONArray("businessInvestments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    businessInvestments.add(
                        BusinessInvestmentEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            category = obj.optString("category", "MATERIEL"),
                            amountAr = obj.optLong("amountAr", 0L),
                            estimatedValueAr = obj.optLong("estimatedValueAr", 0L),
                            generatedReturnAr = obj.optLong("generatedReturnAr", 0L),
                            status = obj.optString("status", "ACTIF"),
                            accountName = obj.optString("accountName", "Externe"),
                            linkedTransactionId = if (!obj.has("linkedTransactionId") || obj.isNull("linkedTransactionId")) null else obj.getLong("linkedTransactionId"),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            // Parse business debts
            val businessDebts = mutableListOf<BusinessDebtEntity>()
            if (root.has("businessDebts")) {
                val array = root.getJSONArray("businessDebts")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    businessDebts.add(
                        BusinessDebtEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            direction = obj.optString("direction", "BORROWED"),
                            counterparty = obj.optString("counterparty", ""),
                            principalAr = obj.optLong("principalAr", 0L),
                            paidAr = obj.optLong("paidAr", 0L),
                            interestPercent = obj.optDouble("interestPercent", 0.0),
                            dueDateMillis = if (!obj.has("dueDateMillis") || obj.isNull("dueDateMillis")) null else obj.getLong("dueDateMillis"),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                            repaymentFrequency = obj.optString("repaymentFrequency", "Ponctuel"),
                            accountName = obj.optString("accountName", "Hors caisse"),
                            linkedTransactionId = if (!obj.has("linkedTransactionId") || obj.isNull("linkedTransactionId")) null else obj.getLong("linkedTransactionId"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            // Parse business debt payments
            val businessDebtPayments = mutableListOf<BusinessDebtPaymentEntity>()
            if (root.has("businessDebtPayments")) {
                val array = root.getJSONArray("businessDebtPayments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    businessDebtPayments.add(
                        BusinessDebtPaymentEntity(
                            id = obj.optLong("id", 0L),
                            debtId = obj.getLong("debtId"),
                            amountAr = obj.optLong("amountAr", 0L),
                            accountName = obj.optString("accountName", "Hors caisse"),
                            linkedTransactionId = if (!obj.has("linkedTransactionId") || obj.isNull("linkedTransactionId")) null else obj.getLong("linkedTransactionId"),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            note = obj.optString("note", "")
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
                            weightKg = if (!obj.has("weightKg") || obj.isNull("weightKg")) null else obj.getDouble("weightKg"),
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

            // Insert into Database (respecting Foreign Key ordering)
            if (wallets.isNotEmpty()) db.walletDao().insertWallets(wallets)
            if (transactions.isNotEmpty()) db.transactionDao().insertTransactions(transactions)
            if (budgets.isNotEmpty()) db.budgetDao().insertBudgets(budgets)
            if (tasks.isNotEmpty()) db.taskDao().insertTasks(tasks)
            if (habits.isNotEmpty()) db.habitDao().insertHabits(habits)
            if (habitLogs.isNotEmpty()) db.habitDao().insertLogs(habitLogs)
            if (loans.isNotEmpty()) db.loanDao().insertLoans(loans)
            if (payments.isNotEmpty()) db.loanDao().insertPayments(payments)

            if (orders.isNotEmpty()) db.businessOrderDao().insertOrders(orders)
            if (yuanTxs.isNotEmpty()) db.yuanTransactionDao().insertTransactions(yuanTxs)
            if (clients.isNotEmpty()) db.clientDao().insertClients(clients)
            if (sales.isNotEmpty()) db.businessSaleDao().insertSales(sales)
            if (salePayments.isNotEmpty()) {
                val validSaleIds = db.businessSaleDao().getAllSalesSnapshot().map { it.id }.toSet()
                val safeSalePayments = salePayments.filter { it.saleId in validSaleIds }
                if (safeSalePayments.isNotEmpty()) {
                    db.salePaymentDao().insertPayments(safeSalePayments)
                }
            }
            if (families.isNotEmpty()) db.catalogueFamilyDao().insertFamilies(families)
            if (platforms.isNotEmpty()) db.cataloguePlatformDao().insertPlatforms(platforms)
            if (transitaires.isNotEmpty()) db.catalogueTransitaireDao().insertTransitaires(transitaires)
            if (businessAccounts.isNotEmpty()) db.businessAccountDao().insertAccounts(businessAccounts)
            if (businessTransactions.isNotEmpty()) db.businessTransactionDao().insertTransactions(businessTransactions)
            if (businessInvestments.isNotEmpty()) db.businessInvestmentDao().insertInvestments(businessInvestments)
            if (businessDebts.isNotEmpty()) db.businessDebtDao().insertDebts(businessDebts)
            if (businessDebtPayments.isNotEmpty()) db.businessDebtDao().insertDebtPayments(businessDebtPayments)

            if (healthEntries.isNotEmpty()) db.healthDao().insertEntries(healthEntries)
            if (goals.isNotEmpty()) db.goalDao().insertGoals(goals)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
