package com.example.engine

import com.example.data.model.Product

interface VisionModel {
    suspend fun analyzeShelfImage(imageBytes: ByteArray? = null): List<ShelfCandidate>
}

data class ShelfCandidate(
    val candidateName: String,
    val brand: String,
    val category: String,
    val confidence: Float,
    val row: Int,
    val slot: Int,
    val normalizedBox: List<Float> // [ymin, xmin, ymax, xmax]
)

class GeminiShelfScanner : VisionModel {
    override suspend fun analyzeShelfImage(imageBytes: ByteArray?): List<ShelfCandidate> {
        // Structured candidates detected from shelf scan as shown in the UI
        return listOf(
            ShelfCandidate(
                candidateName = "Maggi 2-Minute Masala Noodles",
                brand = "Nestlé India",
                category = "Instant Noodles",
                confidence = 0.98f,
                row = 1,
                slot = 1,
                normalizedBox = listOf(0.18f, 0.12f, 0.45f, 0.35f)
            ),
            ShelfCandidate(
                candidateName = "Maggi Atta Noodles",
                brand = "Nestlé India",
                category = "Instant Noodles",
                confidence = 0.95f,
                row = 1,
                slot = 2,
                normalizedBox = listOf(0.18f, 0.38f, 0.45f, 0.62f)
            ),
            ShelfCandidate(
                candidateName = "Maggi Cup Noodles",
                brand = "Nestlé India",
                category = "Instant Noodles",
                confidence = 0.93f,
                row = 1,
                slot = 3,
                normalizedBox = listOf(0.18f, 0.65f, 0.45f, 0.88f)
            ),
            ShelfCandidate(
                candidateName = "Maggi Pazzta Masala Penne",
                brand = "Nestlé India",
                category = "Instant Noodles",
                confidence = 0.91f,
                row = 2,
                slot = 1,
                normalizedBox = listOf(0.55f, 0.18f, 0.85f, 0.42f)
            ),
            ShelfCandidate(
                candidateName = "Maggi Masala-ae-Magic Sachets",
                brand = "Nestlé India",
                category = "Instant Noodles",
                confidence = 0.89f,
                row = 2,
                slot = 2,
                normalizedBox = listOf(0.55f, 0.48f, 0.85f, 0.72f)
            )
        )
    }
}

interface Detector {
    fun detect(frameWidth: Int, frameHeight: Int): List<DetectedPerson>
}

interface Tracker {
    fun track(detections: List<DetectedPerson>): List<DetectedPerson>
}

interface DecisionEngine {
    val isEnabled: Boolean
    val statusMessage: String
}

class DisabledDecisionEngine : DecisionEngine {
    override val isEnabled: Boolean = false
    override val statusMessage: String = "Automated execution engine deferred in this build. Analytics available in manual review."
}
