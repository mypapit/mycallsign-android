package net.mypapit.mobile.callsignview.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.RestartAlt
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.mypapit.mobile.callsignview.BuildConfig
import net.mypapit.mobile.callsignview.data.local.CallsignStats
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import net.mypapit.mobile.callsignview.data.prefs.DbSource
import net.mypapit.mobile.callsignview.data.prefs.UserPreferences
import net.mypapit.mobile.callsignview.data.repository.CallsignRepository
import net.mypapit.mobile.callsignview.data.repository.SyncCooldownPolicy
import net.mypapit.mobile.callsignview.data.repository.SyncResult
import net.mypapit.mobile.callsignview.ui.components.formatDurationBrief
import net.mypapit.mobile.callsignview.ui.components.formatTimestamp
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: CallsignRepository,
    preferences: AppPreferences,
    onOpenAbout: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onBack: () -> Unit
) {
    val prefs by preferences.preferencesFlow.collectAsStateWithLifecycle(initialValue = UserPreferences())
    val stats by repository.observeStats().collectAsStateWithLifecycle(initialValue = CallsignStats())
    val isSyncing by repository.isSyncing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val remainingCooldown = SyncCooldownPolicy.remainingCooldownMs(
        lastSyncEpochMs = prefs.lastDbSyncEpochMs,
        lastSource = prefs.lastDbSource,
        nowEpochMs = System.currentTimeMillis()
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = OrangeAccent)
                        Text(
                            text = "Callsign Database",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "Records stored: ${stats.total}\n" +
                            "Source: ${when (prefs.lastDbSource) {
                                DbSource.ONLINE -> "Live (application server)"
                                DbSource.FALLBACK -> "Offline snapshot"
                                DbSource.NONE -> "Not initialized"
                            }}\n" +
                            "Last synced: ${formatTimestamp(prefs.lastDbSyncEpochMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Updating about once a week is recommended. A 12-hour cooldown prevents server abuse.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                when (val result = repository.syncDatabase()) {
                                    is SyncResult.Updated ->
                                        snackbarHostState.showSnackbar("Updated ${result.count} callsigns")
                                    SyncResult.UpToDate ->
                                        snackbarHostState.showSnackbar("Database is already up to date")
                                    is SyncResult.CooldownActive ->
                                        snackbarHostState.showSnackbar("Cooldown active (${formatDurationBrief(result.remainingMs)} remaining)")
                                    is SyncResult.LoadedFallback ->
                                        snackbarHostState.showSnackbar("Loaded ${result.count} fallback records")
                                    is SyncResult.Error ->
                                        snackbarHostState.showSnackbar("Sync failed: ${result.message}")
                                }
                            }
                        },
                        enabled = !isSyncing && remainingCooldown == 0L,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            when {
                                isSyncing -> "Updating..."
                                remainingCooldown > 0L -> "Update available in ${formatDurationBrief(remainingCooldown)}"
                                else -> "Update Database Now"
                            }
                        )
                    }

                    if (BuildConfig.DEBUG && remainingCooldown > 0L) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    preferences.resetSyncCooldownForDebug()
                                    snackbarHostState.showSnackbar("Debug cooldown reset")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset Cooldown (Debug)")
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Map, contentDescription = null, tint = OrangeAccent)
                        Text(
                            text = "Optional Malaysian APRS Map",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show APRS Map Tab",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Completely optional live MapLibre station viewer (refreshes every 30 min).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.aprsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch { preferences.setAprsEnabled(enabled) }
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Map Location Detection",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Local on-device display only. Location is never transmitted anywhere.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.aprsLocationEnabled,
                            enabled = prefs.aprsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    preferences.setAprsLocationEnabled(
                                        enabled = enabled,
                                        noticeAccepted = prefs.aprsLocationNoticeAccepted
                                    )
                                }
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onReplayOnboarding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                Text(" View Onboarding Guide", modifier = Modifier.padding(start = 6.dp))
            }

            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null)
                Text(" About & Credits", modifier = Modifier.padding(start = 6.dp))
            }
        }
        }
    }
}
