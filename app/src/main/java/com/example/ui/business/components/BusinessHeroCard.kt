package com.example.ui.business.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun BusinessHeroCard(
    uiState: BusinessUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF97316), // Orange
                        Color(0xFFD946EF), // Magenta
                        Color(0xFF7C3AED)  // Violet
                    )
                )
            )
            .padding(16.dp)
            .testTag("business_hero_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Comparison Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÉNÉFICE NET DU MOIS",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${uiState.vsPrevMonthPercent}% vs sept.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Big Number and subtext
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = CurrencyFormatter.formatAriary(uiState.netProfit),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = Color.White
                )
                Text(
                    text = "Net calculé après fret",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Mini-stats row in glassmorphism pills (3 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassMiniStat(
                    modifier = Modifier.weight(1f),
                    label = "Ventes",
                    value = CurrencyFormatter.formatAriary(uiState.salesTotal)
                )
                GlassMiniStat(
                    modifier = Modifier.weight(1f),
                    label = "Coût ventes",
                    value = CurrencyFormatter.formatAriary(uiState.salesCost)
                )
                GlassMiniStat(
                    modifier = Modifier.weight(1f),
                    label = "Fret & Dép.",
                    value = CurrencyFormatter.formatAriary(uiState.freightAndExpenses)
                )
            }

            // Segmented Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val marginWeight = uiState.marginSharePercent.toFloat()
                val exchangeWeight = uiState.exchangeSharePercent.toFloat()
                val freightWeight = uiState.freightSharePercent.toFloat()
                val totalWeight = marginWeight + exchangeWeight + freightWeight

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    if (totalWeight > 0f) {
                        if (marginWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(marginWeight)
                                    .fillMaxWidth()
                                    .background(Color.White)
                            )
                        }
                        if (exchangeWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(exchangeWeight)
                                    .fillMaxWidth()
                                    .background(Color(0xFF06B6D4))
                            )
                        }
                        if (freightWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(freightWeight)
                                    .fillMaxWidth()
                                    .background(Color(0xFFEADDFF))
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color.White, label = "Marge (${uiState.marginSharePercent}%)")
                    LegendItem(color = Color(0xFF06B6D4), label = "Change (${uiState.exchangeSharePercent}%)")
                    LegendItem(color = Color(0xFFEADDFF), label = "Fret (${uiState.freightSharePercent}%)")
                }
            }
        }
    }
}

@Composable
private fun GlassMiniStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.95f)
        )
    }
}
