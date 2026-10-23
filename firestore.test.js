const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read stores", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("stores").doc("store_main").get());
});

test("Cross-user read: Bob cannot read Alice's store", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("stores").doc("store_main").set({
      id: "store_main",
      userId: ALICE_UID,
      name: "Alice's Grocery",
      storeType: "Grocery",
      totalShelves: 4,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("stores").doc("store_main").get());
});

test("Owner: can read and list their own stores", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("stores").doc("store_main").set({
      id: "store_main",
      userId: ALICE_UID,
      name: "Alice's Grocery",
      storeType: "Grocery",
      totalShelves: 4,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("stores").doc("store_main").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("stores").get());
});

test("Owner: can create valid store", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("stores").doc("store_main").set({
      id: "store_main",
      userId: ALICE_UID,
      name: "Fresh Mart",
      storeType: "Grocery",
      totalShelves: 4,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Owner: cannot inject ghost field on store update", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("stores").doc("store_main").set({
      id: "store_main",
      userId: ALICE_UID,
      name: "Fresh Mart",
      storeType: "Grocery",
      totalShelves: 4,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("stores").doc("store_main").update({
      name: "Super Mart",
      isAdmin: true, // Ghost field
      updatedAt: new Date(),
    })
  );
});

test("Observation events: append-only, cannot be deleted or modified", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("observation_events").doc("event_1").set({
      id: "event_1",
      userId: ALICE_UID,
      shelfId: "shelf_1",
      shelfNumber: 1,
      eventType: "DWELL_QUALIFIED",
      dwellSeconds: 4.2,
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Read should succeed
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("observation_events").doc("event_1").get());
  // Update must fail
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("observation_events").doc("event_1").update({
    dwellSeconds: 10.0,
  }));
  // Delete must fail
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("observation_events").doc("event_1").delete());
});
