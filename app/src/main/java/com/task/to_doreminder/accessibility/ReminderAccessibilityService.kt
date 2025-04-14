package com.task.to_doreminder.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityEvent
import java.util.Locale

class ReminderAccessibilityService : AccessibilityService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val textToSpeak = intent.getStringExtra("text_to_speak") ?: return START_NOT_STICKY
        speakText(textToSpeak)
        return START_NOT_STICKY
    }

    fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used here
    }

    override fun onInterrupt() {
        tts?.stop()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}