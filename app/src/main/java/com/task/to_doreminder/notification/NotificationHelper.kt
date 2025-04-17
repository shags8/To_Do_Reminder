package com.task.to_doreminder.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.task.to_doreminder.MainActivity
import com.task.to_doreminder.R

object NotificationHelper {

    private const val CHANNEL_ID = "reminder_channel"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleReminder(
        context: Context,
        timeInMillis: Long,
        id: Long,
        title: String,
        description: String,
        recurrence: String?,
        repeatIntervalInMinutes: Int? = null
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("description", description)
            putExtra("id", id)
            recurrence?.let { putExtra("recurrence", it) }
            repeatIntervalInMinutes?.let { putExtra("repeatIntervalInMinutes", it) }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % 100000).toInt().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Allow Exact Alarms")
                    .setMessage("To trigger reminders exactly on time, please allow exact alarms for this app.")
                    .setPositiveButton("Grant") { _, _ ->
                        try {
                            val settingsIntent =
                                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            context.startActivity(settingsIntent)
                            android.widget.Toast.makeText(
                                context,
                                "Please allow exact alarms in settings.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to open settings. Please enable exact alarms manually.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        android.widget.Toast.makeText(
                            context,
                            "Reminder won't be triggered exactly on time.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    .show()
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "reminder_channel",
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Used for reminder alerts"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, title: String, description: String, id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(id, notification)
    }

}
