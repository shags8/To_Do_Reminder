package com.task.to_doreminder.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.task.to_doreminder.R
import com.task.to_doreminder.data.local.AppDatabase
import com.task.to_doreminder.data.local.Reminder
import com.task.to_doreminder.data.repository.ReminderRepository
import com.task.to_doreminder.databinding.FragmentReminderListBinding
import com.task.to_doreminder.ui.components.ReminderAdapter
import com.task.to_doreminder.viewmodels.ReminderViewModel
import com.task.to_doreminder.viewmodels.ReminderViewModelFactory
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {

    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReminderAdapter

    private val viewModel: ReminderViewModel by activityViewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ReminderViewModelFactory(ReminderRepository(db.reminderDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReminderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ReminderAdapter(
            requireContext(),
            onEditClick = { reminder ->
                viewModel.setReminder(reminder)
                findNavController().navigate(R.id.addReminderFragment)
            },
            onDeleteClick = { reminder ->
                viewModel.deleteReminder(reminder)
                cancelReminderNotification(reminder)
            }
        )

        binding.reminderRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReminderListFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collect { reminderList ->
                adapter.updateData(reminderList)
            }
        }

        binding.addReminderFab.setOnClickListener {
            findNavController().navigate(R.id.addReminderFragment)
        }

        viewModel.loadReminders()
    }

    private fun cancelReminderNotification(reminder: Reminder) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = reminder.uniqueId.toInt()
        notificationManager.cancel(notificationId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}