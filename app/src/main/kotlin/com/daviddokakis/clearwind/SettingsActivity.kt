package com.daviddokakis.clearwind

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.daviddokakis.clearwind.ClearWindState.ArrowColour
import com.daviddokakis.clearwind.ClearWindState.WindUnit

/**
 * ClearWind's settings, opened from the Karoo's app list: wind speed unit and arrow colour, each
 * as the Karoo's own segmented control (as in Karoo Settings → Display and karoo-interval).
 * Follows the Karoo's light / dark mode. Design: docs/design/settings/.
 */
class SettingsActivity : Activity() {

    private lateinit var form: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.settings_bg))
        }
        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.ic_back)
                contentDescription = getString(R.string.back)
                scaleType = ImageView.ScaleType.CENTER
                isClickable = true
                setOnClickListener { finish() }
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply { setMargins(dp(4), dp(8), 0, 0) }
            },
        )
        root.addView(
            TextView(this).apply {
                text = getString(R.string.app_short_name)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(getColor(R.color.settings_title))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dp(12), dp(4), dp(12), dp(4))
            },
        )
        root.addView(ScrollView(this).apply { addView(form, MATCH_PARENT, WRAP_CONTENT) }, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        render() // a remote button or a field tap may have changed things since
    }

    private fun render() {
        form.removeAllViews()

        form.addView(heading(R.drawable.ic_wind, getString(R.string.setting_unit)))
        form.addView(
            segmented(WindUnit.entries.map { it.label to it }, Settings.unit(this)) {
                Settings.setUnit(this, it)
                render()
            },
        )
        form.addView(help(getString(R.string.setting_unit_help)))
        form.addView(divider())

        form.addView(heading(R.drawable.ic_app, getString(R.string.setting_colour)))
        form.addView(
            segmented(
                listOf(
                    getString(R.string.colour_speed) to ArrowColour.SPEED,
                    getString(R.string.colour_plain) to ArrowColour.MONO,
                    getString(R.string.colour_green) to ArrowColour.GREEN,
                ),
                Settings.arrowColour(this),
            ) {
                Settings.setArrowColour(this, it)
                render()
            },
        )
        form.addView(help(getString(R.string.setting_colour_help)))
    }

    /** Icon + title, like the Karoo's settings headings. */
    private fun heading(iconRes: Int, title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(14), 0, dp(10))
        addView(
            ImageView(this@SettingsActivity).apply {
                setImageResource(iconRes)
                setColorFilter(getColor(R.color.settings_title))
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply { marginEnd = dp(8) }
            },
        )
        addView(
            TextView(this@SettingsActivity).apply {
                text = title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(getColor(R.color.settings_title))
            },
        )
    }

    private fun help(value: String) = TextView(this).apply {
        text = value
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(getColor(R.color.settings_help))
        setPadding(0, dp(10), 0, dp(14))
    }

    private fun divider() = View(this).apply {
        setBackgroundColor(getColor(R.color.settings_divider))
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(1))
    }

    /** The Karoo's segmented control: a pale pill track, the selected option as a blue-grey pill. */
    private fun <T> segmented(options: List<Pair<String, T>>, selected: T, onPick: (T) -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundResource(R.drawable.bg_segment_track)
        setPadding(dp(2), dp(2), dp(2), dp(2))
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(42))
        options.forEach { (label, value) ->
            addView(
                TextView(this@SettingsActivity).apply {
                    text = label
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(getColor(R.color.segment_text))
                    if (value == selected) setBackgroundResource(R.drawable.bg_segment_selected)
                    isSelected = value == selected
                    isClickable = true
                    setOnClickListener { if (value != selected) onPick(value) }
                },
                LinearLayout.LayoutParams(0, MATCH_PARENT, 1f),
            )
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
