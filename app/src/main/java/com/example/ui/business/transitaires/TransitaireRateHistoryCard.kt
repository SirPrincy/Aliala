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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
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
import com.example.data.local.entity.TransitaireRateHistoryEntity
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun TransitaireRateHistoryCard(
    rateHistory: List<TransitaireRateHistoryEntity>,
    rateApplicationDate: String,
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Historique des tarifs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (rateApplicationDate.isNotBlank()) "Dernière application : $rateApplicationDate"
                            else "Suivi daté des révisions de fret",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(colors.badgeNeutralBg)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (rateHistory.isEmpty()) "0 maj" else "${rateHistory.size} maj",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (rateHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.subtleBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Aucune modification de tarif enregistrée. Chaque création ou révision future sera datée ici automatiquement.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            } else {
                val sortedHistory = rateHistory.sortedWith(
                    compareByDescending<TransitaireRateHistoryEntity> { it.changedAtMillis }
                        .thenByDescending { it.id }
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedHistory.take(15).forEachIndexed { idx, entry ->
                        val isSea = entry.mode == "MARITIME"
                        val modeLabel = when (entry.mode) {
                            "MARITIME" -> "Maritime"
                            "AERIEN_FAST" -> "Aérien Fast"
                            else -> "Aérien Std"
                        }
                        val unitLabel = if (isSea) "/m³" else "/kg"
                        val modeAccent = if (isSea) colors.violetAccent else colors.cyanAccent
                        val modeSoft = if (isSea) colors.violetSoftBg else colors.cyanSoftBg
                        val hasDiff = entry.oldRateAr > 0L && entry.newRateAr > 0L && entry.deltaAr != 0L
                        val isDown = entry.deltaAr < 0L
                        val newerSameMode = sortedHistory.take(idx).lastOrNull { it.mode == entry.mode }
                        val startLabel = entry.effectiveDateLabel.ifBlank { "—" }
                        val validityLabel = when {
                            entry.newRateAr <= 0L -> "Le $startLabel"
                            newerSameMode != null && newerSameMode.effectiveDateLabel.isNotBlank() ->
                                "Du $startLabel au ${newerSameMode.effectiveDateLabel}"
                            else -> "À partir du $startLabel (en vigueur)"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(modeSoft)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = modeLabel,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = modeAccent
                                            )
                                        }
                                        Text(
                                            text = validityLabel,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = colors.textPrimary
                                        )
                                    }

                                    val detailText = when {
                                        entry.oldRateAr == 0L && entry.newRateAr > 0L ->
                                            "Défini à ${CurrencyFormatter.formatAriary(entry.newRateAr)}$unitLabel"
                                        entry.oldRateAr > 0L && entry.newRateAr == 0L ->
                                            "${CurrencyFormatter.formatAriary(entry.oldRateAr)}$unitLabel → Retiré"
                                        else ->
                                            "${CurrencyFormatter.formatAriary(entry.oldRateAr)} → ${CurrencyFormatter.formatAriary(entry.newRateAr)}$unitLabel"
                                    }
                                    Text(
                                        text = detailText,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                if (hasDiff) {
                                    val pctAbs = abs(entry.deltaPercent)
                                    val pctStr = if (pctAbs >= 10.0 || pctAbs % 1.0 < 0.05) {
                                        "${kotlin.math.round(entry.deltaPercent).toInt()}%"
                                    } else {
                                        String.format(Locale.US, "%.1f%%", entry.deltaPercent)
                                    }
                                    val sign = if (entry.deltaAr > 0L) "+" else ""
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDown) colors.cyanSoftBg else colors.dangerSoftBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (isDown) colors.cyanAccent else colors.dangerAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "$sign${CurrencyFormatter.formatAriary(entry.deltaAr)} ($sign$pctStr)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isDown) colors.cyanAccent else colors.dangerAccent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = entry.note.ifBlank { "Enregistré" },
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
