package com.smswebhook.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smswebhook.model.FilterMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class AppSettings(
    val enabled: Boolean = false,
    val filterMode: String = FilterMode.ALL,
    val terms: String = "",
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val FILTER_MODE = stringPreferencesKey("filter_mode")
        val TERMS = stringPreferencesKey("terms")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            enabled = p[Keys.ENABLED] ?: false,
            filterMode = p[Keys.FILTER_MODE] ?: FilterMode.ALL,
            terms = p[Keys.TERMS] ?: "",
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.ENABLED] = value }
    }

    suspend fun setFilter(mode: String, terms: String) {
        context.dataStore.edit {
            it[Keys.FILTER_MODE] = mode
            it[Keys.TERMS] = terms
        }
    }
}
