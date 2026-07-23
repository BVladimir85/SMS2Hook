@file:OptIn(ExperimentalMaterial3Api::class)

package com.smswebhook.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smswebhook.data.Preset
import com.smswebhook.model.BodyMode
import com.smswebhook.model.HttpMethod
import com.smswebhook.model.KeyValue
import com.smswebhook.template.TemplateEngine

@Composable
fun PresetEditScreen(
    vm: AppViewModel,
    original: Preset,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(original.name) }
    var method by remember { mutableStateOf(original.method) }
    var url by remember { mutableStateOf(original.url) }
    val headers = remember { mutableStateListOf<KeyValue>().apply { addAll(original.headers) } }
    val params = remember { mutableStateListOf<KeyValue>().apply { addAll(original.params) } }
    var bodyMode by remember { mutableStateOf(original.bodyMode) }
    var bodyTemplate by remember { mutableStateOf(original.bodyTemplate) }
    var contentType by remember { mutableStateOf(original.contentType) }

    fun current(): Preset = original.copy(
        name = name.trim().ifBlank { "Без имени" },
        method = method,
        url = url.trim(),
        headers = headers.toList(),
        params = params.toList(),
        bodyMode = if (method == HttpMethod.GET) BodyMode.NONE else bodyMode,
        bodyTemplate = bodyTemplate,
        contentType = contentType,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (original.id == 0L) "Новый пресет" else "Пресет") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (original.id != 0L) {
                        IconButton(onClick = { vm.deletePreset(original); onBack() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                        }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("HTTP-метод", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HttpMethod.ALL.forEach { m ->
                    FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                }
            }

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth(),
            )

            KeyValueSection(title = "Заголовки", items = headers, keyLabel = "Заголовок", valueLabel = "Значение")

            if (method == HttpMethod.POST) {
                Text("Тело запроса", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BodyMode.ALL.forEach { mode ->
                        FilterChip(
                            selected = bodyMode == mode,
                            onClick = { bodyMode = mode },
                            label = { Text(BodyMode.label(mode)) },
                        )
                    }
                }
            }

            val showParams = method == HttpMethod.GET || (method == HttpMethod.POST && bodyMode == BodyMode.FORM)
            if (showParams) {
                val title = if (method == HttpMethod.GET) "Параметры (query в URL)" else "Поля формы"
                KeyValueSection(title = title, items = params, keyLabel = "Имя", valueLabel = "Значение")
            }

            if (method == HttpMethod.POST && (bodyMode == BodyMode.JSON || bodyMode == BodyMode.RAW)) {
                OutlinedTextField(
                    value = bodyTemplate,
                    onValueChange = { bodyTemplate = it },
                    label = { Text(if (bodyMode == BodyMode.JSON) "JSON-тело (шаблон)" else "Тело (шаблон)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                )
                if (bodyMode == BodyMode.RAW) {
                    OutlinedTextField(
                        value = contentType,
                        onValueChange = { contentType = it },
                        label = { Text("Content-Type") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            VariablesHelp()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.savePreset(current()); onBack() }, modifier = Modifier.weight(1f)) {
                    Text("Сохранить")
                }
                OutlinedButton(onClick = { vm.sendTest(current()) }, modifier = Modifier.weight(1f)) {
                    Text("Тест")
                }
            }
            if (original.id != 0L) {
                OutlinedButton(
                    onClick = { vm.duplicatePreset(current()); onBack() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Дублировать") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KeyValueSection(
    title: String,
    items: SnapshotStateList<KeyValue>,
    keyLabel: String,
    valueLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = { items.add(KeyValue()) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Добавить")
            }
        }
        items.forEachIndexed { index, kv ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = kv.key,
                    onValueChange = { if (index < items.size) items[index] = items[index].copy(key = it) },
                    label = { Text(keyLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = kv.value,
                    onValueChange = { if (index < items.size) items[index] = items[index].copy(value = it) },
                    label = { Text(valueLabel) },
                    modifier = Modifier.weight(1.4f),
                )
                IconButton(onClick = { if (index < items.size) items.removeAt(index) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

@Composable
private fun VariablesHelp() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Доступные переменные", style = MaterialTheme.typography.labelLarge)
            Text(TemplateEngine.AVAILABLE.joinToString("   "), style = MaterialTheme.typography.bodyMedium)
            Text(
                "Подставляются в URL, заголовки и тело. В URL значения кодируются автоматически.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
