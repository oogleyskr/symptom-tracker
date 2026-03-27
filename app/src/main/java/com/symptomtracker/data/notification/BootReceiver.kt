package com.symptomtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symptomtracker.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val reminders = db.medicationReminderDao().getAll()
                val scheduler = AlarmScheduler(context)
                scheduler.rescheduleAll(reminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
