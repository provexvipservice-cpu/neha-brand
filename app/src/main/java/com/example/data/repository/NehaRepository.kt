package com.example.data.repository

import com.example.data.model.AffiliateRank
import com.example.data.model.AuditLog
import com.example.data.model.BrandSubRole
import com.example.data.model.ItemStatus
import com.example.data.model.ServiceTask
import com.example.data.model.Shop
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
import com.example.data.supabase.SupabaseAuditLog
import com.example.data.supabase.SupabaseBrandTask
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseProfile
import com.example.data.supabase.SupabaseShop
import com.example.data.supabase.SupabaseTaskProof
import com.example.data.supabase.SupabaseWallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * NehaRepository powered entirely by Supabase.
 * Room DB is completely replaced by Supabase backend:
 * - Profiles, Wallets, Service Earnings, Brand Tasks, Task Proofs, and Audit Logs
 * - Realtime Live Wallet subscription
 * - Bank Verification with Green Badge (bank_verified)
 * - Mark as Paid with transaction_id = 'NEHA-PAY-' + random and paid_by = 'Sathsara D'
 */
class NehaRepository {

    val supabase: SupabaseClient = SupabaseClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-memory OTP storage & rate limiting
    private val activeOtps = mutableMapOf<String, String>() // email -> 6-digit code
    private val otpFailedAttempts = mutableMapOf<String, Int>() // email -> count
    private val otpLockoutUntil = mutableMapOf<String, Long>() // email -> timestamp
    private val shopRegistrationTimestamps = mutableMapOf<String, MutableList<Long>>() // userId -> timestamps

    // Active reactive state caches fed by Supabase
    private val _profilesFlow = MutableStateFlow<List<UserProfile>>(emptyList())
    private val _shopsFlow = MutableStateFlow<List<Shop>>(emptyList())
    private val _tasksFlow = MutableStateFlow<List<ServiceTask>>(emptyList())
    private val _transactionsFlow = MutableStateFlow<List<WalletTransaction>>(emptyList())
    private val _auditLogsFlow = MutableStateFlow<List<AuditLog>>(emptyList())

    init {
        refreshAllFromSupabase()
    }

    private fun refreshAllFromSupabase() {
        scope.launch {
            // Load profiles
            val spProfiles = supabase.getAllProfiles()
            _profilesFlow.value = spProfiles.map { toDomainProfile(it) }

            // Load shops
            val spShops = supabase.getShops()
            _shopsFlow.value = spShops.map { toDomainShop(it) }

            // Load tasks
            val spTasks = supabase.getBrandTasks()
            _tasksFlow.value = spTasks.map { toDomainTask(it) }

            // Load audit logs
            val spLogs = supabase.getAuditLogs()
            _auditLogsFlow.value = spLogs.map { toDomainAuditLog(it) }
        }
    }

    // --- Reactive Flows for ViewModels & UI ---

    fun getProfileFlow(userId: String): Flow<UserProfile?> =
        _profilesFlow.map { list -> list.firstOrNull { it.id == userId } }

    fun getAllProfilesFlow(): Flow<List<UserProfile>> = _profilesFlow.asStateFlow()

    suspend fun getProfile(userId: String): UserProfile? {
        val sp = supabase.getProfileById(userId) ?: return null
        val profile = toDomainProfile(sp)
        updateLocalProfile(profile)
        return profile
    }

    suspend fun getProfileByEmail(email: String): UserProfile? {
        val sp = supabase.getProfileByEmail(email) ?: return null
        val profile = toDomainProfile(sp)
        updateLocalProfile(profile)
        return profile
    }

    /**
     * REALTIME WALLET:
     * Directly subscribes to Supabase real-time updates / continuous ticker.
     * Reflects available, pending, and total_paid immediately as admin releases or marks paid.
     */
    fun getWalletFlow(userId: String): Flow<Wallet?> =
        supabase.observeWalletRealtime(userId).map { sw ->
            Wallet(
                userId = sw.userId,
                availableLkr = sw.available,
                pendingLkr = sw.pending,
                totalPaidLkr = sw.totalPaid
            )
        }

    fun getShopsForUser(userId: String): Flow<List<Shop>> =
        _shopsFlow.map { list -> list.filter { it.affiliateUserId == userId } }

    fun getAllShops(): Flow<List<Shop>> = _shopsFlow.asStateFlow()

