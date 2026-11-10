package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CouponEntity::class,
        OrderEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CouponLoDatabase : RoomDatabase() {

    abstract fun couponLoDao(): CouponLoDao

    companion object {
        @Volatile
        private var INSTANCE: CouponLoDatabase? = null

        fun getDatabase(context: Context): CouponLoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CouponLoDatabase::class.java,
                    "coupon_lo_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate default admin and default coupon
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).couponLoDao()
                                dao.insertUser(
                                    UserEntity(
                                        username = "admin",
                                        passwordHash = "2468",
                                        role = "ADMIN"
                                    )
                                )
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
                                dao.insertNotification(
                                    NotificationEntity(
                                        id = "NOTIF-WELCOME",
                                        title = "Welcome to COUPON LO! 🔥",
                                        body = "Coupons lo, savings karo! Explore our exclusive GG PRO voucher deals.",
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
