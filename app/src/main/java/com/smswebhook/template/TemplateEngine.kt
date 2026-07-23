package com.smswebhook.template

import com.smswebhook.model.SmsData
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TemplateEscape { RAW, URL, JSON }

/**
 * Replaces {placeholders} in a template with SMS values, applying the escaping that
 * matches the destination context (URL query, JSON string literal, or raw).
 */
object TemplateEngine {
    private val TOKEN = Regex("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}")

    fun render(template: String, vars: Map<String, String>, escape: TemplateEscape): String {
        if (template.isEmpty()) return template
        return TOKEN.replace(template) { m ->
            val key = m.groupValues[1]
            val raw = vars[key] ?: return@replace m.value
            when (escape) {
                TemplateEscape.RAW -> raw
                TemplateEscape.URL -> urlEncode(raw)
                TemplateEscape.JSON -> jsonEscape(raw)
            }
        }
    }

    fun urlEncode(s: String): String = URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    /** Escapes a string so it can be embedded inside a JSON string literal. */
    fun jsonEscape(s: String): String {
        val sb = StringBuilder(s.length + 16)
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        return sb.toString()
    }

    /** All supported template variables for a given SMS. */
    fun varsFrom(sms: SmsData): Map<String, String> {
        val date = Date(sms.timestamp)
        return mapOf(
            "sender" to sms.sender,
            "sender_phone" to sms.senderPhone,
            "msg" to sms.body,
            "message" to sms.body,
            "sim" to sms.sim,
            "timestamp" to sms.timestamp.toString(),
            "datetime" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(date),
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date),
            "time" to SimpleDateFormat("HH:mm:ss", Locale.US).format(date),
        )
    }

    /** The variables shown to the user in the editor help text. */
    val AVAILABLE = listOf(
        "{sender}", "{sender_phone}", "{msg}", "{sim}",
        "{date}", "{time}", "{datetime}", "{timestamp}",
    )
}
