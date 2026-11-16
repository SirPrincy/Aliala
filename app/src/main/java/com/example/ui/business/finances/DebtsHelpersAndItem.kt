package com.example.ui.business.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun BusinessDebtEntity.totalDueWithInterestAr(): Long {
    val interest = (principalAr.toDouble() * interestPercent / 100.0).roundToLong().coerceAtLeast(0L)
    return principalAr + interest
}

fun BusinessDebtEntity.remainingAr(): Long {
    return (totalDueWithInterestAr() - paidAr).coerceAtLeast(0L)
}

internal fun debtLocalMillisToUtcPickerMillis(localMillis: Long): Long {
    val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        )
    }
    return utcCal.timeInMillis
}

internal fun debtUtcPickerMillisToLocalNoon(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    val localCal = Calendar.getInstance().apply {
        clear()
        set(
            utcCal.get(Calendar.YEAR),
            utcCal.get(Calendar.MONTH),
            utcCal.get(Calendar.DAY_OF_MONTH),
            12, 0, 0
        )
    }
    return localCal.timeInMillis
}

internal fun computeDueBadge(
    dueDateMillis: Long?,
    remaining: Long,
    colors: TransitaireColors
): Triple<String, Color, Color> {
    if (remaining <= 0L) {
        return Triple("Soldé", colors.successSoftBg, colors.successAccent)
    }
    if (dueDateMillis == null) {
        return Triple("Sans échéance", colors.badgeNeutralBg, colors.textSecondary)
    }
    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val dueCal = Calendar.getInstance().apply {
        timeInMillis = dueDateMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((dueCal.timeInMillis - todayCal.timeInMillis) / 86_400_000L).toInt()
    return when {
        diffDays < 0 -> Triple("Retard ${-diffDays} j", colors.dangerSoftBg, colors.dangerAccent)
        diffDays == 0 -> Triple("Aujourd'hui", colors.warningSoftBg, colors.warningAccent)
        diffDays <= 5 -> Triple("Dans $diffDays j", colors.warningSoftBg, colors.warningAccent)
        else -> Triple("Dans $diffDays jours", colors.violetSoftBg, colors.violetAccent)
    }
}

@Composable
internal fun DebtItemCard(
    debt: BusinessDebtEntity,
    colors: TransitaireColors,
    dateFmt: SimpleDateFormat,
    onInspect: () -> Unit,
    onPay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val totalDue = debt.totalDueWithInterestAr()
    val remaining = debt.remainingAr()
    val progressFraction = if (totalDue > 0L) {
        (debt.paidAr.toFloat() / totalDue.toFloat()).coerceIn(0f, 1f)
    } else 1f
    val paidPct = (progressFraction * 100f).roundToInt().coerceIn(0, 100)
    val isBorrowed = debt.direction == "BORROWED"
    val accentColor = if (isBorrowed) colors.violetAccent else colors.successAccent
    val (dueLabel, dueBg, dueFg) = computeDueBadge(debt.dueDateMillis, remaining, colors)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onInspect)
            .testTag("debt_card_${debt.id}"),
        shape = RoundedCornerShape(16.dp),
        color = colors.innerCardBg
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = debt.title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isBorrowed) colors.dangerSoftBg else colors.successSoftBg
                        ) {
                            Text(
                                text = if (isBorrowed) "Emprunt" else "Prêt",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBorrowed) colors.dangerAccent else colors.successAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        val interestStr = if (debt.interestPercent % 1.0 == 0.0) {
                            "${debt.interestPercent.toInt()}% Intérêt"
                        } else {
                            "${String.format(Locale.US, "%.1f", debt.interestPercent)}% Intérêt"
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.violetSoftBg
                        ) {
                            Text(
                                text = interestStr,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.violetAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val detailsLine = buildString {
                        if (debt.counterparty.isNotBlank()) {
                            append(debt.counterparty)
                            append(" · ")
                        }
                        append("Total dû : ${CurrencyFormatter.formatAriary(totalDue)}")
                        append(" · Reste : ${CurrencyFormatter.formatAriary(remaining)}")
                    }
                    Text(
                        text = detailsLine,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Surface(shape = CircleShape, color = dueBg) {
                    Text(
                        text = dueLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dueFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceAtLeast(if (debt.paidAr > 0L) 0.03f else 0f))
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (remaining <= 0L) colors.successAccent else accentColor)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$paidPct% réglé (${CurrencyFormatter.formatAriary(debt.paidAr)} / ${CurrencyFormatter.formatAriary(totalDue)})",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    val rightMeta = buildString {
                        append(debt.repaymentFrequency)
                        debt.dueDateMillis?.let {
                            append(" • ")
                            append(dateFmt.format(Date(it)))
                        }
                    }
                    Text(
                        text = rightMeta,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (remaining > 0L) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_pay_debt_${debt.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBorrowed) colors.violetAccent else colors.successAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBorrowed) "Rembourser" else "Encaisser",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier
                        .then(if (remaining <= 0L) Modifier.weight(1f) else Modifier)
                        .height(38.dp)
                        .testTag("btn_detail_debt_${debt.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Détails",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_edit_debt_${debt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier ${debt.title}",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_delete_debt_${debt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer ${debt.title}",
                        tint = colors.dangerAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun DebtAccountSelectorChips(
    accounts: List<BusinessAccountEntity>,
    selectedAccount: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accounts.forEach { acc ->
            val isSelected = selectedAccount.equals(acc.name, ignoreCase = true)
            Surface(
                modifier = Modifier.clickable { onSelect(acc.name) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = acc.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(acc.balanceAr),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
