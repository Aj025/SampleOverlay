package simple.program.sampleoverlay.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import simple.program.sampleoverlay.R
import simple.program.sampleoverlay.service.OverlayService
import simple.program.sampleoverlay.util.Overlay
import simple.program.sampleoverlay.util.WindowLayoutParams

class WindowOverlay(
    private val appContext: Context,
    private val callbacks: Callbacks?
) : Overlay(appContext) {

    private var counter = 3
    private var trashZoneOverlay: TrashZoneOverlay? = null

    private val btnDecrease: ImageView
    private val btnIncrease: ImageView
    private val tvText: TextView
    private val imgBackground: ImageView
    private val btnClear: ImageView
    private val btnEnd: ImageView

    interface Callbacks {
        fun onButtonTapped(centerOfButton: Point?)
        fun onButtonHidden()
    }

    fun getCounter(): Int {
        return counter
    }

    init {
        windowManagerView = View.inflate(appContext, R.layout.overlay_main, null)
        windowManagerView!!.layoutParams = FrameLayout.LayoutParams(0, 0)

        btnIncrease = windowManagerView!!.findViewById(R.id.img_increase)
        btnDecrease = windowManagerView!!.findViewById(R.id.img_decrease)
        imgBackground = windowManagerView!!.findViewById(R.id.img_background)
        btnEnd = windowManagerView!!.findViewById(R.id.img_end)
        btnClear = windowManagerView!!.findViewById(R.id.img_recycle)
        tvText = windowManagerView!!.findViewById(R.id.tv_text)

        trashZoneOverlay = TrashZoneOverlay(appContext)
        setInitialLayoutParams()
        setPositionOnScreen(Point(0, 0)) // TODO needs to work for multiple screen sizes

        btnDecrease.setOnClickListener {
            counter -= 1
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(OverlayService.EXTRA_SERVICE_COUNTER, counter)
            i.action = OverlayService.FILTER
            appContext.sendBroadcast(i)
        }

        btnIncrease.setOnClickListener {
            counter += 1
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(OverlayService.EXTRA_SERVICE_COUNTER, counter)
            i.action = OverlayService.FILTER
            appContext.sendBroadcast(i)
        }

        btnClear.setOnClickListener {
            counter =3
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(OverlayService.EXTRA_SERVICE_COUNTER, counter)
            i.action = OverlayService.FILTER
            appContext.sendBroadcast(i)
        }

        btnEnd.setOnClickListener {
            counter += 2
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(OverlayService.EXTRA_SERVICE_COUNTER, counter)
            i.action = OverlayService.FILTER
            appContext.sendBroadcast(i)
        }
        customTouchListener()
        tvText.text = counter.toString()
        imgBackground.setOnClickListener {
            pressButton()
        }
    }

    fun startRevealAnimation() {
        addViewToWindowManager()
        windowManagerView!!.visibility = View.VISIBLE
    }

    private fun setInitialLayoutParams() {
        layoutParams = WindowLayoutParams.defaultParams
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun customTouchListener() {
        imgBackground.setOnTouchListener(
            object : OnTouchListener {
                var velocityTracker: VelocityTracker? = null
                var initialTouchPos = Point()
                var initialPosition = Point()
                var pressTime: Long? = null
                var dX = 0
                var dY = 0
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    event.setLocation(event.rawX, event.rawY)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker = VelocityTracker.obtain()
                            initialTouchPos[event.x.toInt()] = event.y.toInt()
                            initialPosition[layoutParams!!.x] = layoutParams!!.y
                            pressTime = System.currentTimeMillis()
                            //frame.setAlpha(0.8f);
                            trashZoneOverlay!!.show()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            dX = event.x.toInt() - initialTouchPos.x
                            dY = event.y.toInt() - initialTouchPos.y
                            velocityTracker!!.addMovement(event)
                            val currentPosition =
                                Point(initialPosition.x + dX, initialPosition.y + dY)
                            setPositionOnScreen(currentPosition)
                        }
                        MotionEvent.ACTION_UP -> {
                            trashZoneOverlay!!.hide()
                            //frame.setAlpha(1.0f);
                            velocityTracker!!.computeCurrentVelocity(1)
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity
                            if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5) //startInertiaAnimation(xVelocity, yVelocity);
                                velocityTracker!!.recycle()
                            val timeSincePress = System.currentTimeMillis() - pressTime!!
                            if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                                Log.d("test", "you click me")
                                pressButton()
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })

        btnEnd.setOnTouchListener(
            object : OnTouchListener {
                var velocityTracker: VelocityTracker? = null
                var initialTouchPos = Point()
                var initialPosition = Point()
                var pressTime: Long? = null
                var dX = 0
                var dY = 0
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    event.setLocation(event.rawX, event.rawY)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker = VelocityTracker.obtain()
                            initialTouchPos[event.x.toInt()] = event.y.toInt()
                            initialPosition[layoutParams!!.x] = layoutParams!!.y
                            pressTime = System.currentTimeMillis()
                            //frame.setAlpha(0.8f);
                            trashZoneOverlay!!.show()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            dX = event.x.toInt() - initialTouchPos.x
                            dY = event.y.toInt() - initialTouchPos.y
                            velocityTracker!!.addMovement(event)
                            val currentPosition =
                                Point(initialPosition.x + dX, initialPosition.y + dY)
                            setPositionOnScreen(currentPosition)
                        }
                        MotionEvent.ACTION_UP -> {
                            trashZoneOverlay!!.hide()
                            //frame.setAlpha(1.0f);
                            velocityTracker!!.computeCurrentVelocity(1)
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity
                            if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5) //startInertiaAnimation(xVelocity, yVelocity);
                                velocityTracker!!.recycle()
                            val timeSincePress = System.currentTimeMillis() - pressTime!!
                            if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })

        btnClear.setOnTouchListener(
            object : OnTouchListener {
                var velocityTracker: VelocityTracker? = null
                var initialTouchPos = Point()
                var initialPosition = Point()
                var pressTime: Long? = null
                var dX = 0
                var dY = 0
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    event.setLocation(event.rawX, event.rawY)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker = VelocityTracker.obtain()
                            initialTouchPos[event.x.toInt()] = event.y.toInt()
                            initialPosition[layoutParams!!.x] = layoutParams!!.y
                            pressTime = System.currentTimeMillis()
                            //frame.setAlpha(0.8f);
                            trashZoneOverlay!!.show()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            dX = event.x.toInt() - initialTouchPos.x
                            dY = event.y.toInt() - initialTouchPos.y
                            velocityTracker!!.addMovement(event)
                            val currentPosition =
                                Point(initialPosition.x + dX, initialPosition.y + dY)
                            setPositionOnScreen(currentPosition)
                        }
                        MotionEvent.ACTION_UP -> {
                            trashZoneOverlay!!.hide()
                            //frame.setAlpha(1.0f);
                            velocityTracker!!.computeCurrentVelocity(1)
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity
                            if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5) //startInertiaAnimation(xVelocity, yVelocity);
                                velocityTracker!!.recycle()
                            val timeSincePress = System.currentTimeMillis() - pressTime!!
                            if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })

        btnIncrease.setOnTouchListener(
            object : OnTouchListener {
                var velocityTracker: VelocityTracker? = null
                var initialTouchPos = Point()
                var initialPosition = Point()
                var pressTime: Long? = null
                var dX = 0
                var dY = 0
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    event.setLocation(event.rawX, event.rawY)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker = VelocityTracker.obtain()
                            initialTouchPos[event.x.toInt()] = event.y.toInt()
                            initialPosition[layoutParams!!.x] = layoutParams!!.y
                            pressTime = System.currentTimeMillis()
                            //frame.setAlpha(0.8f);
                            trashZoneOverlay!!.show()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            dX = event.x.toInt() - initialTouchPos.x
                            dY = event.y.toInt() - initialTouchPos.y
                            velocityTracker!!.addMovement(event)
                            val currentPosition =
                                Point(initialPosition.x + dX, initialPosition.y + dY)
                            setPositionOnScreen(currentPosition)
                        }
                        MotionEvent.ACTION_UP -> {
                            trashZoneOverlay!!.hide()
                            //frame.setAlpha(1.0f);
                            velocityTracker!!.computeCurrentVelocity(1)
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity
                            if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5) //startInertiaAnimation(xVelocity, yVelocity);
                                velocityTracker!!.recycle()
                            val timeSincePress = System.currentTimeMillis() - pressTime!!
                            if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })

        btnDecrease.setOnTouchListener(
            object : OnTouchListener {
                var velocityTracker: VelocityTracker? = null
                var initialTouchPos = Point()
                var initialPosition = Point()
                var pressTime: Long? = null
                var dX = 0
                var dY = 0
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    event.setLocation(event.rawX, event.rawY)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker = VelocityTracker.obtain()
                            initialTouchPos[event.x.toInt()] = event.y.toInt()
                            initialPosition[layoutParams!!.x] = layoutParams!!.y
                            pressTime = System.currentTimeMillis()
                            //frame.setAlpha(0.8f);
                            trashZoneOverlay!!.show()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            dX = event.x.toInt() - initialTouchPos.x
                            dY = event.y.toInt() - initialTouchPos.y
                            velocityTracker!!.addMovement(event)
                            val currentPosition =
                                Point(initialPosition.x + dX, initialPosition.y + dY)
                            setPositionOnScreen(currentPosition)
                        }
                        MotionEvent.ACTION_UP -> {
                            trashZoneOverlay!!.hide()
                            //frame.setAlpha(1.0f);
                            velocityTracker!!.computeCurrentVelocity(1)
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity
                            if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5) //startInertiaAnimation(xVelocity, yVelocity);
                                velocityTracker!!.recycle()
                            val timeSincePress = System.currentTimeMillis() - pressTime!!
                            if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })
    }

    private fun pressButton() {
        callbacks?.onButtonTapped(getWindowCenterPoint())
    }

    fun hide() {
        removeViewFromWindowManager()
        windowManagerView!!.visibility = View.GONE
        callbacks!!.onButtonHidden()
    }

    private fun getWindowCenterPoint(): Point? {
        val currentPoint = Point(layoutParams!!.x, layoutParams!!.y)
        currentPoint.offset(windowManagerView!!.width / 2, windowManagerView!!.height / 2)
        return currentPoint
    }

    private fun isInTrashZone(positionY: Int): Boolean {
        val ZONE_HEIGHT = 200
        return displayInfo.screenHeight - ZONE_HEIGHT < positionY
    }

    private fun buttonMoved(dx: Int, dy: Int): Boolean {
        return 4 < Math.abs(dx) ||
                4 < Math.abs(dy)
    }

    override fun setPositionOnScreen(point: Point) {
        super.setPositionOnScreen(moveViewIntoScreenBounds(point) ?: Point(0, 0))
    }

    private fun moveViewIntoScreenBounds(point: Point): Point? {
        val Y_MAX: Int = displayInfo.screenHeight - windowManagerView!!.height
        val X_MAX: Int = displayInfo.screenWidth - windowManagerView!!.width

        // Adjust x
        if (point.x < 0) point.x = 0
        if (point.x > X_MAX) point.x = X_MAX

        // Adjust Y
        if (point.y < 0) point.y = 0
        if (point.y > Y_MAX) point.y = Y_MAX
        return point
    }

    fun flipXY() {
        displayInfo.update()
        val temp: Int = layoutParams!!.y
        layoutParams!!.y = layoutParams!!.x
        layoutParams!!.x = temp
        if (isAddedToWindowManager) {
            removeViewFromWindowManager()
            addViewToWindowManager()
        }
    }
}