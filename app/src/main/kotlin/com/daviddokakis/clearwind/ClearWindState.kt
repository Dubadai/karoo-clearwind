package com.daviddokakis.clearwind

import kotlin.math.roundToInt

/** Everything the field shows. Rendering only reads this. */
data class ClearWindState(
    /** Wind speed at 10 m, metres per second. Null until the first weather fetch works. */
    val windSpeedMs: Double? = null,
    /** Where the wind comes FROM, degrees from north (weather convention). */
    val windFromDeg: Double? = null,
    /** Direction of travel, degrees from north. Last known value, kept while standing still. */
    val headingDeg: Double? = null,
    /** Show km/h instead of m/s. */
    val kmh: Boolean = false,
    /** How the arrow is coloured. Tap the field to cycle. */
    val arrowColour: ArrowColour = ArrowColour.SPEED,
) {
    enum class ArrowColour {
        /** Smooth green → red by wind speed. */
        SPEED,
        /** Black in light mode, white in dark mode. */
        MONO,
        /** The Karoo's own icon green. */
        GREEN;

        fun next(): ArrowColour = entries[(ordinal + 1) % entries.size]
    }

    /**
     * Where the arrow points, relative to the rider: the way the wind blows TO.
     * 0 = straight ahead (pure tailwind), ±180 = straight at you (pure headwind),
     * positive = to the right. Null when wind or heading is unknown.
     */
    val arrowDeg: Double?
        get() {
            val from = windFromDeg ?: return null
            val h = headingDeg ?: return null
            return normalize(from + 180.0 - h)
        }

    /** Speed as shown: m/s with one decimal under 10, whole numbers above; km/h whole. "–" if unknown. */
    val speedText: String
        get() {
            val ms = windSpeedMs ?: return "–"
            if (kmh) return (ms * 3.6).roundToInt().toString()
            return if (ms < 9.95) String.format(java.util.Locale.US, "%.1f", ms) else ms.roundToInt().toString()
        }

    companion object {
        /** Any angle to -180..180. */
        fun normalize(deg: Double): Double {
            var t = deg % 360.0
            if (t > 180) t -= 360.0
            if (t <= -180) t += 360.0
            return t
        }
    }
}
