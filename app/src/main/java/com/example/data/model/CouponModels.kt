package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.round

@IgnoreExtraProperties
data class Coupon(
    @DocumentId val id: String = "GGPRO",
    val name: String = "GG PRO",
    val desc: String = "Premium GG PRO coupon deal. Limited stock.",
    val price: Double = 100.0,
    val discount: Double = 15.0,
    val stock: Long = 12L,
    val emoji: String = "🎮",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val finalPrice: Double
        get() = round(price * (1.0 - discount / 100.0) * 100.0) / 100.0
}

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val couponId: String = "GGPRO",
    val couponName: String = "GG PRO",
    val amount: Double = 85.0,
    val utr: String = "",
    val customer: String = "",
    val screenshot: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val key: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
