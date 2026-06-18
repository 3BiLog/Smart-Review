import admin from "firebase-admin";

let app;

export function getFirestore() {
  if (!app) {
    const privateKey = (process.env.FIREBASE_PRIVATE_KEY || "").replace(/\\n/g, "\n");
    app = admin.initializeApp({
      credential: admin.credential.cert({
        projectId: process.env.FIREBASE_PROJECT_ID,
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
        privateKey,
      }),
    });
  }
  return admin.firestore();
}

export const Collections = {
  TRANSACTIONS: "transactions",
  USERS: "users",
  ENROLLMENTS: "enrollments",
};
