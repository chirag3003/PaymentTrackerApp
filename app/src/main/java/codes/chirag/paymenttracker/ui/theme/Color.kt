package codes.chirag.paymenttracker.ui.theme

import androidx.compose.ui.graphics.Color

// ── Backgrounds ──────────────────────────────────────────────────────────────
// Layered dark surfaces following a 0dp → 1dp → 2dp → 3dp elevation model
val Background       = Color(0xFF0D0D0D)  // Page / screen background
val SurfaceL1        = Color(0xFF161616)  // Cards, bottom sheets (1dp elevation)
val SurfaceL2        = Color(0xFF1E1E1E)  // Elevated cards, dialogs (2dp)
val SurfaceL3        = Color(0xFF262626)  // Chips, input fields, nav bar (3dp)

// ── Primary Accent — Orange ───────────────────────────────────────────────────
val OrangePrimary    = Color(0xFFFF9A56)  // Main CTA, active icons, highlights
val OrangeSubtle     = Color(0xFF3D2410)  // Tinted container background (10% orange on dark)

// ── On-colors ─────────────────────────────────────────────────────────────────
val OnPrimary        = Color(0xFF1A0A00)  // Text / icons on orange buttons
val OnBackground     = Color(0xFFF2F2F2)  // Primary text on background
val OnSurface        = Color(0xFFEAEAEA)  // Primary text on surfaces
val OnSurfaceMuted   = Color(0xFF8A8A8A)  // Secondary / hint text

// ── Semantic Accents ──────────────────────────────────────────────────────────
val IncomeGreen      = Color(0xFF34C759)  // Income amounts, positive values
val IncomeGreenSubtle= Color(0xFF0E2E17)  // Container background for income chips
val ExpenseRed       = Color(0xFFFF3B30)  // Expense amounts, negative values
val ExpenseRedSubtle = Color(0xFF2E0E0E)  // Container background for expense chips
val InfoBlue         = Color(0xFF0A84FF)  // Informational highlights

// ── Dividers & Borders ────────────────────────────────────────────────────────
val DividerColor     = Color(0xFF2A2A2A)  // Hairline dividers between list items
val BorderColor      = Color(0xFF333333)  // Outlined field and card borders

// ── Category Palette ──────────────────────────────────────────────────────────
// Consistent per-category colors used across all features
val CategoryFood         = Color(0xFFFF6B35)
val CategoryTransport    = Color(0xFF5E81F4)
val CategoryShopping     = Color(0xFFF4A261)
val CategoryEntertainment= Color(0xFFE040FB)
val CategoryGroceries    = Color(0xFF26A69A)
val CategorySubscription = Color(0xFFEC407A)
val CategoryHealth       = Color(0xFF66BB6A)
val CategoryEducation    = Color(0xFFFFCA28)
val CategoryOther        = Color(0xFF78909C)
