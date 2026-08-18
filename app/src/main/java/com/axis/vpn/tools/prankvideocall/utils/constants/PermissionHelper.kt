package com.axis.vpn.tools.prankvideocall.utils.constants
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat


object PermissionHelper {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun canScheduleExactAlarm(context: Context): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }


    /**
     * Request Camera Permission List
     */
    fun getRequiredPermissions(): Array<String> {

        return arrayOf(
            Manifest.permission.CAMERA
        )
    }


    /**
     * Check All Required Permissions
     */
    fun allPermissionsGranted(context: Context): Boolean {


        val cameraGranted =
            hasCameraPermission(context)


        val alarmGranted =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                canScheduleExactAlarm(context)
            } else {
                true
            }


        return cameraGranted && alarmGranted
    }
}