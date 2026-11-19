package com.example.ui.business.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanDatePickerField
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.tabs.debtRemainingAr
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun CapitalMovementSheet(
    accounts: List<BusinessAccountEntity>,
    initialIsContribution: Boolean,
    existing: BusinessTransactionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, isContribution: Boolean, amountAr: Long, accountName: String, note: String, affectAccount: Boolean, dateMillis: Long) -> Unit,
    onUpdate: (existing: BusinessTransactionEntity, title: String, isContribution: Boolean, amountAr: Long, accountName: String, note: String, affectAccount: Boolean, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDelete: ((BusinessTransactionEntity) -> Unit)? = null
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    val initialContrib = existing?.let { it.type in setOf("IN", "INCOME", "CAPITAL_IN") } ?: initialIsContribution
    val initialAffectAccount = existing?.let {
        it.type !in setOf("CAPITAL_IN", "CAPITAL_OUT") && !it.accountName.equals("Hors caisse", ignoreCase = true)
    } ?: true
    val initialNote = remember(existing?.id) {
        val tag = existing?.referenceTag.orEmpty()
        when {
            tag.contains(" · ") -> tag.substringAfter(" · ").trim()
            tag.startsWith("Capital", ignoreCase = true) -> ""
            else -> tag
        }
    }

    var isContribution by remember(existing?.id) { mutableStateOf(initialContrib) }
    var title by remember(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var amountText by remember(existing?.id) { mutableStateOf(existing?.amountAr?.toString() ?: "") }
    var selectedAccount by remember(existing?.id, ariaryAccounts) {
        val matched = existing?.accountName?.let { accName ->
            ariaryAccounts.firstOrNull { it.name.equals(accName, ignoreCase = true) }?.name
        }
        mutableStateOf(matched ?: ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var note by remember(existing?.id) { mutableStateOf(initialNote) }
    var affectAccount by remember(existing?.id) { mutableStateOf(initialAffectAccount) }
    var dateMillis by remember(existing?.id) { mutableStateOf(existing?.dateMillis ?: System.currentTimeMillis()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = when {
            existing != null && isContribution -> "Modifier l'apport"
            existing != null -> "Modifier le retrait"
            isContribution -> "Apport de capital"
            else -> "Retrait personnel"
        },
        subtitle = "Suivi de votre mise de départ et de vos prélèvements",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanSegmentedSwitch(
            options = listOf("IN" to "Apport (+)", "OUT" to "Retrait (-)"),
            selectedKey = if (isContribution) "IN" else "OUT",
            onSelect = { isContribution = (it == "IN") },
            colors = colors
        )
        HumanTextField(
            value = title,
            onValueChange = { title = it },
            label = "Libellé (optionnel)",
            placeholder = if (isContribution) "Apport personnel" else "Retrait perso",
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
        if (ariaryAccounts.isNotEmpty() && (isContribution || affectAccount)) {
            Text("Caisse concernée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name },
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }
        if (!isContribution) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Déduire d'une caisse", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                Switch(checked = affectAccount, onCheckedChange = { affectAccount = it })
            }
        }
        HumanDatePickerField(
            dateMillis = dateMillis,
            onDateSelected = { dateMillis = it },
            label = "Date (jj/mm/aaaa)",
            colors = colors
        )
        HumanTextField(value = note, onValueChange = { note = it }, label = "Note (optionnel)", colors = colors)

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Enregistrer",
            icon = Icons.Default.Check,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (amt <= 0L) {
                    errorText = "Veuillez saisir un montant valide."
                    return@HumanPrimaryButton
                }
                val effectiveAffect = if (isContribution) true else affectAccount
                if (existing != null) {
                    onUpdate(existing, title.trim(), isContribution, amt, selectedAccount, note.trim(), effectiveAffect, dateMillis)
                } else {
                    onConfirm(title.trim(), isContribution, amt, selectedAccount, note.trim(), effectiveAffect, dateMillis)
                }
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_capital_movement")
        )

        if (existing != null && onDelete != null) {
            HumanTonalButton(
                text = if (isContribution) "Supprimer cet apport" else "Supprimer ce retrait",
                icon = Icons.Default.DeleteOutline,
                onClick = { showDeleteConfirm = true },
                colors = colors,
                containerColor = colors.dangerSoftBg,
                contentColor = colors.dangerAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_delete_capital_movement")
            )
        }
    }

    if (showDeleteConfirm && existing != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = if (isContribution) "Supprimer cet apport ?" else "Supprimer ce retrait ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "L'opération « ${existing.title} » de ${CurrencyFormatter.formatAriary(existing.amountAr)} sera supprimée et le solde de caisse ajusté.",
                    fontFamily = Inter
                )
            },
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
internal fun InvestmentFormSheet(
    existing: BusinessInvestmentEntity?,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, amountAr: Long, estimatedVal: Long, generatedReturn: Long, accountName: String, notes: String, dateMillis: Long) -> Unit,
    onUpdate: (BusinessInvestmentEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "MATERIEL") }
    var amountText by remember { mutableStateOf(existing?.amountAr?.toString() ?: "") }
    var estimatedText by remember { mutableStateOf(existing?.estimatedValueAr?.toString() ?: "") }
    var returnText by remember { mutableStateOf(existing?.generatedReturnAr?.toString() ?: "0") }
    var selectedAccount by remember { mutableStateOf(existing?.accountName ?: (ariaryAccounts.firstOrNull()?.name ?: "Externe")) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = if (existing != null) "Modifier l'investissement" else "Nouvel investissement",
        subtitle = "Matériel, stock clé, local ou placement",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(value = title, onValueChange = { title = it; errorText = null }, label = "Libellé *", colors = colors)
        HumanChipRow(
            items = listOf(
                "MATERIEL" to "Matériel",
                "PLACEMENT" to "Placement",
                "LOGISTIQUE" to "Logistique",
                "INFRASTRUCTURE" to "Local",
                "MARKETING" to "Marketing",
                "AUTRE" to "Autre"
            ),
            selectedKey = category,
            onSelect = { category = it },
            colors = colors
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
                label = "Montant investi *",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = estimatedText,
                onValueChange = { estimatedText = it.filter { c -> c.isDigit() } },
                label = "Valeur actuelle",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
        HumanTextField(
            value = returnText,
            onValueChange = { returnText = it.filter { c -> c.isDigit() } },
            label = "Revenus générés",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )
        if (existing == null) {
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name } + ("Externe" to "Hors caisse"),
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }
        HumanTextField(value = notes, onValueChange = { notes = it }, label = "Notes", colors = colors)
        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)

        HumanPrimaryButton(
            text = if (existing != null) "Enregistrer" else "Ajouter l'investissement",
            icon = Icons.Default.Check,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (title.isBlank() || amt <= 0L) {
                    errorText = "Renseignez un libellé et un montant valide."
                    return@HumanPrimaryButton
                }
                val est = parseLongInput(estimatedText) ?: amt
                val ret = parseLongInput(returnText) ?: 0L
                if (existing != null) {
                    onUpdate(
                        existing.copy(
                            title = title.trim(),
                            category = category,
                            amountAr = amt,
                            estimatedValueAr = est,
                            generatedReturnAr = ret,
                            notes = notes.trim()
                        )
                    )
                } else {
                    onAdd(title.trim(), category, amt, est, ret, selectedAccount, notes.trim(), System.currentTimeMillis())
                }
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun DebtFormSheet(
    existing: BusinessDebtEntity?,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        direction: String,
        counterparty: String,
        principalAr: Long,
        interestPct: Double,
        dueDateMillis: Long?,
        createdAtMillis: Long,
        frequency: String,
        accountName: String,
        affectAccount: Boolean,
        notes: String
    ) -> Unit,
    onUpdate: (BusinessDebtEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var direction by remember { mutableStateOf(existing?.direction ?: "BORROWED") }
    var counterparty by remember { mutableStateOf(existing?.counterparty ?: "") }
    var principalText by remember { mutableStateOf(existing?.principalAr?.toString() ?: "") }
    var interestText by remember { mutableStateOf(existing?.interestPercent?.toString() ?: "0") }
    var dueDateText by remember { mutableStateOf(existing?.dueDateMillis?.let { DateUtils.formatShortDate(it) } ?: "") }
    var selectedAccount by remember { mutableStateOf(existing?.accountName ?: (ariaryAccounts.firstOrNull()?.name ?: "Espèces")) }
    var affectAccount by remember { mutableStateOf(existing == null) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = if (existing != null) "Modifier l'engagement" else "Nouvel emprunt / prêt",
        subtitle = "Suivez ce que vous devez ou ce qu'on vous doit",
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (existing == null) {
            HumanSegmentedSwitch(
                options = listOf("BORROWED" to "J'emprunte", "LENT" to "Je prête"),
                selectedKey = direction,
                onSelect = { direction = it },
                colors = colors
            )
        }
        HumanTextField(value = title, onValueChange = { title = it; errorText = null }, label = "Libellé *", colors = colors)
        HumanTextField(value = counterparty, onValueChange = { counterparty = it }, label = "Contrepartie (nom)", colors = colors)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = principalText,
                onValueChange = { principalText = it.filter { c -> c.isDigit() }; errorText = null },
                label = "Capital *",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = interestText,
                onValueChange = { interestText = it },
                label = "Intérêt (%)",
                keyboardType = KeyboardType.Decimal,
                suffixText = "%",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
        HumanTextField(
            value = dueDateText,
            onValueChange = { dueDateText = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = "Échéance (jj/mm/aaaa, optionnel)",
            colors = colors
        )
        if (existing == null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Impacter une caisse", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                Switch(checked = affectAccount, onCheckedChange = { affectAccount = it })
            }
            if (affectAccount && ariaryAccounts.isNotEmpty()) {
                HumanChipRow(
                    items = ariaryAccounts.map { it.name to it.name },
                    selectedKey = selectedAccount,
                    onSelect = { selectedAccount = it },
                    colors = colors
                )
            }
        }
        HumanTextField(value = notes, onValueChange = { notes = it }, label = "Notes", colors = colors)
        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)

        HumanPrimaryButton(
            text = if (existing != null) "Enregistrer" else "Créer l'engagement",
            icon = Icons.Default.Check,
            onClick = {
                val principal = parseLongInput(principalText) ?: 0L
                if (title.isBlank() || principal <= 0L) {
                    errorText = "Renseignez un libellé et un montant valide."
                    return@HumanPrimaryButton
                }
                val interest = parseDoubleInput(interestText) ?: 0.0
                val dueMs = if (dueDateText.isNotBlank() && DateUtils.isValidFrenchDate(dueDateText)) {
                    DateUtils.frenchDateToEpochMillis(dueDateText)
                } else null
                if (existing != null) {
                    onUpdate(
                        existing.copy(
                            title = title.trim(),
                            counterparty = counterparty.trim(),
                            principalAr = principal,
                            interestPercent = interest,
                            dueDateMillis = dueMs,
                            notes = notes.trim()
                        )
                    )
                } else {
                    onAdd(
                        title.trim(),
                        direction,
                        counterparty.trim(),
                        principal,
                        interest,
                        dueMs,
                        System.currentTimeMillis(),
                        "Ponctuel",
                        selectedAccount,
                        affectAccount,
                        notes.trim()
                    )
                }
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun DebtPaymentSheet(
    debt: BusinessDebtEntity,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (debt: BusinessDebtEntity, amountAr: Long, accountName: String, affectAccount: Boolean, note: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }
    val remaining = debt.debtRemainingAr()
    var amountText by remember(debt.id) { mutableStateOf(if (remaining > 0L) remaining.toString() else "") }
    var selectedAccount by remember { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "") }
    var affectAccount by remember { mutableStateOf(ariaryAccounts.isNotEmpty()) }
    var note by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val isBorrowed = debt.direction == "BORROWED"

    HumanSheetContainer(
        title = if (isBorrowed) "Rembourser l'emprunt" else "Encaisser le prêt",
        subtitle = "${debt.title} · Reste ${CurrencyFormatter.formatAriary(remaining)}",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
            label = "Montant *",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Impacter une caisse", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
            Switch(checked = affectAccount, onCheckedChange = { affectAccount = it })
        }
        if (affectAccount && ariaryAccounts.isNotEmpty()) {
            HumanChipRow(
                items = ariaryAccounts.map { it.name to it.name },
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }
        HumanTextField(value = note, onValueChange = { note = it }, label = "Note (optionnel)", colors = colors)
        if (!errorText.isNullOrBlank()) Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)

        HumanPrimaryButton(
            text = if (isBorrowed) "Enregistrer le remboursement" else "Enregistrer l'encaissement",
            icon = Icons.Default.Payments,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (amt <= 0L) {
                    errorText = "Montant invalide."
                    return@HumanPrimaryButton
                }
                onConfirm(debt, amt, selectedAccount, affectAccount, note.trim())
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
