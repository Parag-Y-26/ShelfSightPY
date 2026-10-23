package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CameraCalibration
import com.example.data.model.ObservationEvent
import com.example.data.model.Product
import com.example.data.model.Shelf
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreShelfSightRepository(
    private val firestore: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = Firebase.auth
) {
    // Primary constructor for Android runtime resolving the database ID from XML
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(userId: String) = firestore.collection("users").document(userId)

    fun observeStore(userId: String): Flow<Store?> {
        val path = "users/$userId/stores/store_main"
        return userDoc(userId).collection("stores").document("store_main")
            .snapshots()
            .map { doc ->
                if (doc.exists()) {
                    Store(
                        id = doc.getString("id") ?: "store_main",
                        name = doc.getString("name") ?: "Fresh Mart",
                        storeType = doc.getString("storeType") ?: "Grocery",
                        totalShelves = (doc.getLong("totalShelves") ?: 4L).toInt(),
                        selectedFloorId = doc.getString("selectedFloorId") ?: "floor_ground",
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                } else null
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                throw e
            }
    }

    fun observeShelves(userId: String): Flow<List<Shelf>> {
        val path = "users/$userId/shelves"
        return userDoc(userId).collection("shelves")
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val shelfNumber = (doc.getLong("shelfNumber") ?: 1L).toInt()
                    val name = doc.getString("name") ?: "Shelf $shelfNumber"
                    val category = doc.getString("category") ?: "Grocery"
                    val shape = doc.getString("shape") ?: "RECTANGLE"
                    Shelf(
                        id = id,
                        storeId = doc.getString("storeId") ?: "store_main",
                        floorId = doc.getString("floorId") ?: "floor_ground",
                        shelfNumber = shelfNumber,
                        name = name,
                        category = category,
                        shape = shape,
                        widthMeters = (doc.getDouble("widthMeters") ?: 3.0).toFloat(),
                        lengthMeters = (doc.getDouble("lengthMeters") ?: 1.0).toFloat(),
                        positionX = (doc.getDouble("positionX") ?: 6.0).toFloat(),
                        positionY = (doc.getDouble("positionY") ?: 4.0).toFloat(),
                        rotationDegrees = (doc.getDouble("rotationDegrees") ?: 0.0).toFloat(),
                        rowCount = (doc.getLong("rowCount") ?: 2L).toInt(),
                        slotsPerRow = (doc.getLong("slotsPerRow") ?: 3L).toInt()
                    )
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    fun observeShelfItems(userId: String): Flow<List<ShelfItem>> {
        val path = "users/$userId/shelf_items"
        return userDoc(userId).collection("shelf_items")
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val shelfId = doc.getString("shelfId") ?: return@mapNotNull null
                    val productId = doc.getString("productId") ?: return@mapNotNull null
                    val row = (doc.getLong("rowNumber") ?: 1L).toInt()
                    val slot = (doc.getLong("slotNumber") ?: 1L).toInt()
                    val qty = (doc.getLong("quantity") ?: 0L).toInt()
                    val conf = (doc.getDouble("confidence") ?: 0.95).toFloat()
                    val threshold = (doc.getLong("lowStockThreshold") ?: 5L).toInt()
                    ShelfItem(
                        id = id,
                        shelfId = shelfId,
                        productId = productId,
                        rowNumber = row,
                        slotNumber = slot,
                        quantity = qty,
                        confidence = conf,
                        lowStockThreshold = threshold,
                        lastCorrectedTimestamp = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    fun observeEvents(userId: String): Flow<List<ObservationEvent>> {
        val path = "users/$userId/observation_events"
        return userDoc(userId).collection("observation_events")
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val shelfId = doc.getString("shelfId") ?: "shelf_1"
                    val shelfNum = (doc.getLong("shelfNumber") ?: 1L).toInt()
                    val trackId = (doc.getLong("trackId") ?: 0L).toInt()
                    val eventType = doc.getString("eventType") ?: "DWELL_QUALIFIED"
                    val dwellSec = (doc.getDouble("dwellSeconds") ?: 0.0).toFloat()
                    val details = doc.getString("details") ?: ""
                    val ts = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    ObservationEvent(
                        id = id,
                        timestamp = ts,
                        shelfId = shelfId,
                        shelfNumber = shelfNum,
                        trackId = trackId,
                        eventType = eventType,
                        dwellSeconds = dwellSec,
                        details = details
                    )
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun saveStore(store: Store): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("stores").document(store.id)
            val path = docRef.path
            val snapshot = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "id" to store.id,
                "userId" to uid,
                "name" to store.name,
                "storeType" to store.storeType,
                "totalShelves" to store.totalShelves,
                "selectedFloorId" to store.selectedFloorId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "stores/${store.id}")
            Result.failure(e)
        }
    }

    suspend fun saveShelf(shelf: Shelf): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("shelves").document(shelf.id)
            val path = docRef.path
            val snapshot = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "id" to shelf.id,
                "userId" to uid,
                "storeId" to shelf.storeId,
                "floorId" to shelf.floorId,
                "shelfNumber" to shelf.shelfNumber,
                "name" to shelf.name,
                "category" to shelf.category,
                "shape" to shelf.shape,
                "widthMeters" to shelf.widthMeters.toDouble(),
                "lengthMeters" to shelf.lengthMeters.toDouble(),
                "positionX" to shelf.positionX.toDouble(),
                "positionY" to shelf.positionY.toDouble(),
                "rotationDegrees" to shelf.rotationDegrees.toDouble(),
                "rowCount" to shelf.rowCount,
                "slotsPerRow" to shelf.slotsPerRow,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "shelves/${shelf.id}")
            Result.failure(e)
        }
    }

    suspend fun saveShelfItem(item: ShelfItem): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("shelf_items").document(item.id)
            val path = docRef.path
            val snapshot = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "id" to item.id,
                "userId" to uid,
                "shelfId" to item.shelfId,
                "productId" to item.productId,
                "rowNumber" to item.rowNumber,
                "slotNumber" to item.slotNumber,
                "quantity" to item.quantity,
                "confidence" to item.confidence.toDouble(),
                "lowStockThreshold" to item.lowStockThreshold,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "shelf_items/${item.id}")
            Result.failure(e)
        }
    }

    suspend fun updateItemQuantity(itemId: String, quantity: Int): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("shelf_items").document(itemId)
            val path = docRef.path
            docRef.update(
                mapOf(
                    "quantity" to quantity,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "shelf_items/$itemId")
            Result.failure(e)
        }
    }

    suspend fun recordObservation(event: ObservationEvent): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("observation_events").document(event.id)
            val path = docRef.path
            val payload = hashMapOf<String, Any>(
                "id" to event.id,
                "userId" to uid,
                "shelfId" to event.shelfId,
                "shelfNumber" to event.shelfNumber,
                "trackId" to event.trackId,
                "eventType" to event.eventType,
                "dwellSeconds" to event.dwellSeconds.toDouble(),
                "details" to event.details,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "observation_events/${event.id}")
            Result.failure(e)
        }
    }

    suspend fun saveCalibration(calibration: CameraCalibration): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("camera_calibrations").document(calibration.shelfId)
            val snapshot = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "id" to calibration.shelfId,
                "userId" to uid,
                "shelfId" to calibration.shelfId,
                "zoneLeft" to calibration.zoneLeft.toDouble(),
                "zoneTop" to calibration.zoneTop.toDouble(),
                "zoneRight" to calibration.zoneRight.toDouble(),
                "zoneBottom" to calibration.zoneBottom.toDouble(),
                "dwellCutoffSeconds" to calibration.dwellCutoffSeconds.toDouble(),
                "exitGraceSeconds" to calibration.exitGraceSeconds.toDouble(),
                "isCalibrated" to calibration.isCalibrated,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "camera_calibrations/${calibration.shelfId}")
            Result.failure(e)
        }
    }

    suspend fun saveProduct(product: Product): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("products").document(product.id)
            val snapshot = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "id" to product.id,
                "userId" to uid,
                "barcode" to product.barcode,
                "name" to product.name,
                "brand" to product.brand,
                "category" to product.category,
                "netWeight" to product.netWeight,
                "mrp" to product.mrp,
                "isVeg" to product.isVeg,
                "isFssaiVerified" to product.isFssaiVerified,
                "energyKcal" to product.energyKcal,
                "proteinG" to product.proteinG.toDouble(),
                "carbsG" to product.carbsG.toDouble(),
                "fatG" to product.fatG.toDouble(),
                "sodiumMg" to product.sodiumMg,
                "ingredients" to product.ingredients,
                "manufacturer" to product.manufacturer,
                "shelfLife" to product.shelfLife,
                "iconResName" to product.iconResName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "products/${product.id}")
            Result.failure(e)
        }
    }
}
