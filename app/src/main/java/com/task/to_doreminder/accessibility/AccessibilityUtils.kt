package com.task.to_doreminder.accessibility

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object AccessibilityUtils {
    fun isServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponent = ComponentName(context, serviceClass).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return TextUtils.SimpleStringSplitter(':').run {
            setString(enabledServices)
            any { it.equals(expectedComponent, ignoreCase = true) }
        }
    }
}