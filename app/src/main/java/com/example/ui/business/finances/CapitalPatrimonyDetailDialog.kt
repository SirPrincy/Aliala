package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date

@Composable
internal fun CapitalPatrimonyDetailDialog(
    metrics: CapitalPatrimonyMetrics,
    investments: List<BusinessInvestmentEntity>,
    dateFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onApportClick: () -> Unit,
    onRetraitClick: () -> Unit,
    onAddInvestmentClick: () -> Unit,
    onInvestmentClick: (BusinessInvestmentEntity) -> Unit,
    onDeleteCapitalTx: (BusinessTransactionEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var selectedTab by remember { mutableStateOf("Patrimoine") }
    var pendingDeleteTx by remember { mutableStateOf<BusinessTransactionEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                Surface(color = colors.cardBg, shadowElevation = 1.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, "Fermer", tint = colors.textPrimary)
                            }
                            Column {
                                Text(
                                    text = "Investissement & Capital",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Patrimoine net : ${CurrencyFormatter.formatAriary(metrics.netPatrimonyAr)}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.cyanAccent
                                )
                            }
                        }

                        Button(
                            onClick = onAddInvestmentClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Investir", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Patrimoine" to "Patrimoine",
                        "Investissements" to "Actifs (${investments.size})",
                        "Capital" to "Capital (${metrics.capitalMovements.size})"
                    ).forEach { (key, label) ->
                        val isSel = selectedTab == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CyanPrimary else colors.cardBg,
                            border = if (isSel) null else BorderStroke(1.dp, colors.border)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        "Patrimoine" -> {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.cardBg,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "ACTIFS DU BUSINESS",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.successAccent
                                    )
                                    CapitalKeyValueRow("Trésorerie liquide (Ariary)", CurrencyFormatter.formatAriary(metrics.totalTreasuryAr), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow("Portefeuille Yuans (¥)", CurrencyFormatter.formatAriary(metrics.yuanValueAr), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow("Stock disponible en rayon", CurrencyFormatter.formatAriary(metrics.stockValueAr), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow("Commandes / Achats en transit", CurrencyFormatter.formatAriary(metrics.inRouteValueAr), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow("Matériel & Projets actifs", CurrencyFormatter.formatAriary(metrics.activeInvestmentsValueAr), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow("Créances clients à encaisser", CurrencyFormatter.formatAriary(metrics.receivablesAr), colors.textSecondary, colors.textPrimary)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.border)
                                    )
                                    CapitalKeyValueRow("Total Actifs", CurrencyFormatter.formatAriary(metrics.totalAssetsAr), colors.textPrimary, colors.cyanAccent)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.cardBg,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "PASSIFS & SYNTHÈSE",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.dangerAccent
                                    )
                                    CapitalKeyValueRow("Dettes / Fret / Compte associé", CurrencyFormatter.formatAriary(metrics.totalLiabilitiesAr), colors.textSecondary, colors.dangerAccent)
                                    CapitalKeyValueRow("Patrimoine Net Réel", CurrencyFormatter.formatAriary(metrics.netPatrimonyAr), colors.textPrimary, colors.violetAccent)
                                    CapitalKeyValueRow("Capital Net Engagé", CurrencyFormatter.formatAriary(metrics.netCapital), colors.textSecondary, colors.textPrimary)
                                    CapitalKeyValueRow(
                                        "Bénéfice cumulé (Ventes + Retours)",
                                        "${if (metrics.totalProfitAr >= 0L) "+" else ""}${CurrencyFormatter.formatAriary(metrics.totalProfitAr)}",
                                        colors.textSecondary,
                                        if (metrics.totalProfitAr >= 0L) colors.successAccent else colors.dangerAccent
                                    )
                                }
                            }
                        }

                        "Investissements" -> {
                            if (investments.isEmpty()) {
                                EmptyCapitalBox(
                                    message = "Aucun investissement matériel ou projet enregistré.",
                                    actionLabel = "+ Ajouter un investissement",
                                    colors = colors,
                                    onAction = onAddInvestmentClick
                                )
                            } else {
                                investments.forEach { inv ->
                                    InvestmentRowCard(
                                        investment = inv,
                                        dateFormatter = dateFormatter,
                                        colors = colors,
                                        onClick = { onInvestmentClick(inv) }
                                    )
                                }
                            }
                        }

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onApportClick,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+ Apport capital", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onRetraitClick,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("- Retrait perso", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.dangerAccent)
                                }
                            }

                            if (metrics.capitalMovements.isEmpty()) {
                                EmptyCapitalBox(
                                    message = "Aucun apport en capital ni retrait personnel enregistré.",
                                    actionLabel = "+ Enregistrer un apport initial",
                                    colors = colors,
                                    onAction = onApportClick
                                )
                            } else {
                                metrics.capitalMovements.forEach { tx ->
                                    val isIn = isCapitalContributionTx(tx)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(colors.cardBg)
                                            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = tx.title,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${dateFormatter.format(Date(tx.dateMillis))} • ${tx.accountName}",
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${if (isIn) "+" else "-"}${CurrencyFormatter.formatAriary(tx.amountAr)}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isIn) colors.successAccent else colors.dangerAccent
                                            )
                                            IconButton(
                                                onClick = { pendingDeleteTx = tx },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, "Supprimer mouvement", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    pendingDeleteTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            containerColor = colors.cardBg,
            title = {
                Text("Supprimer ce mouvement ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.dangerAccent)
            },
            text = {
                val isHorsCaisse = tx.type == "CAPITAL_IN" || tx.type == "CAPITAL_OUT"
                Text(
                    text = if (isHorsCaisse) {
                        "Supprimer « ${tx.title} » (${CurrencyFormatter.formatAriary(tx.amountAr)}) ? Ce mouvement hors caisse sera retiré du suivi du capital."
                    } else {
                        "Supprimer « ${tx.title} » (${CurrencyFormatter.formatAriary(tx.amountAr)}) ajustera le solde du compte « ${tx.accountName} »."
                    },
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCapitalTx(tx)
                        pendingDeleteTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}
