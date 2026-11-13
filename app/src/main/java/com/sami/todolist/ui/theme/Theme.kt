package com.sami.todolist.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val title: String) {
    CLASSIC_BLUE("Classic Blue"),
    DARK_OBSIDIAN("Dark Obsidian"),
    AMOLED_BLACK("AMOLED Black"),
    FOREST_EMERALD("Forest Emerald"),
    SUNSET_CORAL("Sunset Coral"),
    ROYAL_PURPLE("Royal Purple")
}

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = BlueSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkObsidianColorScheme = darkColorScheme(
    primary = BlueSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = BlueTertiary,
    onSecondary = Color.Black,
    background = ObsidianBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val AmoledColorScheme = darkColorScheme(
    primary = BlueSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color.White,
    secondary = BlueTertiary,
    onSecondary = Color.Black,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA1A1AA)
)

private val EmeraldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF0FDF4),
    onBackground = Color(0xFF064E3B),
    surface = Color.White,
    onSurface = Color(0xFF064E3B)
)

private val CoralColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = CoralSecondary,
    onSecondary = Color.White,
    background = Color(0xFFFFF7ED),
    onBackground = Color(0xFF431407),
    surface = Color.White,
    onSurface = Color(0xFF431407)
)

private val PurpleColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    background = Color(0xFFFAF5FF),
    onBackground = Color(0xFF3B0764),
    surface = Color.White,
    onSurface = Color(0xFF3B0764)
)

@Composable
fun TodoListTheme(
    themeMode: AppThemeMode = AppThemeMode.CLASSIC_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.CLASSIC_BLUE -> LightColorScheme
        AppThemeMode.DARK_OBSIDIAN -> DarkObsidianColorScheme
        AppThemeMode.AMOLED_BLACK -> AmoledColorScheme
        AppThemeMode.FOREST_EMERALD -> EmeraldColorScheme
        AppThemeMode.SUNSET_CORAL -> CoralColorScheme
        AppThemeMode.ROYAL_PURPLE -> PurpleColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            val isDark = themeMode == AppThemeMode.DARK_OBSIDIAN || themeMode == AppThemeMode.AMOLED_BLACK
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
