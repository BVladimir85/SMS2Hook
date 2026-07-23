package com.smswebhook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF0EA5E9)

private val LightColors = lightColorScheme(
    primary = Accent,
    secondary = Color(0xFF0284C7),
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Color(0xFF38BDF8),
)

@Composable
fun SmsWebhookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
