package com.daviddokakis.clearwind

import com.dubadai.karoo.common.*
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
import kotlin.math.roundToInt

/**
 * Draws the field (see docs/design/field/README.md) on a transparent background, so the Karoo's
 * own tile (rounded corners, border, white or black) shows through.
 *
 * Two pictures, because the Karoo may show the field shorter than the size it reported (route or
 * workout bar on screen) and then shrinks whatever we draw (docs/design/header-typography):
 * - the header (wind icon left, "5.2 M/S" right), sized and placed like the Karoo's own headers.
 *   The field shows it at 1:1, never scaled;
 * - the arrow, coloured by wind speed, as big as the space allows up to a cap, with lines 9 px
 *   thick that grow gently on big arrows (docs/design/arrow-thickness). This one may shrink.
 * [render] puts both in one bitmap, for the settings example and the gallery.
 */
object WindRenderer {

    private class Palette(val text: Int)

    // Pure black and white, like the Karoo's own fields (its header colour is plain black / white).
    private val LIGHT = Palette(Color.BLACK)
    private val DARK = Palette(Color.WHITE)

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

    /**
     * Tile colours by wind band: the Karoo's zone colours climb1–4, 6, 7 laid over white at 56 %
     * (light) and over black at 37 % (dark), the strongest that keeps black / white text at WCAG
     * AAA (7 : 1) in every band: 7.1–19.0 : 1 light, 7.1–16.0 : 1 dark. docs/design/tile-colour/.
     * Keep in step with res/drawable/tile_*.xml (the live field's background).
     */
    val TILE_LIGHT = intArrayOf(
        0xFFA6F5D4.toInt(), 0xFF70D8BC.toInt(), 0xFFFFF970.toInt(),
        0xFFFDBFA9.toInt(), 0xFFE87272.toInt(), 0xFFD770CB.toInt(),
    )
    val TILE_DARK = intArrayOf(
        0xFF245842.toInt(), 0xFF004432.toInt(), 0xFF5E5B00.toInt(),
        0xFF5D3425.toInt(), 0xFF4F0101.toInt(), 0xFF44003C.toInt(),
    )

    /** Upper bounds (m/s) of the bands: calm, light, gentle, moderate, strong, gale. */
    private val WIND_BANDS = doubleArrayOf(2.0, 4.0, 6.0, 8.0, 11.0)

    /** The tile's wind band (0–5), or null for the Karoo's own tile (setting off, or no wind yet). */
    fun tileBand(s: ClearWindState): Int? {
        if (s.tileColour == ClearWindState.TileColour.OFF) return null
        val ms = s.windSpeedMs ?: return null
        return WIND_BANDS.indexOfFirst { ms < it }.let { if (it < 0) WIND_BANDS.size else it }
    }

    fun tileColor(s: ClearWindState, night: Boolean): Int? =
        tileBand(s)?.let { (if (night) TILE_DARK else TILE_LIGHT)[it] }

    private fun palette(night: Boolean): Palette = if (night) DARK else LIGHT

