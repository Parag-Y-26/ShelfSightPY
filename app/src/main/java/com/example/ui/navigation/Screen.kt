package com.example.ui.navigation

sealed class Screen(val title: String) {
    object Login : Screen("Login")
    object StoreSetup : Screen("Store Setup")
    object Dashboard : Screen("Dashboard")
    object ShelfScanLayout : Screen("Shelf Layout")
    object AddProduct : Screen("Add Product")
    object StoreMap : Screen("Store Map")
    object Monitoring : Screen("Live Monitoring")
    object Analytics : Screen("Analytics")
    object Settings : Screen("Settings")
}
