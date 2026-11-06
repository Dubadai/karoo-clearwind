package com.daviddokakis.clearwind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * DEBUG BUILDS ONLY. Fakes a remote button press (adb can't press bonus actions):
 * `adb shell am broadcast -a com.daviddokakis.clearwind.DEBUG_REMOTE -p com.daviddokakis.clearwind --es id toggle-units` (or `arrow-colour`)
 */
class DebugRemoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val session = ClearWindExtension.session ?: return
        when (intent.getStringExtra("id")) {
            ClearWindExtension.ACTION_TOGGLE_UNITS -> session.toggleUnits()
            ClearWindExtension.ACTION_ARROW_COLOUR -> session.cycleArrowColour()
        }
    }
}
