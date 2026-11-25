package net.mypapit.mobile.callsignview

import net.mypapit.mobile.callsignview.data.prefs.DbSource
import net.mypapit.mobile.callsignview.data.repository.SyncCooldownPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncCooldownPolicyTest {

    @Test
    fun enforcesTwelveHourCooldownForOnlineSource() {
        val t0 = 1_700_000_000_000L
        val sixHoursLater = t0 + 6L * 3_600_000L
        val twelveHoursLater = t0 + 12L * 3_600_000L

        assertFalse(SyncCooldownPolicy.canSync(t0, DbSource.ONLINE, sixHoursLater))
        assertEquals(6L * 3_600_000L, SyncCooldownPolicy.remainingCooldownMs(t0, DbSource.ONLINE, sixHoursLater))

        assertTrue(SyncCooldownPolicy.canSync(t0, DbSource.ONLINE, twelveHoursLater))
        assertEquals(0L, SyncCooldownPolicy.remainingCooldownMs(t0, DbSource.ONLINE, twelveHoursLater))
    }

    @Test
    fun fallbackNeverBlockedByCooldownAndRecommendsWeeklyUpdate() {
        val t0 = 1_700_000_000_000L
        assertTrue(SyncCooldownPolicy.canSync(t0, DbSource.FALLBACK, t0 + 1_000L))
        assertTrue(SyncCooldownPolicy.isWeeklyUpdateRecommended(t0, DbSource.FALLBACK, t0 + 1_000L))

        val eightDaysLater = t0 + 8L * 86_400_000L
        assertFalse(SyncCooldownPolicy.isWeeklyUpdateRecommended(t0, DbSource.ONLINE, t0 + 86_400_000L))
        assertTrue(SyncCooldownPolicy.isWeeklyUpdateRecommended(t0, DbSource.ONLINE, eightDaysLater))
    }
}
