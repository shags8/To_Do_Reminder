package com.task.to_doreminder.data.remote

data class ApiReminder(
    val id: Int,
    val title: String,
    val completed: Boolean,
    val userId: Int,
    val reminderTime: Long
)