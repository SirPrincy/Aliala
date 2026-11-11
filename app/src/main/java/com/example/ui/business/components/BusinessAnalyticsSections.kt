package com.example.ui.business.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun PurchasesDonutSection(
    uiState: BusinessUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .testTag("purchases_donut_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Répartition des achats",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Total : ${CurrencyFormatter.formatAriary(uiState.totalPurchasesAr)}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val arcRadius = (size.minDimension - strokeWidth) / 2f
                        val centerOffset = center

                        // Background track
                        drawCircle(
                            color = Color(0xFFEAEDFF),
                            radius = arcRadius,
                            center = centerOffset,
                            style = Stroke(width = strokeWidth)
                        )

                        // Slices: Business (75% = 270 deg), Personal (15% = 54 deg), Family (10% = 36 deg)
                        val startAngle = -90f
                        val businessSweep = 360f * (uiState.businessPurchasesPercent / 100f)
                        val personalSweep = 360f * (uiState.personalPurchasesPercent / 100f)
                        val familySweep = 360f * (uiState.familyPurchasesPercent / 100f)

                        // Business slice (Orange)
                        drawArc(
                            color = Color(0xFFF97316),
                            startAngle = startAngle,
                            sweepAngle = businessSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Personal slice (Violet)
                        drawArc(
                            color = Color(0xFF7C3AED),
                            startAngle = startAngle + businessSweep,
                            sweepAngle = personalSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Family slice (Cyan)
                        drawArc(
                            color = Color(0xFF06B6D4),
                            startAngle = startAngle + businessSweep + personalSweep,
                            sweepAngle = familySweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CHINE",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "100%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Donut Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DonutLegendRow(
                        color = Color(0xFFF97316),
                        title = "Business (${uiState.businessPurchasesPercent}%)",
                        amountAr = uiState.businessPurchasesAr
                    )
                    DonutLegendRow(
                        color = Color(0xFF7C3AED),
                        title = "Pour moi (${uiState.personalPurchasesPercent}%)",
                        amountAr = uiState.personalPurchasesAr
                    )
                    DonutLegendRow(
                        color = Color(0xFF06B6D4),
                        title = "Famille & amis (${uiState.familyPurchasesPercent}%)",
                        amountAr = uiState.familyPurchasesAr
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutLegendRow(
    color: Color,
    title: String,
    amountAr: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = CurrencyFormatter.formatAriary(amountAr),
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FamilyServiceCard(
    profitAr: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
            .testTag("family_service_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diversity3,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Service Proches & Famille",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "+${CurrencyFormatter.formatAriary(profitAr)}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF7C3AED)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Achats groupés sans impacter la trésorerie commerciale. Gains de change et commissions transparentes isolés.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun AiInsightCard(
    uiState: BusinessUiState,
    onGenerateWhatsAppClick: () -> Unit,
    onNextInsightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(16.dp)
            .testTag("ai_insight_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val insightTitle = when (uiState.currentInsightIndex) {
                1 -> "Insight IA Trésorerie & Change"
                2 -> "Insight IA Stock & Rotation"
                else -> "Insight IA Fret & Logistique"
            }

            val insightText = when (uiState.currentInsightIndex) {
                1 -> "Gain de change estimé ce mois : ${CurrencyFormatter.formatAriary(uiState.exchangeGainAr)} (CMUP moyen : ${uiState.cmupRate} Ar/¥). " +
                        if (uiState.overdueInvoicesCount > 0) "Attention : ${uiState.overdueInvoicesCount} factures sont en retard de paiement (>15j) pour un total de ${CurrencyFormatter.formatAriary(uiState.overdueInvoicesAmount)}."
                        else "Toutes vos créances clients sont à jour."
                2 -> "Valeur totale en stock : ${CurrencyFormatter.formatAriary(uiState.totalStockValueAr)} (${uiState.availableStockPieces} pcs disponibles). " +
                        if (uiState.topProducts.isNotEmpty()) "Meilleure vente : ${uiState.topProducts.first().name} avec ${uiState.topProducts.first().unitsSold} pcs écoules."
                        else "Aucune vente enregistrée pour le moment."
                else -> "Fret et charges en attente : ${CurrencyFormatter.formatAriary(uiState.freightDueAmount)}" +
                        (if (uiState.freightDueDays > 0) " (échéance dans ${uiState.freightDueDays} j). " else ". ") +
                        if (uiState.inRouteAmountAr > 0) "${CurrencyFormatter.formatAriary(uiState.inRouteAmountAr)} de marchandises actuellement en transit."
                        else "Aucune marchandise en transit."
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = insightTitle,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNextInsightClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (uiState.currentInsightIndex == 1) CyanPrimary else Color(0xFFCBD5E1))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (uiState.currentInsightIndex == 2) CyanPrimary else Color(0xFFCBD5E1))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (uiState.currentInsightIndex == 3) CyanPrimary else Color(0xFFCBD5E1))
                    )
                }
            }

            Text(
                text = insightText,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onGenerateWhatsAppClick() }
                ) {
                    Text(
                        text = "Générer relances WhatsApp",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "${uiState.currentInsightIndex} sur ${uiState.totalInsightsCount}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
