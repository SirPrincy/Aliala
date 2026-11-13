package com.example.ui.business.managers

import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.CatalogueFamilyEntity
import com.example.data.local.entity.CataloguePlatformEntity
import com.example.data.repository.AppRepository
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueFamilySummaryUi
import kotlinx.coroutines.flow.firstOrNull

class StockManager(private val repository: AppRepository) {

    suspend fun adjustStock(
        order: BusinessOrderEntity,
        newStockCount: Int,
        newReservedCount: Int,
        newDefectiveCount: Int,
        assignedClient: String
    ): String {
        repository.updateBusinessOrder(
            order.copy(
                stockCount = newStockCount.coerceAtLeast(0),
                reservedCount = newReservedCount.coerceAtLeast(0),
                defectiveCount = newDefectiveCount.coerceAtLeast(0),
                assignedClient = assignedClient
            )
        )
        return "Stock mis à jour pour ${order.product}"
    }

    suspend fun toggleHideStockItem(order: BusinessOrderEntity): String {
        repository.updateBusinessOrder(order.copy(isHidden = !order.isHidden))
        return if (!order.isHidden) "${order.product} masqué" else "${order.product} réaffiché"
    }

    suspend fun toggleHideCatalogueArticle(article: CatalogueArticleSummaryUi): String {
        val targetHidden = !article.isHidden
        val lotsToUpdate = article.lots.ifEmpty { listOf(article.primaryOrder) }
        lotsToUpdate.forEach { lot ->
            repository.updateBusinessOrder(lot.copy(isHidden = targetHidden))
        }
        return if (targetHidden) "${article.primaryOrder.product} masqué du catalogue" else "${article.primaryOrder.product} réaffiché"
    }

    suspend fun deleteStockOrder(order: BusinessOrderEntity): String {
        repository.deleteBusinessOrder(order.id)
        return "${order.product} supprimé"
    }

    suspend fun deleteCatalogueArticle(article: CatalogueArticleSummaryUi): String {
        val lotsToDelete = article.lots.ifEmpty { listOf(article.primaryOrder) }
        lotsToDelete.forEach { lot ->
            repository.deleteBusinessOrder(lot.id)
        }
        return "${article.primaryOrder.product} supprimé (${lotsToDelete.size} lot${if (lotsToDelete.size > 1) "s" else ""})"
    }

    suspend fun addCatalogueArticle(
        product: String,
        family: String,
        referenceCode: String,
        variants: String,
        priceYuan: Double,
        resaleAr: Long,
        stockCount: Int,
        weightKg: Double = 0.2,
        platform: String = "1688",
        cmupRate: Double = 650.0,
        imageUrl: String = "",
        freightMode: String = "AÉRIEN",
        notes: String = "",
        estimatedFreightPerKgAr: Long = 45000L
    ): String {
        val sku = referenceCode.trim().ifBlank {
            val cleanPrefix = product.trim().take(4).uppercase().replace(Regex("[^A-Z0-9]"), "").ifBlank { "ART" }
            "$cleanPrefix-${(100..999).random()}"
        }
        val effectiveRate = if (cmupRate > 0.0) cmupRate else 650.0
        val matchedFamily = repository.getAllCatalogueFamiliesSnapshot().firstOrNull {
            it.name.equals(family.trim(), ignoreCase = true)
        }
        val estUnitFreightAr = if (weightKg > 0.0 && estimatedFreightPerKgAr > 0L) {
            kotlin.math.round(weightKg * estimatedFreightPerKgAr).toLong()
        } else 0L
        val estLandedUnitCostAr = kotlin.math.round(priceYuan * effectiveRate).toLong() + estUnitFreightAr
        val computedMargin = if (resaleAr > 0L && estLandedUnitCostAr > 0L) {
            (((resaleAr - estLandedUnitCostAr).toDouble() / resaleAr.toDouble()) * 100).toInt()
        } else 0

        repository.insertBusinessOrder(
            BusinessOrderEntity(
                product = product.trim(),
                supplier = platform.trim().ifBlank { "1688" },
                platform = platform.trim().ifBlank { "1688" },
                referenceCode = sku,
                quantityBought = stockCount.coerceAtLeast(0),
                priceYuan = priceYuan.coerceAtLeast(0.0),
                exchangeRate = effectiveRate,
                resalePriceAr = resaleAr.coerceAtLeast(0L),
                stockCount = stockCount.coerceAtLeast(0),
                weightKg = weightKg.takeIf { it > 0.0 },
                status = if (stockCount > 0) "DELIVERY_READY" else "CATALOGUE",
                category = "BUSINESS",
                family = family.trim().ifBlank { "Divers" },
                familyId = matchedFamily?.id ?: 0L,
                carrier = "",
                variants = variants.trim(),
                imageUrl = imageUrl.trim(),
                freightMode = freightMode,
                receptionNotes = notes.trim(),
                freightCostAr = if (stockCount > 0) estUnitFreightAr * stockCount else 0L,
                unitCostAr = if (stockCount > 0 && estLandedUnitCostAr > 0L) estLandedUnitCostAr else 0L,
                isFreightPaid = true,
                unitsSold = 0,
                marginPercent = computedMargin
            )
        )
        return "${product.trim()} ajouté au catalogue ($sku)"
    }

