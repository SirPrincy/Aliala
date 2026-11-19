package com.example.ui.business.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.BusinessResponsivePair
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun OverviewTab(
    uiState: BusinessUiState,
    navState: BusinessNavigationState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onSelectSubTab: (String) -> Unit
) {
    val colors = rememberTransitaireColors()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Sélecteur de période discret
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mois précédent",
                            tint = colors.textPrimary
                        )
                    }
                    Text(
                        text = uiState.selectedMonthLabel.ifBlank { "Ce mois" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onResetMonth() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mois suivant",
                            tint = colors.textPrimary
                        )
                    }
                }
            }

            if (uiState.monthOffset != 0) {
                Surface(
                    shape = CircleShape,
                    color = colors.cyanSoftBg,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onResetMonth() }
                        .testTag("btn_reset_month")
                ) {
                    Text(
                        text = "Aujourd'hui",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Carte Hero Santé Financière + Portefeuille Yuans intégré
        HumanHeroCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Bénéfice net · ${uiState.selectedMonthLabel.ifBlank { "Mois en cours" }}",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(uiState.netProfit, showSign = uiState.netProfit > 0L),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }

                val trendPositive = uiState.vsPrevMonthPercent >= 0
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (trendPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${if (trendPositive) "+" else ""}${uiState.vsPrevMonthPercent}%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMiniStat(
                    label = "Ventes encaissées",
                    value = CurrencyFormatter.formatCompactAriary(uiState.salesTotal),
                    modifier = Modifier.weight(1f)
                )
                HeroMiniStat(
                    label = "Coûts & fret",
                    value = CurrencyFormatter.formatCompactAriary(uiState.salesCost + uiState.freightAndExpenses),
                    modifier = Modifier.weight(1f)
                )
            }

            // Bande Portefeuille Yuans intégrée
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { navState.activeSheet = BusinessActiveSheet.BuyYuans }
                    .testTag("hero_yuan_strip")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyYen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Portefeuille Yuans · ${formatYuan(uiState.availableYuans)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "CMUP ${CurrencyFormatter.formatAriary(uiState.cmupRate)}/¥ · ~${CurrencyFormatter.formatCompactAriary(uiState.estimatedYuanValueAr)}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.82f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Text(
                            text = "Recharger ¥",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0E7490),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 3. 3 Actions Rapides en pilules
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                label = "Nouvel achat",
                icon = Icons.Default.AddShoppingCart,
                tint = colors.cyanAccent,
                softBg = colors.cyanSoftBg,
                colors = colors,
                testTag = "quick_action_achat",
                modifier = Modifier.weight(1f),
                onClick = { navState.activeSheet = BusinessActiveSheet.OrderForm() }
            )
            QuickActionTile(
                label = "Vente rapide",
                icon = Icons.Default.PointOfSale,
                tint = colors.violetAccent,
                softBg = colors.violetSoftBg,
                colors = colors,
                testTag = "quick_action_vente",
                modifier = Modifier.weight(1f),
                onClick = { navState.activeSheet = BusinessActiveSheet.AddSale() }
            )
            QuickActionTile(
                label = "Opération",
                icon = Icons.Default.AccountBalanceWallet,
                tint = colors.successAccent,
                softBg = colors.successSoftBg,
                colors = colors,
                testTag = "quick_action_operation",
                modifier = Modifier.weight(1f),
                onClick = { navState.activeSheet = BusinessActiveSheet.AddFinanceOperation() }
            )
        }

        // 4. Actions prioritaires (max 2, conditionnelles)
        val hasPriorityItems = uiState.freightDueAmount > 0L || uiState.clientReceivablesAmount > 0L
        if (hasPriorityItems) {
            HumanSectionCard(colors = colors) {
                HumanSectionHeader(
                    title = "À traiter en priorité",
                    subtitle = "Ce qui demande votre attention aujourd'hui",
                    colors = colors
                )
                if (uiState.freightDueAmount > 0L) {
                    PriorityItemRow(
                        icon = Icons.Default.LocalShipping,
                        tint = colors.warningAccent,
                        softBg = colors.warningSoftBg,
                        title = "Fret transitaire à régler",
                        subtitle = if (uiState.freightDueDays > 0) "Échéance sous ${uiState.freightDueDays} j" else "En attente de règlement",
                        amountText = CurrencyFormatter.formatAriary(uiState.freightDueAmount),
                        actionText = "Régler",
                        colors = colors,
                        testTag = "priority_pay_freight",
                        onClick = { onSelectSubTab("Finances") }
                    )
                }
                if (uiState.freightDueAmount > 0L && uiState.clientReceivablesAmount > 0L) {
                    HumanHairline(colors)
                }
                if (uiState.clientReceivablesAmount > 0L) {
                    PriorityItemRow(
                        icon = Icons.Default.ReceiptLong,
                        tint = colors.roseAccent,
                        softBg = colors.roseSoftBg,
                        title = "Créances clients (${uiState.pendingClientsCount})",
                        subtitle = "Factures en attente d'encaissement",
                        amountText = CurrencyFormatter.formatAriary(uiState.clientReceivablesAmount),
                        actionText = "Relancer",
                        colors = colors,
                        testTag = "priority_relance_clients",
                        onClick = { navState.openTopOverdueRelance(uiState) }
                    )
                }
            }
        } else {
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HumanIconBadge(
                        icon = Icons.Default.CheckCircle,
                        tint = colors.successAccent,
                        backgroundColor = colors.successSoftBg,
                        size = 38.dp,
                        iconSize = 20.dp
                    )
                    Column {
                        Text(
                            text = "Tout est à jour",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Aucun fret impayé ni relance urgente en attente.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        // 5. Pipeline simplifié (4 étapes) + Prochains arrivages & Top ventes (responsives)
        val steps = uiState.pipelineSteps
        val orderedCount = (steps.getOrNull(0)?.count ?: 0) + (steps.getOrNull(1)?.count ?: 0)
        val transitCount = (steps.getOrNull(2)?.count ?: 0) + (steps.getOrNull(3)?.count ?: 0)
        val madaCount = steps.getOrNull(4)?.count ?: 0
        val stockedCount = steps.getOrNull(5)?.count ?: 0

        BusinessResponsivePair(
            first = {
                HumanSectionCard(colors = colors) {
                    HumanSectionHeader(
                        title = "Colis en chemin",
                        subtitle = "${uiState.totalPipelineCount} lot${if (uiState.totalPipelineCount > 1) "s" else ""} actifs",
                        actionText = "Voir achats",
                        onActionClick = { onSelectSubTab("Achats") },
                        colors = colors
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PipelineMiniStage("Commandé", orderedCount, colors.cyanAccent, colors.cyanSoftBg, Modifier.weight(1f), colors)
                        PipelineMiniStage("Transit", transitCount, colors.expressAccent, colors.expressSoftBg, Modifier.weight(1f), colors)
                        PipelineMiniStage("Mada", madaCount, colors.warningAccent, colors.warningSoftBg, Modifier.weight(1f), colors)
                        PipelineMiniStage("En stock", stockedCount, colors.successAccent, colors.successSoftBg, Modifier.weight(1f), colors)
                    }

                    if (uiState.incomingParcels.isNotEmpty()) {
                        HumanHairline(colors)
                        uiState.incomingParcels.take(3).forEachIndexed { idx, parcel ->
                            if (idx > 0) HumanHairline(colors)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { navState.openLot(parcel.id) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    HumanIconBadge(
                                        icon = Icons.Default.FlightTakeoff,
                                        tint = if (parcel.isCustoms) colors.warningAccent else colors.cyanAccent,
                                        backgroundColor = if (parcel.isCustoms) colors.warningSoftBg else colors.cyanSoftBg,
                                        size = 38.dp,
                                        iconSize = 18.dp
                                    )
                                    Column {
                                        Text(
                                            text = parcel.productName,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = buildString {
                                                if (!parcel.bordereauCode.isNullOrBlank()) {
                                                    append(parcel.bordereauCode)
                                                    append(" · ")
                                                }
                                                append(parcel.carrier.ifBlank { "Transitaire" })
                                            },
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HumanStatusPill(
                                    text = parcel.delayBadge,
                                    backgroundColor = if (parcel.isCustoms) colors.warningSoftBg else colors.cyanSoftBg,
                                    contentColor = if (parcel.isCustoms) colors.warningAccent else colors.cyanAccent
                                )
                            }
                        }
                    }
                }
            },
            second = {
                HumanSectionCard(colors = colors) {
                    HumanSectionHeader(
                        title = "Meilleures ventes",
                        subtitle = "Articles les plus demandés ce mois",
                        actionText = "Voir ventes",
                        onActionClick = { onSelectSubTab("Ventes") },
                        colors = colors
                    )

                    if (uiState.topProducts.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            HumanIconBadge(
                                icon = Icons.Default.ShoppingBag,
                                tint = colors.violetAccent,
                                backgroundColor = colors.violetSoftBg
                            )
                            Text(
                                text = "Aucune vente enregistrée sur cette période.",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }
                    } else {
                        uiState.topProducts.take(4).forEachIndexed { idx, prod ->
                            if (idx > 0) HumanHairline(colors)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == 0) colors.violetSoftBg else colors.innerCardBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${prod.rank}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (idx == 0) colors.violetAccent else colors.textSecondary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = prod.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${prod.unitsSold} vendu${if (prod.unitsSold > 1) "s" else ""} · Marge ${prod.marginPercent}%",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                HumanStatusPill(
                                    text = prod.stockStatusLabel,
                                    backgroundColor = if (prod.isRestockNeeded) colors.warningSoftBg else colors.successSoftBg,
                                    contentColor = if (prod.isRestockNeeded) colors.warningAccent else colors.successAccent
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun HeroMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    softBg: Color,
    colors: TransitaireColors,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HumanIconBadge(
                icon = icon,
                tint = tint,
                backgroundColor = softBg,
                size = 38.dp,
                iconSize = 19.dp,
                cornerRadius = 12.dp
            )
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PriorityItemRow(
    icon: ImageVector,
    tint: Color,
    softBg: Color,
    title: String,
    subtitle: String,
    amountText: String,
    actionText: String,
    colors: TransitaireColors,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            HumanIconBadge(icon = icon, tint = tint, backgroundColor = softBg)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$subtitle · $amountText",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(shape = CircleShape, color = softBg) {
            Text(
                text = actionText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = tint,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun PipelineMiniStage(
    label: String,
    count: Int,
    tint: Color,
    softBg: Color,
    modifier: Modifier = Modifier,
    colors: TransitaireColors
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.innerCardBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (count > 0) softBg else colors.badgeNeutralBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (count > 0) tint else colors.textSecondary
                )
            }
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
