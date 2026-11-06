package com.daviddokakis.clearwind

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import timber.log.Timber

/** Entry point the Karoo system binds to. */
class ClearWindExtension : KarooExtension(EXTENSION_ID, "1") {

    private lateinit var karoo: KarooSystemService
    private lateinit var model: ClearWindSession

    override val types: List<DataTypeImpl> by lazy {
        listOf(ClearWindDataType(extension, model))
    }

    override fun onCreate() {
        super.onCreate()
        if (Timber.treeCount == 0) Timber.plant(Timber.DebugTree())
        karoo = KarooSystemService(this)
        model = ClearWindSession(this, karoo)
        session = model
        karoo.connect { connected ->
            Timber.i("Karoo system connected: $connected")
        }
        model.start()
    }

    /** Remote buttons (assigned in the Karoo's controller settings). Always arrive, unlike field taps. */
    override fun onBonusAction(actionId: String) {
        when (actionId) {
            ACTION_TOGGLE_UNITS -> model.toggleUnits()
            ACTION_ARROW_COLOUR -> model.cycleArrowColour()
            else -> Timber.w("Unknown action $actionId")
        }
    }

    override fun onDestroy() {
        model.stop()
        session = null
        karoo.disconnect()
        super.onDestroy()
    }

    companion object {
        const val EXTENSION_ID = "clearwind"
        const val ACTION_TOGGLE_UNITS = "toggle-units"
        const val ACTION_ARROW_COLOUR = "arrow-colour"

        /** The running session, for receivers (field taps, debug remote). */
        @Volatile var session: ClearWindSession? = null
    }
}
