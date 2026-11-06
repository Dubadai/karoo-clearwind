package com.daviddokakis.clearwind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Taps on the data field: cycle the arrow colour. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val session = ClearWindExtension.session ?: return
        when (intent.action) {
            ACTION_TAP -> session.cycleArrowColour()
        }
    }

    companion object {
        const val ACTION_TAP = "com.daviddokakis.clearwind.TAP"
    }
}
