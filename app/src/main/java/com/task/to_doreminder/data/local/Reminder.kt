package com.task.to_doreminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uniqueId : Long = -1L,
    val title: String,
    val description: String,
    val reminderTime: Long,
    val recurrence: String?,
    val isFromApi: Boolean = false
)