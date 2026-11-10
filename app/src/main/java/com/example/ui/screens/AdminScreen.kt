package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.CouponEntity
import com.example.data.local.OrderEntity
import com.example.ui.components.StatusPill
import com.example.ui.theme.CouponAmber
import com.example.ui.theme.CouponGreen
import com.example.ui.theme.CouponGreenDark
import com.example.ui.theme.CouponRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(18.dp),
            color = DarkPanel,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "🔐 Admin Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Admin PIN",
                    fontSize = 12.sp,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = { Text("Enter PIN (Demo: 2468)", color = DarkMuted, fontSize = 13.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg,
                        focusedBorderColor = CouponRed,
                        unfocusedBorderColor = DarkLine,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onLogin(pin) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_login_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CouponRed, contentColor = PureWhite)
                    ) {
                        Text("LOGIN", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkPanel2, contentColor = PureWhite),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine))
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScreen(
    coupons: List<CouponEntity>,
    orders: List<OrderEntity>,
    notificationCount: Int,
    onBackToStore: () -> Unit,
    onUpdateStock: (couponId: String, newStock: Long) -> Unit,
    onApproveOrder: (order: OrderEntity) -> Unit,
    onRejectOrder: (order: OrderEntity) -> Unit,
    onAssignKey: (orderId: String, key: String) -> Unit,
    onBroadcastNotice: (title: String, body: String) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler {
        onBackToStore()
    }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    var noteTitle by remember { mutableStateOf("") }
    var noteBody by remember { mutableStateOf("") }
    var expandedScreenshot by remember { mutableStateOf<String?>(null) }

    // Map to hold temporary coupon key inputs per order
    val keyInputs = remember { mutableStateMapOf<String, String>() }
    // Map to hold temporary stock inputs per coupon
    val stockInputs = remember { mutableStateMapOf<String, String>() }

    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status.uppercase() == "PENDING" }
    val approvedOrders = orders.count { it.status.uppercase() == "APPROVED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A0E13), RoundedCornerShape(99.dp))
                            .border(1.dp, Color(0xFF70232C), RoundedCornerShape(99.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRIVATE CONTROL ROOM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF9AA3),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "👑 Admin Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                }

                OutlinedButton(
                    onClick = onBackToStore,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkPanel2, contentColor = PureWhite),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine)),
                    modifier = Modifier.testTag("admin_back_to_store_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Store", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Total", "$totalOrders", Modifier.weight(1f))
                StatCard("Pending", "$pendingOrders", Modifier.weight(1f), highlightColor = CouponAmber)
                StatCard("Approved", "$approvedOrders", Modifier.weight(1f), highlightColor = CouponGreen)
                StatCard("Notifs", "$notificationCount", Modifier.weight(1f))
            }
        }

        // Stock Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkPanel),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📦 Stock Manager",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = "Set stock quantity. Zero stock marks deal as OUT OF STOCK on customer devices.",
                        fontSize = 12.sp,
                        color = DarkMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    coupons.forEach { coupon ->
                        val currentText = stockInputs[coupon.id] ?: coupon.stock.toString()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkPanel2, RoundedCornerShape(10.dp))
                                .border(1.dp, DarkLine, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${coupon.emoji} ${coupon.name} · ₹${coupon.finalPrice} (MRP ₹${coupon.price}, ${coupon.discount.toInt()}% off)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PureWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentText,
                                    onValueChange = { stockInputs[coupon.id] = it },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stock_input_${coupon.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkBg,
                                        unfocusedContainerColor = DarkBg,
                                        focusedBorderColor = CouponRed,
                                        unfocusedBorderColor = DarkLine,
                                        focusedTextColor = PureWhite,
                                        unfocusedTextColor = PureWhite
                                    )
                                )

                                Button(
                                    onClick = {
                                        val newStock = stockInputs[coupon.id]?.toLongOrNull() ?: coupon.stock
                                        onUpdateStock(coupon.id, newStock)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CouponRed, contentColor = PureWhite),
                                    modifier = Modifier.testTag("update_stock_button_${coupon.id}")
                                ) {
                                    Text("Update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Send Store Notification
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkPanel),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔔 Send Store Notification",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = { Text("Notification title e.g. Flash Restock!", color = DarkMuted, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_notif_title_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg,
                            focusedBorderColor = CouponRed,
                            unfocusedBorderColor = DarkLine,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteBody,
                        onValueChange = { noteBody = it },
                        placeholder = { Text("Write a message for customers...", color = DarkMuted, fontSize = 13.sp) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_notif_body_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg,
                            focusedBorderColor = CouponRed,
                            unfocusedBorderColor = DarkLine,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onBroadcastNotice(noteTitle, noteBody)
                            noteTitle = ""
                            noteBody = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_notif_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CouponRed, contentColor = PureWhite)
                    ) {
                        Text("Publish notification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Submissions List
        item {
            Text(
                text = "🧾 Payment Submissions (${orders.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )
        }

        if (orders.isEmpty()) {
            item {
                Text(
                    text = "No order submissions yet.",
                    color = DarkMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(orders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_order_card_${order.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkPanel),
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
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                            StatusPill(status = order.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${order.couponName} · ₹${order.amount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )

                        Text(
                            text = "UTR: ${order.utr}",
                            fontSize = 13.sp,
                            color = DarkMuted
                        )

                        val custText = if (order.customer.isNotBlank()) order.customer else "Not provided"
                        Text(
                            text = "Customer: $custText",
                            fontSize = 12.sp,
                            color = DarkMuted
                        )

                        val dateStr = dateFormat.format(Date(order.createdAt))
                        Text(text = dateStr, fontSize = 11.sp, color = DarkMuted)

                        // View screenshot link/button
                        if (order.screenshot.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .clickable { expandedScreenshot = order.screenshot }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color(0xFFFF7D88),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View payment screenshot",
                                    color = Color(0xFFFF7D88),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Approve / Reject buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApproveOrder(order) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_order_button_${order.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CouponGreen, contentColor = PureWhite)
                            ) {
                                Text("Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onRejectOrder(order) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_order_button_${order.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkPanel2, contentColor = PureWhite),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine))
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Assign coupon key input
                        Text(
                            text = "🔑 Give coupon key to customer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val currentKeyInput = keyInputs[order.id] ?: (order.couponKey ?: "")
                        OutlinedTextField(
                            value = currentKeyInput,
                            onValueChange = { keyInputs[order.id] = it },
                            placeholder = { Text("Enter redeem code e.g. GGPRO-8912-XYZ", color = DarkMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("key_input_${order.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg,
                                focusedBorderColor = CouponRed,
                                unfocusedBorderColor = DarkLine,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val keyToSave = keyInputs[order.id]?.trim() ?: order.couponKey ?: ""
                                onAssignKey(order.id, keyToSave)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_and_give_key_button_${order.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CouponRed, contentColor = PureWhite)
                        ) {
                            Text("SAVE & GIVE KEY", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        if (!order.couponKey.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(CouponGreenDark, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "KEY ASSIGNED: ${order.couponKey}",
                                    color = CouponGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Logout & Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkPanel),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ Admin Settings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "Store is managed in private mode with PIN 2468.",
                        fontSize = 12.sp,
                        color = DarkMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_logout_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkPanel2, contentColor = PureWhite),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine))
                    ) {
                        Text("Log Out of Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Full-screen / enlarged screenshot dialog
    if (expandedScreenshot != null) {
        Dialog(
            onDismissRequest = { expandedScreenshot = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val base64Clean = expandedScreenshot!!.substringAfter("base64,")
                    val decodedBitmap = remember(base64Clean) {
                        try {
                            val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (decodedBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decodedBitmap,
                            contentDescription = "Full payment screenshot",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = expandedScreenshot,
                            contentDescription = "Full payment screenshot",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    IconButton(
                        onClick = { expandedScreenshot = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color(0x88000000), RoundedCornerShape(99.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close preview",
                            tint = PureWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = PureWhite
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkPanel2),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkLine)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = DarkMuted, fontSize = 11.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = highlightColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
