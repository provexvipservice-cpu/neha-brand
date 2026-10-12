package com.example.data.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseProfile(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "role") val role: String,
    @Json(name = "bank_name") val bankName: String? = null,
    @Json(name = "bank_account") val bankAccount: String? = null,
    @Json(name = "bank_holder") val bankHolder: String? = null,
    @Json(name = "bank_branch") val bankBranch: String? = null,
    @Json(name = "bank_verified") val bankVerified: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseWallet(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "available") val available: Double = 0.0,
    @Json(name = "pending") val pending: Double = 0.0,
    @Json(name = "total_paid") val totalPaid: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class SupabaseServiceEarning(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "shop_name") val shopName: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "status") val status: String = "PENDING", // PENDING, APPROVED, PAID
    @Json(name = "transaction_id") val transactionId: String? = null,
    @Json(name = "paid_by") val paidBy: String? = null,
    @Json(name = "paid_at") val paidAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseBrandTask(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "shop_name") val shopName: String,
    @Json(name = "sub_role") val subRole: String,
    @Json(name = "reward") val reward: Double,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseTaskProof(
    @Json(name = "id") val id: String,
    @Json(name = "task_id") val taskId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "latitude") val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "device_info") val deviceInfo: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuditLog(
    @Json(name = "id") val id: String,
    @Json(name = "actor") val actor: String,
    @Json(name = "action") val action: String,
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "details") val details: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseShop(
    @Json(name = "id") val id: String,
    @Json(name = "affiliate_user_id") val affiliateUserId: String,
    @Json(name = "shop_name") val shopName: String,
    @Json(name = "owner_name") val ownerName: String,
    @Json(name = "owner_phone") val ownerPhone: String,
    @Json(name = "owner_email") val ownerEmail: String,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "commission_rate") val commissionRate: Double = 0.05,
    @Json(name = "escrow_amount") val escrowAmount: Double = 20000.0,
    @Json(name = "escrow_released") val escrowReleased: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null
)
