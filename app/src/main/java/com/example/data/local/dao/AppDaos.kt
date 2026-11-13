package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets")
    suspend fun getAllWalletsSnapshot(): List<WalletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Query("DELETE FROM wallets WHERE isDemo = 1")
    suspend fun deleteDemoWallets()

    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSnapshot(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE isDemo = 1")
    suspend fun deleteDemoTransactions()

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey LIMIT 1")
    fun getBudgetForMonth(monthKey: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSnapshot(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE isDemo = 1")
    suspend fun deleteDemoBudgets()

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE isDemo = 1")
    suspend fun deleteDemoTasks()

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSnapshot(): List<HabitEntity>

    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllLogsSnapshot(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsByHabitId(habitId: Long)

    @Query("DELETE FROM habits WHERE isDemo = 1")
    suspend fun deleteDemoHabits()

    @Query("DELETE FROM habit_logs WHERE isDemo = 1")
    suspend fun deleteDemoLogs()

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY dueDate ASC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans")
    suspend fun getAllLoansSnapshot(): List<LoanEntity>

    @Query("SELECT * FROM loan_payments ORDER BY date ASC")
    fun getAllPayments(): Flow<List<LoanPaymentEntity>>

    @Query("SELECT * FROM loan_payments")
    suspend fun getAllPaymentsSnapshot(): List<LoanPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: LoanPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<LoanPaymentEntity>)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoanById(id: Long)

    @Query("DELETE FROM loans WHERE isDemo = 1")
    suspend fun deleteDemoLoans()

    @Query("DELETE FROM loan_payments WHERE isDemo = 1")
    suspend fun deleteDemoPayments()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM loan_payments")
    suspend fun clearPayments()
}

@Dao
interface BusinessOrderDao {
    @Query("SELECT * FROM business_orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<BusinessOrderEntity>>

    @Query("SELECT * FROM business_orders")
    suspend fun getAllOrdersSnapshot(): List<BusinessOrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: BusinessOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<BusinessOrderEntity>)

    @Update
    suspend fun updateOrder(order: BusinessOrderEntity)

    @Query("DELETE FROM business_orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("DELETE FROM business_orders WHERE isDemo = 1")
    suspend fun deleteDemoOrders()

    @Query("DELETE FROM business_orders")
    suspend fun clearAll()
}

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<HealthEntryEntity>>

    @Query("SELECT * FROM health_entries WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: String): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesBetween(startDate: String, endDate: String): Flow<List<HealthEntryEntity>>

    @Query("SELECT * FROM health_entries WHERE weightKg IS NOT NULL ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries WHERE weightKg IS NOT NULL ORDER BY date ASC LIMIT 1")
    fun getFirstWeightEntry(): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries WHERE waistCm IS NOT NULL ORDER BY date DESC LIMIT 1")
    fun getLatestWaistEntry(): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries WHERE bloodPressure IS NOT NULL ORDER BY date DESC LIMIT 1")
    fun getLatestBloodPressureEntry(): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries WHERE temperatureC IS NOT NULL ORDER BY date DESC LIMIT 1")
    fun getLatestTemperatureEntry(): Flow<HealthEntryEntity?>

    @Query("SELECT * FROM health_entries")
    suspend fun getAllEntriesSnapshot(): List<HealthEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: HealthEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<HealthEntryEntity>)

    @Query("DELETE FROM health_entries WHERE isDemo = 1")
    suspend fun deleteDemoHealthEntries()

    @Query("DELETE FROM health_entries")
    suspend fun clearAll()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY deadline ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals")
    suspend fun getAllGoalsSnapshot(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE isDemo = 1")
    suspend fun deleteDemoGoals()

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}

@Dao
interface YuanTransactionDao {
    @Query("SELECT * FROM yuan_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<YuanTransactionEntity>>

    @Query("SELECT * FROM yuan_transactions")
    suspend fun getAllTransactionsSnapshot(): List<YuanTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(tx: YuanTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(txs: List<YuanTransactionEntity>)

    @Query("DELETE FROM yuan_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE yuan_transactions SET platform = :newName WHERE LOWER(platform) = LOWER(:oldName)")
    suspend fun renamePlatformInYuanTransactions(oldName: String, newName: String)

    @Query("DELETE FROM yuan_transactions WHERE isDemo = 1")
    suspend fun deleteDemoTransactions()

    @Query("DELETE FROM yuan_transactions")
    suspend fun clearAll()
}

@Dao
interface BusinessSaleDao {
    @Query("SELECT * FROM business_sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<BusinessSaleEntity>>

    @Query("SELECT * FROM business_sales")
    suspend fun getAllSalesSnapshot(): List<BusinessSaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: BusinessSaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<BusinessSaleEntity>)

    @Update
    suspend fun updateSale(sale: BusinessSaleEntity)

    @Query("DELETE FROM business_sales WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM business_sales WHERE isDemo = 1")
    suspend fun deleteDemoSales()

    @Query("DELETE FROM business_sales")
    suspend fun clearAll()
}

@Dao
interface CatalogueFamilyDao {
    @Query("SELECT * FROM catalogue_families ORDER BY displayOrder ASC, id ASC")
    fun getAllFamilies(): Flow<List<CatalogueFamilyEntity>>

    @Query("SELECT * FROM catalogue_families")
    suspend fun getAllFamiliesSnapshot(): List<CatalogueFamilyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: CatalogueFamilyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilies(families: List<CatalogueFamilyEntity>)

    @Update
    suspend fun updateFamily(family: CatalogueFamilyEntity)

    @Query("DELETE FROM catalogue_families WHERE id = :id")
    suspend fun deleteFamilyById(id: Long)

    @Query("DELETE FROM catalogue_families WHERE isDemo = 1")
    suspend fun deleteDemoFamilies()

    @Query("DELETE FROM catalogue_families")
    suspend fun clearAll()
}

@Dao
interface CataloguePlatformDao {
    @Query("SELECT * FROM catalogue_platforms ORDER BY displayOrder ASC, id ASC")
    fun getAllPlatforms(): Flow<List<CataloguePlatformEntity>>

    @Query("SELECT * FROM catalogue_platforms")
    suspend fun getAllPlatformsSnapshot(): List<CataloguePlatformEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatform(platform: CataloguePlatformEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatforms(platforms: List<CataloguePlatformEntity>)

    @Update
    suspend fun updatePlatform(platform: CataloguePlatformEntity)

    @Query("UPDATE catalogue_platforms SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    @Query("DELETE FROM catalogue_platforms WHERE id = :id")
    suspend fun deletePlatformById(id: Long)

    @Query("DELETE FROM catalogue_platforms WHERE isDemo = 1")
    suspend fun deleteDemoPlatforms()

    @Query("DELETE FROM catalogue_platforms")
    suspend fun clearAll()
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name COLLATE NOCASE ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients")
    suspend fun getAllClientsSnapshot(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findClientByName(name: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)

    @Query("DELETE FROM clients WHERE isDemo = 1")
    suspend fun deleteDemoClients()

    @Query("DELETE FROM clients")
    suspend fun clearAll()
}

@Dao
interface CatalogueTransitaireDao {
    @Query("SELECT * FROM catalogue_transitaires ORDER BY name COLLATE NOCASE ASC")
    fun getAllTransitaires(): Flow<List<CatalogueTransitaireEntity>>

    @Query("SELECT * FROM catalogue_transitaires")
    suspend fun getAllTransitairesSnapshot(): List<CatalogueTransitaireEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransitaire(transitaire: CatalogueTransitaireEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransitaires(transitaires: List<CatalogueTransitaireEntity>)

    @Update
    suspend fun updateTransitaire(transitaire: CatalogueTransitaireEntity)

    @Query("DELETE FROM catalogue_transitaires WHERE id = :id")
    suspend fun deleteTransitaireById(id: Long)

    @Query("DELETE FROM catalogue_transitaires")
    suspend fun clearAll()
}

@Dao
interface SalePaymentDao {
    @Query("SELECT * FROM sale_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<SalePaymentEntity>>

    @Query("SELECT * FROM sale_payments WHERE saleId = :saleId ORDER BY paymentDate DESC")
    fun getPaymentsForSale(saleId: Long): Flow<List<SalePaymentEntity>>

    @Query("SELECT * FROM sale_payments WHERE saleId = :saleId ORDER BY paymentDate DESC")
    suspend fun getPaymentsForSaleSnapshot(saleId: Long): List<SalePaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SalePaymentEntity): Long

    @Query("DELETE FROM sale_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("DELETE FROM sale_payments WHERE saleId = :saleId")
    suspend fun deletePaymentsForSale(saleId: Long)

    @Query("DELETE FROM sale_payments")
    suspend fun clearAll()
}

@Dao
interface BusinessAccountDao {
    @Query("SELECT * FROM business_accounts WHERE isActive = 1 ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<BusinessAccountEntity>>

    @Query("SELECT * FROM business_accounts WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getAllAccountsSnapshot(): List<BusinessAccountEntity>

    @Query("SELECT COUNT(*) FROM business_accounts")
    suspend fun getAccountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BusinessAccountEntity): Long

    @Update
    suspend fun updateAccount(account: BusinessAccountEntity)

    @Query("UPDATE business_accounts SET balanceAr = balanceAr + :deltaAr WHERE LOWER(name) = LOWER(:accountName)")
    suspend fun adjustBalanceByName(accountName: String, deltaAr: Long)

    @Query("UPDATE business_accounts SET balanceYuan = balanceYuan + :deltaYuan WHERE isYuanAccount = 1")
    suspend fun adjustYuanBalance(deltaYuan: Double)

    @Query("UPDATE business_accounts SET balanceYuan = balanceYuan + :deltaYuan WHERE LOWER(name) = LOWER(:accountName) AND isYuanAccount = 1")
    suspend fun adjustYuanBalanceByName(accountName: String, deltaYuan: Double)

    @Query("DELETE FROM business_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)
}

@Dao
interface BusinessTransactionDao {
    @Query("SELECT * FROM business_transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<BusinessTransactionEntity>>

    @Query("SELECT * FROM business_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): BusinessTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: BusinessTransactionEntity): Long

    @Query("UPDATE business_transactions SET accountName = :newName WHERE LOWER(accountName) = LOWER(:oldName)")
    suspend fun renameAccountInTransactions(oldName: String, newName: String)

    @Query("UPDATE business_transactions SET destinationAccount = :newName WHERE LOWER(destinationAccount) = LOWER(:oldName)")
    suspend fun renameDestinationInTransactions(oldName: String, newName: String)

    @Query("DELETE FROM business_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM business_transactions WHERE yuanTransactionId = :yuanTxId")
    suspend fun deleteByYuanTransactionId(yuanTxId: Long)
}

@Dao
interface BusinessInvestmentDao {
    @Query("SELECT * FROM business_investments ORDER BY dateMillis DESC")
    fun getAllInvestments(): Flow<List<BusinessInvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: BusinessInvestmentEntity): Long

    @Update
    suspend fun updateInvestment(investment: BusinessInvestmentEntity)

    @Query("DELETE FROM business_investments WHERE id = :id")
    suspend fun deleteInvestmentById(id: Long)
}




