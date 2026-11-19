package com.example.ui.business.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.business.BusinessNotificationUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.detail.SyncedTransitaireGridUpdate
import com.example.ui.business.detail.syncTransitaireGridValues
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanDatePickerField
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale
import java.util.UUID

private val FAMILY_EMOJIS = listOf("🎧", "👕", "🏠", "✨", "📱", "⌚", "👟", "👜", "🚗", "🍳", "🧸", "📦")

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddFamilySheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📦") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = "Nouvelle famille",
        subtitle = "Regroupez vos produits par univers",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(value = name, onValueChange = { name = it; errorText = null }, label = "Nom de la famille *", colors = colors)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FAMILY_EMOJIS.forEach { e ->
                val selected = e == emoji
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) colors.cyanSoftBg else colors.innerCardBg)
                        .clickable { emoji = e },
                    contentAlignment = Alignment.Center
                ) {
                    Text(e, fontSize = 22.sp)
                }
            }
        }
        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        HumanPrimaryButton(
            text = "Créer la famille",
            icon = Icons.Default.Check,
            onClick = {
                if (name.isBlank()) {
                    errorText = "Veuillez saisir un nom."
                    return@HumanPrimaryButton
                }
                onConfirm(name.trim(), emoji)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun AddPlatformSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, subtitle: String, shippingTag: String, secondaryTag: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var shippingTag by remember { mutableStateOf("Port Payant (¥ 10 - ¥ 25)") }
    var secondaryTag by remember { mutableStateOf("Min. commande : 2-5 pcs") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = "Nouvelle plateforme",
        subtitle = "Site fournisseur ou grossiste en Chine",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(value = name, onValueChange = { name = it; errorText = null }, label = "Nom *", placeholder = "Ex : 1688, Alibaba...", colors = colors)
        HumanTextField(value = subtitle, onValueChange = { subtitle = it }, label = "Sous-titre", placeholder = "Ex : Grossiste B2B", colors = colors)
        HumanTextField(value = shippingTag, onValueChange = { shippingTag = it }, label = "Port intérieur", colors = colors)
        HumanTextField(value = secondaryTag, onValueChange = { secondaryTag = it }, label = "Note secondaire", colors = colors)
        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        HumanPrimaryButton(
            text = "Ajouter la plateforme",
            icon = Icons.Default.Check,
            onClick = {
                if (name.isBlank()) {
                    errorText = "Veuillez saisir le nom de la plateforme."
                    return@HumanPrimaryButton
                }
                onConfirm(name.trim(), subtitle.trim(), shippingTag.trim(), secondaryTag.trim())
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun TransitaireFormSheet(
    existing: CatalogueTransitaireSummaryUi?,
    onDismiss: () -> Unit,
    onAdd: (name: String, hub: String, airRate: Long, seaRate: Long, contact: String, address: String, isActive: Boolean, rateDate: String, airLead: String, seaLead: String, notes: String, airFastRate: Long, airFastLead: String, goodsDelays: String) -> Unit,
    onUpdate: (id: Long, name: String, hub: String, airRate: Long, seaRate: Long, contact: String, address: String, isActive: Boolean, rateDate: String, airLead: String, seaLead: String, notes: String, airFastRate: Long, airFastLead: String, goodsDelays: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var hub by remember { mutableStateOf(existing?.routeHub ?: "Guangzhou -> Antananarivo") }
    var airRateText by remember { mutableStateOf(existing?.airRateAr?.takeIf { it > 0L }?.toString() ?: "") }
    var airFastRateText by remember { mutableStateOf(existing?.airFastRateAr?.takeIf { it > 0L }?.toString() ?: "") }
    var seaRateText by remember { mutableStateOf(existing?.seaRateAr?.takeIf { it > 0L }?.toString() ?: "") }
    var airLead by remember { mutableStateOf(existing?.airLeadDays ?: "") }
    var seaLead by remember { mutableStateOf(existing?.seaLeadDays ?: "") }
    var contact by remember { mutableStateOf(existing?.contact ?: "") }
    var address by remember { mutableStateOf(existing?.chinaWarehouseAddress ?: "") }
    var notes by remember { mutableStateOf(existing?.internalNotes ?: "") }
    var isActive by remember { mutableStateOf(existing?.let { !it.isArchived } ?: true) }
    var rateDateText by remember { mutableStateOf(DateUtils.getTodayFrenchDate()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = if (existing != null) "Modifier ${existing.name}" else "Nouveau transitaire",
        subtitle = "Tarifs aériens / maritimes et entrepôt Chine",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(value = name, onValueChange = { name = it; errorText = null }, label = "Nom du transitaire *", colors = colors)
        HumanTextField(value = hub, onValueChange = { hub = it }, label = "Axe / Hub", colors = colors)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = airRateText,
                onValueChange = { airRateText = it.filter { c -> c.isDigit() } },
                label = "Tarif Aérien (/kg)",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = seaRateText,
                onValueChange = { seaRateText = it.filter { c -> c.isDigit() } },
                label = "Tarif Maritime (/m³)",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
        HumanTextField(
            value = rateDateText,
            onValueChange = { rateDateText = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = "Nouveau tarif valable à partir du (jj/mm/aaaa)",
            colors = colors
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(value = airFastRateText, onValueChange = { airFastRateText = it.filter { c -> c.isDigit() } }, label = "Aérien Express (/kg)", keyboardType = KeyboardType.Number, suffixText = "Ar", colors = colors, modifier = Modifier.weight(1f))
            HumanTextField(value = contact, onValueChange = { contact = it }, label = "Contact / WeChat", colors = colors, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(value = airLead, onValueChange = { airLead = it }, label = "Délai aérien (ex: 5-7 j)", colors = colors, modifier = Modifier.weight(1f))
            HumanTextField(value = seaLead, onValueChange = { seaLead = it }, label = "Délai mer (ex: 35 j)", colors = colors, modifier = Modifier.weight(1f))
        }
        HumanTextField(value = address, onValueChange = { address = it }, label = "Adresse entrepôt Chine", singleLine = false, colors = colors)
        HumanTextField(value = notes, onValueChange = { notes = it }, label = "Notes", singleLine = false, colors = colors)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Partenaire actif", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
        }

        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)

        HumanPrimaryButton(
            text = if (existing != null) "Enregistrer" else "Ajouter le transitaire",
            icon = Icons.Default.Check,
            onClick = {
                if (name.isBlank()) {
                    errorText = "Veuillez saisir le nom du transitaire."
                    return@HumanPrimaryButton
                }
                val air = parseLongInput(airRateText) ?: 0L
                val airFast = parseLongInput(airFastRateText) ?: 0L
                val sea = parseLongInput(seaRateText) ?: 0L
                val chosenDate = if (DateUtils.isValidFrenchDate(rateDateText)) rateDateText.trim() else DateUtils.getTodayFrenchDate()
                val ratesChanged = existing == null || air != existing.airRateAr || sea != existing.seaRateAr
                val dateApp = if (ratesChanged) {
                    chosenDate
                } else {
                    existing?.rateApplicationDate?.ifBlank { chosenDate } ?: chosenDate
                }

                if (existing != null) {
                    onUpdate(existing.id, name.trim(), hub.trim(), air, sea, contact.trim(), address.trim(), isActive, dateApp, airLead.trim(), seaLead.trim(), notes.trim(), airFast, existing.airFastLeadDays, existing.goodsCategoryDelays)
                } else {
                    onAdd(name.trim(), hub.trim(), air, sea, contact.trim(), address.trim(), isActive, dateApp, airLead.trim(), seaLead.trim(), notes.trim(), airFast, "", "")
                }
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun NotificationsSheet(
    notifications: List<BusinessNotificationUi>,
    onDismiss: () -> Unit,
    onNotificationClick: (BusinessNotificationUi) -> Unit
) {
    val colors = rememberTransitaireColors()
    HumanSheetContainer(
        title = "Rappels & Alertes",
        subtitle = if (notifications.isEmpty()) "Tout est calme aujourd'hui" else "${notifications.size} point${if (notifications.size > 1) "s" else ""} à vérifier",
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (notifications.isEmpty()) {
            HumanSectionCard(colors = colors, contentPadding = PaddingValues(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HumanIconBadge(Icons.Default.CheckCircle, colors.successAccent, colors.successSoftBg)
                    Text(
                        text = "Aucune alerte urgente en cours.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
            }
        } else {
            notifications.forEach { notif ->
                HumanSectionCard(
                    colors = colors,
                    contentPadding = PaddingValues(14.dp),
                    onClick = { onNotificationClick(notif) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HumanIconBadge(
                            icon = Icons.Default.Notifications,
                            tint = if (notif.isUrgent) colors.dangerAccent else colors.warningAccent,
                            backgroundColor = if (notif.isUrgent) colors.dangerSoftBg else colors.warningSoftBg
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = notif.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = notif.message,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TransitaireTariffLineSheet(
    transitaire: CatalogueTransitaireSummaryUi,
    existingItem: TariffItem?,
    onDismiss: () -> Unit,
    onSaveGrid: (SyncedTransitaireGridUpdate) -> Unit
) {
    val colors = rememberTransitaireColors()
    var mode by remember(existingItem) { mutableStateOf(existingItem?.mode ?: "AÉRIEN") }
    var shipmentType by remember(existingItem) {
        mutableStateOf(
            existingItem?.shipmentType ?: if (mode == "MARITIME") "Groupage LCL" else "Standard Cargo"
        )
    }
    var goodsCategory by remember(existingItem) {
        mutableStateOf(existingItem?.goodsCategory ?: "Marchandise Générale")
    }
    var priceArText by remember(existingItem) {
        mutableStateOf(existingItem?.priceAr?.takeIf { it > 0L }?.toString() ?: "")
    }
    var usdPriceText by remember(existingItem) {
        mutableStateOf(existingItem?.usdPrice?.let { String.format(Locale.US, "%.0f", it) } ?: "")
    }
    var leadDays by remember(existingItem) {
        mutableStateOf(existingItem?.leadDays ?: if (mode == "MARITIME") "30 à 45 jours" else "7 à 10 jours")
    }
    var conditionOp by remember(existingItem) { mutableStateOf(existingItem?.conditionOp ?: "NONE") }
    var conditionValText by remember(existingItem) {
        mutableStateOf(
            existingItem?.conditionValue?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var effectiveDate by remember(existingItem) {
        mutableStateOf(existingItem?.effectiveDate?.ifBlank { DateUtils.getTodayFrenchDate() } ?: DateUtils.getTodayFrenchDate())
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    val unitLabel = if (mode == "MARITIME") "m³" else "kg"

    HumanSheetContainer(
        title = if (existingItem != null) "Modifier le tarif" else "Nouvelle ligne tarifaire",
        subtitle = transitaire.name,
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanSegmentedSwitch(
            options = listOf("AÉRIEN" to "Aérien (/kg)", "MARITIME" to "Maritime (/m³)"),
            selectedKey = mode,
            onSelect = { newMode ->
                mode = newMode
                if (existingItem == null) {
                    shipmentType = if (newMode == "MARITIME") "Groupage LCL" else "Standard Cargo"
                    leadDays = if (newMode == "MARITIME") "30 à 45 jours" else "7 à 10 jours"
                }
            },
            colors = colors
        )

        val typePresets = if (mode == "MARITIME") {
            listOf("Groupage LCL", "Conteneur FCL", "Maritime Express")
        } else {
            listOf("Standard Cargo", "Express Direct", "Vol Spécial")
        }
        HumanChipRow(
            items = typePresets.map { it to it },
            selectedKey = shipmentType,
            onSelect = { shipmentType = it },
            colors = colors
        )

        HumanTextField(
            value = shipmentType,
            onValueChange = { shipmentType = it; errorText = null },
            label = "Type d'envoi *",
            placeholder = "Ex : Standard Cargo, Express...",
            colors = colors
        )

        HumanTextField(
            value = goodsCategory,
            onValueChange = { goodsCategory = it },
            label = "Catégorie de marchandise",
            placeholder = "Ex : Générale, Batterie, Téléphones...",
            colors = colors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanTextField(
                value = priceArText,
                onValueChange = { priceArText = it.filter { c -> c.isDigit() }; errorText = null },
                label = "Tarif (Ar/$unitLabel) *",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = usdPriceText,
                onValueChange = { usdPriceText = it },
                label = "Réf. USD (opt.)",
                keyboardType = KeyboardType.Decimal,
                suffixText = "$",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanTextField(
                value = leadDays,
                onValueChange = { leadDays = it },
                label = "Délai annoncé",
                placeholder = "7 à 10 jours",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = effectiveDate,
                onValueChange = { effectiveDate = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
                label = "Date d'effet",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Condition de palier (optionnel)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = listOf(
                "NONE" to "Aucune",
                "LT" to "< Seuil",
                "LTE" to "≤ Seuil",
                "GTE" to "≥ Seuil",
                "GT" to "> Seuil"
            ),
            selectedKey = conditionOp,
            onSelect = { conditionOp = it },
            colors = colors
        )
        if (conditionOp != "NONE") {
            HumanTextField(
                value = conditionValText,
                onValueChange = { conditionValText = it },
                label = "Seuil ($unitLabel)",
                placeholder = if (mode == "MARITIME") "Ex : 1" else "Ex : 10",
                keyboardType = KeyboardType.Decimal,
                suffixText = unitLabel,
                colors = colors
            )
        }

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = if (existingItem != null) "Enregistrer ce tarif" else "Ajouter à la grille",
            icon = Icons.Default.Check,
            onClick = {
                val parsedPrice = parseLongInput(priceArText) ?: 0L
                if (shipmentType.isBlank()) {
                    errorText = "Veuillez préciser le type d'envoi."
                    return@HumanPrimaryButton
                }
                if (parsedPrice <= 0L) {
                    errorText = "Le tarif doit être supérieur à 0 Ar."
                    return@HumanPrimaryButton
                }
                val parsedUsd = parseDoubleInput(usdPriceText)?.takeIf { it > 0.0 }
                val parsedCondVal = if (conditionOp != "NONE") parseDoubleInput(conditionValText) else null
                val finalOp = if (parsedCondVal != null && parsedCondVal > 0.0) conditionOp else "NONE"

                val currentGrid = TariffGridParser.parse(
                    raw = transitaire.goodsCategoryDelays,
                    airRateAr = transitaire.airRateAr,
                    airFastRateAr = transitaire.airFastRateAr,
                    seaRateAr = transitaire.seaRateAr
                )

                val newItem = TariffItem(
                    id = existingItem?.id ?: UUID.randomUUID().toString(),
                    mode = mode,
                    shipmentType = shipmentType.trim(),
                    goodsCategory = goodsCategory.trim().ifBlank { "Marchandise Générale" },
                    priceAr = parsedPrice,
                    leadDays = leadDays.trim(),
                    conditionOp = finalOp,
                    conditionValue = parsedCondVal,
                    usdPrice = parsedUsd,
                    effectiveDate = effectiveDate.trim().ifBlank { DateUtils.getTodayFrenchDate() }
                )

                val updatedGrid = if (existingItem != null) {
                    currentGrid.map { if (it.id == existingItem.id) newItem else it }
                } else {
                    currentGrid + newItem
                }

                val chosenDate = if (DateUtils.isValidFrenchDate(effectiveDate)) {
                    effectiveDate.trim()
                } else {
                    DateUtils.getTodayFrenchDate()
                }
                onSaveGrid(syncTransitaireGridValues(transitaire, updatedGrid, preferredEffectiveDate = chosenDate))
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun PayTransitaireSheet(
    transitaire: CatalogueTransitaireSummaryUi,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amountAr: Long, accountName: String, paymentDateMillis: Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }
    var selectedAccount by remember(ariaryAccounts) { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "") }
    var amountText by remember(transitaire.id) {
        mutableStateOf(if (transitaire.balanceDueAr > 0L) transitaire.balanceDueAr.toString() else "")
    }
    var paymentDateMillis by remember(transitaire.id) { mutableStateOf(System.currentTimeMillis()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = "Régler ${transitaire.name}",
        subtitle = "Solde fret dû : ${CurrencyFormatter.formatAriary(transitaire.balanceDueAr)}",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
            label = "Montant réglé *",
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
                items = ariaryAccounts.map { it.name to "${it.name} (${CurrencyFormatter.formatCompactAriary(it.balanceAr)})" },
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        } else {
            Text(
                text = "Aucun compte Ariary actif trouvé. Créez d'abord un compte dans Finances.",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = colors.dangerAccent
            )
        }

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Valider le règlement",
            icon = Icons.Default.Check,
            enabled = ariaryAccounts.isNotEmpty(),
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (amt <= 0L) {
                    errorText = "Le montant doit être supérieur à 0 Ar."
                    return@HumanPrimaryButton
                }
                onConfirm(amt, selectedAccount, paymentDateMillis)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

