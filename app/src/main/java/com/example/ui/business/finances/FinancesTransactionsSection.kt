package com.example.ui.business.finances

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinancesTransactionsSectionCard(
    cashTransactions: List<BusinessTransactionEntity>,
    activeAccounts: List<BusinessAccountEntity>,
    showTxSearchBar: Boolean,
    showTxAccountFilter: Boolean,
    txSearchQuery: String,
    selectedAccountFilter: String,
    selectedTxFilter: String,
    showAllTransactions: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectAccountFilter: (String) -> Unit,
    onSelectTxFilter: (String) -> Unit,
    onToggleShowAll: () -> Unit,
    onTransactionClick: (BusinessTransactionEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val orangeColor = Color(0xFFEA580C)
    val violetColor = colors.violetAccent
    val cyanColor = colors.cyanAccent
    val emeraldColor = colors.successAccent
    val redColor = colors.dangerAccent

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cyanColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Transactions",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Journal des flux",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (showAllTransactions) "Réduire" else "Voir tout (${cashTransactions.size})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = cyanColor,
                    modifier = Modifier.clickable(onClick = onToggleShowAll)
                )
            }

            if (showTxSearchBar) {
                OutlinedTextField(
                    value = txSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Rechercher libellé, compte, note...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showTxAccountFilter && activeAccounts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (listOf("Tous") + activeAccounts.map { it.name }).forEach { accName ->
                        val isAccSel = selectedAccountFilter.equals(accName, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable { onSelectAccountFilter(accName) },
                            shape = CircleShape,
                            color = if (isAccSel) colors.violetSolidBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = accName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAccSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Tout", "Entrées", "Sorties", "Transferts", "Change ¥").forEach { chip ->
                    val isSel = selectedTxFilter == chip
                    Surface(
                        modifier = Modifier.clickable { onSelectTxFilter(chip) },
                        shape = CircleShape,
                        color = if (isSel) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (selectedAccountFilter != "Tous") {
                Surface(
                    modifier = Modifier.clickable { onSelectAccountFilter("Tous") },
                    shape = CircleShape,
                    color = colors.violetSoftBg
                ) {
                    Text(
                        text = "Compte : $selectedAccountFilter ✕",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.violetAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            val filteredTxs = remember(cashTransactions, selectedTxFilter, selectedAccountFilter, txSearchQuery) {
                val byType = when (selectedTxFilter) {
                    "Entrées" -> cashTransactions.filter { it.type == "IN" || it.type == "INCOME" }
                    "Sorties" -> cashTransactions.filter { it.type == "OUT" || it.type == "EXPENSE" }
                    "Transferts" -> cashTransactions.filter { it.type == "TRANSFER" }
                    "Change ¥" -> cashTransactions.filter { it.type == "YUAN" }
                    else -> cashTransactions
                }
                val byAccount = if (selectedAccountFilter == "Tous") {
                    byType
                } else {
                    byType.filter {
                        it.accountName.equals(selectedAccountFilter, ignoreCase = true) ||
                            it.destinationAccount.equals(selectedAccountFilter, ignoreCase = true)
                    }
                }
                val q = txSearchQuery.trim()
                if (q.isBlank()) {
                    byAccount
                } else {
                    byAccount.filter {
                        it.title.contains(q, ignoreCase = true) ||
                            it.accountName.contains(q, ignoreCase = true) ||
                            it.destinationAccount.contains(q, ignoreCase = true) ||
                            it.referenceTag.contains(q, ignoreCase = true)
                    }
                }
            }

            val displayedTxs = remember(filteredTxs, showAllTransactions) {
                if (showAllTransactions) filteredTxs else filteredTxs.take(8)
            }

            if (filteredTxs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune opération enregistrée pour ce filtre.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }
                val groupedTxs = remember(displayedTxs) {
                    displayedTxs.groupBy { dateFormat.format(Date(it.dateMillis)) }
                }

                groupedTxs.forEach { (dateLabel, dayTxs) ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = dateLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        dayTxs.forEach { tx ->
                            val (badgeText, badgeColor, amountPrefix) = when (tx.type) {
                                "IN", "INCOME" -> Triple("IN", emeraldColor, "+")
                                "OUT", "EXPENSE" -> Triple("OUT", redColor, "-")
                                "TRANSFER" -> Triple("TRF", cyanColor, "")
                                "YUAN" -> Triple("¥", orangeColor, "-")
                                else -> Triple("OP", violetColor, "")
                            }
                            val amountColor = when (tx.type) {
                                "IN", "INCOME" -> emeraldColor
                                "OUT", "EXPENSE", "YUAN" -> redColor
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { onTransactionClick(tx) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(badgeColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = badgeText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = badgeColor
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = tx.title,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val accountDisplay = if (tx.type == "TRANSFER" && tx.destinationAccount.isNotBlank()) {
                                                "${tx.accountName} ➔ ${tx.destinationAccount}"
                                            } else {
                                                tx.accountName
                                            }
                                            val sub = listOfNotNull(
                                                accountDisplay.takeIf { it.isNotBlank() },
                                                tx.referenceTag.takeIf { it.isNotBlank() }
                                            ).joinToString(" • ")
                                            if (sub.isNotBlank()) {
                                                Text(
                                                    text = sub,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$amountPrefix${CurrencyFormatter.formatAriary(tx.amountAr)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = amountColor
                                        )
                                        Text(
                                            text = timeFormat.format(Date(tx.dateMillis)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!showAllTransactions && filteredTxs.size > 8) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleShowAll),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "Afficher tout (${filteredTxs.size} opérations)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = cyanColor,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
