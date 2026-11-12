package com.daviddokakis.clearwind

import com.dubadai.karoo.common.*

import android.content.Context
import android.os.SystemClock
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.ActiveRidePage
import io.hammerhead.karooext.models.RideProfile
import io.hammerhead.karooext.models.HttpResponseState
import io.hammerhead.karooext.models.OnHttpResponse
import io.hammerhead.karooext.models.OnLocationChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import timber.log.Timber
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * App logic and live data. One instance, owned by [ClearWindExtension].
 *
 * Heading and position come from the Karoo. Wind comes from Open-Meteo (free, no key) for the
 * current position, fetched through the Karoo's connection (Wi-Fi or the phone app) every
 * [REFRESH_MS], or sooner after moving [REFETCH_DISTANCE_M].
 */
class ClearWindSession(private val context: Context, private val karoo: KarooSystemService) {

    // One thread for all state changes, so updates never race.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val _state = MutableStateFlow(
        ClearWindState(unit = Settings.unit(context), arrowColour = Settings.arrowColour(context), tileColour = Settings.tileColour(context), showGusts = Settings.showGusts(context)),
    )
    val state: StateFlow<ClearWindState> = _state

    /**
     * The ride page on screen (null until the Karoo says). With ClearWind on two pages the Karoo
     * can show one page's picture in the other's tile, so only the views on this page draw.
     */
    private val _activePage = MutableStateFlow<RideProfile.Page?>(null)
    val activePage: StateFlow<RideProfile.Page?> = _activePage

    // The settings screen writes the same preferences: pick its changes up at once.
    // (Kept in a field: SharedPreferences only holds listeners weakly.)
    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        scope.launch { _state.update { it.copy(unit = Settings.unit(context), arrowColour = Settings.arrowColour(context), tileColour = Settings.tileColour(context), showGusts = Settings.showGusts(context)) } }
    }

    private var lat: Double? = null
    private var lng: Double? = null
    private var fetchedAt = 0L          // elapsedRealtime of the last good fetch
    private var fetchedLat = 0.0
    private var fetchedLng = 0.0

    fun start() {
        Settings.prefs(context).registerOnSharedPreferenceChangeListener(prefsListener)
        scope.launch {
            karoo.consumerFlow<ActiveRidePage>().collect { event ->
                _activePage.value = event.page
                Timber.d("Active page: ${event.page.elements.map { "${it.dataTypeId.substringAfterLast(':')} ${it.gridSize}" }}")
            }
        }
        scope.launch {
            karoo.consumerFlow<OnLocationChanged>().collect { loc ->
                lat = loc.lat
                lng = loc.lng
                // Orientation is null or meaningless when standing still: keep the last one.
                loc.orientation?.let { o -> _state.update { it.copy(headingDeg = o) } }
            }
        }
        scope.launch {
            while (true) {
                val la = lat
                val ln = lng
                val wait = if (la != null && ln != null && fetchDue(la, ln)) {
                    if (fetchWind(la, ln)) CHECK_MS else RETRY_MS
                } else {
                    CHECK_MS
                }
                delay(wait)
            }
        }
    }

    fun stop() {
        Settings.prefs(context).unregisterOnSharedPreferenceChangeListener(prefsListener)
        scope.cancel()
    }

    /** Field tap or remote button: arrow colour speed → black/white → Karoo green, remembered. */
    fun cycleArrowColour() {
        scope.launch {
            _state.update { it.copy(arrowColour = it.arrowColour.next()) }
            Settings.setArrowColour(context, _state.value.arrowColour)
            Timber.i("Arrow colour: ${_state.value.arrowColour}")
        }
    }

    /** Remote button: next unit (knots → m/s → km/h → mph), remembered. */
    fun toggleUnits() {
        scope.launch {
            _state.update { it.copy(unit = it.unit.next()) }
            Settings.setUnit(context, _state.value.unit)
            Timber.i("Units: ${_state.value.unit.label}")
        }
    }

    private fun fetchDue(la: Double, ln: Double): Boolean {
        if (fetchedAt == 0L) return true
        if (SystemClock.elapsedRealtime() - fetchedAt >= REFRESH_MS) return true
        return distanceM(la, ln, fetchedLat, fetchedLng) >= REFETCH_DISTANCE_M
    }

    /** One weather request. Position rounded to ~1 km: plenty for wind, and less precise to share. */
    private suspend fun fetchWind(la: Double, ln: Double): Boolean {
        val url = String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f" +
                "&current=wind_speed_10m,wind_direction_10m,wind_gusts_10m&wind_speed_unit=ms",
            la, ln,
        )
        val body = withTimeoutOrNull(HTTP_TIMEOUT_MS) { httpGet(url) } ?: run {
            Timber.w("Wind fetch failed or timed out")
            _state.update { it.copy(fetchFailed = true) }
            return false
        }
        return try {
            val current = JSONObject(body).getJSONObject("current")
            val speed = current.getDouble("wind_speed_10m")
            val gust = current.optDouble("wind_gusts_10m", Double.NaN).takeIf { it.isFinite() }
            val from = current.getDouble("wind_direction_10m")
            _state.update { it.copy(windSpeedMs = speed, windGustMs = gust, windFromDeg = from, fetchFailed = false) }
            fetchedAt = SystemClock.elapsedRealtime()
            fetchedLat = la
            fetchedLng = ln
            Timber.i("Wind: %.1f m/s, gusts %s, from %.0f°", speed, gust?.let { String.format(Locale.US, "%.1f", it) } ?: "–", from)
            true
        } catch (e: Exception) {
            Timber.w(e, "Wind response not understood")
            _state.update { it.copy(fetchFailed = true) }
            false
        }
    }

    /** GET through the Karoo (Wi-Fi or phone). Null on any failure. */
    private suspend fun httpGet(url: String): String? = suspendCancellableCoroutine { cont ->
        var id: String? = null
        id = karoo.addConsumer<OnHttpResponse>(
            OnHttpResponse.MakeHttpRequest(
                method = "GET",
                url = url,
                headers = mapOf("User-Agent" to "ClearWind-Karoo"),
                waitForConnection = false,
            ),
            onError = { msg ->
                Timber.w("HTTP error: $msg")
                if (cont.isActive) cont.resume(null)
            },
        ) { event ->
            val s = event.state
            if (s is HttpResponseState.Complete) {
                id?.let { karoo.removeConsumer(it) }
                val ok = s.statusCode in 200..299 && s.body != null
                if (!ok) Timber.w("HTTP ${s.statusCode} ${s.error ?: ""}")
                if (cont.isActive) cont.resume(if (ok) s.body!!.decodeToString() else null)
            }
        }
        cont.invokeOnCancellation { id?.let { karoo.removeConsumer(it) } }
    }

    private fun distanceM(la1: Double, ln1: Double, la2: Double, ln2: Double): Double {
        // Flat-earth approximation: fine for a few km.
        val dLat = (la2 - la1) * 111_320.0
        val dLng = (ln2 - ln1) * 111_320.0 * cos(Math.toRadians((la1 + la2) / 2))
        return sqrt(dLat * dLat + dLng * dLng)
    }

    companion object {
        private const val REFRESH_MS = 10 * 60_000L
        private const val REFETCH_DISTANCE_M = 5_000.0
        private const val CHECK_MS = 15_000L
        private const val RETRY_MS = 60_000L
        private const val HTTP_TIMEOUT_MS = 30_000L
    }
}
