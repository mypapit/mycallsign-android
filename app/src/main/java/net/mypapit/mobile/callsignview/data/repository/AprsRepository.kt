package net.mypapit.mobile.callsignview.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.mypapit.mobile.callsignview.BuildConfig
import net.mypapit.mobile.callsignview.data.model.AprsFeatureCollection
import net.mypapit.mobile.callsignview.data.model.AprsStation
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class AprsLoadResult(
    val rawGeoJson: String,
    val stations: List<AprsStation>,
    val lastUpdatedEpochMs: Long,
    val isFromCache: Boolean,
    val errorMessage: String? = null
)

class AprsRepository(
    private val context: Context,
    private val preferences: AppPreferences,
    private val httpClient: OkHttpClient
) {
    companion object {
        const val STATIONS_URL = "https://api.mypapit.net/repeater/stations.json"
        const val REFRESH_INTERVAL_MS = 30L * 60L * 1000L // 30 minutes
        private const val CACHE_FILE_NAME = "aprs_stations.geojson"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cacheFile: File
        get() = File(context.filesDir, CACHE_FILE_NAME)

    suspend fun loadStations(forceRefresh: Boolean = false): AprsLoadResult =
        withContext(Dispatchers.IO) {
            val prefs = preferences.current()
            val now = System.currentTimeMillis()
            val hasCache = cacheFile.exists() && cacheFile.length() > 0L
            val isStale = !hasCache || (now - prefs.lastAprsSyncEpochMs) >= REFRESH_INTERVAL_MS

            if (!forceRefresh && hasCache && !isStale) {
                val cachedRaw = cacheFile.readText()
                val stations = parseStations(cachedRaw)
                if (stations.isNotEmpty()) {
                    return@withContext AprsLoadResult(
                        rawGeoJson = cachedRaw,
                        stations = stations,
                        lastUpdatedEpochMs = prefs.lastAprsSyncEpochMs,
                        isFromCache = true
                    )
                }
            }

            try {
                val request = Request.Builder()
                    .url(STATIONS_URL)
                    .header("User-Agent", "MYCallsign/${BuildConfig.VERSION_NAME}")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("HTTP ${response.code}")
                    }
                    val raw = response.body.string()
                    val stations = parseStations(raw)
                    if (raw.isBlank() || stations.isEmpty()) {
                        throw IllegalStateException("No APRS stations in feed")
                    }
                    cacheFile.writeText(raw)
                    val updatedAt = System.currentTimeMillis()
                    preferences.recordAprsSync(updatedAt)
                    AprsLoadResult(
                        rawGeoJson = raw,
                        stations = stations,
                        lastUpdatedEpochMs = updatedAt,
                        isFromCache = false
                    )
                }
            } catch (e: Exception) {
                if (hasCache) {
                    val cachedRaw = cacheFile.readText()
                    AprsLoadResult(
                        rawGeoJson = cachedRaw,
                        stations = parseStations(cachedRaw),
                        lastUpdatedEpochMs = prefs.lastAprsSyncEpochMs,
                        isFromCache = true,
                        errorMessage = e.message ?: "Unable to refresh APRS feed"
                    )
                } else {
                    AprsLoadResult(
                        rawGeoJson = """{"type":"FeatureCollection","features":[]}""",
                        stations = emptyList(),
                        lastUpdatedEpochMs = 0L,
                        isFromCache = false,
                        errorMessage = e.message ?: "Unable to download APRS stations"
                    )
                }
            }
        }

    internal fun parseStations(rawGeoJson: String): List<AprsStation> {
        return runCatching {
            val collection = json.decodeFromString(AprsFeatureCollection.serializer(), rawGeoJson)
            collection.features.mapNotNull { feature ->
                val props = feature.properties ?: return@mapNotNull null
                val coords = feature.geometry?.coordinates
                val lon = coords?.getOrNull(0) ?: props.longitude
                val lat = coords?.getOrNull(1) ?: props.latitude
                if (props.callsign.isBlank()) return@mapNotNull null
                AprsStation(
                    callsign = props.callsign,
                    baseCallsign = props.callsign.substringBefore("-").trim().uppercase(),
                    objectName = props.objectName?.takeIf { it.isNotBlank() },
                    latitude = lat,
                    longitude = lon,
                    receivedAt = props.receivedAt,
                    comment = props.comment.trim(),
                    symbol = props.symbol,
                    symbolTable = props.symbolTable
                )
            }
        }.getOrDefault(emptyList())
    }
}
