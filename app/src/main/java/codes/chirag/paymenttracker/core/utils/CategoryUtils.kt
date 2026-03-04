package codes.chirag.paymenttracker.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DirectionsTransit
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import codes.chirag.paymenttracker.ui.theme.CategoryEducation
import codes.chirag.paymenttracker.ui.theme.CategoryEntertainment
import codes.chirag.paymenttracker.ui.theme.CategoryFood
import codes.chirag.paymenttracker.ui.theme.CategoryGroceries
import codes.chirag.paymenttracker.ui.theme.CategoryHealth
import codes.chirag.paymenttracker.ui.theme.CategoryOther
import codes.chirag.paymenttracker.ui.theme.CategoryShopping
import codes.chirag.paymenttracker.ui.theme.CategoryTransport
import codes.chirag.paymenttracker.ui.theme.IncomeGreen

/**
 * Canonical mapping from a category name to its display icon and color.
 * Used across all feature screens to ensure visual consistency.
 */
data class CategoryMeta(val icon: ImageVector, val color: Color)

fun getCategoryMeta(category: String): CategoryMeta = when (category.lowercase()) {
    "food", "dining", "restaurants" -> CategoryMeta(Icons.Outlined.Fastfood, CategoryFood)
    "transport", "travel"           -> CategoryMeta(Icons.Outlined.DirectionsTransit, CategoryTransport)
    "shopping"                      -> CategoryMeta(Icons.Outlined.ShoppingBag, CategoryShopping)
    "entertainment"                 -> CategoryMeta(Icons.Outlined.Movie, CategoryEntertainment)
    "groceries"                     -> CategoryMeta(Icons.Outlined.LocalGroceryStore, CategoryGroceries)
    "health", "medical"             -> CategoryMeta(Icons.Outlined.LocalHospital, CategoryHealth)
    "living"                        -> CategoryMeta(Icons.Outlined.Home, CategoryOther)
    "fitness"                       -> CategoryMeta(Icons.Outlined.FitnessCenter, CategoryHealth)
    "education", "study"            -> CategoryMeta(Icons.AutoMirrored.Outlined.MenuBook, CategoryEducation)
    "salary", "income", "freelance" -> CategoryMeta(Icons.Outlined.Work, IncomeGreen)
    else                            -> CategoryMeta(Icons.Outlined.MoreHoriz, CategoryOther)
}
