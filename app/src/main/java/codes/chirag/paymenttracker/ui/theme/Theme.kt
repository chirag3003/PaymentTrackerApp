package codes.chirag.paymenttracker.ui.theme

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

/**
 * Custom dark color scheme with orange primary colors
 */
private val CustomDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = OrangePrimary,
    onPrimary = OnPrimaryDark,
    primaryContainer = OrangePrimaryVariant,
    onPrimaryContainer = Color.White,

    // Secondary colors
    secondary = OrangeSecondary,
    onSecondary = OnPrimaryDark,
    secondaryContainer = OrangePrimaryVariant,
    onSecondaryContainer = Color.White,

    // Tertiary colors
    tertiary = OrangeTertiary,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = OrangePrimaryVariant,
    onTertiaryContainer = Color.White,

    // Background colors
    background = DarkBackground,
    onBackground = OnBackgroundDark,

    // Surface colors
    surface = DarkSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceDark,

    // Other colors
    error = AccentRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = BorderColorDark,
    outlineVariant = DividerColorDark,

    // Container colors
    surfaceTint = OrangePrimary,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = DarkSurface,
    inversePrimary = OrangePrimaryVariant,

    scrim = Color.Black,
)

/**
 * Custom light color scheme with warm orange primary colors
 * Inspired by OKLCH color system for better color harmony
 */
private val CustomLightColorScheme = lightColorScheme(
    // Primary colors
    primary = OrangePrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = Color(0xFFFFDCC5),  // Very light orange container
    onPrimaryContainer = Color(0xFF331200),  // Dark brown for contrast

    // Secondary colors
    secondary = OrangeSecondaryLight,
    onSecondary = OnBackgroundLight,
    secondaryContainer = Color(0xFFFFE5D1),
    onSecondaryContainer = Color(0xFF3D2200),

    // Tertiary colors
    tertiary = OrangeTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0CC),
    onTertiaryContainer = Color(0xFF3A1800),

    // Background colors
    background = LightBackground,
    onBackground = OnBackgroundLight,

    // Surface colors
    surface = LightSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedForegroundLight,

    // Other colors
    error = AccentRedLight,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = BorderColorLight,
    outlineVariant = DividerColorLight,

    // Container colors
    surfaceTint = OrangePrimaryLight,
    inverseSurface = Color(0xFF313030),
    inverseOnSurface = Color(0xFFF5F0EB),
    inversePrimary = OrangePrimary,

    scrim = Color.Black,
)

@Composable
fun PaymentTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Follow system theme
    // Dynamic color disabled for custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Select color scheme based on theme
    val colorScheme = if (darkTheme) {
        CustomDarkColorScheme
    } else {
        CustomLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            // Set light/dark icons based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}