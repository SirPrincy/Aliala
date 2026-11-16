package com.example.ui.business.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans

@Composable
fun AdjustStockDialog(
    order: BusinessOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (stockCount: Int, reservedCount: Int, defectiveCount: Int, assignedClient: String) -> Unit
) {
    var stockCountText by remember { mutableStateOf(order.stockCount.toString()) }
    var reservedCountText by remember { mutableStateOf(order.reservedCount.toString()) }
    var defectiveCountText by remember { mutableStateOf(order.defectiveCount.toString()) }
    var assignedClientText by remember { mutableStateOf(order.assignedClient) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Ajuster le stock",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = order.product + if (order.referenceCode.isNotBlank()) " (${order.referenceCode})" else "",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = stockCountText,
                    onValueChange = { stockCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantité en rayon (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reservedCountText,
                    onValueChange = { reservedCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Pièces réservées client") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = assignedClientText,
                    onValueChange = { assignedClientText = it },
                    label = { Text("Client assigné (ex. Rina)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = defectiveCountText,
                    onValueChange = { defectiveCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Défectueux / SAV (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sCount = stockCountText.toIntOrNull() ?: order.stockCount
                    val rCount = reservedCountText.toIntOrNull() ?: order.reservedCount
                    val dCount = defectiveCountText.toIntOrNull() ?: order.defectiveCount
                    onConfirm(sCount, rCount, dCount, assignedClientText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Enregistrer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}

@Composable
fun SelectOrderToAdjustDialog(
    orders: List<BusinessOrderEntity>,
    onDismiss: () -> Unit,
    onOrderSelected: (BusinessOrderEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choisir l'article à ajuster",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (orders.isEmpty()) {
                    Text(
                        text = "Aucun article disponible en stock",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    onOrderSelected(order)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "SKU: ${order.referenceCode.ifBlank { "N/A" }} • ${order.family}",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${order.stockCount} pcs",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans)
            }
        }
    )
}
