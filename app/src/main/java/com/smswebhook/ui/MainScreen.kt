@file:OptIn(ExperimentalMaterial3Api::class)

package com.smswebhook.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.smswebhook.data.Preset

@Composable
fun MainScreen(
    vm: AppViewModel,
    onEdit: (Preset) -> Unit,
    onNew: () -> Unit,
    onLogs: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val presets by vm.presets.collectAsState()
    val settings by vm.settings.collectAsState()

    var hasSms by remember { mutableStateOf(hasPermission(context, Manifest.permission.RECEIVE_SMS)) }
    var ignoringBattery by remember { mutableStateOf(isIgnoringBattery(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasSms = hasPermission(context, Manifest.permission.RECEIVE_SMS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS → Webhook") },
                actions = {
                    IconButton(onClick = onLogs) { Icon(Icons.Filled.List, contentDescription = "Журнал") }
                    IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Настройки") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Пресет") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MasterSwitchCard(
                    enabled = settings.enabled,
                    onToggle = { vm.setMasterEnabled(it) },
                )
            }
            item {
                StatusCard(
                    hasSms = hasSms,
                    ignoringBattery = ignoringBattery,
                    onGrant = { permissionLauncher.launch(requiredPermissions()) },
                    onFixBattery = {
                        runCatching { context.startActivity(batteryIntent(context)) }
                        ignoringBattery = isIgnoringBattery(context)
                    },
                )
            }
            item { Text("Пресеты", style = MaterialTheme.typography.titleMedium) }
            if (presets.isEmpty()) {
                item {
                    Text(
                        "Пока нет пресетов. Нажмите «Пресет», чтобы добавить.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(presets, key = { it.id }) { preset ->
                PresetRow(
                    preset = preset,
                    onToggle = { vm.setPresetEnabled(preset, it) },
                    onClick = { onEdit(preset) },
                    onTest = { vm.sendTest(preset) },
                )
            }
        }
    }
}

@Composable
private fun MasterSwitchCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Пересылка SMS", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (enabled) "Включена" else "Выключена",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun StatusCard(
    hasSms: Boolean,
    ignoringBattery: Boolean,
    onGrant: () -> Unit,
    onFixBattery: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Состояние", style = MaterialTheme.typography.titleMedium)
            StatusRow(
                ok = hasSms,
                okText = "Разрешение на приём SMS выдано",
                badText = "Нужно разрешение на приём SMS",
            )
            if (!hasSms) {
                Button(onClick = onGrant) { Text("Выдать разрешения") }
            }
            StatusRow(
                ok = ignoringBattery,
                okText = "Оптимизация батареи отключена",
                badText = "Отключите энергосбережение для надёжной доставки",
            )
            if (!ignoringBattery) {
                OutlinedButton(onClick = onFixBattery) { Text("Настроить батарею") }
            }
        }
    }
}

@Composable
private fun StatusRow(ok: Boolean, okText: String, badText: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Text(if (ok) okText else badText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PresetRow(
    preset: Preset,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onTest: () -> Unit,
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name.ifBlank { "(без имени)" }, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${preset.method} · ${preset.url}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onTest) { Text("Тест") }
            Switch(checked = preset.enabled, onCheckedChange = onToggle)
        }
    }
}

// ---- helpers ----

private fun hasPermission(context: Context, perm: String): Boolean =
    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

private fun isIgnoringBattery(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

@SuppressLint("BatteryLife")
private fun batteryIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}"),
    )

private fun requiredPermissions(): Array<String> {
    val perms = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        perms.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return perms.toTypedArray()
}
