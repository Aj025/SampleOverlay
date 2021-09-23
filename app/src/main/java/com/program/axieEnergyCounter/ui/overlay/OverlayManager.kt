package com.program.axieEnergyCounter.ui.overlay

import android.content.Intent
import android.graphics.Point
import com.program.axieEnergyCounter.Constant
import com.program.axieEnergyCounter.service.OverlayService
import com.program.axieEnergyCounter.ui.HomeActivity


class OverlayManager(
    private val service: OverlayService
) : WindowOverlay.Callbacks{

    private var mainOverlay: WindowOverlay = WindowOverlay(service, this)

    fun showButton() {
        mainOverlay.startRevealAnimation()
    }

    fun removeAll() {
        mainOverlay.hide()
    }

    override fun onButtonTapped(centerOfButton: Point?) {
        val intent = Intent(service, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Constant.COUNTER, mainOverlay.getCounter())
        intent.putExtra(Constant.EXTRA_SERVICE_RUNNING, true)
        service.startActivity(intent)
    }

    fun getCounter() : Int {
       return mainOverlay.getCounter()
    }

    override fun onButtonHidden() {
        service.stopSelf()
    }

    fun toggleOrientationChange() {
        mainOverlay.flipXY()
    }

    fun setPortraitOrientation() {
        mainOverlay.flipXY()
    }
}