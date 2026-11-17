package com.example.ui.business.managers

import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.CatalogueFamilyEntity
import com.example.data.local.entity.CataloguePlatformEntity
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CataloguePlatformSummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.FamilyFilterUi
import com.example.ui.business.TransitaireShipmentSummaryUi
import com.example.ui.business.shared.VariantColorUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

internal data class CatalogueMappingResult(
    val activeCatalogueCount: Int,
    val hiddenCatalogueCount: Int,
    val catalogueFamilyPills: List<FamilyFilterUi>,
    val filteredCatalogueOrders: List<BusinessOrderEntity>,
    val allCatalogueArticles: List<CatalogueArticleSummaryUi>,
    val visibleCatalogueArticles: List<CatalogueArticleSummaryUi>,
    val catalogueFamiliesSummary: List<CatalogueFamilySummaryUi>,
    val cataloguePlatformsSummary: List<CataloguePlatformSummaryUi>,
    val catalogueTransitairesSummary: List<CatalogueTransitaireSummaryUi>
)

internal fun mapCatalogueSection(
    raw: BusinessRawData,
    cFilters: CatalogueFilters,
    cmupRate: Long,
    transitaireDueMap: Map<Long, Long>
): CatalogueMappingResult {
    val orders = raw.orders
    val sales = raw.sales

    val defaultFamilies = listOf(
        CatalogueFamilyEntity(id = 1, name = "Électronique & Audio", emoji = "🎧", colorHex = "#06B6D4", displayOrder = 1),
        CatalogueFamilyEntity(id = 2, name = "Mode & Maroquinerie", emoji = "🎒", colorHex = "#EC4899", displayOrder = 2),
        CatalogueFamilyEntity(id = 3, name = "Maison & Rangement", emoji = "🏠", colorHex = "#F97316", displayOrder = 3),
        CatalogueFamilyEntity(id = 4, name = "Accessoires Mobiles", emoji = "📱", colorHex = "#8B5CF6", displayOrder = 4)
    )
    val availableFamilies = raw.families.ifEmpty { defaultFamilies }

    val inTransitStatuses = setOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL")
    val allCatalogueArticles = orders
        .groupBy { order ->
            val skuKey = order.referenceCode.trim().lowercase()
            val nameKey = order.product.trim().lowercase()
            if (skuKey.isNotBlank()) "sku:$skuKey" else "name:$nameKey"
        }
        .map { (articleKey, groupLots) ->
            val sortedLots = groupLots.sortedByDescending { it.orderDate }
            val primary = sortedLots.firstOrNull { !it.isHidden } ?: sortedLots.first()

            val resolvedImage = sortedLots.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: ""
            val resolvedFamily = sortedLots.firstOrNull { it.family.isNotBlank() }?.family ?: "Divers"
            val resolvedPlatform = sortedLots.firstOrNull { it.platform.isNotBlank() }?.platform ?: primary.supplier
            val resolvedWeight = sortedLots.firstOrNull { (it.weightKg ?: 0.0) > 0.0 }?.weightKg
            val resolvedResale = sortedLots.firstOrNull { it.resalePriceAr > 0L }?.resalePriceAr ?: primary.resalePriceAr
            val resolvedSku = sortedLots.firstOrNull { it.referenceCode.isNotBlank() }?.referenceCode ?: primary.referenceCode
            val resolvedNotes = sortedLots.firstOrNull { it.receptionNotes.isNotBlank() }?.receptionNotes ?: ""

            val hydratedPrimary = primary.copy(
                imageUrl = resolvedImage,
                family = resolvedFamily,
                platform = resolvedPlatform,
                weightKg = resolvedWeight,
                resalePriceAr = resolvedResale,
                referenceCode = resolvedSku,
                receptionNotes = resolvedNotes
            )

            val totalStock = sortedLots.sumOf { it.stockCount.coerceAtLeast(0) }
            val totalTransit = sortedLots.filter { it.status in inTransitStatuses }.sumOf { it.quantityBought.coerceAtLeast(0) }
            val totalReserved = sortedLots.sumOf { it.reservedCount.coerceAtLeast(0) }
            val totalDefective = sortedLots.sumOf { it.defectiveCount.coerceAtLeast(0) }
            val totalBought = sortedLots.sumOf { if (it.status == "CATALOGUE") 0 else it.quantityBought.coerceAtLeast(0) }

            val lotIds = sortedLots.map { it.id }.toSet()
            val normalizedProduct = hydratedPrimary.product.trim().lowercase()
            val matchingSales = sales.filter { sale ->
                (sale.linkedOrderId != null && sale.linkedOrderId in lotIds) ||
                    (normalizedProduct.isNotBlank() && sale.productName.trim().lowercase() == normalizedProduct)
            }.sortedByDescending { it.saleDate }

            val salesQty = matchingSales.sumOf { it.quantity }
            val lotSoldSum = sortedLots.sumOf { it.unitsSold.coerceAtLeast(0) }
            val totalSold = maxOf(lotSoldSum, salesQty)

            val latestCostLot = sortedLots.firstOrNull { it.unitCostAr > 0L || it.priceYuan > 0.0 } ?: hydratedPrimary
            val latestPriceYuan = latestCostLot.priceYuan
            val latestUnitCostAr = when {
                latestCostLot.unitCostAr > 0L -> latestCostLot.unitCostAr
                latestCostLot.priceYuan > 0.0 -> {
                    val effRate = if (latestCostLot.exchangeRate > 0.0) latestCostLot.exchangeRate else cmupRate.toDouble()
                    val unitFreight = if (latestCostLot.freightCostAr > 0L && latestCostLot.quantityBought > 0) {
                        latestCostLot.freightCostAr / latestCostLot.quantityBought
                    } else 0L
                    kotlin.math.round(latestCostLot.priceYuan * effRate).toLong() + unitFreight
                }
                else -> 0L
            }

            val computedMarginPct = when {
                resolvedResale > 0L && latestUnitCostAr > 0L ->
                    (((resolvedResale - latestUnitCostAr).toDouble() / resolvedResale.toDouble()) * 100).toInt()
                hydratedPrimary.marginPercent != 0 -> hydratedPrimary.marginPercent
                else -> 0
            }

            val totalRevenueAr = if (matchingSales.isNotEmpty()) {
                matchingSales.sumOf { it.totalPriceAr }
            } else {
                totalSold.toLong() * resolvedResale
            }
            val realProfitAr = if (matchingSales.isNotEmpty()) {
                matchingSales.sumOf { sale ->
                    val c = if (sale.unitCostAr > 0L) sale.unitCostAr else latestUnitCostAr
                    sale.totalPriceAr - (c * sale.quantity)
                }
            } else {
                (resolvedResale - latestUnitCostAr).coerceAtLeast(0L) * totalSold
            }

            val matchedFam = availableFamilies.firstOrNull { fam ->
                resolvedFamily.equals(fam.name, ignoreCase = true) ||
                    fam.name.contains(resolvedFamily, ignoreCase = true) ||
                    (resolvedFamily.length >= 4 && fam.name.contains(resolvedFamily.take(4), ignoreCase = true))
            }

            val isHiddenArticle = sortedLots.all { it.isHidden }
            val isRefOnly = sortedLots.all { it.status == "CATALOGUE" && it.quantityBought == 0 && it.stockCount == 0 }
            val statusTag = when {
                isHiddenArticle -> "Masqué"
                totalStock > 5 -> "En stock"
                totalStock in 1..5 -> "Stock faible"
                totalTransit > 0 -> "En transit"
                isRefOnly -> "Référentiel"
                else -> "Rupture"
            }

            val combinedVariants = sortedLots
                .flatMap { VariantColorUtils.parseVariants(it.variants) }
                .distinct()

            CatalogueArticleSummaryUi(
                articleKey = articleKey,
                primaryOrder = hydratedPrimary,
                lots = sortedLots,
                lotsCount = sortedLots.size,
                totalStockCount = totalStock,
                totalInTransitCount = totalTransit,
                totalReservedCount = totalReserved,
                totalDefectiveCount = totalDefective,
                totalBoughtCount = totalBought,
                totalSoldCount = totalSold,
                totalRevenueAr = totalRevenueAr,
                realProfitAr = realProfitAr,
                latestPriceYuan = latestPriceYuan,
                latestUnitCostAr = latestUnitCostAr,
                resalePriceAr = resolvedResale,
                marginPercent = computedMarginPct,
                familyEmoji = matchedFam?.emoji ?: "📦",
                familyColorHex = matchedFam?.colorHex ?: "#06B6D4",
                statusTag = statusTag,
                isHidden = isHiddenArticle,
                isReferenceOnly = isRefOnly,
                allVariants = combinedVariants,
                salesHistory = matchingSales
            )
        }

    val activeCatalogueCount = allCatalogueArticles.count { !it.isHidden }
    val hiddenCatalogueCount = allCatalogueArticles.count { it.isHidden }

    val catalogueFamilyPills = buildList {
        add(FamilyFilterUi("Toutes", activeCatalogueCount))
        availableFamilies.forEach { fam ->
            val countForFam = allCatalogueArticles.count { art ->
                !art.isHidden && (
                    art.primaryOrder.family.equals(fam.name, ignoreCase = true) ||
                        fam.name.contains(art.primaryOrder.family, ignoreCase = true) ||
                        (art.primaryOrder.family.length >= 4 && fam.name.contains(art.primaryOrder.family.take(4), ignoreCase = true))
                    )
            }
            add(FamilyFilterUi(fam.name, countForFam))
        }
    }

    val visibleCatalogueArticles = allCatalogueArticles
        .filter { art ->
            val matchHidden = if (cFilters.showHidden) true else !art.isHidden
            val matchSearch = if (cFilters.searchQuery.isBlank()) true else {
                val q = cFilters.searchQuery
                art.primaryOrder.product.contains(q, ignoreCase = true) ||
                    art.primaryOrder.referenceCode.contains(q, ignoreCase = true) ||
                    art.allVariants.any { it.contains(q, ignoreCase = true) } ||
                    art.primaryOrder.family.contains(q, ignoreCase = true) ||
                    art.primaryOrder.platform.contains(q, ignoreCase = true)
            }
            val matchFamily = if (cFilters.family == "Toutes") true else {
                val f = art.primaryOrder.family
                f.equals(cFilters.family, ignoreCase = true) ||
                    cFilters.family.contains(f, ignoreCase = true) ||
                    (f.length >= 4 && cFilters.family.contains(f.take(4), ignoreCase = true))
            }
            val matchStock = when (cFilters.stockFilter) {
                "En stock" -> art.totalStockCount > 0
                "En transit" -> art.totalInTransitCount > 0
                "Rupture" -> art.totalStockCount == 0 && art.totalInTransitCount == 0 && !art.isReferenceOnly
                "Référentiel" -> art.isReferenceOnly
                else -> true
            }
            matchHidden && matchSearch && matchFamily && matchStock
        }
        .let { list ->
            when (cFilters.sort) {
                "Plus vendus" -> list.sortedByDescending { it.totalSoldCount }
                "Marge élevée" -> list.sortedByDescending { it.marginPercent }
                "Stock bas" -> list.sortedBy { it.totalStockCount }
                "Dernier coût" -> list.sortedByDescending { it.latestUnitCostAr }
                "Nom" -> list.sortedBy { it.primaryOrder.product.lowercase() }
                "Plus récents" -> list.sortedByDescending { it.primaryOrder.orderDate }
                else -> list.sortedByDescending { it.totalSoldCount }
            }
        }

    val catalogueBaseList = if (cFilters.showHidden) orders else orders.filter { !it.isHidden }
    val filteredCatalogueOrders = catalogueBaseList.filter { order ->
        val matchSearch = if (cFilters.searchQuery.isBlank()) true else {
            order.product.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.referenceCode.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.variants.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.family.contains(cFilters.searchQuery, ignoreCase = true)
        }
        val matchFamily = if (cFilters.family == "Toutes") true else {
            order.family.equals(cFilters.family, ignoreCase = true) ||
                cFilters.family.contains(order.family, ignoreCase = true) ||
                (order.family.length >= 4 && cFilters.family.contains(order.family.take(4), ignoreCase = true))
        }
        matchSearch && matchFamily
    }.let { list ->
        when (cFilters.sort) {
            "Plus vendus" -> list.sortedByDescending { it.unitsSold }
            "Dernier coût" -> list.sortedByDescending {
                if (it.unitCostAr > 0L) it.unitCostAr else (it.priceYuan * it.exchangeRate).toLong()
            }
            "Nom" -> list.sortedBy { it.product.lowercase() }
            else -> list.sortedByDescending { it.unitsSold }
        }
    }

    val catalogueFamiliesSummary = availableFamilies.map { fam ->
        val famOrders = orders.filter { o ->
            val of = o.family.trim()
            val fn = fam.name.trim()
            of.equals(fn, ignoreCase = true) ||
                fn.contains(of, ignoreCase = true) ||
                (of.length >= 4 && fn.contains(of.take(4), ignoreCase = true))
        }
        val totalStock = famOrders.sumOf { it.stockCount }
        val activeCount = famOrders.count { !it.isHidden }
        val hiddenCount = famOrders.count { it.isHidden }
        val stockVal = famOrders.sumOf { order ->
            val unitFifo = when {
                order.unitCostAr > 0L -> order.unitCostAr
                order.priceYuan > 0.0 -> (order.priceYuan * order.exchangeRate).toLong() +
                    (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
                else -> 0L
            }
            unitFifo * order.stockCount
        }
        val avgMargin = if (famOrders.isNotEmpty()) famOrders.map { it.marginPercent }.average().toInt() else 0

        CatalogueFamilySummaryUi(
            id = fam.id,
            name = fam.name,
            emoji = fam.emoji,
            colorHex = fam.colorHex,
            articleCount = famOrders.size,
            activeCount = activeCount,
            hiddenCount = hiddenCount,
            totalStock = totalStock,
            stockValueAr = stockVal,
            avgMarginPercent = avgMargin
        )
    }

    val defaultPlatforms = listOf(
        CataloguePlatformEntity(id = 1, name = "1688.com", aliasKey = "1688", subtitle = "Alibaba Wholesale (Usines B2B)", isVerified = true, iconType = "TEXT_1688", colorHex = "#712ae2", shippingTag = "Port Payant (Sur devis)", shippingTagIcon = "local_shipping", secondaryTag = "Min. commande : 2-5 pcs", isActive = true, displayOrder = 1, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Tarif transitaire aérien: 45 000 Ar/kg. Maritime: 18 000 Ar/kg."),
        CataloguePlatformEntity(id = 2, name = "Taobao", aliasKey = "Taobao", subtitle = "Consommation & Petits lots", badgeText = "C2C / B2C", isVerified = false, iconType = "CART", colorHex = "#8a4cfc", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Livraison rapide état côtier", isActive = true, displayOrder = 2, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Livraison rapide Guangdong en 48h."),
        CataloguePlatformEntity(id = 3, name = "Pinduoduo", aliasKey = "Pinduoduo", subtitle = "Direct Usine / Déstockage & Volume", badgeText = "Usine Direct", isVerified = false, iconType = "PRICE", colorHex = "#ffdad6", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Remises groupées", isActive = true, displayOrder = 3, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Commandes groupées direct usines."),
        CataloguePlatformEntity(id = 4, name = "Fournisseur Direct WeChat", aliasKey = "WeChat", subtitle = "Sino-Contact / Négociations privées", isVerified = false, iconType = "WECHAT", colorHex = "#acedff", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "request_quote", secondaryTag = "Dépôt direct transitaire", isActive = true, displayOrder = 4, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Paiement direct WeChat Pay ou virement agent."),
        CataloguePlatformEntity(id = 5, name = "Xianyu (闲鱼)", aliasKey = "Xianyu", subtitle = "B2C / C2C Occasion & Pièces rares", badgeText = "Seconde main", isVerified = false, iconType = "CART", colorHex = "#FFC107", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "local_shipping", secondaryTag = "Négociation vendeur", isActive = true, displayOrder = 5, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Transactions de seconde main et pièces uniques directes de particuliers ou usines.")
    )
    val availablePlatforms = raw.platforms.ifEmpty { defaultPlatforms }

    val cataloguePlatformsSummary = availablePlatforms.map { plat ->
        val platOrders = orders.filter { o ->
            val op = o.platform.trim()
            val os = o.supplier.trim()
            val pn = plat.name.trim()
            val pa = plat.aliasKey.trim()
            op.equals(pn, ignoreCase = true) ||
                (pa.isNotBlank() && op.contains(pa, ignoreCase = true)) ||
                (pa.isNotBlank() && os.contains(pa, ignoreCase = true)) ||
                os.contains(pn, ignoreCase = true) ||
                pn.contains(op, ignoreCase = true)
        }
        val hasOrders = platOrders.isNotEmpty()
        val totalSpentYuan = if (hasOrders) platOrders.sumOf { it.priceYuan * it.quantityBought } else 0.0
        val totalSpentAr = if (hasOrders) {
            platOrders.sumOf { it.resalePriceAr.coerceAtLeast((it.priceYuan * it.quantityBought * it.exchangeRate).toLong()) }
        } else 0L
        val lastDate = if (hasOrders) {
            val latest = platOrders.maxByOrNull { it.orderDate }
            if (latest != null) {
                val dt = Instant.ofEpochMilli(latest.orderDate).atZone(ZoneId.systemDefault()).toLocalDate()
                dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH))
            } else "Aucune commande"
        } else "Aucune commande"

        CataloguePlatformSummaryUi(
            id = plat.id,
            name = plat.name,
            aliasKey = plat.aliasKey,
            subtitle = plat.subtitle,
            badgeText = plat.badgeText,
            isVerified = plat.isVerified,
            iconType = plat.iconType,
            colorHex = plat.colorHex,
            shippingTag = plat.shippingTag,
            shippingTagIcon = plat.shippingTagIcon,
            secondaryTag = plat.secondaryTag,
            isActive = plat.isActive,
            articleCount = platOrders.size,
            totalLots = platOrders.size,
            totalSpentYuan = totalSpentYuan,
            totalSpentAr = totalSpentAr,
            lastOrderDate = lastDate,
            transitNotes = plat.transitNotes,
            hasTransitDetails = plat.transitNotes.isNotBlank()
        )
    }

    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    val historyByTransitaire = raw.transitaireRateHistory.groupBy { it.transitaireId }

    fun formatTrendFromHistory(entries: List<com.example.data.local.entity.TransitaireRateHistoryEntity>, mode: String): String {
        val latestDiff = entries.firstOrNull { it.mode == mode && it.oldRateAr > 0L && it.newRateAr > 0L && it.deltaAr != 0L }
            ?: return ""
        val pct = latestDiff.deltaPercent
        val sign = if (pct > 0.0) "+" else ""
        return if (kotlin.math.abs(pct) >= 10.0 || kotlin.math.abs(pct % 1.0) < 0.05) {
            "$sign${kotlin.math.round(pct).toInt()}%"
        } else {
            String.format(Locale.US, "%s%.1f%%", sign, pct)
        }
    }

    val ratedAirRates = raw.transitaires
        .filter { !it.notes.startsWith("[ARCHIVED]") }
        .map { if (it.airNormalRateAr > 0L) it.airNormalRateAr else it.defaultTariffPerKg }
        .filter { it > 0L }
    val minActiveAirRate = if (ratedAirRates.size >= 2) ratedAirRates.minOrNull() else null

    val catalogueTransitairesSummary = raw.transitaires.map { t ->
        val transOrders = orders.filter { it.carrier.contains(t.name, ignoreCase = true) }
        val inTransitCount = transOrders.count { it.status in inTransitStatuses }
        val inits = t.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "TR" }

        val totalInvoiced = transOrders.sumOf { it.freightCostAr }
        val paidViaTx = raw.businessTransactions
            .filter { it.referenceTag == "TRANSITAIRE_${t.id}" && it.type == "EXPENSE" }
            .sumOf { it.amountAr }
        val paidOrdersFreight = transOrders.filter { it.isFreightPaid }.sumOf { it.freightCostAr }
        val paidAmount = maxOf(paidViaTx, paidOrdersFreight)
        val balanceDue = transitaireDueMap[t.id] ?: (totalInvoiced - paidAmount).coerceAtLeast(0L)
        val totalWeight = transOrders.sumOf { it.weightKg ?: 0.0 }
        val effectiveRate = if (totalWeight > 0.0 && totalInvoiced > 0L) (totalInvoiced / totalWeight).toLong() else 0L
        val sortedOrders = transOrders.sortedByDescending { it.orderDate }
        val latestOrder = sortedOrders.firstOrNull()
        val lastDate = latestOrder?.let { dateFmt.format(Date(it.orderDate)) } ?: ""
        val lastCode = latestOrder?.referenceCode?.ifBlank { "#CMD-${latestOrder.id}" } ?: ""
        val recentShipments = sortedOrders.take(6).map { ord ->
            val isDelivered = ord.status in listOf("DELIVERY_READY", "IN_STOCK", "COMPLETED", "STOCK", "DELIVERED", "SOLD", "ARRIVED")
            val statusLabel = when (ord.status) {
                "ORDERED" -> "Commandé"
                "WAREHOUSE" -> "Livraison Chine"
                "GUANGZHOU" -> "Entrepôt Chine"
                "FLIGHT" -> "En transit"
                "CUSTOMS" -> "Douane Mada"
                "QUALITY_CONTROL" -> "Contrôle qualité"
                "DELIVERY_READY", "IN_STOCK", "STOCK", "ARRIVED" -> "Arrivé Mada"
                "COMPLETED", "DELIVERED", "SOLD" -> "Livré / Vendu"
                else -> ord.status
            }
            val modeStr = if (ord.freightMode.equals("SEA", ignoreCase = true) || ord.freightMode.equals("MARITIME", ignoreCase = true)) {
                "SEA"
            } else "AIR"
            val orderStats = com.example.util.TransitStatsCalculator.calculateForOrder(ord)
            TransitaireShipmentSummaryUi(
                id = ord.id,
                code = ord.referenceCode.ifBlank { "#CMD-${ord.id}" },
                title = ord.product.ifBlank { "Colis #${ord.id}" },
                transportMode = modeStr,
                weightKg = ord.weightKg ?: 0.0,
                volumeCbm = ord.volumeM3 ?: 0.0,
                freightCostAr = ord.freightCostAr,
                statusLabel = statusLabel,
                isDelivered = isDelivered,
                dateLabel = dateFmt.format(Date(ord.orderDate)),
                fretTransitDays = orderStats.transitDays,
                totalTransitDays = orderStats.totalDays
            )
        }
        val dynamicHub = t.address.lines().firstOrNull()?.trim()?.take(38)?.ifBlank { null }
            ?: "Fret Chine → Madagascar"
        val isArchived = t.notes.startsWith("[ARCHIVED]")
        val resolvedAirRate = if (t.airNormalRateAr > 0L) t.airNormalRateAr else t.defaultTariffPerKg.coerceAtLeast(0L)
        val transHistory = historyByTransitaire[t.id].orEmpty()
        val transitStats = com.example.util.TransitStatsCalculator.aggregateForOrders(transOrders)

        CatalogueTransitaireSummaryUi(
            id = t.id,
            name = t.name,
            initials = inits,
            routeHub = dynamicHub,
            airRateAr = resolvedAirRate,
            airFastRateAr = t.airFastRateAr.coerceAtLeast(0L),
            seaRateAr = t.seaRateAr.coerceAtLeast(0L),
            airRateTrend = formatTrendFromHistory(transHistory, "AERIEN"),
            seaRateTrend = formatTrendFromHistory(transHistory, "MARITIME"),
            isAirCheapest = !isArchived && minActiveAirRate != null && resolvedAirRate == minActiveAirRate,
            contact = t.contact,
            chinaWarehouseAddress = t.address,
            rateApplicationDate = t.rateApplicationDate,
            internalNotes = t.notes.removePrefix("[ARCHIVED]").trim(),
            isArchived = isArchived,
            isActive = !isArchived,
            activeShipmentsCount = inTransitCount,
            lastShipmentDate = lastDate,
            lastShipmentCode = lastCode,
            totalInvoicedAr = totalInvoiced,
            paidAmountAr = paidAmount,
            balanceDueAr = balanceDue,
            effectiveRateAr = effectiveRate,
            airLeadDays = t.airNormalLeadDays,
            airFastLeadDays = t.airFastLeadDays,
            seaLeadDays = t.seaNormalLeadDays,
            goodsCategoryDelays = t.goodsCategoryDelays,
            recentShipments = recentShipments,
            rateHistory = transHistory,
            avgChinaDays = transitStats.avgChinaDays,
            avgHubWaitDays = transitStats.avgHubWaitDays,
            avgAirTransitDays = transitStats.avgAirTransitDays,
            avgSeaTransitDays = transitStats.avgSeaTransitDays,
            avgTotalDays = transitStats.avgTotalDays,
            measuredShipmentsCount = transitStats.measuredShipmentsCount,
            measuredAirCount = transitStats.measuredAirCount,
            measuredSeaCount = transitStats.measuredSeaCount
        )
    }

    return CatalogueMappingResult(
        activeCatalogueCount = activeCatalogueCount,
        hiddenCatalogueCount = hiddenCatalogueCount,
        catalogueFamilyPills = catalogueFamilyPills,
        filteredCatalogueOrders = filteredCatalogueOrders,
        allCatalogueArticles = allCatalogueArticles,
        visibleCatalogueArticles = visibleCatalogueArticles,
        catalogueFamiliesSummary = catalogueFamiliesSummary,
        cataloguePlatformsSummary = cataloguePlatformsSummary,
        catalogueTransitairesSummary = catalogueTransitairesSummary
    )
}
