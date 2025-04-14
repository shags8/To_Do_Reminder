package com.task.to_doreminder

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.task.to_doreminder.accessibility.AccessibilityUtils
import com.task.to_doreminder.accessibility.ReminderAccessibilityService
import com.task.to_doreminder.databinding.ActivityMainBinding
import com.task.to_doreminder.notification.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNotificationDialogShowing = false
    private var isExactAlarmDialogShowing = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && shouldShowNotificationPermissionRationale()) {
            showNotificationPermissionDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNotificationPermission()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            handleExactAlarmPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> { /* Permission already granted */ }

            shouldShowNotificationPermissionRationale() -> showNotificationPermissionDialog()
            else -> requestNotificationPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun handleExactAlarmPermission() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            showExactAlarmPermissionDialog()
        }
    }

    private fun showNotificationPermissionDialog() {
        if (isNotificationDialogShowing) return

        isNotificationDialogShowing = true
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("Reminders will not be shown unless you allow notifications.")
            .setPositiveButton("Grant") { _, _ ->
                isNotificationDialogShowing = false
                openAppNotificationSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                isNotificationDialogShowing = false
                showRetryOrExitDialog("Notification permission is required to continue.")
            }
            .setOnDismissListener { isNotificationDialogShowing = false }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showExactAlarmPermissionDialog() {
        if (isExactAlarmDialogShowing) return

        isExactAlarmDialogShowing = true
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_exact_alarm))
            .setMessage(getString(R.string.dialog_message_exact_alarm))
            .setPositiveButton(getString(R.string.button_grant)) { _, _ ->
                isExactAlarmDialogShowing = false
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    })
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_enable_manual),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.button_cancel)) { _, _ ->
                isExactAlarmDialogShowing = false
                showRetryOrExitDialog("Exact alarm permission is required for proper functioning.")
            }
            .setOnDismissListener { isExactAlarmDialogShowing = false }
            .show()
    }

    private fun showRetryOrExitDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun shouldShowNotificationPermissionRationale(): Boolean {
        return shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun openAppNotificationSettings() {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(this)
        }
    }
}