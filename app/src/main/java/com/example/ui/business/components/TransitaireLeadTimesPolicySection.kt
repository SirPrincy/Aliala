package com.example.ui.business.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.model.TariffGridParser
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

    val (airMin, airMax) = remember(airLeadDays) {
        TariffGridParser.parseMinMaxDays(airLeadDays, 7, 10)
    }
    val (airFastMin, airFastMax) = remember(airFastLeadDays) {
        TariffGridParser.parseMinMaxDays(airFastLeadDays, 3, 5)
    }
    val (seaMin, seaMax) = remember(seaLeadDays) {
        TariffGridParser.parseMinMaxDays(seaLeadDays, 30, 45)
    }

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
            // Header
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
                            text = "Estimation d'arrivée (en jours) et conditions",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.badgeNeutralBg
                ) {
                    Text(
                        text = "$airMin–${airMax}j · $seaMin–${seaMax}j",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Segmented Mode Toggle (Aérien vs Maritime)
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
                        Text(
                            text = "Aérien ($airFastMin–${airMax}j)",
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
                        Text(
                            text = "Maritime ($seaMin–${seaMax}j)",
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
                    presets = listOf(Pair(5, 7), Pair(7, 10), Pair(10, 14)),
                    onDaysChange = { min, max ->
                        onAirLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    }
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
                    presets = listOf(Pair(2, 3), Pair(3, 5), Pair(4, 6)),
                    onDaysChange = { min, max ->
                        onAirFastLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    }
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
                    presets = listOf(Pair(25, 35), Pair(30, 45), Pair(45, 60)),
                    onDaysChange = { min, max ->
                        onSeaLeadDaysChange(TariffGridParser.formatLeadDays(min, max))
                    }
                )
            }

            // Conditions particulières & Notes internes
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeadTimeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    accentColor: Color,
    solidColor: Color,
    softBgColor: Color,
    colors: TransitaireColors,
    scaleMaxDays: Int,
    minDays: Int,
    maxDays: Int,
    presets: List<Pair<Int, Int>>,
    onDaysChange: (min: Int, max: Int) -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Row + Current Window Pill
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(softBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = softBgColor
                            ) {
                                Text(
                                    text = badgeText,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = solidColor
                ) {
                    Text(
                        text = if (minDays == maxDays) "$minDays j" else "$minDays–$maxDays j",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Visual Delivery Window Timeline Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(colors.badgeNeutralBg)
                ) {
                    val startFraction = (minDays.toFloat() / scaleMaxDays.toFloat()).coerceIn(0f, 0.92f)
                    val endFraction = (maxDays.toFloat() / scaleMaxDays.toFloat()).coerceIn(startFraction + 0.06f, 1f)
                    val offsetDp = maxWidth * startFraction
                    val barWidthDp = maxWidth * (endFraction - startFraction)

                    Box(
                        modifier = Modifier
                            .offset(x = offsetDp)
                            .width(barWidthDp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.78f), accentColor)
                                )
                            )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Départ (J0)", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    Text(
                        text = "Arrivée estimée : ${TariffGridParser.formatLeadDays(minDays, maxDays)}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = accentColor
                    )
                    Text("${scaleMaxDays}j", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                }
            }

            // Numeric steppers for Min & Max days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayStepperInput(
                    label = "Min. arrivé",
                    value = minDays,
                    onValueChange = { newMin ->
                        val validMin = newMin.coerceAtLeast(1)
                        val validMax = maxDays.coerceAtLeast(validMin)
                        onDaysChange(validMin, validMax)
                    },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "→",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = accentColor,
                    modifier = Modifier.padding(top = 18.dp)
                )

                DayStepperInput(
                    label = "Max. arrivé",
                    value = maxDays,
                    onValueChange = { newMax ->
                        val validMax = newMax.coerceAtLeast(minDays)
                        onDaysChange(minDays, validMax)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick presets in FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (pMin, pMax) ->
                    val isSelected = minDays == pMin && maxDays == pMax
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (isSelected) solidColor else colors.cardBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) solidColor else colors.border
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .clickable { onDaysChange(pMin, pMax) }
                    ) {
                        Text(
                            text = "$pMin – $pMax j",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayStepperInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.textSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.inputBg)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.badgeNeutralBg)
                    .clickable { onValueChange((value - 1).coerceAtLeast(1)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuer",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedTextField(
                value = if (value > 0) value.toString() else "",
                onValueChange = { input ->
                    val num = input.filter { it.isDigit() }.toIntOrNull()
                    if (num != null) {
                        onValueChange(num.coerceIn(1, 180))
                    } else if (input.isBlank()) {
                        onValueChange(1)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = colors.textPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.cyanAccent
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            )

            Text(
                text = "j",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(end = 4.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.badgeNeutralBg)
                    .clickable { onValueChange((value + 1).coerceAtMost(180)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Augmenter",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
