package com.smswebhook.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.smswebhook.data.AppDatabase
import com.smswebhook.data.AppSettings
import com.smswebhook.data.SettingsStore
import com.smswebhook.model.FilterMode
import com.smswebhook.model.SmsData
import com.smswebhook.work.WorkEnqueuer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manifest-registered receiver for incoming SMS. Fires even when the app is closed
 * (SMS_RECEIVED is an exempt protected broadcast). Reassembles multipart messages,
 * applies the master switch + filters, then enqueues a delivery job per enabled preset.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val rawSender = messages[0].displayOriginatingAddress
            ?: messages[0].originatingAddress
            ?: ""
        val body = buildString { for (m in messages) append(m.messageBody ?: "") }
        val ts = messages[0].timestampMillis.let { if (it > 0) it else System.currentTimeMillis() }
        val sim = extractSim(intent)

        val appContext = context.applicationContext
        val pending = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val settings = SettingsStore(appContext).current()
                if (!settings.enabled) return@launch

                val displayName = resolveContactName(appContext, rawSender) ?: rawSender
                val sms = SmsData(
                    sender = displayName,
                    senderPhone = rawSender,
                    body = body,
                    sim = sim,
                    timestamp = ts,
                )
                if (!passesFilter(settings, sms)) return@launch

                val presets = AppDatabase.get(appContext).presetDao().getEnabled()
                for (p in presets) {
                    WorkEnqueuer.enqueue(appContext, p.id, sms)
                }
            } catch (_: Exception) {
                // Never crash the receiver.
            } finally {
                pending.finish()
            }
        }
    }

    private fun extractSim(intent: Intent): String {
        val extras = intent.extras ?: return ""
        for (key in listOf("subscription", "slot", "simSlot", "phone", "android.telephony.extra.SUBSCRIPTION_INDEX")) {
            if (extras.containsKey(key)) {
                val v = extras.getInt(key, -1)
                if (v >= 0) return v.toString()
            }
        }
        return ""
    }

    private fun passesFilter(settings: AppSettings, sms: SmsData): Boolean {
        if (settings.filterMode == FilterMode.ALL) return true
        val terms = settings.terms.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (terms.isEmpty()) return true
        val hay = (sms.senderPhone + " " + sms.sender + " " + sms.body).lowercase()
        val matched = terms.any { hay.contains(it) }
        return when (settings.filterMode) {
            FilterMode.ONLY -> matched
            FilterMode.EXCEPT -> !matched
            else -> true
        }
    }

    private fun resolveContactName(context: Context, phone: String): String? {
        if (phone.isBlank()) return null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone),
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null,
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (e: Exception) {
            null
        }
    }
}
