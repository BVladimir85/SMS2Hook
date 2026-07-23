package com.smswebhook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smswebhook.ui.AppRoot
import com.smswebhook.ui.theme.SmsWebhookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmsWebhookTheme {
                AppRoot()
            }
        }
    }
}
