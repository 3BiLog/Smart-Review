# SmartReview Payment API (PayOS)

Backend serverless cho **Android app** — tạo link PayOS và kiểm tra trạng thái thanh toán.

## Kiến trúc (2 service)

| Service | URL | Vai trò |
|---------|-----|---------|
| **Payment API** (repo này) | https://smartreview-payment.vercel.app/api | `create-payos`, `check-transaction` — Android gọi |
| **Admin Dashboard** | https://smart-review-dashboard.vercel.app/api | `payos-webhook` — PayOS gửi webhook tại đây |

```
Android App
    │ POST /create-payos
    ▼
smartreview-payment.vercel.app  ──► Firestore transactions
    │
    ▼ checkoutUrl
PayOS (user thanh toán)
    │ webhook
    ▼
smart-review-dashboard.vercel.app/api/payos-webhook
    │
    ▼
Firestore enrollments (users/{uid}/enrollments/{courseId})

Android App (quay lại)
    │ GET /check-transaction
    ▼
smartreview-payment.vercel.app  ──► xác nhận + mở khóa (dự phòng)
```

**PayOS Webhook URL** (đã cấu hình):

```
https://smart-review-dashboard.vercel.app/api/payos-webhook
```

Không cần đổi webhook sang `smartreview-payment` — endpoint `/api/payos-webhook` ở backend này chỉ là dự phòng.

## Cấu hình local

```bash
cd backend
cp .env.example .env.local   # điền PayOS + Firebase Admin
npm install
npm run dev                  # http://localhost:3000
```

## Deploy

```bash
cd backend
bash deploy.sh
```

Env vars trên Vercel:

- `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY`
- `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY`
- `API_BASE_URL` = `https://smartreview-payment.vercel.app/api`
- `APP_RETURN_URL` = `smartreview://payment/return`
- `PAYOS_WEBHOOK_URL` = `https://smart-review-dashboard.vercel.app/api/payos-webhook` (tham chiếu)

## Android

`local.properties`:

```properties
PAYMENT_API_BASE_URL=https://smartreview-payment.vercel.app/api
```

## Firestore Schema (cả 2 service dùng chung)

- `transactions/{id}` — `orderCode`, `courseId`, `userId`, `amount`, `status`
- `users/{uid}/enrollments/{courseId}` — khóa học đã mua

Dashboard webhook cần ghi `enrollments` khi nhận `orderCode` từ PayOS (tra `transactions` theo `orderCode`).

## API Endpoints (payment API)

| Method | Path | Mô tả |
|--------|------|-------|
| POST | `/api/create-payos` | Tạo đơn thanh toán |
| GET | `/api/check-transaction` | Kiểm tra trạng thái |
| POST | `/api/payos-webhook` | Dự phòng (không dùng nếu webhook trỏ dashboard) |
