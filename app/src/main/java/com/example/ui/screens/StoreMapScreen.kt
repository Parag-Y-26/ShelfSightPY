package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Shelf
import com.example.ui.theme.AppBackground
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMapScreen(
    shelves: List<Shelf>,
    selectedShelfId: String = "shelf_1",
    onShelfUpdated: (shelfId: String, posX: Float, posY: Float, width: Float, length: Float, rotation: Float, shape: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedFloor by remember { mutableStateOf("Ground Floor") }
    var currentShelfId by remember { mutableStateOf(selectedShelfId) }
    val currentShelf = shelves.find { it.id == currentShelfId } ?: shelves.firstOrNull()

    var shelfWidth by remember { mutableFloatStateOf(currentShelf?.widthMeters ?: 3f) }
    var shelfLength by remember { mutableFloatStateOf(currentShelf?.lengthMeters ?: 1f) }
    var shelfPosX by remember { mutableFloatStateOf(currentShelf?.positionX ?: 6f) }
    var shelfPosY by remember { mutableFloatStateOf(currentShelf?.positionY ?: 5f) }
    var shelfRotation by remember { mutableFloatStateOf(currentShelf?.rotationDegrees ?: 0f) }
    var shelfShape by remember { mutableStateOf(currentShelf?.shape ?: "RECTANGLE") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Place ${currentShelf?.name ?: "Shelf 1"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onShelfUpdated(
                            currentShelfId,
                            shelfPosX,
                            shelfPosY,
                            shelfWidth,
                            shelfLength,
                            shelfRotation,
                            shelfShape
                        )
                        onBack()
                    }) {
                        Text(
                            text = "Done",
                            color = TealPrimaryVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Floor Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Ground Floor", "Floor 1", "Floor 2").forEach { floor ->
                    val isSelected = selectedFloor == floor
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) TealPrimary else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TealPrimary else Color(0xFFD6E2E8)
                        ),
                        modifier = Modifier
                            .clickable { selectedFloor = floor }
                            .testTag("floor_tab_${floor.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = floor,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6E2E8)),
                    modifier = Modifier.clickable { /* Add floor */ }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TealPrimaryVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add floor", fontSize = 13.sp, color = TealPrimaryVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Interactive Store Canvas with Room Walls & Shelves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                // Interactive Drag Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Convert drag pixels to floor meters (scale approx 30px = 1m)
                                shelfPosX = (shelfPosX + dragAmount.x / 30f).coerceIn(1.5f, 10.5f)
                                shelfPosY = (shelfPosY + dragAmount.y / 30f).coerceIn(1.5f, 6.5f)
                            }
                        }
                        .testTag("store_map_canvas")
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val scaleX = canvasWidth / 12f
                    val scaleY = canvasHeight / 8f
                    val scale = minOf(scaleX, scaleY) * 0.88f

                    val offsetX = (canvasWidth - 12f * scale) / 2f
                    val offsetY = (canvasHeight - 8f * scale) / 2f

                    // 1. Draw Grid Lines (1m cells)
                    val gridColor = Color(0xFFF1F5F9)
                    for (x in 0..12) {
                        drawLine(
                            color = gridColor,
                            start = Offset(offsetX + x * scale, offsetY),
                            end = Offset(offsetX + x * scale, offsetY + 8f * scale),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..8) {
                        drawLine(
                            color = gridColor,
                            start = Offset(offsetX, offsetY + y * scale),
                            end = Offset(offsetX + 12f * scale, offsetY + y * scale),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw Room Outer Walls
                    val wallColor = Color(0xFF334155)
                    val wallStroke = Stroke(width = 4f)
                    drawRoundRect(
                        color = wallColor,
                        topLeft = Offset(offsetX, offsetY),
                        size = Size(12f * scale, 8f * scale),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = wallStroke
                    )

                    // 3. Draw Bottom Entrance Door Opening
                    val entranceWidth = 2f * scale
                    val entranceX = offsetX + 5f * scale
                    val entranceY = offsetY + 8f * scale
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(entranceX, entranceY - 3f),
                        size = Size(entranceWidth, 6f)
                    )
                    // Door swings
                    drawLine(
                        color = wallColor,
                        start = Offset(entranceX, entranceY),
                        end = Offset(entranceX + 0.6f * scale, entranceY + 0.6f * scale),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = wallColor,
                        start = Offset(entranceX + entranceWidth, entranceY),
                        end = Offset(entranceX + entranceWidth - 0.6f * scale, entranceY + 0.6f * scale),
                        strokeWidth = 3f
                    )

                    // 4. Draw Pathway from Entrance to Shelf 1 (Dashed line)
                    val s1X = offsetX + shelfPosX * scale
                    val s1Y = offsetY + shelfPosY * scale
                    drawLine(
                        color = Color(0xFF2DD4BF),
                        start = Offset(offsetX + 6f * scale, entranceY),
                        end = Offset(s1X, s1Y + (shelfLength / 2f) * scale),
                        strokeWidth = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // 5. Draw Other Fixed Shelves (Shelf 2, Shelf 3, Shelf 4)
                    shelves.forEach { shelf ->
                        if (shelf.id != currentShelfId) {
                            val sx = offsetX + shelf.positionX * scale
                            val sy = offsetY + shelf.positionY * scale
                            val sw = (if (shelf.rotationDegrees == 90f) shelf.lengthMeters else shelf.widthMeters) * scale
                            val sl = (if (shelf.rotationDegrees == 90f) shelf.widthMeters else shelf.lengthMeters) * scale

                            drawRoundRect(
                                color = Color(0xFFE2E8F0),
                                topLeft = Offset(sx - sw / 2f, sy - sl / 2f),
                                size = Size(sw, sl),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = Color(0xFF94A3B8),
                                topLeft = Offset(sx - sw / 2f, sy - sl / 2f),
                                size = Size(sw, sl),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 1.5f)
                            )
                        }
                    }

                    // 6. Draw Selected Shelf (Shelf 1) in vibrant Teal with resize handles
                    val activeW = (if (shelfRotation == 90f) shelfLength else shelfWidth) * scale
                    val activeL = (if (shelfRotation == 90f) shelfWidth else shelfLength) * scale
                    val activeLeft = s1X - activeW / 2f
                    val activeTop = s1Y - activeL / 2f

                    // Shelf Body
                    drawRoundRect(
                        color = Color(0xFF00BFA5),
                        topLeft = Offset(activeLeft, activeTop),
                        size = Size(activeW, activeL),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFF00897B),
                        topLeft = Offset(activeLeft, activeTop),
                        size = Size(activeW, activeL),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )

                    // 4 Corner Resize Handles
                    val handleRadius = 6f
                    val corners = listOf(
                        Offset(activeLeft, activeTop),
                        Offset(activeLeft + activeW, activeTop),
                        Offset(activeLeft, activeTop + activeL),
                        Offset(activeLeft + activeW, activeTop + activeL)
                    )
                    corners.forEach { corner ->
                        drawCircle(color = Color.White, radius = handleRadius, center = corner)
                        drawCircle(color = Color(0xFF00897B), radius = handleRadius, center = corner, style = Stroke(2f))
                    }
                }

                // Floating Zoom / Re-center buttons at top right
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { /* Zoom in */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { /* Zoom out */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                shelfPosX = 6f
                                shelfPosY = 5.2f
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Center", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Entrance Label
                Text(
                    text = "Entrance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )

                // 1 cell = 1 m label
                Text(
                    text = "1 cell = 1 m",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            // Bottom Configuration Sheet
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Shelf shape",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rectangle Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (shelfShape == "RECTANGLE") Color(0xFFE0F7FA) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (shelfShape == "RECTANGLE") TealPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable {
                                    shelfShape = "RECTANGLE"
                                    shelfWidth = 3f
                                    shelfLength = 1f
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Rectangle,
                                    contentDescription = null,
                                    tint = if (shelfShape == "RECTANGLE") TealPrimaryVariant else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Rectangle", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Square Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (shelfShape == "SQUARE") Color(0xFFE0F7FA) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (shelfShape == "SQUARE") TealPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable {
                                    shelfShape = "SQUARE"
                                    shelfWidth = 2f
                                    shelfLength = 2f
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CropSquare,
                                    contentDescription = null,
                                    tint = if (shelfShape == "SQUARE") TealPrimaryVariant else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Square", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Dimension Steppers (Width & Length)
                        Column(modifier = Modifier.weight(1.8f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Width", fontSize = 11.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (shelfWidth > 1f) shelfWidth -= 0.5f },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                    Text("${shelfWidth.toInt()} m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    IconButton(
                                        onClick = { shelfWidth += 0.5f },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Length", fontSize = 11.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (shelfLength > 1f) shelfLength -= 0.5f },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                    Text("${shelfLength.toInt()} m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    IconButton(
                                        onClick = { shelfLength += 0.5f },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hint: Drag to move
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            tint = TealPrimaryVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Drag to move, pull corners to resize",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Rotate 90° & Confirm Location
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shelfRotation = (shelfRotation + 90f) % 360f },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("rotate_shelf_button")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = TealPrimaryVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rotate 90°", color = TealPrimaryVariant, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                onShelfUpdated(
                                    currentShelfId,
                                    shelfPosX,
                                    shelfPosY,
                                    shelfWidth,
                                    shelfLength,
                                    shelfRotation,
                                    shelfShape
                                )
                                onBack()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00B4D8), TealPrimary)
                                    )
                                )
                                .testTag("confirm_location_button")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm location", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
