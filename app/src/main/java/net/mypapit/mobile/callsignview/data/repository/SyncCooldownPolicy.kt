package net.mypapit.mobile.callsignview.data.repository

import net.mypapit.mobile.callsignview.data.prefs.DbSource

object SyncCooldownPolicy {
    const val COOLDOWN_MS: Long = 12L * 60L * 60L * 1000L // 12 hours minimum
    const val WEEKLY_RECOMMENDATION_MS: Long = 7L * 24L * 60L * 60L * 1000L // 7 days

    fun remainingCooldownMs(
        lastSyncEpochMs: Long,
        lastSource: DbSource,
        nowEpochMs: Long
    ): Long {
        if (lastSource != DbSource.ONLINE || lastSyncEpochMs <= 0L) return 0L
        val elapsed = (nowEpochMs - lastSyncEpochMs).coerceAtLeast(0L)
        return (COOLDOWN_MS - elapsed).coerceAtLeast(0L)
    }

    fun canSync(
        lastSyncEpochMs: Long,
        lastSource: DbSource,
        nowEpochMs: Long,
        bypassCooldown: Boolean = false
    ): Boolean {
        if (bypassCooldown) return true
        return remainingCooldownMs(lastSyncEpochMs, lastSource, nowEpochMs) == 0L
    }

    fun isWeeklyUpdateRecommended(
        lastSyncEpochMs: Long,
        lastSource: DbSource,
        nowEpochMs: Long
    ): Boolean {
        if (lastSource == DbSource.FALLBACK) return true
        if (lastSource == DbSource.NONE || lastSyncEpochMs <= 0L) return false
        return (nowEpochMs - lastSyncEpochMs) >= WEEKLY_RECOMMENDATION_MS
    }
}
