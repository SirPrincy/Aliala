package com.example.data.model

enum class OrderStatus(val code: String, val stepIndex: Int, val label: String) {
    DRAFT("DRAFT", 0, "Brouillon"),
    ORDERED("ORDERED", 0, "Commandé"),
    WAREHOUSE("WAREHOUSE", 1, "En route vers hub"),
    GUANGZHOU("GUANGZHOU", 2, "Au hub transitaire"),
    FLIGHT("FLIGHT", 3, "En transit Mada"),
    CUSTOMS("CUSTOMS", 4, "Dédouanement"),
    QUALITY_CONTROL("QUALITY_CONTROL", 5, "Contrôle qualité"),
    DELIVERY_READY("DELIVERY_READY", 6, "Prêt / En stock"),
    IN_STOCK("IN_STOCK", 6, "En stock"),
    SOLD("SOLD", 7, "Vendu"),
    COMPLETED("COMPLETED", 7, "Terminé");

    companion object {
        fun fromCode(code: String): OrderStatus {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ORDERED
        }
    }
}

enum class FreightMode(val code: String, val label: String) {
    AIR("AÉRIEN", "Fret Aérien Express"),
    SEA("MARITIME", "Fret Maritime Groupage");

    companion object {
        fun fromCode(code: String): FreightMode {
            return if (code.equals("MARITIME", ignoreCase = true) || code.equals("SEA", ignoreCase = true)) SEA else AIR
        }
    }
}
