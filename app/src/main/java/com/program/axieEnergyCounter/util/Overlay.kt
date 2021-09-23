package com.program.axieEnergyCounter.util

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager


abstract class Overlay(appContext: Context) {
    private var windowManager: WindowManager
    var layoutParams: WindowManager.LayoutParams? = null
    var windowManagerView: View? = null
    var displayInfo: DisplayInfo
    var isAddedToWindowManager = false

    fun removeViewFromWindowManager() {
        try {
            windowManager.removeView(windowManagerView)
            isAddedToWindowManager = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addViewToWindowManager() {
        try {
            windowManager.addView(windowManagerView, layoutParams)
            isAddedToWindowManager = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun setPositionOnScreen(point: Point) {
        layoutParams!!.x = point.x
        layoutParams!!.y = point.y
        if (isAddedToWindowManager) windowManager.updateViewLayout(windowManagerView, layoutParams)
    }

    init {
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        displayInfo = DisplayInfo(appContext)
    }
}
