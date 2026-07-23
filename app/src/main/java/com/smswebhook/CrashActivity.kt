package com.smswebhook

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

/**
 * Diagnostic screen: shown (in a separate ":crash" process) when the app throws an
 * uncaught exception, so the full stack trace is visible and can be shared.
 */
class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra("trace") ?: "Нет данных о трейсе"
        val tv = TextView(this).apply {
            text = "SMS2Hook — приложение упало.\nСкопируйте или пришлите этот текст:\n\n$trace"
            setTextIsSelectable(true)
            setPadding(32, 48, 32, 48)
            textSize = 11f
        }
        setContentView(ScrollView(this).apply { addView(tv) })
    }
}
