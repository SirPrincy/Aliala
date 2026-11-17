package com.example.ui.business.transitaires

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransitaireLeadTimesPolicySection(
    airLeadDays: String,
    onAirLeadDaysChange: (String) -> Unit,
    airFastLeadDays: String,
    onAirFastLeadDaysChange: (String) -> Unit,
    seaLeadDays: String,
    onSeaLeadDaysChange: (String) -> Unit,
    notesInternes: String,
    onNotesInternesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    var selectedMode by remember { mutableStateOf("AÉRIEN") }

    val airParsed = remember(airLeadDays) { TariffGridParser.parseMinMaxDaysOrNull(airLeadDays) }
    val airFastParsed = remember(airFastLeadDays) { TariffGridParser.parseMinMaxDaysOrNull(airFastLeadDays) }
    val seaParsed = remember(seaLeadDays) { TariffGridParser.parseMinMaxDaysOrNull(seaLeadDays) }

    val airMin = airParsed?.first ?: 0
    val airMax = airParsed?.second ?: 0
    val airFastMin = airFastParsed?.first ?: 0
    val airFastMax = airFastParsed?.second ?: 0
    val seaMin = seaParsed?.first ?: 0
    val seaMax = seaParsed?.second ?: 0

    val activeAccent = if (selectedMode == "AÉRIEN") colors.cyanAccent else colors.violetAccent
    val activeSoftBg = if (selectedMode == "AÉRIEN") colors.cyanSoftBg else colors.violetSoftBg
    val airSolidBg = if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)

    val quickPolicyClauses = listOf(
        "Ratio vol. 1:6000",
        "Batteries déclarées",
        "Dédouanement inclus",
        "Groupage sans frais"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Politique & Délais",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Optionnel — laissez vide si non défini",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                val summaryText = listOfNotNull(
                    airParsed?.let { "${it.first}–${it.second}j Air" },
                    seaParsed?.let { "${it.first}–${it.second}j Mer" }
                ).joinToString(" · ").ifBlank { "Non défini" }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.badgeNeutralBg
                ) {
                    Text(
                        text = summaryText,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (summaryText == "Non défini") colors.textSecondary else colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.toggleTrackBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isAir = selectedMode == "AÉRIEN"
                val isSea = selectedMode == "MARITIME"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAir) airSolidBg else Color.Transparent)
                        .clickable { selectedMode = "AÉRIEN" }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("toggle_policy_aerien"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = if (isAir) Color.White else colors.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        val airLabel = when {
                            airParsed != null -> "Aérien (${airParsed.first}–${airParsed.second}j)"
                            airFastParsed != null -> "Aérien (${airFastParsed.first}–${airFastParsed.second}j)"
                            else -> "Aérien"
                        }
                        Text(
                            text = airLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isAir) Color.White else colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSea) colors.violetSolidBg else Color.Transparent)
                        .clickable { selectedMode = "MARITIME" }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("toggle_policy_maritime"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = if (isSea) Color.White else colors.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        val seaLabel = if (seaParsed != null) "Maritime (${seaParsed.first}–${seaParsed.second}j)" else "Maritime"
                        Text(
                            text = seaLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSea) Color.White else colors.textSecondary
                        )
                    }
                }
            }

            if (selectedMode == "AÉRIEN") {
                LeadTimeCard(
                    title = "Aérien Standard",
                    subtitle = "Cargo régulier Chine → Madagascar",
                    icon = Icons.Default.FlightTakeoff,
                    badgeText = "Hebdomadaire",
                    accentColor = colors.cyanAccent,
                    solidColor = airSolidBg,
                    softBgColor = colors.cyanSoftBg,
                    colors = colors,
                    scaleMaxDays = 21,
                    minDays = airMin,
                    maxDays = airMax,
                    isDefined = airParsed != null,
                    presets = listOf(Pair(5, 7), Pair(7, 10), Pair(10, 14)),
                    onDaysChange = { min, max ->
                        onAirLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    },
                    onClear = { onAirLeadDaysChange("") }
                )

                LeadTimeCard(
                    title = "Aérien Express (Fast)",
                    subtitle = "Traitement prioritaire & vol direct",
                    icon = Icons.Default.Speed,
                    badgeText = "Prioritaire",
                    accentColor = colors.expressAccent,
                    solidColor = Color(0xFF0284C7),
                    softBgColor = colors.expressSoftBg,
                    colors = colors,
                    scaleMaxDays = 14,
                    minDays = airFastMin,
                    maxDays = airFastMax,
                    isDefined = airFastParsed != null,
                    presets = listOf(Pair(2, 3), Pair(3, 5), Pair(4, 6)),
                    onDaysChange = { min, max ->
                        onAirFastLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    },
                    onClear = { onAirFastLeadDaysChange("") }
                )
            } else {
                LeadTimeCard(
                    title = "Maritime Groupage (LCL)",
                    subtitle = "Conteneur groupé par voie maritime",
                    icon = Icons.Default.DirectionsBoat,
                    badgeText = "Océan Indien",
                    accentColor = colors.violetAccent,
                    solidColor = colors.violetSolidBg,
                    softBgColor = colors.violetSoftBg,
                    colors = colors,
                    scaleMaxDays = 75,
                    minDays = seaMin,
                    maxDays = seaMax,
                    isDefined = seaParsed != null,
                    presets = listOf(Pair(25, 35), Pair(30, 45), Pair(45, 60)),
                    onDaysChange = { min, max ->
                        onSeaLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    },
                    onClear = { onSeaLeadDaysChange("") }
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Conditions & Règles du transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val chipSolid = if (selectedMode == "AÉRIEN") airSolidBg else colors.violetSolidBg
                        quickPolicyClauses.forEach { clause ->
                            val isActiveClause = notesInternes.contains(clause, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = if (isActiveClause) chipSolid else colors.cardBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isActiveClause) chipSolid else colors.border
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .clickable {
                                        val updated = if (isActiveClause) {
                                            notesInternes
                                                .split("•", "\n")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() && !it.equals(clause, ignoreCase = true) }
                                                .joinToString(" • ")
                                        } else {
                                            if (notesInternes.isBlank()) clause else "${notesInternes.trim()} • $clause"
                                        }
                                        onNotesInternesChange(updated)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isActiveClause) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (isActiveClause) Color.White else colors.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = clause,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isActiveClause) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isActiveClause) Color.White else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notesInternes,
                        onValueChange = onNotesInternesChange,
                        placeholder = {
                            Text(
                                "Saisissez les restrictions (liquides, batteries, poids min., manutention...)",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary.copy(alpha = 0.75f)
                            )
                        },
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.inputBg,
                            unfocusedContainerColor = colors.inputBg,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = activeAccent,
                            unfocusedBorderColor = colors.border,
                            cursorColor = activeAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_policy_notes_internes")
                    )
                }
            }
        }
    }
}
