package com.daviddokakis.clearwind

import android.content.Context
import android.graphics.Bitmap
import com.daviddokakis.clearwind.ClearWindState.ArrowColour
import com.daviddokakis.clearwind.ClearWindState.WindUnit

/** DEBUG ONLY: what the field gallery (FieldGallery.kt) draws: the wind field in each arrow colour. */
object GalleryFields {
    val variants = listOf("speed", "plain", "green")

    fun render(context: Context, variant: String, w: Int, h: Int, night: Boolean): Bitmap {
        val colour = when (variant) { "plain" -> ArrowColour.MONO; "green" -> ArrowColour.GREEN; else -> ArrowColour.SPEED }
        // 5.2 m/s from behind on the left: the arrow points ahead to the right (a tailwind).
        val s = ClearWindState(windSpeedMs = 5.2, windFromDeg = 225.0, headingDeg = 0.0, unit = WindUnit.MS, arrowColour = colour)
        return WindRenderer.render(s, w, h, night)
    }
}