    suspend fun updateCatalogueArticle(
        order: BusinessOrderEntity,
        newPriceYuan: Double,
        newResaleAr: Long,
        newVariants: String,
        isHidden: Boolean
    ): String {
        val effRate = if (order.exchangeRate > 0.0) order.exchangeRate else 650.0
        val unitFreight = if (order.freightCostAr > 0L && order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L
        val estCost = kotlin.math.round(newPriceYuan * effRate).toLong() + unitFreight
        val computedMargin = if (newResaleAr > 0L && estCost > 0L) {
            (((newResaleAr - estCost).toDouble() / newResaleAr.toDouble()) * 100).toInt()
        } else order.marginPercent
        repository.updateBusinessOrder(
            order.copy(
                priceYuan = newPriceYuan,
                resalePriceAr = newResaleAr,
                variants = newVariants,
                isHidden = isHidden,
                marginPercent = computedMargin
            )
        )
        return "${order.product} mis à jour"
    }

    suspend fun updateCatalogueArticleFull(
        article: CatalogueArticleSummaryUi,
        newProduct: String,
        newReferenceCode: String,
        newFamily: String,
        newPlatform: String,
        newPriceYuan: Double,
        newResaleAr: Long,
        newWeightKg: Double?,
        newFreightMode: String,
        newVariants: String,
        newImageUrl: String,
        newNotes: String,
        isHidden: Boolean,
        cmupRate: Double
    ): String {
        val trimmedName = newProduct.trim().ifBlank { article.primaryOrder.product }
        val trimmedSku = newReferenceCode.trim().ifBlank { article.primaryOrder.referenceCode }
        val trimmedFamily = newFamily.trim().ifBlank { article.primaryOrder.family }
        val trimmedPlatform = newPlatform.trim().ifBlank { article.primaryOrder.platform }
        val matchedFamily = repository.getAllCatalogueFamiliesSnapshot().firstOrNull {
            it.name.equals(trimmedFamily, ignoreCase = true)
        }
        val effectiveRate = if (cmupRate > 0.0) cmupRate else 650.0

        val primary = article.primaryOrder
        val primaryUnitFreight = if (primary.freightCostAr > 0L && primary.quantityBought > 0) primary.freightCostAr / primary.quantityBought else 0L
        val primaryEstCost = kotlin.math.round(newPriceYuan * effectiveRate).toLong() + primaryUnitFreight
        val computedMargin = if (newResaleAr > 0L && primaryEstCost > 0L) {
            (((newResaleAr - primaryEstCost).toDouble() / newResaleAr.toDouble()) * 100).toInt()
        } else primary.marginPercent

        val lotsToUpdate = article.lots.ifEmpty { listOf(primary) }
        lotsToUpdate.forEach { lot ->
            val updated = if (lot.id == primary.id) {
                lot.copy(
                    product = trimmedName,
                    referenceCode = trimmedSku,
                    family = trimmedFamily,
                    familyId = matchedFamily?.id ?: lot.familyId,
                    platform = trimmedPlatform,
                    supplier = if (lot.supplier.isBlank() || lot.supplier == lot.platform) trimmedPlatform else lot.supplier,
                    priceYuan = newPriceYuan,
                    resalePriceAr = newResaleAr,
                    weightKg = newWeightKg,
                    freightMode = newFreightMode,
                    variants = newVariants.trim(),
                    imageUrl = newImageUrl.trim(),
                    receptionNotes = newNotes.trim(),
                    isHidden = isHidden,
                    marginPercent = computedMargin
                )
            } else {
                // Propagate shared catalog identity across sibling lots while keeping lot-specific history intact
                lot.copy(
                    product = trimmedName,
                    referenceCode = trimmedSku,
                    family = trimmedFamily,
                    familyId = matchedFamily?.id ?: lot.familyId,
                    resalePriceAr = newResaleAr,
                    weightKg = newWeightKg ?: lot.weightKg,
                    imageUrl = newImageUrl.trim(),
                    isHidden = isHidden
                )
            }
            repository.updateBusinessOrder(updated)
        }
        return "$trimmedName mis à jour"
    }

    suspend fun addCatalogueFamily(name: String, emoji: String = "✨", colorHex: String = "#06B6D4"): String? {
        if (name.isBlank()) return null
        val snapshot = repository.getAllCatalogueFamiliesSnapshot()
        val nextOrder = (snapshot.maxOfOrNull { it.displayOrder } ?: 0) + 1
        repository.insertCatalogueFamily(
            CatalogueFamilyEntity(
                name = name.trim(),
                emoji = emoji,
                colorHex = colorHex,
                displayOrder = nextOrder,
                isDemo = false
            )
        )
        return "Famille '${name.trim()}' créée avec succès"
    }

    suspend fun deleteCatalogueFamily(family: CatalogueFamilySummaryUi, targetFamilyName: String = "Divers"): String {
        val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
        val toMigrate = allOrders.filter { it.family.equals(family.name, ignoreCase = true) }
        toMigrate.forEach { order ->
            repository.updateBusinessOrder(order.copy(family = targetFamilyName))
        }
        if (family.id > 0) {
            repository.deleteCatalogueFamily(family.id)
        }
        return "Famille '${family.name}' supprimée (articles transférés vers $targetFamilyName)"
    }

    suspend fun addBusinessOrder(
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resaleAr: Long,
        category: String,
        carrier: String,
        variants: String,
        freightAr: Long,
        platform: String = "1688",
        referenceCode: String = "",
        family: String = "Électronique",
        cmupRate: Double = 650.0
    ): String {
        repository.insertBusinessOrder(
            BusinessOrderEntity(
                product = product,
                supplier = supplier,
                platform = platform,
                referenceCode = referenceCode.ifBlank { "LOT-${System.currentTimeMillis() % 10000}" },
                quantityBought = quantity,
                priceYuan = priceYuan,
                exchangeRate = cmupRate,
                resalePriceAr = resaleAr,
                status = "ORDERED",
                category = category,
                family = family,
                carrier = carrier,
                variants = variants,
                freightCostAr = freightAr,
                isFreightPaid = false
            )
        )
        return "Nouvelle commande enregistrée"
    }

    suspend fun advanceOrderStatus(order: BusinessOrderEntity): String {
        val nextStatus = when (order.status) {
            "DRAFT" -> "ORDERED"
            "ORDERED" -> "WAREHOUSE"
            "WAREHOUSE" -> "GUANGZHOU"
            "GUANGZHOU" -> "FLIGHT"
            "FLIGHT" -> "CUSTOMS"
            "CUSTOMS" -> "QUALITY_CONTROL"
            "QUALITY_CONTROL" -> "DELIVERY_READY"
            "DELIVERY_READY" -> "SOLD"
            else -> "SOLD"
        }
        val statusLabelFr = when (nextStatus) {
            "DRAFT" -> "Brouillon"
            "ORDERED" -> "Commandé 1688"
            "WAREHOUSE" -> "Reçu entrepôt Chine"
            "GUANGZHOU" -> "Hub Guangzhou"
            "FLIGHT" -> "En vol / Transit"
            "CUSTOMS" -> "Dédouanement Mada"
            "QUALITY_CONTROL" -> "Contrôle qualité"
            "DELIVERY_READY" -> "Prêt à livrer / En stock"
            "SOLD" -> "Vendu"
            else -> nextStatus
        }
        repository.updateBusinessOrder(order.copy(status = nextStatus))
        return "Statut mis à jour : $statusLabelFr"
    }

    suspend fun onAdvanceQualityControl(order: BusinessOrderEntity): String {
        repository.updateBusinessOrder(order.copy(status = "DELIVERY_READY"))
        return "Contrôle qualité validé pour ${order.product}"
    }

    suspend fun updateBusinessOrder(order: BusinessOrderEntity): String {
        repository.updateBusinessOrder(order)
        return "${order.product} mis à jour"
    }

    suspend fun duplicateBusinessOrder(order: BusinessOrderEntity): String {
        val duplicate = order.copy(
            id = 0,
            lotNumber = "${order.lotNumber} (Copie)",
            orderDate = System.currentTimeMillis()
        )
        repository.insertBusinessOrder(duplicate)
        return "Commande dupliquée : ${order.product}"
    }

    suspend fun togglePlatformActive(platformId: Long, isActive: Boolean) {
        repository.updatePlatformActive(platformId, isActive)
    }

    suspend fun updatePlatformShippingTag(platformId: Long, newShippingTag: String): String? {
        val snapshot = repository.getAllCataloguePlatformsSnapshot()
        val target = snapshot.find { it.id == platformId }
        return if (target != null) {
            repository.updateCataloguePlatform(target.copy(shippingTag = newShippingTag))
            "Mode de port mis à jour : $newShippingTag"
        } else null
    }

    suspend fun addCataloguePlatform(
        name: String,
        subtitle: String,
        shippingTag: String,
        secondaryTag: String,
        iconType: String = "STORE",
        colorHex: String = "#712ae2"
    ): String {
        val count = repository.getAllCataloguePlatformsSnapshot().size
        val isVerified = name.contains("1688", ignoreCase = true)
        val badge = if (name.contains("taobao", ignoreCase = true)) "C2C / B2C"
                    else if (name.contains("pinduoduo", ignoreCase = true)) "Usine Direct"
                    else ""
        val shipIcon = if (shippingTag.contains("gratuit", ignoreCase = true)) "check_circle"
                       else if (shippingTag.contains("devis", ignoreCase = true)) "request_quote"
                       else "local_shipping"
        val alias = name.split(" ").firstOrNull() ?: name

        val newPlat = CataloguePlatformEntity(
            name = name.trim(),
            aliasKey = alias,
            subtitle = subtitle.trim(),
            badgeText = badge,
            isVerified = isVerified,
            iconType = iconType,
            colorHex = colorHex,
            shippingTag = shippingTag.trim(),
            shippingTagIcon = shipIcon,
            secondaryTag = secondaryTag.trim(),
            isActive = true,
            displayOrder = count + 1,
            defaultYuanSpent = 0.0,
            defaultOrdersCount = 0,
            defaultLastDate = "",
            transitNotes = ""
        )
        repository.insertCataloguePlatform(newPlat)
        return "Plateforme « $name » ajoutée au catalogue"
    }

    suspend fun deleteCataloguePlatform(platformId: Long): String {
        repository.deleteCataloguePlatform(platformId)
        return "Plateforme supprimée"
    }
}
