package net.mypapit.mobile.callsignview.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.mypapit.mobile.callsignview.data.local.CallsignEntity
import net.mypapit.mobile.callsignview.data.local.CallsignWithFavorite
import net.mypapit.mobile.callsignview.ui.theme.AmberWarning
import net.mypapit.mobile.callsignview.ui.theme.AmberWarningContainer
import net.mypapit.mobile.callsignview.ui.theme.AppMotion
import net.mypapit.mobile.callsignview.ui.theme.CallsignMonoStyle
import net.mypapit.mobile.callsignview.ui.theme.ChartSkyBlue
import net.mypapit.mobile.callsignview.ui.theme.ChartViolet
import net.mypapit.mobile.callsignview.ui.theme.ClassCContainer
import net.mypapit.mobile.callsignview.ui.theme.GatewayContainer
import net.mypapit.mobile.callsignview.ui.theme.OnSteelContainer
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent
import net.mypapit.mobile.callsignview.ui.theme.OrangeContainer
import net.mypapit.mobile.callsignview.ui.theme.RadioCyan
import net.mypapit.mobile.callsignview.ui.theme.RadioCyanContainer
import net.mypapit.mobile.callsignview.ui.theme.SignalRed
import net.mypapit.mobile.callsignview.ui.theme.SignalRedContainer
import net.mypapit.mobile.callsignview.ui.theme.SteelContainer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ExpiryStatus {
    ACTIVE,
    EXPIRED,
    EXPIRED_OVER_5_YEARS,
    UNKNOWN
}

fun evaluateExpiryStatus(
    expiryDateIso: String,
    today: LocalDate = LocalDate.now()
): ExpiryStatus {
    if (expiryDateIso.isBlank()) return ExpiryStatus.UNKNOWN
    return try {
        val date = LocalDate.parse(expiryDateIso, DateTimeFormatter.ISO_LOCAL_DATE)
        val fiveYearsAgo = today.minusYears(5)
        when {
            date.isBefore(fiveYearsAgo) -> ExpiryStatus.EXPIRED_OVER_5_YEARS
            date.isBefore(today) -> ExpiryStatus.EXPIRED
            else -> ExpiryStatus.ACTIVE
        }
    } catch (_: Exception) {
        ExpiryStatus.UNKNOWN
    }
}

@Composable
fun ExpiryStatusBadge(
    status: ExpiryStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        ExpiryStatus.ACTIVE -> Triple("ACTIVE", RadioCyanContainer, RadioCyan)
        ExpiryStatus.EXPIRED -> Triple("EXPIRED", AmberWarningContainer, AmberWarning)
        ExpiryStatus.EXPIRED_OVER_5_YEARS -> Triple("EXPIRED >5Y", SignalRedContainer, SignalRed)
        ExpiryStatus.UNKNOWN -> Triple(
            "UNKNOWN",
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

fun formatPrefixClassLabel(prefixGroup: String): String {
    return when (prefixGroup) {
        "9M2", "9M6", "9M8" -> "$prefixGroup • Class A"
        "9M4" -> "9M4 • Club / Special"
        "9W2", "9W6", "9W8" -> "$prefixGroup • Class B"
        "9W3" -> "9W3 • Class C"
        "9W4" -> "9W4 • Gateway / RoIP"
        else -> prefixGroup
    }
}

@Composable
fun CallsignClassBadge(
    prefixGroup: String,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (prefixGroup) {
        "9M2", "9M6", "9M8" -> Triple("CLASS A", OrangeContainer, OrangeAccent)
        "9M4" -> Triple("CLUB", SteelContainer, OnSteelContainer)
        "9W2", "9W6", "9W8" -> Triple("CLASS B", RadioCyanContainer, RadioCyan)
        "9W3" -> Triple("CLASS C", ClassCContainer, ChartViolet)
        "9W4" -> Triple("GATEWAY", GatewayContainer, ChartSkyBlue)
        else -> Triple(
            prefixGroup,
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun CallsignCard(
    entry: CallsignWithFavorite,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val item = entry.item
    val status = evaluateExpiryStatus(item.expiryDateIso)
    val starScale by animateFloatAsState(
        targetValue = if (entry.isFavorite) 1.15f else 1.0f,
        animationSpec = AppMotion.ExpressiveSpring,
        label = "favoriteStarScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                OrangeContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) OrangeAccent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.callsign,
                        style = CallsignMonoStyle,
                        color = OrangeAccent
                    )
                    CallsignClassBadge(prefixGroup = item.prefixGroup)
                    ExpiryStatusBadge(status = status)
                }

                Text(
                    text = item.holderName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AA ${item.assignmentNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.expiryDateDisplay.isNotBlank()) {
                        Text(
                            text = "Exp ${item.expiryDateDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.scale(starScale)
            ) {
                Icon(
                    imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (entry.isFavorite) {
                        "Remove ${item.callsign} from favorites"
                    } else {
                        "Add ${item.callsign} to favorites"
                    },
                    tint = if (entry.isFavorite) OrangeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

fun formatDurationBrief(ms: Long): String {
    val totalMinutes = (ms / 60_000L).coerceAtLeast(1L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatTimestamp(epochMs: Long): String {
    if (epochMs <= 0L) return "Never"
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun buildShareText(item: CallsignEntity): String {
    return buildString {
        appendLine("Callsign: ${item.callsign}")
        appendLine("Holder: ${item.holderName}")
        appendLine("Apparatus Assignment No: ${item.assignmentNo}")
        append("Expiry: ${item.expiryDateDisplay}")
    }
}
