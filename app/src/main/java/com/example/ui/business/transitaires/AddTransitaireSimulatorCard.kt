package com.example.ui.business.transitaires

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun AddTransitaireSimulatorCard(
    testKgInput: String,
    onTestKgInputChange: (String) -> Unit,
    testCbmInput: String,
    onTestCbmInputChange: (String) -> Unit,
    testKg: Double,
    testCbm: Double,
    matchedAirTariff: TariffItem?,
    matchedSeaTariff: TariffItem?,
    colors: TransitaireColors
) {
    val tarifAerienSim = matchedAirTariff?.priceAr ?: 0L
    val tarifMaritimeSim = matchedSeaTariff?.priceAr ?: 0L

    val totalAirComputed = (testKg * tarifAerienSim).toLong()
    val totalSeaComputed = (testCbm * tarifMaritimeSim).toLong()

    val formattedTestKg = String.format(Locale.FRENCH, "%.1f", testKg).replace('.', ',')
    val formattedTestCbm = String.format(Locale.FRENCH, "%.5f", testCbm).replace('.', ',')

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Simulateur de fret",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Teste vos paliers (kg / CBM) en direct",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(colors.badgeNeutralBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "APERÇU",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.cyanAccent,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.innerCardBg)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Colis test Aérien",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.cyanAccent
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.inputBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = testKgInput,
                                onValueChange = onTestKgInputChange,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(colors.cyanAccent),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.End,
                                    color = colors.textPrimary
                                ),
                                modifier = Modifier.width(44.dp)
                            )
                            Text(
                                text = "kg",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (matchedAirTariff != null) {
                        val cond = TariffGridParser.formatCondition(matchedAirTariff)
                        val usdSuffix = matchedAirTariff.usdPrice?.let { u ->
                            val str = if (u % 1.0 == 0.0) "${u.toInt()}" else String.format(Locale.US, "%.2f", u)
                            " · $str $/kg"
                        } ?: ""
                        Text(
                            text = buildString {
                                append("Palier appliqué : ${matchedAirTariff.shipmentType}")
                                if (cond.isNotBlank()) append(" ($cond)")
                                append(usdSuffix)
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (matchedAirTariff != null) {
                                "$formattedTestKg kg × ${CurrencyFormatter.formatAriary(tarifAerienSim)}"
                            } else {
                                "Aucun tarif aérien configuré"
                            },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalAirComputed),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.innerCardBg)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Carton test Maritime",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.violetAccent
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.inputBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = testCbmInput,
                                onValueChange = onTestCbmInputChange,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(colors.violetAccent),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.End,
                                    color = colors.textPrimary
                                ),
                                modifier = Modifier.width(72.dp)
                            )
                            Text(
                                text = "m³",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (matchedSeaTariff != null) {
                        val cond = TariffGridParser.formatCondition(matchedSeaTariff)
                        val usdSuffix = matchedSeaTariff.usdPrice?.let { u ->
                            val str = if (u % 1.0 == 0.0) "${u.toInt()}" else String.format(Locale.US, "%.2f", u)
                            " · $str $/CBM"
                        } ?: ""
                        Text(
                            text = buildString {
                                append("Palier appliqué : ${matchedSeaTariff.shipmentType}")
                                if (cond.isNotBlank()) append(" ($cond)")
                                append(usdSuffix)
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.violetAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (matchedSeaTariff != null) {
                                "$formattedTestCbm m³ × ${CurrencyFormatter.formatAriary(tarifMaritimeSim)}"
                            } else {
                                "Aucun tarif maritime configuré"
                            },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalSeaComputed),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = colors.violetAccent
                        )
                    }
                }
            }
        }
    }
}
