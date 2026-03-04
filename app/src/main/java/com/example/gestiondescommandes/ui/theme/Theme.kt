package com.example.gestiondescommandes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),        // Bleu
    onPrimary = Color.White,
    secondary = Color(0xFF2E7D32),      // Vert
    onSecondary = Color.White,
    tertiary = Color(0xFFFFA000),       // Orange
    onTertiary = Color.Black,

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF111111),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0B1F33),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0B1F33),
    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF0B1F33),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    surface = Color(0xFF121212),
    onSurface = Color(0xFFEAEAEA),
    surfaceVariant = Color(0xFF2B2930),
    onSurfaceVariant = Color(0xFFCAC4D0),

    background = Color(0xFF0F0F10),
    onBackground = Color(0xFFEAEAEA),
)

@Composable
fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}