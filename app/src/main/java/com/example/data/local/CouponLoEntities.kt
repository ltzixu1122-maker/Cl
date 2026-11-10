package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.round

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val role: String = "CUSTOMER" // "CUSTOMER" or "ADMIN"
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: String = "GGPRO",
    val name: String = "GG PRO",
    val desc: String = "Premium GG PRO coupon deal. Limited stock.",
    val price: Double = 100.0,
    val discount: Double = 15.0,
    val stock: Long = 12L,
    val emoji: String = "🎮"
) {
    val finalPrice: Double
        get() = round(price * (1.0 - discount / 100.0) * 100.0) / 100.0
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val username: String,
    val couponId: String,
    val couponName: String,
    val amount: Double,
    val utr: String,
    val customer: String,
    val screenshot: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val couponKey: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)
