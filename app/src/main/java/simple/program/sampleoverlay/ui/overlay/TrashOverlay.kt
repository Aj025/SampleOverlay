package simple.program.sampleoverlay.ui.overlay

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import simple.program.sampleoverlay.R
import simple.program.sampleoverlay.util.DisplayInfo
import simple.program.sampleoverlay.util.Overlay
import simple.program.sampleoverlay.util.WindowLayoutParams

class TrashZoneOverlay(appContext: Context) : Overlay(appContext) {

    private fun setupWindow(appContext: Context) {
        val displayInfo = DisplayInfo(appContext)
        windowManagerView = View.inflate(appContext, R.layout.overlay_trash, null)
        windowManagerView?.layoutParams = RelativeLayout.LayoutParams(0, 0)
        layoutParams = WindowLayoutParams.defaultParams
        layoutParams?.width = displayInfo.screenWidth
        layoutParams?.y =
            displayInfo.navBarHeight + displayInfo.screenHeight - appContext.resources.getDimension(
                R.dimen.trash_zone_height
            )
                .toInt()
        windowManagerView?.visibility = View.INVISIBLE
    }

    fun show() {
        if (View.INVISIBLE == windowManagerView?.visibility) {
            addViewToWindowManager()
            windowManagerView?.visibility = View.VISIBLE
        }
    }

    fun hide() {
        windowManagerView!!.visibility = View.INVISIBLE
        removeViewFromWindowManager()
    }

    init {
        setupWindow(appContext)
    }
}
