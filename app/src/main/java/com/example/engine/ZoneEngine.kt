package com.example.engine

import com.example.data.model.CameraCalibration
import com.example.data.model.ObservationEvent
import java.util.UUID

data class DetectedPerson(
    val trackId: Int,
    val bboxLeft: Float,
    val bboxTop: Float,
    val bboxRight: Float,
    val bboxBottom: Float,
    val confidence: Float
) {
    // Bottom-center coordinate used for ground-plane interaction zone intersection
    val bottomCenterX: Float get() = (bboxLeft + bboxRight) / 2f
    val bottomCenterY: Float get() = bboxBottom
}

enum class TrackStatus {
    OUTSIDE,
    IN_ZONE,
    QUALIFIED_DWELL,
    EXIT_GRACE
}

data class ActiveTrackState(
    val trackId: Int,
    var status: TrackStatus,
    var firstEnteredTimestamp: Long,
    var lastObservedTimestamp: Long,
    var qualifiedDwellEmitted: Boolean = false,
    var lastPositionX: Float = 0.5f,
    var lastPositionY: Float = 0.5f
)

class ZoneEngine(
    var calibration: CameraCalibration = CameraCalibration(shelfId = "shelf_1"),
    private val onEventEmitted: (ObservationEvent) -> Unit
) {
    private val activeTracks = mutableMapOf<Int, ActiveTrackState>()
    var fps: Float = 30f
        private set
    private var lastFrameTime = System.currentTimeMillis()
    private var frameCount = 0
    private var lastFpsUpdate = System.currentTimeMillis()

    fun updateCalibration(newCalibration: CameraCalibration) {
        this.calibration = newCalibration
    }

    fun processFrame(detectedPeople: List<DetectedPerson>, currentTimestamp: Long = System.currentTimeMillis()): List<ActiveTrackState> {
        // Update FPS counter
        frameCount++
        if (currentTimestamp - lastFpsUpdate >= 1000) {
            fps = (frameCount * 1000f) / (currentTimestamp - lastFpsUpdate)
            frameCount = 0
            lastFpsUpdate = currentTimestamp
        }

        val observedTrackIds = detectedPeople.map { it.trackId }.toSet()

        // 1. Process observed people
        for (person in detectedPeople) {
            val inZone = isPointInZone(person.bottomCenterX, person.bottomCenterY)
            val track = activeTracks.getOrPut(person.trackId) {
                ActiveTrackState(
                    trackId = person.trackId,
                    status = if (inZone) TrackStatus.IN_ZONE else TrackStatus.OUTSIDE,
                    firstEnteredTimestamp = currentTimestamp,
                    lastObservedTimestamp = currentTimestamp,
                    lastPositionX = person.bottomCenterX,
                    lastPositionY = person.bottomCenterY
                )
            }

            track.lastObservedTimestamp = currentTimestamp
            track.lastPositionX = person.bottomCenterX
            track.lastPositionY = person.bottomCenterY

            if (inZone) {
                if (track.status == TrackStatus.OUTSIDE || track.status == TrackStatus.EXIT_GRACE) {
                    track.status = TrackStatus.IN_ZONE
                    track.firstEnteredTimestamp = currentTimestamp
                    onEventEmitted(
                        ObservationEvent(
                            id = UUID.randomUUID().toString(),
                            timestamp = currentTimestamp,
                            shelfId = calibration.shelfId,
                            shelfNumber = 1,
                            trackId = person.trackId,
                            eventType = "ZONE_ENTER",
                            dwellSeconds = 0f,
                            details = "Shopper #${person.trackId} entered Shelf 1 zone"
                        )
                    )
                }

                val dwellSeconds = (currentTimestamp - track.firstEnteredTimestamp) / 1000f
                if (dwellSeconds >= calibration.dwellCutoffSeconds && !track.qualifiedDwellEmitted) {
                    track.status = TrackStatus.QUALIFIED_DWELL
                    track.qualifiedDwellEmitted = true
                    onEventEmitted(
                        ObservationEvent(
                            id = UUID.randomUUID().toString(),
                            timestamp = currentTimestamp,
                            shelfId = calibration.shelfId,
                            shelfNumber = 1,
                            trackId = person.trackId,
                            eventType = "DWELL_QUALIFIED",
                            dwellSeconds = dwellSeconds,
                            details = "Shopper #${person.trackId} reached qualifying dwell (${String.format("%.1f", dwellSeconds)}s)"
                        )
                    )
                }
            } else {
                // If previously in zone, initiate exit grace
                if (track.status == TrackStatus.IN_ZONE || track.status == TrackStatus.QUALIFIED_DWELL) {
                    track.status = TrackStatus.EXIT_GRACE
                }
            }
        }

        // 2. Handle missing tracks or grace expiration
        val toRemove = mutableListOf<Int>()
        for ((trackId, track) in activeTracks) {
            val isMissing = trackId !in observedTrackIds
            val timeSinceLastSeen = (currentTimestamp - track.lastObservedTimestamp) / 1000f

            if (track.status == TrackStatus.EXIT_GRACE || (isMissing && track.status != TrackStatus.OUTSIDE)) {
                if (timeSinceLastSeen >= calibration.exitGraceSeconds) {
                    val totalDwell = (track.lastObservedTimestamp - track.firstEnteredTimestamp) / 1000f
                    if (totalDwell >= 1.0f) {
                        onEventEmitted(
                            ObservationEvent(
                                id = UUID.randomUUID().toString(),
                                timestamp = currentTimestamp,
                                shelfId = calibration.shelfId,
                                shelfNumber = 1,
                                trackId = trackId,
                                eventType = "ZONE_EXIT",
                                dwellSeconds = totalDwell,
                                details = "Shopper #$trackId exited after ${String.format("%.1f", totalDwell)}s total dwell"
                            )
                        )
                    }
                    toRemove.add(trackId)
                }
            } else if (isMissing && track.status == TrackStatus.OUTSIDE) {
                toRemove.add(trackId)
            }
        }

        toRemove.forEach { activeTracks.remove(it) }

        return activeTracks.values.toList()
    }

    private fun isPointInZone(x: Float, y: Float): Boolean {
        return x >= calibration.zoneLeft &&
                x <= calibration.zoneRight &&
                y >= calibration.zoneTop &&
                y <= calibration.zoneBottom
    }
}
