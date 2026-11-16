package com.example.ui.business.clients

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun ClientsView(
    uiState: BusinessUiState,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClientClick: (BusinessClientUi) -> Unit,
    onNewClientClick: () -> Unit,
    onRecordPayment: (BusinessClientUi) -> Unit,
    onWhatsAppRelance: (BusinessClientUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    var sortByBalance by remember { mutableStateOf(true) }
    val rawList = uiState.clientsList

    val filteredList = rawList.filter { client ->
        val matchesQuery = uiState.clientsSearchQuery.isBlank() ||
            client.name.contains(uiState.clientsSearchQuery, ignoreCase = true) ||
            client.phone.contains(uiState.clientsSearchQuery, ignoreCase = true)

        val matchesFilter = when (uiState.clientsFilter) {
            "due" -> client.balanceDueAr > 0
            "business" -> client.category == "Business"
            "famille", "family" -> client.category == "Famille & amis"
            "myself" -> client.category == "Myself"
            else -> true
        }

        matchesQuery && matchesFilter
    }.let { list ->
        if (sortByBalance) {
            list.sortedWith(compareByDescending<BusinessClientUi> { it.balanceDueAr }.thenByDescending { it.totalPurchasesAr })
        } else {
            list.sortedBy { it.name.lowercase() }
        }
    }

    val topDueClient = filteredList.firstOrNull { it.balanceDueAr > 0 && it.isDueUrgent }
        ?: filteredList.firstOrNull { it.balanceDueAr > 0 }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (uiState.clientsSearchQuery.isEmpty()) {
                                Text(
                                    text = "Rechercher un client (nom ou tél)...",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = uiState.clientsSearchQuery,
                                onValueChange = onSearchQueryChanged,
                                singleLine = true,
                                cursorBrush = SolidColor(colors.cyanAccent),
                                textStyle = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_search_input")
                            )
                        }
                        if (uiState.clientsSearchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onSearchQueryChanged("") }
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { sortByBalance = !sortByBalance },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Changer le tri",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "Tous (${uiState.activeClientsCount})",
                    "due" to "Avec solde dû (${uiState.overdueClientsCount})",
                    "business" to "Business",
                    "famille" to "Famille & amis",
                    "myself" to "Myself"
                ).forEach { (filterKey, label) ->
                    val isSelected = uiState.clientsFilter == filterKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) colors.violetSolidBg else colors.cardBg)
                            .clickable { onFilterSelected(filterKey) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else colors.textSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ACTIFS",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = "${uiState.activeClientsCount}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Clients répertoriés",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (uiState.totalDueClientsAr > 0L) colors.dangerSoftBg else colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = colors.dangerAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TOTAL DÛ",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.dangerAccent,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = formatCompactAriary(uiState.totalDueClientsAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (uiState.totalDueClientsAr < 10_000L) 15.sp else 18.sp,
                                color = if (uiState.totalDueClientsAr > 0L) colors.dangerAccent else colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${uiState.overdueClientsCount} impayé${if (uiState.overdueClientsCount > 1) "s" else ""}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = if (uiState.totalDueClientsAr > 0L) colors.dangerAccent else colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TOP",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = uiState.topClientName.ifBlank { "—" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(uiState.topClientTotalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = colors.violetAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RÉPERTOIRE",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (sortByBalance) "Trier par solde" else "Trier par nom",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { sortByBalance = !sortByBalance }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (filteredList.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.cardBg,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(colors.violetSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = colors.violetAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = if (uiState.clientsSearchQuery.isNotBlank() || uiState.clientsFilter != "all") {
                                    "Aucun client pour ce filtre"
                                } else {
                                    "Aucun client enregistré"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (uiState.clientsSearchQuery.isNotBlank() || uiState.clientsFilter != "all") {
                                    "Modifiez votre recherche ou réinitialisez le filtre pour voir vos clients."
                                } else {
                                    "Appuyez sur le bouton « + Client » pour enregistrer votre premier client."
                                },
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    filteredList.forEach { client ->
                        ClientCard(
                            client = client,
                            colors = colors,
                            onClick = { onClientClick(client) }
                        )
                    }
                }
            }

            topDueClient?.let { spotlightClient ->
                ClientPrioritySpotlightCard(
                    spotlightClient = spotlightClient,
                    colors = colors,
                    onRecordPayment = onRecordPayment,
                    onWhatsAppRelance = onWhatsAppRelance
                )
            }
        }
    }
}
