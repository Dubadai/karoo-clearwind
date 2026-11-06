package com.daviddokakis.clearwind

import android.content.Context
import android.content.SharedPreferences
import com.daviddokakis.clearwind.ClearWindState.ArrowColour
import com.daviddokakis.clearwind.ClearWindState.WindUnit

/**
 * The rider's choices, stored on the Karoo (app-private). Written by the settings screen, the
 * field tap and the remote actions; the running field listens for changes.
 */
object Settings {
    private const val FILE = "clearwind"
    private const val KEY_UNIT = "unit"
    private const val KEY_ARROW_COLOUR = "arrow_colour"
    private const val KEY_OLD_KMH = "kmh"              // 0.1.0 stored only km/h on/off

    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Wind speed unit; m/s until the rider picks another. */
    fun unit(context: Context): WindUnit {
        val p = prefs(context)
        p.getString(KEY_UNIT, null)?.let { name -> runCatching { return WindUnit.valueOf(name) } }
        return if (p.getBoolean(KEY_OLD_KMH, false)) WindUnit.KMH else WindUnit.MS
    }

    fun setUnit(context: Context, unit: WindUnit) =
        prefs(context).edit().putString(KEY_UNIT, unit.name).remove(KEY_OLD_KMH).apply()

    fun arrowColour(context: Context): ArrowColour =
        runCatching { ArrowColour.valueOf(prefs(context).getString(KEY_ARROW_COLOUR, null)!!) }
            .getOrDefault(ArrowColour.SPEED)

    fun setArrowColour(context: Context, colour: ArrowColour) =
        prefs(context).edit().putString(KEY_ARROW_COLOUR, colour.name).apply()
}
