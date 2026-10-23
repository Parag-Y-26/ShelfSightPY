package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class Store(
    @PrimaryKey val id: String = "store_main",
    val name: String = "Fresh Mart",
    val storeType: String = "Grocery", // Grocery, Clothing, Electronics, Pharmacy, General, Other
    val totalShelves: Int = 4,
    val selectedFloorId: String = "floor_ground",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "floors")
data class Floor(
    @PrimaryKey val id: String,
    val storeId: String = "store_main",
    val name: String,
    val widthMeters: Float = 12f,
    val lengthMeters: Float = 8f,
    val orderIndex: Int = 0
)

@Entity(tableName = "shelves")
data class Shelf(
    @PrimaryKey val id: String,
    val storeId: String = "store_main",
    val floorId: String = "floor_ground",
    val shelfNumber: Int,
    val name: String,
    val category: String = "Grocery",
    val shape: String = "RECTANGLE", // RECTANGLE, SQUARE
    val widthMeters: Float = 3f,
    val lengthMeters: Float = 1f,
    val positionX: Float = 6f, // meters relative to floor
    val positionY: Float = 4f,
    val rotationDegrees: Float = 0f,
    val rowCount: Int = 2,
    val slotsPerRow: Int = 3
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val barcode: String,
    val name: String,
    val brand: String,
    val category: String,
    val netWeight: String = "70 g",
    val mrp: String = "₹14",
    val isVeg: Boolean = true,
    val isFssaiVerified: Boolean = true,
    val energyKcal: Int = 427,
    val proteinG: Float = 9f,
    val carbsG: Float = 60f,
    val fatG: Float = 16f,
    val sodiumMg: Int = 1100,
    val ingredients: String = "Wheat flour, palm oil, salt, iodized salt, spices and condiments",
    val manufacturer: String = "Nestlé India Limited, New Delhi",
    val shelfLife: String = "9 months from manufacture",
    val iconResName: String = "ic_product_maggi_noodles"
)

@Entity(tableName = "shelf_items")
data class ShelfItem(
    @PrimaryKey val id: String,
    val shelfId: String,
    val productId: String,
    val rowNumber: Int, // 1 = top, 2 = bottom
    val slotNumber: Int, // 1..3
    val quantity: Int,
    val confidence: Float = 0.95f,
    val lowStockThreshold: Int = 5,
    val lastCorrectedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "camera_calibrations")
data class CameraCalibration(
    @PrimaryKey val shelfId: String,
    val zoneLeft: Float = 0.15f,
    val zoneTop: Float = 0.35f,
    val zoneRight: Float = 0.85f,
    val zoneBottom: Float = 0.85f,
    val dwellCutoffSeconds: Float = 3.0f,
    val exitGraceSeconds: Float = 2.0f,
    val isCalibrated: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "observation_events")
data class ObservationEvent(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val shelfId: String,
    val shelfNumber: Int,
    val trackId: Int,
    val eventType: String, // "ZONE_ENTER", "DWELL_QUALIFIED", "ZONE_EXIT", "STOCK_CORRECTION"
    val dwellSeconds: Float = 0f,
    val details: String = ""
)
