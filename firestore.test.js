const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_uid_01";
const ADMIN_EMAIL = "bunyodjon370@gmail.com";
const ALICE_UID = "alice_uid_01";
const BOB_UID = "bob_uid_02";

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

function buildUserPayload(uid, overrides = {}) {
  const ts = new Date(Date.now() - 1000);
  return {
    userId: uid,
    telegramUserId: 12345678,
    fullName: "Alice User",
    username: "alice_tg",
    phoneNumber: "+998901112233",
    bio: "Ghostly user",
    premiumAccount: false,
    channelSummaries: ["Test Channel|@testchan|500|member"],
    spamBanned: false,
    spamUntilMs: 0,
    createdAt: ts,
    updatedAt: ts,
    ...overrides,
  };
}

test("Unauthenticated reads and writes are rejected", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("ghostly_users").get());
  await assertFails(unauth.collection("app_config").doc("ghostly_channel").get());
  await assertFails(unauth.collection("ghostly_users").doc(ALICE_UID).set(buildUserPayload(ALICE_UID)));
});

test("User cannot self-assign spam or spoof email admin", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "user@example.com", email_verified: true }).firestore();
  await assertSucceeds(aliceDb.collection("ghostly_users").doc(ALICE_UID).set(buildUserPayload(ALICE_UID)));

  // User cannot escalate or modify spamBanned
  await assertFails(
    aliceDb.collection("ghostly_users").doc(ALICE_UID).update({
      spamBanned: true,
      spamUntilMs: Date.now() + 86400000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Unverified admin email spoof fails
  const spoofDb = testEnv.authenticatedContext("spoof_1", { email: ADMIN_EMAIL, email_verified: false }).firestore();
  await assertFails(spoofDb.collection("ghostly_users").get());
  await assertFails(
    spoofDb.collection("app_config").doc("ghostly_channel").set({
      channelLink: "https://t.me/spoof",
      enabled: true,
      updatedBy: "spoof_1",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User isolation on ghostly_users and support_threads", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  await assertSucceeds(aliceDb.collection("ghostly_users").doc(ALICE_UID).set(buildUserPayload(ALICE_UID)));
  await assertFails(bobDb.collection("ghostly_users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("ghostly_users").get());
  await assertSucceeds(aliceDb.collection("ghostly_users").where("userId", "==", ALICE_UID).get());
});

test("Admin can list users, toggle 24h spam, set ghostly_channel, and reply to +47777 support threads", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true }).firestore();
  const ts = new Date(Date.now() - 1000);

  await assertSucceeds(aliceDb.collection("ghostly_users").doc(ALICE_UID).set(buildUserPayload(ALICE_UID)));

  // Admin lists users & marks Alice as spam for 24h
  await assertSucceeds(adminDb.collection("ghostly_users").get());
  await assertSucceeds(
    adminDb.collection("ghostly_users").doc(ALICE_UID).update({
      spamBanned: true,
      spamUntilMs: Date.now() + 86400000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin lifts spam immediately
  await assertSucceeds(
    adminDb.collection("ghostly_users").doc(ALICE_UID).update({
      spamBanned: false,
      spamUntilMs: 0,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Admin sets Ghostly channel and regular user reads it (cannot write)
  await assertSucceeds(
    adminDb.collection("app_config").doc("ghostly_channel").set({
      channelLink: "https://t.me/ghostly_official",
      enabled: true,
      updatedBy: ADMIN_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("app_config").doc("ghostly_channel").get());
  await assertFails(
    aliceDb.collection("app_config").doc("ghostly_channel").update({
      channelLink: "https://t.me/hijacked",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Support thread & messages
  await assertSucceeds(
    aliceDb.collection("support_threads").doc(ALICE_UID).set({
      userId: ALICE_UID,
      telegramUserId: 12345678,
      senderName: "Alice User",
      senderUsername: "alice_tg",
      senderPhone: "+998901112233",
      lastMessageText: "Salom yordam kerak",
      lastMessageFromAdmin: false,
      unreadByAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("support_threads").doc(ALICE_UID).collection("messages").doc("msg_1").set({
      threadUserId: ALICE_UID,
      senderUid: ALICE_UID,
      fromAdmin: false,
      text: "Salom yordam kerak",
      createdAt: ts,
    })
  );
  // User cannot spoof admin message
  await assertFails(
    aliceDb.collection("support_threads").doc(ALICE_UID).collection("messages").doc("msg_fake").set({
      threadUserId: ALICE_UID,
      senderUid: ALICE_UID,
      fromAdmin: true,
      text: "Fake admin",
      createdAt: ts,
    })
  );
  // Admin replies cleanly
  await assertSucceeds(
    adminDb.collection("support_threads").doc(ALICE_UID).collection("messages").doc("msg_2").set({
      threadUserId: ALICE_UID,
      senderUid: ADMIN_UID,
      fromAdmin: true,
      text: "Albatta, yordam beramiz!",
      createdAt: new Date(Date.now() - 200),
    })
  );
});
