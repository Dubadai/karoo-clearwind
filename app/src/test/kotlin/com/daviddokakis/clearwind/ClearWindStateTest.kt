package com.daviddokakis.clearwind

import com.daviddokakis.clearwind.ClearWindState.WindUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClearWindStateTest {
    private val wind = ClearWindState(windSpeedMs = 5.24, windGustMs = 9.4, windFromDeg = 0.0, headingDeg = 0.0)

    @Test fun `wind from ahead points straight at the rider`() {
        assertEquals(180.0, wind.arrowDeg!!, 0.001)
    }

    @Test fun `wind from behind points straight ahead`() {
        assertEquals(0.0, wind.copy(windFromDeg = 180.0).arrowDeg!!, 0.001)
    }

    @Test fun `wind from the left points to the right`() {
        assertEquals(90.0, wind.copy(windFromDeg = 270.0).arrowDeg!!, 0.001)
    }

    @Test fun `the arrow turns with the rider`() {
        // Wind from the north, riding east: it blows from the left, so the arrow points right
        assertEquals(90.0, wind.copy(headingDeg = 90.0).arrowDeg!!, 0.001)
    }

    @Test fun `no heading or no wind means no arrow`() {
        assertNull(wind.copy(headingDeg = null).arrowDeg)
        assertNull(wind.copy(windFromDeg = null).arrowDeg)
    }

    @Test fun `angles fold into -180 to 180`() {
        assertEquals(-170.0, ClearWindState.normalize(190.0), 0.001)
        assertEquals(170.0, ClearWindState.normalize(-190.0), 0.001)
        assertEquals(180.0, ClearWindState.normalize(540.0), 0.001)
        assertEquals(180.0, ClearWindState.normalize(-180.0), 0.001)
    }

    @Test fun `metres per second keep one decimal under 10`() {
        assertEquals("5.2", wind.speedText)
        assertEquals("10", wind.copy(windSpeedMs = 9.96).speedText)
    }

    @Test fun `other units are whole numbers`() {
        assertEquals("19", wind.copy(unit = WindUnit.KMH).speedText)
        assertEquals("10", wind.copy(unit = WindUnit.KNOTS).speedText)
        assertEquals("12", wind.copy(unit = WindUnit.MPH).speedText)
    }

    @Test fun `unknown speed shows a dash`() {
        assertEquals("–", ClearWindState().speedText)
    }

    @Test fun `the header shows the gust in brackets`() {
        assertEquals("5.2 (9) M/S", wind.headerText)
    }

    @Test fun `a gust no stronger than the wind is left out`() {
        assertNull(wind.copy(windGustMs = 5.0).gustText)
        assertEquals("5.2 M/S", wind.copy(windGustMs = 5.0).headerText)
    }

    @Test fun `gusts can be switched off`() {
        assertNull(wind.copy(showGusts = false).gustText)
    }

    @Test fun `no wind yet says Searching until a fetch fails`() {
        assertEquals("Searching…", ClearWindState().emptyText)
        assertEquals("No connection", ClearWindState(fetchFailed = true).emptyText)
        assertNull(wind.emptyText)
    }

    @Test fun `units cycle back to the start`() {
        assertEquals(WindUnit.MS, WindUnit.KNOTS.next())
        assertEquals(WindUnit.KNOTS, WindUnit.MPH.next())
    }
}
