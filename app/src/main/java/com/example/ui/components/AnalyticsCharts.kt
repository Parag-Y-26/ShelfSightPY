package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StatusLive
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class DailyDwellData(
    val day: String,
    val avgDwellSeconds: Float,
    val totalVisits: Int
)

data class ReplenishmentData(
    val shelfName: String,
    val restockEvents: Int,
    val unitsAdded: Int,
    val maxCapacity: Int
)

@Composable
fun DailyProductDwellChartCard(
    dwellData: List<DailyDwellData> = listOf(
        DailyDwellData("Mon", 4.2f, 24),
        DailyDwellData("Tue", 6.8f, 31),
        DailyDwellData("Wed", 8.5f, 38),
        DailyDwellData("Thu", 5.4f, 27),
        DailyDwellData("Fri", 9.2f, 44),
        DailyDwellData("Sat", 11.5f, 52),
        DailyDwellData("Sun", 7.8f, 35)
    )
) {
    var selectedIndex by remember { mutableIntStateOf(4) } // Default to Fri
    val selectedItem = dwellData.getOrNull(selectedIndex) ?: dwellData.first()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Icon + Title + Firestore Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = TealPrimaryVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Product Dwell Times",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Firestore Live",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle with selected day metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Average customer interaction time per shelf visit",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                // Interactive Value Tooltip
                Text(
                    text = "${selectedItem.day}: ${String.format("%.1f", selectedItem.avgDwellSeconds)}s (${selectedItem.totalVisits} visits)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimaryVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-style Native Compose Chart (Bar + Trend Curve)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dwellData) {
                            detectTapGestures { offset ->
                                val slotW = size.width / dwellData.size
                                val index = (offset.x / slotW).toInt().coerceIn(0, dwellData.size - 1)
                                selectedIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = 14f // max dwell seconds scale
                    val slotW = w / dwellData.size

                    // 1. Subtle horizontal grid lines (Recharts aesthetic)
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = h * (i / gridSteps.toFloat())
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw Bars with rounded top
                    val trendPoints = mutableListOf<Offset>()

                    dwellData.forEachIndexed { i, item ->
                        val barH = (item.avgDwellSeconds / maxVal) * (h * 0.82f)
                        val barW = slotW * 0.42f
                        val barX = i * slotW + (slotW - barW) / 2f
                        val barY = h - barH
                        val isSelected = i == selectedIndex

                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF00CBA7), Color(0xFF0091B5))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFB2EBF2), Color(0xFF80DEEA))
                            )
                        }

                        // Bar rectangle with rounded top corners
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(barX, barY),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Highlight border for selected bar
                        if (isSelected) {
                            drawRoundRect(
                                color = TealPrimaryVariant,
                                topLeft = Offset(barX, barY),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Trend point for line overlay
                        trendPoints.add(Offset(barX + barW / 2f, barY))
                    }

                    // 3. Draw Spline/Line Overlay across bars (Recharts ComposedChart style)
                    if (trendPoints.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(trendPoints[0].x, trendPoints[0].y)
                            for (k in 1 until trendPoints.size) {
                                val prev = trendPoints[k - 1]
                                val curr = trendPoints[k]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF0284C7),
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )

                        // Draw node dots
                        trendPoints.forEachIndexed { idx, pt ->
                            val isSel = idx == selectedIndex
                            drawCircle(
                                color = if (isSel) Color(0xFF0284C7) else Color.White,
                                radius = if (isSel) 5f else 3.5f,
                                center = pt
                            )
                            drawCircle(
                                color = Color(0xFF0284C7),
                                radius = if (isSel) 5f else 3.5f,
                                center = pt,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }

            // X-Axis Day Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dwellData.forEachIndexed { idx, item ->
                    val isSel = idx == selectedIndex
                    Text(
                        text = item.day,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) TealPrimaryVariant else TextMuted,
                        modifier = Modifier.clickable { selectedIndex = idx }
                    )
                }
            }

            // Peak Insight Footer
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF16A34A), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Peak Customer Engagement: Saturday (11.5s avg dwell time)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

@Composable
fun ShelfReplenishmentChartCard(
    replenishmentData: List<ReplenishmentData> = listOf(
        ReplenishmentData("Shelf 1 · Noodles", restockEvents = 6, unitsAdded = 142, maxCapacity = 160),
        ReplenishmentData("Shelf 2 · Snacks", restockEvents = 8, unitsAdded = 196, maxCapacity = 200),
        ReplenishmentData("Shelf 3 · Dairy", restockEvents = 3, unitsAdded = 74, maxCapacity = 120),
        ReplenishmentData("Shelf 4 · Drinks", restockEvents = 5, unitsAdded = 118, maxCapacity = 150)
    )
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = TealPrimaryVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shelf Stock Replenishment Frequency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Restock cadence & units ingested from Firestore inventory events",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Replenishment Rows
            replenishmentData.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.shelfName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F7FA)
                            ) {
                                Text(
                                    text = "${item.restockEvents} restocks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimaryVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${item.unitsAdded} units",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dual-color progress bar showing restock volume vs shelf capacity
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        val fraction = (item.unitsAdded.toFloat() / item.maxCapacity).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00B4D8), TealPrimary)
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Replenishment Velocity Insight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weekly Replenished: 530 units across 22 restocking events",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "High Velocity",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }
        }
    }
}
