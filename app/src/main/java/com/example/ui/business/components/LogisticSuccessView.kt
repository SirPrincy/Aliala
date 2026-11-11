package com.example.ui.business.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReportProblem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import com.example.data.local.entity.BusinessOrderEntity
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
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val unitPriceYuan = breakdown.unitPriceYuan
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val freightCostAr = breakdown.freightCostAr
    val refundAr = breakdown.refundAr
    val totalNetExpensesAr = breakdown.netCostAr
    val unitFifoCostAr = if (totalVendable > 0) totalNetExpensesAr / totalVendable else breakdown.unitCostAr
    val recommendedPriceAr = if (order.resalePriceAr > 0) order.resalePriceAr else 0L

    val parsedVariants = remember(order.variants) {
        val list = order.variants.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list else listOf("Noir Pro", "Blanc Pur", "Rouge Sport")
    }
    val v1Name = parsedVariants.getOrElse(0) { "Variante 1" }
    val v2Name = parsedVariants.getOrElse(1) { "Variante 2" }
    val v3Name = parsedVariants.getOrElse(2) { "Variante 3" }

    val v1Count = (totalVendable * 0.5).toInt().coerceAtLeast(1)
    val v2Count = (totalVendable * 0.25).toInt().coerceAtLeast(1)
    val v3Count = (totalVendable - v1Count - v2Count).coerceAtLeast(0)
    val v4Count = 0

    val marginPerUnitAr = if (recommendedPriceAr > 0) recommendedPriceAr - unitFifoCostAr else 0L
    val marginPercent = if (unitFifoCostAr > 0 && recommendedPriceAr > 0) ((marginPerUnitAr.toDouble() / unitFifoCostAr.toDouble()) * 100).toInt() else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP DISMISS BAR
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

        // HERO CELEBRATION CARD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                // Subtle decorative background bubbles
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 180.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4).copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-30).dp, y = 100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF712AE2).copy(alpha = 0.06f))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Celebration Checkmark Icon with Sparkles
                    Box(
                        modifier = Modifier.padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00687A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Text(
                            text = "✨",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-6).dp)
                        )
                        Text(
                            text = "🎉",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-6).dp, y = 6.dp)
                        )
                    }

                    Text(
                        text = "Achat Terminé avec Succès !",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF131B2E),
                        textAlign = TextAlign.Center
                    )

                    // Subtitle with lot pill
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Le lot ",
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color(0xFF3D494C)
                            )
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = Color(0xFF06B6D4).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = order.lotNumber.ifBlank { "Lot #${order.id}" },
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00687A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = " de ${order.product.ifBlank { "Article #${order.id}" }}",
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                        Text(
                            text = "est maintenant enregistré dans le stock actif.",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = Color(0xFF3D494C),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Closing Meta Info Badge
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFF2F3FF),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Clôturé le ${com.example.util.DateUtils.formatShortDate(if (order.soldDate != null && order.soldDate > 0) order.soldDate else System.currentTimeMillis())}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }
            }
        }

        // STOCK INTEGRATION SUMMARY BENTO CARD
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
                // Header
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
                                .background(Color(0xFF06B6D4).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Unités Intégrées",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Inventaire physique réceptionné",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$totalVendable",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color(0xFF00687A),
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "VENDABLES",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Stock Visual Mini Grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item 1: Noir Titane (10)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF131B2E))
                                            .border(1.dp, Color(0xFFEAEDFF), CircleShape)
                                    )
                                    Text(
                                        text = v1Name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Text(
                                    text = "$v1Count",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A))
                                )
                                Text(
                                    text = "Prêt à la vente",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }
                    }

                    // Item 2: Blanc Pur (5)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFBCC9CD), CircleShape)
                                    )
                                    Text(
                                        text = v2Name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Text(
                                    text = "$v2Count",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A))
                                )
                                Text(
                                    text = "Prêt à la vente",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item 3: Rouge Sport
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Text(
                                        text = v3Name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Text(
                                    text = "$v3Count",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A))
                                )
                                Text(
                                    text = "Prêt à la vente",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }
                    }

                    // Item 4: Rouge Sport Déstockage
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text(
                                        text = "Rouge Sport",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Text(
                                    text = "$v4Count",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                )
                                Text(
                                    text = "Déstockage / Prix réduit",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFF3D494C),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Loss & Claim Resolution Callout
                val hasAnomalies = order.lostCount > 0 || order.defectiveCount > 0 || order.refundYuan > 0.0
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasAnomalies) Color(0xFFE2E7FF).copy(alpha = 0.6f) else Color(0xFF00687A).copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (hasAnomalies) Color(0xFFFFDAD6) else Color(0xFFACEDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasAnomalies) Icons.Default.ReportProblem else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasAnomalies) Color(0xFF93000A) else Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (hasAnomalies) {
                                    val lostStr = if (order.lostCount > 0) "${order.lostCount} unité(s) perdue(s)" else ""
                                    val defStr = if (order.defectiveCount > 0) "${order.defectiveCount} non vendable(s)" else ""
                                    listOf(lostStr, defStr).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Rapport d'inspection" }
                                } else {
                                    "100% des unités conformes"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = if (order.refundYuan > 0.0) {
                                    "Remboursement usine enregistré : ¥ ${String.format(java.util.Locale.US, "%.1f", order.refundYuan)} (${CurrencyFormatter.formatAriary(refundAr)})."
                                } else if (hasAnomalies) {
                                    "Aucun avoir/remboursement réclamé."
                                } else {
                                    "Stock prêt à la vente sans anomalie."
                                },
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }
            }
        }

        // FINANCIAL SUMMARY CARD (BILAN FINANCIER DÉFINITIF)
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
                // Header
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

                // Highlighted Metric Box
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

                // Projected Margin Ribbon
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

                // Financial Breakdown Rows
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dépense globale nette du lot",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF3D494C)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalNetExpensesAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fret SinoExpress réglé",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF3D494C)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(freightCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formule appliquée",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF3D494C)
                        )
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFEAEDFF)
                        ) {
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

        // PRODUCT PREVIEW & RECEIPT TAG
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
                // Product Thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF131B2E), Color(0xFF00424F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Info
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEAEDFF)
                        ) {
                            Text(
                                 text = "SKU: ${order.referenceCode.ifBlank { "SKU-${order.id}" }}",
                                 fontFamily = PlusJakartaSans,
                                 fontSize = 9.sp,
                                 color = Color(0xFF3D494C),
                                 modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEAEDFF)
                        ) {
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

        // DIRECT POST-IMPORT ACTIONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Action: Voir le stock mis à jour
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
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "📦 Voir le stock mis à jour",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // Secondary Action: Créer une première vente
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onCreateSale() }
                    .testTag("btn_create_first_sale"),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(20.dp)
                        )
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

            // Tertiary Action: Racheter cet article
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
                    Icon(
                        imageVector = Icons.Default.Cached,
                        contentDescription = null,
                        tint = Color(0xFF3D494C),
                        modifier = Modifier.size(18.dp)
                    )
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
}
