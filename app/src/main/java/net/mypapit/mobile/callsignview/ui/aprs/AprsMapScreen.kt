package net.mypapit.mobile.callsignview.ui.aprs

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationDisabled
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.mypapit.mobile.callsignview.data.model.AprsStation
import net.mypapit.mobile.callsignview.ui.components.formatTimestamp
import net.mypapit.mobile.callsignview.ui.theme.CallsignMonoStyle
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent
import net.mypapit.mobile.callsignview.ui.theme.RadioCyan
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val APRS_SOURCE_ID = "aprs-stations-source"
private const val APRS_CIRCLE_LAYER_ID = "aprs-stations-circles"
private const val APRS_LABEL_LAYER_ID = "aprs-stations-labels"
private const val OPENFREEMAP_DARK_STYLE = "https://tiles.openfreemap.org/styles/dark"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprsMapScreen(
    viewModel: AprsMapViewModel,
    onViewCallsignDetail: (String) -> Unit,
    onDisableAprsInSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPrivacyConsentDialog by remember { mutableStateOf(false) }
    var showPrivacyInfoDialog by remember { mutableStateOf(false) }

    val gles3Supported = remember(context) { supportsOpenGlEs3(context) }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyle by remember { mutableStateOf<Style?>(null) }

    val mapView = remember(context, gles3Supported) {
        if (gles3Supported) {
            MapLibre.getInstance(context.applicationContext)
            MapView(context).apply {
                onCreate(null)
            }
        } else {
            null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.setLocationEnabled(true)
        } else {
            viewModel.setLocationEnabled(false)
        }
    }

    if (mapView != null) {
        DisposableEffect(lifecycleOwner, mapView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapView.onPause()
                mapView.onStop()
                mapView.onDestroy()
            }
        }
    }

    // Push updated GeoJSON whenever feed refreshes or style finishes loading
    LaunchedEffect(state.rawGeoJson, loadedStyle) {
        val style = loadedStyle ?: return@LaunchedEffect
        updateAprsGeoJsonLayer(style, state.rawGeoJson)
    }

    // Sync local LocationComponent state strictly with explicit user toggle + permission
    LaunchedEffect(state.userPrefs.aprsLocationEnabled, mapLibreMap, loadedStyle) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val style = loadedStyle ?: return@LaunchedEffect
        applyLocationComponentState(
            context = context,
            map = map,
            style = style,
            enabled = state.userPrefs.aprsLocationEnabled
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MY APRS Live",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitle = if (state.lastUpdatedEpochMs > 0L) {
                            "${state.stations.size} stations • ${formatTimestamp(state.lastUpdatedEpochMs)}"
                        } else {
                            "Optional Malaysian APRS viewer (30m refresh)"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { showPrivacyInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "APRS privacy details"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refresh(force = true) },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh APRS stations"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (gles3Supported) {
                FloatingActionButton(
                    onClick = {
                        if (state.userPrefs.aprsLocationEnabled) {
                            viewModel.setLocationEnabled(false)
                        } else if (!state.userPrefs.aprsLocationNoticeAccepted || !hasLocationPermission(context)) {
                            showPrivacyConsentDialog = true
                        } else {
                            viewModel.setLocationEnabled(true)
                        }
                    },
                    containerColor = if (state.userPrefs.aprsLocationEnabled) {
                        RadioCyan
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (state.userPrefs.aprsLocationEnabled) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        OrangeAccent
                    }
                ) {
                    Icon(
                        imageVector = if (state.userPrefs.aprsLocationEnabled) {
                            Icons.Outlined.MyLocation
                        } else {
                            Icons.Outlined.LocationDisabled
                        },
                        contentDescription = if (state.userPrefs.aprsLocationEnabled) {
                            "Disable map location"
                        } else {
                            "Enable local map location"
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (!gles3Supported || mapView == null) {
            AprsStationListFallback(
                state = state,
                onSelectStation = { viewModel.selectStationByCallsign(it.callsign) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AndroidView(
                    factory = {
                        mapView.apply {
                            getMapAsync { map ->
                                mapLibreMap = map
                                map.uiSettings.isAttributionEnabled = true
                                map.uiSettings.isLogoEnabled = false
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(3.85, 108.2))
                                    .zoom(4.6)
                                    .build()

                                map.setStyle(Style.Builder().fromUri(OPENFREEMAP_DARK_STYLE)) { style ->
                                    loadedStyle = style
                                    updateAprsGeoJsonLayer(style, state.rawGeoJson)
                                }

                                map.addOnMapClickListener { point ->
                                    val screenPoint = map.projection.toScreenLocation(point)
                                    val features = map.queryRenderedFeatures(screenPoint, APRS_CIRCLE_LAYER_ID)
                                    val callsign = features.firstOrNull()?.getStringProperty("callsign")
                                    if (callsign != null) {
                                        viewModel.selectStationByCallsign(callsign)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = OrangeAccent
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegionChip("All Malaysia") {
                            mapLibreMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(3.85, 108.2), 4.6)
                            )
                        }
                        RegionChip("Peninsular") {
                            mapLibreMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(3.8, 101.9), 6.7)
                            )
                        }
                        RegionChip("Sabah / Sarawak") {
                            mapLibreMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(3.4, 114.8), 5.9)
                            )
                        }
                    }

                    state.errorMessage?.let { err ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
                            ),
                            border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Feed notice: $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Status pill bottom-left
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = if (state.userPrefs.aprsLocationEnabled) {
                            "Location: On-device only"
                        } else {
                            "Location: Off"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.userPrefs.aprsLocationEnabled) RadioCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showPrivacyConsentDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyConsentDialog = false },
            title = { Text("Optional Location Detection") },
            text = {
                Text(
                    "The Malaysian APRS viewer is completely optional. Enabling location only shows your current position locally on the map so you can orient yourself near nearby stations.\n\n" +
                        "• Your location is NEVER sent to any server.\n" +
                        "• You can turn location detection off anytime by tapping the location button or in Settings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivacyConsentDialog = false
                        if (hasLocationPermission(context)) {
                            viewModel.setLocationEnabled(true)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Enable Locally")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyConsentDialog = false }) {
                    Text("Keep Off")
                }
            }
        )
    }

    if (showPrivacyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyInfoDialog = false },
            title = { Text("APRS Viewer & Privacy") },
            text = {
                Text(
                    "• This APRS map is completely optional and refreshes live GeoJSON stations from the application server every 30 minutes.\n" +
                        "• Location detection is off by default and never transmitted anywhere.\n" +
                        "• You can disable location detection or hide the APRS Map tab anytime in Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyInfoDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPrivacyInfoDialog = false
                        onDisableAprsInSettings()
                    }
                ) {
                    Text("Disable APRS")
                }
            }
        )
    }

    state.selectedStation?.let { station ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectStationByCallsign(null) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            AprsStationSheetContent(
                station = station,
                onLookupCallsign = {
                    val base = station.baseCallsign
                    viewModel.selectStationByCallsign(null)
                    onViewCallsignDetail(base)
                }
            )
        }
    }
}

