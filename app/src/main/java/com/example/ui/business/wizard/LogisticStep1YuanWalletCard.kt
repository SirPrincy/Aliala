package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1YuanWalletCard(
    order: BusinessOrderEntity,
    cmupRate: Long,
    isIndicativeRate: Boolean,
    appliedRateText: String,
    onAppliedRateChange: (String) -> Unit,
    effectiveCmupRate: Double,
    yuanAccounts: List<BusinessAccountEntity>,
    selectedYuanAccountName: String,
    onSelectYuanAccount: (String) -> Unit,
    isAlreadyDebited: Boolean,
    availableWalletYuans: Double,
    totalOrderYuan: Double,
    totalOrderAr: Long,
    remainingWalletYuans: Double,
    isDraftCheckboxChecked: Boolean,
    onDraftCheckboxChange: (Boolean) -> Unit,
    isCompactPhone: Boolean,
    colors: TransitaireColors,
    onOpenBuyYuans: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Portefeuille Yuan (¥)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (cmupRate > 0L) "CMUP portefeuille : $cmupRate Ar/¥" else "Aucune recharge effectuée",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenBuyYuans,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("btn_recharge_yuans_step1")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Recharger des Yuans",
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recharger",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.cyanAccent
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Taux CMUP appliqué (Ar / ¥)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (effectiveCmupRate > 0.0) "1 ¥ = ${effectiveCmupRate.toInt()} Ar" else "Non défini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (effectiveCmupRate > 0.0) colors.cyanAccent else colors.dangerAccent
                    )
                }

                OutlinedTextField(
                    value = appliedRateText,
                    onValueChange = { input -> onAppliedRateChange(input.filter { it.isDigit() }) },
                    placeholder = { Text("Ex: 650", fontSize = 13.sp) },
                    suffix = { Text("Ar / ¥", fontFamily = PlusJakartaSans, fontSize = 12.sp, color = colors.textSecondary) },
                    supportingText = if (isIndicativeRate) {
                        {
                            Text(
                                text = "Taux CMUP indicatif modifiable (aucun achat de yuans en base)",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedBorderColor = colors.cyanAccent,
                        unfocusedBorderColor = colors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_cmup_rate_step1")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = buildList {
                        if (cmupRate > 0L) add("CMUP ($cmupRate Ar)" to cmupRate.toInt().toString())
                        if (order.exchangeRate > 0.0 && order.exchangeRate.toLong() != cmupRate) {
                            add("Lot (${order.exchangeRate.toInt()} Ar)" to order.exchangeRate.toInt().toString())
                        }
                        if (cmupRate != 650L) add((if (cmupRate <= 0L) "650 Ar (indicatif)" else "650 Ar") to "650")
                        if (cmupRate != 700L) add("700 Ar" to "700")
                    }
                    presets.forEach { (label, valStr) ->
                        val isSelected = appliedRateText == valStr
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.cyanAccent else colors.cardBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAppliedRateChange(valStr) }
                        ) {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (yuanAccounts.isNotEmpty() && !isAlreadyDebited) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Portefeuille Yuan à débiter :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        yuanAccounts.forEach { acc ->
                            val isSelected = acc.name.equals(selectedYuanAccountName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.cyanSoftBg else colors.innerCardBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) colors.cyanAccent else colors.border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectYuanAccount(acc.name) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) colors.cyanAccent else colors.textPrimary
                                    )
                                    Text(
                                        text = "¥ ${String.format(Locale.US, "%.2f", acc.balanceYuan)}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isCompactPhone) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Step1BalanceBox(
                        label = "Solde disponible",
                        yuanText = "¥ ${String.format(Locale.US, "%.2f", availableWalletYuans)}",
                        arText = if (effectiveCmupRate > 0.0) CurrencyFormatter.formatAriary((availableWalletYuans * effectiveCmupRate).toLong()) else "—",
                        yuanColor = colors.textPrimary,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Step1BalanceBox(
                        label = "Montant à débiter",
                        yuanText = "- ¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}",
                        arText = if (totalOrderAr > 0L) CurrencyFormatter.formatAriary(totalOrderAr) else "—",
                        yuanColor = colors.dangerAccent,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Step1BalanceBox(
                        label = "Solde disponible",
                        yuanText = "¥ ${String.format(Locale.US, "%.2f", availableWalletYuans)}",
                        arText = if (effectiveCmupRate > 0.0) CurrencyFormatter.formatAriary((availableWalletYuans * effectiveCmupRate).toLong()) else "—",
                        yuanColor = colors.textPrimary,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Step1BalanceBox(
                        label = "Montant à débiter",
                        yuanText = "- ¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}",
                        arText = if (totalOrderAr > 0L) CurrencyFormatter.formatAriary(totalOrderAr) else "—",
                        yuanColor = colors.dangerAccent,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isAlreadyDebited) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.successSoftBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.successAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Paiement Yuan déjà effectué",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.successAccent
                            )
                            Text(
                                text = "Un montant de ¥ ${String.format(Locale.US, "%.2f", if (order.yuanDebitedAmount > 0.0) order.yuanDebitedAmount else totalOrderYuan)} a déjà été déduit pour ce lot.",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remainingWalletYuans >= 0) colors.cyanSoftBg else colors.dangerSoftBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (remainingWalletYuans >= 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (remainingWalletYuans >= 0) colors.cyanAccent else colors.dangerAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (remainingWalletYuans >= 0) "Solde de yuans suffisant" else "Solde de yuans insuffisant",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (remainingWalletYuans >= 0) colors.cyanAccent else colors.dangerAccent
                                )
                                Text(
                                    text = if (remainingWalletYuans >= 0) {
                                        val restAr = if (effectiveCmupRate > 0.0) CurrencyFormatter.formatAriary((remainingWalletYuans * effectiveCmupRate).toLong()) else "—"
                                        "Reste prévisionnel après confirmation : ¥ ${String.format(Locale.US, "%.2f", remainingWalletYuans)} ($restAr)"
                                    } else {
                                        "Il manque ¥ ${String.format(Locale.US, "%.2f", -remainingWalletYuans)}. Rechargez votre portefeuille ou cochez l'option sans débit."
                                    },
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        if (remainingWalletYuans < 0) {
                            val missingAmount = ceil(-remainingWalletYuans).toInt().coerceAtLeast(1)
                            Button(
                                onClick = onOpenBuyYuans,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 42.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Acheter ¥ $missingAmount maintenant",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDraftCheckboxChange(!isDraftCheckboxChecked) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isDraftCheckboxChecked,
                        onCheckedChange = onDraftCheckboxChange,
                        colors = CheckboxDefaults.colors(checkedColor = colors.cyanAccent)
                    )
                    Text(
                        text = "Conserver en commande active sans débiter le portefeuille immédiatement",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun Step1BalanceBox(
    label: String,
    yuanText: String,
    arText: String,
    yuanColor: Color,
    bgColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontFamily = Inter, fontSize = 11.sp, color = labelColor)
            Text(
                text = yuanText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = yuanColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = arText,
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
