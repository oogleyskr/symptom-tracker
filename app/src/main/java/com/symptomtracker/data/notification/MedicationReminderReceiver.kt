package com.symptomtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.symptomtracker.MainActivity
import com.symptomtracker.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra(EXTRA_MED_NAME) ?: return
        val dose = intent.getStringExtra(EXTRA_DOSE) ?: ""
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Reminders to take your medications" }
        notificationManager.createNotificationChannel(channel)

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for $medicationName")
            .setContentText(if (dose.isNotBlank()) "Dose: $dose" else "Tap to log as taken")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicationName.hashCode(), notification)

        // Reschedule for tomorrow (daily repeating)
        if (reminderId > 0) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val reminder = db.medicationReminderDao().getAll()
                        .find { it.id == reminderId && it.enabled }
                    if (reminder != null) {
                        AlarmScheduler(context).schedule(reminder)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val EXTRA_MED_NAME = "med_name"
        const val EXTRA_DOSE = "dose"
    }
}
