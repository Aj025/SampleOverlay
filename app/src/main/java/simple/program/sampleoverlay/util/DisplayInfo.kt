package simple.program.sampleoverlay.util

import android.app.Service
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt


class DisplayInfo(private val context: Context) {
    var screenHeight = 0
        private set
    var screenWidth = 0
        private set
    var screenDiagonal = 0
        private set
    var navBarHeight = 0
        private set
    var statusBarHeight = 0
        private set

    fun update() {
        setScreenHeight()
        screenWidth = displayMetrics.widthPixels
        screenDiagonal = sqrt(
            screenHeight.toDouble().pow(2.0) + screenWidth.toDouble().pow(2.0)
        ).toInt()
        setNavBarHeight()
        setStatusBarHeight()
    }

    private fun setStatusBarHeight() {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        statusBarHeight = if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun setScreenHeight() {
        screenHeight = displayMetrics.heightPixels
    }

    private fun setNavBarHeight() {
        val resources = context.resources
        val resourceId = resources.getIdentifier(
            "navigation_bar_height",
            "dimen", "android"
        )
        navBarHeight = if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            Log.i("TAG", "NavBarHeight returned 0")
            0
        }
    }

    private val displayMetrics: DisplayMetrics
        private get() {
            val wManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Will not work on os R
//                val display = context.display
//                val dMetrics = DisplayMetrics()
//                Resources.getSystem().getDisplayMetrics()
//                display?.getRealMetrics(dMetrics)
//                dMetrics
                val metrics = Resources.getSystem().displayMetrics
                metrics
            } else {
                val dMetrics = DisplayMetrics()
                wManager.defaultDisplay.getMetrics(dMetrics)
                dMetrics
            }
        }


    companion object {
        fun getCenterShiftedPoint(v: View, centerPoint: Point): Point {
            val width = v.layoutParams.width
            val height = v.layoutParams.height
            val shiftedPoint = Point()
            shiftedPoint.x = centerPoint.x - width / 2
            shiftedPoint.y = centerPoint.y - height / 2
            return shiftedPoint
        }

        fun convertDpToPixel(dp: Float): Int {
            val metrics = Resources.getSystem().displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return Math.round(px)
        }

        fun convertPixelToDp(px: Float): Int {
            val metrics = Resources.getSystem().displayMetrics
            val dp = 160f * px / metrics.densityDpi
            return Math.round(dp)
        }
    }

    // non-private methods
    init {
        update()
    }
}