package com.example.ui.business.finances

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun FinancesMonthHeaderBar(
    selectedMonthLabel: String,
    showTxSearchBar: Boolean,
    showTxAccountFilter: Boolean,
    cyanColor: Color,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleAccountFilter: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mois précédent",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = cyanColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedMonthLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mois suivant",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (showTxSearchBar) cyanColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable(onClick = onToggleSearch),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher une transaction",
                        tint = if (showTxSearchBar) cyanColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (showTxAccountFilter) cyanColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable(onClick = onToggleAccountFilter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtrer par compte",
                        tint = if (showTxAccountFilter) cyanColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FinancesTreasuryHeroCard(
    uiState: BusinessUiState,
    totalLiquidityAr: Long,
    totalTreasuryAr: Long,
    ariaryAccountCount: Int,
    yuanAccountCount: Int,
    cashTransactionCount: Int,
    totalTxInAr: Long,
    totalTxOutAr: Long,
    netTxFlowAr: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEA580C),
                            Color(0xFFC026D3),
                            Color(0xFF712AE2)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRÉSORERIE TOTALE (Ar + ¥)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val yuanHeroFmt = if (uiState.availableYuans % 1.0 == 0.0) {
                                "${uiState.availableYuans.toInt()}"
                            } else {
                                String.format(Locale.US, "%.2f", uiState.availableYuans)
                            }
                            Text(
                                text = "$cashTransactionCount op. • ¥ $yuanHeroFmt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = CurrencyFormatter.formatAriary(totalLiquidityAr).replace(" Ar", ""),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ar",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "Ariary : ${CurrencyFormatter.formatAriary(totalTreasuryAr)} ($ariaryAccountCount cpte) • Yuans : ≈ ${CurrencyFormatter.formatAriary(uiState.estimatedYuanValueAr)} ($yuanAccountCount cpte ¥)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroStatTile(
                        dotColor = Color(0xFF34D399),
                        label = "Entrées",
                        value = CurrencyFormatter.formatAriary(totalTxInAr),
                        valueColor = Color(0xFFA7F3D0),
                        modifier = Modifier.weight(1f)
                    )
                    HeroStatTile(
                        dotColor = Color(0xFFFDA4AF),
                        label = "Sorties & ¥",
                        value = CurrencyFormatter.formatAriary(totalTxOutAr),
                        valueColor = Color(0xFFFFE4E6),
                        modifier = Modifier.weight(1f)
                    )
                    HeroStatTile(
                        dotColor = Color(0xFFFCD34D),
                        label = "Flux net",
                        value = "${if (netTxFlowAr >= 0) "+" else ""}${CurrencyFormatter.formatAriary(netTxFlowAr)}",
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }

                val entriesForRatio = totalTxInAr.toDouble()
                val exitsForRatio = totalTxOutAr.toDouble()
                val coverageRatio = if (exitsForRatio > 0.0) entriesForRatio / exitsForRatio else if (entriesForRatio > 0.0) 2.0 else 0.0
                val ratioFormatted = String.format(Locale.US, "%.2fx", coverageRatio)
                val ratioStatus = when {
                    coverageRatio >= 1.2 -> "Sécurisé"
                    coverageRatio >= 1.0 -> "Équilibré"
                    coverageRatio > 0.0 -> "À surveiller"
                    else -> "Aucune donnée"
                }
                val ratioColor = when {
                    coverageRatio >= 1.2 -> Color(0xFFA7F3D0)
                    coverageRatio >= 1.0 -> Color(0xFFFDE68A)
                    else -> Color(0xFFFCA5A5)
                }
                val ratioFraction = (coverageRatio / 2.0).toFloat().coerceIn(0.05f, 1.0f)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ratio de couverture des charges ($ratioFormatted)",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = ratioStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ratioColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratioFraction)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(ratioColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatTile(
    dotColor: Color,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.15f))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = valueColor
            )
        }
    }
}
