package com.smswebhook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.smswebhook.data.AppDatabase
import com.smswebhook.data.DefaultPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsWebhookApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        seedDefaultsIfEmpty()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Пересылка SMS",
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = "Фоновая отправка SMS на webhook" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun seedDefaultsIfEmpty() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val dao = AppDatabase.get(this@SmsWebhookApp).presetDao()
            if (dao.count() == 0) {
                DefaultPresets.build().forEach { dao.insert(it) }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "forwarding"
    }
}
