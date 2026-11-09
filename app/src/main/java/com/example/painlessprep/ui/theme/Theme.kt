package com.example.painlessprep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = Background,
    onBackground = OnBackground
)

@Composable
fun PainlessPrepTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}