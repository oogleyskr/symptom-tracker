package com.symptomtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.symptomtracker.MainActivity

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra(EXTRA_MED_NAME) ?: return
        val dose = intent.getStringExtra(EXTRA_DOSE) ?: ""

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Reminders to take your medications" }
        notificationManager.createNotificationChannel(channel)

        // Open app intent
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
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val EXTRA_MED_NAME = "med_name"
        const val EXTRA_DOSE = "dose"
    }
}
