package net.mypapit.mobile.callsignview.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mypapit.mobile.callsignview.BuildConfig
import net.mypapit.mobile.callsignview.data.csv.CallsignCsvParser
import net.mypapit.mobile.callsignview.data.local.CallsignDao
import net.mypapit.mobile.callsignview.data.local.CallsignStats
import net.mypapit.mobile.callsignview.data.local.CallsignWithFavorite
import net.mypapit.mobile.callsignview.data.local.FavoriteEntity
import net.mypapit.mobile.callsignview.data.local.SearchMode
import net.mypapit.mobile.callsignview.data.local.SearchQueryHelper
import net.mypapit.mobile.callsignview.data.local.StatusFilter
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import net.mypapit.mobile.callsignview.data.prefs.DbSource
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface SyncResult {
    data class Updated(val count: Int) : SyncResult
    data object UpToDate : SyncResult
    data class CooldownActive(val remainingMs: Long) : SyncResult
    data class LoadedFallback(val count: Int, val onlineError: String?) : SyncResult
    data class Error(val message: String) : SyncResult
}

class CallsignRepository(
    private val context: Context,
    private val dao: CallsignDao,
    private val preferences: AppPreferences,
    private val httpClient: OkHttpClient
) {
    companion object {
        const val CANONICAL_CSV_URL = "https://api.mypapit.net/repeater/callsign.csv"
        private const val FALLBACK_ASSET_NAME = "callsign_fallback.csv"
    }

    private val syncMutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun observeCallsigns(
        mode: SearchMode,
        rawQuery: String,
        statusFilter: StatusFilter,
        prefixFilter: String
    ): Flow<List<CallsignWithFavorite>> {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val query = SearchQueryHelper.buildSearchQuery(
            mode = mode,
            rawQuery = rawQuery,
            statusFilter = statusFilter,
            prefixFilter = prefixFilter,
            todayIso = todayIso
        )
        return dao.observeFiltered(query)
    }

    fun observeByCallsign(callsign: String): Flow<CallsignWithFavorite?> =
        dao.observeByCallsign(callsign)

    fun observeFavorites(): Flow<List<CallsignWithFavorite>> =
        dao.observeFavorites()

    fun observeExpiredOver5Years(): Flow<List<CallsignWithFavorite>> {
        val fiveYearsAgoIso = LocalDate.now().minusYears(5).format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dao.observeExpiredOver5Years(fiveYearsAgoIso)
    }

    fun observeStats(): Flow<CallsignStats> {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        return dao.observeStats(
            todayIso = today.format(fmt),
            in90DaysIso = today.plusDays(90).format(fmt),
            fiveYearsAgoIso = today.minusYears(5).format(fmt)
        )
    }

    suspend fun toggleFavorite(callsign: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.removeFavorite(callsign)
        } else {
            dao.addFavorite(FavoriteEntity(callsign = callsign, addedAtEpochMs = System.currentTimeMillis()))
        }
    }

    suspend fun setFavorite(callsign: String, favorite: Boolean) {
        if (favorite) {
            dao.addFavorite(FavoriteEntity(callsign = callsign, addedAtEpochMs = System.currentTimeMillis()))
        } else {
            dao.removeFavorite(callsign)
        }
    }

    /**
     * Preloads the callsign database from the canonical URL on first launch.
     * Falls back to the bundled CSV asset only if the initial network download fails.
     */
    suspend fun preloadOnFirstLaunchIfEmpty(): SyncResult? = syncMutex.withLock {
        if (dao.count() > 0) return null
        _isSyncing.value = true
        try {
            val onlineResult = fetchAndImportFromNetwork(sendConditionalHeaders = false)
            if (onlineResult is SyncResult.Updated) {
                return onlineResult
            }
            val onlineError = (onlineResult as? SyncResult.Error)?.message
            return loadFallbackAsset(onlineError)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun syncDatabase(bypassCooldownForDebug: Boolean = false): SyncResult = syncMutex.withLock {
        val prefs = preferences.current()
        val now = System.currentTimeMillis()
        val allowBypass = BuildConfig.DEBUG && bypassCooldownForDebug
        val remaining = SyncCooldownPolicy.remainingCooldownMs(
            lastSyncEpochMs = prefs.lastDbSyncEpochMs,
            lastSource = prefs.lastDbSource,
            nowEpochMs = now
        )
        if (!allowBypass && remaining > 0L) {
            return SyncResult.CooldownActive(remaining)
        }

        _isSyncing.value = true
        try {
            return fetchAndImportFromNetwork(sendConditionalHeaders = prefs.lastDbSource == DbSource.ONLINE && dao.count() > 0)
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun fetchAndImportFromNetwork(sendConditionalHeaders: Boolean): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val prefs = preferences.current()
                val requestBuilder = Request.Builder()
                    .url(CANONICAL_CSV_URL)
                    .header("User-Agent", "MYCallsign/${BuildConfig.VERSION_NAME}")

                if (sendConditionalHeaders) {
                    prefs.csvEtag?.let { requestBuilder.header("If-None-Match", it) }
                    prefs.csvLastModified?.let { requestBuilder.header("If-Modified-Since", it) }
                }

                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    val now = System.currentTimeMillis()
                    if (response.code == 304) {
                        preferences.recordDbSync(
                            source = DbSource.ONLINE,
                            syncEpochMs = now,
                            etag = response.header("ETag") ?: prefs.csvEtag,
                            lastModified = response.header("Last-Modified") ?: prefs.csvLastModified
                        )
                        return@withContext SyncResult.UpToDate
                    }
                    if (!response.isSuccessful) {
                        return@withContext SyncResult.Error("Server returned HTTP ${response.code}")
                    }
                    val body = response.body
                    val entities = body.byteStream().use { stream ->
                        InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                            CallsignCsvParser.parse(reader)
                        }
                    }
                    if (entities.isEmpty()) {
                        return@withContext SyncResult.Error("Downloaded CSV contained no valid callsign rows")
                    }
                    dao.replaceAllCallsigns(entities)
                    preferences.recordDbSync(
                        source = DbSource.ONLINE,
                        syncEpochMs = now,
                        etag = response.header("ETag"),
                        lastModified = response.header("Last-Modified")
                    )
                    SyncResult.Updated(entities.size)
                }
            } catch (e: Exception) {
                SyncResult.Error(e.message ?: "Unable to reach application server")
            }
        }

    private suspend fun loadFallbackAsset(onlineError: String?): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val entities = context.assets.open(FALLBACK_ASSET_NAME).use { stream ->
                    InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                        CallsignCsvParser.parse(reader)
                    }
                }
                if (entities.isEmpty()) {
                    return@withContext SyncResult.Error("Fallback CSV snapshot is empty")
                }
                dao.replaceAllCallsigns(entities)
                preferences.recordDbSync(
                    source = DbSource.FALLBACK,
                    syncEpochMs = 0L, // Do not lock out live sync with cooldown
                    etag = null,
                    lastModified = null
                )
                SyncResult.LoadedFallback(entities.size, onlineError)
            } catch (e: Exception) {
                SyncResult.Error(onlineError ?: (e.message ?: "Failed to initialize database"))
            }
        }
}
