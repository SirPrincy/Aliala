package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogisticStep1View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double = 0.0,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    families: List<CatalogueFamilySummaryUi> = emptyList(),
    accounts: List<BusinessAccountEntity> = emptyList(),
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onConfirmOrder: (updatedOrder: BusinessOrderEntity) -> Unit,
    onSaveAsDraft: () -> Unit,
    onSpendYuans: (amountYuan: Double, rateAr: Double, note: String) -> Unit = { _, _, _ -> },
    onBuyYuans: (amountYuan: Double, rateAr: Double, platform: String, sourceAccountName: String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()

    var isDraftCheckboxChecked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showBuyYuansDialog by remember { mutableStateOf(false) }
    var showEditOrderDialog by remember { mutableStateOf(false) }
    var showEditDomesticShippingDialog by remember { mutableStateOf(false) }
    var showEditReferenceDialog by remember { mutableStateOf(false) }
    var showEditDispatchDialog by remember { mutableStateOf(false) }
    var showSelectTransitaireDialog by remember { mutableStateOf(false) }
    var showAddTrackingDialog by remember { mutableStateOf(false) }
    var editingOldTracking by remember { mutableStateOf<String?>(null) }
    var newTrackingInput by remember { mutableStateOf("") }
    var newTrackingCarrierInput by remember { mutableStateOf("SF Express") }

    val isAlreadyDebited = order.yuanDebited

    var appliedRateText by remember(order.id, order.exchangeRate, cmupRate) {
        val defaultRate = when {
            order.exchangeRate > 0.0 -> order.exchangeRate
            cmupRate > 0L -> cmupRate.toDouble()
            else -> 0.0
        }
        mutableStateOf(if (defaultRate > 0.0) defaultRate.toInt().toString() else "")
    }

    val effectiveCmupRate = appliedRateText.toDoubleOrNull()?.takeIf { it > 0.0 }
        ?: (if (cmupRate > 0L) cmupRate.toDouble() else 0.0)

    val unitPriceYuan = if (order.priceYuan > 0.0) order.priceYuan else 0.0
    val totalQty = if (order.quantityBought > 0) order.quantityBought else 0
    val shippingNationalYuan = order.domesticShippingYuan.coerceAtLeast(0.0)
    val isOrderValid = totalQty > 0 && unitPriceYuan > 0.0 && effectiveCmupRate > 0.0

    val goodsTotalYuan = if (totalQty > 0 && unitPriceYuan > 0.0) totalQty * unitPriceYuan else 0.0
    val totalOrderYuan = goodsTotalYuan + shippingNationalYuan
    val totalOrderAr = if (effectiveCmupRate > 0.0) (totalOrderYuan * effectiveCmupRate).toLong() else 0L

    val availableWalletYuans = availableYuans
    val remainingWalletYuans = availableWalletYuans - totalOrderYuan
    val unitPriceAr = if (unitPriceYuan > 0.0 && effectiveCmupRate > 0.0) (unitPriceYuan * effectiveCmupRate).toLong() else 0L
    val shippingNationalAr = if (effectiveCmupRate > 0.0) (shippingNationalYuan * effectiveCmupRate).toLong() else 0L

    // Famille réelle & emoji
    val matchedFamily = remember(families, order.familyId, order.family) {
        families.firstOrNull {
            (order.familyId > 0L && it.id == order.familyId) ||
                it.name.equals(order.family, ignoreCase = true)
        }
    }
    val familyEmoji = matchedFamily?.emoji?.takeIf { it.isNotBlank() } ?: when {
        order.family.contains("MODE", ignoreCase = true) -> "🎒"
        order.family.contains("MAISON", ignoreCase = true) -> "🏠"
        order.family.contains("MOBILE", ignoreCase = true) || order.family.contains("TÉLÉPHONE", ignoreCase = true) -> "📱"
        order.family.contains("ÉLECTRON", ignoreCase = true) || order.family.contains("AUDIO", ignoreCase = true) -> "🎧"
        else -> "📦"
    }

    // Transitaire réel correspondant à order.carrier
    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
    }
    val isMaritime = order.freightMode.equals("MARITIME", ignoreCase = true) ||
        order.freightMode.equals("SEA", ignoreCase = true)

    // Colis synchronisés avec l'Étape 2 via DomesticParcelStorage (jamais de désynchro)
    val parcels = remember(
        order.id,
        order.trackingNumbers,
        order.deliveredTrackingNumbers,
        order.parcelDetailsJson,
        order.variants,
        order.product,
        order.quantityBought,
        order.orderDate
    ) {
        DomesticParcelStorage.parse(order)
    }

    val currentStatusEnum = OrderStatus.fromCode(order.status)
    val isDraftStatus = currentStatusEnum == OrderStatus.DRAFT

    fun copyToClipboard(label: String, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun OrderSummaryCard() {
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
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Récapitulatif Commande",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = colors.cyanSoftBg
                        ) {
                            Text(
                                text = order.platform.ifBlank { "Achat Chine" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(
                            onClick = { showEditOrderDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_edit_step1_order")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la commande",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = familyEmoji, fontSize = 32.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = order.product.ifBlank { "Article sans nom" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (order.referenceCode.isNotBlank()) "Réf : ${order.referenceCode}" else "Référence non définie",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = order.supplier.ifBlank { "Fournisseur non renseigné" },
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Quantités & Variantes
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantités commandées",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (totalQty > 0) "$totalQty unités" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (totalQty > 0) colors.cyanAccent else colors.dangerAccent
                            )
                        }

                        val parsedVariants = remember(order.variants) {
                            VariantColorUtils.parseVariants(order.variants)
                        }

                        if (parsedVariants.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parsedVariants.forEachIndexed { idx, variantText ->
                                    val dotColor = VariantColorUtils.getVariantColor(variantText, idx)
                                    val isLight = VariantColorUtils.isLightColor(dotColor)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = colors.cardBg,
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                                    .then(
                                                        if (isLight) Modifier.border(0.5.dp, colors.border, CircleShape)
                                                        else Modifier
                                                    )
                                            )
                                            Text(variantText, fontFamily = Inter, fontSize = 11.sp, color = colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (totalQty > 0) "Aucune variante spécifique ($totalQty pièce(s))" else "Aucune quantité renseignée",
                                fontFamily = Inter,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Cost breakdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prix unitaire usine", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (unitPriceYuan > 0) "¥ ${String.format(Locale.US, "%.2f", unitPriceYuan)}" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (unitPriceAr > 0) "~ ${CurrencyFormatter.formatAriary(unitPriceAr)}" else "—",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showEditDomesticShippingDialog = true }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Port intérieur (Chine)",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.badgeNeutralBg
                            ) {
                                Text(
                                    text = "Modifier",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = colors.cyanAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (shippingNationalYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", shippingNationalYuan)}" else "0 ¥ (Gratuit)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (shippingNationalAr > 0L) CurrencyFormatter.formatAriary(shippingNationalAr) else "0 Ar",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.badgeNeutralBg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Total commande",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (totalOrderYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = colors.cyanAccent
                            )
                            Text(
                                text = if (totalOrderAr > 0L) CurrencyFormatter.formatAriary(totalOrderAr) else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun YuanWalletCardSection(isCompactPhone: Boolean) {
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
                        onClick = { showBuyYuansDialog = true },
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

                // Taux CMUP appliqué
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
                        onValueChange = { input -> appliedRateText = input.filter { it.isDigit() } },
                        placeholder = { Text("Ex: 650", fontSize = 13.sp) },
                        suffix = { Text("Ar / ¥", fontFamily = PlusJakartaSans, fontSize = 12.sp, color = colors.textSecondary) },
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
                            if (cmupRate != 650L) add("650 Ar" to "650")
                            if (cmupRate != 700L) add("700 Ar" to "700")
                        }
                        presets.forEach { (label, valStr) ->
                            val isSelected = appliedRateText == valStr
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.cyanAccent else colors.cardBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { appliedRateText = valStr }
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

                // Solde disponible / Montant à débiter (adaptatif sur très petit écran)
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
                                    onClick = { showBuyYuansDialog = true },
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
                            .clickable { isDraftCheckboxChecked = !isDraftCheckboxChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isDraftCheckboxChecked,
                            onCheckedChange = { isDraftCheckboxChecked = it },
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
    fun SupplierLogisticsCard(isCompactPhone: Boolean) {
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Suivi Logistique Usine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                // Numéro de commande fournisseur
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Numéro de commande fournisseur (${order.platform.ifBlank { "Plateforme" }})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showEditReferenceDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (order.referenceCode.isNotBlank()) order.referenceCode else "Aucun numéro renseigné",
                                    fontFamily = if (order.referenceCode.isNotBlank()) FontFamily.Monospace else Inter,
                                    fontWeight = if (order.referenceCode.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (order.referenceCode.isNotBlank()) FontStyle.Normal else FontStyle.Italic,
                                    fontSize = 13.sp,
                                    color = if (order.referenceCode.isNotBlank()) colors.textPrimary else colors.textSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier le numéro de commande",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Date commande & Expédition usine
                if (isCompactPhone) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Step1InfoTile(
                            label = "Date commande",
                            value = DateUtils.formatShortDate(order.orderDate),
                            valueColor = colors.textPrimary,
                            bgColor = colors.innerCardBg,
                            labelColor = colors.textSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Step1DispatchTile(
                            delay = order.supplierDispatchDelay,
                            colors = colors,
                            onClick = { showEditDispatchDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Step1InfoTile(
                            label = "Date commande",
                            value = DateUtils.formatShortDate(order.orderDate),
                            valueColor = colors.textPrimary,
                            bgColor = colors.innerCardBg,
                            labelColor = colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Step1DispatchTile(
                            delay = order.supplierDispatchDelay,
                            colors = colors,
                            onClick = { showEditDispatchDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Transitaire & Entrepôt Chine
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transitaire & Entrepôt Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMaritime) colors.violetSoftBg else colors.cyanSoftBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = if (isMaritime) colors.violetAccent else colors.cyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isMaritime) "MARITIME" else "AÉRIEN",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isMaritime) colors.violetAccent else colors.cyanAccent
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSelectTransitaireDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = if (order.carrier.isNotBlank()) colors.cyanAccent else colors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                if (selectedTransitaire != null) {
                                    Text(
                                        text = selectedTransitaire.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    val subInfo = selectedTransitaire.chinaWarehouseAddress.ifBlank {
                                        selectedTransitaire.routeHub.ifBlank { "Adresse entrepôt non renseignée" }
                                    }
                                    Text(
                                        text = subInfo,
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val rateStr = if (isMaritime) {
                                        "${CurrencyFormatter.formatAriary(selectedTransitaire.seaRateAr)}/m³ · ${selectedTransitaire.seaLeadDays}"
                                    } else {
                                        "${CurrencyFormatter.formatAriary(selectedTransitaire.airRateAr)}/kg · ${selectedTransitaire.airLeadDays}"
                                    }
                                    Text(
                                        text = rateStr,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = colors.cyanAccent
                                    )
                                } else if (order.carrier.isNotBlank()) {
                                    Text(
                                        text = order.carrier,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Touchez pour associer un transitaire du catalogue",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                } else {
                                    Text(
                                        text = "Aucun transitaire sélectionné",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.warningAccent
                                    )
                                    Text(
                                        text = if (transitaires.isEmpty()) "Créez d'abord un transitaire ou saisissez un nom"
                                        else "Touchez pour choisir (${transitaires.size} disponible(s))",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                            }
                            if (selectedTransitaire != null && selectedTransitaire.chinaWarehouseAddress.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        copyToClipboard(
                                            label = "Adresse entrepôt Chine",
                                            text = selectedTransitaire.chinaWarehouseAddress,
                                            toastMsg = "Adresse entrepôt copiée !"
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier l'adresse entrepôt",
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Changer de transitaire",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Numéros de suivi intérieur synchronisés avec Étape 2 via DomesticParcelStorage
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Numéros de suivi intérieur (Tracking)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${parcels.size} colis",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    if (parcels.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.innerCardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aucun numéro de suivi enregistré pour le moment.",
                                fontFamily = Inter,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        parcels.forEachIndexed { idx, parcel ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.innerCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(colors.violetSoftBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Inventory2,
                                                contentDescription = null,
                                                tint = colors.violetAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = parcel.trackingNumber,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${parcel.carrierName} · Colis ${idx + 1}",
                                                fontFamily = Inter,
                                                fontSize = 10.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (parcel.isDelivered) colors.successSoftBg else colors.badgeNeutralBg
                                        ) {
                                            Text(
                                                text = if (parcel.isDelivered) "Reçu" else "En attente",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = if (parcel.isDelivered) colors.successAccent else colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                editingOldTracking = parcel.trackingNumber
                                                newTrackingInput = parcel.trackingNumber
                                                newTrackingCarrierInput = parcel.carrierName
                                                showAddTrackingDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = colors.cyanAccent, modifier = Modifier.size(15.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                onUpdateOrder(DomesticParcelStorage.removeTracking(order, parcel.trackingNumber))
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.badgeNeutralBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                editingOldTracking = null
                                newTrackingInput = ""
                                newTrackingCarrierInput = "SF Express"
                                showAddTrackingDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (parcels.isEmpty()) "Ajouter un numéro de suivi" else "Ajouter un autre numéro de suivi",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.cyanAccent
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Step1BottomActionsCard() {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isBalanceSufficient = remainingWalletYuans >= 0.0
                val canConfirm = isOrderValid && !isSubmitting && (isAlreadyDebited || isDraftCheckboxChecked || isBalanceSufficient)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAlreadyDebited) "Paiement Yuan réglé :" else if (isDraftCheckboxChecked) "Montant (sans débit) :" else "Montant à débiter :",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (totalOrderYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isAlreadyDebited) colors.cyanAccent else colors.violetAccent
                        )
                        Text(
                            text = if (totalOrderAr > 0L) "(${CurrencyFormatter.formatAriary(totalOrderAr)})" else "—",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!canConfirm || isSubmitting) return@Button
                        isSubmitting = true
                        val nextStatus = OrderStatus.WAREHOUSE.code
                        val shouldDebitNow = !isAlreadyDebited && !isDraftCheckboxChecked && totalOrderYuan > 0

                        if (shouldDebitNow) {
                            onSpendYuans(
                                totalOrderYuan,
                                effectiveCmupRate,
                                "Paiement ${order.platform.ifBlank { "1688" }}: ${order.product} (Lot #${order.id})"
                            )
                        }

                        val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                            status = nextStatus,
                            priceYuan = unitPriceYuan,
                            quantityBought = totalQty,
                            domesticShippingYuan = shippingNationalYuan,
                            exchangeRate = effectiveCmupRate,
                            yuanDebited = isAlreadyDebited || shouldDebitNow,
                            yuanDebitedAmount = if (shouldDebitNow) totalOrderYuan else if (isAlreadyDebited) order.yuanDebitedAmount else 0.0
                        )
                        onConfirmOrder(syncedOrder)

                        val msg = when {
                            isAlreadyDebited -> "Commande mise à jour · Passage à l'étape 2"
                            shouldDebitNow -> "Commande confirmée et payée en Yuans !"
                            else -> "Commande validée sans débit immédiat"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    enabled = canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("btn_confirm_pay_order"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDraftCheckboxChecked && !isAlreadyDebited) colors.violetAccent else colors.cyanAccent,
                        disabledContainerColor = colors.badgeNeutralBg
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                !isOrderValid -> "Complétez prix, quantité ou CMUP"
                                isSubmitting -> "Traitement en cours..."
                                isAlreadyDebited -> "Valider & Passer à l'étape 2 (Déjà Payé)"
                                !isBalanceSufficient && !isDraftCheckboxChecked -> "Solde Yuan insuffisant (Rechargez ou 'Sans débit')"
                                isDraftCheckboxChecked -> "Enregistrer Commande (Sans Débit)"
                                else -> "Confirmer et Payer la Commande"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (canConfirm) Color.White else colors.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isDraftCheckboxChecked && !isAlreadyDebited) Icons.Default.Bookmark else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (canConfirm) Color.White else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Évite de rétrograder une commande déjà payée en brouillon !
                TextButton(
                    onClick = {
                        val baseUpdated = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                            exchangeRate = effectiveCmupRate,
                            domesticShippingYuan = shippingNationalYuan
                        )
                        if (isAlreadyDebited) {
                            onUpdateOrder(baseUpdated)
                            Toast.makeText(context, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
                        } else {
                            onUpdateOrder(baseUpdated)
                            onSaveAsDraft()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(min = 44.dp)
                ) {
                    Text(
                        text = if (isAlreadyDebited) "Enregistrer les modifications sans avancer" else "Enregistrer comme brouillon sans finaliser",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        val isWideLayout = maxWidth >= 680.dp
        val isCompactPhone = maxWidth < 340.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isOrderValid) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.dangerSoftBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.dangerAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Informations requises manquantes",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.dangerAccent
                            )
                            Text(
                                text = "Vérifiez le prix unitaire (¥), la quantité et le taux CMUP (Ar/¥).",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(onClick = { showEditOrderDialog = true }) {
                            Text("Compléter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.dangerAccent)
                        }
                    }
                }
            }

            WizardStepHeaderCard(
                stepNumber = 1,
                stepTitle = "Validation & Débit",
                statusText = if (isDraftStatus) "Statut : Brouillon (en attente)" else if (isAlreadyDebited) "Statut : Payé en ¥ · Prêt pour expédition" else "Statut : Commandé chez le fournisseur",
                statusIcon = if (isDraftStatus) Icons.Default.Bookmark else Icons.Default.LocalShipping,
                statusAccentColor = if (isDraftStatus) colors.violetAccent else colors.cyanAccent,
                statusContainerColor = if (isDraftStatus) colors.violetSoftBg else colors.cyanSoftBg
            )

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OrderSummaryCard()
                        SupplierLogisticsCard(isCompactPhone = false)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        YuanWalletCardSection(isCompactPhone = false)
                        Step1BottomActionsCard()
                    }
                }
            } else {
                OrderSummaryCard()
                YuanWalletCardSection(isCompactPhone = isCompactPhone)
                SupplierLogisticsCard(isCompactPhone = isCompactPhone)
                Step1BottomActionsCard()
            }
        }
    }

    // ==========================================
    // MODALS & DIALOGUES DYNAMIQUES ÉTAPE 1
    // ==========================================

    if (showBuyYuansDialog) {
        BuyYuansDialog(
            accounts = accounts,
            defaultRate = cmupRate,
            onDismiss = { showBuyYuansDialog = false },
            onConfirm = { amountYuan, rateAr, platform, sourceAccountName ->
                onBuyYuans(amountYuan, rateAr, platform, sourceAccountName)
                if (appliedRateText.isBlank() || appliedRateText == "0") {
                    appliedRateText = rateAr.toInt().toString()
                }
                showBuyYuansDialog = false
            }
        )
    }

    if (showEditOrderDialog) {
        var prodInput by remember { mutableStateOf(order.product) }
        var refInput by remember { mutableStateOf(order.referenceCode) }
        var platformInput by remember { mutableStateOf(order.platform) }
        var supplierInput by remember { mutableStateOf(order.supplier) }
        var qtyInput by remember { mutableStateOf(if (order.quantityBought > 0) order.quantityBought.toString() else "") }
        var priceYuanInput by remember { mutableStateOf(if (order.priceYuan > 0.0) formatYuanCompact(order.priceYuan) else "") }
        var domesticYuanInput by remember { mutableStateOf(formatYuanCompact(order.domesticShippingYuan)) }

        AlertDialog(
            onDismissRequest = { showEditOrderDialog = false },
            title = {
                Text("Modifier la commande", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prodInput,
                        onValueChange = { prodInput = it },
                        label = { Text("Nom de l'article") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Quantité") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceYuanInput,
                            onValueChange = { priceYuanInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Prix usine (¥)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = domesticYuanInput,
                        onValueChange = { domesticYuanInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Frais de port intérieurs Chine (¥)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = platformInput,
                            onValueChange = { platformInput = it },
                            label = { Text("Plateforme") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = refInput,
                            onValueChange = { refInput = it },
                            label = { Text("Référence") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = supplierInput,
                        onValueChange = { supplierInput = it },
                        label = { Text("Fournisseur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = qtyInput.toIntOrNull()?.coerceAtLeast(1) ?: order.quantityBought
                        val newPrice = priceYuanInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: order.priceYuan
                        val newDomestic = domesticYuanInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                        onUpdateOrder(
                            order.copy(
                                product = prodInput.trim().ifBlank { order.product },
                                referenceCode = refInput.trim(),
                                platform = platformInput.trim(),
                                supplier = supplierInput.trim(),
                                quantityBought = newQty,
                                priceYuan = newPrice,
                                domesticShippingYuan = newDomestic
                            )
                        )
                        showEditOrderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOrderDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showEditDomesticShippingDialog) {
        var domesticInput by remember {
            mutableStateOf(formatYuanCompact(order.domesticShippingYuan))
        }
        AlertDialog(
            onDismissRequest = { showEditDomesticShippingDialog = false },
            title = {
                Text("Frais de port intérieurs (Chine)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Montant facturé par le fournisseur pour la livraison vers l'entrepôt transitaire en Chine.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = domesticInput,
                        onValueChange = { domesticInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Frais de port (¥)") },
                        suffix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0" to "0 ¥ (Gratuit)", "8" to "8 ¥", "10" to "10 ¥", "15" to "15 ¥", "20" to "20 ¥").forEach { (v, lbl) ->
                            val selected = domesticInput == v
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { domesticInput = v }
                            ) {
                                Text(
                                    text = lbl,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else colors.cyanAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valYuan = domesticInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                        onUpdateOrder(order.copy(domesticShippingYuan = valYuan))
                        showEditDomesticShippingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Appliquer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDomesticShippingDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showEditReferenceDialog) {
        var refCodeText by remember { mutableStateOf(order.referenceCode) }
        var platformText by remember { mutableStateOf(order.platform) }
        AlertDialog(
            onDismissRequest = { showEditReferenceDialog = false },
            title = {
                Text("Commande fournisseur", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = platformText,
                        onValueChange = { platformText = it },
                        label = { Text("Plateforme (ex: 1688, Taobao, Alibaba)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1688", "Taobao", "Pinduoduo", "Alibaba", "WeChat").forEach { plat ->
                            val selected = platformText.equals(plat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { platformText = plat }
                            ) {
                                Text(
                                    text = plat,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else colors.cyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = refCodeText,
                        onValueChange = { refCodeText = it },
                        label = { Text("Numéro de commande / Réf") },
                        placeholder = { Text("Ex: 248910384710") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOrder(order.copy(platform = platformText.trim(), referenceCode = refCodeText.trim()))
                        showEditReferenceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditReferenceDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showEditDispatchDialog) {
        var dispatchText by remember { mutableStateOf(order.supplierDispatchDelay) }
        AlertDialog(
            onDismissRequest = { showEditDispatchDialog = false },
            title = {
                Text("Délai d'expédition usine", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dispatchText,
                        onValueChange = { dispatchText = it },
                        label = { Text("Délai d'envoi depuis l'usine") },
                        placeholder = { Text("Ex: 2 - 3 j ouvrés") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Immédiat (24h)", "1 - 2 j ouvrés", "2 - 3 j ouvrés", "3 - 5 j ouvrés", "5 - 7 j ouvrés", "Expédié").forEach { preset ->
                            val selected = dispatchText == preset
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { dispatchText = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else colors.cyanAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOrder(order.copy(supplierDispatchDelay = dispatchText.trim()))
                        showEditDispatchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Valider", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDispatchDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showSelectTransitaireDialog) {
        WizardSelectTransitaireDialog(
            order = order,
            transitaires = transitaires,
            onDismiss = { showSelectTransitaireDialog = false },
            onApply = { updated ->
                onUpdateOrder(updated)
                showSelectTransitaireDialog = false
            }
        )
    }

    if (showAddTrackingDialog) {
        var trackingError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAddTrackingDialog = false },
            title = {
                Text(
                    text = if (editingOldTracking != null) "Modifier le numéro de suivi" else "Ajouter un numéro de suivi",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Transporteur intérieur :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DomesticParcelStorage.commonCarriers.take(6).forEach { carrier ->
                            val selected = newTrackingCarrierInput.equals(carrier, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { newTrackingCarrierInput = carrier }
                            ) {
                                Text(
                                    text = carrier,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else colors.cyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newTrackingCarrierInput,
                        onValueChange = { newTrackingCarrierInput = it },
                        label = { Text("Transporteur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTrackingInput,
                        onValueChange = {
                            newTrackingInput = it
                            trackingError = null
                        },
                        label = { Text("Numéro de suivi *") },
                        placeholder = { Text("Ex: SF1234567890") },
                        singleLine = true,
                        isError = trackingError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    trackingError?.let { msg ->
                        Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = newTrackingInput.trim()
                        if (cleaned.isEmpty()) {
                            trackingError = "Veuillez saisir un numéro de suivi."
                        } else if (
                            !cleaned.equals(editingOldTracking ?: "", ignoreCase = true) &&
                            parcels.any { it.trackingNumber.equals(cleaned, ignoreCase = true) }
                        ) {
                            trackingError = "Ce numéro est déjà enregistré."
                        } else {
                            onUpdateOrder(
                                DomesticParcelStorage.upsertTracking(
                                    order = order,
                                    oldTracking = editingOldTracking,
                                    newTracking = cleaned,
                                    carrierName = newTrackingCarrierInput.trim()
                                )
                            )
                            showAddTrackingDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text(if (editingOldTracking != null) "Enregistrer" else "Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTrackingDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun Step1BalanceBox(
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

@Composable
private fun Step1InfoTile(
    label: String,
    value: String,
    valueColor: Color,
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
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = valueColor
            )
        }
    }
}

@Composable
private fun Step1DispatchTile(
    delay: String,
    colors: TransitaireColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.innerCardBg)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Expédition usine", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                Text(
                    text = delay.ifBlank { "À définir" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (delay.isNotBlank()) colors.textPrimary else colors.cyanAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Définir délai d'expédition",
                tint = colors.cyanAccent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
