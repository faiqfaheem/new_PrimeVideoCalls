package com.axis.vpn.tools.prankvideocall.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.axis.vpn.tools.prankvideocall.utils.receivers.CallAlarmReceiver
import java.util.Calendar
class CallAlarmManager(private val context: Context) {

    private val TAG = "CallAlarmManager"

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val PREFS = "call_alarm_prefs"
        private const val KEY_REQUEST_CODE = "last_request_code"
    }

    private fun generateRequestCode(): Int {
        val code = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_REQUEST_CODE, code)
            .apply()
        return code
    }

    private fun getLastRequestCode(): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_REQUEST_CODE, -1)
    }

    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleCall(
        delayInMillis: Long,
        heroName: String,
        videoUrl: String,
        audioUrl: String?
    ): Boolean {

        val triggerAtMillis = System.currentTimeMillis() + delayInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Exact alarm permission NOT granted — cannot schedule")
                val permissionIntent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(permissionIntent)
                return false
            }
        }

        // Cancel any previous pending call before scheduling a new one
        cancelCall()

        val requestCode = generateRequestCode()

        val intent = Intent(context, CallAlarmReceiver::class.java).apply {
            putExtra("delay", delayInMillis)
            putExtra("name", heroName)
            putExtra("videoUrl", videoUrl)
            putExtra("audioUrl", audioUrl)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d(
            TAG,
            "Alarm scheduled: requestCode=$requestCode trigger=$triggerAtMillis " +
                    "(in ${delayInMillis}ms) now=${System.currentTimeMillis()}"
        )

        return true
    }

    fun scheduleCallAtTime(
        hour: Int,
        minute: Int,
        heroName: String,
        videoUrl: String,
        audioUrl: String
    ): Boolean {

        val currentTime = Calendar.getInstance()

        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var delayInMillis = scheduledTime.timeInMillis - currentTime.timeInMillis

        if (delayInMillis < 0) {
            delayInMillis += 24 * 60 * 60 * 1000L
        }

        Log.d(TAG, "scheduleCallAtTime: hour=$hour minute=$minute -> delayInMillis=$delayInMillis")

        return scheduleCall(delayInMillis, heroName, videoUrl, audioUrl)
    }

    fun cancelCall() {
        val requestCode = getLastRequestCode()
        if (requestCode == -1) return

        val intent = Intent(context, CallAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun hasPendingAlarm(): Boolean {
        val requestCode = getLastRequestCode()
        if (requestCode == -1) return false

        val intent = Intent(context, CallAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}