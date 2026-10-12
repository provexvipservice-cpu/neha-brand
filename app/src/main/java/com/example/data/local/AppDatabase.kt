package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getByIdFlow(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: String): UserProfile?

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserProfile?

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE affiliateUserId = :userId ORDER BY createdAt DESC")
    fun getShopsForUser(userId: String): Flow<List<Shop>>

    @Query("SELECT * FROM shops ORDER BY createdAt DESC")
    fun getAllShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingShops(): Flow<List<Shop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shop: Shop)

    @Update
    suspend fun update(shop: Shop)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE crewUserId = :userId ORDER BY createdAt DESC")
    fun getTasksForUser(userId: String): Flow<List<ServiceTask>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<ServiceTask>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingTasks(): Flow<List<ServiceTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ServiceTask)

    @Update
    suspend fun update(task: ServiceTask)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun getWalletFlow(userId: String): Flow<Wallet?>

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWallet(userId: String): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(wallet: Wallet)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: WalletTransaction)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLog)
}

@Database(
    entities = [
        UserProfile::class,
        Shop::class,
        ServiceTask::class,
        Wallet::class,
        WalletTransaction::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): UserProfileDao
    abstract fun shopDao(): ShopDao
    abstract fun taskDao(): TaskDao
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neha_brand_hub.db"
                ).build()
                INSTANCE = instance
                seedDefaultData(instance)
                instance
            }
        }

        private fun seedDefaultData(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val adminEmail = "sathsara.admin@nehaworkhub.com"
                val existing = db.profileDao().getByEmail(adminEmail)
                if (existing == null) {
                    val admin = UserProfile(
                        id = "admin_sathsara_001",
                        email = adminEmail,
                        phone = "+94771234567",
                        name = "Sathsara D (Owner)",
                        role = UserRole.ADMIN,
                        subRole = BrandSubRole.PROMOTION,
                        rank = AffiliateRank.Rank3,
                        bankName = "Commercial Bank",
                        bankAccount = "8009123456",
                        bankHolder = "Sathsara D",
                        bankBranch = "Colombo Super Branch",
                        bankVerified = true,
                        trainingCompleted = true,
                        trainingProgressPercent = 100,
                        ownerName = "Sathsara D"
                    )
                    db.profileDao().insertOrUpdate(admin)
                    db.walletDao().insertOrUpdate(Wallet(userId = admin.id, availableLkr = 50000.0, pendingLkr = 0.0, totalPaidLkr = 250000.0))

                    // Initial sample audit log
                    db.auditLogDao().insert(
                        AuditLog(
                            id = "audit_init_1",
                            actor = "Sathsara D",
                            action = "SYSTEM_INITIALIZED",
                            amountLkr = 0.0,
                            details = "NEHA BRAND Platform initialized with secure escrow and role routing.",
                            device = "AI Studio Engine"
                        )
                    )
                }
            }
        }
    }
}
