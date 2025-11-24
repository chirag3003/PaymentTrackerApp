package codes.chirag.paymenttracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

    outline = BorderColor,
    outlineVariant = DividerColor,

    // Container colors
    surfaceTint = OrangePrimary,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = DarkSurface,
    inversePrimary = OrangePrimaryVariant,

    scrim = Color.Black,
)

@Composable
fun PaymentTrackerTheme(
    darkTheme: Boolean = true,  // Always use dark theme
    // Dynamic color disabled for custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = CustomDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}