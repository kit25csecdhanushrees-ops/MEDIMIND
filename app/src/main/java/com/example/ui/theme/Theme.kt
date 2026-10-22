package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MediTealLight,
    onPrimary = Color.Black,
    primaryContainer = MediTealDark,
    onPrimaryContainer = Color.White,
    secondary = MediCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color.White,
    background = MediBgDark,
    surface = MediSurfaceDark,
    surfaceVariant = MediSurfaceVariantDark,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = SeveritySevere,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MediTeal,
    onPrimary = Color.White,
    primaryContainer = MediTealContainer,
    onPrimaryContainer = MediOnTealContainer,
    secondary = MediCyan,
    onSecondary = Color.White,
    secondaryContainer = MediCyanContainer,
    onSecondaryContainer = MediOnCyanContainer,
    background = MediBgLight,
    surface = MediSurfaceLight,
    surfaceVariant = MediSurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    error = SeveritySevere,
    onError = Color.White
)

@Composable
fun MediMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature medical teal palette for consistent brand feel
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
