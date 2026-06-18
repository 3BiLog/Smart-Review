import { getPayOS } from "../lib/payos.js";
import { grantEnrollment, updateTransactionStatus } from "../lib/enrollment.js";
import { Collections, getFirestore } from "../lib/firebase.js";

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = "";
    req.on("data", (chunk) => (data += chunk));
    req.on("end", () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on("error", reject);
  });
}

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed" });
  }

  try {
    const body = await readBody(req);
    const payos = getPayOS();

    const verified = payos.verifyPaymentWebhookData(body);
    if (!verified) {
      return res.status(400).json({ success: false, error: "Invalid webhook signature" });
    }

    const orderCode = verified.orderCode;
    const success = body.success === true || body.code === "00";

    const db = getFirestore();
    const snap = await db
      .collection(Collections.TRANSACTIONS)
      .where("orderCode", "==", orderCode)
      .limit(1)
      .get();

    if (snap.empty) {
      console.warn("Webhook: transaction not found for orderCode", orderCode);
      return res.status(200).json({ success: true });
    }

    const doc = snap.docs[0];
    const data = doc.data();

    if (success) {
      await updateTransactionStatus(doc.id, "success", {
        paidAt: new Date().toISOString(),
        webhookData: verified,
      });
      await grantEnrollment({
        userId: data.userId,
        courseId: data.courseId,
        orderCode: data.orderCode,
        amount: data.amount,
        transactionId: doc.id,
      });
    } else {
      await updateTransactionStatus(doc.id, "failed", { webhookData: verified });
    }

    return res.status(200).json({ success: true });
  } catch (error) {
    console.error("payos-webhook error:", error);
    return res.status(500).json({ success: false, error: error.message });
  }
}
