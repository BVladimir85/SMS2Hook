package com.smswebhook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smswebhook.data.AppDatabase
import com.smswebhook.data.AppSettings
import com.smswebhook.data.Preset
import com.smswebhook.data.SettingsStore
import com.smswebhook.model.SmsData
import com.smswebhook.net.WebhookSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val settingsStore = SettingsStore(app)

    val presets = db.presetDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val logs = db.logDao().observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    /** One-shot messages surfaced as toasts. */
    val toasts = MutableSharedFlow<String>(extraBufferCapacity = 8)

    fun setMasterEnabled(value: Boolean) = viewModelScope.launch {
        settingsStore.setEnabled(value)
    }

    fun setFilter(mode: String, terms: String) = viewModelScope.launch {
        settingsStore.setFilter(mode, terms)
    }

    fun savePreset(preset: Preset) = viewModelScope.launch {
        if (preset.id == 0L) db.presetDao().insert(preset) else db.presetDao().update(preset)
    }

    fun deletePreset(preset: Preset) = viewModelScope.launch {
        if (preset.id != 0L) db.presetDao().delete(preset)
    }

    fun duplicatePreset(preset: Preset) = viewModelScope.launch {
        db.presetDao().insert(preset.copy(id = 0, name = preset.name + " (копия)", enabled = false))
    }

    fun setPresetEnabled(preset: Preset, enabled: Boolean) = viewModelScope.launch {
        db.presetDao().update(preset.copy(enabled = enabled))
    }

    fun clearLogs() = viewModelScope.launch { db.logDao().clear() }

    fun sendTest(preset: Preset) = viewModelScope.launch(Dispatchers.IO) {
        val sample = SmsData(
            sender = "Тест",
            senderPhone = "+10000000000",
            body = "Проверка связи ✅",
            sim = "0",
            timestamp = System.currentTimeMillis(),
        )
        val r = WebhookSender.send(preset, sample)
        toasts.emit(if (r.success) "Тест успешен: HTTP ${r.code}" else "Ошибка теста: ${r.code} ${r.detail}")
    }
}
