package com.smswebhook.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smswebhook.data.Preset

sealed interface Screen {
    data object Main : Screen
    data class Edit(val preset: Preset) : Screen
    data object Logs : Screen
    data object Settings : Screen
}

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(vm) {
        vm.toasts.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
    }

    var screen by remember { mutableStateOf<Screen>(Screen.Main) }

    when (val s = screen) {
        is Screen.Main -> MainScreen(
            vm = vm,
            onEdit = { screen = Screen.Edit(it) },
            onNew = { screen = Screen.Edit(Preset()) },
            onLogs = { screen = Screen.Logs },
            onSettings = { screen = Screen.Settings },
        )

        is Screen.Edit -> PresetEditScreen(
            vm = vm,
            original = s.preset,
            onBack = { screen = Screen.Main },
        )

        is Screen.Logs -> LogScreen(vm = vm, onBack = { screen = Screen.Main })

        is Screen.Settings -> SettingsScreen(vm = vm, onBack = { screen = Screen.Main })
    }
}
