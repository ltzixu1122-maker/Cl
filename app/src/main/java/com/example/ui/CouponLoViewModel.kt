package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CouponEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.UserEntity
import com.example.data.repository.CouponLoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CouponLoViewModel(
    private val repository: CouponLoRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val coupons: StateFlow<List<CouponEntity>> = repository.observeCoupons()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.observeUserOrders(user.username)
        } else {
            flowOf(emptyList())
        }
    }.catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.observeAllOrders()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.observeNotifications()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCouponForPayment = MutableStateFlow<CouponEntity?>(null)
    val selectedCouponForPayment: StateFlow<CouponEntity?> = _selectedCouponForPayment.asStateFlow()

    private val _showOrdersDialog = MutableStateFlow(false)
    val showOrdersDialog: StateFlow<Boolean> = _showOrdersDialog.asStateFlow()

    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showAdminLoginDialog = MutableStateFlow(false)
    val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repository.login(username, pass)
            _authLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                if (user.role == "ADMIN") {
                    _isAdminMode.value = true
                }
                showToast("Welcome ${user.username}!")
            }.onFailure { err ->
                _authError.value = err.localizedMessage ?: "Invalid credentials"
            }
        }
    }

    fun register(username: String, pass: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repository.register(username, pass)
            _authLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                showToast("Account created! Welcome ${user.username}!")
            }.onFailure { err ->
                _authError.value = err.localizedMessage ?: "Registration failed"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAdminMode.value = false
        showToast("Logged out")
    }

    fun openPayment(coupon: CouponEntity) {
        if (coupon.stock > 0) {
            _selectedCouponForPayment.value = coupon
        } else {
            showToast("This deal is currently out of stock!")
        }
    }

    fun closePayment() {
        _selectedCouponForPayment.value = null
    }

    fun openOrdersDialog() {
        _showOrdersDialog.value = true
    }

    fun closeOrdersDialog() {
        _showOrdersDialog.value = false
    }

    fun openNotificationsDialog() {
        _showNotificationsDialog.value = true
    }

    fun closeNotificationsDialog() {
        _showNotificationsDialog.value = false
    }

    fun openAdminLogin() {
        val user = _currentUser.value
        if (user != null && user.role == "ADMIN") {
            _isAdminMode.value = true
        } else {
            _showAdminLoginDialog.value = true
        }
    }

    fun closeAdminLogin() {
        _showAdminLoginDialog.value = false
    }

    fun verifyAdminPin(pin: String): Boolean {
        return if (pin.trim() == "2468") {
            _isAdminMode.value = true
            _showAdminLoginDialog.value = false
            showToast("Welcome to Admin Dashboard")
            true
        } else {
            showToast("Incorrect Admin PIN")
            false
        }
    }

    fun exitAdminMode() {
        _isAdminMode.value = false
        showToast("Exited Admin mode")
    }

    fun submitPaymentProof(
        coupon: CouponEntity,
        utr: String,
        screenshotBase64: String,
        telegram: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            showToast("Please login first")
            return
        }
        if (utr.isBlank()) {
            showToast("Please enter the UTR number")
            return
        }
        if (screenshotBase64.isBlank()) {
            showToast("Please upload a payment screenshot")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.submitOrder(
                username = user.username,
                coupon = coupon,
                utr = utr,
                screenshotBase64 = screenshotBase64,
                customerHandle = telegram
            )
            _isSubmitting.value = false
            result.onSuccess { orderId ->
                closePayment()
                showToast("Submission received! Order ID: $orderId")
                onSuccess()
            }.onFailure { error ->
                showToast("Failed to submit: ${error.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun updateOrderStatus(order: OrderEntity, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(order, newStatus)
            result.onSuccess {
                showToast("Order marked as $newStatus")
            }.onFailure {
                showToast("Failed to update status")
            }
        }
    }

    fun assignKey(orderId: String, key: String) {
        if (key.isBlank()) {
            showToast("Enter a coupon key first")
            return
        }
        viewModelScope.launch {
            val result = repository.assignCouponKey(orderId, key)
            result.onSuccess {
                showToast("Coupon key assigned to $orderId")
            }.onFailure {
                showToast("Failed to assign key")
            }
        }
    }

    fun updateStock(couponId: String, newStock: Long) {
        viewModelScope.launch {
            val result = repository.updateStock(couponId, newStock)
            result.onSuccess {
                showToast("Stock updated")
            }.onFailure {
                showToast("Failed to update stock")
            }
        }
    }

    fun broadcastNotice(title: String, body: String, onComplete: () -> Unit) {
        if (title.isBlank() || body.isBlank()) {
            showToast("Enter title and message")
            return
        }
        viewModelScope.launch {
            val result = repository.sendNotification(title, body)
            result.onSuccess {
                showToast("Notification published to store")
                onComplete()
            }.onFailure {
                showToast("Failed to publish notification")
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
