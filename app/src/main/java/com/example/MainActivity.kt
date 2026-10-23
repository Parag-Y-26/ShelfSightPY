package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddProductScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LiveMonitoringScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShelfScanLayoutScreen
import com.example.ui.screens.StoreMapScreen
import com.example.ui.screens.StoreSetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ShelfSightViewModel

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: ShelfSightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
                var isDemoSession by remember { mutableStateOf(false) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        if (auth.currentUser == null) {
                            if (!isDemoSession) {
                                viewModel.navigateTo(Screen.Login)
                            }
                        } else {
                            viewModel.login(auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Merchant")
                        }
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                val isAuthed = currentUser != null || isDemoSession

                if (!isAuthed) {
                    LoginScreen(
                        onLoginSuccess = { email ->
                            isDemoSession = true
                            viewModel.login(email)
                        },
                        onNavigateToStoreSetup = { viewModel.navigateTo(Screen.StoreSetup) },
                        onDemoMode = {
                            isDemoSession = true
                            viewModel.login("Demo Store Lead")
                        }
                    )
                } else {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Top level back handling
                    if (uiState.currentScreen != Screen.Dashboard && uiState.currentScreen != Screen.Login) {
                        BackHandler {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        when (uiState.currentScreen) {
                            is Screen.Login -> {
                                LoginScreen(
                                    onLoginSuccess = { email ->
                                        isDemoSession = true
                                        viewModel.login(email)
                                    },
                                    onNavigateToStoreSetup = { viewModel.navigateTo(Screen.StoreSetup) },
                                    onDemoMode = {
                                        isDemoSession = true
                                        viewModel.login("Demo Store Lead")
                                    }
                                )
                            }

                        is Screen.StoreSetup -> {
                            StoreSetupScreen(
                                initialStoreName = uiState.store?.name ?: "Fresh Mart",
                                initialShelves = uiState.store?.totalShelves ?: 4,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                                onContinue = { name, type, shelves ->
                                    viewModel.updateStoreSetup(name, type, shelves)
                                }
                            )
                        }

                        is Screen.Dashboard -> {
                            DashboardScreen(
                                userName = uiState.userName,
                                storeName = uiState.store?.name ?: "Fresh Mart",
                                floorName = uiState.floors.find { it.id == uiState.selectedFloorId }?.name ?: "Ground Floor",
                                isLiveMonitoring = uiState.isMonitoring,
                                onNavigateToMonitoring = { viewModel.navigateTo(Screen.Monitoring) },
                                onNavigateToAnalytics = { viewModel.navigateTo(Screen.Analytics) },
                                onNavigateToMap = { viewModel.navigateTo(Screen.StoreMap) },
                                onNavigateToShelfLayout = { viewModel.navigateTo(Screen.ShelfScanLayout) },
                                onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) }
                            )
                        }

                        is Screen.ShelfScanLayout -> {
                            val selectedNum = uiState.shelves.find { it.id == uiState.selectedShelfId }?.shelfNumber ?: 1
                            ShelfScanLayoutScreen(
                                selectedShelfNumber = selectedNum,
                                products = uiState.products,
                                shelfItems = uiState.shelfItems.filter { it.shelfId == uiState.selectedShelfId },
                                selectedIndex = uiState.selectedProductIndex,
                                onSelectShelf = { num ->
                                    val target = uiState.shelves.find { it.shelfNumber == num }
                                    if (target != null) viewModel.selectShelf(target.id)
                                },
                                onSelectProductIndex = { idx -> viewModel.selectProductIndex(idx) },
                                onQuantityChanged = { itemId, delta -> viewModel.updateItemQuantity(itemId, delta) },
                                onAddMissingProduct = { viewModel.navigateTo(Screen.AddProduct) },
                                onRescan = { viewModel.navigateTo(Screen.Monitoring) },
                                onSaveShelf = { viewModel.navigateTo(Screen.StoreMap) },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }

                        is Screen.AddProduct -> {
                            AddProductScreen(
                                onClose = { viewModel.navigateTo(Screen.ShelfScanLayout) },
                                onSaveProduct = { prodId, row, slot, qty ->
                                    viewModel.addProductToCurrentShelf(prodId, row, slot, qty)
                                }
                            )
                        }

                        is Screen.StoreMap -> {
                            StoreMapScreen(
                                shelves = uiState.shelves,
                                selectedShelfId = uiState.selectedShelfId,
                                onShelfUpdated = { sId, x, y, w, l, rot, shape ->
                                    viewModel.updateShelfPlacement(sId, x, y, w, l, rot, shape)
                                },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }

                        is Screen.Monitoring -> {
                            LiveMonitoringScreen(
                                calibration = uiState.calibration,
                                isMonitoring = uiState.isMonitoring,
                                monitoringFps = uiState.monitoringFps,
                                activeTracks = uiState.activeTracks,
                                events = uiState.events,
                                isSimulatingShopper = uiState.isSimulatingShopper,
                                onToggleMonitoring = { viewModel.toggleMonitoring() },
                                onCalibrationUpdated = { l, t, r, b ->
                                    viewModel.updateZoneCalibration(l, t, r, b)
                                },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }

                        is Screen.Analytics -> {
                            AnalyticsScreen(
                                currentPeriod = uiState.analyticsPeriod,
                                onPeriodSelected = { p -> viewModel.setAnalyticsPeriod(p) },
                                onExportCsv = { viewModel.exportCsv() },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                serverHost = uiState.serverHost,
                                serverPort = uiState.serverPort,
                                certFingerprint = uiState.certFingerprint,
                                connectionStatus = uiState.connectionStatus,
                                onResetDemoData = { viewModel.resetDemoData() },
                                onLogout = {
                                    isDemoSession = false
                                    viewModel.logout()
                                },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
