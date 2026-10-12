package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    AFFILIATE_CREW,
    BRAND_CREW
}

enum class BrandSubRole {
    PROMOTION,
    EBILL,
    STOCK
}

enum class AffiliateRank {
    Rank1, // 5%
    Rank2, // 6%
    Rank3  // 7%
}

enum class ItemStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PAID
}

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val email: String,
    val phone: String,
    val name: String,
    val role: UserRole,
    val subRole: BrandSubRole = BrandSubRole.PROMOTION,
    val rank: AffiliateRank = AffiliateRank.Rank1,
    val bankName: String = "",
    val bankAccount: String = "",
    val bankHolder: String = "",
    val bankBranch: String = "",
    val bankVerified: Boolean = false,
    val trainingCompleted: Boolean = false,
    val trainingProgressPercent: Int = 0,
    val ownerName: String = "Sathsara D",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey val id: String,
    val affiliateUserId: String,
    val shopName: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val status: ItemStatus = ItemStatus.PENDING,
    val commissionRate: Double = 0.05,
    val escrowAmountLkr: Double = 15000.0,
    val escrowReleased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

@Entity(tableName = "tasks")
data class ServiceTask(
    @PrimaryKey val id: String,
    val crewUserId: String,
    val shopName: String,
    val subRole: BrandSubRole,
    val taskTitle: String,
    val description: String,
    val rewardAmountLkr: Double,
    val photoUri: String = "",
    val latitude: Double = 6.9271, // Sri Lanka default
    val longitude: Double = 79.8612,
    val capturedTimestamp: Long = 0L,
    val deviceInfo: String = "Android Device",
    val status: ItemStatus = ItemStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val isOfflineQueued: Boolean = false
)

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey val userId: String,
    val availableLkr: Double = 0.0,
    val pendingLkr: Double = 0.0,
    val totalPaidLkr: Double = 0.0
)

@Entity(tableName = "transactions")
data class WalletTransaction(
    @PrimaryKey val transactionId: String, // Unique idempotency ID e.g. NEHA-PAY-...
    val userId: String,
    val shopOrTaskTitle: String,
    val amountLkr: Double,
    val type: String, // COMMISSION, SERVICE_PAYOUT, ESCROW_RELEASE
    val status: ItemStatus = ItemStatus.PAID,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val actor: String,
    val action: String,
    val amountLkr: Double,
    val details: String,
    val device: String,
    val timestamp: Long = System.currentTimeMillis()
)
