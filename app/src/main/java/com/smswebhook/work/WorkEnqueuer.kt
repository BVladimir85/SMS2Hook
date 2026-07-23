package com.smswebhook.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.smswebhook.model.SmsData
import java.util.concurrent.TimeUnit

object WorkEnqueuer {
    fun enqueue(context: Context, presetId: Long, sms: SmsData) {
        val data = Data.Builder()
            .putLong(WebhookWorker.KEY_PRESET_ID, presetId)
            .putString(WebhookWorker.KEY_SENDER, sms.sender)
            .putString(WebhookWorker.KEY_SENDER_PHONE, sms.senderPhone)
            .putString(WebhookWorker.KEY_BODY, sms.body)
            .putString(WebhookWorker.KEY_SIM, sms.sim)
            .putLong(WebhookWorker.KEY_TIMESTAMP, sms.timestamp)
            .build()

        val request = OneTimeWorkRequestBuilder<WebhookWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
