package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CameraCalibration
import com.example.data.model.Floor
import com.example.data.model.ObservationEvent
import com.example.data.model.Product
import com.example.data.model.Shelf
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        Store::class,
        Floor::class,
        Shelf::class,
        Product::class,
        ShelfItem::class,
        CameraCalibration::class,
        ObservationEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShelfSightDatabase : RoomDatabase() {
    abstract fun dao(): ShelfSightDao

    companion object {
        @Volatile
        private var INSTANCE: ShelfSightDatabase? = null

        fun getInstance(context: Context): ShelfSightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShelfSightDatabase::class.java,
                    "shelfsight.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context).dao())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(dao: ShelfSightDao) {
            val store = Store(
                id = "store_main",
                name = "Fresh Mart",
                storeType = "Grocery",
                totalShelves = 4,
                selectedFloorId = "floor_ground"
            )
            dao.insertStore(store)

            val floors = listOf(
                Floor(id = "floor_ground", name = "Ground Floor", widthMeters = 12f, lengthMeters = 8f, orderIndex = 0),
                Floor(id = "floor_1", name = "Floor 1", widthMeters = 12f, lengthMeters = 8f, orderIndex = 1),
                Floor(id = "floor_2", name = "Floor 2", widthMeters = 12f, lengthMeters = 8f, orderIndex = 2)
            )
            dao.insertFloors(floors)

            val shelves = listOf(
                Shelf(
                    id = "shelf_1",
                    floorId = "floor_ground",
                    shelfNumber = 1,
                    name = "Shelf 1",
                    category = "Instant Noodles",
                    shape = "RECTANGLE",
                    widthMeters = 3f,
                    lengthMeters = 1f,
                    positionX = 6f,
                    positionY = 5.2f,
                    rotationDegrees = 0f,
                    rowCount = 2,
                    slotsPerRow = 3
                ),
                Shelf(
                    id = "shelf_2",
                    floorId = "floor_ground",
                    shelfNumber = 2,
                    name = "Shelf 2",
                    category = "Snacks",
                    shape = "RECTANGLE",
                    widthMeters = 3f,
                    lengthMeters = 1f,
                    positionX = 6f,
                    positionY = 2f,
                    rotationDegrees = 0f,
                    rowCount = 2,
                    slotsPerRow = 3
                ),
                Shelf(
                    id = "shelf_3",
                    floorId = "floor_ground",
                    shelfNumber = 3,
                    name = "Shelf 3",
                    category = "Dairy",
                    shape = "RECTANGLE",
                    widthMeters = 1f,
                    lengthMeters = 3f,
                    positionX = 10f,
                    positionY = 4.5f,
                    rotationDegrees = 90f,
                    rowCount = 2,
                    slotsPerRow = 3
                ),
                Shelf(
                    id = "shelf_4",
                    floorId = "floor_ground",
                    shelfNumber = 4,
                    name = "Shelf 4",
                    category = "Drinks",
                    shape = "RECTANGLE",
                    widthMeters = 1f,
                    lengthMeters = 3f,
                    positionX = 2f,
                    positionY = 4.5f,
                    rotationDegrees = 90f,
                    rowCount = 2,
                    slotsPerRow = 3
                )
            )
            dao.insertShelves(shelves)

            val products = listOf(
                Product(
                    id = "prod_1",
                    barcode = "8901058852349",
                    name = "Maggi 2-Minute Masala Noodles",
                    brand = "Nestlé India",
                    category = "Instant Noodles",
                    netWeight = "70 g",
                    mrp = "₹14",
                    isVeg = true,
                    isFssaiVerified = true,
                    energyKcal = 427,
                    proteinG = 9f,
                    carbsG = 60f,
                    fatG = 16f,
                    sodiumMg = 1100,
                    iconResName = "ic_product_maggi_noodles"
                ),
                Product(
                    id = "prod_2",
                    barcode = "8901058852356",
                    name = "Maggi Atta Noodles",
                    brand = "Nestlé India",
                    category = "Instant Noodles",
                    netWeight = "72 g",
                    mrp = "₹20",
                    isVeg = true,
                    isFssaiVerified = true,
                    energyKcal = 412,
                    proteinG = 10f,
                    carbsG = 63f,
                    fatG = 14f,
                    sodiumMg = 980,
                    iconResName = "ic_product_maggi_atta"
                ),
                Product(
                    id = "prod_3",
                    barcode = "8901058852363",
                    name = "Maggi Cup Noodles",
                    brand = "Nestlé India",
                    category = "Instant Noodles",
                    netWeight = "70 g",
                    mrp = "₹45",
                    isVeg = true,
                    isFssaiVerified = true,
                    energyKcal = 430,
                    proteinG = 8.5f,
                    carbsG = 61f,
                    fatG = 16.5f,
                    sodiumMg = 1150,
                    iconResName = "ic_product_maggi_cup"
                ),
                Product(
                    id = "prod_4",
                    barcode = "8901058852370",
                    name = "Maggi Pazzta Masala Penne",
                    brand = "Nestlé India",
                    category = "Instant Noodles",
                    netWeight = "64 g",
                    mrp = "₹28",
                    isVeg = true,
                    isFssaiVerified = true,
                    energyKcal = 380,
                    proteinG = 8f,
                    carbsG = 68f,
                    fatG = 6f,
                    sodiumMg = 890,
                    iconResName = "ic_product_maggi_pazzta"
                ),
                Product(
                    id = "prod_5",
                    barcode = "8901058852387",
                    name = "Maggi Masala-ae-Magic Sachets",
                    brand = "Nestlé India",
                    category = "Instant Noodles",
                    netWeight = "6 g",
                    mrp = "₹5",
                    isVeg = true,
                    isFssaiVerified = true,
                    energyKcal = 15,
                    proteinG = 0.5f,
                    carbsG = 2.5f,
                    fatG = 0.2f,
                    sodiumMg = 450,
                    iconResName = "ic_product_maggi_magic"
                )
            )
            dao.insertProducts(products)

            val shelfItems = listOf(
                ShelfItem(id = "item_1", shelfId = "shelf_1", productId = "prod_1", rowNumber = 1, slotNumber = 1, quantity = 24, confidence = 0.98f),
                ShelfItem(id = "item_2", shelfId = "shelf_1", productId = "prod_2", rowNumber = 1, slotNumber = 2, quantity = 18, confidence = 0.95f),
                ShelfItem(id = "item_3", shelfId = "shelf_1", productId = "prod_3", rowNumber = 1, slotNumber = 3, quantity = 12, confidence = 0.93f),
                ShelfItem(id = "item_4", shelfId = "shelf_1", productId = "prod_4", rowNumber = 2, slotNumber = 1, quantity = 9, confidence = 0.91f),
                ShelfItem(id = "item_5", shelfId = "shelf_1", productId = "prod_5", rowNumber = 2, slotNumber = 2, quantity = 30, confidence = 0.89f)
            )
            dao.insertShelfItems(shelfItems)

            // Initial camera calibration zone for Shelf 1
            dao.insertCalibration(
                CameraCalibration(
                    shelfId = "shelf_1",
                    zoneLeft = 0.18f,
                    zoneTop = 0.35f,
                    zoneRight = 0.82f,
                    zoneBottom = 0.82f,
                    dwellCutoffSeconds = 3.0f,
                    exitGraceSeconds = 2.0f
                )
            )

            // Seed historical visits matching the Analytics chart:
            // Shelf 2: 46 visits, Shelf 1: 38 visits, Shelf 4: 27 visits, Shelf 3: 17 visits
            val now = System.currentTimeMillis()
            val events = mutableListOf<ObservationEvent>()
            val visitCounts = mapOf("shelf_2" to 46, "shelf_1" to 38, "shelf_4" to 27, "shelf_3" to 17)
            val shelfNums = mapOf("shelf_1" to 1, "shelf_2" to 2, "shelf_3" to 3, "shelf_4" to 4)

            visitCounts.forEach { (shelfId, count) ->
                val shelfNum = shelfNums[shelfId] ?: 1
                for (i in 1..count) {
                    val eventTime = now - (count - i) * 3_600_000L - (i * 123_456L % 3_000_000L)
                    events.add(
                        ObservationEvent(
                            id = "event_${shelfId}_$i",
                            timestamp = eventTime,
                            shelfId = shelfId,
                            shelfNumber = shelfNum,
                            trackId = 100 + i,
                            eventType = "DWELL_QUALIFIED",
                            dwellSeconds = 4.2f + (i % 6),
                            details = "Shopper stayed in interaction zone of Shelf $shelfNum"
                        )
                    )
                }
            }
            dao.insertEvents(events)
        }
    }
}
