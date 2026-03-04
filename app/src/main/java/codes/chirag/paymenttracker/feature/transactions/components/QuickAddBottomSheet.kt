package codes.chirag.paymenttracker.feature.transactions.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.TransactionType
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import codes.chirag.paymenttracker.core.data.repository.AiService
import codes.chirag.paymenttracker.core.utils.QuickAddParser
import kotlinx.coroutines.launch
import codes.chirag.paymenttracker.ui.theme.BorderColor
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnPrimary
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.OrangeSubtle
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3

private val quickAddExamples = listOf(
    "Tea ₹20", "Zomato 180", "Cab 85", "Netflix 199", "Groceries 450"
)

/**
 * Bottom sheet for quick natural-language transaction entry.
 * Parses the input with [QuickAddParser] and calls [onParsed] with pre-filled values
 * so the caller can open [AddTransactionBottomSheet] pre-populated.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onParsed: (
        title: String,
        amount: String,
        type: TransactionType,
        category: String,
        paymentMethod: PaymentMethod,
        notes: String
    ) -> Unit,
    aiService: AiService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by rememberSaveable { mutableStateOf("") }
    var isAnalyzing by rememberSaveable { mutableStateOf(false) }
    val isValid = inputText.isNotBlank() && !isAnalyzing
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceL1,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Quick Add",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Text(
                            text = "AI-powered · describe your expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
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

            // Example chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAddExamples.forEach { example ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceL3)
                            .border(0.5.dp, DividerColor, RoundedCornerShape(20.dp))
                            .clickable { inputText = example }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = example,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }

            // Main input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "e.g. Had lunch at CCD for ₹180",
                        color = OnSurfaceMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
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

            // Preview card — shows parsed result inline while user types
            AnimatedVisibility(
                visible = isValid,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val parsed = QuickAddParser.parse(inputText)
                val amtText = if (parsed.amount > 0) "₹${parsed.amount.toLong()}" else "—"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceL3)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickPreviewChip(amtText, OrangePrimary)
                    QuickPreviewChip(parsed.category, Color(0xFF4CAF50))
                    QuickPreviewChip(parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }, Color(0xFF2196F3))
                    QuickPreviewChip(parsed.paymentMethod.name, Color(0xFF9C27B0))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Parse & Add button
            Button(
                onClick = {
                    if (isValid) {
                        isAnalyzing = true
                        scope.launch {
                            var parsed: codes.chirag.paymenttracker.core.utils.ParsedTransaction? = null
                            try {
                                parsed = aiService.parseText(inputText)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "AI Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                            
                            if (parsed == null) {
                                parsed = QuickAddParser.parse(inputText)
                            }
                            
                            onParsed(
                                parsed.title,
                                if (parsed.amount > 0) parsed.amount.toString() else "",
                                parsed.type,
                                parsed.category,
                                parsed.paymentMethod,
                                inputText // original text goes into notes
                            )
                            isAnalyzing = false
                        }
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = OnPrimary,
                    disabledContainerColor = SurfaceL3,
                    disabledContentColor = OnSurfaceMuted
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Analyzing...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Parse & Add",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPreviewChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
