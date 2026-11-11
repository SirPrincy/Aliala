package com.example.ui.business.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.BusinessUiState
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
    var sortByBalance by remember { mutableStateOf(true) }
    val rawList = uiState.clientsList

    val filteredList = rawList.filter { client ->
        val matchesQuery = uiState.clientsSearchQuery.isBlank() ||
                client.name.contains(uiState.clientsSearchQuery, ignoreCase = true) ||
                client.phone.contains(uiState.clientsSearchQuery, ignoreCase = true)

        val matchesFilter = when (uiState.clientsFilter) {
            "due" -> client.balanceDueAr > 0
            "business" -> client.category == "Business"
            "famille" -> client.category == "Famille & amis"
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

    val topDueClient = rawList.firstOrNull { it.balanceDueAr > 0 && it.isDueUrgent } ?: rawList.firstOrNull { it.balanceDueAr > 0 }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // -------------------------------------------------------------
            // 1. SEARCH & FILTER CONTROLS
            // -------------------------------------------------------------
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
                    color = MaterialTheme.colorScheme.surface,
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (uiState.clientsSearchQuery.isEmpty()) {
                                Text(
                                    text = "Rechercher un client (nom ou tél)...",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = uiState.clientsSearchQuery,
                                onValueChange = onSearchQueryChanged,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Changer le tri",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. FILTER CHIPS ROW
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "Tous (${rawList.size})",
                    "due" to "Avec solde dû (${rawList.count { it.balanceDueAr > 0 }})",
                    "business" to "Business (${rawList.count { it.category == "Business" }})",
                    "famille" to "Famille & amis (${rawList.count { it.category == "Famille & amis" }})",
                    "myself" to "Myself (${rawList.count { it.category == "Myself" }})"
                ).forEach { (filterKey, label) ->
                    val isSelected = uiState.clientsFilter == filterKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) Color(0xFF712AE2) else MaterialTheme.colorScheme.surface)
                            .clickable { onFilterSelected(filterKey) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. SUMMARY METRIC STRIP (3 Tiles)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stat 1: Actifs
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
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
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ACTIFS",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF6D797D),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = "${uiState.activeClientsCount}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Clients répertoriés",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }
                }

                // Stat 2: Total Dû
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFDAD6).copy(alpha = 0.5f),
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
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TOTAL DÛ",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFFBA1A1A),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = "${uiState.totalDueClientsAr / 1000}k",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color(0xFFBA1A1A)
                            )
                            Text(
                                text = "${uiState.overdueClientsCount} impayés",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF93000A)
                            )
                        }
                    }
                }

                // Stat 3: Top Client
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
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
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TOP",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF6D797D),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column {
                            Text(
                                text = uiState.topClientName.ifBlank { "—" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(uiState.topClientTotalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF712AE2)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. CLIENTS LIST SECTION
            // -------------------------------------------------------------
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
                        color = Color(0xFF6D797D),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (sortByBalance) "Trier par solde" else "Trier par nom",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF00687A),
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
                        color = MaterialTheme.colorScheme.surface,
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
                                    .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = "Aucun client enregistré",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Appuyez sur le bouton « + Client » pour enregistrer votre premier client.",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color(0xFF6D797D),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    filteredList.forEach { client ->
                        ClientCard(
                            client = client,
                            onClick = { onClientClick(client) }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 5. SPOTLIGHT QUICK ACTION CARD (RINA RAZAFY)
            // -------------------------------------------------------------
            topDueClient?.let { spotlightClient ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF712AE2).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF712AE2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "FICHE PRIORITAIRE",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF712AE2),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = spotlightClient.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                            }

                            val isUrgent = spotlightClient.isDueUrgent
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(if (isUrgent) Color(0xFFFFDAD6) else Color(0xFFFFE0B2))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isUrgent) "Dû urgent" else "Solde dû",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isUrgent) Color(0xFFBA1A1A) else Color(0xFFE65100)
                                )
                            }
                        }

                        // 3-Column KPI Breakdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total achats", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                    Text(
                                        text = CurrencyFormatter.formatAriary(spotlightClient.totalPurchasesAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Column {
                                    Text("Réglé", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                    Text(
                                        text = CurrencyFormatter.formatAriary(spotlightClient.totalPaidAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                                Column {
                                    Text("Solde dû", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                    Text(
                                        text = CurrencyFormatter.formatAriary(spotlightClient.balanceDueAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFBA1A1A)
                                    )
                                }
                            }
                        }

                        // Suggested WhatsApp Message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEAEDFF))
                                .padding(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 1.dp)
                                )
                                Text(
                                    text = "« ${spotlightClient.suggestedRelanceMsg.ifBlank { "Bonjour " + spotlightClient.name + ", rappel amical de ton solde de " + CurrencyFormatter.formatAriary(spotlightClient.balanceDueAr) + " sur ta commande..." }} »",
                                    fontFamily = Inter,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRecordPayment(spotlightClient) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF712AE2),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_encaisser_client_spotlight")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Encaisser",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onWhatsAppRelance(spotlightClient) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF00687A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("btn_relance_whatsapp_spotlight")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Relance WhatsApp",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    client: BusinessClientUi,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (client.avatarType) {
                                    "CYAN" -> Color(0xFF06B6D4)
                                    "RED" -> Color(0xFFFFDAD6)
                                    "INDIGO" -> Color(0xFFD8E2FF)
                                    else -> Color(0xFF8A4CFC)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.initials,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (client.avatarType == "RED") Color(0xFFBA1A1A) else if (client.avatarType == "INDIGO") Color(0xFF005AC2) else Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = client.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(
                                        when (client.category) {
                                            "Business" -> Color(0xFF00687A).copy(alpha = 0.10f)
                                            "Myself" -> Color(0xFFFFEDD5)
                                            else -> Color(0xFFEADDFF)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = client.category,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = when (client.category) {
                                        "Business" -> Color(0xFF00687A)
                                        "Myself" -> Color(0xFFC2410C)
                                        else -> Color(0xFF25005A)
                                    }
                                )
                            }
                        }

                        Text(
                            text = "${CurrencyFormatter.formatAriary(client.totalPurchasesAr)} cumulés • ${client.totalOrdersCount} achat${if (client.totalOrdersCount > 1) "s" else ""}",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF6D797D)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFBCC9CD),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 4.dp)
                )
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEAEDFF))
            )

            // Bottom Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (client.balanceDueAr > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFBA1A1A))
                        )
                        Text(
                            text = "Reste : ${CurrencyFormatter.formatAriary(client.balanceDueAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFBA1A1A)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "À jour ✓",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF00687A)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            if (client.balanceDueAr > 0 && client.isDueUrgent) Color(0xFFFFDAD6)
                            else Color(0xFFF2F3FF)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = client.statusTag,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (client.isDueUrgent) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (client.balanceDueAr > 0 && client.isDueUrgent) Color(0xFFBA1A1A) else Color(0xFF6D797D)
                    )
                }
            }
        }
    }
}

@Composable
fun ClientDetailDialog(
    client: BusinessClientUi,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit,
    onWhatsAppRelance: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF712AE2).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = client.initials,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF712AE2)
                    )
                }
                Column {
                    Text(
                        text = client.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${client.category}${if (client.phone.isNotBlank()) " • ${client.phone}" else ""}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total achats", fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatAriary(client.totalPurchasesAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Réglé", fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatAriary(client.totalPaidAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00687A))
                        }
                        Column {
                            Text("Solde dû", fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatAriary(client.balanceDueAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (client.balanceDueAr > 0) Color(0xFFBA1A1A) else Color(0xFF00687A)
                            )
                        }
                    }
                }

                if (client.balanceDueAr > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onRecordPayment()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF712AE2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Encaisser", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onWhatsAppRelance()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

