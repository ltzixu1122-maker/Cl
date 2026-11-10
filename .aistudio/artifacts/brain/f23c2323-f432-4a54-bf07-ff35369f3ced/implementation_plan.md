# Coupon Lo — Meme Coupon Store & Admin Console

Coupon Lo brings high-energy meme culture and discounted digital coupons to Android with native Jetpack Compose. Customers can browse curated deals, generate private UPI payment QR codes, submit transaction proof (UTR and screenshot), and claim verified coupon keys, while store managers review payment submissions and manage stock through a dedicated Admin Dashboard.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed during requirements clarification and guide the technical architecture:

- **Confirmed Decision 1 (Persistence & Sync)**: Real-time Cloud Synchronization backed by Firebase Firestore. Customer order submissions, approvals, coupon keys, and broadcasts will sync live between customer devices and the admin console.
- **Confirmed Decision 2 (Authentication)**: Per Firebase Firestore zero-trust security guidelines, authentication is handled via Google Sign-In using Jetpack Credential Manager.
- **Confirmed Decision 3 (Initial Catalog)**: The store launches with the original **GG PRO** gaming coupon deal (₹100 MRP, 15% discount -> ₹85, stock of 12) exactly as specified in the reference web application.
- **Confirmed Decision 4 (Payment Gateway)**: Private UPI intent URI generation (`upi://pay?pa=9928240425@ybl&pn=COUPON%20LO&am=85&cu=INR&tn=GG%20PRO`) displayed as a clean high-contrast QR code without exposing raw payment handles in plain text on the storefront.
- **Confirmed Decision 5 (Admin Security)**: PIN-gated administrator console (default PIN `2468`) allowing stock adjustment, order review, payment approval/rejection, and digital coupon key delivery.

---

## 1. Overview & Core Concept

- **What It Does**: A dedicated digital coupon marketplace providing instant access to exclusive deals with meme-driven branding. Users purchase vouchers via their preferred UPI app (Google Pay, PhonePe, Paytm, BHIM), submit transaction UTR and screenshot receipts, and receive digital redeem keys upon manual admin verification.
- **Target Audience / Persona**: Mobile gamers, digital deal hunters, and community members looking for discounted digital subscriptions, in-game currency, and premium passes.
- **Key Value**: Simple, fraud-resistant manual UPI settlement with zero payment gateway processing fees, paired with real-time cloud tracking so customers never lose their redeem codes.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Browse & Discover**:
   - Customers arrive at an atmospheric dark storefront featuring glowing crimson accents, an energetic animated meme ticker (*"🔥 DEALS • MEMES • COUPONS • GOOD VIBES"*), and coupon cards with live stock counters.
2. **Checkout & QR Payment**:
   - Tapping **BUY NOW** opens a payment sheet displaying final pricing after discount.
   - An on-screen high-contrast QR code is rendered directly in Compose for scanning via another device, or users can copy the payment intent link.
   - The user enters their 12-digit UPI UTR / Transaction Reference ID, uploads or selects a payment proof image from their gallery, and optionally adds their Telegram handle.
3. **Order Tracking & Key Reveal**:
   - Submitted orders appear immediately in the customer's **📦 ORDERS** tab with status pills (`PENDING`, `APPROVED`, `REJECTED`).
   - Once approved by the admin with an assigned key, the redeem key is highlighted in a secure, copyable card.
4. **Broadcast Notifications**:
   - A dedicated notification feed displays flash sale alerts, stock restocks, and order status updates sent by store admins.
5. **Admin Control Room**:
   - Accessible via the **ADMIN** button with secure PIN entry (`2468`).
   - Dashboard displays metric cards: Total Orders, Pending Reviews, Approved Orders, and Active Notifications.
   - **Stock Manager**: Allows quick quantity increments/decrements. Zero stock automatically locks purchases to `OUT OF STOCK`.
   - **Order Review Desk**: Shows customer Telegram handles, UTRs, and expandable proof screenshots with one-tap `Approve` / `Reject` buttons and a coupon key dispatch field.
   - **Store Broadcast Dispatcher**: Form to publish immediate notifications to all connected customers.

### Visual Identity & Theme

- **Aesthetic Direction**: Cyberpunk meme aesthetic with deep volcanic blacks, high-contrast neon crimson red highlights, and crisp typography.
- **Color Palette**:
  - `Background`: `#080809` (deep obsidian black with subtle radial wine gradients `#3B1017`)
  - `Surface / Panel`: `#121214` and `#18181C` with subtle border stroke `#29292F`
  - `Primary / Accent`: `#FF2638` (electric crimson red)
  - `Success`: `#26D98B` (emerald green for in-stock and approved badges)
  - `Warning / Pending`: `#FFCB79` (amber gold)
  - `Text Primary`: `#FFFFFF`
  - `Text Muted`: `#A2A2AA`
