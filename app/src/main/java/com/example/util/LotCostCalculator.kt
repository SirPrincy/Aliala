package com.example.util

import com.example.data.local.entity.BusinessOrderEntity

data class LotCostBreakdown(
    val quantity: Int,
    val effectiveCmupRate: Double,
    val unitPriceYuan: Double,
    val merchandiseTotalAr: Long,
    val domesticPortAr: Long,
    val freightRate: Long,
    val freightCostAr: Long,
    val customsTaxAr: Long,
    val refundYuan: Double,
    val refundAr: Long,
    val totalDisbursedAr: Long,
    val netCostAr: Long,
    val unitCostAr: Long,
    val isMaritime: Boolean,
    val isOrderDataComplete: Boolean
)

object LotCostCalculator {
    fun calculate(
        order: BusinessOrderEntity,
        cmupRate: Long,
        customRefundYuan: Double? = null,
        customsTaxArInput: Long? = null,
        transitaireAirRateAr: Long? = null,
        transitaireSeaRateAr: Long? = null
    ): LotCostBreakdown {
        val totalQty = if (order.quantityBought > 0) order.quantityBought else 0
        val effectiveRate = if (order.exchangeRate > 0.0) order.exchangeRate else if (cmupRate > 0) cmupRate.toDouble() else 0.0
        val unitPriceYuan = if (order.priceYuan > 0.0) order.priceYuan else 0.0
        val isOrderDataComplete = totalQty > 0 && unitPriceYuan > 0.0 && effectiveRate > 0.0

        val merchandiseTotalAr = if (isOrderDataComplete) (totalQty * unitPriceYuan * effectiveRate).toLong() else 0L
        val domesticPortAr = if (effectiveRate > 0.0) (Calculations.DOMESTIC_PORT_YUAN * effectiveRate).toLong() else 0L

        val isMaritime = order.freightMode.equals("MARITIME", ignoreCase = true) || order.freightMode.equals("SEA", ignoreCase = true)

        val airRate = transitaireAirRateAr ?: when {
            order.carrier.contains("MadaAir", ignoreCase = true) -> 36_500L
            order.carrier.contains("QuickMada", ignoreCase = true) -> 42_000L
            order.carrier.contains("Oceanic", ignoreCase = true) -> 39_000L
            else -> 38_000L
        }
        val seaRate = transitaireSeaRateAr ?: when {
            order.carrier.contains("MadaAir", ignoreCase = true) -> 1_280_000L
            order.carrier.contains("QuickMada", ignoreCase = true) -> 1_150_000L
            order.carrier.contains("Oceanic", ignoreCase = true) -> 1_100_000L
            else -> 1_200_000L
        }

        val freightRate = if (isMaritime) seaRate else airRate

        val calculatedFreightCostAr = if (order.freightCostAr > 0) {
            order.freightCostAr
        } else if (isMaritime && order.volumeM3 != null && order.volumeM3 > 0.0) {
            (order.volumeM3 * seaRate).toLong()
        } else if (!isMaritime && order.weightKg != null && order.weightKg > 0.0) {
            (order.weightKg * airRate).toLong()
        } else {
            0L
        }

        val customsTaxAr = customsTaxArInput ?: order.customsTaxAr
        val refundYuan = customRefundYuan ?: order.refundYuan
        val refundAr = if (effectiveRate > 0.0) (refundYuan * effectiveRate).toLong() else 0L

        val totalDisbursed = merchandiseTotalAr + domesticPortAr + calculatedFreightCostAr + customsTaxAr
        val netCost = (totalDisbursed - refundAr).coerceAtLeast(0L)
        val unitCost = if (totalQty > 0) netCost / totalQty else 0L

        return LotCostBreakdown(
            quantity = totalQty,
            effectiveCmupRate = effectiveRate,
            unitPriceYuan = unitPriceYuan,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            freightRate = freightRate,
            freightCostAr = calculatedFreightCostAr,
            customsTaxAr = customsTaxAr,
            refundYuan = refundYuan,
            refundAr = refundAr,
            totalDisbursedAr = totalDisbursed,
            netCostAr = netCost,
            unitCostAr = unitCost,
            isMaritime = isMaritime,
            isOrderDataComplete = isOrderDataComplete
        )
    }
}
