package io.jadu.crisisprotect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CrisisNavyLight,
    onPrimary = CrisisNavy,
    secondary = CrisisTealLight,
    onSecondary = CrisisTeal,
    background = CrisisDarkBackground,
    surface = CrisisDarkSurface,
    onSurface = CrisisDarkOnSurface,
    onSurfaceVariant = CrisisDarkOnSurfaceVariant,
    outline = CrisisOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = CrisisNavy,
    onPrimary = CrisisSurface,
    primaryContainer = CrisisNavyLight,
    onPrimaryContainer = CrisisNavy,
    secondary = CrisisTeal,
    onSecondary = CrisisSurface,
    secondaryContainer = CrisisTealLight,
    onSecondaryContainer = CrisisTeal,
    background = CrisisBackground,
    surface = CrisisSurface,
    onSurface = CrisisOnSurface,
    onSurfaceVariant = CrisisOnSurfaceVariant,
    outline = CrisisOutline,
)

@Composable
fun CrisisProtectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
