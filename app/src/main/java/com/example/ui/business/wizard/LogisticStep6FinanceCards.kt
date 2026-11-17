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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun Step6SupplierRefundCard(
    platform: String,
    refundYuanInput: String,
    onRefundYuanInputChange: (String) -> Unit,
    refundAr: Long,
    effectiveCmupRate: Long,
    yuanAccounts: List<BusinessAccountEntity>,
    selectedRefundYuanAccount: String,
    onSelectRefundYuanAccount: (String) -> Unit,
    isRefundAlreadyCredited: Boolean
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Remboursement Usine Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${platform.ifBlank { "Fournisseur" }} · Avoir ou remboursement (optionnel)",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (isRefundAlreadyCredited) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.successSoftBg
                    ) {
                        Text(
                            text = "Déjà crédité",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.successAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "AVOIR OU REMBOURSEMENT NÉGOCIÉ",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "¥ ",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colors.violetAccent
                            )
                            OutlinedTextField(
                                value = refundYuanInput,
                                onValueChange = { if (!isRefundAlreadyCredited) onRefundYuanInputChange(it) },
                                readOnly = isRefundAlreadyCredited,
                                enabled = !isRefundAlreadyCredited,
                                placeholder = {
                                    Text(
                                        text = "0",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        color = colors.textSecondary.copy(alpha = 0.45f)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .width(105.dp)
                                    .testTag("input_step6_refund_yuan"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    disabledTextColor = colors.textPrimary
                                ),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = colors.textPrimary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+ ${CurrencyFormatter.formatAriary(refundAr)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.cyanAccent
                            )
                            Text(
                                text = "Taux CMUP $effectiveCmupRate Ar/¥",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            if (yuanAccounts.size > 1 && !isRefundAlreadyCredited) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    yuanAccounts.forEach { yAcc ->
                        val isSelected = yAcc.name.equals(selectedRefundYuanAccount, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.violetSolidBg else colors.innerCardBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectRefundYuanAccount(yAcc.name) }
                        ) {
                            Text(
                                text = yAcc.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else colors.violetAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

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
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = colors.violetAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isRefundAlreadyCredited) {
                            "Ce remboursement a déjà été crédité dans votre portefeuille Yuan et ne sera pas dupliqué lors d'une nouvelle validation."
                        } else {
                            "Les yuans saisis seront crédités une seule fois dans votre portefeuille ${selectedRefundYuanAccount.ifBlank { "Yuan" }} lors de la validation."
                        },
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun Step6CertifiedFifoCard(
    totalVendable: Int,
    totalConforme: Int,
    totalDefautVendable: Int,
    unitFifoCostAr: Long,
    merchandiseTotalAr: Long,
    domesticPortAr: Long,
    freightCostAr: Long,
    customsTaxAr: Long,
    refundAr: Long
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
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Coût Unitaire FIFO Certifié",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cyanSoftBg
                ) {
                    Text(
                        text = "$totalVendable vendables",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "COÛT DE REVIENT FINAL NET",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatAriary(unitFifoCostAr).replace(" Ar", ""),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = "Ar / unité",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = if (totalVendable > 0) {
                            "$totalConforme conforme(s) + $totalDefautVendable défaut(s) mineur(s)"
                        } else {
                            "Aucune unité vendable"
                        },
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = if (totalVendable > 0) colors.cyanAccent else colors.dangerAccent,
                        fontWeight = FontWeight.Medium
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
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FORMULE MATHÉMATIQUE TRANSPARENTE",
                        fontFamily = PlusJakartaSans,
                        fontSize = 9.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    val customsPart = if (customsTaxAr > 0L) " + ${CurrencyFormatter.formatAriary(customsTaxAr)} [Douane]" else ""
                    val divisorPart = if (totalVendable > 0) "÷ $totalVendable unités" else "÷ 0 unité vendable"
                    Text(
                        text = "(${CurrencyFormatter.formatAriary(merchandiseTotalAr)} [Marchandise] + ${CurrencyFormatter.formatAriary(domesticPortAr)} [Port CH] + ${CurrencyFormatter.formatAriary(freightCostAr)} [Fret]$customsPart − ${CurrencyFormatter.formatAriary(refundAr)} [Remb.]) $divisorPart",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Text(
                    text = "Les unités non vendables ou perdues ne sont pas comptées dans le stock disponible et augmentent le coût unitaire des pièces saines.",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
internal fun Step6CarrierSettlementCard(
    order: BusinessOrderEntity,
    freightCostAr: Long,
    isInvoicePaid: Boolean,
    onPayFreightClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val canPay = !isInvoicePaid && freightCostAr > 0L
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (order.carrier.isNotBlank()) "Transitaire ${order.carrier}" else "Transitaire non défini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Facture Fret #${order.referenceCode.ifBlank { "LOT-${order.id}" }}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = when {
                        isInvoicePaid -> colors.successSoftBg
                        freightCostAr <= 0L -> colors.badgeNeutralBg
                        else -> colors.dangerSoftBg
                    }
                ) {
                    Text(
                        text = when {
                            isInvoicePaid -> "RÉGLÉ"
                            freightCostAr <= 0L -> "NON DÉFINI"
                            else -> "À PAYER"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when {
                            isInvoicePaid -> colors.successAccent
                            freightCostAr <= 0L -> colors.textSecondary
                            else -> colors.dangerAccent
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.innerCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Montant TTC de la facture",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (freightCostAr > 0L) CurrencyFormatter.formatAriary(freightCostAr) else "Non défini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (freightCostAr > 0L) colors.textPrimary else colors.textSecondary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (canPay) Modifier.clickable(onClick = onPayFreightClick) else Modifier
                    )
                    .testTag("btn_step6_pay_freight"),
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isInvoicePaid -> colors.successAccent
                    canPay -> colors.violetSolidBg
                    else -> colors.badgeNeutralBg
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val textColor = if (isInvoicePaid || canPay) Color.White else colors.textSecondary
                    Icon(Icons.Default.Payments, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isInvoicePaid -> "Fret payé"
                            canPay -> "Payer le fret"
                            else -> "Tarif fret non défini"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}
