package net.mypapit.mobile.callsignview

import net.mypapit.mobile.callsignview.data.local.SearchQueryHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryHelperTest {

    @Test
    fun nameSearchRequiresAtLeastThreeCharacters() {
        assertFalse(SearchQueryHelper.isNameQueryTooShort(""))
        assertTrue(SearchQueryHelper.isNameQueryTooShort("a"))
        assertTrue(SearchQueryHelper.isNameQueryTooShort("ab"))
        assertTrue(SearchQueryHelper.isNameQueryTooShort("  ab "))
        assertFalse(SearchQueryHelper.isNameQueryTooShort("ali"))
    }

    @Test
    fun translatesWildcardsAndCallsignPrefixes() {
        assertEquals("%AHMAD%ALI%", SearchQueryHelper.toNameWildcardSqlPattern("AHMAD*ALI"))
        assertEquals("9M2%", SearchQueryHelper.toCallsignSqlPattern("9m2"))
        assertEquals("%VOT", "%VOT")
        assertEquals("%VOT%", SearchQueryHelper.toCallsignSqlPattern("vot"))
    }
}
