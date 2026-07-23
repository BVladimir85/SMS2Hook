package com.smswebhook.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.smswebhook.R
import com.smswebhook.SmsWebhookApp
import com.smswebhook.data.AppDatabase
import com.smswebhook.data.DeliveryLog
import com.smswebhook.model.SmsData
import com.smswebhook.net.WebhookSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Delivers one SMS to one preset, with retry/backoff and a delivery log entry. */
class WebhookWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val presetId = inputData.getLong(KEY_PRESET_ID, -1L)
        if (presetId <= 0L) return Result.success()

        val sms = SmsData(
            sender = inputData.getString(KEY_SENDER).orEmpty(),
            senderPhone = inputData.getString(KEY_SENDER_PHONE).orEmpty(),
            body = inputData.getString(KEY_BODY).orEmpty(),
            sim = inputData.getString(KEY_SIM).orEmpty(),
            timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis()),
        )

        val db = AppDatabase.get(applicationContext)
        val preset = db.presetDao().getById(presetId) ?: return Result.success()

        val result = withContext(Dispatchers.IO) { WebhookSender.send(preset, sms) }

        db.logDao().insert(
            DeliveryLog(
                timestamp = System.currentTimeMillis(),
                presetName = preset.name,
                sender = sms.sender.ifBlank { sms.senderPhone },
                snippet = sms.body.take(120),
                success = result.success,
                responseCode = result.code,
                detail = result.detail,
            )
        )
        db.logDao().trim()

        return when {
            result.success -> Result.success()
            result.code in 400..499 -> Result.failure() // client error: retrying won't help
            runAttemptCount >= MAX_ATTEMPTS -> Result.failure()
            else -> Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, SmsWebhookApp.CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText("Отправка SMS на webhook…")
            .setSmallIcon(R.drawable.ic_stat_forward)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIF_ID, notification)
    }

    companion object {
        const val KEY_PRESET_ID = "presetId"
        const val KEY_SENDER = "sender"
        const val KEY_SENDER_PHONE = "senderPhone"
        const val KEY_BODY = "body"
        const val KEY_SIM = "sim"
        const val KEY_TIMESTAMP = "timestamp"
        private const val MAX_ATTEMPTS = 5
        private const val NOTIF_ID = 1001
    }
}
