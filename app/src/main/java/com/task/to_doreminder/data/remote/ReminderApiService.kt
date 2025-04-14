package com.task.to_doreminder.data.remote

import retrofit2.http.GET

interface ReminderApiService {
    @GET("todos")
    suspend fun getReminders(): List<ApiReminder>
}