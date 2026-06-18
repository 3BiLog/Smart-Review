import { Collections, getFirestore } from "../lib/firebase.js";
import { getPayOS } from "../lib/payos.js";
import { grantEnrollment, updateTransactionStatus } from "../lib/enrollment.js";

function cors(res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
}

export default async function handler(req, res) {
  cors(res);

  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  if (req.method !== "GET") {
    return res.status(405).json({ success: false, error: "Method not allowed" });
  }

  try {
    const transactionId = req.query.transactionId;
    const orderCode = req.query.orderCode ? Number(req.query.orderCode) : null;

    if (!transactionId && !orderCode) {
      return res.status(400).json({
        success: false,
        error: "Cần transactionId hoặc orderCode",
      });
    }

    const db = getFirestore();
    let doc;
    let data;

    if (transactionId) {
      doc = await db.collection(Collections.TRANSACTIONS).doc(transactionId).get();
      if (!doc.exists) {
        return res.status(404).json({ success: false, error: "Không tìm thấy giao dịch" });
      }
      data = doc.data();
    } else {
      const snap = await db
        .collection(Collections.TRANSACTIONS)
        .where("orderCode", "==", orderCode)
        .limit(1)
        .get();
      if (snap.empty) {
        return res.status(404).json({ success: false, error: "Không tìm thấy giao dịch" });
      }
      doc = snap.docs[0];
      data = doc.data();
    }

    // If still pending, verify with PayOS API
    if (data.status === "pending" && data.orderCode) {
      try {
        const payos = getPayOS();
        const paymentInfo = await payos.getPaymentLinkInformation(data.orderCode);
        if (paymentInfo.status === "PAID" || paymentInfo.status === "paid" || paymentInfo.amountRemaining === 0) {
          await updateTransactionStatus(doc.id, "success", {
            paidAt: new Date().toISOString(),
            payosStatus: paymentInfo.status,
          });
          await grantEnrollment({
            userId: data.userId,
            courseId: data.courseId,
            orderCode: data.orderCode,
            amount: data.amount,
            transactionId: doc.id,
          });
          data.status = "success";
        } else if (paymentInfo.status === "CANCELLED" || paymentInfo.status === "cancelled") {
          await updateTransactionStatus(doc.id, "failed", { payosStatus: paymentInfo.status });
          data.status = "failed";
        }
      } catch (payosErr) {
        console.warn("PayOS status check failed:", payosErr.message);
      }
    }

    return res.status(200).json({
      status: data.status,
      transactionId: doc.id,
      orderCode: data.orderCode,
      courseId: data.courseId,
      paidAt: data.paidAt || null,
    });
  } catch (error) {
    console.error("check-transaction error:", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Không thể kiểm tra giao dịch",
    });
  }
}
