const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "gen-lang-client-0133038704";
const DATABASE_ID = "ai-studio-android-couponlo-f23c2323-f432-4a54-bf07-ff35369f3ced";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
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

test("Unauthenticated access is denied on all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  await assertFails(unauthDb.collection("coupons").get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("notifications").get());
});

test("Authenticated user can read coupons and submit order", async () => {
  const aliceDb = testEnv.authenticatedContext("alice_123", { email: "alice@example.com" }).firestore({ databaseId: DATABASE_ID });
  
  // Submit order as Alice
  const orderRef = aliceDb.collection("orders").doc("CL-TEST1");
  await assertSucceeds(orderRef.set({
    id: "CL-TEST1",
    userId: "alice_123",
    couponId: "GGPRO",
    couponName: "GG PRO",
    amount: 85.0,
    utr: "123456789012",
    status: "PENDING",
    createdAt: new Date()
  }));

  // Alice can read her own order
  await assertSucceeds(orderRef.get());

  // Bob cannot read Alice's order
  const bobDb = testEnv.authenticatedContext("bob_456", { email: "bob@example.com" }).firestore({ databaseId: DATABASE_ID });
  await assertFails(bobDb.collection("orders").doc("CL-TEST1").get());
});

test("Admin can approve order and assign key", async () => {
  const aliceDb = testEnv.authenticatedContext("alice_123", { email: "alice@example.com" }).firestore({ databaseId: DATABASE_ID });
  const adminDb = testEnv.authenticatedContext("admin_uid", { email: "sutharsita65@gmail.com", email_verified: true }).firestore({ databaseId: DATABASE_ID });

  const orderRefAlice = aliceDb.collection("orders").doc("CL-TEST2");
  const createdAt = new Date();
  await assertSucceeds(orderRefAlice.set({
    id: "CL-TEST2",
    userId: "alice_123",
    couponId: "GGPRO",
    couponName: "GG PRO",
    amount: 85.0,
    utr: "987654321012",
    status: "PENDING",
    createdAt: createdAt
  }));

  // Admin approves order and sets coupon key
  const orderRefAdmin = adminDb.collection("orders").doc("CL-TEST2");
  await assertSucceeds(orderRefAdmin.update({
    status: "APPROVED",
    key: "GGPRO-WIN-9999",
    updatedAt: new Date()
  }));

  // Admin can broadcast a store notification
  const notifRef = adminDb.collection("notifications").doc("N-1");
  await assertSucceeds(notifRef.set({
    id: "N-1",
    title: "Sale Live!",
    body: "GG PRO is restocked",
    createdAt: new Date()
  }));
});
