package com.task.to_doreminder.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.task.to_doreminder.R
import com.task.to_doreminder.accessibility.AccessibilityUtils
import com.task.to_doreminder.accessibility.ReminderAccessibilityService
import com.task.to_doreminder.data.local.Reminder
import com.task.to_doreminder.databinding.ReminderItemBinding
import java.util.Calendar

class ReminderAdapter(
    private val context: Context,
    private val onEditClick: (Reminder) -> Unit,
    private val onDeleteClick: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val reminders = mutableListOf<Reminder>()

    fun updateData(newReminders: List<Reminder>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }

    inner class ReminderViewHolder(private val binding: ReminderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.titleText.text = reminder.title
            binding.descriptionText.text = reminder.description
            binding.datetimeText.text = formatDateTime(reminder.reminderTime)
            binding.sourceText.text = if (reminder.isFromApi) "🌐 From API" else "📱 Local"

            binding.editIcon.setOnClickListener { onEditClick(reminder) }
            binding.deleteIcon.setOnClickListener { onDeleteClick(reminder) }
            itemView.setOnClickListener { handleItemClick(reminder) }
        }

        private fun handleItemClick(reminder: Reminder) {
            val serviceIntent = Intent(context, ReminderAccessibilityService::class.java)
                .putExtra("text_to_speak", reminder.title)

            if (AccessibilityUtils.isServiceEnabled(
                    context,
                    ReminderAccessibilityService::class.java
                )
            ) {
                context.startService(serviceIntent)
            } else {
                showAccessibilityPermissionDialog()
            }
        }

        private fun showAccessibilityPermissionDialog() {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_title_accessibility))
                .setMessage(
                    "To read your reminders aloud, please enable the Accessibility Service for this app.\n\n" +
                            "Steps:\n" +
                            "1. Go to 'Settings'.\n" +
                            "2. Select 'Accessibility'.\n" +
                            "3. Tap 'Installed apps'.\n" +
                            "4. Find and enable 'To-Do'."
                )
                .setPositiveButton(context.getString(R.string.button_enable)) { _, _ ->
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton(context.getString(R.string.button_cancel), null)
                .show()
        }


        private fun formatDateTime(timeInMilli: Long): String {
            val calendar = Calendar.getInstance().apply { timeInMillis = timeInMilli }
            return DateFormat.format("dd MMM yyyy, hh:mm a", calendar).toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding =
            ReminderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }
}