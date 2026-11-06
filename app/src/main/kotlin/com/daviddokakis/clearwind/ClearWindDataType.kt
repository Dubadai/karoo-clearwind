package com.daviddokakis.clearwind

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

/** The ClearWind data field: any size, drawn as one bitmap. */
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
        Timber.d("View started: $config")
        emitter.onNext(UpdateGraphicConfig(showHeader = false))
        val (w, h) = config.viewSize
        val job = CoroutineScope(Dispatchers.Default).launch {
            // The Karoo drops updates closer than ~0.9 s apart. Redraw every second, and as soon
            // as allowed when something visible changes (see Key).
            var lastRender = 0L
            var shown: Key? = null
            while (isActive) {
                val s = DemoData.apply(session.state.value)
                val key = Key(s.arrowDeg?.let { (it / 2).roundToInt() }, s.speedText, s.unit, s.arrowColour)
                val since = SystemClock.elapsedRealtime() - lastRender
                if ((key != shown && since >= MIN_GAP_MS) || since >= IDLE_REDRAW_MS) {
                    try {
                        emitter.updateView(render(context, s, w, h))
                        shown = key
                    } catch (e: Exception) {
                        Timber.e(e, "Render failed")
                    }
                    lastRender = SystemClock.elapsedRealtime()
                }
                delay(POLL_MS)
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    private fun render(context: Context, s: ClearWindState, w: Int, h: Int): RemoteViews {
        val night = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val v = RemoteViews(context.packageName, R.layout.field_main)
        v.setInt(R.id.root, "setBackgroundColor", WindRenderer.background(night))
        v.setImageViewBitmap(R.id.dial, WindRenderer.render(s, w, h, night))
        // No tap handler on purpose: a clickable field swallows the touch, so the Karoo's own
        // long-press (change data field) never starts. Colour is changed in Settings or by remote.
        return v
    }

    /** What triggers an immediate redraw when it changes (arrow in 2° steps). */
    private data class Key(
        val arrow: Int?,
        val speed: String,
        val unit: ClearWindState.WindUnit,
        val colour: ClearWindState.ArrowColour,
    )

    companion object {
        const val TYPE_ID = "clearwind-field"
        private const val MIN_GAP_MS = 920L
        private const val IDLE_REDRAW_MS = 5_000L
        private const val POLL_MS = 40L
    }
}
