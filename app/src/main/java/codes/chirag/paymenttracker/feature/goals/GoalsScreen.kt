package codes.chirag.paymenttracker.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.Goal
import codes.chirag.paymenttracker.core.utils.formatCurrency
import codes.chirag.paymenttracker.ui.theme.Background
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.IncomeGreen
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
    onGoalClick: (String) -> Unit = {}
) {
    val goals by viewModel.goals.collectAsState()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val totalSaved  = goals.sumOf { it.savedAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val completedCount = goals.count { it.isCompleted }

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OrangePrimary,
                contentColor = OnPrimary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Goal")
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 80.dp,
                start = 0.dp,
                end = 0.dp
            )
        ) {
            item {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // Summary card
            item {
                GoalsSummaryCard(
                    totalSaved = totalSaved,
                    totalTarget = totalTarget,
                    completedCount = completedCount,
                    totalCount = goals.size,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (goals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No goals yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap + to set your first savings goal",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }
            } else {
                // Section header
                item {
                    Text(
                        text = "Active Goals",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddSheet) {
        AddGoalBottomSheet(
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showAddSheet = false
                }
            },
            onSave = { name, target, targetDate ->
                viewModel.addGoal(name, target, targetDate)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showAddSheet = false
                }
            }
        )
    }
}

// ── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun GoalsSummaryCard(
    totalSaved: Double,
    totalTarget: Double,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceL1)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Saved",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted
                    )
                    Text(
                        text = formatCurrency(totalSaved),
                        style = MaterialTheme.typography.displaySmall,
                        color = OnBackground
                    )
                    Text(
                        text = "of ${formatCurrency(totalTarget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressArc(
                        progress = overallProgress,
                        color = OrangePrimary,
                        trackColor = SurfaceL3,
                        size = 72.dp
                    )
                    Text(
                        text = "${(overallProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OrangePrimary,
                trackColor = DividerColor,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip(label = "Goals", value = "$totalCount")
                StatChip(label = "Completed", value = "$completedCount", valueColor = IncomeGreen)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, valueColor: Color = OrangePrimary) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceL3)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = valueColor)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
    }
}

// ── Goal Card ─────────────────────────────────────────────────────────────────

@Composable
private fun GoalCard(goal: Goal, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val progressColor = if (goal.isCompleted) IncomeGreen else OrangePrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceL1)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (goal.isCompleted) IncomeGreen.copy(alpha = 0.15f) else OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Outlined.EmojiEvents else Icons.Outlined.Savings,
                            contentDescription = null,
                            tint = if (goal.isCompleted) IncomeGreen else OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (goal.targetDate.isNotEmpty()) {
                            Text(
                                text = "Target: ${goal.targetDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(goal.savedAmount),
                        style = MaterialTheme.typography.titleSmall,
                        color = OnBackground
                    )
                    Text(
                        text = "of ${formatCurrency(goal.targetAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = DividerColor,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.isCompleted) "Completed!" else "${(goal.progress * 100).toInt()}% saved",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.isCompleted) IncomeGreen else OnSurfaceMuted
                )
                if (!goal.isCompleted) {
                    Text(
                        text = "${formatCurrency(goal.targetAmount - goal.savedAmount)} to go",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

// ── Circular Progress Arc (Canvas) ───────────────────────────────────────────

@Composable
private fun CircularProgressArc(
    progress: Float,
    color: Color,
    trackColor: Color,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: Float = 8f
) {
    Canvas(
        modifier = Modifier.size(size)
    ) {
        val sweepAngle = 360f * progress
        val inset = strokeWidth / 2f
        val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Progress
        if (sweepAngle > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

// ── Add Goal Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, target: String, targetDate: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var targetDate by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Goal",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnBackground
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceL3)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            GoalFieldLabel("Goal Name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. New Phone", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = goalTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            GoalFieldLabel("Target Amount (₹)") {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    placeholder = { Text("0", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = goalTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            GoalFieldLabel("Target Date (optional)") {
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    placeholder = { Text("e.g. Jun 2026", color = OnSurfaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = goalTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && target.isNotBlank()) {
                        onSave(name, target, targetDate)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = OnPrimary
                )
            ) {
                Text("Create Goal", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun GoalFieldLabel(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
        content()
    }
}

@Composable
private fun goalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor = OrangePrimary,
    focusedContainerColor = SurfaceL3,
    unfocusedContainerColor = SurfaceL3
)
