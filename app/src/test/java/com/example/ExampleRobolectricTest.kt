package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.BrandSubRole
import com.example.data.model.ItemStatus
import com.example.data.model.UserRole
import com.example.data.model.Wallet
import com.example.data.repository.NehaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: NehaRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = AppDatabase.getInstance(context)
        repository = NehaRepository(db)
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("NEHA BRAND", appName)
    }

    @Test
    fun `test Sri Lankan phone normalization`() {
        assertEquals("+94771234567", repository.normalizeSriLankanPhone("0771234567"))
        assertEquals("+94771234567", repository.normalizeSriLankanPhone("077 123 4567"))
        assertEquals("+94771234567", repository.normalizeSriLankanPhone("94771234567"))
        assertEquals("+94771234567", repository.normalizeSriLankanPhone("+94771234567"))
    }

    @Test
    fun `test OTP 3-attempt lockout security`() = runBlocking {
        val email = "testuser@nehaworkhub.com"
        val (sent, code) = repository.sendEmailOtp(email)
        assertTrue(sent)
        assertEquals(6, code.length)

        // Attempt 1: wrong code
        val res1 = repository.verifyOtp(email, "000000")
        assertFalse(res1.isSuccess)

        // Attempt 2: wrong code
        val res2 = repository.verifyOtp(email, "111111")
        assertFalse(res2.isSuccess)

        // Attempt 3: wrong code -> should lock for 5 minutes
        val res3 = repository.verifyOtp(email, "222222")
        assertFalse(res3.isSuccess)
        assertTrue(res3.exceptionOrNull()?.message?.contains("Contact Owner: Sathsara D") == true)

        // Attempt 4: should immediately report account locked
        val (sentAgain, lockMsg) = repository.sendEmailOtp(email)
        assertFalse(sentAgain)
        assertTrue(lockMsg.contains("Contact Owner: Sathsara D"))
    }

    @Test
    fun `test service crew payment zeroes balance and records transaction`() = runBlocking {
        // Register a Brand Crew user
        val crew = repository.registerUserProfile(
            email = "kasun.service@nehaworkhub.com",
            phone = "0719876543",
            name = "Kasun Perera",
            role = UserRole.BRAND_CREW,
            subRole = BrandSubRole.PROMOTION
        )

        // Add bank details
        repository.updateBankDetails(
            userId = crew.id,
            bankName = "Commercial Bank",
            bankAccount = "1234567890",
            bankHolder = "Kasun Perera",
            bankBranch = "Nugegoda"
        )

        // Seed wallet with available balance 5000 LKR
        db.walletDao().insertOrUpdate(
            Wallet(userId = crew.id, availableLkr = 5000.0, pendingLkr = 2000.0, totalPaidLkr = 0.0)
        )

        // Owner Sathsara D marks service as paid
        val paymentResult = repository.markServicePaidByOwner(crew.id)
        assertTrue(paymentResult.isSuccess)

        val tx = paymentResult.getOrNull()
        assertNotNull(tx)
        assertTrue(tx!!.transactionId.startsWith("NEHA-PAY-"))
        assertEquals(5000.0, tx.amountLkr, 0.01)

        // Check wallet state: available should be 0, totalPaid should be 5000
        val updatedWallet = db.walletDao().getWallet(crew.id)
        assertNotNull(updatedWallet)
        assertEquals(0.0, updatedWallet!!.availableLkr, 0.01)
        assertEquals(5000.0, updatedWallet.totalPaidLkr, 0.01)
        assertEquals(2000.0, updatedWallet.pendingLkr, 0.01) // pending remains untouched
    }

    @Test
    fun `test bank details submission and admin verification status update`() = runBlocking {
        // Register a Brand Crew member
        val crew = repository.registerUserProfile(
            email = "nimal.brand@nehaworkhub.com",
            phone = "0778899000",
            name = "Nimal Silva",
            role = UserRole.BRAND_CREW,
            subRole = BrandSubRole.EBILL
        )
        assertFalse(crew.bankVerified)

        // Submit bank details -> should be unverified (pending admin verification)
        repository.updateBankDetails(
            userId = crew.id,
            bankName = "People's Bank (මහජන බැංකුව)",
            bankAccount = "204200190012345",
            bankHolder = "Nimal Silva",
            bankBranch = "Kandy City Branch"
        )

        val pendingProfile = repository.getProfile(crew.id)
        assertNotNull(pendingProfile)
        assertEquals("People's Bank (මහජන බැංකුව)", pendingProfile!!.bankName)
        assertEquals("204200190012345", pendingProfile.bankAccount)
        assertFalse(pendingProfile.bankVerified) // Verification status is pending

        // Admin verifies the bank details
        repository.verifyBankDetailsByAdmin(crew.id)

        val verifiedProfile = repository.getProfile(crew.id)
        assertNotNull(verifiedProfile)
        assertTrue(verifiedProfile!!.bankVerified) // Status badge updates to verified!
    }
}
