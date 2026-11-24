package com.example.painlessprep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.TextUnit

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = Background,
    onBackground = OnBackground
)

private val AppTypography = Typography().let {
    it.copy(bodyLarge = it.bodyLarge.copy(lineHeight = TextUnit.Unspecified))
}
@Composable
fun PainlessPrepTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}