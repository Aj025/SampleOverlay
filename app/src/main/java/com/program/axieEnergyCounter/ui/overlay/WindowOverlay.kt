package com.program.axieEnergyCounter.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.program.axieEnergyCounter.Constant
import com.program.axieEnergyCounter.Constant.BUTTON_PRESSED_INTERVAL
import com.program.axieEnergyCounter.OverlaySize
import com.program.axieEnergyCounter.util.Overlay
import com.program.axieEnergyCounter.util.WindowLayoutParams
import com.program.axieEnergyCounter.util.vibratePhone

import android.media.MediaPlayer
import com.program.axieEnergyCounter.R


class WindowOverlay(
    private val appContext: Context,
    private val callbacks: Callbacks?
) : Overlay(appContext) {

    private var counter = Constant.STARTING_COUNT
    private var trashZoneOverlay: TrashZoneOverlay? = null


    private val overlayParent: ConstraintLayout
    private val btnDecrease: ImageView
    private val btnIncrease: ImageView
    private val tvText: TextView
    private val tvRound: TextView
    private val tvIncrease: TextView
    private val tvDecrease: TextView
    private val imgBackground: ImageView
    private val btnClear: ImageView
    private val btnEnd: ImageView

    private val mp: MediaPlayer

    private var currentRound = 1

    private var lastClear = 0L
    private var lastEnd = 0L
    private var lastIncrease = 0L
    private var lastDecrease = 0L

    private var buttonPressIncreaseCount = 0
    private var buttonPressDecreaseCount = 0

    private var hasVibration = true
    private var hasSound = true

    interface Callbacks {
        fun onButtonTapped(centerOfButton: Point?)
        fun onButtonHidden()
    }

    fun getCounter(): Int {
        return counter
    }

    init {
        windowManagerView = View.inflate(appContext, R.layout.overlay_main_v2, null)
        windowManagerView!!.layoutParams = FrameLayout.LayoutParams(0, 0)


        overlayParent = windowManagerView!!.findViewById(R.id.parent_overlay)
        btnIncrease = windowManagerView!!.findViewById(R.id.img_increase)
        btnDecrease = windowManagerView!!.findViewById(R.id.img_decrease)
        imgBackground = windowManagerView!!.findViewById(R.id.img_background)
        btnEnd = windowManagerView!!.findViewById(R.id.img_end)
        btnClear = windowManagerView!!.findViewById(R.id.img_recycle)
        tvText = windowManagerView!!.findViewById(R.id.tv_text)
        tvRound = windowManagerView!!.findViewById(R.id.tv_round)
        tvIncrease = windowManagerView!!.findViewById(R.id.tv_increase)
        tvDecrease = windowManagerView!!.findViewById(R.id.tv_decrease)

        trashZoneOverlay = TrashZoneOverlay(appContext)
        setInitialLayoutParams()
        setPositionOnScreen(Point(0, 0))


        mp = MediaPlayer.create(appContext, R.raw.button_click)

        customTouchListener()
        tvText.text = counter.toString()
        tvRound.text = appContext.getString(R.string.current_round, currentRound)

        imgBackground.setOnClickListener {
            playButtonClickSound()
            pressButton()
        }
        btnDecrease.setOnClickListener {
            if (!checkLastPress(lastDecrease))
                return@setOnClickListener
            lastDecrease = System.currentTimeMillis()
            if (counter > 0) {
                counter -= 1
                buttonPressDecreaseCount -= 1
                tvDecrease.text = buttonPressDecreaseCount.toString()
            }

            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(Constant.EXTRA_SERVICE_COUNTER, counter)
            i.action = Constant.FILTER
            doVibrate()
            playButtonClickSound()
            appContext.sendBroadcast(i)
        }

        btnIncrease.setOnClickListener {
            if (!checkLastPress(lastIncrease))
                return@setOnClickListener
            lastIncrease = System.currentTimeMillis()
            if (counter < 10) {
                counter += 1
                buttonPressIncreaseCount += 1
                tvIncrease.text = "+$buttonPressIncreaseCount"
            }
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(Constant.EXTRA_SERVICE_COUNTER, counter)
            i.action = Constant.FILTER
            doVibrate()
            playButtonClickSound()
            appContext.sendBroadcast(i)
        }

        btnClear.setOnClickListener {
            if (!checkLastPress(lastClear))
                return@setOnClickListener
            resetIndicators()
            currentRound = 1
            tvRound.text = appContext.getString(R.string.current_round, currentRound)
            lastClear = System.currentTimeMillis()
            counter = Constant.STARTING_COUNT
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(Constant.EXTRA_SERVICE_COUNTER, counter)
            i.action = Constant.FILTER
            doVibrate()
            playButtonClickSound()
            appContext.sendBroadcast(i)
        }

        btnEnd.setOnClickListener {
            if (!checkLastPress(lastEnd))
                return@setOnClickListener
            resetIndicators()
            currentRound += 1
            tvRound.text = appContext.getString(R.string.current_round, currentRound)
            lastEnd = System.currentTimeMillis()
            if (counter + 2 <= 10)
                counter += 2
            else
                counter = 10
            tvText.text = counter.toString()
            val i = Intent()
            i.putExtra(Constant.EXTRA_SERVICE_COUNTER, counter)
            i.action = Constant.FILTER
            doVibrate()
            playButtonClickSound()
            appContext.sendBroadcast(i)
        }
    }

    private fun checkLastPress(lastPressed: Long): Boolean {
        val timeSincePress = System.currentTimeMillis() - lastPressed
        if (timeSincePress < BUTTON_PRESSED_INTERVAL)
            return false
        return true
    }

    private fun resetIndicators(){
        buttonPressIncreaseCount = 0
        tvIncrease.text = buttonPressIncreaseCount.toString()
        buttonPressDecreaseCount = 0
        tvDecrease.text = buttonPressDecreaseCount.toString()
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
                                btnEnd.performClick()
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
                                btnClear.performClick()
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
                                btnIncrease.performClick()
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
                                btnDecrease.performClick()
                                return false
                            }
                            if (isInTrashZone(event.rawY.toInt())) hide()
                        }
                    }
                    return false
                }
            })
    }

    fun setAlpha(value: Int) {
        btnDecrease.imageAlpha = value
        btnIncrease.imageAlpha = value
        imgBackground.imageAlpha = value
        btnClear.background.alpha = value
        btnEnd.background.alpha = value
    }

    fun setSize(size : OverlaySize) {

        when (size) {
            OverlaySize.SMALL -> {
                overlayParent.layoutParams =
                    RelativeLayout.LayoutParams(
                        appContext.resources.getDimension(R.dimen.layout_small_width).toInt(),
                        appContext.resources.getDimension(R.dimen.layout_small_height).toInt()
                    )
            }
            OverlaySize.MEDIUM -> {
                overlayParent.layoutParams =
                    RelativeLayout.LayoutParams(
                        appContext.resources.getDimension(R.dimen.layout_medium_width).toInt(),
                        appContext.resources.getDimension(R.dimen.layout_medium_height).toInt()
                    )
            }
            OverlaySize.LARGE -> {
                overlayParent.layoutParams =
                    RelativeLayout.LayoutParams(
                        appContext.resources.getDimension(R.dimen.layout_large_width).toInt(),
                        appContext.resources.getDimension(R.dimen.layout_large_height).toInt()
                    )
            }
        }
    }

    private fun doVibrate() {
        if (!hasVibration)
            return
        appContext.vibratePhone()
    }

    private fun playButtonClickSound() {
        if (!hasSound)
            return
        if (mp.isPlaying)
            mp.stop()
        mp.start()
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

    fun setVibration(value: Boolean) {
        hasVibration = value
    }
    fun setSound(value: Boolean) {
        hasSound = value
    }
}