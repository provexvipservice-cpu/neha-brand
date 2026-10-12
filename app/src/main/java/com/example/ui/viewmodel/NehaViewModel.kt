package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLog
import com.example.data.model.BrandSubRole
import com.example.data.model.ServiceTask
import com.example.data.model.Shop
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
import com.example.data.repository.NehaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Login : AppScreen()
    data class OtpVerify(val email: String, val simulatedCode: String) : AppScreen()
    data class ChooseCrew(val email: String) : AppScreen()
    object AffiliateHunt : AppScreen()
    object BrandCrew : AppScreen()
    object BrandCrewBankDetails : AppScreen()
    object AdminDashboard : AppScreen()
    object AccessDenied : AppScreen()
}

class NehaViewModel(application: Application) : AndroidViewModel(application) {

    val repository: NehaRepository = NehaRepository()

    // Language Toggle
    private val _isSinhala = MutableStateFlow(false)
    val isSinhala: StateFlow<Boolean> = _isSinhala.asStateFlow()

    fun toggleLanguage() {
        _isSinhala.value = !_isSinhala.value
    }

    // Active User
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Login)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Notification toast / banner
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    fun clearNotification() {
        _notification.value = null
    }

    fun showToast(msg: String) {
        _notification.value = msg
    }

    // Reactive Wallet for current user
    val currentWallet: StateFlow<Wallet?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getWalletFlow(user.id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive Shops for current user
    val currentShops: StateFlow<List<Shop>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getShopsForUser(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Tasks for current user
    val currentTasks: StateFlow<List<ServiceTask>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getTasksForUser(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Transactions for current user
    val currentTransactions: StateFlow<List<WalletTransaction>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getTransactionsForUser(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allShops: StateFlow<List<Shop>> = repository.getAllShops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<ServiceTask>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProfiles: StateFlow<List<UserProfile>> = repository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication Actions
    fun requestOtp(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || !email.contains("@")) {
                onResult(false, "Please enter a valid email address")
                return@launch
            }
            val (success, messageOrCode) = repository.sendEmailOtp(email.trim().lowercase())
            if (success) {
                _currentScreen.value = AppScreen.OtpVerify(email.trim().lowercase(), messageOrCode)
                _notification.value = "Your NEHA Code: $messageOrCode (Sent to email subject)"
                onResult(true, messageOrCode)
            } else {
                onResult(false, messageOrCode)
            }
        }
    }

    fun submitOtp(email: String, enteredCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyOtp(email, enteredCode)
            result.onSuccess { profile ->
                if (profile != null) {
                    _currentUser.value = profile
                    routeUserByRole(profile.role)
                    onResult(true, "Welcome back, ${profile.name}!")
                } else {
                    // New user -> Ask Role Selection Screen
                    _currentScreen.value = AppScreen.ChooseCrew(email)
                    onResult(true, "Please choose your crew role")
                }
            }.onFailure { err ->
                onResult(false, err.message ?: "Verification failed")
            }
        }
    }

    fun adminDirectLogin(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyAdminCredentials(email, pass)
            result.onSuccess { admin ->
                _currentUser.value = admin
                _currentScreen.value = AppScreen.AdminDashboard
                onResult(true, "Logged in as Owner/Admin: ${admin.name}")
            }.onFailure { err ->
                onResult(false, err.message ?: "Invalid credentials")
            }
        }
    }

    fun selectCrewRole(email: String, name: String, phone: String, role: UserRole, subRole: BrandSubRole) {
        viewModelScope.launch {
            val profile = repository.registerUserProfile(email, phone, name, role, subRole)
            _currentUser.value = profile
            routeUserByRole(role)
            _notification.value = "Registered successfully in ${role.name}!"
        }
    }

    private fun routeUserByRole(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> _currentScreen.value = AppScreen.AdminDashboard
            UserRole.AFFILIATE_CREW -> _currentScreen.value = AppScreen.AffiliateHunt
            UserRole.BRAND_CREW -> _currentScreen.value = AppScreen.BrandCrew
        }
    }

    fun navigateTo(screen: AppScreen) {
        val user = _currentUser.value
        // Enforce protected routes
        if (screen is AppScreen.AdminDashboard && user?.role != UserRole.ADMIN) {
            _currentScreen.value = AppScreen.AccessDenied
            return
        }
        if (screen is AppScreen.BrandCrew && user?.role == UserRole.AFFILIATE_CREW) {
            _currentScreen.value = AppScreen.AccessDenied
            return
        }
        if (screen is AppScreen.AffiliateHunt && user?.role == UserRole.BRAND_CREW) {
            _currentScreen.value = AppScreen.AccessDenied
            return
        }
        _currentScreen.value = screen
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = AppScreen.Login
        _notification.value = "Logged out successfully"
    }

    // Affiliate actions
    fun registerShop(
        shopName: String,
        ownerName: String,
        ownerPhone: String,
        ownerEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.registerShop(
                userId = user.id,
                shopName = shopName,
                ownerName = ownerName,
                ownerPhone = ownerPhone,
                ownerEmail = ownerEmail
            )
            result.onSuccess { shop ->
                _notification.value = "Shop '${shop.shopName}' saved! Status: PENDING_ADMIN_APPROVAL"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to register shop")
            }
        }
    }

    // Brand Crew actions
    fun saveBankDetails(
        bankName: String,
        account: String,
        holder: String,
        branch: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateBankDetails(user.id, bankName, account, holder, branch)
            _currentUser.value = repository.getProfile(user.id)
            _notification.value = "Bank details submitted! Awaiting Admin verification by Owner Sathsara D."
            onSuccess()
        }
    }

    fun adminVerifyBank(crewUserId: String) {
        viewModelScope.launch {
            repository.verifyBankDetailsByAdmin(crewUserId)
            if (_currentUser.value?.id == crewUserId) {
                _currentUser.value = repository.getProfile(crewUserId)
            }
            _notification.value = "Bank account verified successfully for crew member!"
        }
    }

    fun saveTrainingProgress(percent: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateTrainingProgress(user.id, percent)
            _currentUser.value = repository.getProfile(user.id)
            if (percent >= 100) {
                _notification.value = "Training completed 100%! Service Tasks are now unlocked!"
            }
        }
    }

    fun submitTaskProof(
        shopName: String,
        subRole: BrandSubRole,
        title: String,
        desc: String,
        rewardLkr: Double,
        photoUri: String,
        lat: Double,
        lng: Double,
        timestamp: Long,
        deviceInfo: String,
        isOffline: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.submitTaskProof(
                userId = user.id,
                shopName = shopName,
                subRole = subRole,
                taskTitle = title,
                description = desc,
                rewardLkr = rewardLkr,
                photoUri = photoUri,
                lat = lat,
                lng = lng,
                capturedTimestamp = timestamp,
                deviceInfo = deviceInfo,
                isOfflineQueued = isOffline
            )
            res.onSuccess {
                _notification.value = "Task submitted successfully with Live GPS! LKR $rewardLkr added to Pending Balance."
                onResult(true, "Success")
            }.onFailure { err ->
                onResult(false, err.message ?: "Task submission failed")
            }
        }
    }

    // Admin Actions
    fun adminApproveShop(shop: Shop) {
        viewModelScope.launch {
            repository.approveShop(shop)
            _notification.value = "Shop '${shop.shopName}' approved! Commission added to pending wallet."
        }
    }

    fun adminRejectShop(shop: Shop) {
        viewModelScope.launch {
            repository.rejectShop(shop)
            _notification.value = "Shop '${shop.shopName}' rejected."
        }
    }

    fun adminReleaseEscrow(shop: Shop) {
        viewModelScope.launch {
            repository.releaseEscrow(shop)
            _notification.value = "Escrow released for shop '${shop.shopName}'! Funds moved to available balance."
        }
    }

    fun adminApproveTask(task: ServiceTask) {
        viewModelScope.launch {
            repository.approveTask(task)
            _notification.value = "Task '${task.taskTitle}' approved! LKR ${task.rewardAmountLkr} moved to Available balance."
        }
    }

    fun adminRejectTask(task: ServiceTask) {
        viewModelScope.launch {
            repository.rejectTask(task)
            _notification.value = "Task '${task.taskTitle}' rejected."
        }
    }

    fun adminMarkServicePaid(crewUserId: String) {
        viewModelScope.launch {
            val res = repository.markServicePaidByOwner(crewUserId)
            res.onSuccess { tx ->
                _notification.value = "Owner Sathsara D transferred LKR ${tx.amountLkr} to bank! Balance is now 0. Tx: ${tx.transactionId}"
            }.onFailure { err ->
                _notification.value = "Payment failed: ${err.message}"
            }
        }
    }

    fun adminChangeUserRole(userId: String, newRole: UserRole, subRole: BrandSubRole = BrandSubRole.PROMOTION) {
        viewModelScope.launch {
            repository.updateUserRole(userId, newRole, subRole)
            _notification.value = "User role updated to ${newRole.name}."
        }
    }
}
