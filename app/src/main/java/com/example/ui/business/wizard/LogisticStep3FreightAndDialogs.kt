package com.example.ui.business.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun Step3FreightCalculationCard(
    isAirFreight: Boolean,
    airRatePerKg: Long,
    seaRatePerM3: Long,
    weightDouble: Double,
    volumeDouble: Double,
    calculatedFreightCostAr: Long
) {
    val colors = rememberTransitaireColors()

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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = if (isAirFreight) "Calcul du Fret Aérien" else "Calcul du Fret Maritime",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = colors.badgeNeutralBg) {
                    Text(
                        text = "Barème actif",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.innerCardBg) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tarif unitaire applicable :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(
                            text = if (isAirFreight) "${CurrencyFormatter.formatAriary(airRatePerKg)} / kg" else "${CurrencyFormatter.formatAriary(seaRatePerM3)} / m³",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAirFreight) "${String.format(Locale.US, "%.1f", weightDouble)} kg × ${CurrencyFormatter.formatAriary(airRatePerKg)}" else "${String.format(Locale.FRENCH, "%.5f", volumeDouble)} m³ × ${CurrencyFormatter.formatAriary(seaRatePerM3)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(calculatedFreightCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.violetAccent
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Montant final facturé", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textPrimary)
                    Surface(shape = RoundedCornerShape(9999.dp), color = colors.cyanSoftBg) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(12.dp))
                            Text("Conforme au barème", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = colors.cyanAccent)
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.innerCardBg) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyFormatter.formatAriary(calculatedFreightCostAr).replace(" Ar", ""),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = colors.textPrimary
                        )
                        Text("MGA", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textSecondary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                Text("Le transitaire émet sa facture globale après pesée et contrôle.", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
            }
        }
    }
}

@Composable
internal fun Step3MutualisationCard(
    order: BusinessOrderEntity,
    totalQty: Int,
    isAirFreight: Boolean,
    weightDouble: Double,
    volumeDouble: Double
) {
    val colors = rememberTransitaireColors()

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShareLocation, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(18.dp))
                    }
                    Text("Mutualisation de Fret", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = colors.violetSolidBg) {
                    Text(
                        text = order.lotNumber.ifBlank { order.referenceCode.ifBlank { "Lot #${order.id}" } },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.innerCardBg) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(24.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("${order.product.ifEmpty { "Article #${order.id}" }} ($totalQty pcs)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                        val measureText = when {
                            isAirFreight && weightDouble > 0.0 -> "Poids attribué : ${String.format(Locale.US, "%.1f", weightDouble)} kg (100% de ce colis)"
                            !isAirFreight && volumeDouble > 0.0 -> "Volume attribué : ${String.format(Locale.FRENCH, "%.5f", volumeDouble)} m³ (100% de ce colis)"
                            else -> "Mesure en attente de saisie"
                        }
                        Text(measureText, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                    }

                    Icon(Icons.Default.Tune, contentDescription = "Répartition", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clé de répartition au prorata :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                Text("Monocolis (100%)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
            }
        }
    }
}

@Composable
internal fun Step3BudgetSynthesisCard(
    isBudgetExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    totalCostOfGoodsAr: Long,
    totalQty: Int,
    unitPriceYuan: Double,
    merchandiseTotalAr: Long,
    domesticShippingYuan: Double,
    domesticPortAr: Long,
    isAirFreight: Boolean,
    selectedForwarder: String,
    weightDouble: Double,
    volumeDouble: Double,
    calculatedFreightCostAr: Long,
    unitCostAr: Long,
    projectedGrossMarginPercent: String
) {
    val colors = rememberTransitaireColors()
    val chevronRotation by animateFloatAsState(targetValue = if (isBudgetExpanded) 180f else 0f, label = "rot")

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("SYNTHÈSE BUDGÉTAIRE", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, letterSpacing = 0.5.sp)
                        Text("Coût de Revient Estimé", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(CurrencyFormatter.formatAriary(totalCostOfGoodsAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.cyanAccent)
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp).rotate(chevronRotation))
                }
            }

            AnimatedVisibility(visible = isBudgetExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Marchandise ($totalQty pcs · ¥ ${(unitPriceYuan * totalQty).toInt()}) :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(merchandiseTotalAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Port Chine domestique (¥ ${String.format(Locale.US, "%.2f", domesticShippingYuan)}) :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(domesticPortAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = if (isAirFreight) "Fret $selectedForwarder (${String.format(Locale.US, "%.1f", weightDouble)} kg) :" else "Fret $selectedForwarder (${String.format(Locale.FRENCH, "%.5f", volumeDouble)} m³) :",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(CurrencyFormatter.formatAriary(calculatedFreightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.violetAccent)
                    }

                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.innerCardBg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("COÛT UNITAIRE ESTIMÉ", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary)
                                Text("${CurrencyFormatter.formatAriary(unitCostAr)} / unité", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.cyanAccent)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("MARGE BRUTE PROJETÉE", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary)
                                Text(
                                    text = if (projectedGrossMarginPercent == "—") "Non défini" else "$projectedGrossMarginPercent%",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (projectedGrossMarginPercent.startsWith("-")) colors.dangerAccent else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step3BottomActionsCard(
    isAirFreight: Boolean,
    isInputValid: Boolean,
    onValidateArrivalClick: () -> Unit,
    onPreviousStep: () -> Unit
) {
    val colors = rememberTransitaireColors()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onValidateArrivalClick,
                enabled = isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_validate_warehouse_arrival"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = colors.badgeNeutralBg,
                    disabledContentColor = colors.textSecondary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isAirFreight) "Valider l'arrivée à l'entrepôt (Aérien)" else "Valider l'arrivée à l'entrepôt (Maritime)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPreviousStep)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Revenir à l'étape 2 (Livraison Chine)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textSecondary)
            }
        }
    }
}

@Composable
internal fun Step3ChangeForwarderDialog(
    transitaires: List<CatalogueTransitaireSummaryUi>,
    selectedForwarder: String,
    isAirFreight: Boolean,
    onDismiss: () -> Unit,
    onSelectForwarder: (CatalogueTransitaireSummaryUi) -> Unit
) {
    val colors = rememberTransitaireColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choisir un transitaire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (transitaires.isEmpty()) {
                    Text(
                        text = "Aucun transitaire configuré dans le catalogue. Vous pouvez en créer un dans l'onglet Catalogue.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                } else {
                    transitaires.forEach { fwd ->
                        val isSelected = fwd.name.equals(selectedForwarder, ignoreCase = true)
                        val rateLabel = if (isAirFreight) {
                            if (fwd.airRateAr > 0L) "${CurrencyFormatter.formatAriary(fwd.airRateAr)} / kg" else "Tarif aérien non défini"
                        } else {
                            if (fwd.seaRateAr > 0L) "${CurrencyFormatter.formatAriary(fwd.seaRateAr)} / m³" else "Tarif maritime non défini"
                        }
                        val hubLabel = fwd.chinaWarehouseAddress.ifBlank { fwd.routeHub.ifBlank { "Entrepôt non renseigné" } }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectForwarder(fwd) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.violetSoftBg else colors.innerCardBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = fwd.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) colors.violetAccent else colors.textPrimary
                                    )
                                    Text(
                                        text = rateLabel,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                                Text(hubLabel, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
