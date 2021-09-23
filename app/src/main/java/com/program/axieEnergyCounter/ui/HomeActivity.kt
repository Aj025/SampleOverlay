package com.program.axieEnergyCounter.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.program.axieEnergyCounter.Constant
import com.program.axieEnergyCounter.Constant.COUNTER
import com.program.axieEnergyCounter.R
import com.program.axieEnergyCounter.service.OverlayService

class HomeActivity : AppCompatActivity() {

    private var serviceIntent: Intent? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var enableButton: Button? = null
    private var stopButton: Button? = null
    private var textView: AppCompatTextView? = null
    private var textService: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        enableButton = findViewById(R.id.btn_start)
        stopButton = findViewById(R.id.btn_stop)
        textView = findViewById(R.id.tv_test)
        textService = findViewById(R.id.tv_label)

        var currentCounter =Constant.STARTING_COUNT

        if (OverlayService.getInstance() != null) {
            currentCounter = OverlayService.getInstance()?.overlayManager?.getCounter() ?: Constant.STARTING_COUNT
            Log.d("TEST_SOMETHING", currentCounter.toString())
        }

        textView?.text = "$currentCounter : Current Counter"

        val isRunning =
            intent.getBooleanExtra(Constant.EXTRA_SERVICE_RUNNING, false)
        if (isRunning) {
            attemptToStartService()
            textService?.text = getString(R.string.is_running_service)
        } else {
            enableButton()
            textService?.text = getString(R.string.is_not_running_service)
        }

        enableButton?.setOnClickListener {
            attemptToStartService()
        }
        stopButton?.setOnClickListener {
            if (serviceIntent != null) stopService(serviceIntent)
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val serviceDestroyed =
                    intent.getBooleanExtra(Constant.EXTRA_SERVICE_DESTROYED, false)
                if (serviceDestroyed) {
                    enableButton()
                    textService?.text = getString(R.string.is_not_running_service)
                }

                val currentCounter = intent.getIntExtra(Constant.EXTRA_SERVICE_COUNTER, Constant.STARTING_COUNT)
                textView?.text = "$currentCounter : Current Counter"
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val currentCounter = intent?.extras?.get(COUNTER)
        currentCounter?.let {
            Log.d("TEST_SOMETHING", "onNewIntent $it")
            textView?.text = "$it : Current Counter"
            textService?.text = getString(R.string.is_running_service)
        } ?: run {
            Log.d("TEST_SOMETHING", "onNewIntent Null")
        }

    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Constant.FILTER)
        this.registerReceiver(broadcastReceiver, intentFilter)
        resetEnableButton()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    private fun attemptToStartService() {
        if (!Settings.canDrawOverlays(this)) {
            askForSystemOverlayPermission()
        } else {
            serviceIntent = OverlayService.getIntent(this@HomeActivity)
            startService(serviceIntent)
            disableButton()
            textService?.text = getString(R.string.is_running_service)
        }
    }


    var permissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Constant.REQUEST_OVERLAY_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        //Permission is not available. Display error text.
                        errorToast()
                    }
                }
            }
        }

    private fun askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            permissionLauncher.launch((intent))
        }
    }


    override fun onDestroy() {
        super.onDestroy()

    }


    private fun errorToast() {
        Toast.makeText(
            this,
            "Draw over other app permission not available. Can't start the application without the permission.",
            Toast.LENGTH_LONG
        ).show()
    }


    private fun resetEnableButton() {
        if (OverlayService.getInstance() == null) {
            enableButton()
        }
    }

    private fun enableButton() {
        enableButton!!.isEnabled = true
    }

    private fun disableButton() {
        enableButton!!.isEnabled = false
    }
}