package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun SuccessHeroCelebrationCard(order: BusinessOrderEntity) {
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
                            text = "Clôturé le ${DateUtils.formatShortDate(if (order.soldDate != null && order.soldDate > 0) order.soldDate else System.currentTimeMillis())}",
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
}

@Composable
internal fun SuccessStockIntegrationCard(
    order: BusinessOrderEntity,
    totalVendable: Int,
    v1Name: String,
    v2Name: String,
    v3Name: String,
    v1Count: Int,
    v2Count: Int,
    v3Count: Int,
    v4Count: Int,
    refundAr: Long
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuccessVariantTile(
                    name = v1Name,
                    count = v1Count,
                    dotColor = Color(0xFF131B2E),
                    dotBorderColor = Color(0xFFEAEDFF),
                    statusDotColor = Color(0xFF00687A),
                    statusLabel = "Prêt à la vente",
                    statusTextColor = Color(0xFF00687A),
                    modifier = Modifier.weight(1f)
                )
                SuccessVariantTile(
                    name = v2Name,
                    count = v2Count,
                    dotColor = Color.White,
                    dotBorderColor = Color(0xFFBCC9CD),
                    statusDotColor = Color(0xFF00687A),
                    statusLabel = "Prêt à la vente",
                    statusTextColor = Color(0xFF00687A),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuccessVariantTile(
                    name = v3Name,
                    count = v3Count,
                    dotColor = Color(0xFFEF4444),
                    dotBorderColor = null,
                    statusDotColor = Color(0xFF00687A),
                    statusLabel = "Prêt à la vente",
                    statusTextColor = Color(0xFF00687A),
                    modifier = Modifier.weight(1f)
                )
                SuccessVariantTile(
                    name = "Rouge Sport",
                    count = v4Count,
                    dotColor = Color(0xFFF59E0B),
                    dotBorderColor = null,
                    statusDotColor = Color(0xFFF59E0B),
                    statusLabel = "Déstockage / Prix réduit",
                    statusTextColor = Color(0xFF3D494C),
                    statusFontSizeSp = 9,
                    modifier = Modifier.weight(1f)
                )
            }

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
                                "Remboursement usine enregistré : ¥ ${String.format(Locale.US, "%.1f", order.refundYuan)} (${CurrencyFormatter.formatAriary(refundAr)})."
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
}

@Composable
private fun SuccessVariantTile(
    name: String,
    count: Int,
    dotColor: Color,
    dotBorderColor: Color?,
    statusDotColor: Color,
    statusLabel: String,
    statusTextColor: Color,
    statusFontSizeSp: Int = 10,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
                    val dotMod = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .let { base ->
                            if (dotBorderColor != null) base.border(1.dp, dotBorderColor, CircleShape) else base
                        }
                    Box(modifier = dotMod)
                    Text(
                        text = name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                }
                Text(
                    text = "$count",
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
                        .background(statusDotColor)
                )
                Text(
                    text = statusLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = statusFontSizeSp.sp,
                    color = statusTextColor,
                    maxLines = 1
                )
            }
        }
    }
}