    // Our own card, only for a coloured tile, drawn to the Karoo's tile rules (karoo-type README,
    // SmallerFields TileRenderer): radius 14, 1 px #7D7D7D border, clipped to the rounded tile;
    // no corners or border when data field boundaries are off.
    private const val TILE_RADIUS = 14f
    private const val TILE_BORDER = 0xFF7D7D7D.toInt()
    private val tileFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tileLine = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = TILE_BORDER }

    /** Fills [w]×[h] with the tile colour, if any. Call before drawing the header and arrow. */
    fun drawTile(c: Canvas, s: ClearWindState, w: Float, h: Float, night: Boolean, boundaries: Boolean = true) {
        val colour = tileColor(s, night) ?: return
        tileFill.color = colour
        if (!boundaries) { c.drawRect(0f, 0f, w, h, tileFill); return }
        val r = RectF(0.5f, 0.5f, w - 0.5f, h - 0.5f)
        c.drawRoundRect(r, TILE_RADIUS, TILE_RADIUS, tileFill)
        c.drawRoundRect(r, TILE_RADIUS, TILE_RADIUS, tileLine)
    }

    private fun arrowColor(s: ClearWindState, p: Palette): Int {
        val ms = s.windSpeedMs ?: return NO_DATA
        if (s.tileColour != ClearWindState.TileColour.OFF) return p.text
        return when (s.arrowColour) {
            ClearWindState.ArrowColour.SPEED -> speedColor(ms)
            ClearWindState.ArrowColour.MONO -> p.text
            ClearWindState.ArrowColour.GREEN -> KAROO_GREEN
        }
    }

    // Header, measured on the Karoo's own fields (docs/design/header-typography): 23 px icons 12 px
    // in from the left, centred on the capitals; the text's ink ends 10 px from the right edge
    // (the last letter has ~2 px of side space). Capitals are 20 px tall in tiles up to 142 px,
    // 23 px in 168 px tiles, and start lower in taller tiles: 21 px from the top at 142, 26 at 168,
    // never above 18. The Karoo goes by the size it reported, even when a bar makes the tile shorter.
    private const val ICON_PX = 23f
    private const val ICON_LEFT = 12f
    private const val TEXT_RIGHT = KarooType.HEADER_RIGHT_INSET_PX.toFloat()
    /** Gaps in the header, in text sizes (a normal space is about 0.22). */
    private const val UNIT_GAP_EM = 0.38f
    private const val GUST_GAP_EM = 0.17f
    /**
     * Extra outline on the header text, in text sizes. Plain Plex Condensed Medium comes out ~5 %
     * thinner than the Karoo's own labels (stroke 3.03 vs 3.18 px at 23 px caps, measured on the
     * device). 0.002 gives 3.27; anything under ~0.002 changes nothing on the Karoo.
     */
    private const val HEADER_STROKE_EM = 0.002f
    // Cap height and top come from KarooType (shared by all our apps). A full-width row counts
    // as full width from 400 px (the Karoo's is 478).
    private fun capsTop(w: Int, h: Int): Float = KarooType.headerCapsTop(h, fullWidth = w >= 400).toFloat()

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
        // The Karoo's own data-label style: "ibm-plex-sans-condensed", regular, black (white at
        // night), capitals. Its size follows the tile (see Layout).
        typeface = Typeface.create("ibm-plex-sans-condensed", Typeface.NORMAL)
        textAlign = Paint.Align.RIGHT
        style = Paint.Style.FILL_AND_STROKE
        strokeJoin = Paint.Join.ROUND
        textSize = 100f
    }
    private val bounds = Rect()

    /** Capital height per px of text size, to size the header by its capitals. */
    private val capsPerPx: Float = Rect().also { header.getTextBounds("M", 0, 1, it) }.height() / 100f

    /** Clear space kept between the arrow and the header (icon and speed). */
    private const val ARROW_GAP = 6f

    /** Radius (in design units, box = 100) that holds the arrow at any angle. */
    private const val ARROW_RADIUS = 34f

    /** Arrow lines: 9 px up to this arrow length (the short half-width tile, 68 px)… */
    private const val LINE_PX = 9f
    private const val LINE_FROM_LEN = 68f
    /** …growing to [ArrowCap.linePx] at the longest arrow allowed. */
    private class ArrowCap(val maxLen: Float, val linePx: Float)
    private val CAP_TILE = ArrowCap(100f, 12f)
    private val CAP_PAGE = ArrowCap(126f, 15f)
    /** The arrow at 90 % of the room it has (feedback: a tad smaller, more air around it). */
    private const val ARROW_SCALE = 0.9f
    /** Lines never thicker than this many units of the 24-unit arrow (tiny arrows stay arrows). */
    private const val MAX_LINE_UNITS = 2.6f

    /** Where everything goes in a field of [w]×[h] px. */
    class Layout internal constructor(
        val w: Int, val h: Int,
        /** Header picture height: header plus the clear space under it. */
        val headerH: Int,
        val capsTop: Float, val capsPx: Float, val textPx: Float, val iconTop: Float,
        /** Arrow centred under the header (true) or in the whole field, beside the header. */
        val under: Boolean,
        /** Arrow centre and size, in the arrow picture: [w]×([h]−[headerH]) if [under], else [w]×[h]. */
        val cx: Float, val cy: Float,
        /** px per unit of the 24-unit arrow box. */
        val unit: Float,
        /** Line thickness in arrow units. */
        val lineUnits: Float,
    ) {
        val arrowH: Int get() = if (under) h - headerH else h
    }

    fun layout(s: ClearWindState, width: Int, height: Int): Layout {
        val w = max(width, 40)
        val h = max(height, 40)
        val capsPx = KarooType.headerCapsPx(h).toFloat()
        val capsTop = capsTop(w, h)
        val textPx = capsPx / capsPerPx
        val iconTop = capsTop + capsPx / 2f - ICON_PX / 2f
        val baseline = capsTop + capsPx

        header.textSize = textPx
        val widestW = widestHeaderWidth(s)
        header.getTextBounds("M/S()", 0, 5, bounds)
        val textRect = RectF(
            w - TEXT_RIGHT - widestW - ARROW_GAP, baseline + bounds.top - ARROW_GAP,
            w - TEXT_RIGHT + ARROW_GAP, baseline + bounds.bottom + ARROW_GAP,
        )
        val iconRect = RectF(
            ICON_LEFT - ARROW_GAP, iconTop - ARROW_GAP,
            ICON_LEFT + ICON_PX + ARROW_GAP, iconTop + ICON_PX + ARROW_GAP,
        )
        val edge = 4f
        // The arrow sits in a circle that holds it at any angle, ARROW_GAP clear of the header.
        // A: centred under the header.
        val top = max(textRect.bottom, iconRect.bottom)
        val radiusUnder = min((h - edge - top) / 2f, w / 2f - edge)
        // B: centred in the whole field (wide, short fields: it can be bigger beside the header).
        val radiusCentre = min(
            min(w, h) / 2f - edge,
            min(distance(w / 2f, h / 2f, textRect), distance(w / 2f, h / 2f, iconRect)),
        )
        val under = radiusUnder >= radiusCentre
        val headerH = kotlin.math.ceil(top).toInt()
        // Design box (100 units) → 24-unit arrow box: px per arrow unit; the arrow is 16 units long.
        var unit = max(radiusUnder, radiusCentre) * 100f / ARROW_RADIUS / 24f * ARROW_SCALE
        val cap = if (w >= 300 && h >= 250) CAP_PAGE else CAP_TILE
        unit = min(unit, cap.maxLen / ARROW_LEN)
        val len = unit * ARROW_LEN
        val linePx = if (len <= LINE_FROM_LEN) LINE_PX
        else LINE_PX + (cap.linePx - LINE_PX) * min(1f, (len - LINE_FROM_LEN) / (cap.maxLen - LINE_FROM_LEN))
        val lineUnits = min(linePx / unit, MAX_LINE_UNITS)
        val cx = w / 2f
        val cy = if (under) (h - edge - headerH) / 2f else h / 2f
        return Layout(w, h, headerH, capsTop, capsPx, textPx, iconTop, under, cx, cy, unit, lineUnits)
    }

    /** The header picture: [Layout.w]×[Layout.headerH], to show at 1:1 at the top of the field. */
    fun renderHeader(s: ClearWindState, l: Layout, night: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(l.w, max(l.headerH, 1), Bitmap.Config.ARGB_8888)
        drawHeader(Canvas(bmp), s, l, palette(night))
        return bmp
    }

    /** The arrow picture: [Layout.w]×[Layout.arrowH]; transparent where there's no arrow. */
    fun renderArrow(s: ClearWindState, l: Layout, night: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(l.w, max(l.arrowH, 1), Bitmap.Config.ARGB_8888)
        drawArrowAt(Canvas(bmp), s, l, palette(night))
        return bmp
    }

    /** Header and arrow in one bitmap (settings example, gallery). */
    fun render(s: ClearWindState, width: Int, height: Int, night: Boolean): Bitmap {
        val l = layout(s, width, height)
        val p = palette(night)
        val bmp = Bitmap.createBitmap(l.w, l.h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp) // left transparent: the Karoo draws the tile behind it
        drawTile(c, s, l.w.toFloat(), l.h.toFloat(), night)
        drawHeader(c, s, l, p)
        c.save()
        if (l.under) c.translate(0f, l.headerH.toFloat())
        drawArrowAt(c, s, l, p)
        c.restore()
        return bmp
    }

    private fun drawHeader(c: Canvas, s: ClearWindState, l: Layout, p: Palette) {
        // The Karoo's sensor legend: grey, full size, while there's no wind. No red ×: the tile
        // already says "No connection" in words (shared rule: × only when a tile shows no text).
        val iconColour = if (s.status == ClearWindState.WindStatus.LIVE) p.text else KarooType.STATE_GREY
        drawWindIcon(c, ICON_LEFT, l.iconTop, ICON_PX, iconColour)
        header.textSize = l.textPx
        header.strokeWidth = l.textPx * HEADER_STROKE_EM
        header.color = p.text
        // Right to left: unit, a wide gap, gust, a narrow gap, speed. "6.0 (10)   M/S"
        var x = l.w - TEXT_RIGHT
        val y = l.capsTop + l.capsPx
        for ((i, part) in headerParts(s).asReversed().withIndex()) {
            if (i > 0) x -= gapBefore(i)
            c.drawText(part, x, y, header)
            x -= header.measureText(part)
        }
    }

    private fun drawArrowAt(c: Canvas, s: ClearWindState, l: Layout, p: Palette) {
        s.emptyText?.let { text ->
            // Like the Karoo's "No Sensor": label font at 25 px caps, centred, ink bottom 39 px above
            // the tile bottom (KarooType). This picture starts under the header when [Layout.under].
            val paint = KarooType.emptyPaint(text, l.w).apply { color = p.text }
            val top = if (l.under) l.headerH else 0
            c.drawText(text, l.w / 2f, KarooType.emptyBaseline(l.h) - top, paint)
            return
        }
        val deg = s.arrowDeg ?: return
        c.save()
        c.translate(l.cx, l.cy)
        c.rotate(deg.toFloat())
        c.scale(l.unit, l.unit)
        c.translate(-12f, -12f)
        arrowFill.color = arrowColor(s, p)
        c.drawPath(arrowPath(l.lineUnits), arrowFill)
        c.restore()
    }

    /** Header words, left to right: speed, "(gust)" if known, unit. */
    private fun headerParts(s: ClearWindState): List<String> =
        listOfNotNull(s.speedText, s.gustText?.let { "($it)" }, s.unit.header)

    /** Gap before the i-th part counted from the right: wide before the unit, narrow inside "6.0 (10)". */
    private fun gapBefore(i: Int): Float = header.textSize * if (i == 1) UNIT_GAP_EM else GUST_GAP_EM

    /**
     * The widest header this unit can show, at the current size: "8.8 (88) M/S" (m/s) or
     * "88 (88) KM/H"; without the "(88)" when gusts are hidden. The arrow is sized and placed around this, never around the live text, so
     * it keeps its size and place in a tile whatever the wind (it used to jump between under and
     * beside the header when the number got wider, e.g. 9.8 → 12, or when gusts came and went).
     */
    private fun widestHeaderWidth(s: ClearWindState): Float {
        val parts = listOfNotNull(if (s.unit.decimals) "8.8" else "88", "(88)".takeIf { s.showGusts }, s.unit.header)
        return parts.sumOf { header.measureText(it).toDouble() }.toFloat() +
            (1 until parts.size).sumOf { gapBefore(it).toDouble() }.toFloat()
    }

    /** Distance from a point to the nearest edge of a rectangle (0 if inside). */
    private fun distance(x: Float, y: Float, r: RectF): Float {
        val dx = max(max(r.left - x, 0f), x - r.right)
        val dy = max(max(r.top - y, 0f), y - r.bottom)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /** Arrow length in its 24-unit box: tip at y 4, tail at y 20. */
    private const val ARROW_LEN = 16f

    /**
     * The Karoo's own wind arrow style (arrow A2, docs/design/arrow-head): straight shaft, sharp
     * angled head with the wings 25% shorter than Material's, filled, pointing up in a 24-unit box.
     * Built from the line thickness [t] (in units) instead of scaling one shape, so the lines can
     * stay the same thickness at any size (docs/design/arrow-thickness). t = 2 is the old shape.
     */
    private fun arrowPath(t: Float): Path {
        val d = t / kotlin.math.sqrt(2f)            // wing thickness, sideways
        val neck = 4f + t / 2f + t * kotlin.math.sqrt(2f) // where the wings meet the shaft
        return Path().apply {
            moveTo(12f, 4f); lineTo(18f, 10f); lineTo(18f - d, 10f + d); lineTo(12f + t / 2f, neck)
            lineTo(12f + t / 2f, 20f); lineTo(12f - t / 2f, 20f); lineTo(12f - t / 2f, neck)
            lineTo(6f + d, 10f + d); lineTo(6f, 10f); close()
        }
    }

    private val arrowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

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
