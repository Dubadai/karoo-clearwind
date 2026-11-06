package com.daviddokakis.clearwind

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.max
import kotlin.math.min

/**
 * Draws the whole field into a bitmap (see docs/design/field/README.md): plain white (black at
 * night) field, the Karoo-style wind arrow dead centre and as big as the field allows without
 * touching the header, coloured by wind speed on a smooth green → red scale.
 * A Karoo-style header on top: wind icon left, speed with unit right ("5.2 M/S").
 */
object WindRenderer {

    private class Palette(val bg: Int, val text: Int)

    private val LIGHT = Palette(Color.WHITE, 0xFF0B0B0B.toInt())
    private val DARK = Palette(0xFF0B0B0B.toInt(), Color.WHITE)

    /** Smooth speed scale: m/s → colour. */
    private val STOPS = listOf(
        0.0 to 0xFF2F8A3E.toInt(),
        4.0 to 0xFFC99A00.toInt(),
        8.0 to 0xFFE06A10.toInt(),
        12.0 to 0xFFC62828.toInt(),
    )

    /** No wind data: grey arrow. */
    private const val NO_DATA = 0xFF9A9A9A.toInt()

    /** The Karoo's own icon green (sampled from its data-field icons on the device). */
    private const val KAROO_GREEN = 0xFF129A5E.toInt()

    private fun arrowColor(s: ClearWindState, p: Palette): Int {
        val ms = s.windSpeedMs ?: return NO_DATA
        return when (s.arrowColour) {
            ClearWindState.ArrowColour.SPEED -> speedColor(ms)
            ClearWindState.ArrowColour.MONO -> p.text
            ClearWindState.ArrowColour.GREEN -> KAROO_GREEN
        }
    }

    // Header, measured on the Karoo's own fields: 23 px icons 12 px in from the left, header
    // text ending ~10 px from the right edge, centred on the icon.
    private const val ICON_PX = 23f
    private const val ICON_LEFT = 12f
    private const val ICON_TOP = 12f
    private const val TEXT_RIGHT = 10f

    fun background(night: Boolean): Int = (if (night) DARK else LIGHT).bg

    fun speedColor(ms: Double): Int {
        val v = ms.coerceIn(0.0, STOPS.last().first)
        for (i in 1 until STOPS.size) {
            val (a1, c1) = STOPS[i]
            if (v <= a1) {
                val (a0, c0) = STOPS[i - 1]
                return blend(c0, c1, ((v - a0) / (a1 - a0)).toFloat())
            }
        }
        return STOPS.last().second
    }

