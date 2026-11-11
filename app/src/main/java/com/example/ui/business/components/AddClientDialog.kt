package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, phone: String, initialPurchaseAr: Long) -> Unit
) {
    val context = LocalContext.current
    var nameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Business") } // "Business" ou "Famille & amis"
    var initialPurchaseText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouveau Client",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF131B2E)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nom complet du client *") },
                    placeholder = { Text("ex: Rina Razafy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phoneText,
                    onValueChange = { phoneText = it },
                    label = { Text("Téléphone / WhatsApp") },
                    placeholder = { Text("+261 34 00 000 00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Segmented category selection
                Text(
                    text = "Catégorie",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF131B2E)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { categoryText = "Business" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (categoryText == "Business") Color(0xFF00687A) else Color(0xFFF2F3FF),
                            contentColor = if (categoryText == "Business") Color.White else Color(0xFF3D494C)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Business", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { categoryText = "Famille & amis" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (categoryText == "Famille & amis") Color(0xFF712AE2) else Color(0xFFF2F3FF),
                            contentColor = if (categoryText == "Famille & amis") Color.White else Color(0xFF3D494C)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.25f)
                    ) {
                        Text("Famille & amis", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { categoryText = "Myself" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (categoryText == "Myself") Color(0xFFEA580C) else Color(0xFFF2F3FF),
                            contentColor = if (categoryText == "Myself") Color.White else Color(0xFF3D494C)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.95f)
                    ) {
                        Text("Myself", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                OutlinedTextField(
                    value = initialPurchaseText,
                    onValueChange = { initialPurchaseText = it.filter { c -> c.isDigit() } },
                    label = { Text("Montant premier achat (Ar)") },
                    placeholder = { Text("ex: 150 000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.trim().isBlank()) {
                        Toast.makeText(context, "Veuillez saisir le nom du client", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val initialAr = initialPurchaseText.toLongOrNull() ?: 0L
                    onConfirm(nameText.trim(), categoryText, phoneText.trim(), initialAr)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF712AE2)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ajouter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}
