package com.example.util

import com.example.data.local.entity.BusinessOrderEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class BordereauGroupUi(
    val code: String,
    val isMaritime: Boolean,
    val carrier: String,
    val departureMillis: Long?,
    val estimatedArrivalMillis: Long?,
    val orders: List<BusinessOrderEntity>,
    val totalWeightKg: Double,
    val totalVolumeM3: Double,
    val totalQuantity: Int,
    val statusLabel: String,
    val isInTransit: Boolean
)

object BordereauUtils {

    private val dayMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMM")

    private val activeOrPastDispatchStatuses = setOf(
        "FLIGHT",
        "CUSTOMS",
        "QUALITY_CONTROL",
        "DELIVERY_READY",
        "IN_STOCK",
        "COMPLETED",
        "SOLD"
    )

    fun isMaritime(freightMode: String): Boolean {
        return freightMode.equals("MARITIME", ignoreCase = true) ||
            freightMode.equals("SEA", ignoreCase = true)
    }

    fun prefix(order: BusinessOrderEntity): String {
        return if (isMaritime(order.freightMode)) "TC" else "AR"
    }

    fun departureMillis(order: BusinessOrderEntity): Long? {
        return (order.madaDispatchDate ?: order.warehouseArrivalDate ?: order.orderDate)
            .takeIf { it > 0L }
    }

    fun hasBordereau(order: BusinessOrderEntity): Boolean {
        return order.madaDispatchDate != null || order.status in activeOrPastDispatchStatuses
    }

    fun formatCode(order: BusinessOrderEntity): String {
        val pref = prefix(order)
        val depMillis = departureMillis(order)
        return if (depMillis != null) {
            val dayMonth = Instant.ofEpochMilli(depMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(dayMonthFormatter)
            "$pref-$dayMonth"
        } else {
            "$pref-LOT-${order.id}"
        }
    }

    fun resolveCodeOrNull(order: BusinessOrderEntity): String? {
        return if (hasBordereau(order)) formatCode(order) else null
    }

    fun groupOrders(orders: List<BusinessOrderEntity>): List<BordereauGroupUi> {
        val dispatched = orders.filter { hasBordereau(it) }
        return dispatched
            .groupBy { formatCode(it) }
            .map { (code, groupOrders) ->
                val first = groupOrders.first()
                val maritime = isMaritime(first.freightMode)
                val departure = groupOrders.mapNotNull { departureMillis(it) }.minOrNull()
                val eta = groupOrders.map { it.estimatedArrival }.filter { it > 0L }.maxOrNull()
                val carriers = groupOrders.map { it.carrier.trim() }.filter { it.isNotBlank() }.distinct()
                val carrierLabel = when {
                    carriers.isEmpty() -> "Transitaire non défini"
                    carriers.size == 1 -> carriers.first()
                    else -> carriers.joinToString(" / ")
                }
                val statuses = groupOrders.map { it.status }.toSet()
                val statusLabel = when {
                    statuses.any { it == "FLIGHT" } -> "En transit"
                    statuses.any { it == "CUSTOMS" } -> "Dédouanement"
                    statuses.any { it == "QUALITY_CONTROL" } -> "Contrôle qualité"
                    statuses.any { it in setOf("DELIVERY_READY", "IN_STOCK") } -> "Arrivé / En stock"
                    else -> "Terminé"
                }
                val inTransit = statuses.any { it in setOf("FLIGHT", "CUSTOMS", "QUALITY_CONTROL") }

                BordereauGroupUi(
                    code = code,
                    isMaritime = maritime,
                    carrier = carrierLabel,
                    departureMillis = departure,
                    estimatedArrivalMillis = eta,
                    orders = groupOrders.sortedByDescending { it.orderDate },
                    totalWeightKg = groupOrders.sumOf { it.weightKg ?: 0.0 },
                    totalVolumeM3 = groupOrders.sumOf { it.volumeM3 ?: 0.0 },
                    totalQuantity = groupOrders.sumOf { it.quantityBought },
                    statusLabel = statusLabel,
                    isInTransit = inTransit
                )
            }
            .sortedWith(
                compareByDescending<BordereauGroupUi> { it.isInTransit }
                    .thenByDescending { it.departureMillis ?: 0L }
                    .thenBy { it.code }
            )
    }
}
