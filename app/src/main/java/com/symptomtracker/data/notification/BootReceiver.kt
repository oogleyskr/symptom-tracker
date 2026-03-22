package com.symptomtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Re-registers alarms after device reboot
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: re-schedule all medication reminders from DB
        }
    }
}
