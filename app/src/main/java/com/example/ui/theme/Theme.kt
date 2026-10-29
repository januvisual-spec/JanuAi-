package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JanuDarkColorScheme = darkColorScheme(
    primary = JanuNeonGreen,
    onPrimary = Color.Black,
    primaryContainer = JanuDarkGreen,
    onPrimaryContainer = JanuAccentMint,
    secondary = JanuNeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF063337),
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = JanuEmerald,
    onTertiary = Color.Black,
    background = JanuBlack,
    onBackground = JanuTextPrimary,
    surface = JanuDarkSurface,
    onSurface = JanuTextPrimary,
    surfaceVariant = JanuCardBg,
    onSurfaceVariant = JanuTextSecondary,
    outline = JanuCardBorder,
    outlineVariant = Color(0xFF133227)
)

@Composable
fun JanuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JanuDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = JanuTheme(content = content)
