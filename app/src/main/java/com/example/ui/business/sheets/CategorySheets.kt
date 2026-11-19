package com.example.ui.business.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessCategoryEntity
import com.example.ui.business.shared.BUSINESS_CATEGORY_COLORS
import com.example.ui.business.shared.BUSINESS_CATEGORY_ICONS
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.shared.resolveCategoryColors
import com.example.ui.business.shared.resolveCategoryIcon
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ManageCategoriesSheet(
    categories: List<BusinessCategoryEntity>,
    initialType: String,
    onDismiss: () -> Unit,
    onAdd: (name: String, type: String, iconKey: String, colorKey: String) -> Unit,
    onUpdate: (existing: BusinessCategoryEntity, name: String, type: String, iconKey: String, colorKey: String) -> Unit,
    onDelete: (BusinessCategoryEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var selectedType by remember(initialType) {
        mutableStateOf(if (initialType == "IN") "IN" else "OUT")
    }
    var editingCategory by remember { mutableStateOf<BusinessCategoryEntity?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var deleteConfirmTarget by remember { mutableStateOf<BusinessCategoryEntity?>(null) }

    var formName by remember { mutableStateOf("") }
    var formType by remember { mutableStateOf(if (initialType == "IN") "IN" else "OUT") }
    var formIconKey by remember { mutableStateOf(BUSINESS_CATEGORY_ICONS.first().key) }
    var formColorKey by remember { mutableStateOf("VIOLET") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isInFormMode = isCreating || editingCategory != null

    fun openCreateForm() {
        editingCategory = null
        formName = ""
        formType = selectedType
        formIconKey = if (selectedType == "IN") "storefront" else "receipt_long"
        formColorKey = if (selectedType == "IN") "GREEN" else "VIOLET"
        errorText = null
        isCreating = true
    }

    fun openEditForm(cat: BusinessCategoryEntity) {
        isCreating = false
        editingCategory = cat
        formName = cat.name
        formType = if (cat.type == "IN") "IN" else "OUT"
        formIconKey = cat.iconKey
        formColorKey = cat.colorKey
        errorText = null
    }

    fun closeForm() {
        isCreating = false
        editingCategory = null
        errorText = null
    }

    BackHandler(enabled = isInFormMode) {
        closeForm()
    }

    val filteredCategories = remember(categories, selectedType) {
        categories
            .filter { it.type == selectedType }
            .sortedWith(compareBy<BusinessCategoryEntity> { it.displayOrder }.thenBy { it.id })
    }

    HumanSheetContainer(
        title = when {
            editingCategory != null -> "Modifier la catégorie"
            isCreating -> "Nouvelle catégorie"
            else -> "Catégories Business"
        },
        subtitle = if (isInFormMode) {
            "Nom, icône et couleur pour vos opérations"
        } else {
            "Organisez vos revenus et dépenses avec des icônes"
        },
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (!isInFormMode) {
            HumanSegmentedSwitch(
                options = listOf("OUT" to "Dépenses (-)", "IN" to "Revenus (+)"),
                selectedKey = selectedType,
                onSelect = { selectedType = it },
                colors = colors
            )

            if (filteredCategories.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.innerCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune catégorie configurée pour ce type.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                HumanSectionCard(
                    colors = colors,
                    containerColor = colors.innerCardBg,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    filteredCategories.forEachIndexed { idx, cat ->
                        if (idx > 0) HumanHairline(colors)
                        val (catAccent, catSoftBg) = resolveCategoryColors(cat.colorKey, colors)
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
                                HumanIconBadge(
                                    icon = resolveCategoryIcon(cat.iconKey),
                                    tint = catAccent,
                                    backgroundColor = catSoftBg,
                                    size = 40.dp,
                                    iconSize = 20.dp,
                                    cornerRadius = 12.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = cat.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (cat.isDefault) {
                                        Text(
                                            text = "Par défaut",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { openEditForm(cat) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_edit_category_${cat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifier ${cat.name}",
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deleteConfirmTarget = cat },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_delete_category_${cat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer ${cat.name}",
                                        tint = colors.dangerAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HumanPrimaryButton(
                text = "Nouvelle catégorie",
                icon = Icons.Default.Add,
                onClick = { openCreateForm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_new_category")
            )
        } else {
            val (previewAccent, previewSoftBg) = resolveCategoryColors(formColorKey, colors)

            // Aperçu en direct
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, previewAccent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HumanIconBadge(
                        icon = resolveCategoryIcon(formIconKey),
                        tint = previewAccent,
                        backgroundColor = previewSoftBg,
                        size = 44.dp,
                        iconSize = 22.dp,
                        cornerRadius = 14.dp
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = formName.trim().ifBlank { "Aperçu de la catégorie" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        HumanStatusPill(
                            text = if (formType == "IN") "Revenu (+)" else "Dépense (-)",
                            backgroundColor = previewSoftBg,
                            contentColor = previewAccent
                        )
                    }
                }
            }

            HumanSegmentedSwitch(
                options = listOf("OUT" to "Dépense (-)", "IN" to "Revenu (+)"),
                selectedKey = formType,
                onSelect = { formType = it; errorText = null },
                colors = colors
            )

            HumanTextField(
                value = formName,
                onValueChange = { formName = it; errorText = null },
                label = "Nom de la catégorie *",
                placeholder = "Ex : Livraison, Marketing, Sav...",
                colors = colors,
                modifier = Modifier.testTag("input_category_name")
            )

            Text(
                text = "Couleur",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BUSINESS_CATEGORY_COLORS.forEach { colorOpt ->
                    val (accent, softBg) = resolveCategoryColors(colorOpt.key, colors)
                    val isSelected = formColorKey == colorOpt.key
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { formColorKey = colorOpt.key }
                            .testTag("cat_color_${colorOpt.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 38.dp else 32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accent else softBg)
                                .border(
                                    width = if (isSelected) 2.dp else 1.5.dp,
                                    color = accent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = colorOpt.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Icône",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BUSINESS_CATEGORY_ICONS.forEach { iconOpt ->
                    val isSelected = formIconKey == iconOpt.key
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) previewSoftBg else colors.innerCardBg,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) previewAccent else colors.border
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { formIconKey = iconOpt.key }
                            .testTag("cat_icon_${iconOpt.key}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconOpt.icon,
                                contentDescription = iconOpt.label,
                                tint = if (isSelected) previewAccent else colors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (!errorText.isNullOrBlank()) {
                Text(
                    text = errorText!!,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.dangerAccent
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HumanTonalButton(
                    text = "Retour",
                    onClick = { closeForm() },
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .testTag("btn_cancel_category_form")
                )
                HumanPrimaryButton(
                    text = "Enregistrer",
                    icon = Icons.Default.Check,
                    onClick = {
                        val cleanName = formName.trim()
                        if (cleanName.isBlank()) {
                            errorText = "Veuillez saisir un nom de catégorie."
                            return@HumanPrimaryButton
                        }
                        val currentEditing = editingCategory
                        val duplicateExists = categories.any {
                            it.type == formType &&
                                it.id != (currentEditing?.id ?: -1L) &&
                                it.name.equals(cleanName, ignoreCase = true)
                        }
                        if (duplicateExists) {
                            errorText = "Une catégorie « $cleanName » existe déjà pour ce type."
                            return@HumanPrimaryButton
                        }
                        if (currentEditing != null) {
                            onUpdate(currentEditing, cleanName, formType, formIconKey, formColorKey)
                        } else {
                            onAdd(cleanName, formType, formIconKey, formColorKey)
                        }
                        selectedType = formType
                        closeForm()
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("btn_save_category")
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
    }

    deleteConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = {
                Text(
                    text = "Supprimer « ${target.name} » ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Les opérations déjà enregistrées conserveront leur historique sans être supprimées.",
                    fontFamily = Inter
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target)
                        deleteConfirmTarget = null
                    }
                ) {
                    Text(
                        text = "Supprimer",
                        color = colors.dangerAccent,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTarget = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}
