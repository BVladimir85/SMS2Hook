package com.smswebhook.net

import com.smswebhook.data.Preset
import com.smswebhook.model.BodyMode
import com.smswebhook.model.HttpMethod
import com.smswebhook.model.SmsData
import com.smswebhook.template.TemplateEngine
import com.smswebhook.template.TemplateEscape
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** Renders a preset against an SMS and performs the HTTP request. Never throws. */
object WebhookSender {
    data class Result(val success: Boolean, val code: Int, val detail: String)

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    fun send(preset: Preset, sms: SmsData): Result {
        val vars = TemplateEngine.varsFrom(sms)
        val method = preset.method.uppercase()
        val headers = preset.headers.filter { it.enabled && it.key.isNotBlank() }
        val params = preset.params.filter { it.enabled && it.key.isNotBlank() }

        return try {
            val baseUrl = TemplateEngine.render(preset.url, vars, TemplateEscape.URL)
            val builder = Request.Builder()

            if (method == HttpMethod.GET) {
                val finalUrl = if (params.isEmpty()) baseUrl else buildString {
                    append(baseUrl)
                    var sep = if (baseUrl.contains("?")) "&" else "?"
                    for (p in params) {
                        val k = TemplateEngine.urlEncode(TemplateEngine.render(p.key, vars, TemplateEscape.RAW))
                        val v = TemplateEngine.urlEncode(TemplateEngine.render(p.value, vars, TemplateEscape.RAW))
                        append(sep).append(k).append("=").append(v)
                        sep = "&"
                    }
                }
                builder.url(finalUrl).get()
            } else {
                val body = when (preset.bodyMode) {
                    BodyMode.FORM -> {
                        val fb = FormBody.Builder()
                        for (p in params) {
                            fb.add(
                                TemplateEngine.render(p.key, vars, TemplateEscape.RAW),
                                TemplateEngine.render(p.value, vars, TemplateEscape.RAW),
                            )
                        }
                        fb.build()
                    }

                    BodyMode.JSON -> {
                        val json = TemplateEngine.render(preset.bodyTemplate, vars, TemplateEscape.JSON)
                        json.toRequestBody("application/json; charset=utf-8".toMediaType())
                    }

                    BodyMode.RAW -> {
                        val raw = TemplateEngine.render(preset.bodyTemplate, vars, TemplateEscape.RAW)
                        val ct = (preset.contentType.ifBlank { "text/plain" }) + "; charset=utf-8"
                        raw.toRequestBody(ct.toMediaType())
                    }

                    else -> ByteArray(0).toRequestBody(null)
                }
                builder.url(baseUrl).post(body)
            }

            for (h in headers) {
                val hk = TemplateEngine.render(h.key, vars, TemplateEscape.RAW).replace("\n", "").replace("\r", "")
                val hv = TemplateEngine.render(h.value, vars, TemplateEscape.RAW).replace("\n", "").replace("\r", "")
                if (hk.isNotBlank()) builder.header(hk, hv)
            }

            client.newCall(builder.build()).execute().use { resp ->
                val text = resp.body?.string().orEmpty().take(300)
                Result(resp.isSuccessful, resp.code, if (resp.isSuccessful) "OK" else text)
            }
        } catch (e: Exception) {
            Result(false, -1, e.message ?: e.javaClass.simpleName)
        }
    }
}
