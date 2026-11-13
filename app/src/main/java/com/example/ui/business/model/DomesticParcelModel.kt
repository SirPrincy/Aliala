package com.example.ui.business.model

import com.example.data.local.entity.BusinessOrderEntity
import org.json.JSONArray
import org.json.JSONObject

data class DomesticParcelItem(
    val trackingNumber: String,
    val carrierName: String,
    val contentDescription: String,
    val checkpointTitle: String,
    val checkpointRoute: String,
    val updatedAtMillis: Long,
    val isDelivered: Boolean
)

object DomesticParcelStorage {

    val commonCarriers = listOf(
        "SF Express",
        "YTO Express",
        "ZTO Express",
        "Yunda Express",
        "Cainiao",
        "J&T Express",
        "STO Express",
        "China Post"
    )

    val checkpointPresets = listOf(
        "Pris en charge par le transporteur",
        "En transit vers le centre de tri",
        "En cours d'acheminement",
        "En livraison locale",
        "Arrivé à l'entrepôt transitaire"
    )

    fun inferCarrierFromTracking(tracking: String): String {
        val upper = tracking.trim().uppercase()
        return when {
            upper.startsWith("SF EXPRESS") || upper.startsWith("SF") -> "SF Express"
            upper.startsWith("YTO") || upper.startsWith("YT") -> "YTO Express"
            upper.startsWith("ZTO") || upper.startsWith("ZT") -> "ZTO Express"
            upper.startsWith("YUNDA") || upper.startsWith("YD") -> "Yunda Express"
            upper.startsWith("CAINIAO") || upper.startsWith("CN") -> "Cainiao"
            upper.startsWith("J&T") || upper.startsWith("JT") -> "J&T Express"
            upper.startsWith("STO") -> "STO Express"
            upper.startsWith("EMS") || upper.startsWith("CP") -> "China Post"
            else -> "Transporteur local"
        }
    }

    fun parse(order: BusinessOrderEntity): List<DomesticParcelItem> {
        val trackingList = order.trackingNumbers
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (trackingList.isEmpty()) return emptyList()

        val deliveredSet = order.deliveredTrackingNumbers
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val metaByTracking = mutableMapOf<String, JSONObject>()
        if (order.parcelDetailsJson.isNotBlank()) {
            try {
                val arr = JSONArray(order.parcelDetailsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val trk = obj.optString("tracking", "").trim()
                    if (trk.isNotEmpty()) {
                        metaByTracking[trk] = obj
                    }
                }
            } catch (_: Exception) {
                // Ignore malformed JSON gracefully and fall back to trackingNumbers
            }
        }

        val defaultContent = buildString {
            if (order.quantityBought > 0) append("${order.quantityBought} u")
            if (order.variants.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(order.variants)
            } else if (order.product.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(order.product)
            }
        }.ifBlank { "Marchandise" }

        return trackingList.map { trk ->
            val obj = metaByTracking[trk]
            val isDelivered = deliveredSet.contains(trk)
            val savedCarrier = obj?.optString("carrier", "")?.trim().orEmpty()
            val carrier = savedCarrier.ifBlank { inferCarrierFromTracking(trk) }
            val content = obj?.optString("content", "")?.trim().orEmpty().ifBlank { defaultContent }
            val defaultCheckpoint = if (isDelivered) {
                "Arrivé à l'entrepôt transitaire"
            } else {
                "En cours d'acheminement"
            }
            val checkpoint = obj?.optString("checkpoint", "")?.trim().orEmpty().ifBlank { defaultCheckpoint }
            val route = obj?.optString("route", "")?.trim().orEmpty()
            val updatedAt = obj?.optLong("updatedAt", order.orderDate) ?: order.orderDate

            DomesticParcelItem(
                trackingNumber = trk,
                carrierName = carrier,
                contentDescription = content,
                checkpointTitle = checkpoint,
                checkpointRoute = route,
                updatedAtMillis = if (updatedAt > 0L) updatedAt else order.orderDate,
                isDelivered = isDelivered
            )
        }
    }

    fun applyToOrder(
        order: BusinessOrderEntity,
        parcels: List<DomesticParcelItem>
    ): BusinessOrderEntity {
        val arr = JSONArray()
        parcels.forEach { parcel ->
            val obj = JSONObject().apply {
                put("tracking", parcel.trackingNumber)
                put("carrier", parcel.carrierName)
                put("content", parcel.contentDescription)
                put("checkpoint", parcel.checkpointTitle)
                put("route", parcel.checkpointRoute)
                put("updatedAt", parcel.updatedAtMillis)
            }
            arr.put(obj)
        }
        val allTrackings = parcels.joinToString(",") { it.trackingNumber }
        val deliveredTrackings = parcels.filter { it.isDelivered }.joinToString(",") { it.trackingNumber }
        return order.copy(
            trackingNumbers = allTrackings,
            deliveredTrackingNumbers = deliveredTrackings,
            parcelDetailsJson = if (parcels.isEmpty()) "" else arr.toString()
        )
    }

    fun upsertTracking(
        order: BusinessOrderEntity,
        oldTracking: String?,
        newTracking: String,
        carrierName: String? = null,
        contentDescription: String? = null
    ): BusinessOrderEntity {
        val cleaned = newTracking.trim()
        if (cleaned.isEmpty()) return order

        val existing = parse(order).toMutableList()
        val now = System.currentTimeMillis()
        val resolvedCarrier = carrierName?.trim()?.takeIf { it.isNotBlank() } ?: inferCarrierFromTracking(cleaned)

        val editIndex = if (!oldTracking.isNullOrBlank()) {
            existing.indexOfFirst { it.trackingNumber.equals(oldTracking.trim(), ignoreCase = true) }
        } else -1

        if (editIndex in existing.indices) {
            val prev = existing[editIndex]
            existing[editIndex] = prev.copy(
                trackingNumber = cleaned,
                carrierName = if (!carrierName.isNullOrBlank()) resolvedCarrier else inferCarrierFromTracking(cleaned),
                contentDescription = contentDescription?.trim()?.takeIf { it.isNotBlank() } ?: prev.contentDescription,
                updatedAtMillis = now
            )
        } else if (existing.none { it.trackingNumber.equals(cleaned, ignoreCase = true) }) {
            val defaultContent = buildString {
                if (order.quantityBought > 0) append("${order.quantityBought} u")
                if (order.variants.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(order.variants)
                } else if (order.product.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(order.product)
                }
            }.ifBlank { "Marchandise" }

            existing.add(
                DomesticParcelItem(
                    trackingNumber = cleaned,
                    carrierName = resolvedCarrier,
                    contentDescription = contentDescription?.trim()?.takeIf { it.isNotBlank() } ?: defaultContent,
                    checkpointTitle = "Pris en charge par le transporteur",
                    checkpointRoute = "",
                    updatedAtMillis = now,
                    isDelivered = false
                )
            )
        }
        return applyToOrder(order, existing)
    }

    fun removeTracking(
        order: BusinessOrderEntity,
        trackingNumber: String
    ): BusinessOrderEntity {
        val remaining = parse(order).filterNot { it.trackingNumber.equals(trackingNumber.trim(), ignoreCase = true) }
        return applyToOrder(order, remaining)
    }
}
