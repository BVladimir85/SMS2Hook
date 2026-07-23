@file:OptIn(ExperimentalMaterial3Api::class)

package com.smswebhook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smswebhook.model.FilterMode

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    var mode by remember(settings.filterMode) { mutableStateOf(settings.filterMode) }
    var terms by remember(settings.terms) { mutableStateOf(settings.terms) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Фильтр сообщений", style = MaterialTheme.typography.titleMedium)
            val options = listOf(
                FilterMode.ALL to "Пересылать все SMS",
                FilterMode.ONLY to "Только совпадающие",
                FilterMode.EXCEPT to "Кроме совпадающих",
            )
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mode = value }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == value, onClick = { mode = value })
                        Text(label)
                    }
                }
            }
            if (mode != FilterMode.ALL) {
                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("Слова или номера через запятую") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Совпадение ищется в отправителе и тексте сообщения. Пример: сбербанк, 900, код",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = { vm.setFilter(mode, terms); onBack() }) { Text("Сохранить") }

            HorizontalDivider()

            Text("О приложении", style = MaterialTheme.typography.titleMedium)
            Text(
                "SMS2Hook пересылает входящие SMS на HTTP-эндпоинты по пресетам. " +
                    "Пресеты и токены хранятся только на устройстве.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
