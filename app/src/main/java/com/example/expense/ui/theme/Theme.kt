package com.example.expense.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = CreamContainer,
    onPrimaryContainer = CharcoalOnSurface,
    secondary = Color(0xFF8A7A65),
    onSecondary = Color.White,
    secondaryContainer = CreamContainerHigh,
    onSecondaryContainer = CharcoalOnSurface,
    tertiary = Color(0xFF6B8065),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD4E8D0),
    onTertiaryContainer = CharcoalOnSurface,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFFF5C0BC),
    onErrorContainer = Color(0xFF3B0A08),
    background = CreamSurface,
    onBackground = CharcoalOnSurface,
    surface = CreamSurface,
    onSurface = CharcoalOnSurface,
    surfaceVariant = CreamContainer,
    onSurfaceVariant = WarmGrey,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    surfaceContainer = CreamContainer,
    surfaceContainerHigh = CreamContainerHigh,
    surfaceContainerHighest = Color(0xFFDDD5CB),
    surfaceContainerLow = CreamSurface,
    surfaceContainerLowest = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimaryDark,
    onPrimary = Color(0xFF2C1E00),
    primaryContainer = Color(0xFF3D2C00),
    onPrimaryContainer = Color(0xFFFFD9A0),
    secondary = Color(0xFFCBB99A),
    onSecondary = Color(0xFF352A14),
    secondaryContainer = Color(0xFF4D3E28),
    onSecondaryContainer = CreamOnDark,
    tertiary = Color(0xFF9FC49A),
    onTertiary = Color(0xFF1E3B1A),
    tertiaryContainer = Color(0xFF35552F),
    onTertiaryContainer = Color(0xFFBBE0B5),
    error = Color(0xFFE57373),
    onError = Color(0xFF3B0A08),
    errorContainer = Color(0xFF5C1A18),
    onErrorContainer = Color(0xFFF5C0BC),
    background = DarkSurface,
    onBackground = CreamOnDark,
    surface = DarkSurface,
    onSurface = CreamOnDark,
    surfaceVariant = DarkContainer,
    onSurfaceVariant = WarmGreyOnDark,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = Color(0xFF3B3633),
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = Color(0xFF141210),
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
