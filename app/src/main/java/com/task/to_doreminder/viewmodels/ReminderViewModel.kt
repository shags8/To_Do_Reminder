package com.task.to_doreminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.task.to_doreminder.data.local.Reminder
import com.task.to_doreminder.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    private val _reminderLiveData = MutableLiveData<Reminder?>()
    val reminderLiveData: LiveData<Reminder?> get() = _reminderLiveData

    fun loadReminders() {
        viewModelScope.launch {
            _reminders.value = repository.getAllReminders()
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
            loadReminders()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            loadReminders()
        }
    }

    fun setReminder(reminder: Reminder) {
        _reminderLiveData.value = reminder
    }

    fun clearReminder() {
        _reminderLiveData.value = null
    }
}


class ReminderViewModelFactory(
    private val repository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(repository) as T
    }
}