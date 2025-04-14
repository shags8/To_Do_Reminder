package com.task.to_doreminder.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.task.to_doreminder.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getLongExtra("id", -1L) ?: -1L
        if (id == -1L) return

        val title = intent?.getStringExtra("title") ?: "Reminder"
        val description = intent?.getStringExtra("description") ?: ""
        val recurrence = intent?.getStringExtra("recurrence")

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val reminder = db.reminderDao().getReminderById(id)

            if (reminder != null) {
                NotificationHelper.showNotification(context, title, description, id.toInt())

                val nextTime = when (recurrence) {
                    "Minutes" -> System.currentTimeMillis() + 60000L
                    "Hourly" -> System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR
                    "Daily" -> System.currentTimeMillis() + AlarmManager.INTERVAL_DAY
                    else -> null
                }

                nextTime?.let {
                    NotificationHelper.scheduleReminder(
                        context,
                        it,
                        id,
                        title,
                        description,
                        recurrence
                    )
                }
            }
        }
    }
}
