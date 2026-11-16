package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1OrderSummaryCard(
    order: BusinessOrderEntity,
    familyEmoji: String,
    totalQty: Int,
    unitPriceYuan: Double,
    unitPriceAr: Long,
    shippingNationalYuan: Double,
    shippingNationalAr: Long,
    totalOrderYuan: Double,
    totalOrderAr: Long,
    colors: TransitaireColors,
    onEditOrder: () -> Unit,
    onEditDomesticShipping: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Récapitulatif Commande",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.cyanSoftBg
                    ) {
                        Text(
                            text = order.platform.ifBlank { "Achat Chine" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditOrder,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_edit_step1_order")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier la commande",
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.badgeNeutralBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = familyEmoji, fontSize = 32.sp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = order.product.ifBlank { "Article sans nom" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (order.referenceCode.isNotBlank()) "Réf : ${order.referenceCode}" else "Référence non définie",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = order.supplier.ifBlank { "Fournisseur non renseigné" },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantités commandées",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (totalQty > 0) "$totalQty unités" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (totalQty > 0) colors.cyanAccent else colors.dangerAccent
                        )
                    }

                    val parsedVariants = remember(order.variants) {
                        VariantColorUtils.parseVariants(order.variants)
                    }

                    if (parsedVariants.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedVariants.forEachIndexed { idx, variantText ->
                                val dotColor = VariantColorUtils.getVariantColor(variantText, idx)
                                val isLight = VariantColorUtils.isLightColor(dotColor)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.cardBg,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                                .then(
                                                    if (isLight) Modifier.border(0.5.dp, colors.border, CircleShape)
                                                    else Modifier
                                                )
                                        )
                                        Text(variantText, fontFamily = Inter, fontSize = 11.sp, color = colors.textPrimary)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (totalQty > 0) "Aucune variante spécifique ($totalQty pièce(s))" else "Aucune quantité renseignée",
                            fontFamily = Inter,
                            fontStyle = FontStyle.Italic,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prix unitaire usine", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (unitPriceYuan > 0) "¥ ${String.format(Locale.US, "%.2f", unitPriceYuan)}" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (unitPriceAr > 0) "~ ${CurrencyFormatter.formatAriary(unitPriceAr)}" else "—",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditDomesticShipping)
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Port intérieur (Chine)",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.badgeNeutralBg
                        ) {
                            Text(
                                text = "Modifier",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (shippingNationalYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", shippingNationalYuan)}" else "0 ¥ (Gratuit)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (shippingNationalAr > 0L) CurrencyFormatter.formatAriary(shippingNationalAr) else "0 Ar",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.badgeNeutralBg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Total commande",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (totalOrderYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = if (totalOrderAr > 0L) CurrencyFormatter.formatAriary(totalOrderAr) else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
