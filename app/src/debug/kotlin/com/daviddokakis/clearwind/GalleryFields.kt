package com.daviddokakis.clearwind

import android.content.Context
import android.graphics.Bitmap
import com.daviddokakis.clearwind.ClearWindState.ArrowColour
import com.daviddokakis.clearwind.ClearWindState.WindUnit

/** DEBUG ONLY: what the field gallery (FieldGallery.kt) draws: the wind field in each arrow colour, and its two empty states. */
object GalleryFields {
    /** A ride for the README GIF (seq-00…): the road turns a full circle, so the wind swings from tail to head and back; m/s and gust. */
    private val SEQ = List(12) { i -> Triple(i * 30.0, listOf(4.6, 5.0, 5.4, 5.8, 6.3, 6.6, 6.2, 5.7, 5.2, 4.9, 4.7, 4.5)[i], listOf(7.8, 8.4, 9.0, 9.6, 10.4, 10.9, 10.2, 9.4, 8.7, 8.2, 7.9, 7.6)[i]) }

    val variants = listOf("speed", "plain", "green", "searching", "noconn", "tile", "nogust") + SEQ.indices.map { "seq-%02d".format(it) }
    /** Every Karoo size. */
    val sizes: List<String>? = null

    fun render(context: Context, variant: String, w: Int, h: Int, night: Boolean): Bitmap {
        val colour = when (variant) { "plain" -> ArrowColour.MONO; "green" -> ArrowColour.GREEN; else -> ArrowColour.SPEED }
        // 5.2 m/s from behind on the left: the arrow points ahead to the right (a tailwind).
        if (variant.startsWith("searching") || variant.startsWith("noconn")) {
            val e = ClearWindState(fetchFailed = variant == "noconn", unit = WindUnit.MS)
            return WindRenderer.render(e, w, h, night)
        }
        if (variant.startsWith("seq-")) {
            val (heading, ms, gust) = SEQ[variant.removePrefix("seq-").toInt()]
            return WindRenderer.render(ClearWindState(windSpeedMs = ms, windGustMs = gust, windFromDeg = 225.0, headingDeg = heading, unit = WindUnit.MS, arrowColour = ArrowColour.SPEED), w, h, night)
        }
        val tile = if (variant == "tile") ClearWindState.TileColour.WIND else ClearWindState.TileColour.OFF
        val s = ClearWindState(windSpeedMs = 5.2, windGustMs = 9.0, windFromDeg = 225.0, headingDeg = 0.0, unit = WindUnit.MS, arrowColour = colour, tileColour = tile, showGusts = variant != "nogust")
        return WindRenderer.render(s, w, h, night)
    }
}
