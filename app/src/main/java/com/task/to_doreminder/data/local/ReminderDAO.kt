package com.task.to_doreminder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE uniqueId = :id LIMIT 1")
    suspend fun getReminderById(id: Long): Reminder?
}