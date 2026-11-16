package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun ArticleHeroVisualCard(
    article: CatalogueArticleSummaryUi,
    familyColor: Color,
    colors: TransitaireColors,
    onPickPhoto: () -> Unit,
    onPickEmoji: () -> Unit,
    onClearVisual: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    val imageUrl = primaryOrder.imageUrl
    val isPhoto = BusinessImageStorage.isPhotoUriOrPath(imageUrl)
    val displayEmoji = if (imageUrl.isNotBlank() && !imageUrl.contains("/")) imageUrl else article.familyEmoji

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        familyColor.copy(alpha = 0.18f),
                        colors.innerCardBg
                    )
                )
            )
            .border(1.dp, colors.border, RoundedCornerShape(22.dp))
            .testTag("article_detail_carousel")
    ) {
        if (isPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = primaryOrder.product,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayEmoji,
                    fontSize = 68.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = primaryOrder.family.ifBlank { "Catalogue" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (article.isHidden) colors.badgeNeutralBg else CyanPrimary.copy(alpha = 0.94f)
            ) {
                Text(
                    text = if (article.isHidden) "Masqué" else "Actif",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (article.isHidden) colors.textPrimary else Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (article.lotsCount > 1) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cardBg.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = "${article.lotsCount} lots",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.violetAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier.clickable(onClick = onPickPhoto)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = if (isPhoto) "Changer photo" else "Photo",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier.clickable(onClick = onPickEmoji)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.EmojiEmotions, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Émoji",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            if (imageUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier.clickable(onClick = onClearVisual)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Retirer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArticleHeaderAndActionsCard(
    article: CatalogueArticleSummaryUi,
    familyColor: Color,
    colors: TransitaireColors,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onAdjust: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = familyColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = article.familyEmoji, fontSize = 13.sp)
                        Text(
                            text = primaryOrder.family.ifBlank { "Sans famille" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = familyColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.innerCardBg
                ) {
                    Text(
                        text = primaryOrder.platform.ifBlank { primaryOrder.supplier.ifBlank { "1688" } },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (article.isReferenceOnly) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.cyanSoftBg
                    ) {
                        Text(
                            text = "Référentiel",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = primaryOrder.product.ifBlank { "Article #${primaryOrder.id}" },
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = colors.textPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "Prix de vente à définir",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colors.cyanAccent
                )
                if (article.latestPriceYuan > 0.0) {
                    val yuanLabel = if (article.latestPriceYuan % 1.0 == 0.0) {
                        "¥ ${article.latestPriceYuan.toInt()}"
                    } else {
                        String.format(Locale.US, "¥ %.2f", article.latestPriceYuan)
                    }
                    Text(
                        text = "Achat : $yuanLabel",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBuy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_buy"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Acheter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }

            Button(
                onClick = onSell,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_sell"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.violetSoftBg, contentColor = colors.violetAccent),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.PointOfSale, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Vendre", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }

            Button(
                onClick = onAdjust,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_adjust"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.badgeNeutralBg, contentColor = colors.textPrimary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Ajuster", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun ArticleSourcingNotesCard(
    article: CatalogueArticleSummaryUi,
    colors: TransitaireColors,
    onEditNotesClick: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    val recordedPlatform = primaryOrder.platform.ifBlank { primaryOrder.supplier.ifBlank { "Non renseignée" } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Sourcing & Notes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Fournisseur, transitaire et remarques",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                TextButton(onClick = onEditNotesClick) {
                    Text("Modifier", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.cyanAccent)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plateforme / Fournisseur",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = recordedPlatform,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transitaire",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = primaryOrder.carrier.ifBlank { "Non assigné" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp)
            ) {
                Text(
                    text = primaryOrder.receptionNotes.ifBlank {
                        "Aucune note ajoutée. Appuyez sur Modifier pour noter un lien 1688, un contact usine ou des instructions qualité."
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = if (primaryOrder.receptionNotes.isNotBlank()) colors.textPrimary else colors.textSecondary
                )
            }
        }
    }
}
