package com.example.ui.business.sales

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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun SalesHeroPerformanceCard(uiState: BusinessUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF59E0B),
                        Color(0xFFEA580C),
                        Color(0xFF8A4CFC)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "CHIFFRE ENCAISSÉ",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = CurrencyFormatter.formatAriary(uiState.salesTotalCollectedAr).replace(" Ar", ""),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ar",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                val isGrowthPositive = uiState.salesGrowthVsPrevMonthPercent >= 0
                val growthText = if (isGrowthPositive) "+${uiState.salesGrowthVsPrevMonthPercent}%" else "${uiState.salesGrowthVsPrevMonthPercent}%"
                val growthTint = if (isGrowthPositive) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isGrowthPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = growthTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = growthText,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = growthTint
                            )
                        }
                        Text(
                            text = "vs ${uiState.prevMonthShortLabel.ifBlank { "mois préc." }}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bénéfice net total",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(uiState.salesNetProfitAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Répartition du profit",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "100%",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(1.5.dp)
                ) {
                    val prodW = uiState.salesProductMarginPercent.toFloat()
                    val exchW = uiState.salesExchangeGainPercent.toFloat()
                    val fretW = uiState.salesFreightMarginPercent.toFloat()
                    val sumW = prodW + exchW + fretW

                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (sumW > 0f) {
                            if (prodW > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(prodW)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFFDE047))
                                )
                            }
                            if (exchW > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(exchW)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFF67E8F9))
                                )
                            }
                            if (fretW > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(fretW)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFD8B4FE))
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFDE047)))
                        Text("Prod ${uiState.salesProductMarginPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF67E8F9)))
                        Text("Change ${uiState.salesExchangeGainPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFD8B4FE)))
                        Text("Fret ${uiState.salesFreightMarginPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Les ventes famille & amis sont isolées du CA fiscal",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
internal fun SalesMiniSummaryGrid(uiState: BusinessUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFDAD6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "À encaisser",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (uiState.salesRemainingToCollectAr >= 1_000_000L) {
                        "${uiState.salesRemainingToCollectAr / 1000}k Ar"
                    } else {
                        CurrencyFormatter.formatAriary(uiState.salesRemainingToCollectAr)
                    },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFBA1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Factures ouv.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.salesOpenInvoicesCount}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFF712AE2),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "En attente",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.salesPendingOrdersCount}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF712AE2)
                )
            }
        }
    }
}

@Composable
internal fun SalesDomainSwitcher(
    selectedDomain: String,
    onDomainSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val isBusiness = selectedDomain == "BUSINESS"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isBusiness) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onDomainSelected("BUSINESS") }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Business Pro",
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (isBusiness) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isBusiness) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val isFamily = selectedDomain == "FAMILY_FRIENDS"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isFamily) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onDomainSelected("FAMILY_FRIENDS") }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Famille & Amis",
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (isFamily) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isFamily) Color(0xFF712AE2) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
