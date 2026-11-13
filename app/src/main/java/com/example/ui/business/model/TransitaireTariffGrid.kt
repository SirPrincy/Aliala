package com.example.ui.business.model

import java.util.UUID

data class TariffItem(
    val id: String = UUID.randomUUID().toString(),
    val mode: String = "AÉRIEN", // "AÉRIEN" or "MARITIME"
    val shipmentType: String,    // e.g. "Standard Cargo", "Express Direct", "Groupage LCL"
    val goodsCategory: String,   // e.g. "Marchandise Générale", "Électronique & Batterie"
    val priceAr: Long,           // Price in Ariary (per kg for Aérien, per m³ for Maritime)
    val leadDays: String = "",   // e.g. "7 à 10 jours"
    val conditionOp: String = "NONE", // "NONE", "LT", "LTE", "GT", "GTE"
    val conditionValue: Double? = null, // Threshold in CBM (Maritime) or kg (Aérien)
    val usdPrice: Double? = null // Optional USD price reference (e.g. 380.0 $)
)

object TariffGridParser {
    fun parse(
        raw: String,
        airRateAr: Long = 45000L,
        airFastRateAr: Long = 60000L,
        seaRateAr: Long = 18000L
    ): List<TariffItem> {
        if (raw.startsWith("GRID:")) {
            val content = raw.removePrefix("GRID:")
            if (content.isBlank()) return defaultTariffs(airRateAr, airFastRateAr, seaRateAr)
            val items = content.split("||").mapNotNull { part ->
                val tokens = part.split("~")
                if (tokens.size >= 5) {
                    TariffItem(
                        id = if (tokens.size >= 6 && tokens[5].isNotBlank()) tokens[5] else UUID.randomUUID().toString(),
                        mode = tokens[0].ifBlank { "AÉRIEN" },
                        shipmentType = tokens[1],
                        goodsCategory = tokens[2],
                        priceAr = tokens[3].toLongOrNull() ?: 0L,
                        leadDays = tokens[4],
                        conditionOp = if (tokens.size >= 7 && tokens[6].isNotBlank()) tokens[6] else "NONE",
                        conditionValue = if (tokens.size >= 8) tokens[7].toDoubleOrNull() else null,
                        usdPrice = if (tokens.size >= 9) tokens[8].toDoubleOrNull() else null
                    )
                } else null
            }
            if (items.isNotEmpty()) return items
        }

        return defaultTariffs(airRateAr, airFastRateAr, seaRateAr)
    }

    fun encode(items: List<TariffItem>): String {
        if (items.isEmpty()) return ""
        val gridStr = items.joinToString("||") { item ->
            val condValStr = item.conditionValue?.toString() ?: ""
            val usdStr = item.usdPrice?.toString() ?: ""
            "${item.mode}~${item.shipmentType}~${item.goodsCategory}~${item.priceAr}~${item.leadDays}~${item.id}~${item.conditionOp}~$condValStr~$usdStr"
        }
        return "GRID:$gridStr"
    }

    fun formatCondition(item: TariffItem): String {
        val v = item.conditionValue ?: return ""
        val opSymbol = when (item.conditionOp) {
            "LT" -> "<"
            "LTE" -> "≤"
            "GT" -> ">"
            "GTE" -> "≥"
            else -> return ""
        }
        val unit = if (item.mode == "MARITIME") "CBM" else "kg"
        val formattedNum = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
        return "$opSymbol $formattedNum $unit"
    }

    fun oppositeOp(op: String): String = when (op) {
        "LT" -> "GTE"
        "LTE" -> "GT"
        "GT" -> "LTE"
        "GTE" -> "LT"
        else -> "NONE"
    }

    fun matchesMeasure(item: TariffItem, measure: Double): Boolean {
        val threshold = item.conditionValue ?: return false
        return when (item.conditionOp) {
            "LT" -> measure < threshold
            "LTE" -> measure <= threshold
            "GT" -> measure > threshold
            "GTE" -> measure >= threshold
            else -> false
        }
    }

    fun matchTariffForMeasure(items: List<TariffItem>, mode: String, measure: Double): TariffItem? {
        val modeItems = items.filter { it.mode == mode }
        if (modeItems.isEmpty()) return null
        val conditionalMatch = modeItems.firstOrNull { it.conditionOp != "NONE" && matchesMeasure(it, measure) }
        if (conditionalMatch != null) return conditionalMatch
        return modeItems.firstOrNull { it.conditionOp == "NONE" } ?: modeItems.firstOrNull()
    }

    fun defaultTariffs(
        airRateAr: Long = 45000L,
        airFastRateAr: Long = 60000L,
        seaRateAr: Long = 18000L
    ): List<TariffItem> {
        val effectiveAir = if (airRateAr > 0) airRateAr else 45000L
        val effectiveAirFast = if (airFastRateAr > 0) airFastRateAr else 60000L
        val effectiveSea = if (seaRateAr > 0) seaRateAr else 1200000L

        return listOf(
            TariffItem(
                mode = "AÉRIEN",
                shipmentType = "Standard Cargo",
                goodsCategory = "Marchandise Générale",
                priceAr = effectiveAir,
                leadDays = "7 à 10 jours"
            ),
            TariffItem(
                mode = "AÉRIEN",
                shipmentType = "Express Direct",
                goodsCategory = "Électronique & Batterie",
                priceAr = effectiveAirFast,
                leadDays = "3 à 5 jours"
            ),
            TariffItem(
                mode = "AÉRIEN",
                shipmentType = "Vol Spécial",
                goodsCategory = "Téléphones & High-Tech",
                priceAr = (effectiveAir * 1.25).toLong(),
                leadDays = "5 à 7 jours"
            ),
            TariffItem(
                mode = "MARITIME",
                shipmentType = "Groupage LCL",
                goodsCategory = "Marchandise Générale",
                priceAr = effectiveSea,
                leadDays = "30 à 45 jours"
            ),
            TariffItem(
                mode = "MARITIME",
                shipmentType = "Groupage LCL",
                goodsCategory = "Lourde & Volumineuse",
                priceAr = (effectiveSea * 0.85).toLong(),
                leadDays = "35 à 50 jours"
            )
        )
    }

    fun parseMinMaxDays(raw: String, defaultMin: Int, defaultMax: Int): Pair<Int, Int> {
        val nums = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.filter { it > 0 }.toList()
        return when {
            nums.size >= 2 -> Pair(nums[0], nums[1])
            nums.size == 1 -> Pair(nums[0], nums[0])
            else -> Pair(defaultMin, defaultMax)
        }
    }

    fun formatLeadDays(min: Int, max: Int): String {
        val validMin = min.coerceAtLeast(1)
        val validMax = max.coerceAtLeast(validMin)
        return if (validMin == validMax) {
            "$validMin ${if (validMin > 1) "jours" else "jour"}"
        } else {
            "$validMin à $validMax jours"
        }
    }
}
