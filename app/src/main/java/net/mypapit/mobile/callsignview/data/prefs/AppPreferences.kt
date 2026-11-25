package net.mypapit.mobile.callsignview.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mycallsign_prefs")

enum class DbSource {
    NONE,
    ONLINE,
    FALLBACK
}

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val aprsEnabled: Boolean = false,
    val aprsLocationEnabled: Boolean = false,
    val aprsLocationNoticeAccepted: Boolean = false,
    val lastDbSyncEpochMs: Long = 0L,
    val lastDbSource: DbSource = DbSource.NONE,
    val csvEtag: String? = null,
    val csvLastModified: String? = null,
    val lastAprsSyncEpochMs: Long = 0L
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val APRS_ENABLED = booleanPreferencesKey("aprs_enabled")
        val APRS_LOCATION_ENABLED = booleanPreferencesKey("aprs_location_enabled")
        val APRS_LOCATION_NOTICE_ACCEPTED = booleanPreferencesKey("aprs_location_notice_accepted")
        val LAST_DB_SYNC_EPOCH_MS = longPreferencesKey("last_db_sync_epoch_ms")
        val LAST_DB_SOURCE = stringPreferencesKey("last_db_source")
        val CSV_ETAG = stringPreferencesKey("csv_etag")
        val CSV_LAST_MODIFIED = stringPreferencesKey("csv_last_modified")
        val LAST_APRS_SYNC_EPOCH_MS = longPreferencesKey("last_aprs_sync_epoch_ms")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val sourceName = prefs[Keys.LAST_DB_SOURCE] ?: DbSource.NONE.name
        val source = runCatching { DbSource.valueOf(sourceName) }.getOrDefault(DbSource.NONE)
        UserPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            aprsEnabled = prefs[Keys.APRS_ENABLED] ?: false,
            aprsLocationEnabled = prefs[Keys.APRS_LOCATION_ENABLED] ?: false,
            aprsLocationNoticeAccepted = prefs[Keys.APRS_LOCATION_NOTICE_ACCEPTED] ?: false,
            lastDbSyncEpochMs = prefs[Keys.LAST_DB_SYNC_EPOCH_MS] ?: 0L,
            lastDbSource = source,
            csvEtag = prefs[Keys.CSV_ETAG],
            csvLastModified = prefs[Keys.CSV_LAST_MODIFIED],
            lastAprsSyncEpochMs = prefs[Keys.LAST_APRS_SYNC_EPOCH_MS] ?: 0L
        )
    }

    suspend fun current(): UserPreferences = preferencesFlow.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setAprsEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[Keys.APRS_ENABLED] = enabled
            if (!enabled) {
                it[Keys.APRS_LOCATION_ENABLED] = false
            }
        }
    }

    suspend fun setAprsLocationEnabled(enabled: Boolean, noticeAccepted: Boolean = true) {
        context.dataStore.edit {
            it[Keys.APRS_LOCATION_ENABLED] = enabled
            if (noticeAccepted) {
                it[Keys.APRS_LOCATION_NOTICE_ACCEPTED] = true
            }
        }
    }

    suspend fun recordDbSync(
        source: DbSource,
        syncEpochMs: Long,
        etag: String?,
        lastModified: String?
    ) {
        context.dataStore.edit {
            it[Keys.LAST_DB_SOURCE] = source.name
            it[Keys.LAST_DB_SYNC_EPOCH_MS] = syncEpochMs
            if (etag != null) it[Keys.CSV_ETAG] = etag else it.remove(Keys.CSV_ETAG)
            if (lastModified != null) it[Keys.CSV_LAST_MODIFIED] = lastModified else it.remove(Keys.CSV_LAST_MODIFIED)
        }
    }

    suspend fun recordAprsSync(syncEpochMs: Long) {
        context.dataStore.edit {
            it[Keys.LAST_APRS_SYNC_EPOCH_MS] = syncEpochMs
        }
    }

    suspend fun resetSyncCooldownForDebug() {
        context.dataStore.edit {
            it[Keys.LAST_DB_SYNC_EPOCH_MS] = 0L
        }
    }
}
