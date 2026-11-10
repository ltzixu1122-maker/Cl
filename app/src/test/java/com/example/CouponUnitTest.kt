package com.example

import com.example.data.local.CouponEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class CouponUnitTest {
    @Test
    fun coupon_finalPriceCalculation_isAccurate() {
        val coupon = CouponEntity(
            id = "GGPRO",
            name = "GG PRO",
            price = 100.0,
            discount = 15.0
        )
        // 100 * (1 - 0.15) = 85.0
        assertEquals(85.0, coupon.finalPrice, 0.001)
    }

    @Test
    fun upiUri_formatting_isCorrect() {
        val coupon = CouponEntity(id = "GGPRO", name = "GG PRO", price = 100.0, discount = 15.0)
        val upiId = "9928240425@ybl"
        val payeeName = "COUPON LO"
        val encodedPayee = URLEncoder.encode(payeeName, StandardCharsets.UTF_8.toString())
        val encodedTn = URLEncoder.encode(coupon.name, StandardCharsets.UTF_8.toString())
        val uri = "upi://pay?pa=$upiId&pn=$encodedPayee&am=${coupon.finalPrice}&cu=INR&tn=$encodedTn"

        assertEquals("upi://pay?pa=9928240425@ybl&pn=COUPON+LO&am=85.0&cu=INR&tn=GG+PRO", uri)
    }
}
