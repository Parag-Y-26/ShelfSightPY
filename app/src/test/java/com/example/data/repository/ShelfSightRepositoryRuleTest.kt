package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ObservationEvent
import com.example.data.model.ShelfItem
import com.example.data.model.Store
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfSightRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveStore_whenAuthenticated_succeeds() = runBlocking {
        signInTestUser("alice@shelfsight.test")
        val repository = FirestoreShelfSightRepository(firestore)

        val store = Store(
            id = "store_main",
            name = "Alice Retail",
            storeType = "Grocery",
            totalShelves = 4
        )

        val result = withTimeout(5000L) { repository.saveStore(store) }
        assertTrue("Expected saveStore to succeed", result.isSuccess)
    }

    @Test
    fun saveStore_whenUnauthenticated_fails() = runBlocking {
        // Sign out first
        auth.signOut()
        val repository = FirestoreShelfSightRepository(firestore)

        val store = Store(
            id = "store_main",
            name = "Unauthenticated Store",
            storeType = "Grocery",
            totalShelves = 4
        )

        val result = withTimeout(5000L) { repository.saveStore(store) }
        assertFalse("Expected unauthenticated write to fail", result.isSuccess)
    }

    @Test
    fun recordObservation_whenAuthenticated_succeeds() = runBlocking {
        val aliceUid = signInTestUser("alice2@shelfsight.test")
        val repository = FirestoreShelfSightRepository(firestore)

        val event = ObservationEvent(
            id = "event_rule_test_1",
            shelfId = "shelf_1",
            shelfNumber = 1,
            trackId = 42,
            eventType = "DWELL_QUALIFIED",
            dwellSeconds = 3.5f,
            details = "Shopper dwelled at shelf 1"
        )

        val result = withTimeout(5000L) { repository.recordObservation(event) }
        assertTrue("Expected recordObservation to succeed", result.isSuccess)
    }

    @Test
    fun shelfItem_updateQuantity_succeeds() = runBlocking {
        signInTestUser("alice3@shelfsight.test")
        val repository = FirestoreShelfSightRepository(firestore)

        val item = ShelfItem(
            id = "item_test_1",
            shelfId = "shelf_1",
            productId = "prod_maggi",
            rowNumber = 1,
            slotNumber = 1,
            quantity = 24
        )

        val saveResult = withTimeout(5000L) { repository.saveShelfItem(item) }
        assertTrue("Expected saveShelfItem to succeed", saveResult.isSuccess)

        val updateResult = withTimeout(5000L) { repository.updateItemQuantity("item_test_1", 28) }
        assertTrue("Expected updateItemQuantity to succeed", updateResult.isSuccess)
    }
}
