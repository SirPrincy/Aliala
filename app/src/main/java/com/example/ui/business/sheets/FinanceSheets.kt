package com.example.ui.business.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessCategoryEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanDatePickerField
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.shared.resolveCategoryColors
import com.example.ui.business.shared.resolveCategoryIcon
import com.example.ui.business.shared.resolveTransactionCategory
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun BuyYuansSheet(
    accounts: List<BusinessAccountEntity>,
    defaultCmupRate: Long,
    onDismiss: () -> Unit,
    onQuickCreateYuanAccount: (name: String) -> Unit,
    onBuyYuans: (amountYuan: Double, rateAr: Double, platform: String, sourceAccount: String, dateMillis: Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    val yuanAccounts = remember(accounts) { accounts.filter { it.isYuanAccount && it.isActive } }
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    var amountYuanText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf(if (defaultCmupRate > 0L) defaultCmupRate.toString() else "650") }
    var selectedPlatform by remember(yuanAccounts) { mutableStateOf(yuanAccounts.firstOrNull()?.name ?: "Alipay") }
    var selectedSource by remember(ariaryAccounts) { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "Externe") }
    var dateText by remember { mutableStateOf(DateUtils.getTodayFrenchDate()) }
    var quickAccountName by remember { mutableStateOf("Alipay") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val amountYuan = parseDoubleInput(amountYuanText) ?: 0.0
    val rateAr = parseDoubleInput(rateText) ?: 0.0
    val totalAr = (amountYuan * rateAr).toLong()

    HumanSheetContainer(
        title = "Acheter des Yuans (¥)",
        subtitle = "Rechargez votre portefeuille Alipay ou WeChat Pay",
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (yuanAccounts.isEmpty()) {
            // Bloc inline : jamais de modale empilée
            Surface(shape = RoundedCornerShape(16.dp), color = colors.warningSoftBg, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Aucun compte Yuan actif. Créez-en un ici :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.warningAccent
                    )
                    HumanSegmentedSwitch(
                        options = listOf("Alipay" to "Alipay", "Wechat Pay" to "WeChat Pay"),
                        selectedKey = quickAccountName,
                        onSelect = { quickAccountName = it },
                        colors = colors
                    )
                    HumanTonalButton(
                        text = "Créer $quickAccountName",
                        icon = Icons.Default.Add,
                        onClick = {
                            onQuickCreateYuanAccount(quickAccountName)
                            selectedPlatform = quickAccountName
                        },
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Text("Compte Yuan crédité", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = yuanAccounts.map { it.name to "${it.name} (${formatYuan(it.balanceYuan)})" },
                selectedKey = selectedPlatform,
                onSelect = { selectedPlatform = it },
                colors = colors
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = amountYuanText,
                onValueChange = { amountYuanText = it; errorText = null },
                label = "Montant (¥) *",
                keyboardType = KeyboardType.Decimal,
                suffixText = "¥",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = rateText,
                onValueChange = { rateText = it; errorText = null },
                label = "Taux (Ar/¥) *",
                keyboardType = KeyboardType.Decimal,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Compte source Ariary", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = ariaryAccounts.map {
                it.name to "${it.name} (${CurrencyFormatter.formatCompactAriary(it.balanceAr)})"
            } + ("Externe" to "Externe (hors caisse)"),
            selectedKey = selectedSource,
            onSelect = { selectedSource = it },
            colors = colors
        )
        Text(
            text = if (selectedSource == "Externe") {
                "Crédite ${selectedPlatform} sans toucher à vos caisses Ariary."
            } else {
                "Déduira ${if (totalAr > 0L) CurrencyFormatter.formatAriary(totalAr) else "le montant Ar"} de $selectedSource."
            },
            fontFamily = Inter,
            fontSize = 11.sp,
            color = colors.textSecondary
        )

        HumanTextField(
            value = dateText,
            onValueChange = { dateText = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = "Date (jj/mm/aaaa)",
            colors = colors
        )

        if (totalAr > 0L) {
            Surface(shape = RoundedCornerShape(16.dp), color = colors.cyanSoftBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Coût en Ariary", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                    Text(
                        text = CurrencyFormatter.formatAriary(totalAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Valider l'achat de Yuans",
            icon = Icons.Default.CurrencyYen,
            enabled = yuanAccounts.isNotEmpty(),
            onClick = {
                if (amountYuan <= 0.0) {
                    errorText = "Veuillez saisir un montant en Yuans."
                    return@HumanPrimaryButton
                }
                if (rateAr <= 0.0) {
                    errorText = "Veuillez saisir un taux de change valide."
                    return@HumanPrimaryButton
                }
                val parsedDate = if (DateUtils.isValidFrenchDate(dateText)) DateUtils.frenchDateToEpochMillis(dateText) else System.currentTimeMillis()
                onBuyYuans(amountYuan, rateAr, selectedPlatform, selectedSource, parsedDate)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_confirm_buy_yuans")
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddFinanceOperationSheet(
    accounts: List<BusinessAccountEntity>,
    categories: List<BusinessCategoryEntity>,
    presetTitle: String,
    presetType: String,
    presetTag: String,
    onDismiss: () -> Unit,
    onManageCategories: (currentType: String) -> Unit,
    onConfirm: (title: String, type: String, amountAr: Long, accountName: String, tag: String, destAccount: String, categoryId: Long?) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    var type by remember { mutableStateOf(presetType.ifBlank { "IN" }) }
    var title by remember { mutableStateOf(presetTitle) }
    var amountText by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "Espèces") }
    var destAccount by remember { mutableStateOf(ariaryAccounts.getOrNull(1)?.name ?: ariaryAccounts.firstOrNull()?.name ?: "") }
    var tag by remember { mutableStateOf(presetTag) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val categoriesForType = remember(categories, type) {
        categories
            .filter { it.type == type }
            .sortedWith(compareBy<BusinessCategoryEntity> { it.displayOrder }.thenBy { it.id })
    }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(type, categoriesForType) {
        if (type == "IN" || type == "OUT") {
            val stillValid = selectedCategoryId != null && categoriesForType.any { it.id == selectedCategoryId }
            if (!stillValid) {
                val presetMatched = if (presetTag.isNotBlank()) {
                    categoriesForType.firstOrNull { it.name.equals(presetTag.trim(), ignoreCase = true) }
                } else null
                selectedCategoryId = (presetMatched ?: categoriesForType.firstOrNull())?.id
            }
        } else {
            selectedCategoryId = null
        }
    }

    HumanSheetContainer(
        title = "Nouvelle opération",
        subtitle = "Entrée, sortie ou virement entre caisses",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanSegmentedSwitch(
            options = listOf("IN" to "Entrée", "OUT" to "Sortie", "TRANSFER" to "Virement"),
            selectedKey = type,
            onSelect = { type = it },
            colors = colors
        )

        if (type == "IN" || type == "OUT") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catégorie",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Gérer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.cyanAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onManageCategories(type) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_manage_categories_inline")
                )
            }

            if (categoriesForType.isEmpty()) {
                Text(
                    text = "Aucune catégorie — cliquez sur Gérer pour en créer.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesForType.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val (catAccent, catSoftBg) = resolveCategoryColors(cat.colorKey, colors)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) catSoftBg else colors.innerCardBg,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) catAccent else colors.border
                            ),
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .clip(CircleShape)
                                .clickable { selectedCategoryId = cat.id }
                                .testTag("op_cat_chip_${cat.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = resolveCategoryIcon(cat.iconKey),
                                    contentDescription = null,
                                    tint = if (isSelected) catAccent else colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = cat.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) catAccent else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        HumanTextField(
            value = title,
            onValueChange = { title = it; errorText = null },
            label = "Libellé *",
            placeholder = "Ex : Frais livraison, Versement...",
            colors = colors
        )

        HumanTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
            label = "Montant *",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )

        if (ariaryAccounts.isNotEmpty()) {
            Text(
                text = if (type == "TRANSFER") "Compte source" else "Compte concerné",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name },
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }

        if (type == "TRANSFER" && ariaryAccounts.size >= 2) {
            Text("Compte destinataire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name },
                selectedKey = destAccount,
                onSelect = { destAccount = it },
                colors = colors
            )
        }

        HumanTextField(
            value = tag,
            onValueChange = { tag = it },
            label = if (type == "TRANSFER") "Note (optionnel)" else "Note complémentaire (optionnel)",
            colors = colors
        )

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Enregistrer l'opération",
            icon = Icons.Default.Check,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (title.isBlank()) {
                    errorText = "Veuillez saisir un libellé."
                    return@HumanPrimaryButton
                }
                if (amt <= 0L) {
                    errorText = "Veuillez saisir un montant valide."
                    return@HumanPrimaryButton
                }
                if (type == "TRANSFER" && selectedAccount.equals(destAccount, ignoreCase = true)) {
                    errorText = "Choisissez un compte destinataire différent."
                    return@HumanPrimaryButton
                }
                onConfirm(
                    title.trim(),
                    type,
                    amt,
                    selectedAccount,
                    tag.trim(),
                    if (type == "TRANSFER") destAccount else "",
                    if (type == "IN" || type == "OUT") selectedCategoryId else null
                )
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_operation")
        )
    }
}

@Composable
internal fun AccountFormSheet(
    existing: BusinessAccountEntity?,
    defaultIsYuan: Boolean,
    onDismiss: () -> Unit,
    onAdd: (name: String, subtitle: String, balanceAr: Long, isYuan: Boolean, balanceYuan: Double) -> Unit,
    onUpdate: (existing: BusinessAccountEntity, name: String, subtitle: String, balanceAr: Long, balanceYuan: Double) -> Unit,
    onDelete: (BusinessAccountEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var subtitle by remember { mutableStateOf(existing?.subtitle ?: "") }
    var isYuan by remember { mutableStateOf(existing?.isYuanAccount ?: defaultIsYuan) }
    var balanceText by remember {
        mutableStateOf(
            when {
                existing == null -> "0"
                existing.isYuanAccount -> existing.balanceYuan.toString()
                else -> existing.balanceAr.toString()
            }
        )
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = if (existing != null) "Modifier le compte" else "Nouveau compte",
        subtitle = if (existing != null) existing.name else "Caisse Ariary ou portefeuille Yuan",
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (existing == null) {
            HumanSegmentedSwitch(
                options = listOf("AR" to "Caisse Ariary", "YUAN" to "Portefeuille ¥"),
                selectedKey = if (isYuan) "YUAN" else "AR",
                onSelect = { isYuan = (it == "YUAN") },
                colors = colors
            )
        }

        HumanTextField(
            value = name,
            onValueChange = { name = it; errorText = null },
            label = "Nom du compte *",
            placeholder = if (isYuan) "Ex : Alipay" else "Ex : MVola",
            colors = colors
        )

        HumanTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = "Description",
            placeholder = if (isYuan) "Chine (¥)" else "Mobile Money / Espèces",
            colors = colors
        )

        HumanTextField(
            value = balanceText,
            onValueChange = { balanceText = it },
            label = if (isYuan) "Solde (¥)" else "Solde (Ar)",
            keyboardType = if (isYuan) KeyboardType.Decimal else KeyboardType.Number,
            suffixText = if (isYuan) "¥" else "Ar",
            colors = colors
        )

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = if (existing != null) "Enregistrer" else "Créer le compte",
            icon = Icons.Default.Check,
            onClick = {
                if (name.isBlank()) {
                    errorText = "Veuillez saisir un nom de compte."
                    return@HumanPrimaryButton
                }
                val balAr = if (!isYuan) parseLongInput(balanceText) ?: 0L else 0L
                val balYuan = if (isYuan) parseDoubleInput(balanceText) ?: 0.0 else 0.0
                if (existing != null) {
                    onUpdate(existing, name.trim(), subtitle.trim(), balAr, balYuan)
                } else {
                    onAdd(name.trim(), subtitle.trim(), balAr, isYuan, balYuan)
                }
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (existing != null) {
            HumanTonalButton(
                text = "Supprimer ce compte",
                icon = Icons.Default.DeleteOutline,
                onClick = { showDeleteConfirm = true },
                colors = colors,
                containerColor = colors.dangerSoftBg,
                contentColor = colors.dangerAccent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDeleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ce compte ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
            text = { Text("Le compte « ${existing.name} » sera supprimé.", fontFamily = Inter) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(existing)
                        onDismiss()
                    }
                ) {
                    Text("Supprimer", color = colors.dangerAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
internal fun PayFreightOrderSheet(
    order: BusinessOrderEntity,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (order: BusinessOrderEntity, amountAr: Long, accountName: String, paymentDateMillis: Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }
    var amountText by remember(order.id) { mutableStateOf(order.freightCostAr.toString()) }
    var selectedAccount by remember { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "") }
    var paymentDateMillis by remember(order.id) { mutableStateOf(System.currentTimeMillis()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = "Régler le fret",
        subtitle = "${order.product} · ${order.carrier.ifBlank { "Transitaire" }}",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
            label = "Montant du fret (Ar) *",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )

        HumanDatePickerField(
            dateMillis = paymentDateMillis,
            onDateSelected = { paymentDateMillis = it },
            label = "Date de règlement (jj/mm/aaaa)",
            colors = colors
        )

        if (ariaryAccounts.isNotEmpty()) {
            Text("Compte débité", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name },
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Payer le fret",
            icon = Icons.Default.Payments,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (amt <= 0L) {
                    errorText = "Montant invalide."
                    return@HumanPrimaryButton
                }
                onConfirm(order, amt, selectedAccount, paymentDateMillis)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_confirm_pay_freight_order")
        )
    }
}

@Composable
internal fun TransactionDetailSheet(
    tx: BusinessTransactionEntity,
    categories: List<BusinessCategoryEntity> = emptyList(),
    onDismiss: () -> Unit,
    onDelete: (BusinessTransactionEntity) -> Unit,
    onEdit: ((BusinessTransactionEntity) -> Unit)? = null
) {
    val colors = rememberTransitaireColors()
    val matchedCategory = remember(tx, categories) { resolveTransactionCategory(tx, categories) }
    HumanSheetContainer(
        title = tx.title,
        subtitle = DateUtils.formatShortDate(tx.dateMillis),
        onDismiss = onDismiss,
        colors = colors
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = colors.innerCardBg, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Montant", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(
                        CurrencyFormatter.formatAriary(tx.amountAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.cyanAccent
                    )
                }
                HumanHairline(colors)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Compte", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(tx.accountName, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                }
                if (matchedCategory != null) {
                    val (catAccent, catSoftBg) = resolveCategoryColors(matchedCategory.colorKey, colors)
                    HumanHairline(colors)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Catégorie", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                        Surface(shape = CircleShape, color = catSoftBg) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = resolveCategoryIcon(matchedCategory.iconKey),
                                    contentDescription = null,
                                    tint = catAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = matchedCategory.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = catAccent
                                )
                            }
                        }
                    }
                }
                if (tx.destinationAccount.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vers", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                        Text(tx.destinationAccount, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                    }
                }
                if (tx.referenceTag.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Référence", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                        Text(tx.referenceTag, fontFamily = Inter, fontSize = 12.sp, color = colors.textPrimary)
                    }
                }
            }
        }

        if (onEdit != null) {
            HumanTonalButton(
                text = "Modifier cette opération",
                icon = Icons.Default.Edit,
                onClick = { onEdit(tx) },
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_edit_transaction")
            )
        }

        HumanTonalButton(
            text = "Supprimer cette opération",
            icon = Icons.Default.DeleteOutline,
            onClick = {
                onDelete(tx)
                onDismiss()
            },
            colors = colors,
            containerColor = colors.dangerSoftBg,
            contentColor = colors.dangerAccent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun YuanTransactionDetailSheet(
    ytx: YuanTransactionEntity,
    onDismiss: () -> Unit,
    onDelete: (YuanTransactionEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val typeLabel = when (ytx.type) {
        "SPEND" -> "Paiement fournisseur"
        "REFUND" -> "Remboursement usine"
        else -> "Recharge Yuans"
    }
    HumanSheetContainer(
        title = ytx.note.ifBlank { "$typeLabel · ${ytx.platform}" },
        subtitle = DateUtils.formatShortDate(ytx.date),
        onDismiss = onDismiss,
        colors = colors
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = colors.innerCardBg, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Montant (¥)", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(
                        text = (if (ytx.type == "SPEND") "-" else "+") + formatYuan(ytx.amountYuan),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (ytx.type == "SPEND") colors.violetAccent else colors.successAccent
                    )
                }
                if (ytx.amountAr > 0L) {
                    HumanHairline(colors)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Équivalent Ariary", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                        Text(
                            CurrencyFormatter.formatAriary(ytx.amountAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                HumanHairline(colors)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Portefeuille", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(ytx.platform, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                }
                HumanHairline(colors)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Type", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(typeLabel, fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                }
            }
        }

        HumanTonalButton(
            text = "Annuler / supprimer cette opération",
            icon = Icons.Default.DeleteOutline,
            onClick = {
                onDelete(ytx)
                onDismiss()
            },
            colors = colors,
            containerColor = colors.dangerSoftBg,
            contentColor = colors.dangerAccent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

