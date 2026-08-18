package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleCallViewModel : ViewModel() {

    data class CallSchedule(
        val id: String = UUID.randomUUID().toString(),
        val date: String,
        val time: String,
        val ringtone: String,
        val duration: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    private val scheduledCalls = mutableListOf<CallSchedule>()

    fun scheduleCall(
        date: String,
        time: String,
        ringtone: String,
        duration: String
    ) {
        viewModelScope.launch {
            try {
                val callSchedule = CallSchedule(
                    date = date,
                    time = time,
                    ringtone = ringtone,
                    duration = duration
                )

                scheduledCalls.add(callSchedule)

                // TODO: Save to database or API
                // Repository call would go here
                println("Call scheduled: $callSchedule")

                // Set up notification/alarm for the scheduled time
                scheduleNotification(callSchedule)

            } catch (e: Exception) {
                println("Error scheduling call: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNotification(callSchedule: CallSchedule) {
        // TODO: Implement WorkManager or AlarmManager to trigger the call at scheduled time
        try {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US)
            val scheduledDateTime = dateFormat.parse("${callSchedule.date} ${callSchedule.time}")
            val delayInMillis = scheduledDateTime.time - System.currentTimeMillis()

            if (delayInMillis > 0) {
                // Schedule notification/alarm
                println("Notification scheduled for ${callSchedule.time}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getScheduledCalls(): List<CallSchedule> {
        return scheduledCalls.toList()
    }

    fun cancelCall(callId: String) {
        scheduledCalls.removeIf { it.id == callId }
        // TODO: Cancel notification/alarm
    }
}