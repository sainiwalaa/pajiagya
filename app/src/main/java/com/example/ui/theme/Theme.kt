package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BgmiColorScheme = darkColorScheme(
    primary = BgmiGold,
    onPrimary = Color.Black,
    primaryContainer = BgmiSurfaceVariant,
    onPrimaryContainer = BgmiGoldLight,
    secondary = BgmiOlive,
    onSecondary = Color.White,
    tertiary = BgmiBlueZone,
    onTertiary = Color.White,
    background = BgmiDarkBg,
    onBackground = BgmiTextPrimary,
    surface = BgmiSurface,
    onSurface = BgmiTextPrimary,
    surfaceVariant = BgmiSurfaceVariant,
    onSurfaceVariant = BgmiTextSecondary,
    error = BgmiRedZone,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep consistent tactical military theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BgmiColorScheme,
        typography = Typography,
        content = content
    )
}
