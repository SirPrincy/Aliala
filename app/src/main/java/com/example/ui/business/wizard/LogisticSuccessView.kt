package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator

@Composable
fun LogisticSuccessView(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onDismiss: () -> Unit,
    onViewStock: () -> Unit,
    onCreateSale: () -> Unit,
    onReorder: () -> Unit
) {
    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val totalQty = breakdown.quantity
    val totalVendable = if (order.stockCount > 0) order.stockCount else totalQty
    val freightCostAr = breakdown.freightCostAr
    val refundAr = breakdown.refundAr
    val totalNetExpensesAr = breakdown.netCostAr
    val unitFifoCostAr = if (totalVendable > 0) totalNetExpensesAr / totalVendable else breakdown.unitCostAr
    val recommendedPriceAr = if (order.resalePriceAr > 0) order.resalePriceAr else 0L

    val parsedVariants = remember(order.variants, order.product) {
        val list = VariantColorUtils.parseVariants(order.variants)
        if (list.isNotEmpty()) list else listOf("Standard")
    }
    val v1Name = parsedVariants.getOrElse(0) { "Variante 1" }
    val v2Name = parsedVariants.getOrElse(1) { "Variante 2" }
    val v3Name = parsedVariants.getOrElse(2) { "Variante 3" }

    val v1Count = (totalVendable * 0.5).toInt().coerceAtLeast(1)
    val v2Count = (totalVendable * 0.25).toInt().coerceAtLeast(1)
    val v3Count = (totalVendable - v1Count - v2Count).coerceAtLeast(0)
    val v4Count = 0

    val marginPerUnitAr = if (recommendedPriceAr > 0) recommendedPriceAr - unitFifoCostAr else 0L
    val marginPercent = if (unitFifoCostAr > 0 && recommendedPriceAr > 0) {
        ((marginPerUnitAr.toDouble() / unitFifoCostAr.toDouble()) * 100).toInt()
    } else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00687A).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Importation Réussie",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF131B2E)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAEDFF))
                    .testTag("btn_close_success_wizard")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer le wizard",
                    tint = Color(0xFF3D494C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        SuccessHeroCelebrationCard(order = order)

        SuccessStockIntegrationCard(
            order = order,
            totalVendable = totalVendable,
            v1Name = v1Name,
            v2Name = v2Name,
            v3Name = v3Name,
            v1Count = v1Count,
            v2Count = v2Count,
            v3Count = v3Count,
            v4Count = v4Count,
            refundAr = refundAr
        )

        SuccessFinancialSummaryCard(
            order = order,
            unitFifoCostAr = unitFifoCostAr,
            recommendedPriceAr = recommendedPriceAr,
            marginPercent = marginPercent,
            marginPerUnitAr = marginPerUnitAr,
            totalNetExpensesAr = totalNetExpensesAr,
            freightCostAr = freightCostAr
        )

        SuccessProductPreviewCard(order = order)

        SuccessActionButtonsColumn(
            onViewStock = onViewStock,
            onCreateSale = onCreateSale,
            onReorder = onReorder
        )
    }
}

@Composable
private fun SuccessFinancialSummaryCard(
    order: BusinessOrderEntity,
    unitFifoCostAr: Long,
    recommendedPriceAr: Long,
    marginPercent: Int,
    marginPerUnitAr: Long,
    totalNetExpensesAr: Long,
    freightCostAr: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8A4CFC).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Bilan Financier Définitif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Calcul FIFO post-logistique",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFF8A4CFC).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "CMUP FIXÉ",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF712AE2),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F3FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Coût d'achat unitaire net",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(unitFifoCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Prix conseillé",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(recommendedPriceAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF00687A)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Marge brute prévisionnelle",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "+$marginPercent%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF00687A)
                        )
                        Text(
                            text = "(+${CurrencyFormatter.formatAriary(marginPerUnitAr).replace(" Ar", "")} Ar / pce)",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dépense globale nette du lot", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF3D494C))
                    Text(CurrencyFormatter.formatAriary(totalNetExpensesAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fret ${order.carrier.ifBlank { "transitaire" }} ${if (order.isFreightPaid) "réglé" else "prévu"}", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF3D494C))
                    Text(CurrencyFormatter.formatAriary(freightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Formule appliquée", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF3D494C))
                    Surface(shape = RoundedCornerShape(9999.dp), color = Color(0xFFEAEDFF)) {
                        Text(
                            text = "Marchandise + Port + Fret - Remboursement",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessProductPreviewCard(order: BusinessOrderEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF131B2E), Color(0xFF00424F)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.product.ifBlank { "Article #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF131B2E)
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = order.supplier.ifBlank { "Fournisseur Chine" },
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = Color(0xFF3D494C)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEAEDFF)) {
                        Text(
                            text = "SKU: ${order.referenceCode.ifBlank { "SKU-${order.id}" }}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEAEDFF)) {
                        Text(
                            text = "Transitaire: ${order.carrier.ifBlank { "Transitaire" }}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessActionButtonsColumn(
    onViewStock: () -> Unit,
    onCreateSale: () -> Unit,
    onReorder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onViewStock,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_view_updated_stock"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    text = "📦 Voir le stock mis à jour",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onCreateSale)
                .testTag("btn_create_first_sale"),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(20.dp))
                    Text(
                        text = "💰 Créer une première vente",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF712AE2)
                    )
                }
            }
        }

        TextButton(
            onClick = onReorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_reorder_item"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Cached, contentDescription = null, tint = Color(0xFF3D494C), modifier = Modifier.size(18.dp))
                Text(
                    text = "🔄 Racheter cet article (Nouveau lot 1688)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            }
        }
    }
}
