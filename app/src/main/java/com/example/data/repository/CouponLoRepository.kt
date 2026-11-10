package com.example.data.repository

import android.content.Context
import com.example.data.local.CouponEntity
import com.example.data.local.CouponLoDao
import com.example.data.local.CouponLoDatabase
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class CouponLoRepository(
    private val dao: CouponLoDao
) {
    constructor(context: Context) : this(
        CouponLoDatabase.getDatabase(context).couponLoDao()
    )

    suspend fun login(username: String, password: String): Result<UserEntity> {
        val cleanUser = username.trim()
        val cleanPass = password.trim()
        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return Result.failure(IllegalArgumentException("Username and password cannot be empty"))
        }

        // Special fallback for preconfigured admin
        if (cleanUser.equals("admin", ignoreCase = true) && cleanPass == "2468") {
            var existingAdmin = dao.getUser("admin")
            if (existingAdmin == null) {
                existingAdmin = UserEntity(username = "admin", passwordHash = "2468", role = "ADMIN")
                dao.insertUser(existingAdmin)
            }
            return Result.success(existingAdmin)
        }

        val user = dao.getUser(cleanUser)
        return if (user != null && user.passwordHash == cleanPass) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Invalid username or password"))
        }
    }

    suspend fun register(username: String, password: String): Result<UserEntity> {
        val cleanUser = username.trim()
        val cleanPass = password.trim()
        if (cleanUser.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        }
        if (cleanPass.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        }

        val existing = dao.getUser(cleanUser)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }

        val newUser = UserEntity(
            username = cleanUser,
            passwordHash = cleanPass,
            role = "CUSTOMER"
        )
        return try {
            dao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCoupons(): Flow<List<CouponEntity>> {
        return dao.getAllCoupons().onEach { list ->
            if (list.isEmpty()) {
                dao.insertCoupon(
                    CouponEntity(
                        id = "GGPRO",
                        name = "GG PRO",
                        desc = "Premium GG PRO coupon deal. Limited stock.",
                        price = 100.0,
                        discount = 15.0,
                        stock = 12L,
                        emoji = "🎮"
                    )
                )
            }
        }
    }

    suspend fun updateStock(couponId: String, newStock: Long): Result<Unit> {
        return try {
            dao.updateStock(couponId, newStock)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitOrder(
        username: String,
        coupon: CouponEntity,
        utr: String,
        screenshotBase64: String,
        customerHandle: String
    ): Result<String> {
        return try {
            val orderId = "CL-" + System.currentTimeMillis().toString(36).uppercase()
            val order = OrderEntity(
                id = orderId,
                username = username,
                couponId = coupon.id,
                couponName = coupon.name,
                amount = coupon.finalPrice,
                utr = utr.trim(),
                customer = customerHandle.trim(),
                screenshot = screenshotBase64,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            dao.insertOrder(order)
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserOrders(username: String): Flow<List<OrderEntity>> {
        return dao.getOrdersByUsername(username)
    }

    fun observeAllOrders(): Flow<List<OrderEntity>> {
        return dao.getAllOrders()
    }

    suspend fun updateOrderStatus(order: OrderEntity, newStatus: String): Result<Unit> {
        return try {
            dao.updateOrderStatus(order.id, newStatus)
            if (newStatus == "APPROVED") {
                val coupon = dao.getCouponById(order.couponId)
                if (coupon != null && coupon.stock > 0L) {
                    dao.updateStock(order.couponId, coupon.stock - 1L)
                }
                sendNotification(
                    title = "Order Approved!",
                    body = "Order ${order.id} for ${order.couponName} has been approved."
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignCouponKey(orderId: String, couponKey: String): Result<Unit> {
        return try {
            dao.assignCouponKey(orderId, couponKey.trim())
            sendNotification(
                title = "Coupon Key Delivered",
                body = "Redeem key assigned to Order $orderId! Check your ORDERS tab."
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeNotifications(): Flow<List<NotificationEntity>> {
        return dao.getAllNotifications()
    }

    suspend fun sendNotification(title: String, body: String): Result<Unit> {
        return try {
            val id = "NOTIF-" + System.currentTimeMillis().toString(36).uppercase()
            dao.insertNotification(
                NotificationEntity(
                    id = id,
                    title = title.trim(),
                    body = body.trim(),
                    createdAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
