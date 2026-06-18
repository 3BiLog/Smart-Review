import { FieldValue } from "firebase-admin/firestore";
import { Collections, getFirestore } from "../lib/firebase.js";
import { generateOrderCode, getPayOS } from "../lib/payos.js";

function cors(res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
}

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
  cors(res);

  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed" });
  }

  try {
    const body = await readBody(req);
    const { courseId, userId, userEmail, userName, amount, courseName } = body;

    if (!courseId || !userId || !amount || amount <= 0) {
      return res.status(400).json({
        success: false,
        error: "Thiếu thông tin: courseId, userId, amount",
      });
    }

    const orderCode = generateOrderCode();
    const apiBase = (process.env.API_BASE_URL || "").replace(/\/$/, "");
    const appReturn = process.env.APP_RETURN_URL || "smartreview://payment/return";

    const returnUrl = `${appReturn}?courseId=${encodeURIComponent(courseId)}&orderCode=${orderCode}&status=success`;
    const cancelUrl = `${appReturn}?courseId=${encodeURIComponent(courseId)}&orderCode=${orderCode}&status=cancel`;

    const description = `SmartReview - ${(courseName || "Khoa hoc").slice(0, 50)}`;

    const payos = getPayOS();
    const paymentLink = await payos.createPaymentLink({
      orderCode,
      amount: Number(amount),
      description,
      returnUrl,
      cancelUrl,
    });

    const db = getFirestore();
    const transactionRef = db.collection(Collections.TRANSACTIONS).doc();
    const transactionId = transactionRef.id;

    await transactionRef.set({
      orderCode,
      courseId,
      userId,
      userEmail: userEmail || "",
      userName: userName || "",
      amount: Number(amount),
      courseName: courseName || "",
      status: "pending",
      checkoutUrl: paymentLink.checkoutUrl,
      paymentLinkId: paymentLink.paymentLinkId || null,
      createdAt: FieldValue.serverTimestamp(),
    });

    return res.status(200).json({
      success: true,
      data: {
        transactionId,
        orderCode,
        checkoutUrl: paymentLink.checkoutUrl,
      },
    });
  } catch (error) {
    console.error("create-payos error:", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Không thể tạo đơn thanh toán",
    });
  }
}
