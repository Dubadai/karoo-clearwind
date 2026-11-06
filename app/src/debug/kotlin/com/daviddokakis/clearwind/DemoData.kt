package com.daviddokakis.clearwind

import android.os.SystemClock

/**
 * DEBUG BUILDS ONLY (src/debug): made-up wind, always on, so the field can be judged indoors
 * without a connection. Real wind and heading are ignored in debug builds. Release builds use
 * the pass-through in src/release, so none of this ships.
 *
 * Wind from the south-west; the "rider" turns 15° every 2 s, so the arrow walks all the way
 * round through tail, cross and head. The speed changes every 6 s and includes one value over
 * 10 m/s to show the whole-number format. The unit switch (tap / remote) still works.
 */
object DemoData {
    private val startedAt = SystemClock.elapsedRealtime()
    private val SPEEDS_MS = doubleArrayOf(5.2, 3.4, 7.8, 11.6, 2.1, 6.0)

    fun apply(s: ClearWindState): ClearWindState {
        val ms = SystemClock.elapsedRealtime() - startedAt
        val heading = (ms / 2_000L * 15 % 360).toDouble()
        val speed = SPEEDS_MS[((ms / 6_000L) % SPEEDS_MS.size).toInt()]
        return s.copy(windSpeedMs = speed, windFromDeg = 225.0, headingDeg = heading)
    }
}