    private fun blend(a: Int, b: Int, f: Float): Int {
        fun ch(shift: Int): Int {
            val x = (a shr shift) and 0xFF
            val y = (b shr shift) and 0xFF
            return (x + (y - x) * f + 0.5f).toInt()
        }
        return Color.rgb(ch(16), ch(8), ch(0))
    }

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val header = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // The Karoo's own data-label style (read from its ride app, dataHeaderTextStyle):
        // "ibm-plex-sans-condensed", 17 sp, regular, black (white at night), capitals.
        typeface = Typeface.create("ibm-plex-sans-condensed", Typeface.NORMAL)
        textAlign = Paint.Align.RIGHT
        textSize = 17f * android.content.res.Resources.getSystem().displayMetrics.scaledDensity
    }
    private val bounds = Rect()

    /** Height of the header's capitals in px, to centre them on the icon. */
    private val capsPx: Float = Rect().also { header.getTextBounds("M", 0, 1, it) }.height().toFloat()

    /** Clear space kept between the arrow and the header (icon and speed). */
    private const val ARROW_GAP = 6f

    /** Radius (in design units, box = 100) that holds the arrow at any angle. */
    private const val ARROW_RADIUS = 34f

    fun render(s: ClearWindState, width: Int, height: Int, night: Boolean): Bitmap {
        val p = if (night) DARK else LIGHT
        val w = max(width, 40)
        val h = max(height, 40)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(p.bg)

        // Header: icon left, "5.2 M/S" right, both centred on the same line.
        val mid = ICON_TOP + ICON_PX / 2f
        val baseline = mid + capsPx / 2f
        drawWindIcon(c, ICON_LEFT, ICON_TOP, ICON_PX, p.text)
        val label = "${s.speedText} ${if (s.kmh) "KM/H" else "M/S"}"
        header.color = p.text
        c.drawText(label, w - TEXT_RIGHT, baseline, header)

        // Arrow: a 100×100 design box, as big as the field allows while a circle round it (any
        // angle) stays ARROW_GAP clear of the icon and the label. Normally centred in the space
        // under the header; in fields wide enough that it can be bigger centred in the whole
        // field (beside the header), it goes there instead.
        // Measure the label with every digit as 8, so the arrow keeps its size while the number
        // changes (it only changes when the format does, e.g. 9.8 → 12).
        val widest = label.replace(Regex("[0-9]"), "8")
        header.getTextBounds(widest, 0, widest.length, bounds)
        val textRect = RectF(
            w - TEXT_RIGHT - bounds.width() - ARROW_GAP, baseline + bounds.top - ARROW_GAP,
            w - TEXT_RIGHT + ARROW_GAP, baseline + bounds.bottom + ARROW_GAP,
        )
        val iconRect = RectF(
            ICON_LEFT - ARROW_GAP, ICON_TOP - ARROW_GAP,
            ICON_LEFT + ICON_PX + ARROW_GAP, ICON_TOP + ICON_PX + ARROW_GAP,
        )
        val edge = 4f
        // A: centred under the header.
        val top = max(textRect.bottom, iconRect.bottom)
        val radiusUnder = min((h - edge - top) / 2f, w / 2f - edge)
        // B: centred in the whole field.
        val radiusCentre = min(
            min(w, h) / 2f - edge,
            min(distance(w / 2f, h / 2f, textRect), distance(w / 2f, h / 2f, iconRect)),
        )
        val under = radiusUnder >= radiusCentre
        val cx = w / 2f
        val cy = if (under) (top + h - edge) / 2f else h / 2f
        val size = max(radiusUnder, radiusCentre) * 100f / ARROW_RADIUS
        c.save()
        c.translate(cx, cy)
        c.scale(size / 100f, size / 100f)
        s.arrowDeg?.let { drawArrow(c, it.toFloat(), arrowColor(s, p)) }
        c.restore()
        return bmp
    }

    /** Distance from a point to the nearest edge of a rectangle (0 if inside). */
    private fun distance(x: Float, y: Float, r: RectF): Float {
        val dx = max(max(r.left - x, 0f), x - r.right)
        val dy = max(max(r.top - y, 0f), y - r.bottom)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * The Karoo's own wind arrow style: straight shaft, sharp angled head (Material "arrow upward"
     * shape, 24-unit box scaled to the 100-unit design box), filled, pointing [deg] clockwise from up.
     */
    private val ARROW_PATH = Path().apply {
        // Arrow A2 (docs/design/arrow-head): the native shape with the wings 25% shorter.
        moveTo(12f, 4f); lineTo(18f, 10f); lineTo(16.58f, 11.42f); lineTo(13f, 7.83f)
        lineTo(13f, 20f); lineTo(11f, 20f); lineTo(11f, 7.83f); lineTo(7.42f, 11.42f)
        lineTo(6f, 10f); close()
    }

    private val arrowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private fun drawArrow(c: Canvas, deg: Float, color: Int) {
        c.save()
        c.rotate(deg)
        c.scale(100f / 24f, 100f / 24f)
        c.translate(-12f, -12f)
        arrowFill.color = color
        c.drawPath(ARROW_PATH, arrowFill)
        c.restore()
    }

    /**
     * Three wind lines with curled ends, drawn in a 24-unit box whose ink spans x 2..19, y 3.5..21.5.
     * [left]/[top] place the ink's top-left corner; [inkHeight] is its height in px.
     */
    private fun drawWindIcon(c: Canvas, left: Float, top: Float, inkHeight: Float, color: Int) {
        val s = inkHeight / 18f
        c.save()
        c.translate(left - 2f * s, top - 3.5f * s)
        c.scale(s, s)
        stroke.color = color
        stroke.strokeWidth = 2.2f
        val path = Path().apply {
            // Top: short line, curl up.
            moveTo(3f, 9f); lineTo(13f, 9f)
            arcTo(RectF(10.5f, 4f, 15.5f, 9f), 90f, -270f)
            // Middle: long line, curl up.
            moveTo(3f, 12.5f); lineTo(16.5f, 12.5f)
            arcTo(RectF(14f, 7.5f, 19f, 12.5f), 90f, -270f)
            // Bottom: line, curl down.
            moveTo(3f, 16f); lineTo(14f, 16f)
            arcTo(RectF(11.5f, 16f, 16.5f, 21f), -90f, 270f)
        }
        c.drawPath(path, stroke)
        c.restore()
    }
}
