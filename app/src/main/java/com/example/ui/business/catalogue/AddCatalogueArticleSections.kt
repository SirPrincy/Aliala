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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun AddCatalogueModeAndVisualSection(
    creationMode: String,
    onModeChange: (String) -> Unit,
    selectedVisual: String,
    familyEmoji: String,
    productName: String,
    colors: TransitaireColors,
    onPickPhoto: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onClearVisual: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mode d'enregistrement",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.cyanAccent
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeOptionCard(
                    selected = creationMode == "REFERENTIEL",
                    icon = Icons.Default.Inventory2,
                    title = "Fiche Référence",
                    subtitle = "Catalogue sans stock",
                    accent = colors.cyanAccent,
                    softBg = colors.cyanSoftBg,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange("REFERENTIEL") }
                )
                ModeOptionCard(
                    selected = creationMode == "STOCK",
                    icon = Icons.Default.ShoppingCart,
                    title = "Avec Stock Initial",
                    subtitle = "Prêt en rayon",
                    accent = colors.violetAccent,
                    softBg = colors.violetSoftBg,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange("STOCK") }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.cyanSoftBg, colors.innerCardBg)
                )
            )
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
    ) {
        val isPhotoUri = BusinessImageStorage.isPhotoUriOrPath(selectedVisual)

        if (isPhotoUri) {
            AsyncImage(
                model = selectedVisual,
                contentDescription = productName.ifBlank { "Visuel article" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (selectedVisual.isNotBlank() && !selectedVisual.contains("/")) selectedVisual else familyEmoji,
                    fontSize = 58.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = productName.ifBlank { "Aperçu de la fiche produit" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier
                    .height(36.dp)
                    .clickable(onClick = onPickPhoto)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(
                        text = if (isPhotoUri) "Changer photo" else "Photo",
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
                modifier = Modifier
                    .height(36.dp)
                    .clickable(onClick = onOpenEmojiPicker)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.EmojiEmotions, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Émoji",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            if (selectedVisual.isNotBlank()) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onClearVisual)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DeleteOutline, "Retirer le visuel", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddCatalogueIdentityAndPricingSection(
    product: String,
    onProductChange: (String) -> Unit,
    referenceCode: String,
    onReferenceCodeChange: (String) -> Unit,
    family: String,
    onFamilyChange: (String) -> Unit,
    families: List<String>,
    familySummaries: List<CatalogueFamilySummaryUi>,
    cmupRate: Long,
    effectiveCmupRate: Double,
    priceYuanText: String,
    onPriceYuanChange: (String) -> Unit,
    resaleArText: String,
    onResaleArChange: (String) -> Unit,
    purchaseAr: Long,
    estUnitFreightAr: Long,
    selectedFreightMode: String,
    landedUnitCostAr: Long,
    resaleAr: Long,
    marginAr: Long,
    marginPercent: Int,
    hasValidationError: Boolean,
    colors: TransitaireColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "1. Identité & Famille",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.cyanAccent
            )

            OutlinedTextField(
                value = product,
                onValueChange = onProductChange,
                label = { Text("Nom du produit *") },
                placeholder = { Text("ex. Écouteurs TWS Pro 5") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_article_name"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = hasValidationError && product.isBlank()
            )

            OutlinedTextField(
                value = referenceCode,
                onValueChange = onReferenceCodeChange,
                label = { Text("Code SKU / Référence (auto si vide)") },
                placeholder = { Text("ex. TWS-P5-PRO") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = "Famille de produits",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                families.forEach { fam ->
                    val isSelected = family.equals(fam, ignoreCase = true)
                    val famEmoji = familySummaries.firstOrNull { it.name.equals(fam, ignoreCase = true) }?.emoji ?: ""
                    Surface(
                        modifier = Modifier.clickable { onFamilyChange(fam) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.cyanSoftBg else colors.innerCardBg,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.cyanAccent else colors.border)
                    ) {
                        Text(
                            text = if (famEmoji.isNotBlank()) "$famEmoji $fam" else fam,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.cyanAccent else colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
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
                Text(
                    text = "2. Tarifs & Rentabilité",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.cyanAccent
                )
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.badgeNeutralBg
                ) {
                    Text(
                        text = if (cmupRate <= 0L) {
                            "CMUP ${effectiveCmupRate.toLong()} Ar/¥ (indicatif)"
                        } else {
                            "CMUP ${effectiveCmupRate.toLong()} Ar/¥"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = priceYuanText,
                    onValueChange = { raw ->
                        val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                        if (cleaned.count { it == '.' } <= 1) onPriceYuanChange(cleaned)
                    },
                    label = { Text("Achat (¥)") },
                    placeholder = { Text("45.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = resaleArText,
                    onValueChange = { onResaleArChange(it.filter { ch -> ch.isDigit() }) },
                    label = { Text("Vente public (Ar)") },
                    placeholder = { Text("65000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (purchaseAr > 0L || resaleAr > 0L) {
                val marginColor = when {
                    marginPercent >= 30 -> colors.successAccent
                    marginPercent > 0 -> colors.cyanAccent
                    resaleAr > 0L && marginAr <= 0L -> colors.dangerAccent
                    else -> colors.textSecondary
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.innerCardBg
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Achat en Ariary", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                            Text(CurrencyFormatter.formatAriary(purchaseAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                        }
                        if (estUnitFreightAr > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fret est. ($selectedFreightMode)", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                                Text("+ ${CurrencyFormatter.formatAriary(estUnitFreightAr)}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.expressAccent)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (estUnitFreightAr > 0L) "Coût de revient est." else "Coût unitaire",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(landedUnitCostAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                        }
                        if (resaleAr > 0L && landedUnitCostAr > 0L) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Bénéfice unitaire estimé", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = marginColor)
                                Text(
                                    text = "${CurrencyFormatter.formatAriary(marginAr)} ($marginPercent%)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = marginColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    softBg: Color,
    colors: TransitaireColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) softBg else colors.innerCardBg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) accent else colors.border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) accent else colors.textSecondary, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) accent else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AddCatalogueLogisticsSection(
    creationMode: String,
    stockCountText: String,
    onStockCountChange: (String) -> Unit,
    weightText: String,
    onWeightTextChange: (String) -> Unit,
    selectedFreightMode: String,
    onFreightModeChange: (String) -> Unit,
    defaultAirRate: Long,
    defaultSeaRate: Long,
    weightKgVal: Double,
    weightGrams: Int,
    volumeM3: String,
    estUnitFreightAr: Long,
    colors: TransitaireColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "4. Logistique & Stock",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.cyanAccent
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (creationMode == "STOCK") {
                    OutlinedTextField(
                        value = stockCountText,
                        onValueChange = onStockCountChange,
                        label = { Text("Stock initial (pcs) *") },
                        placeholder = { Text("15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = onWeightTextChange,
                    label = { Text("Poids unit. (kg)") },
                    placeholder = { Text("0.20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "AÉRIEN" to "Aérien (${CurrencyFormatter.formatAriary(defaultAirRate)}/kg)",
                    "MARITIME" to "Maritime (${CurrencyFormatter.formatAriary(defaultSeaRate)}/kg)"
                ).forEach { (mode, label) ->
                    val isSelected = selectedFreightMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFreightModeChange(mode) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.expressSoftBg else colors.innerCardBg,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) colors.expressAccent else colors.border
                        )
                    ) {
                        Text(
                            text = label,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) colors.expressAccent else colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (weightKgVal > 0.0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.expressSoftBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Poids : $weightGrams g • Vol. est. : $volumeM3 m³",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Fret estimé : ${CurrencyFormatter.formatAriary(estUnitFreightAr)} / pc",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.expressAccent
                            )
                        }
                        Icon(Icons.Default.LocalShipping, null, tint = colors.expressAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
