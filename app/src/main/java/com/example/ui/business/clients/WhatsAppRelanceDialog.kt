package com.example.ui.business.clients

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun WhatsAppRelanceDialog(
    clientName: String? = null,
    clientPhone: String? = null,
    balanceDueAr: Long = 0L,
    customMessage: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val message = customMessage ?: if (!clientName.isNullOrBlank() && balanceDueAr > 0L) {
        "Bonjour $clientName ! J'espère que tu vas bien. Petit rappel amical concernant ton solde de ${CurrencyFormatter.formatAriary(balanceDueAr)}. Merci d'avance pour le règlement !"
    } else if (!clientName.isNullOrBlank()) {
        "Bonjour $clientName ! J'espère que tu vas bien. Petit message amical pour le règlement de ta commande."
    } else {
        "Bonjour ! J'espère que tu vas bien. Petit message amical pour le règlement de ta commande chez nous. N'hésite pas à m'envoyer la confirmation dès que c'est fait. Merci !"
    }

    val openWhatsApp = {
        val rawDigits = (clientPhone ?: "").replace(Regex("[^0-9]"), "")
        val normalizedPhone = when {
            rawDigits.startsWith("261") -> rawDigits
            rawDigits.startsWith("0") && rawDigits.length >= 10 -> "261" + rawDigits.substring(1)
            rawDigits.isNotBlank() -> "261$rawDigits"
            else -> ""
        }
        val uri = if (normalizedPhone.isNotBlank()) {
            Uri.parse("https://wa.me/$normalizedPhone?text=${Uri.encode(message)}")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Relance WhatsApp", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Message copié dans le presse-papier", Toast.LENGTH_SHORT).show()
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (!clientName.isNullOrBlank()) "Relance WhatsApp : $clientName" else "Relance WhatsApp",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Message prêt à envoyer :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = openWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ouvrir WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Relance WhatsApp", message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Message copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Copier", fontFamily = PlusJakartaSans)
            }
        }
    )
}
