package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CameraCalibration
import com.example.data.model.Floor
import com.example.data.model.ObservationEvent
import com.example.data.model.Product
import com.example.data.model.Shelf
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfSightDao {
    // Store
    @Query("SELECT * FROM stores LIMIT 1")
    fun getStoreFlow(): Flow<Store?>

    @Query("SELECT * FROM stores LIMIT 1")
    suspend fun getStore(): Store?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    // Floors
    @Query("SELECT * FROM floors ORDER BY orderIndex ASC")
    fun getFloorsFlow(): Flow<List<Floor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloors(floors: List<Floor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floor: Floor)

    // Shelves
    @Query("SELECT * FROM shelves WHERE floorId = :floorId ORDER BY shelfNumber ASC")
    fun getShelvesForFloorFlow(floorId: String): Flow<List<Shelf>>

    @Query("SELECT * FROM shelves ORDER BY shelfNumber ASC")
    fun getAllShelvesFlow(): Flow<List<Shelf>>

    @Query("SELECT * FROM shelves WHERE id = :shelfId LIMIT 1")
    suspend fun getShelf(shelfId: String): Shelf?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelves(shelves: List<Shelf>)

    @Update
    suspend fun updateShelf(shelf: Shelf)

    // Products
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    // Shelf Items
    @Query("SELECT * FROM shelf_items WHERE shelfId = :shelfId")
    fun getItemsForShelfFlow(shelfId: String): Flow<List<ShelfItem>>

    @Query("SELECT * FROM shelf_items")
    fun getAllShelfItemsFlow(): Flow<List<ShelfItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfItems(items: List<ShelfItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfItem(item: ShelfItem)

    @Update
    suspend fun updateShelfItem(item: ShelfItem)

    @Query("UPDATE shelf_items SET quantity = :quantity, lastCorrectedTimestamp = :timestamp WHERE id = :itemId")
    suspend fun updateShelfItemQuantity(itemId: String, quantity: Int, timestamp: Long = System.currentTimeMillis())

    // Calibrations
    @Query("SELECT * FROM camera_calibrations WHERE shelfId = :shelfId LIMIT 1")
    fun getCalibrationForShelfFlow(shelfId: String): Flow<CameraCalibration?>

    @Query("SELECT * FROM camera_calibrations WHERE shelfId = :shelfId LIMIT 1")
    suspend fun getCalibrationForShelf(shelfId: String): CameraCalibration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalibration(calibration: CameraCalibration)

    // Observation Events Ledger
    @Query("SELECT * FROM observation_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<ObservationEvent>>

    @Query("SELECT * FROM observation_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getEventsSinceFlow(sinceTimestamp: Long): Flow<List<ObservationEvent>>

    @Query("SELECT * FROM observation_events ORDER BY timestamp DESC")
    suspend fun getAllEventsList(): List<ObservationEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ObservationEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ObservationEvent>)

    @Query("DELETE FROM observation_events")
    suspend fun clearEvents()
}
