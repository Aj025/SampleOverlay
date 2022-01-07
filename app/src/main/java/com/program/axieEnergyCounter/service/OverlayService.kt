package com.program.axieEnergyCounter.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.program.axieEnergyCounter.Constant
import com.program.axieEnergyCounter.Constant.EXTRA_SERVICE_DESTROYED
import com.program.axieEnergyCounter.Constant.FILTER
import com.program.axieEnergyCounter.OverlaySize
import com.program.axieEnergyCounter.R
import com.program.axieEnergyCounter.ui.HomeActivity
import com.program.axieEnergyCounter.ui.overlay.OverlayManager
import com.program.axieEnergyCounter.util.getEnumExtra

class OverlayService : Service() {

    companion object {
        private var overlayService: OverlayService? = null

        fun getInstance(): OverlayService? {
            return if (overlayService != null) {
                overlayService
            } else {
                null
            }
        }

        fun getIntent(context: Context?): Intent? {
            return Intent(context, OverlayService::class.java)
        }
    }

    var overlayManager: OverlayManager? = null
    var alpha = 255
    var size = 1
    var hasVibration = true
    var hasSound = true

    private var isRunning = false


    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground()
        else
            startForeground(
                1,
                Notification()
            )

        overlayManager = OverlayManager(this)
        overlayService = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alpha = intent?.getIntExtra(Constant.EXTRA_SERVICE_ALPHA, 255) ?: 255
        size = intent?.getIntExtra(Constant.EXTRA_SERVICE_SIZE, 2) ?: 1
        hasVibration = intent?.getBooleanExtra(Constant.EXTRA_SERVICE_VIBRATION, true) ?: true
        hasSound =  intent?.getBooleanExtra(Constant.EXTRA_SERVICE_SOUND, true) ?: true
        Log.d("TEST_SIZE", "From Service " + size.toString())
        if (!isRunning) {
            overlayManager?.showButton()
            overlayManager?.setAlpha(alpha)
            overlayManager?.setVibration(hasVibration)
            overlayManager?.setSounds(hasSound)
            isRunning = true

        } else {
            overlayManager?.setAlpha(alpha)
            overlayManager?.setSize(OverlaySize.values()[size])
            overlayManager?.setVibration(hasVibration)
            overlayManager?.setSounds(hasSound)
        }
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        overlayService = null
        overlayManager?.removeAll()
        overlayManager = null
        broadcastThisServiceIsDestroyed()
        super.onDestroy()
    }

    private fun broadcastThisServiceIsDestroyed() {
        val i = Intent()
        isRunning = false
        i.putExtra(EXTRA_SERVICE_DESTROYED, true)
        i.action = FILTER
        sendBroadcast(i)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = " simple.program.sampleoverlay"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val intent = Intent(this, HomeActivity::class.java)
        Log.d("TEST_SERVICE", overlayManager?.getCounter().toString())
        intent.putExtra(Constant.COUNTER, overlayManager?.getCounter())
        intent.putExtra(Constant.EXTRA_SERVICE_RUNNING, true)

        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Service running")
            .setContentText("Displaying over other apps") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        startForeground(2, notification)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("OverlayService", "landscape")
            overlayManager?.toggleOrientationChange()
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("OverlayService", "portrait")
        }
    }
}






