package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.BusinessAccountDao
import com.example.data.local.dao.BusinessInvestmentDao
import com.example.data.local.dao.BusinessOrderDao
import com.example.data.local.dao.BusinessSaleDao
import com.example.data.local.dao.BusinessTransactionDao
import com.example.data.local.dao.CatalogueFamilyDao
import com.example.data.local.dao.CataloguePlatformDao
import com.example.data.local.dao.CatalogueTransitaireDao
import com.example.data.local.dao.ClientDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HealthDao
import com.example.data.local.dao.LoanDao
import com.example.data.local.dao.SalePaymentDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.dao.YuanTransactionDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        BusinessOrderEntity::class,
        HealthEntryEntity::class,
        GoalEntity::class,
        YuanTransactionEntity::class,
        BusinessSaleEntity::class,
        CatalogueFamilyEntity::class,
        CataloguePlatformEntity::class,
        ClientEntity::class,
        CatalogueTransitaireEntity::class,
        SalePaymentEntity::class,
        BusinessAccountEntity::class,
        BusinessTransactionEntity::class,
        BusinessInvestmentEntity::class
    ],
    version = 24,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun loanDao(): LoanDao
    abstract fun businessOrderDao(): BusinessOrderDao
    abstract fun healthDao(): HealthDao
    abstract fun goalDao(): GoalDao
    abstract fun yuanTransactionDao(): YuanTransactionDao
    abstract fun businessSaleDao(): BusinessSaleDao
    abstract fun catalogueFamilyDao(): CatalogueFamilyDao
    abstract fun cataloguePlatformDao(): CataloguePlatformDao
    abstract fun clientDao(): ClientDao
    abstract fun catalogueTransitaireDao(): CatalogueTransitaireDao
    abstract fun salePaymentDao(): SalePaymentDao
    abstract fun businessAccountDao(): BusinessAccountDao
    abstract fun businessTransactionDao(): BusinessTransactionDao
    abstract fun businessInvestmentDao(): BusinessInvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `business_accounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `shortCode` TEXT NOT NULL,
                        `balanceAr` INTEGER NOT NULL,
                        `isYuanAccount` INTEGER NOT NULL,
                        `balanceYuan` REAL NOT NULL,
                        `isActive` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `business_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `amountAr` INTEGER NOT NULL,
                        `accountName` TEXT NOT NULL,
                        `referenceTag` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `airNormalRateAr` INTEGER NOT NULL DEFAULT 45000")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `airFastRateAr` INTEGER NOT NULL DEFAULT 60000")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `seaRateAr` INTEGER NOT NULL DEFAULT 18000")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `airNormalLeadDays` TEXT NOT NULL DEFAULT '7 à 10 jours'")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `airFastLeadDays` TEXT NOT NULL DEFAULT '3 à 5 jours'")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `seaNormalLeadDays` TEXT NOT NULL DEFAULT '30 à 45 jours'")
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `goodsCategoryDelays` TEXT NOT NULL DEFAULT 'Générale: Normal | Batterie: +2j | Poudre/Liquide: +3j | Téléphone: +2j'")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `catalogue_transitaires` ADD COLUMN `rateApplicationDate` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `business_orders` ADD COLUMN `supplierDispatchDelay` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `business_transactions` ADD COLUMN `destinationAccount` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `business_transactions` ADD COLUMN `yuanTransactionId` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `business_orders` ADD COLUMN `parcelDetailsJson` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `business_investments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `amountAr` INTEGER NOT NULL,
                        `estimatedValueAr` INTEGER NOT NULL,
                        `generatedReturnAr` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `accountName` TEXT NOT NULL,
                        `linkedTransactionId` INTEGER DEFAULT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ma_vie_database.db"
                ).addMigrations(MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default wallets upon first launch (clean zero balance)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.walletDao().insertWallets(
                                listOf(
                                    WalletEntity(id = 1, name = "Espèces", iconName = "cash", initialBalance = 0L, isDemo = false),
                                    WalletEntity(id = 2, name = "MVola", iconName = "mvola", initialBalance = 0L, isDemo = false),
                                    WalletEntity(id = 3, name = "Orange Money", iconName = "orange", initialBalance = 0L, isDemo = false),
                                    WalletEntity(id = 4, name = "Banque", iconName = "bank", initialBalance = 0L, isDemo = false)
                                )
                            )
                            database.catalogueFamilyDao().insertFamilies(
                                listOf(
                                    CatalogueFamilyEntity(name = "Électronique & Audio", emoji = "🎧", colorHex = "#06B6D4", displayOrder = 1),
                                    CatalogueFamilyEntity(name = "Mode & Maroquinerie", emoji = "🎒", colorHex = "#EC4899", displayOrder = 2),
                                    CatalogueFamilyEntity(name = "Maison & Rangement", emoji = "🏠", colorHex = "#F97316", displayOrder = 3),
                                    CatalogueFamilyEntity(name = "Accessoires Mobiles", emoji = "📱", colorHex = "#8B5CF6", displayOrder = 4)
                                )
                            )
                            database.cataloguePlatformDao().insertPlatforms(
                                listOf(
                                    CataloguePlatformEntity(name = "1688.com", aliasKey = "1688", subtitle = "Alibaba Wholesale (Usines B2B)", isVerified = true, iconType = "TEXT_1688", colorHex = "#712ae2", shippingTag = "Port Payant (¥ 10 - ¥ 25)", shippingTagIcon = "local_shipping", secondaryTag = "Min. commande : 2-5 pcs", isActive = true, displayOrder = 1, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Tarif transitaire aérien: 45 000 Ar/kg. Maritime: 18 000 Ar/kg."),
                                    CataloguePlatformEntity(name = "Taobao", aliasKey = "Taobao", subtitle = "Consommation & Petits lots", badgeText = "C2C / B2C", isVerified = false, iconType = "CART", colorHex = "#8a4cfc", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Livraison rapide état côtier", isActive = true, displayOrder = 2, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Livraison rapide Guangdong en 48h."),
                                    CataloguePlatformEntity(name = "Pinduoduo", aliasKey = "Pinduoduo", subtitle = "Direct Usine / Déstockage & Volume", badgeText = "Usine Direct", isVerified = false, iconType = "PRICE", colorHex = "#ffdad6", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Remises groupées", isActive = true, displayOrder = 3, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Commandes groupées direct usines."),
                                    CataloguePlatformEntity(name = "Fournisseur Direct WeChat", aliasKey = "WeChat", subtitle = "Sino-Contact / Négociations privées", isVerified = false, iconType = "WECHAT", colorHex = "#acedff", shippingTag = "Port Payant (Sur devis)", shippingTagIcon = "request_quote", secondaryTag = "Dépôt direct transitaire", isActive = true, displayOrder = 4, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Paiement direct WeChat Pay ou virement agent.")
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
