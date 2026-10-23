# ShelfSight Security Specification

## 1. Data Invariants & Authorization Boundaries

- **User Isolation**: All resources are strictly scoped under the path `/users/{userId}/...`.
- **Identity Integrity**: Every read, list, create, update, and delete requires `request.auth != null` and `request.auth.uid == userId`. Cross-user data access is strictly forbidden.
- **Immutable Fields**: `userId` and `createdAt` cannot be modified during updates.
- **Temporal Integrity**: `createdAt` and `updatedAt` must be Firestore timestamps with upper bounds `<= request.time`. Clients submit `FieldValue.serverTimestamp()`.
- **Schema Validation**: Every write must strictly conform to `isValid[Entity]()` type, string length, and numeric constraints.
- **Resource Poisoning Protection**: IDs must conform to `isValidId()` regex `^[a-zA-Z0-9_\-]+$` and length `<= 128`.

---

## 2. The "Dirty Dozen" Attack Payloads

1. **Unauthenticated Read**: Attempting to read `/users/user_123/stores/store_main` with `request.auth == null`.
2. **Cross-User Snooping**: User Bob (`bob_456`) attempting to read `/users/alice_123/stores/store_main`.
3. **Cross-User Write**: User Bob attempting to write a product under `/users/alice_123/products/prod_1`.
4. **Identity Spoofing**: User Alice writing an entity where `userId` payload is `"bob_456"`.
5. **Junk ID Poisoning**: Creating an entity with document ID containing 2000 invalid characters (`/users/alice_123/stores/%%%bad$$$id`).
6. **Immutable Field Tampering**: Attempting to alter `createdAt` on an existing store or shelf item during an update.
7. **Future Timestamp Injection**: Submitting `createdAt` timestamp with a future date (`request.time + 1000s`).
8. **Ghost Field Injection (Shadow Update)**: Attempting to update a store document with an extra unauthorized field like `isAdmin: true` or `verified: true`.
9. **String Overflow Attack (Denial of Wallet)**: Submitting a store name or product description containing 100,000 characters.
10. **Negative Value / Type Confusion**: Setting shelf item `quantity: -50` or providing a string for an integer field.
11. **Blanket Query Scraping**: Attempting a collection group query or query without authenticated user scope.
12. **Unauthorized Deletion**: User Bob attempting to delete a shelf or product belonging to Alice.

---

## 3. Test Runner Configuration

- Local emulators: Firestore on `127.0.0.1:8085`, Auth on `127.0.0.1:9099`.
- Tier 1: `node --test firestore.test.js` (~1s rapid syntax & rules validation).
- Tier 2: Robolectric integration tests in Android JVM testing repository operations against the emulators.
