package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CouponEntity
import com.example.ui.components.MarqueeTicker
import com.example.ui.components.StoreHeader
import com.example.ui.theme.CouponGreen
import com.example.ui.theme.CouponRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite

@Composable
fun StorefrontScreen(
    coupons: List<CouponEntity>,
    notificationCount: Int,
    currentUsername: String? = null,
    onOrdersClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBuyCouponClick: (CouponEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Sticky Header
        StoreHeader(
            notificationCount = notificationCount,
            username = currentUsername,
            onOrdersClick = onOrdersClick,
            onNotificationsClick = onNotificationsClick,
            onAdminClick = onAdminClick,
            onLogoutClick = onLogoutClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A0E13), RoundedCornerShape(99.dp))
                            .border(1.dp, Color(0xFF70232C), RoundedCornerShape(99.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MEME ENERGY • REAL DEALS",
                            color = Color(0xFFFF9AA3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "COUPON ",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = PureWhite,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = "LO.",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = CouponRed,
                            letterSpacing = (-1.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Coupons lo, savings karo. Pick your deal, scan the payment QR and submit your payment proof. Let the memes do the talking.",
                        color = DarkMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Marquee Ticker
            item {
                MarqueeTicker()
            }

            // Section Header
            item {
                val availableCount = coupons.count { it.stock > 0 }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 Available Coupons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = "$availableCount coupon types in stock",
                        fontSize = 12.sp,
                        color = DarkMuted
                    )
                }
            }

            // Coupon Cards
            items(coupons, key = { it.id }) { coupon ->
                CouponCard(
                    coupon = coupon,
                    onBuyClick = { onBuyCouponClick(coupon) }
                )
            }

            // Notice Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF220E12), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF66313A), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "⚠️ Payment QR is generated privately from the configured UPI account. UPI ID is not displayed on the page. A submitted UTR/screenshot is only a payment claim; approve an order only after checking your actual UPI/bank receipt.",
                        fontSize = 12.sp,
                        color = Color(0xFFFFB3BA),
                        lineHeight = 17.sp
                    )
                }
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "COUPON LO © 2026 • Made for meme lovers",
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
fun CouponCard(
    coupon: CouponEntity,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inStock = coupon.stock > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("coupon_card_${coupon.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF18181C), Color(0xFF101013))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = coupon.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = coupon.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = coupon.desc,
                fontSize = 13.sp,
                color = DarkMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${coupon.price.toInt()}",
                    fontSize = 15.sp,
                    color = Color(0xFF888888),
                    textDecoration = TextDecoration.LineThrough,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹${coupon.finalPrice}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF103426), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${coupon.discount.toInt()}% OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CouponGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (inStock) "● IN STOCK · ${coupon.stock} LEFT" else "● SOLD OUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = if (inStock) CouponGreen else Color(0xFFFF6A75)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBuyClick,
                enabled = inStock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("buy_coupon_button_${coupon.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CouponRed,
                    contentColor = PureWhite,
                    disabledContainerColor = Color(0xFF39393F),
                    disabledContentColor = Color(0xFFAAAAAA)
                )
            ) {
                Text(
                    text = if (inStock) "BUY NOW" else "OUT OF STOCK",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