- **Typography & Hierarchy**: Bold geometric sans-serif hierarchy; heavy display weights for numerals and promo tags, clean readable body typography for order details and instructions.
- **Motion & Micro-interactions**: Smooth sheet reveals, pulsing deal badges, animated marquee ticker, and haptic feedback on successful order submissions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Cloud Firestore with Real-Time Listeners vs Local SQLite**:
  - *Chosen Approach*: Firebase Firestore using collection snapshot flows (`observeOrders()`, `observeCoupons()`).
  - *Why*: Allows true multi-device synchronization where orders submitted from a customer device appear instantly on the admin's device without polling.
  - *Trade-off*: Requires Firebase backend provisioning and zero-trust security rules enforcement.
- **Decision 2: Native Canvas / ZXing QR Rendering**:
  - *Chosen Approach*: Lightweight on-device QR code generation into Compose `Bitmap` or `Canvas` without external webviews.
  - *Why*: Instant offline rendering, zero network lag, and perfectly crisp scaling on high-DPI Android displays.
- **Decision 3: Image Proof Attachment Handling**:
  - *Chosen Approach*: Standard Android Photo Picker (`PickVisualMedia`) for safe zero-permission image selection, converted to optimized base64 / cloud storage payload.
  - *Why*: Strict adherence to Google Play policies without requesting broad storage permissions (`READ_EXTERNAL_STORAGE`).
- **Decision 4: Admin Access & Role Gating**:
  - *Chosen Approach*: In-app PIN modal (`2468`) paired with Firestore field validation to switch into admin mode while keeping customer and admin views cleanly separated.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Coupon Lo Android App                       │
├───────────────────────────────┬─────────────────────────────────┤
│         Customer UI           │            Admin UI             │
│  - Hero Banner & Ticker       │  - PIN Gate Modal (2468)        │
│  - Coupon Storefront (GG PRO) │  - Metric Stat Counters         │
│  - Payment Sheet & QR Code    │  - Real-Time Order Verification │
│  - Photo Picker (UTR/Proof)   │  - Stock Manager                │
│  - Customer Orders & Keys     │  - Notification Broadcast Form  │
│  - Broadcast Notification Feed│                                 │
└───────────────┬───────────────┴─────────────────┬───────────────┘
                │                                 │
                ▼                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                CouponLoViewModel / StateFlow                    │
│   - couponsState: StateFlow<List<Coupon>>                       │
│   - ordersState: StateFlow<List<Order>>                         │
│   - notificationsState: StateFlow<List<StoreNotification>>      │
│   - adminAuthenticated: StateFlow<Boolean>                      │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CouponLoRepository                          │
│   - observeCoupons(): Flow<List<Coupon>>                        │
│   - submitOrder(order: Order): Result<Unit>                     │
│   - updateOrderStatus(id, status, key): Result<Unit>            │
│   - updateStock(couponId, newStock): Result<Unit>               │
│   - broadcastNotification(title, body): Result<Unit>            │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│              Firebase Firestore & Google Auth                   │
│   - /coupons/{couponId}                                         │
│   - /orders/{orderId}                                           │
│   - /notifications/{notificationId}                             │
│   - Zero-trust security rules with validated schemas            │
└─────────────────────────────────────────────────────────────────┘
```

### Data Models

```kotlin
data class Coupon(
    val id: String = "GGPRO",
    val name: String = "GG PRO",
    val desc: String = "Premium GG PRO coupon deal. Limited stock.",
    val price: Double = 100.0,
    val discount: Int = 15,
    val stock: Int = 12,
    val emoji: String = "🎮"
) {
    val finalPrice: Double get() = (price * (1.0 - discount / 100.0)).roundTo(2)
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val couponId: String = "GGPRO",
    val couponName: String = "GG PRO",
    val amount: Double = 85.0,
    val utr: String = "",
    val customerTelegram: String = "",
    val screenshotUrl: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val couponKey: String? = null,
    val createdAt: Timestamp? = null
)

data class StoreNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val createdAt: Timestamp? = null
)
```

### Execution Steps Post-Approval

1. **Firebase Backend Setup**:
   - Extract keystore SHA-1 / SHA-256 fingerprints.
   - Show Firebase Terms UI via `show_aistudio_ui` (`requested_products: ["firestore"]`).
   - Call `ProvisionFirebase` via `rpc_action`.
2. **Schema & Security Rules**:
   - Generate `firebase-blueprint.json` and hardened `firestore.rules`.
   - Test and deploy rules via `DeployRules`.
3. **Core Application Implementation**:
   - Add QR code generation utility and photo picker integration.
   - Build `CouponLoTheme`, custom colors, and typography.
   - Build Storefront screen, Buy & UPI QR Payment bottom sheet, Orders screen, Notification feed, and Admin Control Room.
   - Configure launcher icon with `app-icon-generation`.
4. **Verification**:
   - Compile applet with `compile_applet` and verify end-to-end functionality.
