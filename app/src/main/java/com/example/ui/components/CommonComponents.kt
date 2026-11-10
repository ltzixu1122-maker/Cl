package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CouponAmber
import com.example.ui.theme.CouponAmberDark
import com.example.ui.theme.CouponGreen
import com.example.ui.theme.CouponGreenDark
import com.example.ui.theme.CouponRed
import com.example.ui.theme.CouponRedDark
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite

import androidx.compose.material.icons.automirrored.filled.Logout

@Composable
fun StoreHeader(
    notificationCount: Int,
    username: String? = null,
    onOrdersClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkPanel,
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "COUPON ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "LO.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CouponRed,
                    letterSpacing = 1.sp
                )
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOrdersClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkPanel2,
                        contentColor = PureWhite
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine)),
                    modifier = Modifier.testTag("orders_header_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Orders",
                        modifier = Modifier.size(16.dp),
                        tint = PureWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ORDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(
                                containerColor = CouponRed,
                                contentColor = PureWhite
                            ) {
                                Text(
                                    text = "$notificationCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkPanel2, RoundedCornerShape(8.dp))
                            .border(1.dp, DarkLine, RoundedCornerShape(8.dp))
                            .testTag("notifications_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DarkPanel2, RoundedCornerShape(8.dp))
                        .border(1.dp, DarkLine, RoundedCornerShape(8.dp))
                        .testTag("admin_header_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = CouponRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onLogoutClick != null) {
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkPanel2, RoundedCornerShape(8.dp))
                            .border(1.dp, DarkLine, RoundedCornerShape(8.dp))
                            .testTag("user_logout_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = DarkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarqueeTicker(
    text: String = "🔥 DEALS • MEMES • COUPONS • GOOD VIBES • 🔥 DEALS • MEMES • COUPONS • GOOD VIBES • ",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBg)
            .border(width = 1.dp, color = DarkLine)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = (offsetProgress * 600).toInt(), y = 0) }
        ) {
            Text(
                text = "$text $text $text",
                color = Color(0xFFFF7C86),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "APPROVED" -> CouponGreenDark to CouponGreen
        "REJECTED" -> CouponRedDark to Color(0xFFFF929B)
        else -> CouponAmberDark to CouponAmber
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun CouponKeyCard(keyText: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF220E12), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF66313A), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔑 Your Coupon Key",
                    color = Color(0xFFFFB3BA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Coupon Key", keyText)
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Coupon Key",
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = keyText,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                letterSpacing = 1.sp
            )
        }
    }
}
