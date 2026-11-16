package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EmpruntsDettesCard(
    debts: List<BusinessDebtEntity>,
    payments: List<BusinessDebtPaymentEntity>,
    ariaryAccounts: List<BusinessAccountEntity>,
    onAddDebt: (
        title: String,
        direction: String,
        counterparty: String,
        principalAr: Long,
        interestPercent: Double,
        dueDateMillis: Long?,
        createdAtMillis: Long,
        repaymentFrequency: String,
        accountName: String,
        affectAccount: Boolean,
        notes: String
    ) -> Unit,
    onRecordPayment: (
        debt: BusinessDebtEntity,
        amountAr: Long,
        accountName: String,
        affectAccount: Boolean,
        dateMillis: Long,
        note: String
    ) -> Unit,
    onUpdateDebt: (BusinessDebtEntity) -> Unit,
    onDeleteDebt: (BusinessDebtEntity) -> Unit,
    onCreateAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    var selectedFilter by remember { mutableStateOf("Tout") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<BusinessDebtEntity?>(null) }
    var payingDebt by remember { mutableStateOf<BusinessDebtEntity?>(null) }
    var inspectingDebt by remember { mutableStateOf<BusinessDebtEntity?>(null) }
    var deletingDebt by remember { mutableStateOf<BusinessDebtEntity?>(null) }

    val totalBorrowedRemainingAr = remember(debts) {
        debts.filter { it.direction == "BORROWED" }.sumOf { it.remainingAr() }
    }
    val totalLentRemainingAr = remember(debts) {
        debts.filter { it.direction == "LENT" }.sumOf { it.remainingAr() }
    }
    val activeDebtsCount = remember(debts) {
        debts.count { it.remainingAr() > 0L }
    }

    val filteredDebts = remember(debts, selectedFilter) {
        when (selectedFilter) {
            "Emprunts" -> debts.filter { it.direction == "BORROWED" && it.remainingAr() > 0L }
            "Prêts" -> debts.filter { it.direction == "LENT" && it.remainingAr() > 0L }
            "Soldés" -> debts.filter { it.remainingAr() <= 0L }
            else -> debts.sortedWith(
                compareBy<BusinessDebtEntity> { it.remainingAr() <= 0L }
                    .thenBy { it.dueDateMillis ?: Long.MAX_VALUE }
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
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
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Emprunts & Dettes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (activeDebtsCount == 0) "Aucun engagement en cours"
                            else "$activeDebtsCount engagement(s) actif(s)",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.violetSoftBg)
                        .clickable { showCreateDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_add_debt")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nouvel emprunt ou prêt",
                        tint = colors.violetAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nouveau",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.violetAccent
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.dangerSoftBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Emprunté (dû)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.dangerAccent
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalBorrowedRemainingAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.dangerAccent
                        )
                        Text(
                            text = "Reste à rembourser",
                            fontSize = 9.sp,
                            color = colors.dangerAccent.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.successSoftBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Prêté à des tiers",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.successAccent
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalLentRemainingAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.successAccent
                        )
                        Text(
                            text = "À recouvrer",
                            fontSize = 9.sp,
                            color = colors.successAccent.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (debts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tout", "Emprunts", "Prêts", "Soldés").forEach { chip ->
                        val isSel = selectedFilter == chip
                        Surface(
                            modifier = Modifier
                                .clickable { selectedFilter = chip }
                                .testTag("chip_debt_filter_$chip"),
                            shape = CircleShape,
                            color = if (isSel) colors.violetAccent else colors.innerCardBg
                        ) {
                            Text(
                                text = chip,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.innerCardBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (debts.isEmpty()) {
                                "Aucun emprunt ni prêt enregistré."
                            } else {
                                "Aucun élément dans ce filtre."
                            },
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (debts.isEmpty()) {
                            Text(
                                text = "+ Enregistrer un emprunt ou prêt",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.violetAccent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCreateDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                filteredDebts.forEach { debt ->
                    DebtItemCard(
                        debt = debt,
                        colors = colors,
                        dateFmt = dateFmt,
                        onInspect = { inspectingDebt = debt },
                        onPay = { payingDebt = debt },
                        onEdit = { editingDebt = debt },
                        onDelete = { deletingDebt = debt }
                    )
                }
            }
        }
    }

    if (showCreateDialog || editingDebt != null) {
        AddOrEditBusinessDebtDialog(
            existing = editingDebt,
            ariaryAccounts = ariaryAccounts,
            onDismiss = {
                showCreateDialog = false
                editingDebt = null
            },
            onConfirmCreate = { title, dir, counterparty, principal, interest, dueMs, createdMs, freq, accName, affectAcc, notes ->
                onAddDebt(title, dir, counterparty, principal, interest, dueMs, createdMs, freq, accName, affectAcc, notes)
                showCreateDialog = false
            },
            onConfirmUpdate = { updated ->
                onUpdateDebt(updated)
                editingDebt = null
            },
            onCreateAccountClick = {
                showCreateDialog = false
                editingDebt = null
                onCreateAccountClick()
            }
        )
    }

    payingDebt?.let { debt ->
        RecordBusinessDebtPaymentDialog(
            debt = debt,
            ariaryAccounts = ariaryAccounts,
            onDismiss = { payingDebt = null },
            onConfirm = { amount, accName, affectAcc, dateMs, note ->
                onRecordPayment(debt, amount, accName, affectAcc, dateMs, note)
                payingDebt = null
            },
            onCreateAccountClick = {
                payingDebt = null
                onCreateAccountClick()
            }
        )
    }

    inspectingDebt?.let { debt ->
        val debtPayments = payments.filter { it.debtId == debt.id }
        BusinessDebtDetailDialog(
            debt = debt,
            payments = debtPayments,
            onDismiss = { inspectingDebt = null },
            onPayClick = {
                inspectingDebt = null
                payingDebt = debt
            },
            onEditClick = {
                inspectingDebt = null
                editingDebt = debt
            },
            onDeleteClick = {
                inspectingDebt = null
                deletingDebt = debt
            }
        )
    }

    deletingDebt?.let { debt ->
        AlertDialog(
            onDismissRequest = { deletingDebt = null },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Supprimer « ${debt.title} » ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                Text(
                    text = "Cette action supprimera l'engagement ainsi que les transactions liées aux comptes Ariary (les soldes seront réajustés automatiquement).",
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDebt(debt)
                        deletingDebt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDebt = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}
