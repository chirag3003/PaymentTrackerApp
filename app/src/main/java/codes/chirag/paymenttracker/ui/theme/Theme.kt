package codes.chirag.paymenttracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary             = OrangePrimary,
    onPrimary           = OnPrimary,
    primaryContainer    = OrangeSubtle,
    onPrimaryContainer  = OrangePrimary,

    // Secondary (re-uses orange family for consistency)
    secondary           = OrangePrimary,
    onSecondary         = OnPrimary,
    secondaryContainer  = OrangeSubtle,
    onSecondaryContainer= OrangePrimary,

    // Background & surfaces
    background          = Background,
    onBackground        = OnBackground,
    surface             = SurfaceL1,
    onSurface           = OnSurface,
    surfaceVariant      = SurfaceL2,
    onSurfaceVariant    = OnSurfaceMuted,

    // Error
    error               = ExpenseRed,
    onError             = Color.White,
    errorContainer      = ExpenseRedSubtle,
    onErrorContainer    = ExpenseRed,

    // Borders / outlines
    outline             = BorderColor,
    outlineVariant      = DividerColor,

    // Misc
    scrim               = Color(0xCC000000),
    inverseSurface      = OnSurface,
    inverseOnSurface    = SurfaceL1,
    inversePrimary      = OrangePrimary,
    surfaceTint         = OrangePrimary,
)

@Composable
fun PaymentTrackerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor     = Background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