    fun getPendingShops(): Flow<List<Shop>> =
        _shopsFlow.map { list -> list.filter { it.status == ItemStatus.PENDING } }

    fun getTasksForUser(userId: String): Flow<List<ServiceTask>> =
        _tasksFlow.map { list -> list.filter { it.crewUserId == userId } }

    fun getAllTasks(): Flow<List<ServiceTask>> = _tasksFlow.asStateFlow()

    fun getPendingTasks(): Flow<List<ServiceTask>> =
        _tasksFlow.map { list -> list.filter { it.status == ItemStatus.PENDING } }

    fun getTransactionsForUser(userId: String): Flow<List<WalletTransaction>> =
        _transactionsFlow.map { list -> list.filter { it.userId == userId } }

    fun getAllTransactions(): Flow<List<WalletTransaction>> = _transactionsFlow.asStateFlow()

    fun getAuditLogs(): Flow<List<AuditLog>> = _auditLogsFlow.asStateFlow()

    // 1. Send 6-Digit Email OTP
    fun sendEmailOtp(email: String): Pair<Boolean, String> {
        val now = System.currentTimeMillis()
        val lockout = otpLockoutUntil[email] ?: 0L
        if (now < lockout) {
            val remainingMins = ((lockout - now) / 60000) + 1
            return Pair(false, "Account locked for $remainingMins min. Contact Owner: Sathsara D")
        }

        val code = (100000..999999).random().toString()
        activeOtps[email] = code
        return Pair(true, code)
    }

    // 2. Verify OTP
    suspend fun verifyOtp(email: String, enteredCode: String): Result<UserProfile?> {
        val now = System.currentTimeMillis()
        val lockout = otpLockoutUntil[email] ?: 0L
        if (now < lockout) {
            return Result.failure(Exception("Account locked. Contact Owner: Sathsara D"))
        }

        val correctCode = activeOtps[email]
        if (correctCode == null || correctCode != enteredCode.trim()) {
            val currentAttempts = (otpFailedAttempts[email] ?: 0) + 1
            otpFailedAttempts[email] = currentAttempts
            if (currentAttempts >= 3) {
                otpLockoutUntil[email] = now + (5 * 60 * 1000) // 5 minutes lock
                otpFailedAttempts[email] = 0
                return Result.failure(Exception("OTP failed 3 times! Locked for 5 mins. Contact Owner: Sathsara D"))
            }
            return Result.failure(Exception("Invalid 6-digit code. Attempt $currentAttempts of 3"))
        }

        activeOtps.remove(email)
        otpFailedAttempts.remove(email)

        // Check if user already exists in Supabase
        val existing = getProfileByEmail(email)
        return Result.success(existing)
    }

    // Direct Admin Login Verification
    suspend fun verifyAdminCredentials(email: String, pass: String): Result<UserProfile> {
        if (email.trim().lowercase() == "sathsara.admin@nehaworkhub.com" && pass == "NehaAdmin@2026") {
            var admin = getProfileByEmail(email.trim())
            if (admin == null) {
                admin = UserProfile(
                    id = "admin_sathsara_001",
                    email = "sathsara.admin@nehaworkhub.com",
                    phone = "+94771234567",
                    name = "Sathsara D (Owner)",
                    role = UserRole.ADMIN,
                    bankName = "Commercial Bank",
                    bankAccount = "8009123456",
                    bankHolder = "Sathsara D",
                    bankBranch = "Colombo Super Branch",
                    bankVerified = true,
                    trainingCompleted = true
                )
                supabase.upsertProfile(toSupabaseProfile(admin))
                updateLocalProfile(admin)
            }
            return Result.success(admin)
        }
        return Result.failure(Exception("Invalid admin credentials. Use official email and password."))
    }

