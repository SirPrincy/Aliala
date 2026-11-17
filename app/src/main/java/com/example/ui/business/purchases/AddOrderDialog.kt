package com.example.ui.business.purchases

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils
import java.util.Locale

data class VariantItemState(
    val id: Int,
    val name: String,
    val colorHex: Long,
    val quantity: Int
)

private fun parseInitialVariants(variantsStr: String?): List<VariantItemState> {
    if (variantsStr.isNullOrBlank()) {
        return listOf(
            VariantItemState(1, "Noir", 0xFF0F172A, 10),
            VariantItemState(2, "Blanc", 0xFFFFFFFF, 5)
        )
    }
    val parsed = VariantColorUtils.parseVariants(variantsStr).mapIndexed { idx, raw ->
        val timesMatch = Regex("""^(.*?)\s*[×xX]\s*(\d+)$""").find(raw)
        val okMatch = Regex("""^(.*?)\s*\((\d+)\s*ok\)$""", RegexOption.IGNORE_CASE).find(raw)
        val (cleanName, qty) = when {
            timesMatch != null -> timesMatch.groupValues[1].trim() to (timesMatch.groupValues[2].toIntOrNull()?.coerceAtLeast(1) ?: 10)
            okMatch != null -> okMatch.groupValues[1].trim() to (okMatch.groupValues[2].toIntOrNull()?.coerceAtLeast(1) ?: 10)
            else -> raw.trim() to 10
        }
        val safeName = cleanName.ifBlank { "Variante ${idx + 1}" }
        val cHex = VariantColorUtils.getVariantColor(safeName, idx).value.toLong()
        VariantItemState(idx + 1, safeName, cHex, qty)
    }
    return parsed.ifEmpty {
        listOf(VariantItemState(1, "Standard", 0xFF0F172A, 10))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderDialog(
    cmupRate: Long,
    availableYuans: Double = 0.0,
    availableArticles: List<BusinessOrderEntity> = emptyList(),
    initialArticle: BusinessOrderEntity? = null,
    editingOrder: BusinessOrderEntity? = null,
    transitaires: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resaleAr: Long,
        category: String,
        carrier: String,
        variants: String,
        freightAr: Long,
        platform: String,
        referenceCode: String,
        family: String,
        domesticShippingYuan: Double,
        orderDateMillis: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = editingOrder != null
    val targetItem = editingOrder ?: initialArticle
    var selectedArticleId by remember(targetItem?.id) { mutableStateOf<Long?>(targetItem?.id) }
    var product by remember(targetItem?.id) { mutableStateOf(targetItem?.product ?: "") }
    var supplierRef by remember(targetItem?.id) { mutableStateOf(targetItem?.referenceCode ?: "") }
    var familyCategory by remember(targetItem?.id) { mutableStateOf(targetItem?.family?.ifBlank { "Électronique" } ?: "Électronique") }
    var selectedSupplier by remember(targetItem?.id) { mutableStateOf(targetItem?.platform?.ifBlank { "1688.com" } ?: "1688.com") }
    var priceYuanText by remember(targetItem?.id) {
        mutableStateOf(
            if ((targetItem?.priceYuan ?: 0.0) > 0) String.format(Locale.US, "%.2f", targetItem!!.priceYuan).removeSuffix(".00")
            else ""
        )
    }
    var isShippingPaid by remember(targetItem?.id) { mutableStateOf((targetItem?.domesticShippingYuan ?: 0.0) > 0.0) }
    var shippingCostYuanText by remember(targetItem?.id) {
        mutableStateOf(
            if ((targetItem?.domesticShippingYuan ?: 0.0) > 0.0) {
                String.format(Locale.US, "%.2f", targetItem!!.domesticShippingYuan).removeSuffix(".00")
            } else ""
        )
    }
    var category by remember(targetItem?.id) { mutableStateOf(targetItem?.category?.ifBlank { "BUSINESS" } ?: "BUSINESS") }
    var carrier by remember(targetItem?.id) { mutableStateOf(if (targetItem?.carrier?.isNotBlank() == true) targetItem.carrier else (transitaires.firstOrNull() ?: "")) }
    var resaleArText by remember(targetItem?.id) { mutableStateOf(if ((targetItem?.resalePriceAr ?: 0L) > 0) targetItem!!.resalePriceAr.toString() else "") }
    var orderDateText by remember(editingOrder?.id) {
        mutableStateOf(
            if (editingOrder != null) DateUtils.formatShortDate(editingOrder.orderDate)
            else DateUtils.getTodayFrenchDate()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    var variantsList by remember(targetItem?.id) {
        mutableStateOf(parseInitialVariants(targetItem?.variants))
    }

    val totalUnits = variantsList.sumOf { it.quantity }.coerceAtLeast(1)
    val priceYuan = priceYuanText.toDoubleOrNull() ?: 0.0
    val effectiveCmupRate = if (cmupRate > 0) cmupRate else 650L
    val unitPriceAr = (priceYuan * effectiveCmupRate).toLong()
    val goodsTotalYuan = totalUnits * priceYuan
    val shippingCostYuan = if (isShippingPaid) (shippingCostYuanText.toDoubleOrNull() ?: 0.0) else 0.0
    val orderTotalYuan = goodsTotalYuan + shippingCostYuan
    val orderTotalAr = (orderTotalYuan * effectiveCmupRate).toLong()
    val effectiveAvailableYuans = availableYuans
    val isSoldeSuffisant = effectiveAvailableYuans >= orderTotalYuan

    val scrollState = rememberScrollState()

    val saveAction = {
        if (product.isBlank()) {
            Toast.makeText(context, "Veuillez saisir le nom de l'article", Toast.LENGTH_SHORT).show()
        } else if (priceYuan <= 0.0) {
            Toast.makeText(context, "Veuillez indiquer le prix unitaire en Yuans (¥)", Toast.LENGTH_SHORT).show()
        } else if (!DateUtils.isValidFrenchDate(orderDateText)) {
            Toast.makeText(context, "Date d'achat invalide (format attendu : jj/mm/aaaa)", Toast.LENGTH_SHORT).show()
        } else {
            val variantsStr = variantsList.filter { it.name.isNotBlank() }.joinToString(", ") { "${it.name} ×${it.quantity}" }
            val resaleAr = resaleArText.toLongOrNull() ?: (priceYuan * effectiveCmupRate * 1.5).toLong()
            val cleanPlat = if (selectedSupplier.equals("1688.com", ignoreCase = true)) "1688" else selectedSupplier.trim()
            val finalOrderDateMillis = if (
                editingOrder != null &&
                orderDateText.trim() == DateUtils.formatShortDate(editingOrder.orderDate)
            ) {
                editingOrder.orderDate
            } else {
                DateUtils.frenchDateToEpochMillis(orderDateText)
            }
            onConfirm(
                product.trim(),
                cleanPlat,
                totalUnits,
                priceYuan,
                resaleAr,
                category,
                carrier.trim(),
                variantsStr.ifBlank { "Standard ×$totalUnits" },
                0L,
                cleanPlat,
                supplierRef.trim(),
                familyCategory.trim().ifBlank { "Électronique" },
                shippingCostYuan,
                finalOrderDateMillis
            )
            onDismiss()
        }
    }

    if (showDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(orderDateText)
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            orderDateText = DateUtils.utcPickerMillisToFrenchDate(picked)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 76.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onDismiss() }
                                        .testTag("btn_close_new_order"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (isEditMode) "Modifier l'achat" else "Nouvel Achat",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = saveAction,
                                    modifier = Modifier.testTag("top_save_action")
                                ) {
                                    Text(
                                        text = "Enregistrer",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profil",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OrderDialogStepperHeader(isEditMode = isEditMode)

                        OrderIdentificationSectionCard(
                            availableArticles = if (isEditMode) emptyList() else availableArticles,
                            product = product,
                            onProductChange = { product = it },
                            supplierRef = supplierRef,
                            onSupplierRefChange = { supplierRef = it },
                            familyCategory = familyCategory,
                            onFamilyCategoryChange = { familyCategory = it },
                            orderDateText = orderDateText,
                            onOrderDateChange = { orderDateText = it },
                            onOpenDatePicker = { showDatePicker = true },
                            onArticleSelected = { art ->
                                selectedArticleId = art.id
                                product = art.product
                                if (art.referenceCode.isNotBlank()) supplierRef = art.referenceCode
                                if (art.family.isNotBlank()) familyCategory = art.family
                                if (art.priceYuan > 0) priceYuanText = String.format(Locale.US, "%.2f", art.priceYuan).removeSuffix(".00")
                                if (art.resalePriceAr > 0) resaleArText = art.resalePriceAr.toString()
                                if (art.platform.isNotBlank()) selectedSupplier = art.platform
                                if (art.carrier.isNotBlank()) carrier = art.carrier
                                if (art.variants.isNotBlank()) variantsList = parseInitialVariants(art.variants)
                            },
                            onClearArticle = {
                                product = ""
                                supplierRef = ""
                                priceYuanText = ""
                                resaleArText = ""
                            }
                        )

                        OrderSupplierCarrierSectionCard(
                            selectedSupplier = selectedSupplier,
                            onSupplierSelected = { selectedSupplier = it },
                            carrier = carrier,
                            transitaires = transitaires,
                            onCarrierSelected = { carrier = it }
                        )

                        OrderUnitPriceSectionCard(
                            priceYuanText = priceYuanText,
                            onPriceYuanChange = { priceYuanText = it },
                            unitPriceAr = unitPriceAr,
                            cmupRate = cmupRate,
                            effectiveCmupRate = effectiveCmupRate
                        )

                        OrderVariantsSectionCard(
                            variantsList = variantsList,
                            totalUnits = totalUnits,
                            onVariantsChange = { variantsList = it }
                        )

                        OrderDomesticShippingSectionCard(
                            isShippingPaid = isShippingPaid,
                            onShippingPaidChange = { isShippingPaid = it },
                            shippingCostYuanText = shippingCostYuanText,
                            onShippingCostChange = { shippingCostYuanText = it }
                        )

                        OrderFinancialSummarySectionCard(
                            totalUnits = totalUnits,
                            priceYuan = priceYuan,
                            goodsTotalYuan = goodsTotalYuan,
                            shippingCostYuan = shippingCostYuan,
                            orderTotalYuan = orderTotalYuan,
                            orderTotalAr = orderTotalAr,
                            effectiveAvailableYuans = effectiveAvailableYuans,
                            isSoldeSuffisant = isSoldeSuffisant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable { onDismiss() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Précédent",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp)
                                    .clickable { saveAction() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEditMode) "Enregistrer les modifications" else "Continuer vers Livraison",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Button(
                            onClick = saveAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_save_purchase_final")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isEditMode) "Mettre à jour l'achat" else "Enregistrer l'achat",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
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
private fun OrderDialogStepperHeader(isEditMode: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "MODIFICATION" else "MODULE BUSINESS",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = if (isEditMode) "Édition de l'achat" else "Étape 2 sur 4",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = if (isEditMode) "Détails de la commande" else "Quantités & Prix",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF06B6D4))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Article",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Quantités",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Text(
                    text = "Livraison",
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Text(
                    text = "Paiement",
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}
