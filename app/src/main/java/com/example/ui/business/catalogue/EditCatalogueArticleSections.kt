package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.PlusJakartaSans

@Composable
internal fun EditCatalogueArticleVisualCard(
    productName: String,
    visualValue: String,
    fallbackEmoji: String,
    colors: TransitaireColors,
    onPickPhoto: () -> Unit,
    onPickEmoji: () -> Unit,
    onClearVisual: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPhoto = BusinessImageStorage.isPhotoUriOrPath(visualValue)
            val displayEmoji = if (visualValue.isNotBlank() && !visualValue.contains("/")) visualValue else fallbackEmoji
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.innerCardBg)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPhoto) {
                    AsyncImage(
                        model = visualValue,
                        contentDescription = productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = displayEmoji, fontSize = 34.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Visuel de l'article",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.cyanSoftBg,
                        modifier = Modifier.clickable { onPickPhoto() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, tint = colors.cyanAccent, modifier = Modifier.size(14.dp))
                            Text("Photo", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.cyanAccent)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.violetSoftBg,
                        modifier = Modifier.clickable { onPickEmoji() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.EmojiEmotions, null, tint = colors.violetAccent, modifier = Modifier.size(14.dp))
                            Text("Émoji", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.violetAccent)
                        }
                    }
                    if (visualValue.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.dangerSoftBg,
                            modifier = Modifier.clickable { onClearVisual() }
                        ) {
                            Box(modifier = Modifier.padding(7.dp)) {
                                Icon(Icons.Default.DeleteOutline, "Retirer", tint = colors.dangerAccent, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
