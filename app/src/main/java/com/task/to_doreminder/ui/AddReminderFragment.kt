package com.task.to_doreminder.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.task.to_doreminder.R
import com.task.to_doreminder.data.local.AppDatabase
import com.task.to_doreminder.data.local.Reminder
import com.task.to_doreminder.data.repository.ReminderRepository
import com.task.to_doreminder.databinding.FragmentAddReminderBinding
import com.task.to_doreminder.notification.NotificationHelper
import com.task.to_doreminder.viewmodels.ReminderViewModel
import com.task.to_doreminder.viewmodels.ReminderViewModelFactory
import java.util.Calendar

class AddReminderFragment : Fragment() {

    private var _binding: FragmentAddReminderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReminderViewModel by activityViewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ReminderViewModelFactory(ReminderRepository(db.reminderDao()))
    }
    private var selectedTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecurrenceSpinner()
        observeReminderData()
        binding.dateTimeText.setOnClickListener { pickDateTime() }

        binding.saveReminderButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val desc = binding.descriptionEditText.text.toString()
            val recurrence =
                binding.recurrenceSpinner.selectedItem.toString().takeIf { it != "None" }

            if (title.isNotEmpty() && selectedTime > 0L) {
                val existingReminder = viewModel.reminderLiveData.value
                if (selectedTime <= System.currentTimeMillis()) {
                    Toast.makeText(
                        requireContext(),
                        "The reminder time must be in the future. Please select a valid time.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val reminder = Reminder(
                    id = existingReminder?.id ?: 0,
                    uniqueId = existingReminder?.uniqueId ?: System.currentTimeMillis(),
                    title = title,
                    description = desc,
                    reminderTime = selectedTime,
                    recurrence = recurrence,
                    isFromApi = false
                )

                viewModel.addReminder(reminder)
                NotificationHelper.scheduleReminder(
                    requireContext(),
                    selectedTime,
                    reminder.uniqueId,
                    title,
                    desc,
                    recurrence
                )
                viewModel.clearReminder()
                findNavController().navigateUp()
            } else {
                binding.titleEditText.error = "Title required"
            }
        }
    }

    private fun observeReminderData() {
        viewModel.reminderLiveData.observe(viewLifecycleOwner) { reminder ->
            reminder?.let {
                binding.titleEditText.setText(it.title)
                binding.descriptionEditText.setText(it.description)
                selectedTime = it.reminderTime

                val formattedTime = DateFormat.format("dd MMM yyyy, hh:mm a", it.reminderTime)
                binding.dateTimeText.text = formattedTime

                val options = resources.getStringArray(R.array.recurrence_options)
                val index = options.indexOf(it.recurrence ?: "None")
                if (index != -1) binding.recurrenceSpinner.setSelection(index)
            }
        }
    }

    private fun setupRecurrenceSpinner() {
        val options = listOf("None", "Minutes", "Hourly", "Daily")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.recurrenceSpinner.adapter = adapter
    }

    private fun pickDateTime() {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute)
                }
                selectedTime = selected.timeInMillis
                binding.dateTimeText.text = DateFormat.format("dd MMM yyyy, hh:mm a", selected)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}