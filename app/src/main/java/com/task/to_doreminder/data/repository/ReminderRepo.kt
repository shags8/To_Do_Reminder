package com.task.to_doreminder.data.repository

import com.task.to_doreminder.data.local.Reminder
import com.task.to_doreminder.data.local.ReminderDao
import com.task.to_doreminder.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ReminderRepository(private val reminderDao: ReminderDao) {

    private suspend fun getLocalReminders(): List<Reminder> = withContext(Dispatchers.IO) {
        reminderDao.getAll()
    }

    private suspend fun getApiReminders(): List<Reminder> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.getReminders()
            response.take(10).map { apiItem ->
                Reminder(
                    title = apiItem.title,
                    description = "From API",
                    reminderTime = System.currentTimeMillis() + Random.nextLong(60000, 3600000),
                    recurrence = null,
                    isFromApi = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllReminders(): List<Reminder> = withContext(Dispatchers.IO) {
        val local = getLocalReminders()
        val api = getApiReminders()
        local + api
    }

    suspend fun insertReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        reminderDao.insert(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        reminderDao.delete(reminder)
    }
}