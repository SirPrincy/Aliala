package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans

@Composable
fun AddVariantDialog(
    onDismiss: () -> Unit,
    onConfirm: (variantName: String) -> Unit
) {
    var variantName by remember { mutableStateOf("") }
    val colorSuggestions = listOf("Noir", "Blanc", "Gris", "Rose", "Bleu Nuit", "Rouge", "Vert Émeraude", "Violet", "Or", "Argent")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter une couleur / déclinaison",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = variantName,
                    onValueChange = { variantName = it },
                    label = { Text("Nom de la couleur / variante") },
                    placeholder = { Text("ex. Bleu Nuit, Noir Mat...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = "Suggestions rapides :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorSuggestions.forEach { colorStr ->
                        val suggColor = VariantColorUtils.getVariantColor(colorStr)
                        val isLight = VariantColorUtils.isLightColor(suggColor)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { variantName = colorStr }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(suggColor)
                                        .then(
                                            if (isLight) Modifier.border(1.dp, Color(0xFFCCCCCC), CircleShape)
                                            else Modifier
                                        )
                                )
                                Text(
                                    text = colorStr,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (variantName.isNotBlank()) {
                        onConfirm(variantName.trim())
                        onDismiss()
                    }
                },
                enabled = variantName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ajouter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.Black)
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
fun ArticleEmojiPickerDialog(
    currentEmoji: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val emojis = listOf(
        "🎧", "📱", "⌚", "💻", "🎒", "👜", "👟", "👗",
        "💄", "🧴", "💍", "🕶️", "🏠", "💡", "🔌", "🎮",
        "📷", "🧸", "🚗", "⚽", "🛠️", "📦", "✨", "🎁"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choisir une icône article",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emojis.chunked(6).forEach { rowEmojis ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowEmojis.forEach { emoji ->
                            val isSelected = currentEmoji == emoji
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, CyanPrimary) else null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onSelect(emoji) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
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
