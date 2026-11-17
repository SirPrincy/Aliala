package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun Step5SecurityAndInvoiceCard(
    carrier: String,
    freightCostAr: Long,
    isFreightPaid: Boolean
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.innerCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Règle de sécurisation du Stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Le stock ne sera incrémenté qu'après validation du contrôle qualité à l'Étape 6.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.violetSoftBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (carrier.isNotBlank()) "Facture $carrier" else "Facture fret",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = when {
                                    freightCostAr <= 0L -> "Tarif non encore calculé"
                                    isFreightPaid -> "Fret déjà réglé"
                                    else -> "Solde fret à régler"
                                },
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (freightCostAr > 0L) CurrencyFormatter.formatAriary(freightCostAr) else "Non défini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.violetAccent
                        )
                        if (freightCostAr > 0L) {
                            Text(
                                text = if (isFreightPaid) "Payé" else "En attente",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = if (isFreightPaid) colors.successAccent else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step5CustomsTaxCard(
    customsTaxInput: String,
    onCustomsTaxInputChange: (String) -> Unit,
    customsTaxAr: Long,
    savedCustomsTaxAr: Long,
    onSaveCustomsTax: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val hasPendingChange = customsTaxAr != savedCustomsTaxAr
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
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Frais de douane & taxes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Dédouanement à Madagascar (optionnel)",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (savedCustomsTaxAr > 0L && !hasPendingChange) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.successSoftBg
                    ) {
                        Text(
                            text = "Enregistré",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.successAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customsTaxInput,
                    onValueChange = { raw ->
                        onCustomsTaxInputChange(raw.filter { it.isDigit() })
                    },
                    label = { Text("Montant douane (Ar)", fontSize = 12.sp) },
                    placeholder = { Text("0", fontSize = 13.sp) },
                    suffix = {
                        Text(
                            text = "Ar",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_step5_customs_tax"),
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.violetAccent,
                        unfocusedBorderColor = colors.border.copy(alpha = 0.5f),
                        focusedContainerColor = colors.innerCardBg,
                        unfocusedContainerColor = colors.innerCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = onSaveCustomsTax,
                    enabled = hasPendingChange,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_save_step5_customs_tax"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.violetSolidBg,
                        disabledContainerColor = colors.badgeNeutralBg
                    )
                ) {
                    Text(
                        text = "Appliquer",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (hasPendingChange) Color.White else colors.textSecondary
                    )
                }
            }

            Text(
                text = if (customsTaxAr > 0L) {
                    "Montant saisi : ${CurrencyFormatter.formatAriary(customsTaxAr)} (inclus dans le coût unitaire FIFO)"
                } else {
                    "Aucun droit de douane saisi (0 Ar)."
                },
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
internal fun Step5LiveCostCard(
    arrivalShortName: String,
    totalDisbursedAr: Long,
    merchandiseTotalAr: Long,
    domesticPortAr: Long,
    freightCostAr: Long,
    customsTaxAr: Long
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Coût de revient en direct",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cyanSoftBg
                ) {
                    Text(
                        text = "Consolidé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.subtleBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Total déboursé réel",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalDisbursedAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = colors.cyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Marchandise usine (Chine)", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(merchandiseTotalAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Port Chine & logistique locale", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(domesticPortAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fret international ($arrivalShortName)", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(freightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    if (customsTaxAr > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Douane & taxes", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                            Text(CurrencyFormatter.formatAriary(customsTaxAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step5BottomActionsCard(
    receivedParcelsCount: Int,
    expectedParcels: Int,
    onValidateArrival: () -> Unit,
    onOpenIncidentDialog: () -> Unit,
    onPreviousStep: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val canAdvance = receivedParcelsCount > 0
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
                onClick = onValidateArrival,
                enabled = canAdvance,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("btn_validate_arrival_and_qc"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanSolidBg,
                    disabledContainerColor = colors.badgeNeutralBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                val ctaColor = if (canAdvance) Color.White else colors.textSecondary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (canAdvance) "Valider l'arrivée ($receivedParcelsCount/$expectedParcels colis) et lancer le contrôle" else "Sélectionnez au moins 1 colis reçu",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ctaColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ctaColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenIncidentDialog)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = colors.dangerAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Signaler un colis endommagé ou manquant",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = colors.dangerAccent
                )
            }

            TextButton(
                onClick = onPreviousStep,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 44.dp)
                    .testTag("btn_back_to_step4")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retour à l'étape 4 (Transit)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
internal fun Step5Dialogs(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    arrivalPortName: String,
    expectedParcels: Int,
    weightKg: Double,
    showIncidentDialog: Boolean,
    incidentDescription: String,
    showColisDetailsDialog: Boolean,
    onDismissIncident: () -> Unit,
    onIncidentDescriptionChange: (String) -> Unit,
    onConfirmIncident: () -> Unit,
    onDismissColisDetails: () -> Unit
) {
    val colors = rememberTransitaireColors()

    if (showIncidentDialog) {
        AlertDialog(
            onDismissRequest = onDismissIncident,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = colors.dangerAccent)
                    Text("Signaler une anomalie", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Précisez les dommages ou manquants constatés à l'arrivée ($arrivalPortName) :",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = incidentDescription,
                        onValueChange = onIncidentDescriptionChange,
                        placeholder = { Text("Ex: Colis abîmé, quantité manquante...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmIncident,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissIncident) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showColisDetailsDialog) {
        AlertDialog(
            onDismissRequest = onDismissColisDetails,
            title = {
                Text("Détail du lot à l'arrivée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val measureLabel = if (isMaritime) {
                        val vol = order.volumeM3
                        if (vol != null && vol > 0.0 && expectedParcels > 0) {
                            "Volume par colis : ${String.format(Locale.FRENCH, "%.5f", vol / expectedParcels)} m³"
                        } else {
                            "Volume : Non renseigné"
                        }
                    } else {
                        if (weightKg > 0.0 && expectedParcels > 0) {
                            "Poids par colis : ${String.format(Locale.US, "%.1f", weightKg / expectedParcels)} kg"
                        } else {
                            "Poids : Non renseigné"
                        }
                    }
                    Text(
                        text = measureLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    val contentSummary = order.variants.ifBlank { order.product.ifBlank { "Articles commandés" } }
                    Text("Contenu : $contentSummary", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    Text("Point d'arrivée : $arrivalPortName", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    if (order.receptionNotes.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = colors.innerCardBg, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Note entrepôt : ${order.receptionNotes}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (order.incidentReport.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = colors.dangerSoftBg, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Anomalie : ${order.incidentReport}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.dangerAccent,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissColisDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanSolidBg)
                ) {
                    Text("Fermer", color = Color.White)
                }
            }
        )
    }
}
