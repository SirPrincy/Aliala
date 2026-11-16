package com.example.ui.business.managers

import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.data.repository.AppRepository
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import kotlin.math.roundToLong

data class YuanOperationResult(
    val success: Boolean,
    val message: String
)

class FinancialReporter(private val repository: AppRepository) {

    private fun formatYuanAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) amount.toInt().toString()
        else String.format(Locale.US, "%.2f", amount)
    }

    suspend fun buyYuans(
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        sourceAccountName: String = ""
    ): String {
        if (amountYuan <= 0.0 || rateAr <= 0.0) return "Montant ou taux invalide"
        val accounts = repository.getBusinessAccountsSnapshot()
        val yuanAccounts = accounts.filter { it.isYuanAccount && it.isActive }
        if (yuanAccounts.isEmpty()) {
            return "Achat bloqué : veuillez d'abord créer un compte Yuan (Alipay ou Wechat Pay)."
        }

        val cleanPlatform = platform.trim()
        val targetYuanAccount = yuanAccounts.firstOrNull {
            it.name.equals(cleanPlatform, ignoreCase = true)
        } ?: yuanAccounts.firstOrNull {
            cleanPlatform.isNotBlank() && (
                it.name.contains(cleanPlatform, ignoreCase = true) ||
                    cleanPlatform.contains(it.name, ignoreCase = true)
                )
        }
        if (targetYuanAccount == null) {
            return "Achat bloqué : compte Yuan « $cleanPlatform » introuvable. Créez Alipay ou Wechat Pay."
        }

        val ariaryAccounts = accounts.filter { !it.isYuanAccount && it.isActive }
        val rawSource = sourceAccountName.trim()
        val isExternal = rawSource.isBlank() ||
            rawSource.equals("Externe", ignoreCase = true) ||
            rawSource.equals("Hors caisse", ignoreCase = true)

        val accountLabel = if (isExternal) {
            "Externe"
        } else {
            val matchedSource = ariaryAccounts.firstOrNull { it.name.equals(rawSource, ignoreCase = true) }
                ?: return "Compte payeur « $rawSource » introuvable. Veuillez créer ou choisir un compte Ariary."
            matchedSource.name
        }

        val amountAr = (amountYuan * rateAr).roundToLong()
        val amountDisplay = formatYuanAmount(amountYuan)
        val note = if (accountLabel != "Externe") {
            "Recharge ${targetYuanAccount.name} via $accountLabel"
        } else {
            "Recharge ${targetYuanAccount.name}"
        }

        repository.runInTransaction {
            val yuanTxId = repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "BUY",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = targetYuanAccount.name,
                    note = note
                )
            )
            repository.adjustYuanAccountBalance(amountYuan, targetYuanAccount.name)

            repository.insertBusinessTransaction(
                BusinessTransactionEntity(
                    title = "Achat ¥ $amountDisplay (${targetYuanAccount.name})",
                    type = "YUAN",
                    amountAr = amountAr,
                    accountName = accountLabel,
                    yuanTransactionId = yuanTxId,
                    referenceTag = "Change ¥ • ${rateAr.toInt()} Ar/¥"
                )
            )
        }
        return "¥ $amountDisplay ajoutés à ${targetYuanAccount.name} (${CurrencyFormatter.formatAriary(amountAr)})"
    }

    suspend fun refundYuans(
        amountYuan: Double,
        rateAr: Double,
        platform: String = "Alipay",
        note: String = "",
        orderId: Long? = null
    ): YuanOperationResult {
        if (amountYuan <= 0.0) return YuanOperationResult(true, "")
        if (orderId != null && orderId > 0L) {
            val existingTxs = repository.yuanTransactions.firstOrNull() ?: emptyList()
            val alreadyRefunded = existingTxs.any { it.orderId == orderId && it.type == "REFUND" }
            if (alreadyRefunded) {
                return YuanOperationResult(true, "Remboursement déjà effectué pour ce lot")
            }
        }
        val yuanAccounts = repository.getBusinessAccountsSnapshot().filter { it.isYuanAccount && it.isActive }
        val cleanPlatform = platform.trim()
        val targetAccount = if (cleanPlatform.isNotBlank()) {
            yuanAccounts.firstOrNull { it.name.equals(cleanPlatform, ignoreCase = true) }
                ?: yuanAccounts.firstOrNull {
                    it.name.contains(cleanPlatform, ignoreCase = true) ||
                        cleanPlatform.contains(it.name, ignoreCase = true)
                }
        } else {
            null
        } ?: yuanAccounts.firstOrNull()
        val resolvedAccountName = targetAccount?.name ?: cleanPlatform.ifBlank { "Alipay" }

        val amountAr = (amountYuan * rateAr).roundToLong()
        return repository.runInTransaction {
            val insertedId = repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "REFUND",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = resolvedAccountName,
                    note = note,
                    orderId = orderId
                )
            )
            if (insertedId == -1L) {
                YuanOperationResult(true, "Remboursement déjà enregistré pour ce lot")
            } else {
                repository.adjustYuanAccountBalance(amountYuan, resolvedAccountName)
                YuanOperationResult(true, "Remboursement usine de ¥ ${formatYuanAmount(amountYuan)} crédité sur $resolvedAccountName")
            }
        }
    }

    suspend fun spendYuans(
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        note: String,
        orderId: Long? = null,
        availableYuans: Double = 0.0,
        skipBalanceCheck: Boolean = false
    ): YuanOperationResult {
        if (amountYuan <= 0.0) return YuanOperationResult(false, "Montant en Yuans invalide")
        val yuanAccounts = repository.getBusinessAccountsSnapshot().filter { it.isYuanAccount && it.isActive }
        if (yuanAccounts.isEmpty()) {
            return YuanOperationResult(false, "Paiement bloqué : veuillez d'abord créer un compte Yuan (Alipay ou Wechat Pay).")
        }

        val cleanPlatform = platform.trim()
        val targetYuanAccount = if (cleanPlatform.isNotBlank()) {
            yuanAccounts.firstOrNull { it.name.equals(cleanPlatform, ignoreCase = true) }
                ?: yuanAccounts.firstOrNull {
                    it.name.contains(cleanPlatform, ignoreCase = true) ||
                        cleanPlatform.contains(it.name, ignoreCase = true)
                }
        } else {
            null
        } ?: yuanAccounts.maxByOrNull { it.balanceYuan } ?: yuanAccounts.first()

        if (!skipBalanceCheck) {
            val accountsSum = yuanAccounts.sumOf { it.balanceYuan }
            if (accountsSum > 0.0) {
                if (amountYuan > targetYuanAccount.balanceYuan + 0.001) {
                    return YuanOperationResult(
                        false,
                        "Solde ${targetYuanAccount.name} insuffisant (¥ ${formatYuanAmount(targetYuanAccount.balanceYuan)} dispo, ¥ ${formatYuanAmount(amountYuan)} requis)"
                    )
                }
            } else if (amountYuan > availableYuans + 0.001) {
                return YuanOperationResult(
                    false,
                    "Solde Yuan insuffisant (¥ ${formatYuanAmount(availableYuans)} dispo, ¥ ${formatYuanAmount(amountYuan)} requis)"
                )
            }
        }

        if (orderId != null && orderId > 0L) {
            val existingTxs = repository.yuanTransactions.firstOrNull() ?: emptyList()
            val alreadyDebited = existingTxs.any { it.orderId == orderId && it.type == "SPEND" }
            if (alreadyDebited) {
                return YuanOperationResult(true, "Paiement déjà effectué pour ce lot")
            }
        }

        val amountAr = (amountYuan * rateAr).roundToLong()
        return repository.runInTransaction {
            val insertedId = repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "SPEND",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = targetYuanAccount.name,
                    note = note,
                    orderId = orderId
                )
            )
            if (insertedId == -1L) {
                YuanOperationResult(true, "Paiement déjà effectué pour ce lot")
            } else {
                repository.adjustYuanAccountBalance(-amountYuan, targetYuanAccount.name)
                YuanOperationResult(true, "Paiement de ¥ ${formatYuanAmount(amountYuan)} débité de ${targetYuanAccount.name}")
            }
        }
    }

    suspend fun addBusinessAccount(
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean = false,
        initialBalanceYuan: Double = 0.0
    ): String {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return "Veuillez saisir un nom de compte"
        val existing = repository.getBusinessAccountsSnapshot()
        if (existing.any { it.name.equals(cleanName, ignoreCase = true) }) {
            return "Un compte nommé « $cleanName » existe déjà"
        }
        val shortCode = when {
            cleanName.equals("Espèces", ignoreCase = true) -> "CASH"
            cleanName.contains("Mvola", ignoreCase = true) -> "MV"
            cleanName.contains("Alipay", ignoreCase = true) -> "ALI"
            cleanName.contains("Wechat", ignoreCase = true) -> "WXP"
            else -> cleanName.take(3).uppercase().ifBlank { "CPT" }
        }
        repository.insertBusinessAccount(
            BusinessAccountEntity(
                name = cleanName,
                subtitle = subtitle.trim().ifBlank { if (isYuanAccount) "Chine (¥)" else "Compte" },
                shortCode = shortCode,
                balanceAr = if (isYuanAccount) 0L else initialBalanceAr,
                isYuanAccount = isYuanAccount,
                balanceYuan = if (isYuanAccount) initialBalanceYuan else 0.0
            )
        )
        return "Compte « $cleanName » ajouté"
    }

    suspend fun updateBusinessAccount(
        existing: BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ): String {
        val cleanName = newName.trim()
        if (cleanName.isBlank()) return "Veuillez saisir un nom de compte"
        val allAccounts = repository.getBusinessAccountsSnapshot()
        if (allAccounts.any { it.id != existing.id && it.name.equals(cleanName, ignoreCase = true) }) {
            return "Un autre compte nommé « $cleanName » existe déjà"
        }
        val updated = existing.copy(
            name = cleanName,
            subtitle = newSubtitle.trim().ifBlank { if (existing.isYuanAccount) "Chine (¥)" else "Compte" },
            balanceAr = if (existing.isYuanAccount) 0L else newBalanceAr,
            balanceYuan = if (existing.isYuanAccount) newBalanceYuan else 0.0
        )
        repository.updateBusinessAccount(updated, oldName = existing.name)
        return "Compte « $cleanName » mis à jour"
    }

    suspend fun deleteBusinessAccount(account: BusinessAccountEntity): String {
        repository.deleteBusinessAccount(account.id)
        return "Compte « ${account.name} » supprimé"
    }

    suspend fun addBusinessTransaction(
        title: String,
        type: String,
        amountAr: Long,
        accountName: String,
        referenceTag: String = "",
        destinationAccount: String = ""
    ): String {
        repository.insertBusinessTransaction(
            BusinessTransactionEntity(
                title = title.trim(),
                type = type,
                amountAr = amountAr,
                accountName = accountName.trim().ifBlank { "Espèces" },
                destinationAccount = destinationAccount.trim(),
                referenceTag = referenceTag.trim()
            )
        )
        return "Opération « $title » enregistrée"
    }

    suspend fun deleteBusinessTransaction(tx: BusinessTransactionEntity): String {
        repository.deleteBusinessTransaction(tx)
        return "Opération « ${tx.title} » supprimée"
    }

    suspend fun recordCapitalMovement(
        title: String,
        isContribution: Boolean,
        amountAr: Long,
        accountName: String,
        note: String = "",
        affectAccount: Boolean = true,
        dateMillis: Long = System.currentTimeMillis()
    ): String {
        if (amountAr <= 0L) return "Montant invalide"
        val mustUseAccount = isContribution || affectAccount
        val cleanAccount: String
        if (mustUseAccount) {
            val ariaryAccounts = repository.getBusinessAccountsSnapshot()
                .filter { !it.isYuanAccount && it.isActive }
            if (ariaryAccounts.isEmpty()) {
                return "Apport bloqué : veuillez d'abord créer un compte Ariary (Espèces, Mvola...)."
            }
            val matched = ariaryAccounts.firstOrNull {
                it.name.equals(accountName.trim(), ignoreCase = true)
            } ?: return "Compte « ${accountName.trim()} » introuvable. Veuillez créer ou choisir un compte."
            cleanAccount = matched.name
        } else {
            cleanAccount = "Hors caisse"
        }

        val cleanTitle = title.trim().ifBlank {
            if (isContribution) "Apport de capital" else "Retrait personnel"
        }
        val txType = if (mustUseAccount) {
            if (isContribution) "IN" else "OUT"
        } else {
            if (isContribution) "CAPITAL_IN" else "CAPITAL_OUT"
        }
        val tag = buildString {
            append(if (isContribution) "Capital • Apport" else "Capital • Retrait perso")
            if (!mustUseAccount) append(" (Hors caisse)")
            if (note.isNotBlank()) {
                append(" · ")
                append(note.trim())
            }
        }
        repository.insertBusinessTransaction(
            BusinessTransactionEntity(
                title = cleanTitle,
                type = txType,
                amountAr = amountAr,
                accountName = cleanAccount,
                referenceTag = tag,
                dateMillis = dateMillis
            )
        )
        val actionLabel = if (isContribution) "Apport de capital" else "Retrait personnel"
        return "$actionLabel de ${CurrencyFormatter.formatAriary(amountAr)} enregistré"
    }

    suspend fun addBusinessInvestment(
        title: String,
        category: String,
        amountAr: Long,
        estimatedValueAr: Long,
        generatedReturnAr: Long,
        accountName: String,
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ): String {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return "Veuillez saisir un titre d'investissement"
        if (amountAr <= 0L) return "Veuillez saisir un montant investi valide"

        val cleanAccount = accountName.trim().ifBlank { "Hors caisse" }
        val isExternal = cleanAccount.equals("Externe", ignoreCase = true) ||
            cleanAccount.equals("Hors caisse", ignoreCase = true)

        repository.runInTransaction {
            val linkedTxId = if (!isExternal) {
                val catLabel = when (category) {
                    "PLACEMENT" -> "Argent / Placement"
                    "MATERIEL" -> "Matériel"
                    "LOGISTIQUE" -> "Logistique"
                    "STOCK_STRATEGIQUE" -> "Stock stratégique"
                    "MARKETING" -> "Marketing"
                    "INFRASTRUCTURE" -> "Infrastructure"
                    else -> "Autre"
                }
                repository.insertBusinessTransaction(
                    BusinessTransactionEntity(
                        title = "Investissement : $cleanTitle",
                        type = "OUT",
                        amountAr = amountAr,
                        accountName = cleanAccount,
                        referenceTag = "Investissement • $catLabel",
                        dateMillis = dateMillis
                    )
                )
            } else null

            repository.insertBusinessInvestment(
                BusinessInvestmentEntity(
                    title = cleanTitle,
                    category = category,
                    amountAr = amountAr,
                    estimatedValueAr = estimatedValueAr.coerceAtLeast(0L),
                    generatedReturnAr = generatedReturnAr.coerceAtLeast(0L),
                    status = "ACTIF",
                    accountName = cleanAccount,
                    linkedTransactionId = linkedTxId,
                    dateMillis = dateMillis,
                    notes = notes.trim()
                )
            )
        }
        return "Investissement « $cleanTitle » enregistré (${CurrencyFormatter.formatAriary(amountAr)})"
    }

    suspend fun updateBusinessInvestment(investment: BusinessInvestmentEntity): String {
        repository.updateBusinessInvestment(investment)
        return "Investissement « ${investment.title} » mis à jour"
    }

    suspend fun deleteBusinessInvestment(investment: BusinessInvestmentEntity): String {
        repository.deleteBusinessInvestment(investment)
        return "Investissement « ${investment.title} » supprimé"
    }

    suspend fun addBusinessDebt(
        title: String,
        direction: String,
        counterparty: String,
        principalAr: Long,
        interestPercent: Double,
        dueDateMillis: Long?,
        createdAtMillis: Long,
        repaymentFrequency: String,
        accountName: String,
        affectAccount: Boolean,
        notes: String = ""
    ): String {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return "Veuillez saisir un libellé pour l'emprunt ou le prêt"
        if (principalAr <= 0L) return "Veuillez saisir un montant valide"

        val isBorrowed = direction == "BORROWED"
        val cleanCounterparty = counterparty.trim()
        return repository.runInTransaction {
            val chosenAccount: String
            val linkedTxId: Long?

            if (affectAccount) {
                val ariaryAccounts = repository.getBusinessAccountsSnapshot()
                    .filter { !it.isYuanAccount && it.isActive }
                if (ariaryAccounts.isEmpty()) {
                    return@runInTransaction "Opération bloquée : créez d'abord un compte Ariary ou désactivez l'impact sur compte."
                }
                val matched = ariaryAccounts.firstOrNull {
                    it.name.equals(accountName.trim(), ignoreCase = true)
                } ?: return@runInTransaction "Compte « ${accountName.trim()} » introuvable."
                chosenAccount = matched.name

                val tagBase = if (isBorrowed) "Emprunt" else "Prêt"
                val tag = if (cleanCounterparty.isNotBlank()) "$tagBase • $cleanCounterparty" else tagBase
                linkedTxId = repository.insertBusinessTransaction(
                    BusinessTransactionEntity(
                        title = if (isBorrowed) "Emprunt : $cleanTitle" else "Prêt : $cleanTitle",
                        type = if (isBorrowed) "IN" else "OUT",
                        amountAr = principalAr,
                        accountName = chosenAccount,
                        referenceTag = tag,
                        dateMillis = createdAtMillis
                    )
                )
            } else {
                chosenAccount = "Hors caisse"
                linkedTxId = null
            }

            repository.insertBusinessDebt(
                BusinessDebtEntity(
                    title = cleanTitle,
                    direction = if (isBorrowed) "BORROWED" else "LENT",
                    counterparty = cleanCounterparty,
                    principalAr = principalAr,
                    paidAr = 0L,
                    interestPercent = interestPercent.coerceAtLeast(0.0),
                    dueDateMillis = dueDateMillis,
                    createdAtMillis = createdAtMillis,
                    repaymentFrequency = repaymentFrequency.trim().ifBlank { "Ponctuel" },
                    accountName = chosenAccount,
                    linkedTransactionId = linkedTxId,
                    notes = notes.trim()
                )
            )
            val label = if (isBorrowed) "Emprunt" else "Prêt"
            "$label « $cleanTitle » enregistré (${CurrencyFormatter.formatAriary(principalAr)})"
        }
    }

    suspend fun recordBusinessDebtPayment(
        debt: BusinessDebtEntity,
        amountAr: Long,
        accountName: String,
        affectAccount: Boolean,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ): String {
        if (amountAr <= 0L) return "Veuillez saisir un montant de règlement valide"
        val isBorrowed = debt.direction == "BORROWED"
        return repository.runInTransaction {
            val chosenAccount: String
            val linkedTxId: Long?

            if (affectAccount) {
                val ariaryAccounts = repository.getBusinessAccountsSnapshot()
                    .filter { !it.isYuanAccount && it.isActive }
                if (ariaryAccounts.isEmpty()) {
                    return@runInTransaction "Opération bloquée : créez d'abord un compte Ariary ou désactivez l'impact sur compte."
                }
                val matched = ariaryAccounts.firstOrNull {
                    it.name.equals(accountName.trim(), ignoreCase = true)
                } ?: return@runInTransaction "Compte « ${accountName.trim()} » introuvable."
                chosenAccount = matched.name

                val tag = if (isBorrowed) "Remboursement Emprunt" else "Recouvrement Prêt"
                linkedTxId = repository.insertBusinessTransaction(
                    BusinessTransactionEntity(
                        title = if (isBorrowed) "Remb. : ${debt.title}" else "Recouvr. : ${debt.title}",
                        type = if (isBorrowed) "OUT" else "IN",
                        amountAr = amountAr,
                        accountName = chosenAccount,
                        referenceTag = tag,
                        dateMillis = dateMillis
                    )
                )
            } else {
                chosenAccount = "Hors caisse"
                linkedTxId = null
            }

            repository.insertBusinessDebtPayment(
                BusinessDebtPaymentEntity(
                    debtId = debt.id,
                    amountAr = amountAr,
                    accountName = chosenAccount,
                    linkedTransactionId = linkedTxId,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
            repository.updateBusinessDebt(debt.copy(paidAr = debt.paidAr + amountAr))
            val action = if (isBorrowed) "Remboursement" else "Recouvrement"
            "$action de ${CurrencyFormatter.formatAriary(amountAr)} enregistré"
        }
    }

    suspend fun updateBusinessDebt(debt: BusinessDebtEntity): String {
        val cleanTitle = debt.title.trim()
        if (cleanTitle.isBlank()) return "Veuillez saisir un libellé"
        if (debt.principalAr <= 0L) return "Montant initial invalide"
        repository.updateBusinessDebt(
            debt.copy(
                title = cleanTitle,
                counterparty = debt.counterparty.trim(),
                interestPercent = debt.interestPercent.coerceAtLeast(0.0),
                notes = debt.notes.trim()
            )
        )
        return "Engagement « $cleanTitle » mis à jour"
    }

    suspend fun deleteBusinessDebt(debt: BusinessDebtEntity): String {
        repository.deleteBusinessDebt(debt)
        return "Engagement « ${debt.title} » supprimé"
    }
}
