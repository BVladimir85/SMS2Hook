package com.smswebhook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smswebhook.model.BodyMode
import com.smswebhook.model.HttpMethod
import com.smswebhook.model.KeyValue

@Entity(tableName = "presets")
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val method: String = HttpMethod.POST,
    val url: String = "",
    val headers: List<KeyValue> = emptyList(),
    val params: List<KeyValue> = emptyList(),
    val bodyMode: String = BodyMode.FORM,
    val bodyTemplate: String = "",
    val contentType: String = "application/json",
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
)
