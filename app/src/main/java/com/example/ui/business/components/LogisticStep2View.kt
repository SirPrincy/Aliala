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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.LotCostCalculator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogisticStep2View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    families: List<CatalogueFamilySummaryUi> = emptyList(),
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onAdvanceToWeighing: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()

    // Transitaire réel correspondant à order.carrier
    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
    }

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

    val breakdown = remember(order, cmupRate, selectedTransitaire) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            transitaireAirRateAr = selectedTransitaire?.airRateAr,
            transitaireSeaRateAr = selectedTransitaire?.seaRateAr
        )
    }
    val totalQty = breakdown.quantity
    val unitPriceYuan = breakdown.unitPriceYuan
    val totalOrderYuan = (totalQty * unitPriceYuan) + order.domesticShippingYuan.coerceAtLeast(0.0)
    val totalOrderAr = breakdown.merchandiseTotalAr + breakdown.domesticPortAr
    val isMaritime = breakdown.isMaritime

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

    val deliveredParcelsCount = parcels.count { it.isDelivered }
    val allParcelsDelivered = parcels.isNotEmpty() && deliveredParcelsCount == parcels.size

    var showAddParcelDialog by remember { mutableStateOf(false) }
    var editingParcel by remember { mutableStateOf<DomesticParcelItem?>(null) }
    var parcelToDelete by remember { mutableStateOf<DomesticParcelItem?>(null) }
    var showSelectTransitaireDialog by remember { mutableStateOf(false) }
    var showEditEtaDialog by remember { mutableStateOf(false) }
    var showPartialProceedConfirm by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    fun persistParcels(updatedParcels: List<DomesticParcelItem>) {
        onUpdateOrder(DomesticParcelStorage.applyToOrder(order, updatedParcels))
    }

    val destinationLabel = when {
        selectedTransitaire != null && selectedTransitaire.routeHub.isNotBlank() ->
            "${selectedTransitaire.name} (${selectedTransitaire.routeHub})"
        selectedTransitaire != null -> selectedTransitaire.name
        order.carrier.isNotBlank() -> order.carrier
        else -> "l'entrepôt transitaire"
    }

    val statusBannerText = when {
        parcels.isEmpty() -> "En attente des numéros de suivi vers $destinationLabel"
        allParcelsDelivered -> "Tous les colis (${parcels.size}/${parcels.size}) réceptionnés chez $destinationLabel"
        deliveredParcelsCount > 0 -> "$deliveredParcelsCount/${parcels.size} colis reçus chez $destinationLabel"
        else -> "En livraison vers $destinationLabel"
    }

    @Composable
    fun LotSummarySection() {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header harmonisé avec Étape 1
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
                            text = "Récapitulatif du Lot",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        if (order.yuanDebited) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = colors.successSoftBg
                            ) {
                                Text(
                                    text = "Payé ¥",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = colors.successAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Product row harmonisé avec Étape 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = familyEmoji, fontSize = 30.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = order.product.ifBlank { "Article #${order.id}" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (order.referenceCode.isNotBlank()) "Réf : ${order.referenceCode}" else "$totalQty unités commandées",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = buildString {
                                    append(order.supplier.ifBlank { "Fournisseur Chine" })
                                    if (order.supplierDispatchDelay.isNotBlank()) {
                                        append(" · ")
                                        append(order.supplierDispatchDelay)
                                    }
                                },
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Variantes & Coût total (cohérent avec Étape 1)
                val parsedVariants = remember(order.variants) {
                    VariantColorUtils.parseVariants(order.variants)
                }
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
                            Column {
                                Text(
                                    text = "$totalQty unités · ¥ ${formatYuanCompact(unitPriceYuan)}/u",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                if (order.domesticShippingYuan > 0.0) {
                                    Text(
                                        text = "Port Chine inclus : ¥ ${formatYuanCompact(order.domesticShippingYuan)}",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ ${formatYuanCompact(totalOrderYuan)}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = colors.cyanAccent
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(totalOrderAr),
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
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
                                            Text(
                                                text = variantText,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Destination Transitaire & Arrivée prévue (cliquables)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSelectTransitaireDialog = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Destination transitaire",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    val destTitle = when {
                                        selectedTransitaire != null -> selectedTransitaire.name
                                        order.carrier.isNotBlank() -> order.carrier
                                        else -> "Non défini — toucher pour choisir"
                                    }
                                    Text(
                                        text = destTitle,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (selectedTransitaire != null || order.carrier.isNotBlank()) {
                                            colors.textPrimary
                                        } else {
                                            colors.warningAccent
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isMaritime) colors.violetSoftBg else colors.cyanSoftBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                            contentDescription = null,
                                            tint = if (isMaritime) colors.violetAccent else colors.cyanAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = if (isMaritime) "MARITIME" else "AÉRIEN",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = if (isMaritime) colors.violetAccent else colors.cyanAccent
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier le transitaire",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showEditEtaDialog = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colors.cyanSolidBg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Arrivée prévue : ${DateUtils.formatShortDate(order.estimatedArrival)} (${DateUtils.formatRelativeDays(order.estimatedArrival)})",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.cyanAccent,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la date d'arrivée",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChinaDropPointSection() {
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Point de chute Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.violetSoftBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .clickable { showSelectTransitaireDialog = true }
                    ) {
                        Text(
                            text = if (selectedTransitaire != null || order.carrier.isNotBlank()) "Changer" else "Choisir",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.violetAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedTransitaire != null) {
                            Text(
                                text = selectedTransitaire.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            if (selectedTransitaire.routeHub.isNotBlank()) {
                                Text(
                                    text = "Hub : ${selectedTransitaire.routeHub}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.cyanAccent
                                )
                            }

                            if (selectedTransitaire.chinaWarehouseAddress.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedTransitaire.chinaWarehouseAddress,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(
                                                label = "Adresse entrepôt Chine",
                                                text = selectedTransitaire.chinaWarehouseAddress,
                                                toastMsg = "Adresse entrepôt copiée !"
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copier l'adresse entrepôt",
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Adresse entrepôt Chine non renseignée dans la fiche transitaire.",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }

                            val contactText = listOfNotNull(
                                selectedTransitaire.contact.takeIf { it.isNotBlank() },
                                selectedTransitaire.wechat.takeIf { it.isNotBlank() }?.let { "WeChat: $it" },
                                selectedTransitaire.whatsapp.takeIf { it.isNotBlank() }?.let { "WhatsApp: $it" }
                            ).joinToString(" · ")

                            if (contactText.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = colors.violetAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = contactText,
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(
                                                label = "Contact transitaire",
                                                text = contactText,
                                                toastMsg = "Contact transitaire copié !"
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copier le contact",
                                            tint = colors.violetAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        } else if (order.carrier.isNotBlank()) {
                            Text(
                                text = "Transitaire : ${order.carrier}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Ce nom n'est pas encore relié à une fiche du catalogue Transitaires. Touchez « Changer » pour afficher l'adresse Chine.",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        } else {
                            Text(
                                text = "Aucun transitaire assigné à ce lot",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.warningAccent
                            )
                            Text(
                                text = "Sélectionnez votre transitaire pour afficher ici l'adresse de livraison Chine à copier au fournisseur.",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ParcelsTrackingSection(isCompactPhone: Boolean) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Colis en transit intérieur",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (parcels.size >= 2 && !allParcelsDelivered) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = colors.successSoftBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .clickable {
                                    val now = System.currentTimeMillis()
                                    val updated = parcels.map {
                                        it.copy(
                                            isDelivered = true,
                                            checkpointTitle = "Arrivé à l'entrepôt transitaire",
                                            updatedAtMillis = now
                                        )
                                    }
                                    persistParcels(updated)
                                    Toast.makeText(context, "Tous les colis marqués comme réceptionnés", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = "Tout recevoir",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.successAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.cyanSoftBg
                    ) {
                        Text(
                            text = "$deliveredParcelsCount/${parcels.size} reçu${if (deliveredParcelsCount > 1) "s" else ""}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (parcels.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.cyanSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Aucun numéro de suivi enregistré",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Ajoutez le ou les numéros de tracking fournis pour suivre la livraison intérieure jusqu'à l'entrepôt transitaire.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                parcels.forEachIndexed { index, parcel ->
                    val isPurpleAccent = index % 2 == 1
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = parcel.carrierName,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = parcel.trackingNumber,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        IconButton(
                                            onClick = {
                                                copyToClipboard(
                                                    label = "Numéro de suivi",
                                                    text = parcel.trackingNumber,
                                                    toastMsg = "Numéro ${parcel.trackingNumber} copié"
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copier le numéro",
                                                tint = colors.cyanAccent,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { parcelToDelete = parcel },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer le colis",
                                                tint = colors.dangerAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = when {
                                        parcel.isDelivered -> colors.successSoftBg
                                        isPurpleAccent -> colors.violetSoftBg
                                        else -> colors.cyanSoftBg
                                    }
                                ) {
                                    Text(
                                        text = if (parcel.isDelivered) "Réceptionné" else "En acheminement",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when {
                                            parcel.isDelivered -> colors.successAccent
                                            isPurpleAccent -> colors.violetAccent
                                            else -> colors.cyanAccent
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (parcel.isDelivered) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Hub,
                                                contentDescription = null,
                                                tint = colors.successAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = parcel.checkpointTitle,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = DateUtils.formatTransactionDate(parcel.updatedAtMillis),
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.cyanSolidBg)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(22.dp)
                                                        .background(colors.badgeNeutralBg)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.badgeNeutralBg)
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = parcel.checkpointTitle,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = DateUtils.formatTransactionDate(parcel.updatedAtMillis),
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                                if (parcel.checkpointRoute.isNotBlank()) {
                                                    Text(
                                                        text = parcel.checkpointRoute,
                                                        fontFamily = Inter,
                                                        fontSize = 11.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Contenu : ${parcel.contentDescription}",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            val toggleAction: () -> Unit = {
                                val now = System.currentTimeMillis()
                                val nextDelivered = !parcel.isDelivered
                                val updatedList = parcels.toMutableList()
                                updatedList[index] = parcel.copy(
                                    isDelivered = nextDelivered,
                                    checkpointTitle = if (nextDelivered) {
                                        "Arrivé à l'entrepôt transitaire"
                                    } else {
                                        "En cours d'acheminement"
                                    },
                                    updatedAtMillis = now
                                )
                                persistParcels(updatedList)
                                val statusMsg = if (nextDelivered) {
                                    "Colis ${parcel.trackingNumber} marqué réceptionné !"
                                } else {
                                    "Colis ${parcel.trackingNumber} remis en acheminement"
                                }
                                Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                            }

                            if (isCompactPhone) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ParcelActionPillButton(
                                        text = "Mettre à jour",
                                        icon = Icons.Default.Edit,
                                        bgColor = colors.badgeNeutralBg,
                                        contentColor = colors.textPrimary,
                                        onClick = { editingParcel = parcel },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    ParcelActionPillButton(
                                        text = if (parcel.isDelivered) "Reçu entrepôt" else "Réceptionné",
                                        icon = Icons.Default.DoneAll,
                                        bgColor = if (parcel.isDelivered) colors.successSoftBg else colors.cyanSoftBg,
                                        contentColor = if (parcel.isDelivered) colors.successAccent else colors.cyanAccent,
                                        onClick = toggleAction,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ParcelActionPillButton(
                                        text = "Mettre à jour",
                                        icon = Icons.Default.Edit,
                                        bgColor = colors.badgeNeutralBg,
                                        contentColor = colors.textPrimary,
                                        onClick = { editingParcel = parcel },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ParcelActionPillButton(
                                        text = if (parcel.isDelivered) "Reçu entrepôt" else "Réceptionné",
                                        icon = Icons.Default.DoneAll,
                                        bgColor = if (parcel.isDelivered) colors.successSoftBg else colors.cyanSoftBg,
                                        contentColor = if (parcel.isDelivered) colors.successAccent else colors.cyanAccent,
                                        onClick = toggleAction,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showAddParcelDialog = true }
                    .testTag("btn_add_parcel_step2"),
                shape = RoundedCornerShape(14.dp),
                color = colors.cardBg,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ajouter un colis / numéro de suivi",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }
    }

    @Composable
    fun Step2BottomActionsSection() {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.cyanSoftBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Marquez chaque colis comme réceptionné dès que le transporteur intérieur confirme la livraison à l'entrepôt Chine pour passer à la pesée (étape 3).",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

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
                    val hasDeliveredParcels = deliveredParcelsCount > 0

                    Button(
                        onClick = {
                            if (deliveredParcelsCount < parcels.size) {
                                showPartialProceedConfirm = true
                            } else {
                                val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                                    status = OrderStatus.GUANGZHOU.code
                                )
                                onAdvanceToWeighing(syncedOrder)
                                Toast.makeText(
                                    context,
                                    "$deliveredParcelsCount colis réceptionné(s) ! Passage à la pesée.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = hasDeliveredParcels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("btn_all_parcels_arrived"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cyanAccent,
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
                                    parcels.isEmpty() -> "Ajoutez et réceptionnez un colis"
                                    hasDeliveredParcels -> "Passer à la pesée ($deliveredParcelsCount/${parcels.size} reçus)"
                                    else -> "Réceptionnez au moins 1 colis"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (hasDeliveredParcels) Color.White else colors.textSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (hasDeliveredParcels) Color.White else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = onPreviousStep,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(min = 48.dp)
                            .testTag("btn_back_to_step1")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Retour à l'étape 1 (Commande)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
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
            WizardStepHeaderCard(
                stepNumber = 2,
                stepTitle = "Vers l'entrepôt transitaire",
                statusText = statusBannerText,
                statusIcon = Icons.Default.LocalShipping,
                statusAccentColor = if (allParcelsDelivered) colors.successAccent else colors.cyanAccent,
                statusContainerColor = if (allParcelsDelivered) colors.successSoftBg else colors.cyanSoftBg
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
                        LotSummarySection()
                        ChinaDropPointSection()
                        Step2BottomActionsSection()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ParcelsTrackingSection(isCompactPhone = false)
                    }
                }
            } else {
                LotSummarySection()
                ParcelsTrackingSection(isCompactPhone = isCompactPhone)
                ChinaDropPointSection()
                Step2BottomActionsSection()
            }
        }
    }

    // ==========================================
    // DIALOGUES ÉTAPE 2
    // ==========================================
    if (showAddParcelDialog) {
        var carrierInput by remember { mutableStateOf("SF Express") }
        var trackingInput by remember { mutableStateOf("") }
        var contentInput by remember {
            mutableStateOf(
                buildString {
                    if (totalQty > 0) append("$totalQty u")
                    if (order.variants.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(order.variants)
                    } else if (order.product.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(order.product)
                    }
                }
            )
        }
        var routeInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddParcelDialog = false },
            title = {
                Text("Ajouter un colis", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Transporteur intérieur :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DomesticParcelStorage.commonCarriers.take(6).forEach { preset ->
                            val selected = carrierInput.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { carrierInput = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("Nom du transporteur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = {
                            trackingInput = it
                            errorMessage = null
                        },
                        label = { Text("Numéro de tracking *") },
                        placeholder = { Text("Ex: SF160498210344") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Contenu du colis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = routeInput,
                        onValueChange = { routeInput = it },
                        label = { Text("Trajet / hub (optionnel)") },
                        placeholder = { Text("Ex: Shenzhen → Guangzhou") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let { msg ->
                        Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanedTracking = trackingInput.trim()
                        when {
                            cleanedTracking.isEmpty() -> errorMessage = "Veuillez saisir un numéro de suivi."
                            parcels.any { it.trackingNumber.equals(cleanedTracking, ignoreCase = true) } ->
                                errorMessage = "Ce numéro de suivi est déjà enregistré."
                            else -> {
                                val newParcel = DomesticParcelItem(
                                    trackingNumber = cleanedTracking,
                                    carrierName = carrierInput.trim().ifBlank {
                                        DomesticParcelStorage.inferCarrierFromTracking(cleanedTracking)
                                    },
                                    contentDescription = contentInput.trim().ifBlank { "Marchandise" },
                                    checkpointTitle = "Pris en charge par le transporteur",
                                    checkpointRoute = routeInput.trim(),
                                    updatedAtMillis = System.currentTimeMillis(),
                                    isDelivered = false
                                )
                                persistParcels(parcels + newParcel)
                                showAddParcelDialog = false
                                Toast.makeText(context, "Colis $cleanedTracking ajouté", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddParcelDialog = false }) { Text("Annuler") }
            }
        )
    }

    editingParcel?.let { parcel ->
        var carrierInput by remember(parcel) { mutableStateOf(parcel.carrierName) }
        var trackingInput by remember(parcel) { mutableStateOf(parcel.trackingNumber) }
        var contentInput by remember(parcel) { mutableStateOf(parcel.contentDescription) }
        var checkpointInput by remember(parcel) { mutableStateOf(parcel.checkpointTitle) }
        var routeInput by remember(parcel) { mutableStateOf(parcel.checkpointRoute) }
        var isDeliveredState by remember(parcel) { mutableStateOf(parcel.isDelivered) }
        var errorMessage by remember(parcel) { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { editingParcel = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.cyanAccent)
                    Text("Suivi & Mise à jour", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = {
                            trackingInput = it
                            errorMessage = null
                        },
                        label = { Text("Numéro de suivi *") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("Transporteur intérieur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Étape de livraison :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DomesticParcelStorage.checkpointPresets.forEach { stage ->
                            val active = checkpointInput.equals(stage, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        checkpointInput = stage
                                        if (stage.contains("entrepôt transitaire", ignoreCase = true)) {
                                            isDeliveredState = true
                                        }
                                    }
                            ) {
                                Text(
                                    text = stage,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = if (active) Color.White else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = checkpointInput,
                        onValueChange = { checkpointInput = it },
                        label = { Text("Statut personnalisé") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = routeInput,
                        onValueChange = { routeInput = it },
                        label = { Text("Itinéraire / étape") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Contenu associé") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDeliveredState) colors.successSoftBg else colors.innerCardBg)
                            .clickable { isDeliveredState = !isDeliveredState }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDeliveredState) "Colis réceptionné à l'entrepôt" else "Marquer comme réceptionné",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isDeliveredState) colors.successAccent else colors.textPrimary
                        )
                        if (isDeliveredState) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = colors.successAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    errorMessage?.let { msg ->
                        Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanedTracking = trackingInput.trim()
                        if (cleanedTracking.isEmpty()) {
                            errorMessage = "Le numéro de suivi est obligatoire."
                        } else if (
                            !cleanedTracking.equals(parcel.trackingNumber, ignoreCase = true) &&
                            parcels.any { it.trackingNumber.equals(cleanedTracking, ignoreCase = true) }
                        ) {
                            errorMessage = "Un autre colis utilise déjà ce numéro."
                        } else {
                            val updatedParcels = parcels.map { current ->
                                if (current.trackingNumber == parcel.trackingNumber) {
                                    current.copy(
                                        trackingNumber = cleanedTracking,
                                        carrierName = carrierInput.trim().ifBlank {
                                            DomesticParcelStorage.inferCarrierFromTracking(cleanedTracking)
                                        },
                                        contentDescription = contentInput.trim().ifBlank { "Marchandise" },
                                        checkpointTitle = checkpointInput.trim().ifBlank {
                                            if (isDeliveredState) "Arrivé à l'entrepôt transitaire" else "En cours d'acheminement"
                                        },
                                        checkpointRoute = routeInput.trim(),
                                        updatedAtMillis = System.currentTimeMillis(),
                                        isDelivered = isDeliveredState
                                    )
                                } else current
                            }
                            persistParcels(updatedParcels)
                            editingParcel = null
                            Toast.makeText(context, "Suivi mis à jour", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingParcel = null }) { Text("Annuler") }
            }
        )
    }

    parcelToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { parcelToDelete = null },
            title = {
                Text("Supprimer ce colis ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    text = "Le numéro de suivi ${target.trackingNumber} sera retiré de ce lot.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        persistParcels(parcels.filterNot { it.trackingNumber == target.trackingNumber })
                        parcelToDelete = null
                        Toast.makeText(context, "Colis supprimé", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent)
                ) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { parcelToDelete = null }) { Text("Annuler") }
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

    if (showEditEtaDialog) {
        val currentRemainingDays = (((order.estimatedArrival - System.currentTimeMillis()) / 86_400_000L).toInt()).coerceAtLeast(1)
        var daysInput by remember { mutableStateOf(currentRemainingDays.toString()) }
        var etaError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditEtaDialog = false },
            title = {
                Text("Ajuster l'arrivée prévue", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Actuelle : ${DateUtils.formatShortDate(order.estimatedArrival)}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 5, 7, 15, 30, 45).forEach { presetDays ->
                            val active = daysInput == presetDays.toString()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) colors.cyanAccent else colors.badgeNeutralBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        daysInput = presetDays.toString()
                                        etaError = null
                                    }
                            ) {
                                Text(
                                    text = "+${presetDays}j",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (active) Color.White else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = {
                            daysInput = it.filter { ch -> ch.isDigit() }
                            etaError = null
                        },
                        label = { Text("Nombre de jours à compter d'aujourd'hui") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = etaError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val previewDays = daysInput.toIntOrNull()
                    if (previewDays != null && previewDays > 0) {
                        val previewMillis = System.currentTimeMillis() + previewDays * 86_400_000L
                        Text(
                            text = "Nouvelle date : ${DateUtils.formatShortDate(previewMillis)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                    etaError?.let { msg ->
                        Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = daysInput.toIntOrNull()
                        if (days == null || days <= 0) {
                            etaError = "Entrez un nombre de jours supérieur à 0."
                        } else {
                            val newEta = System.currentTimeMillis() + days * 86_400_000L
                            onUpdateOrder(order.copy(estimatedArrival = newEta))
                            showEditEtaDialog = false
                            Toast.makeText(context, "Date d'arrivée mise à jour", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Valider", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEtaDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showPartialProceedConfirm) {
        AlertDialog(
            onDismissRequest = { showPartialProceedConfirm = false },
            title = {
                Text("Réception partielle", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    text = "$deliveredParcelsCount colis sur ${parcels.size} sont marqués comme réceptionnés à l'entrepôt. Voulez-vous tout de même passer à l'étape 3 (Pesée) ?",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPartialProceedConfirm = false
                        val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                            status = OrderStatus.GUANGZHOU.code
                        )
                        onAdvanceToWeighing(syncedOrder)
                        Toast.makeText(
                            context,
                            "$deliveredParcelsCount colis réceptionné(s) ! Passage à la pesée.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
                ) {
                    Text("Passer à la pesée", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPartialProceedConfirm = false }) {
                    Text("Attendre les autres")
                }
            }
        )
    }
}

@Composable
private fun ParcelActionPillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
