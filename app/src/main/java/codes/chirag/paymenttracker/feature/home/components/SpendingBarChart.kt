package codes.chirag.paymenttracker.feature.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import codes.chirag.paymenttracker.ui.theme.DividerColor
import codes.chirag.paymenttracker.ui.theme.OnBackground
import codes.chirag.paymenttracker.ui.theme.OnSurfaceMuted
import codes.chirag.paymenttracker.ui.theme.OrangePrimary
import codes.chirag.paymenttracker.ui.theme.SurfaceL1
import codes.chirag.paymenttracker.ui.theme.SurfaceL3

data class WeeklyBarData(
    val day: String,    // "Mon", "Tue", etc.
    val amount: Double
)

@Composable
fun SpendingBarChart(
    weeklyData: List<WeeklyBarData>,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) return
    val maxAmount = weeklyData.maxOf { it.amount }.coerceAtLeast(1.0)
    val barColor = OrangePrimary
    val barTrackColor = SurfaceL3

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceL1)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground
                )
                Text(
                    text = "Spending",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            val chartHeight = 120.dp
            val barWidth = 28.dp
            val cornerRadius = 6.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val filledFraction = (entry.amount / maxAmount).toFloat().coerceIn(0f, 1f)

                        Canvas(
                            modifier = Modifier
                                .width(barWidth)
                                .height(chartHeight)
                        ) {
                            val barW = size.width
                            val totalH = size.height
                            val filledH = totalH * filledFraction
                            val cr = cornerRadius.toPx()

                            // Track bar (background)
                            drawRoundRect(
                                color = barTrackColor,
                                topLeft = Offset(0f, 0f),
                                size = Size(barW, totalH),
                                cornerRadius = CornerRadius(cr, cr)
                            )
                            // Filled bar
                            if (filledH > 0f) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(0f, totalH - filledH),
                                    size = Size(barW, filledH),
                                    cornerRadius = CornerRadius(cr, cr)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.day,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
