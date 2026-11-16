package com.example.ui.business.transitaires

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

internal data class RevisionItemData(
    val provider: String,
    val mode: String,
    val date: String,
    val range: String,
    val delta: String,
    val isDown: Boolean,
    val colorHex: String
)

@Composable
internal fun TransitaireDeclaredGridsCard(
    transitaires: List<CatalogueTransitaireSummaryUi>,
    selectedProviderFilter: String,
    onProviderFilterChange: (String) -> Unit,
    colors: TransitaireColors
) {
    var showFilterDropdown by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = "Grilles actives déclarées",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tarifs aériens et maritimes par partenaire",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                val providerNames = remember(transitaires) {
                    transitaires.map { it.name }.distinct()
                }
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.innerCardBg)
                            .clickable { showFilterDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (selectedProviderFilter == "all") "Tous transitaires" else selectedProviderFilter,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tous transitaires", fontFamily = PlusJakartaSans) },
                            onClick = {
                                onProviderFilterChange("all")
                                showFilterDropdown = false
                            }
                        )
                        providerNames.forEach { providerName ->
                            DropdownMenuItem(
                                text = { Text(providerName, fontFamily = PlusJakartaSans) },
                                onClick = {
                                    onProviderFilterChange(providerName)
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            val allRevisions = remember(transitaires) {
                transitaires.flatMap { t ->
                    listOf(
                        RevisionItemData(
                            provider = t.name,
                            mode = "Aérien",
                            date = t.rateApplicationDate.ifBlank { "Actif" },
                            range = "${CurrencyFormatter.formatAriary(t.airRateAr)}/kg",
                            delta = t.airRateTrend.ifBlank { if (t.isArchived) "Archivé" else "Actif" },
                            isDown = t.airRateTrend.startsWith("-"),
                            colorHex = "#06B6D4"
                        ),
                        RevisionItemData(
                            provider = t.name,
                            mode = "Maritime",
                            date = t.rateApplicationDate.ifBlank { "Actif" },
                            range = "${CurrencyFormatter.formatAriary(t.seaRateAr)}/m³",
                            delta = t.seaRateTrend.ifBlank { if (t.isArchived) "Archivé" else "Actif" },
                            isDown = t.seaRateTrend.startsWith("-"),
                            colorHex = "#7C3AED"
                        )
                    )
                }
            }

            val filteredRevisions = allRevisions.filter {
                selectedProviderFilter == "all" || it.provider == selectedProviderFilter
            }

            if (filteredRevisions.isEmpty()) {
                Text(
                    text = "Aucun transitaire enregistré pour le moment.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredRevisions.forEach { item ->
                        val tintColor = if (item.mode == "Aérien") colors.cyanAccent else colors.violetAccent
                        val iconBg = if (item.mode == "Aérien") colors.cyanSoftBg else colors.violetSoftBg
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (item.mode == "Aérien") Icons.Default.FlightTakeoff else Icons.Default.DirectionsBoat,
                                            contentDescription = null,
                                            tint = tintColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.provider,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(colors.badgeNeutralBg)
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = item.mode,
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                        Text(
                                            text = item.date,
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.range,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val badgeTint = if (item.isDown) colors.cyanAccent else colors.textSecondary
                                        if (item.delta.startsWith("+") || item.delta.startsWith("-")) {
                                            Icon(
                                                imageVector = if (item.isDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (item.isDown) colors.cyanAccent else colors.dangerAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = item.delta,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = badgeTint
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
}

@Composable
internal fun TransitaireMultiSimulatorDialog(
    initialFretMode: String,
    activeTransitaires: List<CatalogueTransitaireSummaryUi>,
    colors: TransitaireColors,
    onDismiss: () -> Unit
) {
    var simQuantityInput by remember { mutableStateOf(if (initialFretMode == "AERIEN") "10" else "0.5") }
    var simMode by remember { mutableStateOf(if (initialFretMode == "AERIEN") "AERIEN_NORMAL" else "MARITIME") }
    val simVal = simQuantityInput.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Simulateur multi-transitaires",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "AERIEN_NORMAL" to "Aérien Normal",
                        "AERIEN_EXPRESS" to "Express",
                        "MARITIME" to "Maritime"
                    ).forEach { (code, lbl) ->
                        FilterChip(
                            selected = simMode == code,
                            onClick = {
                                simMode = code
                                if (code == "MARITIME" && (simQuantityInput.toDoubleOrNull() ?: 0.0) >= 5.0) {
                                    simQuantityInput = "0.5"
                                }
                            },
                            label = { Text(lbl, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = simQuantityInput,
                    onValueChange = { raw ->
                        val clean = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                        if (clean.count { it == '.' } <= 1) simQuantityInput = clean
                    },
                    label = { Text(if (simMode == "MARITIME") "Volume (m³ / CBM)" else "Poids (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (activeTransitaires.isEmpty()) {
                    Text(
                        text = "Aucun transitaire actif enregistré.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                } else {
                    val quotes = activeTransitaires.map { t ->
                        val grid = TariffGridParser.parse(
                            raw = t.goodsCategoryDelays,
                            airRateAr = t.airRateAr,
                            airFastRateAr = t.airFastRateAr,
                            seaRateAr = t.seaRateAr
                        )
                        val matchingTariff = when (simMode) {
                            "MARITIME" -> TariffGridParser.matchTariffForMeasure(grid, "MARITIME", simVal)
                                ?: grid.firstOrNull { it.mode == "MARITIME" }
                            "AERIEN_EXPRESS" -> grid.firstOrNull {
                                it.mode == "AÉRIEN" && it.shipmentType.contains("Express", ignoreCase = true)
                            }
                            else -> TariffGridParser.matchTariffForMeasure(
                                grid.filterNot { it.shipmentType.contains("Express", ignoreCase = true) },
                                "AÉRIEN",
                                simVal
                            ) ?: grid.firstOrNull { it.mode == "AÉRIEN" && !it.shipmentType.contains("Express", ignoreCase = true) }
                        }
                        val rate = matchingTariff?.priceAr?.takeIf { it > 0L } ?: when (simMode) {
                            "MARITIME" -> t.seaRateAr
                            "AERIEN_EXPRESS" -> t.airFastRateAr
                            else -> t.airRateAr
                        }
                        val totalAr = kotlin.math.round(simVal * rate).toLong()
                        Triple(t, rate, totalAr)
                    }.sortedBy { it.third }

                    quotes.forEachIndexed { idx, (t, rate, totalAr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (idx == 0) colors.cyanSoftBg else colors.innerCardBg)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (idx == 0) "${t.name} (Meilleur prix)" else t.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (idx == 0) colors.cyanAccent else colors.textPrimary
                                )
                                Text(
                                    text = "${CurrencyFormatter.formatAriary(rate)}/${if (simMode == "MARITIME") "m³" else "kg"}",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = CurrencyFormatter.formatAriary(totalAr),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (idx == 0) colors.cyanAccent else colors.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
