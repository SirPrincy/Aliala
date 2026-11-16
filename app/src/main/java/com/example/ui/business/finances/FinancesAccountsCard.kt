package com.example.ui.business.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.round

@Composable
fun FinancesAccountsCard(
    activeAccounts: List<BusinessAccountEntity>,
    ariaryAccounts: List<BusinessAccountEntity>,
    yuanAccounts: List<BusinessAccountEntity>,
    transactions: List<BusinessTransactionEntity>,
    totalTreasuryAr: Long,
    cmupRate: Long,
    selectedAccountFilter: String,
    onSelectAccountFilter: (String) -> Unit,
    onAddAccountClick: () -> Unit,
    onEditAccountClick: (BusinessAccountEntity) -> Unit,
    onDeleteAccountClick: (BusinessAccountEntity) -> Unit
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
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Comptes & Modes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${ariaryAccounts.size} compte(s) Ar • ${yuanAccounts.size} compte(s) ¥",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddAccountClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("btn_add_account")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter un compte",
                        tint = cyanColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Ajouter",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = cyanColor
                    )
                }
            }

            if (activeAccounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun compte enregistré. Créez Espèces, Mvola, Alipay ou Wechat Pay.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeAccounts.forEach { acc ->
                        val isYuan = acc.isYuanAccount
                        val isFilteredHere = selectedAccountFilter.equals(acc.name, ignoreCase = true)
                        val accInAr = remember(transactions, acc.name) {
                            transactions.filter {
                                (it.type == "IN" || it.type == "INCOME" || it.type == "CAPITAL_IN") &&
                                    it.accountName.equals(acc.name, ignoreCase = true) ||
                                    (it.type == "TRANSFER" && it.destinationAccount.equals(acc.name, ignoreCase = true))
                            }.sumOf { it.amountAr }
                        }
                        val accOutAr = remember(transactions, acc.name) {
                            transactions.filter {
                                ((it.type == "OUT" || it.type == "EXPENSE" || it.type == "YUAN" || it.type == "TRANSFER" || it.type == "CAPITAL_OUT") &&
                                    it.accountName.equals(acc.name, ignoreCase = true))
                            }.sumOf { it.amountAr }
                        }

                        Box(
                            modifier = Modifier
                                .width(214.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isFilteredHere) cyanColor.copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isFilteredHere) 1.5.dp else 0.dp,
                                    color = if (isFilteredHere) cyanColor else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onSelectAccountFilter(if (isFilteredHere) "Tous" else acc.name)
                                }
                                .padding(12.dp)
                                .testTag("account_card_${acc.id}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val badgeAccent = when {
                                            acc.name.contains("Alipay", ignoreCase = true) -> Color(0xFF0EA5E9)
                                            acc.name.contains("Wechat", ignoreCase = true) -> Color(0xFF10B981)
                                            isYuan -> colors.cyanAccent
                                            acc.shortCode.equals("MV", ignoreCase = true) -> Color(0xFFF59E0B)
                                            acc.shortCode.equals("OM", ignoreCase = true) -> orangeColor
                                            acc.shortCode.equals("BOA", ignoreCase = true) -> violetColor
                                            else -> emeraldColor
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(badgeAccent.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                acc.shortCode.equals("CASH", ignoreCase = true) -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Payments,
                                                        contentDescription = null,
                                                        tint = badgeAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                acc.shortCode.equals("BOA", ignoreCase = true) -> {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalance,
                                                        contentDescription = null,
                                                        tint = badgeAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                else -> {
                                                    Text(
                                                        text = if (isYuan) "¥" else acc.shortCode.take(3),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        color = badgeAccent
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = acc.subtitle.ifBlank { if (isYuan) "Chine (¥)" else "Compte" },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isYuan) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditAccountClick(acc) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("btn_edit_account_${acc.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier ${acc.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteAccountClick(acc) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("btn_delete_account_${acc.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Supprimer ${acc.name}",
                                                tint = redColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = acc.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val yuanRateForCard = if (cmupRate > 0L) cmupRate.toDouble() else 0.0
                                Text(
                                    text = if (isYuan) {
                                        val yVal = acc.balanceYuan
                                        if (yVal % 1.0 == 0.0) "¥ ${yVal.toInt()}"
                                        else "¥ ${String.format(Locale.US, "%.2f", yVal)}"
                                    } else {
                                        CurrencyFormatter.formatAriary(acc.balanceAr)
                                    },
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (isYuan) {
                                    val estCardAr = round(acc.balanceYuan * yuanRateForCard).toLong()
                                    Text(
                                        text = if (cmupRate > 0L) {
                                            "≈ ${CurrencyFormatter.formatAriary(estCardAr)} ($cmupRate Ar/¥)"
                                        } else {
                                            "Taux CMUP non défini"
                                        },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "+${CurrencyFormatter.formatAriary(accInAr)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldColor
                                        )
                                        Text(
                                            text = "-${CurrencyFormatter.formatAriary(accOutAr)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = redColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val positiveAccounts = remember(ariaryAccounts) {
                ariaryAccounts.filter { it.balanceAr > 0L }
            }
            val positiveTotalAr = remember(positiveAccounts) {
                positiveAccounts.sumOf { it.balanceAr }
            }
            val segmentColors = listOf(Color(0xFFF59E0B), emeraldColor, orangeColor, cyanColor, violetColor)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Répartition des soldes Ariary",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(totalTreasuryAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (positiveTotalAr > 0L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        positiveAccounts.forEachIndexed { idx, acc ->
                            val weight = (acc.balanceAr.toFloat() / positiveTotalAr.toFloat()).coerceAtLeast(0.02f)
                            Box(
                                modifier = Modifier
                                    .weight(weight)
                                    .height(8.dp)
                                    .background(segmentColors[idx % segmentColors.size])
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        positiveAccounts.forEachIndexed { idx, acc ->
                            val pct = ((acc.balanceAr.toDouble() / positiveTotalAr.toDouble()) * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(segmentColors[idx % segmentColors.size])
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${acc.name} ($pct%)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}
