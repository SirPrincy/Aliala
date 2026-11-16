package com.example.ui.business.transitaires

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.io.File

internal fun buildRateSummaryTooltip(
    sortedList: List<CatalogueTransitaireSummaryUi>,
    mode: String
): String {
    val unitLabel = if (mode == "AERIEN") "kg" else "m³"
    return when {
        sortedList.isEmpty() -> "Aucun transitaire actif enregistré pour comparer les tarifs."
        sortedList.size == 1 -> {
            val t = sortedList.first()
            val rate = if (mode == "AERIEN") t.airRateAr else t.seaRateAr
            "${t.name} : ${CurrencyFormatter.formatAriary(rate)}/$unitLabel."
        }
        else -> {
            val cheapest = sortedList.first()
            val priciest = sortedList.last()
            val minRate = if (mode == "AERIEN") cheapest.airRateAr else cheapest.seaRateAr
            val maxRate = if (mode == "AERIEN") priciest.airRateAr else priciest.seaRateAr
            val diff = (maxRate - minRate).coerceAtLeast(0L)
            "${cheapest.name} est le plus avantageux à ${CurrencyFormatter.formatAriary(minRate)}/$unitLabel (économie jusqu'à ${CurrencyFormatter.formatAriary(diff)}/$unitLabel)."
        }
    }
}

@Composable
fun TransitairePriceComparisonDialog(
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    orders: List<BusinessOrderEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()

    var selectedFretMode by remember { mutableStateOf("AERIEN") }
    var selectedPeriod by remember { mutableStateOf("6m") }

    var showTile1 by remember { mutableStateOf(true) }
    var showTile0 by remember { mutableStateOf(true) }
    var showTile2 by remember { mutableStateOf(true) }

    var selectedProviderFilter by remember { mutableStateOf("all") }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    val activeTransitaires = remember(transitaires) {
        transitaires.filter { !it.isArchived }
    }
    val sortedByRate = remember(activeTransitaires, selectedFretMode) {
        activeTransitaires.sortedBy { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr }
    }
    var activeTooltipText by remember(sortedByRate, selectedFretMode) {
        mutableStateOf(buildRateSummaryTooltip(sortedByRate, selectedFretMode))
    }

    val cheapestTrans = sortedByRate.firstOrNull()
    val priciestTrans = sortedByRate.lastOrNull()
    val bestRate = cheapestTrans?.let { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr } ?: 0L
    val maxRate = priciestTrans?.let { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr } ?: 0L
    val rateSpread = (maxRate - bestRate).coerceAtLeast(0L)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_back_price_comparison")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Comparaison de Prix",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        buildRateSummaryTooltip(sortedByRate, selectedFretMode),
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Résumé tarifs",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyanSolidBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyanSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "MARCHÉ FRET MADAGASCAR",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Button(
                            onClick = {
                                val csv = buildString {
                                    appendLine("Transitaire;Aerien_Normal_Ar_kg;Aerien_Express_Ar_kg;Maritime_Ar_m3;Delai_Aerien;Delai_Maritime;Date_Application;Statut")
                                    transitaires.forEach { t ->
                                        val st = if (t.isArchived) "Archivé" else "Actif"
                                        appendLine("${t.name};${t.airRateAr};${t.airFastRateAr};${t.seaRateAr};${t.airLeadDays};${t.seaLeadDays};${t.rateApplicationDate};$st")
                                    }
                                }
                                runCatching {
                                    File(context.filesDir, "comparatif_transitaires.csv").writeText(csv)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Comparatif Transitaires", csv))
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Comparatif tarifs transitaires")
                                        putExtra(Intent.EXTRA_TEXT, csv)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Exporter le comparatif CSV"))
                                }.onFailure {
                                    Toast.makeText(context, "Comparatif CSV copié dans le presse-papiers.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyanSoftBg,
                                contentColor = colors.cyanAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_export_comparison")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.IosShare,
                                    contentDescription = null,
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exporter",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .padding(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedFretMode == "AERIEN") colors.cardBg else Color.Transparent)
                                        .clickable {
                                            selectedFretMode = "AERIEN"
                                            val sortedAir = activeTransitaires.sortedBy { it.airRateAr }
                                            activeTooltipText = buildRateSummaryTooltip(sortedAir, "AERIEN")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlightTakeoff,
                                            contentDescription = null,
                                            tint = if (selectedFretMode == "AERIEN") colors.cyanAccent else colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Aérien (Ar/kg)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretMode == "AERIEN") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (selectedFretMode == "AERIEN") colors.cyanAccent else colors.textSecondary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedFretMode == "MARITIME") colors.cardBg else Color.Transparent)
                                        .clickable {
                                            selectedFretMode = "MARITIME"
                                            val sortedSea = activeTransitaires.sortedBy { it.seaRateAr }
                                            activeTooltipText = buildRateSummaryTooltip(sortedSea, "MARITIME")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBoat,
                                            contentDescription = null,
                                            tint = if (selectedFretMode == "MARITIME") colors.violetAccent else colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Maritime (Ar/m³)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretMode == "MARITIME") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (selectedFretMode == "MARITIME") colors.violetAccent else colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "3m" to "3 mois",
                                "6m" to "6 mois",
                                "1y" to "1 an",
                                "all" to "Tout"
                            ).forEach { (code, label) ->
                                val isSelected = selectedPeriod == code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(if (isSelected) colors.cyanSolidBg else colors.innerCardBg)
                                        .clickable { selectedPeriod = code }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    TransitaireComparisonChartCard(
                        selectedFretMode = selectedFretMode,
                        selectedPeriod = selectedPeriod,
                        activeTransitaires = activeTransitaires,
                        sortedByRate = sortedByRate,
                        activeTooltipText = activeTooltipText,
                        showTile0 = showTile0,
                        showTile1 = showTile1,
                        showTile2 = showTile2,
                        onToggleTile0 = { newVal, tooltip ->
                            showTile0 = newVal
                            if (tooltip != null) activeTooltipText = tooltip
                        },
                        onToggleTile1 = { newVal, tooltip ->
                            showTile1 = newVal
                            if (tooltip != null) activeTooltipText = tooltip
                        },
                        onToggleTile2 = { newVal, tooltip ->
                            showTile2 = newVal
                            if (tooltip != null) activeTooltipText = tooltip
                        },
                        colors = colors
                    )

                    TransitaireMarketKpisGrid(
                        selectedFretMode = selectedFretMode,
                        activeTransitaires = activeTransitaires,
                        sortedByRate = sortedByRate,
                        colors = colors
                    )

                    TransitairePotentialSavingCard(
                        selectedFretMode = selectedFretMode,
                        orders = orders,
                        sortedByRate = sortedByRate,
                        cheapestTrans = cheapestTrans,
                        priciestTrans = priciestTrans,
                        bestRate = bestRate,
                        rateSpread = rateSpread,
                        colors = colors,
                        onOpenSimulator = { showSimulatorDialog = true }
                    )

                    TransitaireDeclaredGridsCard(
                        transitaires = transitaires,
                        selectedProviderFilter = selectedProviderFilter,
                        onProviderFilterChange = { selectedProviderFilter = it },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showSimulatorDialog) {
        TransitaireMultiSimulatorDialog(
            initialFretMode = selectedFretMode,
            activeTransitaires = activeTransitaires,
            colors = colors,
            onDismiss = { showSimulatorDialog = false }
        )
    }
}
