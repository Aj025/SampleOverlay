package com.program.axieEnergyCounter

object Constant {

    const val STARTING_COUNT = 3
    const val REQUEST_OVERLAY_CODE = 1
    const val COUNTER = "COUNTER"

    const val BUTTON_PRESSED_INTERVAL = 300
    const val EXTRA_SERVICE_DESTROYED = "EXTRA_SERVICE_DESTROYED"
    const val EXTRA_SERVICE_COUNTER = "EXTRA_SERVICE_COUNTER"
    const val EXTRA_SERVICE_RUNNING = "EXTRA_SERVICE_RUNNING"
    const val EXTRA_SERVICE_ALPHA = "EXTRA_SERVICE_ALPHA"
    const val EXTRA_SERVICE_SIZE = "EXTRA_SERVICE_SIZE"
    const val EXTRA_SERVICE_VIBRATION = "EXTRA_SERVICE_VIBRATION"
    const val EXTRA_SERVICE_SOUND = "EXTRA_SERVICE_SOUND"
    const val FILTER = "com.program.axieEnergyCounter.service.OverlayService"
}


enum class OverlaySize{
    SMALL,
    MEDIUM,
    LARGE
}