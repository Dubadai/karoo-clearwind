package com.daviddokakis.clearwind

import com.dubadai.karoo.common.*
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.widget.LinearLayout
import com.daviddokakis.clearwind.ClearWindState.ArrowColour
import com.daviddokakis.clearwind.ClearWindState.TileColour
import com.daviddokakis.clearwind.ClearWindState.WindUnit

/**
 * ClearWind's settings, opened from the Karoo's app list, built from the shared settings kit
 * (SettingsKit.kt; design: karoo-starter/docs/design/settings-kit): the app's name, the Example (the
 * field as these settings draw it, scrolling with the page), then unit, tile colour, arrow colour,
 * gusts, and the same last rows as every app. Follows the Karoo's light / dark mode.
 */
class SettingsActivity : Activity() {

    private lateinit var form: LinearLayout
    private lateinit var example: ExampleCard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }
        example = ExampleCard(this)
        setContentView(
            SettingsLayout(
                this,
                back = settingsBack(getString(R.string.back)) { finish() },
                barTitle = getString(R.string.settings),
                title = settingsTitle(getString(R.string.app_short_name)),
                card = example,
                form = form,
                pageColor = getColor(R.color.settings_bg),
                textColor = getColor(R.color.settings_title),
            ),
        )
    }

    override fun onResume() {
        super.onResume()
        render() // a remote button or a field tap may have changed things since
    }

    private fun render() {
        showExample()
        form.removeAllViews()

        form.addView(settingsHeading(R.drawable.ic_settings_speed, getString(R.string.setting_unit)))
        form.addView(
            settingsSegmented(WindUnit.entries.map { it.label to it }, Settings.unit(this)) {
                Settings.setUnit(this, it)
                render()
            },
        )
        form.addView(settingsHelp(getString(R.string.setting_unit_help)))
        form.addView(settingsDivider())

        val tiled = Settings.tileColour(this) != TileColour.OFF
        form.addView(settingsHeading(R.drawable.ic_settings_tiles, getString(R.string.setting_tile)))
        form.addView(
            settingsSegmented(
                listOf(getString(R.string.tile_off) to TileColour.OFF, getString(R.string.tile_wind) to TileColour.WIND),
                Settings.tileColour(this),
            ) {
                Settings.setTileColour(this, it)
                render()
            },
        )
        form.addView(settingsHelp(getString(R.string.setting_tile_help)))
        form.addView(settingsDivider())

        form.addView(settingsHeading(R.drawable.ic_arrow, getString(R.string.setting_colour)))
        form.addView(
            settingsSegmented(
                listOf(
                    getString(R.string.colour_speed) to ArrowColour.SPEED,
                    getString(R.string.colour_plain) to ArrowColour.MONO,
                    getString(R.string.colour_green) to ArrowColour.GREEN,
                ),
                Settings.arrowColour(this),
                // A coloured tile keeps the arrow black / white for contrast, so the colour choice is off.
                enabled = !tiled,
            ) {
                Settings.setArrowColour(this, it)
                render()
            },
        )
        form.addView(settingsHelp(getString(if (tiled) R.string.setting_colour_tiled_help else R.string.setting_colour_help)))
        form.addView(settingsDivider())

        form.addView(
            settingsSwitchRow(R.drawable.ic_wind, getString(R.string.setting_gusts), Settings.showGusts(this)) {
                Settings.setShowGusts(this, it)
                render()
            },
        )
        form.addView(settingsHelp(getString(R.string.setting_gusts_help)))

        settingsAppRows(form, repo = "karoo-clearwind") { render() }
    }

    /** The wind field with made-up wind (5.2 m/s, gusts 9, from behind on the left), at a Karoo half-width tile's size. */
    private fun showExample() {
        val s = ClearWindState(
            windSpeedMs = 5.2, windGustMs = 9.0, windFromDeg = 225.0, headingDeg = 0.0,
            unit = Settings.unit(this), arrowColour = Settings.arrowColour(this), tileColour = Settings.tileColour(this), showGusts = Settings.showGusts(this),
        )
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        example.show(
            WindRenderer.render(s, EXAMPLE_W, EXAMPLE_H, night),
            getString(R.string.example_description, listOfNotNull(s.speedText, s.gustText?.let { "($it)" }, s.unit.label).joinToString(" ")),
        )
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private companion object {
        /** A half-width, one-row field on the Karoo 3 (pixels). */
        const val EXAMPLE_W = 238
        const val EXAMPLE_H = 158
    }
}
