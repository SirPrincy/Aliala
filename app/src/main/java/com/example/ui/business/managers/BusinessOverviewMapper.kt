package com.example.ui.business.managers

import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.SalePaymentEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.ui.business.BusinessPipelineStep
import com.example.ui.business.IncomingParcelUi
import com.example.ui.business.TopProductUi
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class OverviewMetricsResult(
    val monthLabel: String,
    val startOfMonthMs: Long,
    val endOfMonthMs: Long,
    val prevMonthShortLabel: String,
    val startOfPrevMonthMs: Long,
    val endOfPrevMonthMs: Long,
    val buyTxs: List<YuanTransactionEntity>,
    val availableYuans: Double,
    val exactCmupRate: Double,
    val cmupRate: Long,
    val estimatedYuanValue: Long,
    val draftOrders: List<BusinessOrderEntity>,
    val insufficientShortage: Double?,
    val paymentsBySaleId: Map<Long, List<SalePaymentEntity>>,
    val salesTotal: Long,
    val salesCost: Long,
    val freightAndExpenses: Long,
    val netProfit: Long,
    val cumulativeBusinessProfitAr: Long,
    val vsPrevMonthPercent: Int,
    val unpaidFreightOrders: List<BusinessOrderEntity>,
    val transitaireDueMap: Map<Long, Long>,
    val freightDueAmount: Long,
    val unpaidSales: List<BusinessSaleEntity>,
    val clientReceivables: Long,
    val pendingClientsCount: Int,
    val pipelineSteps: List<BusinessPipelineStep>,
    val totalPipeline: Int,
    val incomingList: List<IncomingParcelUi>,
    val topProducts: List<TopProductUi>,
    val orderCostLambda: (BusinessOrderEntity) -> Long,
    val totalPurchasesAr: Long,
    val businessPurchasesAr: Long,
    val personalPurchasesAr: Long,
    val familyPurchasesAr: Long,
    val bizPct: Int,
    val persPct: Int,
    val famPct: Int,
    val overdue15d: List<BusinessSaleEntity>,
    val overdueAmount: Long,
    val overdueCount: Int,
    val freightDueDays: Int
)

