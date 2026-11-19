package com.example.ui.business.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.entity.BusinessCategoryEntity
import com.example.data.local.entity.BusinessTransactionEntity

internal data class BusinessCategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

internal data class BusinessCategoryColorOption(
    val key: String,
    val label: String
)

internal val BUSINESS_CATEGORY_ICONS: List<BusinessCategoryIconOption> = listOf(
    BusinessCategoryIconOption("storefront", "Ventes", Icons.Default.Storefront),
    BusinessCategoryIconOption("payments", "Paiement", Icons.Default.Payments),
    BusinessCategoryIconOption("local_shipping", "Fret", Icons.Default.LocalShipping),
    BusinessCategoryIconOption("directions_car", "Transport", Icons.Default.DirectionsCar),
    BusinessCategoryIconOption("inventory_2", "Stock", Icons.Default.Inventory2),
    BusinessCategoryIconOption("shopping_bag", "Achats", Icons.Default.ShoppingBag),
    BusinessCategoryIconOption("campaign", "Pub", Icons.Default.Campaign),
    BusinessCategoryIconOption("bolt", "Charges", Icons.Default.Bolt),
    BusinessCategoryIconOption("receipt_long", "Facture", Icons.Default.ReceiptLong),
    BusinessCategoryIconOption("trending_up", "Revenu", Icons.AutoMirrored.Filled.TrendingUp),
    BusinessCategoryIconOption("replay", "Retour", Icons.Default.Replay),
    BusinessCategoryIconOption("build", "Matériel", Icons.Default.Build),
    BusinessCategoryIconOption("phone_android", "Télécom", Icons.Default.PhoneAndroid),
    BusinessCategoryIconOption("restaurant", "Repas", Icons.Default.Restaurant),
    BusinessCategoryIconOption("card_giftcard", "Cadeau", Icons.Default.CardGiftcard),
    BusinessCategoryIconOption("account_balance", "Banque", Icons.Default.AccountBalance)
)

internal val BUSINESS_CATEGORY_COLORS: List<BusinessCategoryColorOption> = listOf(
    BusinessCategoryColorOption("CYAN", "Cyan"),
    BusinessCategoryColorOption("VIOLET", "Violet"),
    BusinessCategoryColorOption("GREEN", "Vert"),
    BusinessCategoryColorOption("ORANGE", "Orange"),
    BusinessCategoryColorOption("ROSE", "Rose"),
    BusinessCategoryColorOption("BLUE", "Bleu")
)

internal fun resolveCategoryIcon(iconKey: String): ImageVector {
    return BUSINESS_CATEGORY_ICONS.firstOrNull { it.key == iconKey }?.icon
        ?: Icons.Default.ReceiptLong
}

internal fun resolveCategoryColors(colorKey: String, colors: TransitaireColors): Pair<Color, Color> {
    return when (colorKey.uppercase()) {
        "VIOLET" -> colors.violetAccent to colors.violetSoftBg
        "GREEN" -> colors.successAccent to colors.successSoftBg
        "ORANGE" -> colors.warningAccent to colors.warningSoftBg
        "ROSE" -> colors.roseAccent to colors.roseSoftBg
        "BLUE" -> colors.expressAccent to colors.expressSoftBg
        else -> colors.cyanAccent to colors.cyanSoftBg
    }
}

internal fun resolveTransactionCategory(
    tx: BusinessTransactionEntity,
    categories: List<BusinessCategoryEntity>
): BusinessCategoryEntity? {
    tx.categoryId?.let { cid ->
        categories.firstOrNull { it.id == cid }?.let { return it }
    }
    if (tx.referenceTag.isNotBlank()) {
        val cleanTag = tx.referenceTag.substringBefore(" · ").trim()
        categories.firstOrNull { it.name.equals(cleanTag, ignoreCase = true) }?.let { return it }
    }
    return null
}