    // 3. Register New Profile with Crew Selection
    suspend fun registerUserProfile(
        email: String,
        phone: String,
        name: String,
        role: UserRole,
        subRole: BrandSubRole = BrandSubRole.PROMOTION
    ): UserProfile {
        val normalizedPhone = normalizeSriLankanPhone(phone)
        val newId = UUID.randomUUID().toString()
        val profile = UserProfile(
            id = newId,
            email = email.trim().lowercase(),
            phone = normalizedPhone,
            name = name.ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } },
            role = role,
            subRole = subRole,
            rank = AffiliateRank.Rank1,
            ownerName = "Sathsara D"
        )

        // Write to Supabase profiles & wallets
        supabase.upsertProfile(toSupabaseProfile(profile))
        supabase.upsertWallet(SupabaseWallet(userId = newId, available = 0.0, pending = 0.0, totalPaid = 0.0))
        updateLocalProfile(profile)

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = profile.name,
            action = "USER_REGISTERED",
            amountLkr = 0.0,
            details = "Registered in Supabase as ${role.name} (${subRole.name})",
            device = "Mobile App"
        )
        insertAudit(log)
        return profile
    }

    suspend fun updateUserRole(userId: String, role: UserRole, subRole: BrandSubRole) {
        val current = getProfile(userId) ?: return
        val updated = current.copy(role = role, subRole = subRole)
        supabase.upsertProfile(toSupabaseProfile(updated))
        updateLocalProfile(updated)
    }

    // 4. Update Bank Details -> sets bank_verified = false, saves to Supabase
    suspend fun updateBankDetails(
        userId: String,
        bankName: String,
        bankAccount: String,
        bankHolder: String,
        bankBranch: String
    ) {
        val current = getProfile(userId) ?: return
        val updated = current.copy(
            bankName = bankName,
            bankAccount = bankAccount,
            bankHolder = bankHolder,
            bankBranch = bankBranch,
            bankVerified = false
        )
        updateLocalProfile(updated)

        supabase.updateBankDetails(userId, bankName, bankAccount, bankHolder, bankBranch)

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = current.name,
            action = "BANK_DETAILS_SUBMITTED",
            amountLkr = 0.0,
            details = "Submitted to Supabase: $bankName - $bankAccount ($bankHolder, $bankBranch). Awaiting admin verification.",
            device = "Mobile App"
        )
        insertAudit(log)
    }

    // 5. Verify Bank Details (Admin) -> sets bank_verified = true with Green Badge
    suspend fun verifyBankDetailsByAdmin(userId: String) {
        val current = getProfile(userId) ?: return
        val updated = current.copy(bankVerified = true)
        updateLocalProfile(updated)

        supabase.verifyBankDetails(userId)

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "BANK_ACCOUNT_VERIFIED",
            amountLkr = 0.0,
            details = "Admin verified bank account in Supabase for ${current.name}: ${current.bankName} (${current.bankAccount})",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    suspend fun updateTrainingProgress(userId: String, percent: Int) {
        val current = getProfile(userId) ?: return
        val updated = current.copy(
            trainingProgressPercent = percent,
            trainingCompleted = percent >= 100
        )
        updateLocalProfile(updated)
        supabase.upsertProfile(toSupabaseProfile(updated))
    }

    fun normalizeSriLankanPhone(raw: String): String {
        val clean = raw.trim().replace(" ", "").replace("-", "")
        return when {
            clean.startsWith("+94") -> clean
            clean.startsWith("94") -> "+$clean"
            clean.startsWith("0") -> "+94" + clean.substring(1)
            clean.length == 9 -> "+94$clean"
            else -> if (clean.startsWith("+")) clean else "+94$clean"
        }
    }

    // Rate Limiting: Max 5 shops/hour per user
    suspend fun registerShop(
        userId: String,
        shopName: String,
        ownerName: String,
        ownerPhone: String,
        ownerEmail: String
    ): Result<Shop> {
        val now = System.currentTimeMillis()
        val hourAgo = now - (60 * 60 * 1000)
        val timestamps = shopRegistrationTimestamps.getOrPut(userId) { mutableListOf() }
        timestamps.removeAll { it < hourAgo }

        if (timestamps.size >= 5) {
            return Result.failure(Exception("Rate limit exceeded! Max 5 shops per hour. Please wait."))
        }

        val profile = getProfile(userId)
        val commissionRate = when (profile?.rank) {
            AffiliateRank.Rank2 -> 0.06
            AffiliateRank.Rank3 -> 0.07
            else -> 0.05
        }

        val normalizedPhone = normalizeSriLankanPhone(ownerPhone)
        val shop = Shop(
            id = UUID.randomUUID().toString(),
            affiliateUserId = userId,
            shopName = shopName.trim(),
            ownerName = ownerName.trim(),
            ownerPhone = normalizedPhone,
            ownerEmail = ownerEmail.trim(),
            status = ItemStatus.PENDING,
            commissionRate = commissionRate,
            escrowAmountLkr = 20000.0,
            escrowReleased = false
        )

        // Save in Supabase
        supabase.insertShop(toSupabaseShop(shop))
        _shopsFlow.value = listOf(shop) + _shopsFlow.value
        timestamps.add(now)

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = profile?.name ?: "Affiliate",
            action = "SHOP_HUNTED_PENDING",
            amountLkr = shop.escrowAmountLkr,
            details = "Shop ${shop.shopName} added. Saved to Supabase. Waiting Admin Approval.",
            device = "Mobile App"
        )
        insertAudit(log)

        return Result.success(shop)
    }

    suspend fun approveShop(shop: Shop) {
        val updated = shop.copy(status = ItemStatus.APPROVED, verifiedAt = System.currentTimeMillis())
        supabase.updateShopStatus(shop.id, "APPROVED")
        updateLocalShop(updated)

        // Add commission to affiliate's pending wallet in Supabase
        val commissionLkr = shop.escrowAmountLkr * shop.commissionRate
        val currentWallet = supabase.getWallet(shop.affiliateUserId)
        val newPending = currentWallet.pending + commissionLkr
        supabase.upsertWallet(currentWallet.copy(pending = newPending))

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "SHOP_APPROVED",
            amountLkr = commissionLkr,
            details = "Approved shop ${shop.shopName}. Added LKR $commissionLkr commission to Pending balance in Supabase.",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    suspend fun rejectShop(shop: Shop) {
        val updated = shop.copy(status = ItemStatus.REJECTED)
        supabase.updateShopStatus(shop.id, "REJECTED")
        updateLocalShop(updated)

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "SHOP_REJECTED",
            amountLkr = 0.0,
            details = "Rejected shop ${shop.shopName}.",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    suspend fun releaseEscrow(shop: Shop) {
        if (shop.escrowReleased) return
        val commissionLkr = shop.escrowAmountLkr * shop.commissionRate
        val currentWallet = supabase.getWallet(shop.affiliateUserId)

        val newPending = (currentWallet.pending - commissionLkr).coerceAtLeast(0.0)
        val newAvailable = currentWallet.available + commissionLkr
        supabase.upsertWallet(currentWallet.copy(available = newAvailable, pending = newPending))

        val updatedShop = shop.copy(escrowReleased = true)
        supabase.updateShopStatus(shop.id, "APPROVED", escrowReleased = true)
        updateLocalShop(updatedShop)

        val txId = "NEHA-ESCROW-${System.currentTimeMillis()}-${shop.id.take(6)}"
        val tx = WalletTransaction(
            transactionId = txId,
            userId = shop.affiliateUserId,
            shopOrTaskTitle = "Escrow Release: ${shop.shopName}",
            amountLkr = commissionLkr,
            type = "COMMISSION_RELEASE",
            status = ItemStatus.APPROVED,
            notes = "Released by Owner Sathsara D"
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "ESCROW_RELEASED",
            amountLkr = commissionLkr,
            details = "Moved LKR $commissionLkr from Pending to Available in Supabase for shop ${shop.shopName}.",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    // Submit Task Proof with Live Camera, GPS, Fake Detection
    suspend fun submitTaskProof(
        userId: String,
        shopName: String,
        subRole: BrandSubRole,
        taskTitle: String,
        description: String,
        rewardLkr: Double,
        photoUri: String,
        lat: Double,
        lng: Double,
        capturedTimestamp: Long,
        deviceInfo: String,
        isOfflineQueued: Boolean = false
    ): Result<ServiceTask> {
        val now = System.currentTimeMillis()
        if (!isOfflineQueued && (now - capturedTimestamp) > (2 * 60 * 1000)) {
            return Result.failure(Exception("Fake photo detected! Photo must be captured live within 2 minutes."))
        }

        val taskId = UUID.randomUUID().toString()
        val task = ServiceTask(
            id = taskId,
            crewUserId = userId,
            shopName = shopName,
            subRole = subRole,
            taskTitle = taskTitle,
            description = description,
            rewardAmountLkr = rewardLkr,
            photoUri = photoUri,
            latitude = lat,
            longitude = lng,
            capturedTimestamp = capturedTimestamp,
            deviceInfo = deviceInfo,
            status = ItemStatus.PENDING,
            isOfflineQueued = isOfflineQueued
        )

        // Save in Supabase brand_tasks and task_proofs
        supabase.insertBrandTask(
            SupabaseBrandTask(
                id = taskId,
                userId = userId,
                title = taskTitle,
                shopName = shopName,
                subRole = subRole.name,
                reward = rewardLkr,
                status = "PENDING"
            )
        )
        supabase.insertTaskProof(
            SupabaseTaskProof(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                userId = userId,
                photoUrl = photoUri,
                latitude = lat,
                longitude = lng,
                deviceInfo = deviceInfo
            )
        )
        _tasksFlow.value = listOf(task) + _tasksFlow.value

        // Put reward into crew pending wallet in Supabase
        val wallet = supabase.getWallet(userId)
        supabase.upsertWallet(wallet.copy(pending = wallet.pending + rewardLkr))

        val profile = getProfile(userId)
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = profile?.name ?: "Brand Crew",
            action = "TASK_PROOF_SUBMITTED",
            amountLkr = rewardLkr,
            details = "Task '$taskTitle' at $shopName saved to Supabase with GPS ($lat, $lng). Pending admin approval.",
            device = deviceInfo
        )
        insertAudit(log)

        return Result.success(task)
    }

    suspend fun approveTask(task: ServiceTask) {
        val updated = task.copy(status = ItemStatus.APPROVED)
        supabase.updateBrandTaskStatus(task.id, "APPROVED")
        updateLocalTask(updated)

        val wallet = supabase.getWallet(task.crewUserId)
        val newPending = (wallet.pending - task.rewardAmountLkr).coerceAtLeast(0.0)
        val newAvailable = wallet.available + task.rewardAmountLkr
        supabase.upsertWallet(wallet.copy(pending = newPending, available = newAvailable))

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "TASK_APPROVED",
            amountLkr = task.rewardAmountLkr,
            details = "Task '${task.taskTitle}' approved. LKR ${task.rewardAmountLkr} moved to Available balance in Supabase.",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    suspend fun rejectTask(task: ServiceTask) {
        val updated = task.copy(status = ItemStatus.REJECTED)
        supabase.updateBrandTaskStatus(task.id, "REJECTED")
        updateLocalTask(updated)

        val wallet = supabase.getWallet(task.crewUserId)
        val newPending = (wallet.pending - task.rewardAmountLkr).coerceAtLeast(0.0)
        supabase.upsertWallet(wallet.copy(pending = newPending))

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "TASK_REJECTED",
            amountLkr = task.rewardAmountLkr,
            details = "Task '${task.taskTitle}' rejected. Removed from pending balance.",
            device = "Admin Console"
        )
        insertAudit(log)
    }

    /**
     * MARK SERVICE PAID BY OWNER:
     * When admin clicks PAID:
     * - Generates transaction_id = 'NEHA-PAY-' + random
     * - Sets service_earnings.status = PAID, paid_by = 'Sathsara D' in Supabase
     * - Sets wallets.available = 0, wallets.total_paid increases in Supabase
     * - Realtime wallet updates automatically
     */
    suspend fun markServicePaidByOwner(crewUserId: String): Result<WalletTransaction> {
        val crewProfile = getProfile(crewUserId)
            ?: return Result.failure(Exception("Crew member not found"))

        val wallet = supabase.getWallet(crewUserId)
        val amountToPay = wallet.available
        if (amountToPay <= 0.0) {
            return Result.failure(Exception("No available balance to pay (LKR 0.00)"))
        }

        val randomSuffix = (1000..9999).random()
        val txId = "NEHA-PAY-${System.currentTimeMillis()}-$randomSuffix"

        // Execute payment in Supabase
        supabase.markServicePaid(
            userId = crewUserId,
            amountToPay = amountToPay,
            transactionId = txId,
            paidBy = "Sathsara D"
        )

        // Mark any tasks for this user as PAID locally
        _tasksFlow.value = _tasksFlow.value.map { t ->
            if (t.crewUserId == crewUserId && t.status == ItemStatus.APPROVED) {
                t.copy(status = ItemStatus.PAID)
            } else t
        }

        val tx = WalletTransaction(
            transactionId = txId,
            userId = crewUserId,
            shopOrTaskTitle = "Direct Bank Transfer by Owner Sathsara D",
            amountLkr = amountToPay,
            type = "SERVICE_PAYOUT",
            status = ItemStatus.PAID,
            notes = "Transferred to ${crewProfile.bankName} - A/C ${crewProfile.bankAccount} (${crewProfile.bankHolder})"
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value

        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            actor = "Owner: Sathsara D",
            action = "SERVICE_PAID_BY_SATHSARA",
            amountLkr = amountToPay,
            details = "Transferred LKR $amountToPay in Supabase to ${crewProfile.name}'s bank: ${crewProfile.bankName} (Acc: ${crewProfile.bankAccount}). Balance zeroed. Tx: $txId",
            device = "Owner Console"
        )
        insertAudit(log)

        return Result.success(tx)
    }

    // --- Helper Mappers & Local State Sync ---

    private fun updateLocalProfile(p: UserProfile) {
        val current = _profilesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == p.id }
        if (index >= 0) current[index] = p else current.add(0, p)
        _profilesFlow.value = current
    }

    private fun updateLocalShop(s: Shop) {
        val current = _shopsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == s.id }
        if (index >= 0) current[index] = s else current.add(0, s)
        _shopsFlow.value = current
    }

    private fun updateLocalTask(t: ServiceTask) {
        val current = _tasksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == t.id }
        if (index >= 0) current[index] = t else current.add(0, t)
        _tasksFlow.value = current
    }

    private fun insertAudit(log: AuditLog) {
        _auditLogsFlow.value = listOf(log) + _auditLogsFlow.value
        scope.launch {
            supabase.insertAuditLog(
                SupabaseAuditLog(
                    id = log.id,
                    actor = log.actor,
                    action = log.action,
                    amount = log.amountLkr,
                    details = log.details
                )
            )
        }
    }

    private fun toDomainProfile(sp: SupabaseProfile): UserProfile {
        val role = try {
            UserRole.valueOf(sp.role)
        } catch (_: Exception) {
            UserRole.AFFILIATE_CREW
        }
        return UserProfile(
            id = sp.id,
            email = sp.email,
            phone = "",
            name = sp.fullName ?: sp.email.substringBefore("@"),
            role = role,
            bankName = sp.bankName ?: "",
            bankAccount = sp.bankAccount ?: "",
            bankHolder = sp.bankHolder ?: "",
            bankBranch = sp.bankBranch ?: "",
            bankVerified = sp.bankVerified
        )
    }

    private fun toSupabaseProfile(p: UserProfile): SupabaseProfile {
        return SupabaseProfile(
            id = p.id,
            email = p.email,
            fullName = p.name,
            role = p.role.name,
            bankName = p.bankName.ifBlank { null },
            bankAccount = p.bankAccount.ifBlank { null },
            bankHolder = p.bankHolder.ifBlank { null },
            bankBranch = p.bankBranch.ifBlank { null },
            bankVerified = p.bankVerified
        )
    }

    private fun toDomainShop(s: SupabaseShop): Shop {
        val status = try { ItemStatus.valueOf(s.status) } catch (_: Exception) { ItemStatus.PENDING }
        return Shop(
            id = s.id,
            affiliateUserId = s.affiliateUserId,
            shopName = s.shopName,
            ownerName = s.ownerName,
            ownerPhone = s.ownerPhone,
            ownerEmail = s.ownerEmail,
            status = status,
            commissionRate = s.commissionRate,
            escrowAmountLkr = s.escrowAmount,
            escrowReleased = s.escrowReleased
        )
    }

    private fun toSupabaseShop(s: Shop): SupabaseShop {
        return SupabaseShop(
            id = s.id,
            affiliateUserId = s.affiliateUserId,
            shopName = s.shopName,
            ownerName = s.ownerName,
            ownerPhone = s.ownerPhone,
            ownerEmail = s.ownerEmail,
            status = s.status.name,
            commissionRate = s.commissionRate,
            escrowAmount = s.escrowAmountLkr,
            escrowReleased = s.escrowReleased
        )
    }

    private fun toDomainTask(t: SupabaseBrandTask): ServiceTask {
        val subRole = try { BrandSubRole.valueOf(t.subRole) } catch (_: Exception) { BrandSubRole.PROMOTION }
        val status = try { ItemStatus.valueOf(t.status) } catch (_: Exception) { ItemStatus.PENDING }
        return ServiceTask(
            id = t.id,
            crewUserId = t.userId,
            shopName = t.shopName,
            subRole = subRole,
            taskTitle = t.title,
            description = "Service task for ${t.shopName}",
            rewardAmountLkr = t.reward,
            status = status
        )
    }

    private fun toDomainAuditLog(l: SupabaseAuditLog): AuditLog {
        return AuditLog(
            id = l.id,
            actor = l.actor,
            action = l.action,
            amountLkr = l.amount,
            details = l.details,
            device = "Supabase Engine"
        )
    }
}
