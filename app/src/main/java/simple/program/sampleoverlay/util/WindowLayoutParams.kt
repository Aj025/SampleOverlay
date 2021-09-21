package simple.program.sampleoverlay.util

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager


object WindowLayoutParams {
    @get:SuppressLint("RtlHardcoded")
    val defaultParams: WindowManager.LayoutParams
        get() {
            val params = WindowManager.LayoutParams()
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.type = windowLayoutType
            params.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            params.format = PixelFormat.TRANSPARENT
            params.gravity = Gravity.TOP or Gravity.LEFT
            return params
        }
    private val windowLayoutType: Int
        private get() {
            return if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }
}
