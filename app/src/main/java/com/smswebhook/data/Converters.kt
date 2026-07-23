package com.smswebhook.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smswebhook.model.KeyValue

class Converters {
    private val gson = Gson()
    private val listType = object : TypeToken<List<KeyValue>>() {}.type

    @TypeConverter
    fun fromKeyValueList(list: List<KeyValue>?): String =
        gson.toJson(list ?: emptyList<KeyValue>())

    @TypeConverter
    fun toKeyValueList(json: String?): List<KeyValue> =
        if (json.isNullOrBlank()) emptyList() else gson.fromJson(json, listType)
}
