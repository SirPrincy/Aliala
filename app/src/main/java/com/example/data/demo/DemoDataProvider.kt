package com.example.data.demo

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.HealthEntryEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.util.DateUtils
import java.time.LocalDate

object DemoDataProvider {

    suspend fun insertDemoData(db: AppDatabase) {
        val todayStr = DateUtils.getTodayDateString()
        val now = System.currentTimeMillis()
        val currentMonthKey = DateUtils.getCurrentMonthKey()

        // 1. Wallets
        val wallets = listOf(
            WalletEntity(id = 101, name = "Espèces", iconName = "cash", initialBalance = 320_000L, isDemo = true),
            WalletEntity(id = 102, name = "MVola", iconName = "mvola", initialBalance = 780_000L, isDemo = true),
            WalletEntity(id = 103, name = "Orange M.", iconName = "orange", initialBalance = 150_000L, isDemo = true)
        )
        db.walletDao().insertWallets(wallets)

        // 2. Budget
        db.budgetDao().insertOrUpdate(
            BudgetEntity(monthKey = currentMonthKey, amount = 1_000_000L, isDemo = true)
        )

        // 3. Transactions
        val transactions = listOf(
            TransactionEntity(
                title = "Vente Airpods TWS (x2)",
                walletId = 102,
                date = now - 3600_000L, // today
                amount = 150_000L,
                type = "INCOME",
                category = "Business",
                isDemo = true
            ),
            TransactionEntity(
                title = "Marché Leader Price",
                walletId = 101,
                date = now - 86400_000L, // yesterday
                amount = 48_000L,
                type = "EXPENSE",
                category = "Nourriture",
                isDemo = true
            ),
            TransactionEntity(
                title = "Station Shell Ankorondrano",
                walletId = 103,
                date = now - 2 * 86400_000L,
                amount = 30_000L,
                type = "EXPENSE",
                category = "Transport",
                isDemo = true
            ),
            TransactionEntity(
                title = "Remboursement Faly",
                walletId = 102,
                date = now - 3 * 86400_000L,
                amount = 50_000L,
                type = "INCOME",
                category = "Autres",
                isDemo = true
            ),
            TransactionEntity(
                title = "Forfait Telma Net",
                walletId = 102,
                date = now - 4 * 86400_000L,
                amount = 25_000L,
                type = "EXPENSE",
                category = "Loisirs",
                isDemo = true
            ),
            // Background transactions to achieve 2 100 000 Ar revenue and 850 000 Ar expense
            TransactionEntity(
                title = "Contrat Client Madagascar",
                walletId = 102,
                date = now - 5 * 86400_000L,
                amount = 1_900_000L,
                type = "INCOME",
                category = "Business",
                isDemo = true
            ),
            TransactionEntity(
                title = "Approvisionnement Stock",
                walletId = 102,
                date = now - 6 * 86400_000L,
                amount = 450_000L,
                type = "EXPENSE",
                category = "Business",
                isDemo = true
            ),
            TransactionEntity(
                title = "Supermarché & Épicerie",
                walletId = 101,
                date = now - 7 * 86400_000L,
                amount = 250_000L,
                type = "EXPENSE",
                category = "Nourriture",
                isDemo = true
            ),
            TransactionEntity(
                title = "Sortie restaurant",
                walletId = 101,
                date = now - 8 * 86400_000L,
                amount = 47_000L,
                type = "EXPENSE",
                category = "Loisirs",
                isDemo = true
            )
        )
        db.transactionDao().insertTransactions(transactions)

        // 4. Tasks
        val calendar = java.util.Calendar.getInstance()
        val setTime: (Int, Int) -> Long = { hour, min ->
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
            calendar.set(java.util.Calendar.MINUTE, min)
            calendar.timeInMillis
        }

        val tasks = listOf(
            TaskEntity(
                title = "Finaliser devis transporteur Colis",
                dueDate = setTime(9, 0),
                categoryColorHex = "#EF4444",
                isCompleted = false,
                isDemo = true
            ),
            TaskEntity(
                title = "Rendez-vous BNI Express",
                dueDate = setTime(14, 30),
                categoryColorHex = "#3B82F6",
                isCompleted = false,
                isDemo = true
            ),
            TaskEntity(
                title = "Réviser leçon Kapitel 5 allemand",
                dueDate = setTime(18, 0),
                categoryColorHex = "#06B6D4",
                isCompleted = false,
                isDemo = true
            ),
            TaskEntity(
                title = "Boire 1.5L d'eau ce matin",
                dueDate = setTime(8, 0),
                categoryColorHex = "#06B6D4",
                isCompleted = true,
                isDemo = true
            ),
            // Background tasks to reach 5 completed / 8 total
            TaskEntity(
                title = "Méditation 10 minutes",
                dueDate = setTime(7, 0),
                categoryColorHex = "#22C55E",
                isCompleted = true,
                isDemo = true
            ),
            TaskEntity(
                title = "Vérifier suivi colis",
                dueDate = setTime(10, 0),
                categoryColorHex = "#F97316",
                isCompleted = true,
                isDemo = true
            ),
            TaskEntity(
                title = "Lecture 20 pages",
                dueDate = setTime(21, 0),
                categoryColorHex = "#6366F1",
                isCompleted = true,
                isDemo = true
            ),
            TaskEntity(
                title = "Rangement bureau",
                dueDate = setTime(19, 0),
                categoryColorHex = "#8B5CF6",
                isCompleted = true,
                isDemo = true
            )
        )
        db.taskDao().insertTasks(tasks)

        // 5. Habits
        val h1 = db.habitDao().insertHabit(
            HabitEntity(
                name = "Eau",
                iconName = "water",
                habitType = "COUNTER",
                targetCount = 8,
                unit = "verres",
                moment = "Matin",
                colorHex = "#06B6D4",
                isDemo = true
            )
        )
        val h2 = db.habitDao().insertHabit(
            HabitEntity(
                name = "Sport",
                iconName = "sport",
                habitType = "DURATION",
                targetCount = 30,
                unit = "min",
                moment = "Matin",
                colorHex = "#22C55E",
                isDemo = true
            )
        )
        val h3 = db.habitDao().insertHabit(
            HabitEntity(
                name = "< 23h",
                iconName = "sleep",
                habitType = "BOOLEAN",
                targetCount = 1,
                unit = "fois",
                moment = "Soir",
                colorHex = "#7C3AED",
                isDemo = true
            )
        )
        val h4 = db.habitDao().insertHabit(
            HabitEntity(
                name = "Lecture",
                iconName = "book",
                habitType = "DURATION",
                targetCount = 20,
                unit = "min",
                moment = "Matin",
                colorHex = "#A855F7",
                isDemo = true
            )
        )
        val h5 = db.habitDao().insertHabit(
            HabitEntity(
                name = "Allemand",
                iconName = "translate",
                habitType = "DURATION",
                targetCount = 15,
                unit = "min",
                moment = "Soir",
                colorHex = "#6366F1",
                isDemo = true
            )
        )

        // Add Habit logs for streaks (Eau: 12 days, Sport: 4 days, < 23h: 5 days, Lecture: 3 days, Allemand: 8 days)
        val today = LocalDate.now()
        val habitLogs = mutableListOf<HabitLogEntity>()
        for (i in 0 until 12) {
            habitLogs.add(HabitLogEntity(habitId = h1, date = today.minusDays(i.toLong()).toString(), isDemo = true))
        }
        for (i in 0 until 4) {
            habitLogs.add(HabitLogEntity(habitId = h2, date = today.minusDays(i.toLong()).toString(), isDemo = true))
        }
        for (i in 0 until 5) {
            habitLogs.add(HabitLogEntity(habitId = h3, date = today.minusDays(i.toLong()).toString(), isDemo = true))
        }
        for (i in 0 until 3) {
            habitLogs.add(HabitLogEntity(habitId = h4, date = today.minusDays(i.toLong()).toString(), isDemo = true))
        }
        for (i in 0 until 8) {
            habitLogs.add(HabitLogEntity(habitId = h5, date = today.minusDays(i.toLong()).toString(), isDemo = true))
        }
        // Additional past history for heatmap and ranking
        for (dayOffset in 8..25) {
            val d = today.minusDays(dayOffset.toLong()).toString()
            if (dayOffset % 2 == 0) habitLogs.add(HabitLogEntity(habitId = h1, date = d, isDemo = true))
            if (dayOffset % 3 == 0) habitLogs.add(HabitLogEntity(habitId = h3, date = d, isDemo = true))
            if (dayOffset % 4 == 0) habitLogs.add(HabitLogEntity(habitId = h2, date = d, isDemo = true))
        }
        db.habitDao().insertLogs(habitLogs)

        // 6. Loans
        val loanId = db.loanDao().insertLoan(
            LoanEntity(
                direction = "BORROWED",
                counterpartyType = "BANK",
                name = "Prêt BNI Express",
                amount = 1_000_000L,
                interestPercent = 5.0,
                dueDate = now + 5 * 86400_000L,
                isDemo = true
            )
        )
        db.loanDao().insertPayment(
            LoanPaymentEntity(loanId = loanId, amount = 750_000L, date = now - 10 * 86400_000L, isDemo = true)
        )
        // Additional loan to make "Je dois" = 400 000 Ar (1M - 750k = 250k, + 150k = 400k)
        db.loanDao().insertLoan(
            LoanEntity(
                direction = "BORROWED",
                counterpartyType = "PERSON",
                name = "Avance fournisseur",
                amount = 150_000L,
                interestPercent = null,
                dueDate = now + 15 * 86400_000L,
                isDemo = true
            )
        )
        // Loan given to someone: "On me doit" = 150 000 Ar
        db.loanDao().insertLoan(
            LoanEntity(
                direction = "LENT",
                counterpartyType = "PERSON",
                name = "Prêt ami Rivo",
                amount = 150_000L,
                interestPercent = null,
                dueDate = now + 12 * 86400_000L,
                isDemo = true
            )
        )

        // 7. Business Orders & Logistics Pipeline
        val orders = listOf(
            // 1. Écouteurs TWS Pro 5 (En stock en rayon - 23 pcs, 2 réservés Rina, 1 défectueux)
            BusinessOrderEntity(
                product = "Écouteurs TWS Pro 5",
                supplier = "1688",
                platform = "1688",
                referenceCode = "TWS-P5-PRO",
                quantityBought = 23,
                priceYuan = 44.258,
                exchangeRate = 650.0,
                resalePriceAr = 661_664L, // 28 768 Ar/u
                status = "DELIVERY_READY",
                orderDate = now - 14 * 86400_000L,
                estimatedArrival = now - 2 * 86400_000L,
                category = "BUSINESS",
                family = "Électronique",
                carrier = "SinoExpress",
                assignedClient = "Rina",
                variants = "12 Noir · 8 Blanc · 3 Rouge",
                imageUrl = "earbuds",
                stockCount = 23,
                reservedCount = 2,
                defectiveCount = 1,
                unitsSold = 28,
                marginPercent = 54,
                freightCostAr = 85_000L,
                isFreightPaid = true,
                isDemo = true
            ),

            // 2. Montres Connectées X7 (En stock - 16 pcs, Libre)
            BusinessOrderEntity(
                product = "Montres Connectées X7",
                supplier = "Taobao",
                platform = "Taobao",
                referenceCode = "W-X7-SPORT",
                quantityBought = 16,
                priceYuan = 65.0,
                exchangeRate = 650.0,
                resalePriceAr = 676_000L, // 42 250 Ar/u
                status = "DELIVERY_READY",
                orderDate = now - 12 * 86400_000L,
                estimatedArrival = now - 1 * 86400_000L,
                category = "BUSINESS",
                family = "Électronique",
                carrier = "SinoExpress Hub",
                weightKg = 3.2,
                variants = "8 Noir · 8 Stellaire",
                imageUrl = "smartwatch",
                stockCount = 16,
                reservedCount = 0,
                defectiveCount = 1,
                unitsSold = 15,
                marginPercent = 45,
                freightCostAr = 50_000L,
                isFreightPaid = true,
                isDemo = true
            ),

            // 3. Sacoches Chest Bag (En stock - 12 pcs, Réservé 1)
            BusinessOrderEntity(
                product = "Sacoches Chest Bag",
                supplier = "Pinduoduo",
                platform = "Pinduoduo",
                referenceCode = "BAG-CB-01",
                quantityBought = 12,
                priceYuan = 35.0,
                exchangeRate = 650.0,
                resalePriceAr = 273_000L, // 22 750 Ar/u
                status = "DELIVERY_READY",
                orderDate = now - 10 * 86400_000L,
                estimatedArrival = now - 3 * 86400_000L,
                category = "PERSONAL",
                family = "Mode",
                carrier = "Transitaire MadaAir Tana",
                weightKg = 4.1,
                assignedClient = "",
                variants = "12 Noir",
                imageUrl = "bag",
                stockCount = 12,
                reservedCount = 1,
                defectiveCount = 1,
                unitsSold = 19,
                marginPercent = 49,
                freightCostAr = 45_000L,
                isFreightPaid = true,
                isDemo = true
            ),

            // 4. Gourde Isotherme Inox 1L (Maison - 6 pcs)
            BusinessOrderEntity(
                product = "Gourde Isotherme Inox 1L",
                supplier = "Guangzhou",
                platform = "1688",
                referenceCode = "BOT-INOX-1L",
                quantityBought = 6,
                priceYuan = 25.0,
                exchangeRate = 650.0,
                resalePriceAr = 120_000L,
                status = "DELIVERY_READY",
                orderDate = now - 8 * 86400_000L,
                estimatedArrival = now - 2 * 86400_000L,
                category = "PERSONAL",
                family = "Maison",
                carrier = "Colis interne",
                variants = "6 Noir mat",
                imageUrl = "bottle",
                stockCount = 6,
                reservedCount = 0,
                defectiveCount = 0,
                isDemo = true
            ),

            // 5. Coques iPhone Magnétiques (En stock)
            BusinessOrderEntity(
                product = "Coques iPhone Magnétiques",
                supplier = "1688",
                platform = "1688",
                referenceCode = "CAS-IPH-MAG",
                quantityBought = 5,
                priceYuan = 4.5,
                exchangeRate = 650.0,
                resalePriceAr = 15_000L,
                status = "DELIVERY_READY",
                orderDate = now - 5 * 86400_000L,
                estimatedArrival = now - 1 * 86400_000L,
                category = "BUSINESS",
                family = "Électronique",
                carrier = "1688 / Taobao",
                variants = "3 Noir · 2 Titane",
                imageUrl = "case",
                stockCount = 5,
                unitsSold = 42,
                marginPercent = 52,
                restockAlert = true,
                isDemo = true
            ),

            // 6. En cours d'acheminement 1: Écouteurs TWS Pro 5 (20 pcs vers Mada)
            BusinessOrderEntity(
                product = "Écouteurs TWS Pro 5",
                supplier = "1688",
                platform = "1688",
                referenceCode = "TWS-P5-PRO",
                quantityBought = 20,
                priceYuan = 28.0,
                exchangeRate = 650.0,
                resalePriceAr = 364_000L,
                status = "FLIGHT", // Vers Mada · J+6
                orderDate = now - 7 * 86400_000L,
                estimatedArrival = now + 6 * 86400_000L,
                category = "BUSINESS",
                family = "Électronique",
                carrier = "SinoExpress",
                variants = "10 Noir · 5 Blanc · 5 Rouge",
                imageUrl = "earbuds",
                stockCount = 0,
                freightCostAr = 85_000L,
                isFreightPaid = false,
                freightDueDate = now + 3 * 86400_000L,
                isDemo = true
            ),

            // 7. En cours d'acheminement 2: Câbles Charge Rapide 65W (40 pcs arrivé entrepôt)
            BusinessOrderEntity(
                product = "Câbles Charge Rapide 65W",
                supplier = "1688",
                platform = "1688",
                referenceCode = "CAB-65W-USB",
                quantityBought = 40,
                priceYuan = 8.0,
                exchangeRate = 650.0,
                resalePriceAr = 208_000L,
                status = "WAREHOUSE", // Arrivé entrepôt
                orderDate = now - 3 * 86400_000L,
                estimatedArrival = now + 8 * 86400_000L,
                category = "BUSINESS",
                family = "Électronique",
                carrier = "SinoExpress Hub",
                weightKg = 3.2,
                variants = "25 Noir · 15 Bleu",
                imageUrl = "cable",
                stockCount = 0,
                unitsSold = 36,
                marginPercent = 46,
                isDemo = true
            ),

            // 8, 9, 10: Articles masqués (3 articles archivés/masqués)
            BusinessOrderEntity(
                product = "Ancien Chargeur 18W",
                supplier = "1688",
                platform = "1688",
                referenceCode = "CHG-18W-OLD",
                quantityBought = 2,
                priceYuan = 12.0,
                exchangeRate = 650.0,
                resalePriceAr = 25_000L,
                status = "DELIVERY_READY",
                category = "BUSINESS",
                family = "Électronique",
                variants = "2 Blanc",
                stockCount = 2,
                isHidden = true,
                isDemo = true
            ),
            BusinessOrderEntity(
                product = "T-Shirt Promo Coton",
                supplier = "Taobao",
                platform = "Taobao",
                referenceCode = "TSH-WHT-PRO",
                quantityBought = 5,
                priceYuan = 15.0,
                exchangeRate = 650.0,
                resalePriceAr = 45_000L,
                status = "DELIVERY_READY",
                category = "PERSONAL",
                family = "Mode",
                variants = "5 Blanc",
                stockCount = 5,
                isHidden = true,
                isDemo = true
            ),
            BusinessOrderEntity(
                product = "Câble Audio Jack 3.5mm",
                supplier = "1688",
                platform = "1688",
                referenceCode = "CAB-JACK-01",
                quantityBought = 3,
                priceYuan = 5.0,
                exchangeRate = 650.0,
                resalePriceAr = 10_000L,
                status = "DELIVERY_READY",
                category = "BUSINESS",
                family = "Électronique",
                variants = "3 Noir",
                stockCount = 3,
                isHidden = true,
                isDemo = true
            )
        )
        db.businessOrderDao().insertOrders(orders)

        // 7b. Yuan Transactions (Solde dispo = ¥ 1 840, CMUP = 650 Ar, Gain de change = 12 500 Ar)
        val yuanTxs = listOf(
            YuanTransactionEntity(
                type = "BUY",
                amountYuan = 2000.0,
                exchangeRate = 640.0, // Achat anticipé à 640 Ar/¥ créant le gain de change
                amountAr = 1_280_000L,
                platform = "Alipay",
                date = now - 15 * 86400_000L,
                note = "Recharge Alipay",
                isDemo = true
            ),
            YuanTransactionEntity(
                type = "BUY",
                amountYuan = 1000.0,
                exchangeRate = 670.0,
                amountAr = 670_000L,
                platform = "WeChat Pay",
                date = now - 8 * 86400_000L,
                note = "Recharge WeChat Pay",
                isDemo = true
            ),
            YuanTransactionEntity(
                type = "SPEND",
                amountYuan = 1160.0,
                exchangeRate = 650.0,
                amountAr = 754_000L,
                platform = "1688",
                date = now - 5 * 86400_000L,
                note = "Paiement commandes fournisseurs",
                isDemo = true
            )
        )
        db.yuanTransactionDao().insertTransactions(yuanTxs)

        // 7c. Business Sales & Factures Ventes (Chiffre encaissé = 1 350 000 Ar, Reste = 240 000 Ar, Factures ouvertes = 6, 9 factures au total)
        val sales = listOf(
            // 1. Rina Razafy (#088) · 22 oct. 2026 · Écouteurs TWS Pro 5 ×2 (110 000 Ar, réglé 60 000 Ar, reste 50 000 Ar, Marge +48k, Partielle, Sur commande, retard 18j)
            BusinessSaleEntity(
                productName = "Écouteurs TWS Pro 5 ×2",
                clientName = "Rina Razafy",
                clientPhone = "034 55 666 77",
                quantity = 2,
                unitResalePriceAr = 55_000L,
                unitCostAr = 31_000L, // Marge 110k - 62k = 48k (+48k)
                totalPriceAr = 110_000L,
                paidAmountAr = 60_000L,
                isFullyPaid = false,
                saleDate = now - 10 * 86400_000L,
                dueDate = now - 18 * 86400_000L,
                invoiceNumber = "#088",
                saleSource = "ORDER",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 2. Tahina K. (#087) · 20 oct. 2026 · Montres Connectées X7 ×1 (65 000 Ar, 100% réglé, Marge +22,7k, Réglée, Sur stock)
            BusinessSaleEntity(
                productName = "Montres Connectées X7 ×1",
                clientName = "Tahina K.",
                clientPhone = "032 88 999 00",
                quantity = 1,
                unitResalePriceAr = 65_000L,
                unitCostAr = 42_250L, // Marge +22,7k
                totalPriceAr = 65_000L,
                paidAmountAr = 65_000L,
                isFullyPaid = true,
                saleDate = now - 12 * 86400_000L,
                dueDate = now + 10 * 86400_000L,
                invoiceNumber = "#087",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 3. Miora H. (#086) · 18 oct. 2026 · Coque MagSafe & Verre (45 000 Ar, réglé 0 Ar, reste 45 000 Ar, Marge +14,2k, Impayée, Sur stock)
            BusinessSaleEntity(
                productName = "Coque MagSafe & Verre",
                clientName = "Miora H.",
                clientPhone = "034 11 222 33",
                quantity = 1,
                unitResalePriceAr = 45_000L,
                unitCostAr = 30_800L, // Marge +14,2k
                totalPriceAr = 45_000L,
                paidAmountAr = 0L,
                isFullyPaid = false,
                saleDate = now - 14 * 86400_000L,
                dueDate = now - 5 * 86400_000L,
                invoiceNumber = "#086",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 4. Faly Rasolofo (#085) · Partielle (80 000 Ar, réglé 40 000 Ar, reste 40 000 Ar)
            BusinessSaleEntity(
                productName = "Sacoche Chest Bag",
                clientName = "Faly Rasolofo",
                clientPhone = "033 44 555 66",
                quantity = 1,
                unitResalePriceAr = 80_000L,
                unitCostAr = 45_000L,
                totalPriceAr = 80_000L,
                paidAmountAr = 40_000L,
                isFullyPaid = false,
                saleDate = now - 16 * 86400_000L,
                dueDate = now - 2 * 86400_000L,
                invoiceNumber = "#085",
                saleSource = "ORDER",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 5. Sarah Andry (#084) · Partielle (80 000 Ar, réglé 50 000 Ar, reste 30 000 Ar)
            BusinessSaleEntity(
                productName = "Supports Tél. Aluminium ×2",
                clientName = "Sarah Andry",
                clientPhone = "033 77 888 99",
                quantity = 2,
                unitResalePriceAr = 40_000L,
                unitCostAr = 25_000L,
                totalPriceAr = 80_000L,
                paidAmountAr = 50_000L,
                isFullyPaid = false,
                saleDate = now - 18 * 86400_000L,
                dueDate = now + 4 * 86400_000L,
                invoiceNumber = "#084",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 6. Bako Nirina (#083) · Partielle (75 000 Ar, réglé 40 000 Ar, reste 35 000 Ar)
            BusinessSaleEntity(
                productName = "Câbles Charge Rapide ×3",
                clientName = "Bako Nirina",
                clientPhone = "038 99 000 11",
                quantity = 3,
                unitResalePriceAr = 25_000L,
                unitCostAr = 15_000L,
                totalPriceAr = 75_000L,
                paidAmountAr = 40_000L,
                isFullyPaid = false,
                saleDate = now - 20 * 86400_000L,
                dueDate = now + 6 * 86400_000L,
                invoiceNumber = "#083",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 7. Jean Rakoto (#082) · Réglée (50 000 Ar, 100% réglé)
            BusinessSaleEntity(
                productName = "Écouteurs Bluetooth Simple",
                clientName = "Jean Rakoto",
                clientPhone = "034 22 333 44",
                quantity = 1,
                unitResalePriceAr = 50_000L,
                unitCostAr = 28_000L,
                totalPriceAr = 50_000L,
                paidAmountAr = 50_000L,
                isFullyPaid = true,
                saleDate = now - 22 * 86400_000L,
                dueDate = now,
                invoiceNumber = "#082",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 8. Solofo M. (#080) · Impayée (40 000 Ar, réglé 0 Ar, reste 40 000 Ar)
            BusinessSaleEntity(
                productName = "Coque Silicone Antichoc ×2",
                clientName = "Solofo M.",
                clientPhone = "032 11 444 77",
                quantity = 2,
                unitResalePriceAr = 20_000L,
                unitCostAr = 12_000L,
                totalPriceAr = 40_000L,
                paidAmountAr = 0L,
                isFullyPaid = false,
                saleDate = now - 24 * 86400_000L,
                dueDate = now - 4 * 86400_000L,
                invoiceNumber = "#080",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 9. Ventes Comptant Boutique (#081) · Réglée (1 045 000 Ar encaissés comptant)
            BusinessSaleEntity(
                productName = "Lots Accessoires & Coques",
                clientName = "Boutique Tana Cash",
                clientPhone = "",
                quantity = 20,
                unitResalePriceAr = 52_250L,
                unitCostAr = 35_000L,
                totalPriceAr = 1_045_000L,
                paidAmountAr = 1_045_000L,
                isFullyPaid = true,
                saleDate = now - 5 * 86400_000L,
                dueDate = now,
                invoiceNumber = "#081",
                saleSource = "STOCK",
                domain = "BUSINESS",
                isDemo = true
            ),

            // 10. Famille & Amis (Isolée du CA fiscal)
            BusinessSaleEntity(
                productName = "Montre Sport Pro",
                clientName = "Cousin Hery",
                clientPhone = "034 99 888 77",
                quantity = 1,
                unitResalePriceAr = 65_000L,
                unitCostAr = 65_000L,
                totalPriceAr = 65_000L,
                paidAmountAr = 65_000L,
                isFullyPaid = true,
                saleDate = now - 7 * 86400_000L,
                dueDate = now,
                invoiceNumber = "#F01",
                saleSource = "ORDER",
                domain = "FAMILY_FRIENDS",
                isDemo = true
            )
        )
        db.businessSaleDao().insertSales(sales)

        // 8. Health Entries
        val healthEntries = mutableListOf<HealthEntryEntity>()
        val pastSleep = listOf(7.2, 7.8, 8.0, 7.5, 7.3, 8.2)
        val demoWeights = listOf(68.0, 68.3, 68.7, 69.4, 70.0, 70.8, 71.5)
        val demoMoods = listOf(0, 1, 1, 2, 0, 1, 2, 1, 1, 3, 2, 1, 1, 1, 0, 1, 2, 1, 1, 0, 1, 2, 1, 1, 0, 1, 2, 1, 1, 0)
        
        // Today
        healthEntries.add(
            HealthEntryEntity(
                date = todayStr,
                waterGlasses = 6,
                sleepHours = 7.5,
                weightKg = 68.0,
                steps = 4200,
                mood = 0,
                waistCm = 82.0,
                bloodPressure = "12 / 8",
                temperatureC = 36.6,
                moodNote = "Belle énergie après le sport ce matin",
                workoutMinutes = 30,
                distanceKm = 3.1,
                caloriesKcal = 280,
                sleepBedTime = "22h45",
                sleepWakeTime = "06h15",
                isDemo = true
            )
        )
        // Past 29 days
        for (i in 1..29) {
            val d = today.minusDays(i.toLong()).toString()
            val sleep = if (i <= 6) pastSleep[i - 1] else 7.0 + (i % 3) * 0.4
            val wt = if (i < demoWeights.size) demoWeights[i] else 71.5
            val wGlasses = if (i % 3 == 0) 6 else 8
            val st = 4500 + (i * 350) % 4000
            val md = demoMoods[i % demoMoods.size]
            val hasWaist = if (i == 5) 82.5 else null
            val hasBp = if (i == 1) "12 / 8" else null
            val hasTemp = if (i == 2) 36.7 else null
            healthEntries.add(
                HealthEntryEntity(
                    date = d,
                    waterGlasses = wGlasses,
                    sleepHours = sleep,
                    weightKg = wt,
                    steps = st,
                    mood = md,
                    waistCm = hasWaist,
                    bloodPressure = hasBp,
                    temperatureC = hasTemp,
                    workoutMinutes = if (i % 2 == 0) 30 else 0,
                    distanceKm = if (i % 2 == 0) 3.2 else 1.5,
                    caloriesKcal = if (i % 2 == 0) 290 else 120,
                    sleepBedTime = "23h00",
                    sleepWakeTime = "06h30",
                    isDemo = true
                )
            )
        }
        db.healthDao().insertEntries(healthEntries)

        // 9. Goals
        val goals = listOf(
            GoalEntity(
                name = "Mariage 2027",
                targetAmount = 20_000_000L,
                savedAmount = 7_000_000L,
                deadline = now + 820 * 86400_000L,
                category = "Épargne Projet",
                isDemo = true
            ),
            GoalEntity(
                name = "Nouveau tél Pro",
                targetAmount = 1_500_000L,
                savedAmount = 900_000L,
                deadline = now + 45 * 86400_000L,
                category = "Matériel Pro",
                isDemo = true
            )
        )
        db.goalDao().insertGoals(goals)
    }

    suspend fun clearDemoData(db: AppDatabase) {
        db.walletDao().deleteDemoWallets()
        db.transactionDao().deleteDemoTransactions()
        db.budgetDao().deleteDemoBudgets()
        db.taskDao().deleteDemoTasks()
        db.habitDao().deleteDemoHabits()
        db.habitDao().deleteDemoLogs()
        db.loanDao().deleteDemoLoans()
        db.loanDao().deleteDemoPayments()
        db.businessOrderDao().deleteDemoOrders()
        db.yuanTransactionDao().deleteDemoTransactions()
        db.businessSaleDao().deleteDemoSales()
        db.catalogueFamilyDao().deleteDemoFamilies()
        db.cataloguePlatformDao().deleteDemoPlatforms()
        db.healthDao().deleteDemoHealthEntries()
        db.goalDao().deleteDemoGoals()
    }
}
