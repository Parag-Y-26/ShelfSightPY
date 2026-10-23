package com.example.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ShelfSightDatabase
import com.example.data.model.CameraCalibration
import com.example.data.model.Floor
import com.example.data.model.ObservationEvent
import com.example.data.model.Product
import com.example.data.model.Shelf
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import com.example.data.repository.ShelfSightRepository
import com.example.engine.ActiveTrackState
import com.example.engine.DetectedPerson
import com.example.engine.DisabledDecisionEngine
import com.example.engine.GeminiShelfScanner
import com.example.engine.ZoneEngine
import com.example.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.data.repository.FirestoreShelfSightRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

data class UiState(
    val currentScreen: Screen = Screen.Dashboard,
    val isLoggedIn: Boolean = true,
    val userName: String = "Parag",
    val store: Store? = null,
    val floors: List<Floor> = emptyList(),
    val selectedFloorId: String = "floor_ground",
    val shelves: List<Shelf> = emptyList(),
    val selectedShelfId: String = "shelf_1",
    val shelfItems: List<ShelfItem> = emptyList(),
    val products: List<Product> = emptyList(),
    val events: List<ObservationEvent> = emptyList(),
    val calibration: CameraCalibration = CameraCalibration(shelfId = "shelf_1"),
    val selectedProductIndex: Int = 1, // 1..5 for highlighting shelf <-> list
    val isMonitoring: Boolean = false,
    val monitoringFps: Float = 24.5f,
    val activeTracks: List<ActiveTrackState> = emptyList(),
    val isSimulatingShopper: Boolean = false,
    val analyticsPeriod: String = "7 Days", // "Today", "7 Days", "30 Days", "Custom"
    val serverHost: String = "192.168.1.104",
    val serverPort: String = "8000",
    val certFingerprint: String = "SHA256:7B:4F:92:A1:0C:8E:53:DF:6B:42:19",
    val connectionStatus: String = "Connected · Edge Server Active",
    val infoMessage: String? = null
)

class ShelfSightViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ShelfSightDatabase.getInstance(application)
    private val repository = ShelfSightRepository(db.dao())
    private val firestoreRepo = FirestoreShelfSightRepository(application)
    private val geminiScanner = GeminiShelfScanner()
    private val decisionEngine = DisabledDecisionEngine()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var zoneEngine: ZoneEngine? = null
    private var simulationJob: Job? = null

    init {
        // Collect flows from Room repository
        repository.storeFlow.onEach { store ->
            _uiState.value = _uiState.value.copy(store = store)
        }.launchIn(viewModelScope)

        repository.floorsFlow.onEach { floors ->
            _uiState.value = _uiState.value.copy(floors = floors)
        }.launchIn(viewModelScope)

        repository.allShelvesFlow.onEach { shelves ->
            _uiState.value = _uiState.value.copy(shelves = shelves)
        }.launchIn(viewModelScope)

        repository.allProductsFlow.onEach { products ->
            _uiState.value = _uiState.value.copy(products = products)
        }.launchIn(viewModelScope)

        repository.allShelfItemsFlow.onEach { items ->
            _uiState.value = _uiState.value.copy(shelfItems = items)
        }.launchIn(viewModelScope)

        repository.allEventsFlow.onEach { events ->
            _uiState.value = _uiState.value.copy(events = events)
        }.launchIn(viewModelScope)

        repository.getCalibrationFlow("shelf_1").onEach { cal ->
            val calibration = cal ?: CameraCalibration(shelfId = "shelf_1")
            _uiState.value = _uiState.value.copy(calibration = calibration)
            zoneEngine?.updateCalibration(calibration)
        }.launchIn(viewModelScope)

        // Initialize Zone Engine with dual persistence to Room and Firestore
        zoneEngine = ZoneEngine(
            calibration = _uiState.value.calibration,
            onEventEmitted = { event ->
                viewModelScope.launch {
                    repository.recordObservation(event)
                    firestoreRepo.recordObservation(event)
                }
            }
        )

        // Initial sync if user is already authenticated
        if (Firebase.auth.currentUser != null) {
            syncCurrentStateToFirestore()
        }
    }

    fun syncCurrentStateToFirestore() {
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            _uiState.value.store?.let { firestoreRepo.saveStore(it) }
            _uiState.value.shelves.forEach { firestoreRepo.saveShelf(it) }
            _uiState.value.products.forEach { firestoreRepo.saveProduct(it) }
            _uiState.value.shelfItems.forEach { firestoreRepo.saveShelfItem(it) }
            firestoreRepo.saveCalibration(_uiState.value.calibration)
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun selectFloor(floorId: String) {
        _uiState.value = _uiState.value.copy(selectedFloorId = floorId)
    }

    fun selectShelf(shelfId: String) {
        _uiState.value = _uiState.value.copy(selectedShelfId = shelfId)
        viewModelScope.launch {
            repository.getCalibrationFlow(shelfId).collect { cal ->
                val calibration = cal ?: CameraCalibration(shelfId = shelfId)
                _uiState.value = _uiState.value.copy(calibration = calibration)
                zoneEngine?.updateCalibration(calibration)
            }
        }
    }

    fun selectProductIndex(index: Int) {
        _uiState.value = _uiState.value.copy(selectedProductIndex = index)
    }

    fun updateItemQuantity(itemId: String, delta: Int) {
        val currentItem = _uiState.value.shelfItems.find { it.id == itemId } ?: return
        val newQty = (currentItem.quantity + delta).coerceAtLeast(0)
        viewModelScope.launch {
            repository.updateItemQuantity(itemId, newQty)
            firestoreRepo.updateItemQuantity(itemId, newQty)
        }
    }

    fun updateStoreSetup(name: String, storeType: String, totalShelves: Int) {
        viewModelScope.launch {
            repository.updateStore(name, storeType, totalShelves)
            val updated = (_uiState.value.store ?: Store()).copy(name = name, storeType = storeType, totalShelves = totalShelves)
            firestoreRepo.saveStore(updated)
            _uiState.value = _uiState.value.copy(currentScreen = Screen.ShelfScanLayout)
        }
    }

    fun updateShelfPlacement(
        shelfId: String,
        posX: Float,
        posY: Float,
        widthMeters: Float,
        lengthMeters: Float,
        rotationDegrees: Float,
        shape: String
    ) {
        viewModelScope.launch {
            repository.updateShelfPosition(
                shelfId = shelfId,
                posX = posX,
                posY = posY,
                widthMeters = widthMeters,
                lengthMeters = lengthMeters,
                rotationDegrees = rotationDegrees,
                shape = shape
            )
            val currentShelf = _uiState.value.shelves.find { it.id == shelfId }
            if (currentShelf != null) {
                firestoreRepo.saveShelf(
                    currentShelf.copy(
                        positionX = posX,
                        positionY = posY,
                        widthMeters = widthMeters,
                        lengthMeters = lengthMeters,
                        rotationDegrees = rotationDegrees,
                        shape = shape
                    )
                )
            }
        }
    }

    fun addProductToCurrentShelf(
        productId: String,
        rowNumber: Int,
        slotNumber: Int,
        quantity: Int
    ) {
        viewModelScope.launch {
            repository.addProductToShelf(
                productId = productId,
                shelfId = _uiState.value.selectedShelfId,
                rowNumber = rowNumber,
                slotNumber = slotNumber,
                quantity = quantity
            )
            val newItem = ShelfItem(
                id = "item_${UUID.randomUUID().toString().take(8)}",
                shelfId = _uiState.value.selectedShelfId,
                productId = productId,
                rowNumber = rowNumber,
                slotNumber = slotNumber,
                quantity = quantity,
                confidence = 1.0f
            )
            firestoreRepo.saveShelfItem(newItem)
            _uiState.value = _uiState.value.copy(currentScreen = Screen.ShelfScanLayout)
        }
    }

    fun updateZoneCalibration(left: Float, top: Float, right: Float, bottom: Float) {
        val updated = _uiState.value.calibration.copy(
            zoneLeft = left.coerceIn(0.05f, 0.95f),
            zoneTop = top.coerceIn(0.05f, 0.95f),
            zoneRight = right.coerceIn(left + 0.1f, 0.98f),
            zoneBottom = bottom.coerceIn(top + 0.1f, 0.98f),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(calibration = updated)
        zoneEngine?.updateCalibration(updated)
        viewModelScope.launch {
            repository.saveCalibration(updated)
            firestoreRepo.saveCalibration(updated)
        }
    }

    fun toggleMonitoring() {
        val next = !_uiState.value.isMonitoring
        _uiState.value = _uiState.value.copy(isMonitoring = next)
        if (next) {
            startShopperSimulation()
        } else {
            stopShopperSimulation()
        }
    }

    fun startShopperSimulation() {
        simulationJob?.cancel()
        _uiState.value = _uiState.value.copy(isSimulatingShopper = true)
        simulationJob = viewModelScope.launch {
            var step = 0
            val trackId = 101
            while (_uiState.value.isMonitoring) {
                // Simulate realistic shopper walking across camera view:
                // Steps 0..5: walking toward zone
                // Steps 6..18: staying inside calibrated interaction zone (dwell accumulates)
                // Steps 19..25: walking away out of frame
                val progress = (step % 26) / 26f
                val (x, y) = when {
                    step % 26 < 6 -> Pair(0.1f + progress * 1.5f, 0.9f - progress * 0.5f)
                    step % 26 in 6..18 -> Pair(0.48f + ((step % 3) * 0.02f), 0.65f)
                    else -> Pair(0.55f + ((step % 26 - 18) * 0.05f), 0.65f + ((step % 26 - 18) * 0.04f))
                }

                val person = DetectedPerson(
                    trackId = trackId,
                    bboxLeft = x - 0.12f,
                    bboxTop = y - 0.35f,
                    bboxRight = x + 0.12f,
                    bboxBottom = y,
                    confidence = 0.94f
                )

                val engine = zoneEngine ?: ZoneEngine(calibration = _uiState.value.calibration) {}
                val updatedTracks = engine.processFrame(listOf(person))
                _uiState.value = _uiState.value.copy(
                    activeTracks = updatedTracks,
                    monitoringFps = 25.2f + (step % 4) * 0.5f
                )

                step++
                delay(300)
            }
        }
    }

    fun stopShopperSimulation() {
        simulationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isSimulatingShopper = false,
            activeTracks = emptyList()
        )
    }

    fun setAnalyticsPeriod(period: String) {
        _uiState.value = _uiState.value.copy(analyticsPeriod = period)
    }

    fun exportCsv() {
        viewModelScope.launch {
            val path = repository.exportEventsToCsv(getApplication())
            Toast.makeText(getApplication(), "Exported Event Ledger to:\n$path", Toast.LENGTH_LONG).show()
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        _uiState.value = _uiState.value.copy(isLoggedIn = false, currentScreen = Screen.Login)
    }

    fun login(email: String) {
        val displayName = if (email.contains("parag", ignoreCase = true)) "Parag" else email.substringBefore("@").replaceFirstChar { it.uppercase() }
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            userName = displayName,
            currentScreen = Screen.Dashboard
        )
        syncCurrentStateToFirestore()
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToSeedData()
            Toast.makeText(getApplication(), "ShelfSight data reset to initial showcase demo state", Toast.LENGTH_SHORT).show()
        }
    }
}
