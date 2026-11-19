package com.example.ui.business.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.BusinessAdaptiveGrid
import com.example.ui.business.shared.HumanAvatarCircle
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanSearchBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.pastelToneForKey
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun ClientsTab(
    uiState: BusinessUiState,
    navState: BusinessNavigationState,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    val colors = rememberTransitaireColors()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 2 KPIs calmes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Clients actifs",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "${uiState.activeClientsCount}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = colors.cyanAccent
                )
                if (uiState.topClientName.isNotBlank() && uiState.topClientName != "-") {
                    Text(
                        text = "Top : ${uiState.topClientName}",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "À recouvrer",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = CurrencyFormatter.formatCompactAriary(uiState.totalDueClientsAr),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (uiState.totalDueClientsAr > 0L) colors.warningAccent else colors.successAccent,
                    maxLines = 1
                )
                if (uiState.overdueClientsCount > 0) {
                    Text(
                        text = "${uiState.overdueClientsCount} en retard",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.dangerAccent
                    )
                }
            }
        }

        HumanSearchBar(
            query = uiState.clientsSearchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = "Rechercher un client ou téléphone...",
            colors = colors,
            modifier = Modifier.testTag("clients_search_bar")
        )

        HumanChipRow(
            items = listOf(
                "all" to "Tous (${uiState.allClients.size})",
                "due" to "Avec solde dû",
                "business" to "Business",
                "famille" to "Proches"
            ),
            selectedKey = uiState.clientsFilter,
            onSelect = onFilterSelected,
            colors = colors
        )

        if (uiState.clientsList.isEmpty()) {
            HumanEmptyState(
                icon = Icons.Default.People,
                title = "Aucun client trouvé",
                subtitle = "Ajoutez un client ou enregistrez une vente pour construire votre carnet automatiquement.",
                actionLabel = "Nouveau client",
                onActionClick = { navState.activeSheet = BusinessActiveSheet.AddClient },
                colors = colors
            )
        } else {
            BusinessAdaptiveGrid(items = uiState.clientsList) { client ->
                HumanClientCard(
                    client = client,
                    colors = colors,
                    onClick = { navState.activeSheet = BusinessActiveSheet.ClientDetail(client) }
                )
            }
        }
    }
}

@Composable
private fun HumanClientCard(
    client: BusinessClientUi,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val (avatarBg, avatarFg) = pastelToneForKey(client.name, colors)
    val hasDue = client.balanceDueAr > 0L

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
        modifier = Modifier.testTag("client_card_${client.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HumanAvatarCircle(
                initials = client.initials,
                tint = avatarFg,
                backgroundColor = avatarBg,
                size = 46.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = client.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    HumanStatusPill(
                        text = if (!hasDue) "À jour" else CurrencyFormatter.formatCompactAriary(client.balanceDueAr),
                        backgroundColor = when {
                            client.isDueUrgent -> colors.dangerSoftBg
                            hasDue -> colors.warningSoftBg
                            else -> colors.successSoftBg
                        },
                        contentColor = when {
                            client.isDueUrgent -> colors.dangerAccent
                            hasDue -> colors.warningAccent
                            else -> colors.successAccent
                        }
                    )
                }

                Text(
                    text = "${client.category} · ${client.totalOrdersCount} commande${if (client.totalOrdersCount > 1) "s" else ""}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        HumanHairline(colors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total acheté : ${CurrencyFormatter.formatAriary(client.totalPurchasesAr)}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.textPrimary
            )
            Text(
                text = "Dernier : ${client.lastPurchaseDate}",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}
