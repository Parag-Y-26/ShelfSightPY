package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import com.example.data.model.CameraCalibration
import com.example.data.model.ObservationEvent
import com.example.engine.ActiveTrackState
import com.example.engine.TrackStatus
import com.example.ui.components.CameraPermissionRationaleCard
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StatusLive
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LiveMonitoringScreen(
    calibration: CameraCalibration,
    isMonitoring: Boolean,
    monitoringFps: Float,
    activeTracks: List<ActiveTrackState>,
    events: List<ObservationEvent>,
    isSimulatingShopper: Boolean,
    onToggleMonitoring: () -> Unit,
    onCalibrationUpdated: (Float, Float, Float, Float) -> Unit,
    onBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showPermissionCard by remember { mutableStateOf(!cameraPermissionState.status.isGranted) }

    var isCalibrating by remember { mutableStateOf(false) }

    var calLeft by remember(calibration) { mutableFloatStateOf(calibration.zoneLeft) }
    var calTop by remember(calibration) { mutableFloatStateOf(calibration.zoneTop) }
    var calRight by remember(calibration) { mutableFloatStateOf(calibration.zoneRight) }
    var calBottom by remember(calibration) { mutableFloatStateOf(calibration.zoneBottom) }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Live Monitoring",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMonitoring) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isMonitoring) StatusLive else Color(0xFF94A3B8), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMonitoring) "ACTIVE" else "STANDBY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMonitoring) Color(0xFF16A34A) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isCalibrating = !isCalibrating
                            if (!isCalibrating) {
                                onCalibrationUpdated(calLeft, calTop, calRight, calBottom)
                            }
                        },
                        modifier = Modifier.testTag("toggle_calibration_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Adjust,
                            contentDescription = "Calibrate Zone",
                            tint = if (isCalibrating) TealPrimaryVariant else TextSecondary
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
                .padding(16.dp)
        ) {
            // Viewfinder & Shopper Tracking Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F1E29))
                    .border(2.dp, if (isCalibrating) TealPrimary else Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .testTag("monitoring_viewfinder")
            ) {
                // Interactive / Overlay Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isCalibrating) {
                            if (isCalibrating) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    calRight = (calRight + dragAmount.x / size.width).coerceIn(calLeft + 0.1f, 0.98f)
                                    calBottom = (calBottom + dragAmount.y / size.height).coerceIn(calTop + 0.1f, 0.98f)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Simulated Shelf Backdrop Outline
                    drawRoundRect(
                        color = Color(0xFF1B3041),
                        topLeft = Offset(w * 0.1f, h * 0.08f),
                        size = Size(w * 0.8f, h * 0.32f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    // Shelf Bars
                    drawLine(
                        color = Color(0xFF2C4559),
                        start = Offset(w * 0.1f, h * 0.22f),
                        end = Offset(w * 0.9f, h * 0.22f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color(0xFF2C4559),
                        start = Offset(w * 0.1f, h * 0.38f),
                        end = Offset(w * 0.9f, h * 0.38f),
                        strokeWidth = 6f
                    )

                    // 2. Draw Calibrated Shopper Interaction Zone Box
                    val zoneX = calLeft * w
                    val zoneY = calTop * h
                    val zoneW = (calRight - calLeft) * w
                    val zoneH = (calBottom - calTop) * h

                    // Translucent fill
                    drawRoundRect(
                        color = if (isCalibrating) Color(0x3300BFA5) else Color(0x1A00B4D8),
                        topLeft = Offset(zoneX, zoneY),
                        size = Size(zoneW, zoneH),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    // Border stroke
                    drawRoundRect(
                        color = if (isCalibrating) Color(0xFF00BFA5) else Color(0xFF00B4D8),
                        topLeft = Offset(zoneX, zoneY),
                        size = Size(zoneW, zoneH),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = if (isCalibrating) PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f) else null
                        )
                    )

                    // 3. Draw Active Tracks & Bottom-Center Footprint
                    activeTracks.forEach { track ->
                        val footX = track.lastPositionX * w
                        val footY = track.lastPositionY * h
                        val personW = w * 0.16f
                        val personH = h * 0.35f
                        val boxLeft = footX - personW / 2f
                        val boxTop = footY - personH

                        val trackColor = when (track.status) {
                            TrackStatus.QUALIFIED_DWELL -> Color(0xFF00E676) // Green: Qualified Dwell
                            TrackStatus.IN_ZONE -> Color(0xFF00B4D8) // Cyan: Inside Zone
                            TrackStatus.EXIT_GRACE -> Color(0xFFF59E0B) // Amber: Grace
                            TrackStatus.OUTSIDE -> Color(0xFF94A3B8)
                        }

                        // Person bounding box
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(boxLeft, boxTop),
                            size = Size(personW, personH),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 2.5f)
                        )

                        // Shopper Bottom-Center Point (Crucial anchor used for zone checking)
                        drawCircle(
                            color = trackColor,
                            radius = 6f,
                            center = Offset(footX, footY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = Offset(footX, footY)
                        )
                    }
                }

                // Top Left Overlay: Telemetry info (FPS & Zone info)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val camStatus = if (cameraPermissionState.status.isGranted) "Camera: Live Feed" else "Camera: Simulated"
                    Text(
                        text = "$camStatus · ${String.format("%.1f", monitoringFps)} FPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Zone: Shelf 1 Interaction (Dwell: ${calibration.dwellCutoffSeconds.toInt()}s)",
                        fontSize = 10.sp,
                        color = CyanAccent
                    )
                }

                // Top Right: Mode indicator
                if (isCalibrating) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "DRAG TO CALIBRATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Center: Active tracks status
                val inZoneCount = activeTracks.count { it.status == TrackStatus.IN_ZONE || it.status == TrackStatus.QUALIFIED_DWELL }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = if (inZoneCount > 0) Color(0xFF00E676) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active Tracks: ${activeTracks.size} ($inZoneCount in-zone)",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Accompanist Camera Permission Rationale Overlay
                if (!cameraPermissionState.status.isGranted && showPermissionCard) {
                    CameraPermissionRationaleCard(
                        permissionState = cameraPermissionState,
                        onDismissOrSimulate = { showPermissionCard = false },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Monitoring Controls & Simulation Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        onToggleMonitoring()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoring) Color(0xFFEF4444) else TealPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_monitoring_button")
                ) {
                    Icon(
                        imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMonitoring) "Stop Monitoring" else "Start Monitoring",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (!isMonitoring) onToggleMonitoring()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("test_walkthrough_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = TealPrimaryVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simulate Walk",
                        color = TealPrimaryVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Event Ledger Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Observation Ledger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${events.size} recorded",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Feed List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events.take(30)) { event ->
                    EventLedgerRow(event = event, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
fun EventLedgerRow(event: ObservationEvent, dateFormat: SimpleDateFormat) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (badgeColor, label) = when (event.eventType) {
                "DWELL_QUALIFIED" -> Pair(Color(0xFF16A34A), "DWELL")
                "ZONE_ENTER" -> Pair(Color(0xFF0284C7), "ENTER")
                "ZONE_EXIT" -> Pair(Color(0xFF64748B), "EXIT")
                else -> Pair(Color(0xFFF59E0B), "STOCK")
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.details.ifEmpty { "Shelf ${event.shelfNumber} observation" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "Shelf ${event.shelfNumber} · Track #${event.trackId}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = dateFormat.format(Date(event.timestamp)),
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
