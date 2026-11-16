package com.example.ui.business.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SaleInvoiceCard(
    sale: BusinessSaleEntity,
    onClick: () -> Unit,
    onWhatsAppRelance: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
    val progress = if (sale.totalPriceAr > 0L) {
        (sale.paidAmountAr.toFloat() / sale.totalPriceAr.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val isOverdue = !sale.isFullyPaid && sale.dueDate < System.currentTimeMillis()
    val overdueDays = if (isOverdue) {
        ((System.currentTimeMillis() - sale.dueDate) / 86400000L).coerceAtLeast(1L)
    } else 0L

    val marginAr = (sale.totalPriceAr - (sale.unitCostAr * sale.quantity)).coerceAtLeast(0L)
    val initials = sale.clientName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        .ifBlank { "CL" }

    val avatarBg = when ((initials.hashCode() % 3 + 3) % 3) {
        0 -> Color(0xFF8A4CFC).copy(alpha = 0.15f)
        1 -> CyanPrimary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val avatarTextColor = when ((initials.hashCode() % 3 + 3) % 3) {
        0 -> Color(0xFF712AE2)
        1 -> CyanPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = avatarTextColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = sale.clientName,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (sale.invoiceNumber.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = sale.invoiceNumber,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date(sale.saleDate))
                        Text(
                            text = "$dateStr • ${sale.productName}",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatAriary(sale.totalPriceAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (marginAr >= 1000) "Marge +${marginAr / 1000}k" else "Marge +$marginAr",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        sale.isFullyPaid -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Intégralement réglé (${CurrencyFormatter.formatAriary(sale.paidAmountAr)})",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                            Text(
                                text = "100%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF059669)
                            )
                        }
                        sale.paidAmountAr > 0L -> {
                            Text(
                                text = "Réglé : ${CurrencyFormatter.formatAriary(sale.paidAmountAr)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "Reste : ${CurrencyFormatter.formatAriary(remaining)}",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = "Réglé : 0 Ar",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Reste : ${CurrencyFormatter.formatAriary(remaining)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val gaugeColor = when {
                        sale.isFullyPaid -> Color(0xFF10B981)
                        sale.paidAmountAr > 0L -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(gaugeColor)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (statusLabel, statusBg, statusColor) = when {
                        sale.isFullyPaid -> Triple("Réglée", Color(0xFFDCFCE7), Color(0xFF166534))
                        sale.paidAmountAr > 0L -> Triple("Partielle", Color(0xFFFEF3C7), Color(0xFF92400E))
                        else -> Triple("Impayée", Color(0xFFFFE4E6), Color(0xFF9F1239))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }

                    val isOrder = sale.saleSource == "ORDER"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isOrder) Color(0xFF8A4CFC).copy(alpha = 0.12f) else CyanPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isOrder) "Sur commande" else "Sur stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (isOrder) Color(0xFF712AE2) else CyanPrimary
                        )
                    }

                    if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFFFDAD6))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "En retard de ${overdueDays}j",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFBA1A1A)
                                )
                            }
                        }
                    }
                }

                if (!sale.isFullyPaid && (isOverdue || sale.paidAmountAr == 0L)) {
                    Button(
                        onClick = onWhatsAppRelance,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.successAccent),
                        shape = RoundedCornerShape(9999.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Relancer WhatsApp",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PendingCustomerOrderCard(
    order: BusinessOrderEntity,
    onViewClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val isInTransit = order.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL")
    val isArrived = order.status in listOf("DELIVERY_READY", "IN_STOCK", "COMPLETED")
    val isReady = isInTransit || isArrived
    val badgeLabel = when (order.status) {
        "DELIVERY_READY", "IN_STOCK", "COMPLETED" -> "Disponible"
        "QUALITY_CONTROL" -> "Contrôle qualité"
        "CUSTOMS" -> "En douane"
        "FLIGHT" -> {
            if (order.estimatedArrival > 0L) {
                val days = ((order.estimatedArrival - System.currentTimeMillis()) / 86400000L).coerceAtLeast(0L)
                if (days == 0L) "Arrivée imminente" else "Dans ${days}j"
            } else "En vol"
        }
        "GUANGZHOU" -> "Entrepôt Chine"
        "WAREHOUSE" -> "Vers entrepôt"
        "ORDERED" -> "Commandé"
        else -> "Action requise"
    }
    val stageSubtitle = when (order.status) {
        "DELIVERY_READY", "IN_STOCK", "COMPLETED" -> "Achat lié • Prêt pour remise"
        "QUALITY_CONTROL", "CUSTOMS" -> "Achat lié • Arrivé Madagascar"
        "FLIGHT" -> "Achat lié • En vol (${order.carrier.ifBlank { "Fret" }})"
        "GUANGZHOU", "WAREHOUSE", "ORDERED" -> "Achat lié • Chine → Mada"
        else -> "En attente d'achat"
    }

    Box(
        modifier = Modifier
            .width(285.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIENT • ${order.assignedClient.ifBlank { "Client non assigné" }.uppercase()}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isReady) colors.violetSoftBg else colors.warningSoftBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = if (isReady) colors.violetAccent else colors.warningAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when {
                            order.imageUrl == "earbuds" || order.product.contains("Écouteurs", ignoreCase = true) -> Icons.Default.Headphones
                            order.imageUrl == "smartwatch" || order.product.contains("Montre", ignoreCase = true) -> Icons.Default.Watch
                            else -> Icons.Default.ShoppingCartCheckout
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.product,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${order.stockCount.coerceAtLeast(order.quantityBought)} pcs • ${order.variants.ifBlank { "Standard" }}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prix convenu",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${CurrencyFormatter.formatAriary(order.resalePriceAr)} / u",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isReady) Icons.Default.FlightLand else Icons.Default.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = if (isReady) Color(0xFF712AE2) else Color(0xFFB45309),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stageSubtitle,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isReady) colors.violetAccent else colors.warningAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onViewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Color(0xFF8A4CFC).copy(alpha = 0.15f) else Color(0xFFF59E0B),
                    contentColor = if (isReady) Color(0xFF712AE2) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Default.Visibility else Icons.Default.AddLink,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isReady) "Voir l'achat lié" else "Relier à un achat",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
