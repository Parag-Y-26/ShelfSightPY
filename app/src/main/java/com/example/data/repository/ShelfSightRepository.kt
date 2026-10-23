package com.example.data.repository

import android.content.Context
import com.example.data.local.ShelfSightDao
import com.example.data.local.ShelfSightDatabase
import com.example.data.model.CameraCalibration
import com.example.data.model.Floor
import com.example.data.model.ObservationEvent
import com.example.data.model.Product
import com.example.data.model.Shelf
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ShelfSightRepository(private val dao: ShelfSightDao) {

    val storeFlow: Flow<Store?> = dao.getStoreFlow()
    val floorsFlow: Flow<List<Floor>> = dao.getFloorsFlow()
    val allShelvesFlow: Flow<List<Shelf>> = dao.getAllShelvesFlow()
    val allProductsFlow: Flow<List<Product>> = dao.getAllProductsFlow()
    val allShelfItemsFlow: Flow<List<ShelfItem>> = dao.getAllShelfItemsFlow()
    val allEventsFlow: Flow<List<ObservationEvent>> = dao.getAllEventsFlow()

    fun getShelvesForFloorFlow(floorId: String): Flow<List<Shelf>> = dao.getShelvesForFloorFlow(floorId)
    fun getItemsForShelfFlow(shelfId: String): Flow<List<ShelfItem>> = dao.getItemsForShelfFlow(shelfId)
    fun getCalibrationFlow(shelfId: String): Flow<CameraCalibration?> = dao.getCalibrationForShelfFlow(shelfId)

    suspend fun updateStore(name: String, storeType: String, totalShelves: Int) {
        val current = dao.getStore() ?: Store()
        dao.insertStore(current.copy(name = name, storeType = storeType, totalShelves = totalShelves))
    }

    suspend fun updateShelfPosition(
        shelfId: String,
        posX: Float,
        posY: Float,
        widthMeters: Float,
        lengthMeters: Float,
        rotationDegrees: Float,
        shape: String
    ) {
        val current = dao.getShelf(shelfId) ?: return
        dao.updateShelf(
            current.copy(
                positionX = posX,
                positionY = posY,
                widthMeters = widthMeters,
                lengthMeters = lengthMeters,
                rotationDegrees = rotationDegrees,
                shape = shape
            )
        )
    }

    suspend fun updateItemQuantity(itemId: String, quantity: Int) {
        dao.updateShelfItemQuantity(itemId, quantity)
        // Record inventory event
        val event = ObservationEvent(
            id = UUID.randomUUID().toString(),
            shelfId = "shelf_1",
            shelfNumber = 1,
            trackId = 0,
            eventType = "STOCK_CORRECTION",
            dwellSeconds = 0f,
            details = "Item $itemId quantity manually corrected to $quantity"
        )
        dao.insertEvent(event)
    }

    suspend fun saveCalibration(calibration: CameraCalibration) {
        dao.insertCalibration(calibration)
    }

    suspend fun recordObservation(event: ObservationEvent) {
        dao.insertEvent(event)
    }

    suspend fun addProductToShelf(
        productId: String,
        shelfId: String,
        rowNumber: Int,
        slotNumber: Int,
        quantity: Int
    ) {
        val newItem = ShelfItem(
            id = "item_${UUID.randomUUID().toString().take(8)}",
            shelfId = shelfId,
            productId = productId,
            rowNumber = rowNumber,
            slotNumber = slotNumber,
            quantity = quantity,
            confidence = 1.0f
        )
        dao.insertShelfItem(newItem)
    }

    suspend fun exportEventsToCsv(context: Context): String = withContext(Dispatchers.IO) {
        val events = dao.getAllEventsList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csvBuilder = StringBuilder()
        csvBuilder.append("Event ID,Timestamp,Date Time,Shelf ID,Shelf Number,Track ID,Event Type,Dwell Seconds,Details\n")
        events.forEach { e ->
            val formattedDate = dateFormat.format(Date(e.timestamp))
            csvBuilder.append("${e.id},${e.timestamp},\"$formattedDate\",${e.shelfId},${e.shelfNumber},${e.trackId},${e.eventType},${e.dwellSeconds},\"${e.details.replace("\"", "\"\"")}\"\n")
        }

        val exportFile = File(context.cacheDir, "shelfsight_events_ledger_${System.currentTimeMillis()}.csv")
        exportFile.writeText(csvBuilder.toString())
        exportFile.absolutePath
    }

    suspend fun resetToSeedData() {
        dao.clearEvents()
        ShelfSightDatabase.seedInitialData(dao)
    }
}
