package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CouponEntity
import com.example.ui.theme.CouponGreen
import com.example.ui.theme.CouponRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite
import com.example.util.ImageUtils
import com.example.util.QrCodeGenerator
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    coupon: CouponEntity,
    sheetState: SheetState,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (utr: String, screenshotBase64: String, telegram: String) -> Unit
) {
    val context = LocalContext.current
    var utr by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var base64Screenshot by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                base64Screenshot = base64
            } else {
                Toast.makeText(context, "Could not process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val finalAmount = coupon.finalPrice
    val upiId = "9928240425@ybl"
    val payeeName = "COUPON LO"
    val encodedPayee = URLEncoder.encode(payeeName, StandardCharsets.UTF_8.toString())
    val encodedTn = URLEncoder.encode(coupon.name, StandardCharsets.UTF_8.toString())
    val upiUriString = "upi://pay?pa=$upiId&pn=$encodedPayee&am=$finalAmount&cu=INR&tn=$encodedTn"

    val qrBitmap = remember(upiUriString) {
        QrCodeGenerator.generateQrBitmap(upiUriString, size = 360)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkPanel,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💳 Complete Payment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_payment_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Deal & Price summary
            Text(
                text = "${coupon.name} · ₹$finalAmount (${coupon.discount.toInt()}% OFF)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CouponGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code Box
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "UPI Payment QR Code",
                        modifier = Modifier.size(180.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Generating QR...", color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Scan this white-background QR in your UPI app. UPI ID is kept private.",
                fontSize = 12.sp,
                color = DarkMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Open UPI App Button
            OutlinedButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUriString))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_via_upi_app_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkPanel2,
                    contentColor = PureWhite
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkLine))
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CouponRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open in UPI App (GPay / PhonePe / Paytm)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // UTR Field
            Text(
                text = "UTR / Transaction Reference Number",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = utr,
                onValueChange = { if (it.length <= 30) utr = it },
                placeholder = { Text("Enter 12-digit UTR number", color = DarkMuted, fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("utr_input_field"),
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

            Spacer(modifier = Modifier.height(14.dp))

            // Screenshot Picker
            Text(
                text = "Payment Screenshot",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg, RoundedCornerShape(10.dp))
                    .border(1.dp, if (selectedImageUri != null) CouponGreen else DarkLine, RoundedCornerShape(10.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(14.dp)
                    .testTag("upload_screenshot_button"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected screenshot preview",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CouponGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Screenshot attached (Tap to change)",
                            color = CouponGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = CouponRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose Payment Screenshot",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Telegram Field
            Text(
                text = "Your Telegram Username (Optional)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = telegram,
                onValueChange = { telegram = it },
                placeholder = { Text("@username", color = DarkMuted, fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("telegram_input_field"),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    onSubmit(utr, base64Screenshot, telegram)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_payment_proof_button"),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CouponRed,
                    contentColor = PureWhite
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "SUBMIT PAYMENT PROOF",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "After submission, your order stays pending until admin verifies the payment.",
                fontSize = 11.sp,
                color = DarkMuted,
                lineHeight = 15.sp
            )
        }
    }
}
