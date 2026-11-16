package com.example.ui.business.clients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun ClientDetailDialog(
    client: BusinessClientUi,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit,
    onWhatsAppRelance: () -> Unit
) {
    val colors = rememberTransitaireColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.violetSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = client.initials,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.violetAccent
                    )
                }
                Column {
                    Text(
                        text = client.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${client.category}${if (client.phone.isNotBlank()) " • ${client.phone}" else ""}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total achats", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = CurrencyFormatter.formatAriary(client.totalPurchasesAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                        }
                        Column {
                            Text("Réglé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = CurrencyFormatter.formatAriary(client.totalPaidAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.cyanAccent
                            )
                        }
                        Column {
                            Text("Solde dû", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = CurrencyFormatter.formatAriary(client.balanceDueAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (client.balanceDueAr > 0) colors.dangerAccent else colors.cyanAccent
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${client.totalOrdersCount} achat${if (client.totalOrdersCount > 1) "s" else ""}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Dernier achat : ${client.lastPurchaseDate.ifBlank { "Aucun achat" }}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                if (client.balanceDueAr > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                                onRecordPayment()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.violetSolidBg,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Encaisser", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onWhatsAppRelance()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyanAccent,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun RecordClientPaymentDialog(
    client: BusinessClientUi,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amountAr: Long, accountName: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    var amountText by remember(client.id) { mutableStateOf(client.balanceDueAr.toString()) }
    var selectedAccountName by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "Hors caisse")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Column {
                Text(
                    text = "Encaisser : ${client.name}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Solde dû : ${CurrencyFormatter.formatAriary(client.balanceDueAr)}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.dangerAccent
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Montant encaissé (Ar)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_client_payment_amount")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val halfAmount = (client.balanceDueAr / 2L).coerceAtLeast(1L)
                    if (client.balanceDueAr >= 2000L) {
                        OutlinedButton(
                            onClick = {
                                amountText = halfAmount.toString()
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("50%", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            amountText = client.balanceDueAr.toString()
                            errorMessage = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tout solder", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Text(
                    text = "Compte crédité",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ariaryAccounts.forEach { acc ->
                        val isSel = selectedAccountName.equals(acc.name, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable { selectedAccountName = acc.name },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) colors.cyanSoftBg else colors.innerCardBg,
                            border = if (isSel) BorderStroke(1.dp, colors.cyanAccent) else null
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = acc.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) colors.cyanAccent else colors.textPrimary
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                    fontFamily = Inter,
                                    fontSize = 10.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                    val isOffCash = selectedAccountName.equals("Hors caisse", ignoreCase = true)
                    Surface(
                        modifier = Modifier.clickable { selectedAccountName = "Hors caisse" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOffCash) colors.violetSoftBg else colors.innerCardBg,
                        border = if (isOffCash) BorderStroke(1.dp, colors.violetAccent) else null
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "Hors caisse",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isOffCash) colors.violetAccent else colors.textPrimary
                            )
                            Text(
                                text = "Sans impact compte",
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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
                    val amount = amountText.toLongOrNull() ?: 0L
                    when {
                        amount <= 0L -> errorMessage = "Veuillez saisir un montant valide"
                        amount > client.balanceDueAr -> errorMessage = "Le montant dépasse le solde dû (${CurrencyFormatter.formatAriary(client.balanceDueAr)})"
                        else -> {
                            onConfirm(amount, selectedAccountName)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.violetSolidBg, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirmer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}
