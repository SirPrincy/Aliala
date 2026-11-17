package com.example.ui.business.transitaires

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.TransitStatsCalculator

@Composable
internal fun TransitaireRealTransitStatsCard(
    transitaire: CatalogueTransitaireSummaryUi,
    colors: TransitaireColors
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Statistiques de délais réels",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Calculé d'après vos dates enregistrées",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (transitaire.measuredShipmentsCount > 0) colors.cyanSoftBg else colors.badgeNeutralBg
                ) {
                    Text(
                        text = "${transitaire.measuredShipmentsCount} lot(s)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (transitaire.measuredShipmentsCount > 0) colors.cyanAccent else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (transitaire.measuredShipmentsCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.subtleBg)
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Aucune statistique de délai encore mesurée. Renseignez les dates d'arrivée au dépôt Chine, d'envoi vers Madagascar et d'arrivée à Madagascar dans vos commandes pour calculer les moyennes réelles.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RealStatCell(
                        label = "Fret Aérien réel",
                        value = TransitStatsCalculator.formatDaysAvg(transitaire.avgAirTransitDays),
                        subtitle = if (transitaire.measuredAirCount > 0) "${transitaire.measuredAirCount} vol(s)" else "Aucune mesure",
                        accentColor = colors.cyanAccent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    RealStatCell(
                        label = "Fret Maritime réel",
                        value = TransitStatsCalculator.formatDaysAvg(transitaire.avgSeaTransitDays),
                        subtitle = if (transitaire.measuredSeaCount > 0) "${transitaire.measuredSeaCount} envoi(s)" else "Aucune mesure",
                        accentColor = colors.violetAccent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RealStatCell(
                        label = "Achat → Dépôt Chine",
                        value = TransitStatsCalculator.formatDaysAvg(transitaire.avgChinaDays),
                        subtitle = "Livraison interne",
                        accentColor = colors.textPrimary,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    RealStatCell(
                        label = "Attente Hub Chine",
                        value = TransitStatsCalculator.formatDaysAvg(transitaire.avgHubWaitDays),
                        subtitle = "Dépôt → Envoi Mada",
                        accentColor = colors.textPrimary,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    RealStatCell(
                        label = "Total Achat → Mada",
                        value = TransitStatsCalculator.formatDaysAvg(transitaire.avgTotalDays),
                        subtitle = "Cycle complet",
                        accentColor = colors.successAccent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RealStatCell(
    label: String,
    value: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    colors: TransitaireColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.innerCardBg)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}
