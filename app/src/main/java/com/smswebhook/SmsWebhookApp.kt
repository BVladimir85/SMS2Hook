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
        installCrashHandler()
        createNotificationChannel()
        seedDefaultsIfEmpty()
    }

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = java.io.StringWriter()
                throwable.printStackTrace(java.io.PrintWriter(sw))
                val intent = android.content.Intent(this, CrashActivity::class.java).apply {
                    addFlags(
                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    )
                    putExtra("trace", sw.toString())
                }
                startActivity(intent)
            } catch (_: Throwable) {
                previous?.uncaughtException(thread, throwable)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(1)
        }
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
