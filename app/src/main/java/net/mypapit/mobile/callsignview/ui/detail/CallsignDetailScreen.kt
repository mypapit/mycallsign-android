package net.mypapit.mobile.callsignview.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.mypapit.mobile.callsignview.data.repository.CallsignRepository
import net.mypapit.mobile.callsignview.ui.components.CallsignClassBadge
import net.mypapit.mobile.callsignview.ui.components.CompactTopAppBarHeight
import net.mypapit.mobile.callsignview.ui.components.ExpiryStatus
import net.mypapit.mobile.callsignview.ui.components.ExpiryStatusBadge
import net.mypapit.mobile.callsignview.ui.components.buildShareText
import net.mypapit.mobile.callsignview.ui.components.evaluateExpiryStatus
import net.mypapit.mobile.callsignview.ui.components.formatPrefixClassLabel
import net.mypapit.mobile.callsignview.ui.theme.AppMotion
import net.mypapit.mobile.callsignview.ui.theme.CallsignMonoStyle
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent

@Composable
fun CallsignDetailScreen(
    callsign: String,
    repository: CallsignRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    CallsignDetailPane(
        callsign = callsign,
        repository = repository,
        showBackButton = true,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsignDetailPane(
    callsign: String,
    repository: CallsignRepository,
    showBackButton: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry by repository.observeByCallsign(callsign)
        .collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (showBackButton) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                expandedHeight = CompactTopAppBarHeight,
                title = { Text(callsign, style = CallsignMonoStyle) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showBackButton) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    }
                ),
                actions = {
                    val current = entry
                    if (current != null) {
                        val starScale by animateFloatAsState(
                            targetValue = if (current.isFavorite) 1.18f else 1.0f,
                            animationSpec = AppMotion.ExpressiveSpring,
                            label = "detailStarScale"
                        )
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.toggleFavorite(current.item.callsign, current.isFavorite)
                                    val msg = if (current.isFavorite) "Removed from favorites" else "Added to favorites"
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            modifier = Modifier.scale(starScale)
                        ) {
                            Icon(
                                imageVector = if (current.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (current.isFavorite) OrangeAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Callsign: ${current.item.callsign}")
                                    putExtra(Intent.EXTRA_TEXT, buildShareText(current.item))
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share callsign"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share callsign"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val current = entry
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Callsign $callsign was not found in the local directory.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return@Scaffold
        }

        val item = current.item
        val status = evaluateExpiryStatus(item.expiryDateIso)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.callsign,
                            style = CallsignMonoStyle.copy(fontSize = MaterialTheme.typography.displaySmall.fontSize),
                            color = OrangeAccent
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CallsignClassBadge(prefixGroup = item.prefixGroup)
                            ExpiryStatusBadge(status = status)
                        }
                    }

                    Text(
                        text = item.holderName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val statusDescription = when (status) {
                        ExpiryStatus.ACTIVE -> "Assignment is currently active."
                        ExpiryStatus.EXPIRED -> "Assignment has expired."
                        ExpiryStatus.EXPIRED_OVER_5_YEARS -> "Assignment expired more than 5 years ago."
                        ExpiryStatus.UNKNOWN -> "Expiry date unavailable."
                    }
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(
                        icon = Icons.Outlined.Person,
                        label = "Assignment Holder",
                        value = item.holderName
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(
                        icon = Icons.Outlined.Badge,
                        label = "Apparatus Assignment No.",
                        value = item.assignmentNo.ifBlank { "N/A" }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Expiry Date",
                        value = item.expiryDateDisplay.ifBlank { "Unknown" }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    DetailRow(
                        icon = Icons.Outlined.Radio,
                        label = "Prefix Class",
                        value = formatPrefixClassLabel(item.prefixGroup)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Callsign", item.callsign))
                        scope.launch {
                            snackbarHostState.showSnackbar("Copied ${item.callsign}")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Copy", modifier = Modifier.padding(start = 4.dp))
                }

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Callsign: ${item.callsign}")
                            putExtra(Intent.EXTRA_TEXT, buildShareText(item))
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share callsign"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Share", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