internal fun mapBusinessOverviewMetrics(
    raw: BusinessRawData,
    monthOffset: Int,
    nowMs: Long
): OverviewMetricsResult {
    val orders = raw.orders
    val yuanTxs = raw.yuanTxs
    val sales = raw.sales

    // 1. Date label & Monthly filtering
    val refDate = LocalDate.now().withDayOfMonth(1).plusMonths(monthOffset.toLong())
    val monthLabel = refDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }

    val zoneId = ZoneId.systemDefault()
    val startOfMonthMs = refDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfMonthMs = refDate.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    val monthSales = sales.filter { it.saleDate in startOfMonthMs..endOfMonthMs }
    val monthOrders = orders.filter { it.orderDate in startOfMonthMs..endOfMonthMs }

    // 2. Portefeuille Yuans (comptes Yuan actifs + CMUP exact)
    val buyTxs = yuanTxs.filter { it.type == "BUY" }
    val refundTxs = yuanTxs.filter { it.type == "REFUND" }
    val spendTxs = yuanTxs.filter { it.type == "SPEND" }
    val totalBoughtYuan = buyTxs.sumOf { it.amountYuan }
    val totalRefundedYuan = refundTxs.sumOf { it.amountYuan }
    val totalSpentYuan = spendTxs.sumOf { it.amountYuan }
    val totalCostAr = buyTxs.sumOf { it.amountAr }

    val activeYuanAccounts = raw.accounts.filter { it.isYuanAccount && it.isActive }
    val txNetYuans = (totalBoughtYuan + totalRefundedYuan - totalSpentYuan).coerceAtLeast(0.0)
    val accountsYuanSum = activeYuanAccounts.sumOf { it.balanceYuan }
    val availableYuans = when {
        activeYuanAccounts.isEmpty() -> 0.0
        accountsYuanSum == 0.0 && txNetYuans > 0.0 -> txNetYuans
        else -> accountsYuanSum.coerceAtLeast(0.0)
    }
    val exactCmupRate = if (totalBoughtYuan > 0.0) totalCostAr.toDouble() / totalBoughtYuan else 0.0
    val cmupRate = kotlin.math.round(exactCmupRate).toLong()
    val estimatedYuanValue = if (exactCmupRate > 0.0) kotlin.math.round(availableYuans * exactCmupRate).toLong() else 0L

    val draftOrders = orders.filter { it.status == "DRAFT" }
    val draftYuansNeeded = draftOrders.sumOf { (it.priceYuan * it.quantityBought) + it.domesticShippingYuan }
    val insufficientShortage = if (draftYuansNeeded > availableYuans) (draftYuansNeeded - availableYuans) else null

    // 3. Ventes & Bénéfice du mois
    val salesById = sales.associateBy { it.id }
    val paymentsBySaleId = raw.salePayments.groupBy { it.saleId }

    fun computeCashInAndCost(startMs: Long, endMs: Long): Pair<Long, Long> {
        val monthPayments = raw.salePayments.filter { it.paymentDate in startMs..endMs }
        val legacySales = sales.filter {
            it.saleDate in startMs..endMs && it.paidAmountAr > 0 && paymentsBySaleId[it.id].isNullOrEmpty()
        }
        val total = monthPayments.sumOf { it.amountAr } + legacySales.sumOf { it.paidAmountAr }
        val cost = monthPayments.sumOf { p ->
            val s = salesById[p.saleId]
            if (s != null && s.totalPriceAr > 0) {
                (s.unitCostAr * s.quantity * (p.amountAr.toDouble() / s.totalPriceAr.toDouble())).toLong()
            } else 0L
        } + legacySales.sumOf {
            (it.unitCostAr * it.quantity * (if (it.totalPriceAr > 0) it.paidAmountAr.toDouble() / it.totalPriceAr.toDouble() else 1.0)).toLong()
        }
        return Pair(total, cost)
    }

    val (salesTotal, salesCost) = computeCashInAndCost(startOfMonthMs, endOfMonthMs)
    val freightAndExpenses = monthOrders.sumOf { it.freightCostAr }
    val netProfit = salesTotal - salesCost - freightAndExpenses

    val (allTimeSalesTotal, allTimeSalesCost) = computeCashInAndCost(0L, Long.MAX_VALUE)
    val allTimeFreightCost = orders.sumOf { it.freightCostAr }
    val cumulativeBusinessProfitAr = allTimeSalesTotal - allTimeSalesCost - allTimeFreightCost

    val prevRefDate = refDate.minusMonths(1)
    val prevMonthShortLabel = prevRefDate.format(DateTimeFormatter.ofPattern("MMM", Locale.FRENCH)).lowercase()
    val startOfPrevMonthMs = prevRefDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfPrevMonthMs = prevRefDate.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
    val prevMonthOrders = orders.filter { it.orderDate in startOfPrevMonthMs..endOfPrevMonthMs }
    val (prevSalesTotal, prevSalesCost) = computeCashInAndCost(startOfPrevMonthMs, endOfPrevMonthMs)
    val prevFreight = prevMonthOrders.sumOf { it.freightCostAr }
    val prevNetProfit = prevSalesTotal - prevSalesCost - prevFreight
    val vsPrevMonthPercent = if (prevNetProfit != 0L) {
        (((netProfit - prevNetProfit).toDouble() / kotlin.math.abs(prevNetProfit.toDouble())) * 100).toInt()
    } else 0

    // 4. Alertes trésorerie
    val unpaidFreightOrders = orders.filter { !it.isFreightPaid && it.freightCostAr > 0L }
    val transitaireDueMap = raw.transitaires.associate { t ->
        val tOrders = orders.filter { it.carrier.contains(t.name, ignoreCase = true) && it.freightCostAr > 0L }
        val unpaidSum = tOrders.filter { !it.isFreightPaid }.sumOf { it.freightCostAr }
        val paidViaTx = raw.businessTransactions
            .filter { it.referenceTag == "TRANSITAIRE_${t.id}" && it.type == "EXPENSE" }
            .sumOf { it.amountAr }
        val paidOrdersFreight = tOrders.filter { it.isFreightPaid }.sumOf { it.freightCostAr }
        val unallocatedSurplus = (paidViaTx - paidOrdersFreight).coerceAtLeast(0L)
        t.id to (unpaidSum - unallocatedSurplus).coerceAtLeast(0L)
    }
    val unmatchedUnpaidFreight = unpaidFreightOrders.filter { ord ->
        raw.transitaires.none { t -> ord.carrier.contains(t.name, ignoreCase = true) }
    }.sumOf { it.freightCostAr }
    val freightDueAmount = transitaireDueMap.values.sum() + unmatchedUnpaidFreight
    val unpaidSales = sales.filter { !it.isFullyPaid }
    val clientReceivables = unpaidSales.sumOf { it.totalPriceAr - it.paidAmountAr }
    val pendingClientsCount = unpaidSales.map { it.clientName }.filter { it.isNotBlank() }.distinct().size

    // 5. Pipeline Chine -> Mada
    val countOrdered = orders.count { it.status == "ORDERED" }
    val countWarehouse = orders.count { it.status == "WAREHOUSE" }
    val countGuangzhou = orders.count { it.status == "GUANGZHOU" }
    val countFlight = orders.count { it.status == "FLIGHT" }
    val countCustoms = orders.count { it.status in listOf("CUSTOMS", "QUALITY_CONTROL") }
    val countControl = orders.count { it.status in listOf("DELIVERY_READY", "IN_STOCK", "COMPLETED") }

    val pipelineSteps = listOf(
        BusinessPipelineStep(1, countOrdered, "Commandé", "1688 / Taobao"),
        BusinessPipelineStep(2, countWarehouse, "Vers entrepôt", "Colis interne"),
        BusinessPipelineStep(3, countGuangzhou, "Guangzhou", "Pesée faite"),
        BusinessPipelineStep(4, countFlight, "Vol / Fret", "En vol"),
        BusinessPipelineStep(5, countCustoms, "Arrivé Mada", "Dédouanement"),
        BusinessPipelineStep(6, countControl, "Contrôle", "Prêt à livrer")
    )
    val totalPipeline = countOrdered + countWarehouse + countGuangzhou + countFlight + countCustoms + countControl

    // 6. Prochains arrivages
    val transitOrders = orders.filter { it.status in listOf("FLIGHT", "CUSTOMS", "QUALITY_CONTROL", "GUANGZHOU") }
    val incomingList = transitOrders.take(3).map { o ->
        IncomingParcelUi(
            id = o.id,
            productName = o.product,
            variants = o.variants.ifBlank { "Quantité : ${o.quantityBought}" },
            carrier = o.carrier.ifBlank { "Non spécifié" },
            delayBadge = when (o.status) {
                "CUSTOMS" -> "En douane"
                "QUALITY_CONTROL" -> "Contrôle qualité"
                "GUANGZHOU" -> "Pesée faite"
                else -> if (o.estimatedArrival <= 0L) "En vol" else {
                    val days = ((o.estimatedArrival - nowMs) / 86400000L).coerceAtLeast(0L)
                    if (days == 0L) "Aujourd'hui" else "Dans ${days}j"
                }
            },
            isCustoms = o.status in listOf("CUSTOMS", "QUALITY_CONTROL"),
            iconCategory = o.imageUrl.ifBlank { "earbuds" }
        )
    }

    // 7. Top Ventes du mois sélectionné
    val topProducts = monthSales
        .groupBy { it.productName.trim() }
        .map { (prodName, salesList) ->
            val soldQty = salesList.sumOf { it.quantity }
            val matchingOrder = orders.firstOrNull { it.product.trim().equals(prodName, ignoreCase = true) }
            Triple(prodName, soldQty, matchingOrder)
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .take(3)
        .mapIndexed { idx, (prodName, soldQty, matchingOrder) ->
            val stock = matchingOrder?.stockCount ?: 0
            val restock = matchingOrder?.restockAlert ?: false
            TopProductUi(
                rank = idx + 1,
                name = prodName,
                unitsSold = soldQty,
                marginPercent = matchingOrder?.marginPercent ?: 0,
                stockCount = stock,
                stockStatusLabel = if (restock) "À racheter" else "$stock en stock",
                isRestockNeeded = restock
            )
        }

    // 8. Répartition des achats
    val actualPurchases = orders.filter { it.status != "CATALOGUE" }
    val orderCostLambda: (BusinessOrderEntity) -> Long = {
        if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought
        else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought +
            (it.domesticShippingYuan * it.exchangeRate).toLong() +
            it.freightCostAr + it.customsTaxAr)
    }
    val totalPurchasesAr = actualPurchases.sumOf(orderCostLambda)
    val businessPurchasesAr = actualPurchases.filter { it.category == "BUSINESS" }.sumOf(orderCostLambda)
    val personalPurchasesAr = actualPurchases.filter { it.category == "PERSONAL" }.sumOf(orderCostLambda)
    val familyPurchasesAr = actualPurchases.filter { it.category == "FAMILY_FRIENDS" }.sumOf(orderCostLambda)

    val totalPurchasesSafe = totalPurchasesAr.coerceAtLeast(1L)
    val bizPct = ((businessPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()
    val persPct = ((personalPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()
    val famPct = ((familyPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()

    // 9. Insight IA Trésorerie & échéances fret
    val overdue15d = unpaidSales.filter { (nowMs - it.saleDate) > 15 * 86400000L }
    val overdueAmount = overdue15d.sumOf { it.totalPriceAr - it.paidAmountAr }
    val overdueCount = overdue15d.size
    val minFreightDueDate = unpaidFreightOrders.mapNotNull { it.freightDueDate }.minOrNull()
    val freightDueDays = if (minFreightDueDate != null) {
        (((minFreightDueDate - nowMs) / 86400000L).toInt()).coerceAtLeast(0)
    } else 0

    return OverviewMetricsResult(
        monthLabel = monthLabel,
        startOfMonthMs = startOfMonthMs,
        endOfMonthMs = endOfMonthMs,
        prevMonthShortLabel = prevMonthShortLabel,
        startOfPrevMonthMs = startOfPrevMonthMs,
        endOfPrevMonthMs = endOfPrevMonthMs,
        buyTxs = buyTxs,
        availableYuans = availableYuans,
        exactCmupRate = exactCmupRate,
        cmupRate = cmupRate,
        estimatedYuanValue = estimatedYuanValue,
        draftOrders = draftOrders,
        insufficientShortage = insufficientShortage,
        paymentsBySaleId = paymentsBySaleId,
        salesTotal = salesTotal,
        salesCost = salesCost,
        freightAndExpenses = freightAndExpenses,
        netProfit = netProfit,
        cumulativeBusinessProfitAr = cumulativeBusinessProfitAr,
        vsPrevMonthPercent = vsPrevMonthPercent,
        unpaidFreightOrders = unpaidFreightOrders,
        transitaireDueMap = transitaireDueMap,
        freightDueAmount = freightDueAmount,
        unpaidSales = unpaidSales,
        clientReceivables = clientReceivables,
        pendingClientsCount = pendingClientsCount,
        pipelineSteps = pipelineSteps,
        totalPipeline = totalPipeline,
        incomingList = incomingList,
        topProducts = topProducts,
        orderCostLambda = orderCostLambda,
        totalPurchasesAr = totalPurchasesAr,
        businessPurchasesAr = businessPurchasesAr,
        personalPurchasesAr = personalPurchasesAr,
        familyPurchasesAr = familyPurchasesAr,
        bizPct = bizPct,
        persPct = persPct,
        famPct = famPct,
        overdue15d = overdue15d,
        overdueAmount = overdueAmount,
        overdueCount = overdueCount,
        freightDueDays = freightDueDays
    )
}
