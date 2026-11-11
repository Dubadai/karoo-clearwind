package com.daviddokakis.clearwind

import com.dubadai.karoo.common.*
import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import android.widget.RemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

/** The ClearWind data field: any size; a header picture and an arrow picture (see WindRenderer). */
class ClearWindDataType(
    extension: String,
    private val session: ClearWindSession,
) : DataTypeImpl(extension, TYPE_ID) {

    /** Numeric fallback for places that show the field as a plain number: wind speed in m/s. */
    override fun startStream(emitter: Emitter<StreamState>) {
        val job = CoroutineScope(Dispatchers.Default).launch {
            session.state.collect { s ->
                val ms = s.windSpeedMs
                emitter.onNext(
                    if (ms == null) StreamState.NotAvailable
                    else StreamState.Streaming(DataPoint(dataTypeId, values = mapOf(DataType.Field.SINGLE to ms))),
                )
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val viewId = nextViewId.incrementAndGet()
        Timber.d("View $viewId started: $config")
        emitter.onNext(UpdateGraphicConfig(showHeader = false))
        val (w, h) = config.viewSize
        if (config.preview) {
            // The page editor's preview: one still picture with example wind, then nothing more.
            // The Karoo can keep a preview view running after the editor closes, and its updates
            // then land in the live field on the ride page, swapping between two sizes
            // (docs/design/preview-swap). One draw can't fight the live field.
            try {
                val s = session.state.value
                emitter.updateView(render(context, EXAMPLE.copy(unit = s.unit, arrowColour = s.arrowColour, tileColour = s.tileColour, showGusts = s.showGusts), w, h, config.boundariesEnabled))
            } catch (e: Exception) {
                Timber.e(e, "Preview render failed")
            }
            emitter.setCancellable { Timber.d("View $viewId stopped") }
            return
        }
        val job = CoroutineScope(Dispatchers.Default).launch {
            // The Karoo drops updates closer than ~0.9 s apart. Redraw every second, and as soon
            // as allowed when something visible changes (see Key).
            var lastRender = 0L
            var shown: Key? = null
            var wasOnScreen = true
            // Sleep until the wind or the page on screen changes (karoo-common FieldPacer), no polling
            val pace = FieldPacer(listOf(session.state, session.activePage))
            val pagePace = FieldPacer(listOf(session.activePage))
            while (isActive) {
                pace.mark()
                pagePace.mark()
                // Only draw while this field's page is on screen (see ClearWindSession.activePage).
                // Coming back on screen: draw at once.
                val onScreen = onScreen(config)
                if (onScreen != wasOnScreen) {
                    Timber.d("View $viewId ${if (onScreen) "on screen" else "off screen, paused"}")
                    wasOnScreen = onScreen
                    if (onScreen) shown = null
                }
                if (!onScreen) {
                    pagePace.sleep(FieldPacer.CAP_MS)
                    continue
                }
                val s = session.state.value
                val key = Key(s.arrowDeg?.let { (it / 2).roundToInt() }, s.headerText, s.unit, s.arrowColour, WindRenderer.tileBand(s), s.showGusts)
                val since = SystemClock.elapsedRealtime() - lastRender
                if ((key != shown && since >= MIN_GAP_MS) || since >= IDLE_REDRAW_MS) {
                    try {
                        emitter.updateView(render(context, s, w, h, config.boundariesEnabled))
                        shown = key
                    } catch (e: Exception) {
                        Timber.e(e, "Render failed")
                    }
                    lastRender = SystemClock.elapsedRealtime()
                }
                pace.sleep(FieldPacer.nextMs(key != shown, SystemClock.elapsedRealtime() - lastRender, MIN_GAP_MS, IDLE_REDRAW_MS))
            }
        }
        emitter.setCancellable {
            Timber.d("View $viewId stopped")
            job.cancel()
        }
    }

    /**
     * Is this view's field on the ride page on screen? The shared page-rows rule
     * ([KarooType.onPage]): matched by grid size, allowing for the Karoo stretching a page's last
     * field. True until the Karoo has said which page is on screen.
     */
    private fun onScreen(config: ViewConfig): Boolean {
        val page = session.activePage.value ?: return true
        return KarooType.onPage(
            pageGrids = page.elements.map { it.gridSize },
            fieldGrids = page.elements.filter { it.dataTypeId == dataTypeId }.map { it.gridSize },
            view = config.gridSize,
        )
    }

    private fun render(context: Context, s: ClearWindState, w: Int, h: Int, boundaries: Boolean): RemoteViews {
        val night = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val l = WindRenderer.layout(s, w, h)
        val v = RemoteViews(context.packageName, if (l.under) R.layout.field_under else R.layout.field_beside)
        v.setImageViewBitmap(R.id.header, WindRenderer.renderHeader(s, l, night))
        v.setImageViewBitmap(R.id.arrow, WindRenderer.renderArrow(s, l, night))
        // Coloured tile: our own card as the root's background (a shape drawable keeps its corners
        // and border at whatever size the Karoo shows the field). Off / no wind: none, the Karoo's tile.
        v.setInt(R.id.root, "setBackgroundResource", WindRenderer.tileBand(s)?.let { tileDrawable(it, night, boundaries) } ?: 0)
        // No tap handler on purpose: a clickable field swallows the touch, so the Karoo's own
        // long-press (change data field) never starts. Colour is changed in Settings or by remote.
        return v
    }

    private fun tileDrawable(band: Int, night: Boolean, boundaries: Boolean): Int {
        val bordered = if (night) TILE_DARK else TILE_LIGHT
        val flat = if (night) TILE_DARK_FLAT else TILE_LIGHT_FLAT
        return (if (boundaries) bordered else flat)[band]
    }

    /** What triggers an immediate redraw when it changes (arrow in 2° steps). */
    private data class Key(
        val arrow: Int?,
        val speed: String,
        val unit: ClearWindState.WindUnit,
        val colour: ClearWindState.ArrowColour,
        val tileBand: Int?,
        val gusts: Boolean,
    )

    companion object {
        const val TYPE_ID = "clearwind-field"
        private val TILE_LIGHT = intArrayOf(R.drawable.tile_light_0, R.drawable.tile_light_1, R.drawable.tile_light_2, R.drawable.tile_light_3, R.drawable.tile_light_4, R.drawable.tile_light_5)
        private val TILE_DARK = intArrayOf(R.drawable.tile_dark_0, R.drawable.tile_dark_1, R.drawable.tile_dark_2, R.drawable.tile_dark_3, R.drawable.tile_dark_4, R.drawable.tile_dark_5)
        private val TILE_LIGHT_FLAT = intArrayOf(R.drawable.tile_light_0_flat, R.drawable.tile_light_1_flat, R.drawable.tile_light_2_flat, R.drawable.tile_light_3_flat, R.drawable.tile_light_4_flat, R.drawable.tile_light_5_flat)
        private val TILE_DARK_FLAT = intArrayOf(R.drawable.tile_dark_0_flat, R.drawable.tile_dark_1_flat, R.drawable.tile_dark_2_flat, R.drawable.tile_dark_3_flat, R.drawable.tile_dark_4_flat, R.drawable.tile_dark_5_flat)
        private const val MIN_GAP_MS = 920L
        private const val IDLE_REDRAW_MS = 5_000L
        /** The wind shown in the page editor's preview (like the Settings example). */
        private val EXAMPLE = ClearWindState(windSpeedMs = 5.2, windGustMs = 9.0, windFromDeg = 225.0, headingDeg = 0.0)
        private val nextViewId = java.util.concurrent.atomic.AtomicInteger()
    }
}
