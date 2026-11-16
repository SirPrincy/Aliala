package com.example.ui.business.finances

import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CashflowForecastItem(
    val title: String,
    val subtitle: String,
    val amountAr: Long,
    val isInflow: Boolean,
    val dateMillis: Long?,
    val isOverdue: Boolean
)

data class CashflowForecastBucket(
    val index: Int,
    val label: String,
    val endMs: Long,
    val inflowAr: Long,
    val outflowAr: Long,
    val projectedBalanceAr: Long
)

data class CashflowForecastSummary(
    val horizonDays: Int,
    val totalInflowAr: Long,
    val totalOutflowAr: Long,
    val projectedEndBalanceAr: Long,
    val minBucketIndex: Int,
    val hasTension: Boolean,
    val buckets: List<CashflowForecastBucket>,
    val itemsInHorizon: List<CashflowForecastItem>,
    val unscheduledDebts: List<CashflowForecastItem>
)

fun buildCashflowForecast(
    horizonLabel: String,
    startingTreasuryAr: Long,
    unpaidSales: List<BusinessSaleEntity>,
    unpaidFreightOrders: List<BusinessOrderEntity>,
    debts: List<BusinessDebtEntity>
): CashflowForecastSummary {
    val dayMs = 86_400_000L
    val nowMs = System.currentTimeMillis()
    val horizonDays = when (horizonLabel) {
        "60 j" -> 60
        "90 j" -> 90
        else -> 30
    }
    val horizonEndMs = nowMs + horizonDays * dayMs
    val bucketStepMs = (horizonDays * dayMs) / 4L
    val labelFmt = SimpleDateFormat("dd/MM", Locale.FRANCE)

    val allScheduled = mutableListOf<CashflowForecastItem>()
    val unscheduled = mutableListOf<CashflowForecastItem>()

    unpaidSales.forEach { sale ->
        val rem = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
        if (rem > 0L && sale.dueDate <= horizonEndMs) {
            allScheduled += CashflowForecastItem(
                title = "Créance : ${sale.clientName.ifBlank { "Client" }}",
                subtitle = sale.productName.ifBlank { sale.invoiceNumber.ifBlank { "Vente" } },
                amountAr = rem,
                isInflow = true,
                dateMillis = sale.dueDate,
                isOverdue = sale.dueDate < nowMs
            )
        }
    }

    unpaidFreightOrders.forEach { order ->
        val dueMs = order.freightDueDate ?: order.estimatedArrival
        if (order.freightCostAr > 0L && dueMs <= horizonEndMs) {
            allScheduled += CashflowForecastItem(
                title = "Fret ${order.carrier.ifBlank { "Transitaire" }}",
                subtitle = order.lotNumber.ifBlank { order.referenceCode.ifBlank { order.product.ifBlank { "Lot #${order.id}" } } },
                amountAr = order.freightCostAr,
                isInflow = false,
                dateMillis = dueMs,
                isOverdue = dueMs < nowMs
            )
        }
    }

    debts.forEach { debt ->
        val rem = debt.remainingAr()
        if (rem > 0L) {
            val isInflow = debt.direction == "LENT"
            val item = CashflowForecastItem(
                title = (if (isInflow) "Prêt à recouvrer : " else "Dette à rembourser : ") + debt.title,
                subtitle = debt.counterparty.ifBlank { debt.repaymentFrequency },
                amountAr = rem,
                isInflow = isInflow,
                dateMillis = debt.dueDateMillis,
                isOverdue = debt.dueDateMillis != null && debt.dueDateMillis < nowMs
            )
            if (debt.dueDateMillis == null) {
                unscheduled += item
            } else if (debt.dueDateMillis <= horizonEndMs) {
                allScheduled += item
            }
        }
    }

    val inflowBuckets = LongArray(4)
    val outflowBuckets = LongArray(4)

    allScheduled.forEach { item ->
        val dt = item.dateMillis ?: nowMs
        val bIdx = if (dt <= nowMs) {
            0
        } else {
            ((dt - nowMs) / bucketStepMs).toInt().coerceIn(0, 3)
        }
        if (item.isInflow) {
            inflowBuckets[bIdx] += item.amountAr
        } else {
            outflowBuckets[bIdx] += item.amountAr
        }
    }

    var runningBalance = startingTreasuryAr
    val buckets = (0..3).map { idx ->
        val endMs = nowMs + ((idx + 1) * bucketStepMs)
        runningBalance += inflowBuckets[idx] - outflowBuckets[idx]
        CashflowForecastBucket(
            index = idx,
            label = labelFmt.format(Date(endMs)),
            endMs = endMs,
            inflowAr = inflowBuckets[idx],
            outflowAr = outflowBuckets[idx],
            projectedBalanceAr = runningBalance
        )
    }

    val minBucket = buckets.minByOrNull { it.projectedBalanceAr }
    val minIdx = minBucket?.index ?: 0
    val hasTension = minBucket != null && (
        minBucket.projectedBalanceAr < 0L ||
            (startingTreasuryAr > 0L && minBucket.projectedBalanceAr < (startingTreasuryAr * 0.25f).toLong()) ||
            (minBucket.outflowAr > minBucket.inflowAr && minBucket.outflowAr > 0L)
        )

    return CashflowForecastSummary(
        horizonDays = horizonDays,
        totalInflowAr = allScheduled.filter { it.isInflow }.sumOf { it.amountAr },
        totalOutflowAr = allScheduled.filter { !it.isInflow }.sumOf { it.amountAr },
        projectedEndBalanceAr = buckets.lastOrNull()?.projectedBalanceAr ?: startingTreasuryAr,
        minBucketIndex = minIdx,
        hasTension = hasTension,
        buckets = buckets,
        itemsInHorizon = allScheduled.sortedBy { it.dateMillis ?: Long.MAX_VALUE },
        unscheduledDebts = unscheduled
    )
}
