package com.example.data.supabase

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * SupabaseClient for NEHA BRAND.
 * Connects to Supabase backend:
 * - URL: https://upfgdufgmzsxzpagynqe.supabase.co
 * - Tables: profiles, wallets, service_earnings, brand_tasks, task_proofs, audit_logs, shops
 *
 * Provides real-time polling for wallets, bank verification, and "Mark as Paid" logic.
 */
class SupabaseClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // In-memory synced state cache for ultra-responsive UI & network resilience
    private val localProfiles = ConcurrentHashMap<String, SupabaseProfile>()
    private val localWallets = ConcurrentHashMap<String, SupabaseWallet>()
    private val localEarnings = ConcurrentHashMap<String, MutableList<SupabaseServiceEarning>>()
    private val localTasks = ConcurrentHashMap<String, MutableList<SupabaseBrandTask>>()
    private val localShops = ConcurrentHashMap<String, MutableList<SupabaseShop>>()
    private val localAuditLogs = mutableListOf<SupabaseAuditLog>()

    init {
        // Seed default Admin: Sathsara D
        val adminId = "admin_sathsara_001"
        val admin = SupabaseProfile(
            id = adminId,
            email = "sathsara.admin@nehaworkhub.com",
            fullName = "Sathsara D (Owner)",
            role = "ADMIN",
            bankName = "Commercial Bank",
            bankAccount = "8009123456",
            bankHolder = "Sathsara D",
            bankBranch = "Colombo Super Branch",
            bankVerified = true,
            createdAt = "2026-10-12T00:00:00Z"
        )
        localProfiles[adminId] = admin
        localProfiles[admin.email.lowercase()] = admin
        localWallets[adminId] = SupabaseWallet(userId = adminId, available = 0.0, pending = 0.0, totalPaid = 0.0)

        // Seed sample Brand Crew member to ensure Bank Details & Verification can be tested immediately
        val crewId = "crew_dilshan_002"
        val sampleCrew = SupabaseProfile(
            id = crewId,
            email = "dilshan.brand@nehaworkhub.com",
            fullName = "Dilshan Perera",
            role = "BRAND_CREW",
            bankName = "Commercial Bank of Ceylon",
            bankAccount = "8004128954",
            bankHolder = "K. A. Dilshan Perera",
            bankBranch = "Kandy City",
            bankVerified = false,
            createdAt = "2026-10-12T00:00:00Z"
        )
        localProfiles[crewId] = sampleCrew
        localProfiles[sampleCrew.email.lowercase()] = sampleCrew
        localWallets[crewId] = SupabaseWallet(userId = crewId, available = 15000.0, pending = 5000.0, totalPaid = 35000.0)

        localEarnings[crewId] = mutableListOf(
            SupabaseServiceEarning(
                id = "earn_001",
                userId = crewId,
                shopName = "Royal Textiles Kandy",
                amount = 15000.0,
                status = "APPROVED",
                createdAt = "2026-10-12T10:00:00Z"
            )
        )
    }

    private fun requestBuilder(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .addHeader("apikey", SupabaseConstants.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConstants.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
    }

    // ==========================================
    // --- PROFILES ---
    // ==========================================

    suspend fun getProfileByEmail(email: String): SupabaseProfile? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles?email=eq.$cleanEmail&select=*"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseProfile::class.java)
                val list = moshi.adapter<List<SupabaseProfile>>(type).fromJson(body)
                val profile = list?.firstOrNull()
                if (profile != null) {
                    localProfiles[profile.id] = profile
                    localProfiles[cleanEmail] = profile
                    return@withContext profile
                }
            }
        } catch (_: Exception) {}
        localProfiles[cleanEmail] ?: localProfiles.values.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
    }

    suspend fun getProfileById(userId: String): SupabaseProfile? = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles?id=eq.$userId&select=*"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseProfile::class.java)
                val list = moshi.adapter<List<SupabaseProfile>>(type).fromJson(body)
                val profile = list?.firstOrNull()
                if (profile != null) {
                    localProfiles[userId] = profile
                    return@withContext profile
                }
            }
        } catch (_: Exception) {}
        localProfiles[userId]
    }

    suspend fun getAllProfiles(): List<SupabaseProfile> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles?select=*&order=created_at.desc"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseProfile::class.java)
                val remoteList = moshi.adapter<List<SupabaseProfile>>(type).fromJson(body)
                if (!remoteList.isNullOrEmpty()) {
                    for (p in remoteList) {
                        localProfiles[p.id] = p
                        localProfiles[p.email.lowercase()] = p
                    }
                    return@withContext remoteList
                }
            }
        } catch (_: Exception) {}
        localProfiles.values.distinctBy { it.id }.sortedByDescending { it.createdAt }
    }

    suspend fun upsertProfile(profile: SupabaseProfile): Boolean = withContext(Dispatchers.IO) {
        localProfiles[profile.id] = profile
        localProfiles[profile.email.lowercase()] = profile
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles"
            val json = moshi.adapter(SupabaseProfile::class.java).toJson(profile)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true // locally saved
        }
    }

    suspend fun updateBankDetails(
        userId: String,
        bankName: String,
        account: String,
        holder: String,
        branch: String
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = localProfiles[userId]
        if (existing != null) {
            val updated = existing.copy(
                bankName = bankName,
                bankAccount = account,
                bankHolder = holder,
                bankBranch = branch,
                bankVerified = false
            )
            localProfiles[userId] = updated
            localProfiles[updated.email.lowercase()] = updated
        }

        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles?id=eq.$userId"
            val payload = mapOf(
                "bank_name" to bankName,
                "bank_account" to account,
                "bank_holder" to holder,
                "bank_branch" to branch,
                "bank_verified" to false
            )
            val json = moshi.adapter(Map::class.java).toJson(payload)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).patch(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    suspend fun verifyBankDetails(userId: String): Boolean = withContext(Dispatchers.IO) {
        val existing = localProfiles[userId]
        if (existing != null) {
            val updated = existing.copy(bankVerified = true)
            localProfiles[userId] = updated
            localProfiles[updated.email.lowercase()] = updated
        }

        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/profiles?id=eq.$userId"
            val payload = mapOf("bank_verified" to true)
            val json = moshi.adapter(Map::class.java).toJson(payload)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).patch(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    // ==========================================
    // --- WALLETS & REALTIME ---
    // ==========================================

    suspend fun getWallet(userId: String): SupabaseWallet = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/wallets?user_id=eq.$userId&select=*"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseWallet::class.java)
                val list = moshi.adapter<List<SupabaseWallet>>(type).fromJson(body)
                val remoteWallet = list?.firstOrNull()
                if (remoteWallet != null) {
                    localWallets[userId] = remoteWallet
                    return@withContext remoteWallet
                }
            }
        } catch (_: Exception) {}
        localWallets.getOrPut(userId) {
            SupabaseWallet(userId = userId, available = 0.0, pending = 0.0, totalPaid = 0.0)
        }
    }

    suspend fun upsertWallet(wallet: SupabaseWallet): Boolean = withContext(Dispatchers.IO) {
        localWallets[wallet.userId] = wallet
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/wallets"
            val json = moshi.adapter(SupabaseWallet::class.java).toJson(wallet)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Realtime subscription / polling Flow for wallets.
     * Tickers every 3 seconds to fetch the latest balance from Supabase,
     * ensuring the user's available, pending, and total paid amounts are always live.
     */
    fun observeWalletRealtime(userId: String): Flow<SupabaseWallet> = flow {
        while (true) {
            val wallet = getWallet(userId)
            emit(wallet)
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // --- SERVICE EARNINGS & MARK AS PAID ---
    // ==========================================

    suspend fun getServiceEarnings(userId: String): List<SupabaseServiceEarning> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/service_earnings?user_id=eq.$userId&select=*&order=created_at.desc"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseServiceEarning::class.java)
                val list = moshi.adapter<List<SupabaseServiceEarning>>(type).fromJson(body)
                if (!list.isNullOrEmpty()) {
                    localEarnings[userId] = list.toMutableList()
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        localEarnings[userId] ?: emptyList()
    }

    suspend fun insertServiceEarning(earning: SupabaseServiceEarning): Boolean = withContext(Dispatchers.IO) {
        val list = localEarnings.getOrPut(earning.userId) { mutableListOf() }
        list.add(0, earning)
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/service_earnings"
            val json = moshi.adapter(SupabaseServiceEarning::class.java).toJson(earning)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).post(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    /**
     * MARK AS PAID:
     * When admin clicks PAID:
     * 1. Sets service_earnings.status = PAID, transaction_id = 'NEHA-PAY-' + random, paid_by = 'Sathsara D'
     * 2. Sets wallets.available = 0.0, wallets.total_paid increase
     * 3. Inserts into audit_logs
     */
    suspend fun markServicePaid(
        userId: String,
        amountToPay: Double,
        transactionId: String,
        paidBy: String = "Sathsara D"
    ): Boolean = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        // 1. Update local earnings
        val earningsList = localEarnings[userId]
        earningsList?.forEachIndexed { index, earning ->
            if (earning.status != "PAID") {
                earningsList[index] = earning.copy(
                    status = "PAID",
                    transactionId = transactionId,
                    paidBy = paidBy,
                    paidAt = nowStr
                )
            }
        }

        // 2. Update local wallet
        val existingWallet = getWallet(userId)
        val newTotalPaid = existingWallet.totalPaid + amountToPay
        val updatedWallet = existingWallet.copy(
            available = 0.0,
            totalPaid = newTotalPaid
        )
        localWallets[userId] = updatedWallet

        // 3. Post to Supabase REST
        try {
            // Update earnings in Supabase
            val earningsUrl = "${SupabaseConstants.REST_ENDPOINT}/service_earnings?user_id=eq.$userId&status=neq.PAID"
            val earningsPayload = mapOf(
                "status" to "PAID",
                "transaction_id" to transactionId,
                "paid_by" to paidBy,
                "paid_at" to nowStr
            )
            val earningsJson = moshi.adapter(Map::class.java).toJson(earningsPayload)
            val earningsReq = requestBuilder(earningsUrl).patch(earningsJson.toRequestBody(jsonMediaType)).build()
            client.newCall(earningsReq).execute()

            // Update wallet in Supabase
            val walletUrl = "${SupabaseConstants.REST_ENDPOINT}/wallets?user_id=eq.$userId"
            val walletPayload = mapOf(
                "available" to 0.0,
                "total_paid" to newTotalPaid
            )
            val walletJson = moshi.adapter(Map::class.java).toJson(walletPayload)
            val walletReq = requestBuilder(walletUrl).patch(walletJson.toRequestBody(jsonMediaType)).build()
            client.newCall(walletReq).execute()
        } catch (_: Exception) {}

        // 4. Audit Log
        insertAuditLog(
            SupabaseAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                actor = "Owner: $paidBy",
                action = "SERVICE_PAID_BY_SATHSARA",
                amount = amountToPay,
                details = "Direct bank payout executed by Owner $paidBy. Balance zeroed. Tx: $transactionId",
                createdAt = nowStr
            )
        )

        return@withContext true
    }

    // ==========================================
    // --- BRAND TASKS & PROOFS ---
    // ==========================================

    suspend fun getBrandTasks(userId: String? = null): List<SupabaseBrandTask> = withContext(Dispatchers.IO) {
        try {
            val url = if (userId != null) {
                "${SupabaseConstants.REST_ENDPOINT}/brand_tasks?user_id=eq.$userId&select=*&order=created_at.desc"
            } else {
                "${SupabaseConstants.REST_ENDPOINT}/brand_tasks?select=*&order=created_at.desc"
            }
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseBrandTask::class.java)
                val list = moshi.adapter<List<SupabaseBrandTask>>(type).fromJson(body)
                if (!list.isNullOrEmpty()) {
                    if (userId != null) localTasks[userId] = list.toMutableList()
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        if (userId != null) localTasks[userId] ?: emptyList()
        else localTasks.values.flatten()
    }

    suspend fun insertBrandTask(task: SupabaseBrandTask): Boolean = withContext(Dispatchers.IO) {
        val list = localTasks.getOrPut(task.userId) { mutableListOf() }
        list.add(0, task)
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/brand_tasks"
            val json = moshi.adapter(SupabaseBrandTask::class.java).toJson(task)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).post(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    suspend fun updateBrandTaskStatus(taskId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        localTasks.values.forEach { list ->
            list.forEachIndexed { i, t ->
                if (t.id == taskId) {
                    list[i] = t.copy(status = newStatus)
                }
            }
        }
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/brand_tasks?id=eq.$taskId"
            val payload = mapOf("status" to newStatus)
            val json = moshi.adapter(Map::class.java).toJson(payload)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).patch(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    suspend fun insertTaskProof(proof: SupabaseTaskProof): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/task_proofs"
            val json = moshi.adapter(SupabaseTaskProof::class.java).toJson(proof)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).post(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    // ==========================================
    // --- SHOPS ---
    // ==========================================

    suspend fun getShops(userId: String? = null): List<SupabaseShop> = withContext(Dispatchers.IO) {
        try {
            val url = if (userId != null) {
                "${SupabaseConstants.REST_ENDPOINT}/shops?affiliate_user_id=eq.$userId&select=*&order=created_at.desc"
            } else {
                "${SupabaseConstants.REST_ENDPOINT}/shops?select=*&order=created_at.desc"
            }
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseShop::class.java)
                val list = moshi.adapter<List<SupabaseShop>>(type).fromJson(body)
                if (!list.isNullOrEmpty()) {
                    if (userId != null) localShops[userId] = list.toMutableList()
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        if (userId != null) localShops[userId] ?: emptyList()
        else localShops.values.flatten()
    }

    suspend fun insertShop(shop: SupabaseShop): Boolean = withContext(Dispatchers.IO) {
        val list = localShops.getOrPut(shop.affiliateUserId) { mutableListOf() }
        list.add(0, shop)
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/shops"
            val json = moshi.adapter(SupabaseShop::class.java).toJson(shop)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).post(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    suspend fun updateShopStatus(shopId: String, status: String, escrowReleased: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        localShops.values.forEach { list ->
            list.forEachIndexed { i, s ->
                if (s.id == shopId) {
                    list[i] = s.copy(status = status, escrowReleased = escrowReleased)
                }
            }
        }
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/shops?id=eq.$shopId"
            val payload = mapOf("status" to status, "escrow_released" to escrowReleased)
            val json = moshi.adapter(Map::class.java).toJson(payload)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).patch(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    // ==========================================
    // --- AUDIT LOGS ---
    // ==========================================

    suspend fun getAuditLogs(): List<SupabaseAuditLog> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/audit_logs?select=*&order=created_at.desc&limit=100"
            val request = requestBuilder(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val type = Types.newParameterizedType(List::class.java, SupabaseAuditLog::class.java)
                val list = moshi.adapter<List<SupabaseAuditLog>>(type).fromJson(body)
                if (!list.isNullOrEmpty()) {
                    localAuditLogs.clear()
                    localAuditLogs.addAll(list)
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        localAuditLogs.toList()
    }

    suspend fun insertAuditLog(log: SupabaseAuditLog): Boolean = withContext(Dispatchers.IO) {
        localAuditLogs.add(0, log)
        try {
            val url = "${SupabaseConstants.REST_ENDPOINT}/audit_logs"
            val json = moshi.adapter(SupabaseAuditLog::class.java).toJson(log)
            val body = json.toRequestBody(jsonMediaType)
            val request = requestBuilder(url).post(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }
}
