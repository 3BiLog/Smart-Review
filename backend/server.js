// server.js
import express from 'express';
import cors from 'cors';
import { getPayOS, generateOrderCode } from './lib/payos.js';
import { getFirestore, Collections } from './lib/firebase.js';
import { FieldValue } from 'firebase-admin/firestore';

const app = express();
app.use(cors());
app.use(express.json());

// Create order endpoint
app.post('/api/create-payos', async (req, res) => {
    try {
        const { courseId, userId, userEmail, userName, amount, courseName } = req.body;

        if (!courseId || !userId || !amount || amount <= 0) {
            return res.status(400).json({
                success: false,
                error: "Thiếu thông tin: courseId, userId, amount",
            });
        }

        const orderCode = generateOrderCode();
        const returnUrl = `smartreview://payment/return?courseId=${encodeURIComponent(courseId)}&orderCode=${orderCode}&status=success`;
        const cancelUrl = `smartreview://payment/return?courseId=${encodeURIComponent(courseId)}&orderCode=${orderCode}&status=cancel`;

        const description = (courseName || "Khoa hoc").slice(0, 25);

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
});

// Check transaction endpoint
app.get('/api/check-transaction', async (req, res) => {
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

        return res.status(200).json({
            status: data.status || "pending",
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
});

// Webhook endpoint
app.post('/api/webhook', async (req, res) => {
    try {
        const body = req.body;
        console.log('Webhook received:', body);

        // Process webhook...
        return res.status(200).json({ success: true });
    } catch (error) {
        console.error('Webhook error:', error);
        return res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on http://localhost:${PORT}`);
});