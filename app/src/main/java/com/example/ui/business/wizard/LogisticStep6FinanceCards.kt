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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun Step6SupplierRefundCard(
    refundYuanInput: String,
    onRefundYuanInputChange: (String) -> Unit,
    refundAr: Long,
    effectiveCmupRate: Long,
    yuanAccounts: List<BusinessAccountEntity>,
    selectedRefundYuanAccount: String,
    onSelectRefundYuanAccount: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEADDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = Color(0xFF25005A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Remboursement Usine Chine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF131B2E)
                    )
                    Text(
                        text = "Litige Taobao / 1688 ouvert",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F3FF)
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
                        color = Color(0xFF3D494C),
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
                                color = Color(0xFF712AE2)
                            )
                            OutlinedTextField(
                                value = refundYuanInput,
                                onValueChange = onRefundYuanInputChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(90.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF131B2E)
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+ ${CurrencyFormatter.formatAriary(refundAr)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF00687A)
                            )
                            Text(
                                text = "Taux CMUP $effectiveCmupRate Ar/¥",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }
            }

            if (yuanAccounts.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    yuanAccounts.forEach { yAcc ->
                        val isSelected = yAcc.name.equals(selectedRefundYuanAccount, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF712AE2) else Color(0xFFF2F3FF),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectRefundYuanAccount(yAcc.name) }
                        ) {
                            Text(
                                text = yAcc.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF712AE2),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE2E7FF)
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
                        tint = Color(0xFF712AE2),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Les yuans sont immédiatement recrédités dans votre portefeuille ${selectedRefundYuanAccount.ifBlank { "Yuan" }} pour amortir le coût d'achat global.",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C),
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Coût Unitaire FIFO Certifié",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF131B2E)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFACEDFF)
                ) {
                    Text(
                        text = "$totalVendable vendables",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF001F26),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F3FF)
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
                        color = Color(0xFF3D494C),
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
                            color = Color(0xFF00687A)
                        )
                        Text(
                            text = "Ar / unité",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                    Text(
                        text = "$totalConforme conformes + $totalDefautVendable vendable à perte",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF00687A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEAEDFF)
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
                        color = Color(0xFF6D797D),
                        letterSpacing = 0.5.sp
                    )
                    val customsPart = if (customsTaxAr > 0L) " + ${CurrencyFormatter.formatAriary(customsTaxAr)} [Douane]" else ""
                    Text(
                        text = "(${CurrencyFormatter.formatAriary(merchandiseTotalAr)} [Marchandise] + ${CurrencyFormatter.formatAriary(domesticPortAr)} [Port CH] + ${CurrencyFormatter.formatAriary(freightCostAr)} [Fret]$customsPart − ${CurrencyFormatter.formatAriary(refundAr)} [Remb.]) ÷ $totalVendable unités",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C)
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
                    tint = Color(0xFF6D797D),
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Text(
                    text = "Les unités non vendables ou perdues ne sont pas comptées dans le stock disponible et augmentent le coût unitaire des pièces saines.",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = Color(0xFF3D494C),
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
    onSaveInvoiceClick: () -> Unit,
    onPayFreightClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Transitaire ${order.carrier.ifBlank { "partenaire" }}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Facture Fret #${order.referenceCode.ifBlank { "LOT-${order.id}" }}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isInvoicePaid) Color(0xFFE8F5E9) else Color(0xFFFFDAD6)
                ) {
                    Text(
                        text = if (isInvoicePaid) "RÉGLÉ" else "À PAYER",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isInvoicePaid) Color(0xFF2E7D32) else Color(0xFF93000A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF2F3FF)
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
                        color = Color(0xFF131B2E)
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(freightCostAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF131B2E)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(onClick = onSaveInvoiceClick),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEAEDFF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFF131B2E), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enregistrer facture",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(onClick = onPayFreightClick),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isInvoicePaid) Color(0xFF2E7D32) else Color(0xFF712AE2)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isInvoicePaid) "Payé ✓" else "Payer le fret",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
