package com.axis.vpn.tools.prankvideocall.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.axis.vpn.tools.prankvideocall.ui.activity.StartActivity

class CallAlarmReceiver : BroadcastReceiver() {

    private val TAG = "CallAlarmReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "Alarm Triggered at ${System.currentTimeMillis()}")

        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null")
            return
        }

        val heroName = intent.getStringExtra("name") ?: ""
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val audioUrl = intent.getStringExtra("audioUrl") ?: ""

        Log.d(TAG, "heroName=$heroName videoUrl=$videoUrl")

        try {
            val launchIntent = Intent(context, StartActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("startFakeCall", true)
                // Alarm already waited the full delay — fragment must ring NOW, not wait again
                putExtra("delay", 0L)
                putExtra("name", heroName)
                putExtra("videoUrl", videoUrl)
                putExtra("audioUrl", audioUrl)
            }

            context.startActivity(launchIntent)
            Log.d(TAG, "StartActivity launched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed : ${e.message}", e)
        }
    }
}