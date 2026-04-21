package com.example.easyssh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EasySshColorScheme = darkColorScheme(
    primary          = AccentGreen,
    onPrimary        = BgDeep,
    secondary        = AccentBlue,
    onSecondary      = BgDeep,
    tertiary         = AccentYellow,
    background       = BgDeep,
    onBackground     = TextPrimary,
    surface          = Surface,
    onSurface        = TextPrimary,
    surfaceVariant   = Surface2,
    onSurfaceVariant = TextSecondary,
    outline          = BorderClr,
    error            = AccentRed,
    onError          = BgDeep,
)

@Composable
fun EasysshTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EasySshColorScheme,
        typography  = Typography,
        content     = content,
    )
}
