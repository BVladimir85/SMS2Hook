package com.smswebhook.model

/** A parsed, ready-to-forward SMS. */
data class SmsData(
    val sender: String = "",
    val senderPhone: String = "",
    val body: String = "",
    val sim: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

/** A single key/value row used for headers and body/query parameters. */
data class KeyValue(
    val key: String = "",
    val value: String = "",
    val enabled: Boolean = true,
)

object HttpMethod {
    const val GET = "GET"
    const val POST = "POST"
    val ALL = listOf(GET, POST)
}

object BodyMode {
    const val NONE = "NONE"
    const val FORM = "FORM"
    const val JSON = "JSON"
    const val RAW = "RAW"
    val ALL = listOf(NONE, FORM, JSON, RAW)

    fun label(mode: String): String = when (mode) {
        NONE -> "Без тела"
        FORM -> "Форма"
        JSON -> "JSON"
        RAW -> "Raw"
        else -> mode
    }
}

object FilterMode {
    const val ALL = "ALL"
    const val ONLY = "ONLY"
    const val EXCEPT = "EXCEPT"
}
