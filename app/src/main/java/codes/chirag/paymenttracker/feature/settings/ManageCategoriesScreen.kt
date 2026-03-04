package codes.chirag.paymenttracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.utils.getCategoryMeta
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3

private val defaultCategories = listOf(
    "Food", "Transport", "Shopping", "Entertainment",
    "Groceries", "Living", "Health", "Education", "Other"
)

@Composable
fun ManageCategoriesScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customCategories = remember { viewModel.getCustomCategories().toMutableList() }
    val allCategories = remember(customCategories.size) {
        (defaultCategories + customCategories).distinct()
    }
    val savedBudgets = remember { viewModel.getCategoryBudgets() }

    // Local budget edits — category → text input
    val budgetInputs = remember {
        mutableStateMapOf<String, String>().also { map ->
            allCategories.forEach { cat ->
                map[cat] = savedBudgets[cat]?.let {
                    if (it > 0) it.toLong().toString() else ""
                } ?: ""
            }
        }
    }

    var newCategoryInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = OnBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Manage Categories",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Text(
                text = "Set budgets for each category. Leave blank for no limit.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Category rows
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceL1)
            ) {
                Column {
                    allCategories.forEachIndexed { index, category ->
                        CategoryBudgetRow(
                            category = category,
                            budgetInput = budgetInputs[category] ?: "",
                            onBudgetChange = { budgetInputs[category] = it },
                            onSave = {
                                val budget = budgetInputs[category]?.toDoubleOrNull() ?: 0.0
                                viewModel.setCategoryBudget(category, budget)
                            }
                        )
                        if (index < allCategories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = DividerColor,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Add custom category
        item {
            Text(
                text = "Add Custom Category",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    placeholder = { Text("e.g. Rent, Gifts...", color = OnSurfaceMuted) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground,
                        unfocusedTextColor = OnBackground,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = OrangePrimary,
                        focusedContainerColor = SurfaceL3,
                        unfocusedContainerColor = SurfaceL3
                    )
                )
                Button(
                    onClick = {
                        val trimmed = newCategoryInput.trim()
                        if (trimmed.isNotBlank() && !allCategories.contains(trimmed)) {
                            customCategories.add(trimmed)
                            viewModel.saveCustomCategories(customCategories.toList())
                            budgetInputs[trimmed] = ""
                            newCategoryInput = ""
                        }
                    },
                    enabled = newCategoryInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = OnPrimary
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    category: String,
    budgetInput: String,
    onBudgetChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val meta = getCategoryMeta(category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(meta.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = meta.icon,
                contentDescription = null,
                tint = meta.color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = budgetInput,
            onValueChange = onBudgetChange,
            placeholder = { Text("₹ limit", color = OnSurfaceMuted, style = MaterialTheme.typography.labelSmall) },
            singleLine = true,
            prefix = { if (budgetInput.isNotBlank()) Text("₹", color = OnSurfaceMuted, style = MaterialTheme.typography.labelSmall) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.size(width = 90.dp, height = 48.dp),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.labelMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = BorderColor,
                cursorColor = OrangePrimary,
                focusedContainerColor = SurfaceL3,
                unfocusedContainerColor = SurfaceL3
            )
        )
        // Save checkmark button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OrangeSubtle),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSave, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Save budget",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
