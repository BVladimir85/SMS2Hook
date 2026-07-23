package com.smswebhook.data

import com.smswebhook.model.BodyMode
import com.smswebhook.model.HttpMethod
import com.smswebhook.model.KeyValue

/**
 * Built-in presets seeded on first launch. All are disabled by default so nothing
 * is sent until the user fills in tokens and enables them.
 */
object DefaultPresets {
    fun build(): List<Preset> = listOf(
        // --- Telegram bot ---
        // Send a message via the Bot API. Replace <BOT_TOKEN> and <CHAT_ID>.
        Preset(
            name = "Telegram",
            method = HttpMethod.POST,
            url = "https://api.telegram.org/bot<BOT_TOKEN>/sendMessage",
            headers = emptyList(),
            params = listOf(
                KeyValue("chat_id", "<CHAT_ID>"),
                KeyValue("text", "📩 SMS от {sender} ({sender_phone}):\n{msg}"),
            ),
            bodyMode = BodyMode.FORM,
            enabled = false,
            sortOrder = 0,
        ),
        // --- Gotify ---
        // Push to a Gotify server. Replace the host and <APP_TOKEN>.
        Preset(
            name = "Gotify",
            method = HttpMethod.POST,
            url = "https://gotify.example.com/message",
            headers = listOf(KeyValue("X-Gotify-Key", "<APP_TOKEN>")),
            params = listOf(
                KeyValue("title", "SMS от {sender}"),
                KeyValue("message", "{sender_phone}:\n{msg}"),
                KeyValue("priority", "5"),
            ),
            bodyMode = BodyMode.FORM,
            enabled = false,
            sortOrder = 1,
        ),
        // --- Discord ---
        // Channel webhook (Server Settings -> Integrations -> Webhooks). Paste the full
        // webhook URL. Body is JSON; {msg} is JSON-escaped automatically.
        Preset(
            name = "Discord",
            method = HttpMethod.POST,
            url = "https://discord.com/api/webhooks/<WEBHOOK_ID>/<WEBHOOK_TOKEN>",
            headers = emptyList(),
            params = emptyList(),
            bodyMode = BodyMode.JSON,
            bodyTemplate = "{\"content\":\"📩 SMS от {sender} ({sender_phone}):\\n{msg}\"}",
            contentType = "application/json",
            enabled = false,
            sortOrder = 2,
        ),
        // --- WhatsApp (via CallMeBot, free personal notifications) ---
        // 1) Add +34 621 331 709 to contacts, 2) send "I allow callmebot to send me
        // messages to this number" from WhatsApp, 3) you receive an <API_KEY>.
        // Then set <PHONE> (your number, digits only, with country code) and <API_KEY>.
        Preset(
            name = "WhatsApp (CallMeBot)",
            method = HttpMethod.GET,
            url = "https://api.callmebot.com/whatsapp.php",
            headers = emptyList(),
            params = listOf(
                KeyValue("phone", "<PHONE>"),
                KeyValue("apikey", "<API_KEY>"),
                KeyValue("text", "SMS от {sender} ({sender_phone}): {msg}"),
            ),
            bodyMode = BodyMode.NONE,
            enabled = false,
            sortOrder = 3,
        ),
        // --- Generic webhook (matches the "URL + method + key/value" sketch) ---
        Preset(
            name = "Свой webhook (пример)",
            method = HttpMethod.POST,
            url = "https://example.com/webhook",
            headers = emptyList(),
            params = listOf(
                KeyValue("sender", "{sender}"),
                KeyValue("phone", "{sender_phone}"),
                KeyValue("text", "{msg}"),
            ),
            bodyMode = BodyMode.FORM,
            enabled = false,
            sortOrder = 4,
        ),
    )
}