@Composable
private fun RegionChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AprsStationSheetContent(
    station: AprsStation,
    onLookupCallsign: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = station.callsign,
            style = CallsignMonoStyle.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize),
            color = OrangeAccent
        )
        station.objectName?.let { obj ->
            Text(
                text = "Object: $obj",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Last packet: ${formatAprsIsoTimestamp(station.receivedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = RadioCyan
        )
        Text(
            text = String.format(Locale.ROOT, "Coordinates: %.5f, %.5f", station.latitude, station.longitude),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (station.comment.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = station.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        OutlinedButton(
            onClick = onLookupCallsign,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Look up ${station.baseCallsign} in Directory")
        }
    }
}

private fun updateAprsGeoJsonLayer(style: Style, rawGeoJson: String) {
    val existingSource = style.getSourceAs<GeoJsonSource>(APRS_SOURCE_ID)
    if (existingSource != null) {
        existingSource.setGeoJson(rawGeoJson)
        return
    }

    style.addSource(GeoJsonSource(APRS_SOURCE_ID, rawGeoJson))

    val circleLayer = CircleLayer(APRS_CIRCLE_LAYER_ID, APRS_SOURCE_ID).withProperties(
        circleRadius(
            Expression.interpolate(
                Expression.linear(),
                Expression.zoom(),
                Expression.stop(4, 5.5f),
                Expression.stop(10, 9.0f),
                Expression.stop(15, 13.0f)
            )
        ),
        circleColor(AndroidColor.parseColor("#FF6D00")),
        circleStrokeWidth(2f),
        circleStrokeColor(AndroidColor.parseColor("#121316"))
    )
    style.addLayer(circleLayer)

    val labelLayer = SymbolLayer(APRS_LABEL_LAYER_ID, APRS_SOURCE_ID).withProperties(
        textField(Expression.get("callsign")),
        textSize(11f),
        textOffset(arrayOf(0f, 1.35f)),
        textColor(AndroidColor.parseColor("#F2F4F7")),
        textHaloColor(AndroidColor.parseColor("#121316")),
        textHaloWidth(1.5f)
    ).apply {
        minZoom = 8.0f
    }
    style.addLayer(labelLayer)
}

@SuppressLint("MissingPermission")
private fun applyLocationComponentState(
    context: Context,
    map: MapLibreMap,
    style: Style,
    enabled: Boolean
) {
    val locationComponent = map.locationComponent
    if (enabled && hasLocationPermission(context)) {
        if (!locationComponent.isLocationComponentActivated) {
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, style)
                    .useDefaultLocationEngine(true)
                    .build()
            )
        }
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.COMPASS
    } else if (locationComponent.isLocationComponentActivated) {
        locationComponent.isLocationComponentEnabled = false
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

private fun supportsOpenGlEs3(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val reqGlEsVersion = activityManager?.deviceConfigurationInfo?.reqGlEsVersion ?: 0
    return reqGlEsVersion >= 0x30000
}

@Composable
private fun AprsStationListFallback(
    state: AprsMapUiState,
    onSelectStation: (AprsStation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = OrangeAccent
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f))
        ) {
            Text(
                text = "Interactive map requires OpenGL ES 3.0 — showing live Malaysian APRS station list",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.stations, key = { "${it.callsign}_${it.receivedAt}" }) { station ->
                Card(
                    onClick = { onSelectStation(station) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = station.callsign,
                                style = CallsignMonoStyle,
                                color = OrangeAccent
                            )
                            Text(
                                text = formatAprsIsoTimestamp(station.receivedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = RadioCyan
                            )
                        }
                        Text(
                            text = String.format(Locale.ROOT, "%.4f, %.4f", station.latitude, station.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (station.comment.isNotBlank()) {
                            Text(
                                text = station.comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAprsIsoTimestamp(iso: String): String {
    if (iso.isBlank()) return "Unknown"
    return runCatching {
        val instant = Instant.parse(iso)
        val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        instant.atZone(ZoneId.systemDefault()).format(fmt)
    }.getOrDefault(iso)
}
