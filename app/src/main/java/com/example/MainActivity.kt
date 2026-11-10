package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.UserEntity
import com.example.data.repository.CouponLoRepository
import com.example.ui.CouponLoViewModel
import com.example.ui.screens.AdminLoginDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CustomerOrdersDialog
import com.example.ui.screens.NotificationsDialog
import com.example.ui.screens.PaymentBottomSheet
import com.example.ui.screens.StorefrontScreen
import com.example.ui.theme.CouponLoTheme
import com.example.ui.theme.CouponRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkPanel2
import com.example.ui.theme.PureWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CouponLoTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { CouponLoRepository(context) }
    val viewModel: CouponLoViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CouponLoViewModel(repository) as T
            }
        }
    )

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    if (currentUser == null) {
        AuthScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onLogin = { u, p -> viewModel.login(u, p) },
            onRegister = { u, p -> viewModel.register(u, p) }
        )
    } else {
        MainFeatureApp(
            currentUser = currentUser!!,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeatureApp(
    currentUser: UserEntity,
    viewModel: CouponLoViewModel
) {
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val selectedCouponForPayment by viewModel.selectedCouponForPayment.collectAsStateWithLifecycle()
    val showOrdersDialog by viewModel.showOrdersDialog.collectAsStateWithLifecycle()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsStateWithLifecycle()
    val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val paymentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAdminMode) {
                AdminScreen(
                    coupons = coupons,
                    orders = allOrders,
                    notificationCount = notifications.size,
                    onBackToStore = { viewModel.exitAdminMode() },
                    onUpdateStock = { couponId, newStock -> viewModel.updateStock(couponId, newStock) },
                    onApproveOrder = { order -> viewModel.updateOrderStatus(order, "APPROVED") },
                    onRejectOrder = { order -> viewModel.updateOrderStatus(order, "REJECTED") },
                    onAssignKey = { orderId, key -> viewModel.assignKey(orderId, key) },
                    onBroadcastNotice = { title, body -> viewModel.broadcastNotice(title, body) {} },
                    onLogout = { viewModel.logout() }
                )
            } else {
                StorefrontScreen(
                    coupons = coupons,
                    notificationCount = notifications.size,
                    currentUsername = currentUser.username,
                    onOrdersClick = { viewModel.openOrdersDialog() },
                    onNotificationsClick = { viewModel.openNotificationsDialog() },
                    onAdminClick = { viewModel.openAdminLogin() },
                    onLogoutClick = { viewModel.logout() },
                    onBuyCouponClick = { coupon -> viewModel.openPayment(coupon) }
                )
            }

            // In-app Toast message popup
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                if (toastMessage != null) {
                    Box(
                        modifier = Modifier
                            .background(DarkPanel2, RoundedCornerShape(10.dp))
                            .border(1.dp, CouponRed, RoundedCornerShape(10.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = toastMessage!!,
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LaunchedEffect(toastMessage) {
                        kotlinx.coroutines.delay(2600)
                        viewModel.clearToast()
                    }
                }
            }
        }
    }

    // Payment Bottom Sheet
    if (selectedCouponForPayment != null) {
        PaymentBottomSheet(
            coupon = selectedCouponForPayment!!,
            sheetState = paymentSheetState,
            isSubmitting = isSubmitting,
            onDismiss = { viewModel.closePayment() },
            onSubmit = { utr, screenshotBase64, telegram ->
                viewModel.submitPaymentProof(
                    coupon = selectedCouponForPayment!!,
                    utr = utr,
                    screenshotBase64 = screenshotBase64,
                    telegram = telegram
                ) {
                    viewModel.openOrdersDialog()
                }
            }
        )
    }

    // Customer Orders Dialog
    if (showOrdersDialog) {
        CustomerOrdersDialog(
            orders = userOrders,
            onDismiss = { viewModel.closeOrdersDialog() }
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { viewModel.closeNotificationsDialog() }
        )
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { viewModel.closeAdminLogin() },
            onLogin = { pin ->
                viewModel.verifyAdminPin(pin)
            }
        )
    }
}
