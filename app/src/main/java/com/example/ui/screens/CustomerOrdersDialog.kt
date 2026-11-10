package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.OrderEntity
import com.example.ui.components.CouponKeyCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerOrdersDialog(
    orders: List<OrderEntity>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = DarkPanel,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Your Orders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_orders_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkMuted
                        )
                    }
                }

                Text(
                    text = "Orders placed on your account appear here. After admin approves and assigns a key, you can view it here.",
                    fontSize = 12.sp,
                    color = DarkMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = DarkMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No orders found yet.",
                                color = DarkMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_order_item_${order.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkPanel2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = order.id,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = PureWhite
                                        )
                                        StatusPill(status = order.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "${order.couponName} · ₹${order.amount}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )

                                    Text(
                                        text = "UTR: ${order.utr}",
                                        fontSize = 12.sp,
                                        color = DarkMuted
                                    )

                                    val dateStr = dateFormat.format(Date(order.createdAt))
                                    Text(
                                        text = dateStr,
                                        fontSize = 11.sp,
                                        color = DarkMuted
                                    )

                                    if (!order.couponKey.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        CouponKeyCard(keyText = order.couponKey)
                                    } else if (order.status.uppercase() == "PENDING") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "⏳ Payment verification in progress. Key will appear once approved.",
                                            fontSize = 11.sp,
                                            color = DarkMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
