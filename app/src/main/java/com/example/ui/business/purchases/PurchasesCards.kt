package com.example.ui.business.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun PurchaseDetailedCard(
    order: BusinessOrderEntity,
    availableYuans: Double,
    activeYuanNames: List<String>,
    onOrderClick: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onQualityControlClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val isDraft = order.status == "DRAFT"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (isDraft) Modifier.border(1.dp, colors.border, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onOrderClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (order.imageUrl) {
                            "smartwatch" -> Icons.Default.Watch
                            "bag" -> Icons.Default.ShoppingBag
                            "cable" -> Icons.Default.Cable
                            "case" -> Icons.Default.PhoneAndroid
                            "bottle" -> Icons.Default.WaterDrop
                            else -> Icons.Default.Headphones
                        },
                        contentDescription = null,
                        tint = when (order.platform) {
                            "Taobao" -> Color(0xFFEA580C)
                            "Pinduoduo" -> Color(0xFFE11D48)
                            else -> Color(0xFFDC2626)
                        },
                        modifier = Modifier.size(30.dp)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                when (order.platform) {
                                    "Taobao" -> Color(0xFFEA580C)
                                    "Pinduoduo" -> Color(0xFFE11D48)
                                    else -> Color(0xFFDC2626)
                                }
                            )
                            .padding(vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.platform,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.product,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        val (catBg, catText, catLabel) = when (order.category) {
                            "PERSONAL" -> Triple(colors.warningSoftBg, colors.warningAccent, "Pour moi")
                            "FAMILY_FRIENDS" -> Triple(colors.violetSoftBg, colors.violetAccent, "Famille & amis")
                            else -> Triple(colors.cyanSoftBg, colors.cyanAccent, "Business")
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(catBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = catLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = catText
                            )
                        }
                    }

                    Text(
                        text = "Réf: ${order.referenceCode.ifBlank { "LOT-${order.id}" }}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.variants.ifBlank { "Quantité : ${order.quantityBought}" },
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Total: ${order.quantityBought} pcs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            val unitAr = (order.priceYuan * order.exchangeRate).toLong()
            val domesticAr = (order.domesticShippingYuan * order.exchangeRate).toLong()
            val totalAr = if (order.unitCostAr > 0L) {
                order.unitCostAr * order.quantityBought
            } else {
                (unitAr * order.quantityBought + domesticAr + order.freightCostAr + order.customsTaxAr)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "¥ ${order.priceYuan.toInt()} × ${order.exchangeRate.toInt()} Ar = ${CurrencyFormatter.formatAriary(unitAr)}/u",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = CurrencyFormatter.formatAriary(totalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isDraft) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(¥ ${(order.priceYuan * order.quantityBought).toInt()})",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        when (order.status) {
                            "DRAFT" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.badgeNeutralBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Brouillon",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            "ORDERED" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.cyanSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Commandé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                                Text(
                                    text = "Fournisseur validé",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.cyanAccent
                                )
                            }
                            "WAREHOUSE" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.cyanSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Livraison Chine",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                                Text(
                                    text = "Vers entrepôt",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.cyanAccent
                                )
                            }
                            "GUANGZHOU" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.violetSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Arrivé entrepôt",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.violetAccent
                                    )
                                }
                                Text(
                                    text = "Prêt au départ",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.violetAccent
                                )
                            }
                            "FLIGHT" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.violetSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "En transit (Étape 4/6)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.violetAccent
                                    )
                                }
                                Text(
                                    text = order.carrier.ifBlank { "En cours" },
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.violetAccent
                                )
                            }
                            "CUSTOMS" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.warningSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Dédouanement",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.warningAccent
                                    )
                                }
                                Text(
                                    text = "Arrivé Mada",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.warningAccent
                                )
                            }
                            "QUALITY_CONTROL" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.dangerSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Contrôle qualité",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.dangerAccent
                                    )
                                }
                                Text(
                                    text = "Arrivé Tana",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = colors.dangerAccent
                                )
                            }
                            "IN_STOCK", "DELIVERY_READY" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.successSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "En stock",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.successAccent
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.successSoftBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Terminé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = colors.successAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (order.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT")) {
                val filledSteps = when (order.status) {
                    "ORDERED" -> 1
                    "WAREHOUSE" -> 2
                    "GUANGZHOU" -> 3
                    "FLIGHT" -> 4
                    else -> 2
                }
                val stepColor = if (order.status == "FLIGHT") colors.violetAccent else colors.cyanAccent

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..6) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(if (i <= filledSteps) stepColor else colors.border)
                        )
                    }
                }
            }

            if (order.status == "QUALITY_CONTROL") {
                Button(
                    onClick = onQualityControlClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.violetSoftBg,
                        contentColor = colors.violetAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "À contrôler (${order.quantityBought} unités restantes)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (isDraft) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.warningSoftBg)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.warningAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        val shortage = (order.priceYuan * order.quantityBought - availableYuans).coerceAtLeast(0.0).toInt()
                        Text(
                            text = if (shortage > 0) "Yuans insuffisants : il manque ¥ $shortage sur le compte." else "Brouillon prêt à payer avec le solde dispo.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                val yuanTargetsLabel = if (activeYuanNames.isNotEmpty()) {
                    activeYuanNames.joinToString(" / ")
                } else {
                    "Portefeuille ¥"
                }
                Button(
                    onClick = onBuyYuansClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.warningAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "+ Acheter des yuans ($yuanTargetsLabel)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (order.status == "FLIGHT") {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (order.status == "GUANGZHOU") {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (order.status == "ORDERED") {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = order.carrier.ifBlank { "Non attribué" },
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (order.assignedClient.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.successSoftBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.successAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = order.assignedClient,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = colors.successAccent
                            )
                        }
                    }
                } else if (order.weightKg != null) {
                    Text(
                        text = if (order.status == "QUALITY_CONTROL") "Poids réel: ${order.weightKg} kg" else "Poids pesé: ${order.weightKg} kg",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (order.lotNumber.isNotBlank()) {
                    Text(
                        text = order.lotNumber,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
