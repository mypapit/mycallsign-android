package net.mypapit.mobile.callsignview

import net.mypapit.mobile.callsignview.ui.components.WindowLayoutType
import net.mypapit.mobile.callsignview.ui.components.classifyWindowWidth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutTest {

    @Test
    fun classifiesPhoneMediumAndTabletWidths() {
        val phoneSmall = classifyWindowWidth(360)
        val phoneLarge = classifyWindowWidth(412)
        assertEquals(WindowLayoutType.COMPACT, phoneSmall)
        assertEquals(WindowLayoutType.COMPACT, phoneLarge)
        assertFalse(phoneSmall.useNavigationRail)
        assertFalse(phoneSmall.isTwoPane)

        val foldable = classifyWindowWidth(600)
        val medium = classifyWindowWidth(700)
        assertEquals(WindowLayoutType.MEDIUM, foldable)
        assertEquals(WindowLayoutType.MEDIUM, medium)
        assertTrue(foldable.useNavigationRail)
        assertFalse(foldable.isTwoPane)

        val tabletMin = classifyWindowWidth(720)
        val tabletPortrait = classifyWindowWidth(800)
        val tabletLandscape = classifyWindowWidth(1280)
        assertEquals(WindowLayoutType.EXPANDED, tabletMin)
        assertEquals(WindowLayoutType.EXPANDED, tabletPortrait)
        assertEquals(WindowLayoutType.EXPANDED, tabletLandscape)
        assertTrue(tabletMin.useNavigationRail)
        assertTrue(tabletMin.isTwoPane)
    }
}
