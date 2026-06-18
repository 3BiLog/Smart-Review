import { FieldValue } from "firebase-admin/firestore";
import { Collections, getFirestore } from "./firebase.js";

/**
 * Grant course access to a user after successful payment.
 */
export async function grantEnrollment({ userId, courseId, orderCode, amount, transactionId }) {
  const db = getFirestore();
  const enrollmentRef = db
    .collection(Collections.USERS)
    .doc(userId)
    .collection(Collections.ENROLLMENTS)
    .doc(courseId);

  await enrollmentRef.set(
    {
      courseId,
      userId,
      orderCode,
      amount,
      transactionId,
      purchasedAt: FieldValue.serverTimestamp(),
    },
    { merge: true },
  );
}

export async function updateTransactionStatus(transactionId, status, extra = {}) {
  const db = getFirestore();
  await db
    .collection(Collections.TRANSACTIONS)
    .doc(transactionId)
    .set(
      {
        status,
        updatedAt: FieldValue.serverTimestamp(),
        ...extra,
      },
      { merge: true },
    );
}
