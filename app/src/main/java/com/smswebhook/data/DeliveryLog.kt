package com.smswebhook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class DeliveryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val presetName: String = "",
    val sender: String = "",
    val snippet: String = "",
    val success: Boolean = false,
    val responseCode: Int = 0,
    val detail: String = "",
)
