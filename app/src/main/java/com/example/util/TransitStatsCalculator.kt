package com.example.util

import com.example.data.local.entity.BusinessOrderEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

data class OrderTransitStats(
    val chinaDays: Long? = null,
    val hubWaitDays: Long? = null,
    val transitDays: Long? = null,
    val totalDays: Long? = null
) {
    val hasAnyMeasurement: Boolean
        get() = chinaDays != null || hubWaitDays != null || transitDays != null || totalDays != null
}

data class TransitaireTransitStats(
    val avgChinaDays: Double? = null,
    val avgHubWaitDays: Double? = null,
    val avgAirTransitDays: Double? = null,
    val avgSeaTransitDays: Double? = null,
    val avgTotalDays: Double? = null,
    val measuredShipmentsCount: Int = 0,
    val measuredAirCount: Int = 0,
    val measuredSeaCount: Int = 0
)

object TransitStatsCalculator {

    private fun toLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    fun daysBetween(startMillis: Long?, endMillis: Long?): Long? {
        if (startMillis == null || endMillis == null || startMillis <= 0L || endMillis <= 0L) return null
        val diff = ChronoUnit.DAYS.between(toLocalDate(startMillis), toLocalDate(endMillis))
        return if (diff >= 0L) diff else null
    }

    fun isChronologicallyBefore(candidateMillis: Long?, earlierMilestoneMillis: Long?): Boolean {
        if (candidateMillis == null || earlierMilestoneMillis == null || candidateMillis <= 0L || earlierMilestoneMillis <= 0L) {
            return false
        }
        return toLocalDate(candidateMillis).isBefore(toLocalDate(earlierMilestoneMillis))
    }

    fun calculateForOrder(order: BusinessOrderEntity): OrderTransitStats {
        val chinaDays = daysBetween(order.orderDate, order.warehouseArrivalDate)
        val hubWaitDays = daysBetween(order.warehouseArrivalDate, order.madaDispatchDate)
        val transitDays = daysBetween(order.madaDispatchDate, order.madaArrivalDate)
        val totalDays = daysBetween(order.orderDate, order.madaArrivalDate)
        return OrderTransitStats(
            chinaDays = chinaDays,
            hubWaitDays = hubWaitDays,
            transitDays = transitDays,
            totalDays = totalDays
        )
    }

    fun aggregateForOrders(orders: List<BusinessOrderEntity>): TransitaireTransitStats {
        val chinaList = mutableListOf<Long>()
        val hubList = mutableListOf<Long>()
        val airTransitList = mutableListOf<Long>()
        val seaTransitList = mutableListOf<Long>()
        val totalList = mutableListOf<Long>()
        var measuredCount = 0

        orders.forEach { order ->
            val stats = calculateForOrder(order)
            if (stats.hasAnyMeasurement) {
                measuredCount++
            }
            stats.chinaDays?.let { chinaList.add(it) }
            stats.hubWaitDays?.let { hubList.add(it) }
            stats.totalDays?.let { totalList.add(it) }
            stats.transitDays?.let { trDays ->
                val isSea = order.freightMode.equals("MARITIME", ignoreCase = true) ||
                    order.freightMode.equals("SEA", ignoreCase = true)
                if (isSea) seaTransitList.add(trDays) else airTransitList.add(trDays)
            }
        }

        return TransitaireTransitStats(
            avgChinaDays = chinaList.takeIf { it.isNotEmpty() }?.average(),
            avgHubWaitDays = hubList.takeIf { it.isNotEmpty() }?.average(),
            avgAirTransitDays = airTransitList.takeIf { it.isNotEmpty() }?.average(),
            avgSeaTransitDays = seaTransitList.takeIf { it.isNotEmpty() }?.average(),
            avgTotalDays = totalList.takeIf { it.isNotEmpty() }?.average(),
            measuredShipmentsCount = measuredCount,
            measuredAirCount = airTransitList.size,
            measuredSeaCount = seaTransitList.size
        )
    }

    fun formatDaysAvg(days: Double?): String {
        if (days == null) return "—"
        val roundedOne = (days * 10.0).roundToInt() / 10.0
        return if (roundedOne % 1.0 == 0.0) {
            "${roundedOne.toInt()} j"
        } else {
            "${String.format(Locale.FRANCE, "%.1f", roundedOne)} j"
        }
    }

    fun formatDaysExact(days: Long?): String {
        if (days == null) return "—"
        return if (days <= 1L) "$days jour" else "$days jours"
    }
}
