package com.example.ui.business.transitaires

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.business.TransitaireShipmentSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun TransitaireInvoicesCard(
    totalInvoiced: Long,
    paidAmount: Long,
    balanceDue: Long,
    settlementPercent: Int,
    colors: TransitaireColors,
    onRecordPaymentClick: () -> Unit
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Factures & Règlements",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                if (balanceDue > 0L) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.dangerSoftBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Reste : ${CurrencyFormatter.formatAriary(balanceDue)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.dangerAccent
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total facturé", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(totalInvoiced),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Réglé", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(paidAmount),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (balanceDue > 0L) colors.dangerSoftBg else colors.successSoftBg)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Dû",
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = if (balanceDue > 0L) colors.dangerAccent else colors.successAccent
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(balanceDue),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (balanceDue > 0L) colors.dangerAccent else colors.successAccent
                        )
                    }
                }
            }

            if (totalInvoiced > 0L) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Taux de règlement",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "$settlementPercent% réglé",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.badgeNeutralBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(settlementPercent / 100f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(colors.cyanSolidBg)
                        )
                    }
                }

                Button(
                    onClick = onRecordPaymentClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cyanSolidBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_enregistrer_paiement")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Enregistrer un paiement",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucune facture de fret enregistrée pour ce transitaire.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
internal fun TransitaireRecentShipmentsCard(
    transitaireName: String,
    recentShipments: List<TransitaireShipmentSummaryUi>,
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Expéditions récentes",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.badgeNeutralBg
                ) {
                    Text(
                        text = "${recentShipments.size} lot(s)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (recentShipments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.subtleBg)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Aucune expédition associée à $transitaireName pour le moment.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            } else {
                recentShipments.forEach { ship ->
                    val isSea = ship.transportMode == "SEA"
                    val measureStr = if (isSea) {
                        if (ship.volumeCbm > 0.0) "${String.format(Locale.FRENCH, "%.2f", ship.volumeCbm)} m³" else "Maritime"
                    } else {
                        if (ship.weightKg > 0.0) "${String.format(Locale.FRENCH, "%.1f", ship.weightKg)} kg" else "Aérien"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.innerCardBg)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSea) colors.violetSoftBg else colors.cyanSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSea) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                        contentDescription = null,
                                        tint = if (isSea) colors.violetAccent else colors.cyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${ship.code} · ${ship.title}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$measureStr · ${CurrencyFormatter.formatAriary(ship.freightCostAr)} · ${ship.dateLabel}",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(
                                        if (ship.isDelivered) colors.cyanSoftBg
                                        else colors.badgeNeutralBg
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = ship.statusLabel,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (ship.isDelivered) colors.cyanAccent else colors.textPrimary
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
internal fun TransitairePaymentDialog(
    transitaireId: Long,
    balanceDue: Long,
    accounts: List<BusinessAccountEntity>,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirmPayment: (id: Long, amountAr: Long, accountName: String) -> Unit
) {
    val context = LocalContext.current
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }
    var paymentAmountInput by remember { mutableStateOf(if (balanceDue > 0L) balanceDue.toString() else "") }
    var selectedAccountName by remember(ariaryAccounts) {
        mutableStateOf((ariaryAccounts.maxByOrNull { it.balanceAr } ?: ariaryAccounts.firstOrNull())?.name ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = {
            Text(
                text = "Enregistrer un règlement",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Solde restant dû : ${CurrencyFormatter.formatAriary(balanceDue)}",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )

                OutlinedTextField(
                    value = paymentAmountInput,
                    onValueChange = { paymentAmountInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Montant payé (Ariary)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.inputBg,
                        unfocusedContainerColor = colors.inputBg,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.cyanAccent,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.cyanAccent,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.cyanAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (ariaryAccounts.isNotEmpty()) {
                    Text(
                        text = "Compte Ariary débité :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ariaryAccounts.forEach { acc ->
                            val isSelected = acc.name.equals(selectedAccountName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.cyanSoftBg else colors.innerCardBg)
                                    .clickable { selectedAccountName = acc.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) colors.cyanAccent else colors.textPrimary
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Aucun compte Ariary actif. Créez un compte dans Finances > Trésorerie.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.dangerAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = paymentAmountInput.toLongOrNull() ?: 0L
                    if (ariaryAccounts.isEmpty()) {
                        Toast.makeText(context, "Créez d'abord un compte Ariary dans Finances", Toast.LENGTH_SHORT).show()
                    } else if (amount <= 0L) {
                        Toast.makeText(context, "Veuillez saisir un montant valide", Toast.LENGTH_SHORT).show()
                    } else {
                        val chosenAccount = selectedAccountName.ifBlank { ariaryAccounts.first().name }
                        onConfirmPayment(transitaireId, amount, chosenAccount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanSolidBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirmer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans, color = colors.textSecondary)
            }
        }
    )
}

@Composable
internal fun TransitaireDeleteConfirmDialog(
    transitaireName: String,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        titleContentColor = colors.dangerAccent,
        textContentColor = colors.textSecondary,
        title = {
            Text(
                text = "Supprimer le transitaire ?",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.dangerAccent
            )
        },
        text = {
            Text(
                text = "Voulez-vous supprimer définitivement $transitaireName du catalogue ? Les achats passés associés conserveront leurs données d'historique.",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans, color = colors.textSecondary)
            }
        }
    )
}
