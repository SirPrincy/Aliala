package com.example.data.repository

import android.content.Context
import com.example.data.backup.BackupManager
import com.example.data.datastore.SettingsDataStore
import com.example.data.demo.DemoDataProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessAccountEntity
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
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    val wallets: Flow<List<WalletEntity>> = db.walletDao().getAllWallets()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val tasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    val habits: Flow<List<HabitEntity>> = db.habitDao().getAllHabits()
    val habitLogs: Flow<List<HabitLogEntity>> = db.habitDao().getAllLogs()
    val loans: Flow<List<LoanEntity>> = db.loanDao().getAllLoans()
    val loanPayments: Flow<List<LoanPaymentEntity>> = db.loanDao().getAllPayments()
    val businessOrders: Flow<List<BusinessOrderEntity>> = db.businessOrderDao().getAllOrders()
    val yuanTransactions: Flow<List<YuanTransactionEntity>> = db.yuanTransactionDao().getAllTransactions()
    val businessSales: Flow<List<BusinessSaleEntity>> = db.businessSaleDao().getAllSales()
    val catalogueFamilies: Flow<List<CatalogueFamilyEntity>> = db.catalogueFamilyDao().getAllFamilies()
    val cataloguePlatforms: Flow<List<CataloguePlatformEntity>> = db.cataloguePlatformDao().getAllPlatforms()
    val clients: Flow<List<ClientEntity>> = db.clientDao().getAllClients()
    val catalogueTransitaires: Flow<List<CatalogueTransitaireEntity>> = db.catalogueTransitaireDao().getAllTransitaires()
    val salePayments: Flow<List<SalePaymentEntity>> = db.salePaymentDao().getAllPayments()
    val healthEntries: Flow<List<HealthEntryEntity>> = db.healthDao().getAllEntries()
    val latestWeight: Flow<HealthEntryEntity?> = db.healthDao().getLatestWeightEntry()
    val firstWeight: Flow<HealthEntryEntity?> = db.healthDao().getFirstWeightEntry()
    val latestWaist: Flow<HealthEntryEntity?> = db.healthDao().getLatestWaistEntry()
    val latestBloodPressure: Flow<HealthEntryEntity?> = db.healthDao().getLatestBloodPressureEntry()
    val latestTemperature: Flow<HealthEntryEntity?> = db.healthDao().getLatestTemperatureEntry()
    val goals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()

    val hideAmounts: Flow<Boolean> = settingsDataStore.hideAmountsFlow
    val lastExportDate: Flow<Long?> = settingsDataStore.lastExportDateFlow
    val userName: Flow<String> = settingsDataStore.userNameFlow
    val themeMode: Flow<String> = settingsDataStore.themeModeFlow
    val totalXp: Flow<Long> = settingsDataStore.totalXpFlow
    val recordStreak: Flow<Int> = settingsDataStore.recordStreakFlow
    val userHeightCm: Flow<Float?> = settingsDataStore.userHeightCmFlow
    val weightTargetKg: Flow<Double> = settingsDataStore.weightTargetKgFlow

    fun getBudget(monthKey: String): Flow<BudgetEntity?> = db.budgetDao().getBudgetForMonth(monthKey)

    suspend fun addXp(delta: Long) {
        settingsDataStore.addXp(delta)
    }

    suspend fun setTotalXp(xp: Long) {
        settingsDataStore.setTotalXp(xp)
    }

    suspend fun updateRecordStreak(streak: Int) {
        settingsDataStore.updateRecordStreak(streak)
    }

    suspend fun setHideAmounts(hide: Boolean) {
        settingsDataStore.setHideAmounts(hide)
    }

    suspend fun setThemeMode(mode: String) {
        settingsDataStore.setThemeMode(mode)
    }

    suspend fun setUserHeightCm(heightCm: Float) {
        settingsDataStore.setUserHeightCm(heightCm)
        addXp(15L)
    }

    suspend fun setWeightTargetKg(target: Double) {
        settingsDataStore.setWeightTargetKg(target)
        addXp(15L)
    }

    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)

    suspend fun toggleTask(task: TaskEntity) {
        val willBeCompleted = !task.isCompleted
        db.taskDao().updateTask(task.copy(isCompleted = willBeCompleted))
        if (willBeCompleted) {
            addXp(15L)
        } else {
            addXp(-15L)
        }
    }

    suspend fun deleteTask(taskId: Long) = db.taskDao().deleteTaskById(taskId)

    suspend fun insertTransaction(transaction: TransactionEntity): Long = db.transactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = db.transactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transactionId: Long) = db.transactionDao().deleteTransactionById(transactionId)

    suspend fun setBudget(monthKey: String, amount: Long) {
        db.budgetDao().insertOrUpdate(BudgetEntity(monthKey = monthKey, amount = amount, isDemo = false))
    }

    suspend fun insertLoan(loan: LoanEntity): Long = db.loanDao().insertLoan(loan)

    suspend fun insertLoanPayment(payment: LoanPaymentEntity): Long = db.loanDao().insertPayment(payment)

    suspend fun insertBusinessOrder(order: BusinessOrderEntity): Long = db.businessOrderDao().insertOrder(order)

    suspend fun insertHabit(habit: HabitEntity): Long = db.habitDao().insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = db.habitDao().updateHabit(habit)

    suspend fun getAllHabitsSnapshot(): List<HabitEntity> = db.habitDao().getAllHabitsSnapshot()

    suspend fun getAllHabitLogsSnapshot(): List<HabitLogEntity> = db.habitDao().getAllLogsSnapshot()

    suspend fun getAllHealthEntriesSnapshot(): List<HealthEntryEntity> = db.healthDao().getAllEntriesSnapshot()

    suspend fun insertGoal(goal: GoalEntity): Long = db.goalDao().insertGoal(goal)

    suspend fun toggleHabitToday(habitId: Long) {
        val todayStr = DateUtils.getTodayDateString()
        toggleHabitForDate(habitId, todayStr)
    }

    suspend fun toggleHabitForDate(habitId: Long, dateStr: String) {
        val allLogs = db.habitDao().getAllLogsSnapshot()
        val exists = allLogs.any { it.habitId == habitId && it.date == dateStr }
        if (exists) {
            db.habitDao().deleteLog(habitId, dateStr)
            addXp(-25L)
        } else {
            db.habitDao().insertLog(HabitLogEntity(habitId = habitId, date = dateStr, isDemo = false))
            addXp(25L)
        }
    }

    suspend fun incrementWater() {
        val todayStr = DateUtils.getTodayDateString()
        val existing = db.healthDao().getEntryForDate(todayStr).firstOrNull()
        val nextGlasses = if (existing != null) {
            val count = (existing.waterGlasses + 1).coerceAtMost(16)
            db.healthDao().insertOrUpdate(existing.copy(waterGlasses = count))
            count
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = todayStr, waterGlasses = 1, sleepHours = 0.0, steps = 0, mood = -1)
            )
            1
        }
        addXp(10L)
        syncWaterHabitLog(todayStr, nextGlasses)
    }

    suspend fun decrementWater() {
        val todayStr = DateUtils.getTodayDateString()
        val existing = db.healthDao().getEntryForDate(todayStr).firstOrNull()
        if (existing != null && existing.waterGlasses > 0) {
            val updated = existing.waterGlasses - 1
            db.healthDao().insertOrUpdate(existing.copy(waterGlasses = updated))
            addXp(-10L)
            syncWaterHabitLog(todayStr, updated)
        }
    }

    suspend fun setWaterGlasses(glasses: Int) {
        val todayStr = DateUtils.getTodayDateString()
        val clamped = glasses.coerceIn(0, 16)
        val existing = db.healthDao().getEntryForDate(todayStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(existing.copy(waterGlasses = clamped))
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = todayStr, waterGlasses = clamped, sleepHours = 0.0, steps = 0, mood = -1)
            )
        }
        syncWaterHabitLog(todayStr, clamped)
    }

    private suspend fun syncWaterHabitLog(dateStr: String, glasses: Int) {
        val allHabits = db.habitDao().getAllHabitsSnapshot()
        val waterHabit = allHabits.firstOrNull {
            it.iconName == "water" || it.iconName == "💧" || it.unit == "verres" || it.name.contains("eau", ignoreCase = true) || it.name.contains("hydratation", ignoreCase = true)
        } ?: return

        val target = if (waterHabit.targetCount > 0) waterHabit.targetCount else 8
        val allLogs = db.habitDao().getAllLogsSnapshot()
        val hasLog = allLogs.any { it.habitId == waterHabit.id && it.date == dateStr }
        if (glasses >= target && !hasLog) {
            db.habitDao().insertLog(HabitLogEntity(habitId = waterHabit.id, date = dateStr, isDemo = false))
        } else if (glasses < target && hasLog) {
            db.habitDao().deleteLog(waterHabit.id, dateStr)
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        db.habitDao().deleteLogsByHabitId(habitId)
        db.habitDao().deleteHabitById(habitId)
    }

    suspend fun setMood(mood: Int) {
        val todayStr = DateUtils.getTodayDateString()
        val existing = db.healthDao().getEntryForDate(todayStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(existing.copy(mood = mood))
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = todayStr, waterGlasses = 0, sleepHours = 0.0, steps = 0, mood = mood)
            )
        }
    }

    suspend fun logSleep(hours: Double, bedTime: String, wakeTime: String, dateStr: String = DateUtils.getTodayDateString()) {
        val existing = db.healthDao().getEntryForDate(dateStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(existing.copy(sleepHours = hours, sleepBedTime = bedTime, sleepWakeTime = wakeTime))
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = dateStr, sleepHours = hours, sleepBedTime = bedTime, sleepWakeTime = wakeTime)
            )
        }
        addXp(15L)
    }

    suspend fun logWeight(weightKg: Double, dateStr: String = DateUtils.getTodayDateString()) {
        val existing = db.healthDao().getEntryForDate(dateStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(existing.copy(weightKg = weightKg))
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = dateStr, weightKg = weightKg)
            )
        }
        addXp(15L)
    }

    suspend fun logActivity(workoutMinutes: Int, distanceKm: Double, caloriesKcal: Int, additionalSteps: Int, dateStr: String = DateUtils.getTodayDateString()) {
        val existing = db.healthDao().getEntryForDate(dateStr).firstOrNull()
        if (existing != null) {
            val newSteps = existing.steps + additionalSteps
            val newMinutes = existing.workoutMinutes + workoutMinutes
            val newDist = existing.distanceKm + distanceKm
            val newCal = existing.caloriesKcal + caloriesKcal
            db.healthDao().insertOrUpdate(
                existing.copy(steps = newSteps, workoutMinutes = newMinutes, distanceKm = newDist, caloriesKcal = newCal)
            )
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(
                    date = dateStr,
                    steps = additionalSteps,
                    workoutMinutes = workoutMinutes,
                    distanceKm = distanceKm,
                    caloriesKcal = caloriesKcal
                )
            )
        }
        addXp(25L)
    }

    suspend fun logMood(mood: Int, note: String, dateStr: String = DateUtils.getTodayDateString()) {
        val existing = db.healthDao().getEntryForDate(dateStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(existing.copy(mood = mood, moodNote = note))
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(date = dateStr, mood = mood, moodNote = note)
            )
        }
        addXp(10L)
    }

    suspend fun logBodyMeasurement(
        waistCm: Double? = null,
        bloodPressure: String? = null,
        temperatureC: Double? = null,
        dateStr: String = DateUtils.getTodayDateString()
    ) {
        val existing = db.healthDao().getEntryForDate(dateStr).firstOrNull()
        if (existing != null) {
            db.healthDao().insertOrUpdate(
                existing.copy(
                    waistCm = waistCm ?: existing.waistCm,
                    bloodPressure = bloodPressure ?: existing.bloodPressure,
                    temperatureC = temperatureC ?: existing.temperatureC
                )
            )
        } else {
            db.healthDao().insertOrUpdate(
                HealthEntryEntity(
                    date = dateStr,
                    waistCm = waistCm,
                    bloodPressure = bloodPressure,
                    temperatureC = temperatureC
                )
            )
        }
        addXp(15L)
    }

    suspend fun updateBusinessOrder(order: BusinessOrderEntity) = db.businessOrderDao().updateOrder(order)
    suspend fun deleteBusinessOrder(orderId: Long) = db.businessOrderDao().deleteOrderById(orderId)

    suspend fun insertYuanTransaction(tx: YuanTransactionEntity): Long = db.yuanTransactionDao().insertTransaction(tx)
    suspend fun deleteYuanTransaction(id: Long) = db.yuanTransactionDao().deleteById(id)

    suspend fun insertBusinessSale(sale: BusinessSaleEntity): Long = db.businessSaleDao().insertSale(sale)
    suspend fun updateBusinessSale(sale: BusinessSaleEntity) = db.businessSaleDao().updateSale(sale)
    suspend fun deleteBusinessSale(id: Long) = db.businessSaleDao().deleteById(id)

    suspend fun insertCatalogueFamily(family: CatalogueFamilyEntity): Long = db.catalogueFamilyDao().insertFamily(family)
    suspend fun updateCatalogueFamily(family: CatalogueFamilyEntity) = db.catalogueFamilyDao().updateFamily(family)
    suspend fun deleteCatalogueFamily(id: Long) = db.catalogueFamilyDao().deleteFamilyById(id)
    suspend fun getAllCatalogueFamiliesSnapshot(): List<CatalogueFamilyEntity> = db.catalogueFamilyDao().getAllFamiliesSnapshot()

    suspend fun insertCataloguePlatform(platform: CataloguePlatformEntity): Long = db.cataloguePlatformDao().insertPlatform(platform)
    suspend fun updateCataloguePlatform(platform: CataloguePlatformEntity) = db.cataloguePlatformDao().updatePlatform(platform)
    suspend fun updatePlatformActive(id: Long, isActive: Boolean) = db.cataloguePlatformDao().updateActiveStatus(id, isActive)
    suspend fun deleteCataloguePlatform(id: Long) = db.cataloguePlatformDao().deletePlatformById(id)
    suspend fun getAllCataloguePlatformsSnapshot(): List<CataloguePlatformEntity> = db.cataloguePlatformDao().getAllPlatformsSnapshot()

    // Clients
    suspend fun insertClient(client: ClientEntity): Long = db.clientDao().insertClient(client)
    suspend fun updateClient(client: ClientEntity) = db.clientDao().updateClient(client)
    suspend fun deleteClient(id: Long) = db.clientDao().deleteClientById(id)
    suspend fun getClientById(id: Long): ClientEntity? = db.clientDao().getClientById(id)
    suspend fun findClientByName(name: String): ClientEntity? = db.clientDao().findClientByName(name)
    suspend fun getAllClientsSnapshot(): List<ClientEntity> = db.clientDao().getAllClientsSnapshot()

    // Catalogue Transitaires
    suspend fun insertCatalogueTransitaire(transitaire: CatalogueTransitaireEntity): Long = db.catalogueTransitaireDao().insertTransitaire(transitaire)
    suspend fun updateCatalogueTransitaire(transitaire: CatalogueTransitaireEntity) = db.catalogueTransitaireDao().updateTransitaire(transitaire)
    suspend fun deleteCatalogueTransitaire(id: Long) = db.catalogueTransitaireDao().deleteTransitaireById(id)
    suspend fun getAllCatalogueTransitairesSnapshot(): List<CatalogueTransitaireEntity> = db.catalogueTransitaireDao().getAllTransitairesSnapshot()

    // Sale Payments
    suspend fun insertSalePayment(payment: SalePaymentEntity): Long = db.salePaymentDao().insertPayment(payment)
    suspend fun getPaymentsForSale(saleId: Long): Flow<List<SalePaymentEntity>> = db.salePaymentDao().getPaymentsForSale(saleId)
    suspend fun getPaymentsForSaleSnapshot(saleId: Long): List<SalePaymentEntity> = db.salePaymentDao().getPaymentsForSaleSnapshot(saleId)
    suspend fun deleteSalePayment(id: Long) = db.salePaymentDao().deletePaymentById(id)

    suspend fun loadDemoData() {
        DemoDataProvider.insertDemoData(db)
        settingsDataStore.setTotalXp(1840L)
        settingsDataStore.updateRecordStreak(21)
    }

    suspend fun clearDemoData() {
        DemoDataProvider.clearDemoData(db)
        settingsDataStore.setTotalXp(0L)
        settingsDataStore.updateRecordStreak(0)
    }

    suspend fun wipeAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        db.walletDao().insertWallets(
            listOf(
                WalletEntity(id = 1, name = "Espèces", iconName = "cash", initialBalance = 0L, isDemo = false),
                WalletEntity(id = 2, name = "MVola", iconName = "mvola", initialBalance = 0L, isDemo = false),
                WalletEntity(id = 3, name = "Orange Money", iconName = "orange", initialBalance = 0L, isDemo = false),
                WalletEntity(id = 4, name = "Banque", iconName = "bank", initialBalance = 0L, isDemo = false)
            )
        )
        settingsDataStore.resetAllSettings()
    }

    suspend fun exportBackup(): String {
        val json = BackupManager.exportToJson(context, db)
        settingsDataStore.setLastExportDate(System.currentTimeMillis())
        return json
    }

    suspend fun importBackup(jsonString: String): Boolean {
        return BackupManager.importFromJson(jsonString, db)
    }

    suspend fun seedDefaultCatalogueTablesIfEmpty() = withContext(Dispatchers.IO) {
        if (db.catalogueFamilyDao().getAllFamiliesSnapshot().isEmpty()) {
            val defaultFamilies = listOf(
                CatalogueFamilyEntity(name = "Électronique & High-Tech", emoji = "🎧", colorHex = "#06B6D4", displayOrder = 1),
                CatalogueFamilyEntity(name = "Mode & Maroquinerie", emoji = "🎒", colorHex = "#EC4899", displayOrder = 2),
                CatalogueFamilyEntity(name = "Maison & Rangement", emoji = "🏠", colorHex = "#F97316", displayOrder = 3),
                CatalogueFamilyEntity(name = "Accessoires Mobiles", emoji = "📱", colorHex = "#8B5CF6", displayOrder = 4)
            )
            db.catalogueFamilyDao().insertFamilies(defaultFamilies)
        }

        if (db.cataloguePlatformDao().getAllPlatformsSnapshot().isEmpty()) {
            val defaultPlatforms = listOf(
                CataloguePlatformEntity(name = "1688.com", aliasKey = "1688", subtitle = "Alibaba Wholesale (Usines B2B)", isVerified = true, iconType = "TEXT_1688", colorHex = "#712ae2", shippingTag = "Port Payant (Sur devis)", shippingTagIcon = "local_shipping", secondaryTag = "Min. commande : 2-5 pcs", isActive = true, displayOrder = 1),
                CataloguePlatformEntity(name = "Taobao", aliasKey = "Taobao", subtitle = "Consommation & Petits lots", badgeText = "C2C / B2C", isVerified = false, iconType = "CART", colorHex = "#8a4cfc", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Livraison rapide", isActive = true, displayOrder = 2),
                CataloguePlatformEntity(name = "Pinduoduo", aliasKey = "Pinduoduo", subtitle = "Direct Usine / Déstockage", badgeText = "Usine Direct", isVerified = false, iconType = "PRICE", colorHex = "#ffdad6", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Remises groupées", isActive = true, displayOrder = 3),
                CataloguePlatformEntity(name = "Fournisseur Direct WeChat", aliasKey = "WeChat", subtitle = "Sino-Contact / Privé", isVerified = false, iconType = "WECHAT", colorHex = "#acedff", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "request_quote", secondaryTag = "Dépôt transitaire", isActive = true, displayOrder = 4),
                CataloguePlatformEntity(name = "Xianyu (闲鱼)", aliasKey = "Xianyu", subtitle = "B2C / C2C Occasion & Pièces rares", badgeText = "Seconde main", isVerified = false, iconType = "CART", colorHex = "#FFC107", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "local_shipping", secondaryTag = "Négociation vendeur", isActive = true, displayOrder = 5)
            )
            db.cataloguePlatformDao().insertPlatforms(defaultPlatforms)
        } else {
            val snapshot = db.cataloguePlatformDao().getAllPlatformsSnapshot()
            if (snapshot.none { it.name.contains("Xianyu", ignoreCase = true) }) {
                db.cataloguePlatformDao().insertPlatform(
                    CataloguePlatformEntity(name = "Xianyu (闲鱼)", aliasKey = "Xianyu", subtitle = "B2C / C2C Occasion & Pièces rares", badgeText = "Seconde main", isVerified = false, iconType = "CART", colorHex = "#FFC107", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "local_shipping", secondaryTag = "Négociation vendeur", isActive = true, displayOrder = 5)
                )
            }
        }

        // Transitaires par défaut supprimés : l'utilisateur crée ses propres transitaires
    }

    val allBusinessAccounts: Flow<List<BusinessAccountEntity>> = db.businessAccountDao().getAllAccounts()

    suspend fun seedDefaultBusinessAccountsIfEmpty() {
        if (db.businessAccountDao().getAccountCount() == 0) {
            val defaults = listOf(
                BusinessAccountEntity(name = "Espèces", subtitle = "Caisse", shortCode = "CASH", balanceAr = 0L, isYuanAccount = false),
                BusinessAccountEntity(name = "Mvola", subtitle = "Mobile Money", shortCode = "MV", balanceAr = 0L, isYuanAccount = false),
                BusinessAccountEntity(name = "Alipay", subtitle = "Chine (¥)", shortCode = "ALI", balanceAr = 0L, isYuanAccount = true, balanceYuan = 0.0),
                BusinessAccountEntity(name = "Wechat Pay", subtitle = "Chine (¥)", shortCode = "WXP", balanceAr = 0L, isYuanAccount = true, balanceYuan = 0.0)
            )
            defaults.forEach { db.businessAccountDao().insertAccount(it) }
        }
    }

    suspend fun getBusinessAccountsSnapshot(): List<BusinessAccountEntity> {
        return db.businessAccountDao().getAllAccountsSnapshot()
    }

    suspend fun insertBusinessAccount(account: BusinessAccountEntity): Long {
        return db.businessAccountDao().insertAccount(account)
    }

    suspend fun updateBusinessAccount(account: BusinessAccountEntity, oldName: String? = null) {
        db.businessAccountDao().updateAccount(account)
        if (!oldName.isNullOrBlank() && !oldName.equals(account.name, ignoreCase = true)) {
            db.businessTransactionDao().renameAccountInTransactions(oldName, account.name)
            db.businessTransactionDao().renameDestinationInTransactions(oldName, account.name)
            if (account.isYuanAccount) {
                db.yuanTransactionDao().renamePlatformInYuanTransactions(oldName, account.name)
            }
        }
    }

    suspend fun deleteBusinessAccount(accountId: Long) {
        db.businessAccountDao().deleteAccountById(accountId)
    }

    suspend fun adjustYuanAccountBalance(deltaYuan: Double, platformAccountName: String = "") {
        val yuanAccounts = db.businessAccountDao().getAllAccountsSnapshot().filter { it.isYuanAccount && it.isActive }
        if (yuanAccounts.isEmpty()) return

        val cleanTarget = platformAccountName.trim()
        val matched = if (cleanTarget.isNotBlank()) {
            yuanAccounts.firstOrNull { it.name.equals(cleanTarget, ignoreCase = true) }
                ?: yuanAccounts.firstOrNull {
                    it.name.contains(cleanTarget, ignoreCase = true) || cleanTarget.contains(it.name, ignoreCase = true)
                }
        } else null

        val target = matched ?: if (deltaYuan < 0.0) {
            yuanAccounts.maxByOrNull { it.balanceYuan } ?: yuanAccounts.first()
        } else {
            yuanAccounts.first()
        }
        db.businessAccountDao().adjustYuanBalanceByName(target.name, deltaYuan)
    }

    val allBusinessTransactions: Flow<List<BusinessTransactionEntity>> = db.businessTransactionDao().getAllTransactions()

    suspend fun insertBusinessTransaction(tx: BusinessTransactionEntity): Long {
        val id = db.businessTransactionDao().insertTransaction(tx)
        when (tx.type) {
            "IN", "INCOME" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), tx.amountAr)
                }
            }
            "OUT", "EXPENSE", "YUAN" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), -tx.amountAr)
                }
            }
            "TRANSFER" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), -tx.amountAr)
                }
                if (tx.destinationAccount.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.destinationAccount.trim(), tx.amountAr)
                }
            }
        }
        return id
    }

    suspend fun deleteBusinessTransaction(tx: BusinessTransactionEntity) {
        when (tx.type) {
            "IN", "INCOME" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), -tx.amountAr)
                }
            }
            "OUT", "EXPENSE", "YUAN" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), tx.amountAr)
                }
            }
            "TRANSFER" -> {
                if (tx.accountName.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.accountName.trim(), tx.amountAr)
                }
                if (tx.destinationAccount.isNotBlank()) {
                    db.businessAccountDao().adjustBalanceByName(tx.destinationAccount.trim(), -tx.amountAr)
                }
            }
        }
        db.businessTransactionDao().deleteTransactionById(tx.id)
        tx.yuanTransactionId?.let { yuanId ->
            val yuanTx = db.yuanTransactionDao().getAllTransactionsSnapshot().firstOrNull { it.id == yuanId }
            if (yuanTx != null) {
                val deltaYuan = when (yuanTx.type) {
                    "BUY", "REFUND" -> -yuanTx.amountYuan
                    "SPEND" -> yuanTx.amountYuan
                    else -> 0.0
                }
                if (deltaYuan != 0.0) {
                    adjustYuanAccountBalance(deltaYuan, yuanTx.platform)
                }
                db.yuanTransactionDao().deleteById(yuanId)
            }
        }
    }

    val allBusinessInvestments: Flow<List<BusinessInvestmentEntity>> = db.businessInvestmentDao().getAllInvestments()

    suspend fun insertBusinessInvestment(investment: BusinessInvestmentEntity): Long {
        return db.businessInvestmentDao().insertInvestment(investment)
    }

    suspend fun updateBusinessInvestment(investment: BusinessInvestmentEntity) {
        db.businessInvestmentDao().updateInvestment(investment)
    }

    suspend fun deleteBusinessInvestment(investment: BusinessInvestmentEntity) {
        db.businessInvestmentDao().deleteInvestmentById(investment.id)
        investment.linkedTransactionId?.let { txId ->
            val linkedTx = db.businessTransactionDao().getTransactionById(txId)
            if (linkedTx != null) {
                deleteBusinessTransaction(linkedTx)
            }
        }
    }
}
