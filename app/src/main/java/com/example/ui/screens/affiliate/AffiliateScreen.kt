package com.example.ui.screens.affiliate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemStatus
import com.example.data.model.Shop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.components.LivePayoutFeedTicker
import com.example.ui.components.LuxuryCard
import com.example.ui.components.NehaFooter
import com.example.ui.components.NehaTopBar
import com.example.ui.components.SixDigitOtpInput
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.NehaGold
import com.example.ui.theme.NehaGoldDark
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.NehaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AffiliateHuntScreen(
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val wallet by viewModel.currentWallet.collectAsState()
    val shops by viewModel.currentShops.collectAsState()

    var showAddShopDialog by remember { mutableStateOf(false) }
    var hideBalance by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NehaTopBar(
                subtitle = "AFFILIATE CREW - Hunt Shops",
                isSinhala = isSinhala,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onLogout = { viewModel.logout() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddShopDialog = true },
                containerColor = RoyalPurple,
                contentColor = NehaGold,
                modifier = Modifier.testTag("add_shop_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shop")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hunt Shop", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = LuxuryBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Security Banner (Prevent Screenshot warning)
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confidential Banking & Commission Zone. Keep Private.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }

                        Icon(
                            imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance Privacy",
                            tint = Color(0xFFD97706),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { hideBalance = !hideBalance }
                        )
                    }
                }
            }

            // Live Payout Feed Ticker
            item {
                LivePayoutFeedTicker()
            }

            // Affiliate Rank & Profile Card
            item {
                LuxuryCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = RoyalPurple,
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = NehaGold, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.name ?: "Affiliate Partner",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurpleDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFBF4E2),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, NehaGold)
                                ) {
                                    Text(
                                        text = user?.rank?.name ?: "Rank1",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NehaGoldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            val commissionText = when (user?.rank?.name) {
                                "Rank2" -> "6% Shop Onboarding Commission"
                                "Rank3" -> "7% Shop Onboarding Commission"
                                else -> "5% Shop Onboarding Commission"
                            }
                            Text(text = commissionText, fontSize = 12.sp, color = NehaGoldDark, fontWeight = FontWeight.SemiBold)
                            Text(text = "ID: ${user?.id?.take(10)} | Rate limit: Max 5 shops/hr", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Live Wallet Cards (Realtime Reactive)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Available Balance
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("affiliate_available_balance_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalPurple),
                        border = BorderStroke(1.5.dp, NehaGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = AppStrings.availableBalance(isSinhala),
                                fontSize = 11.sp,
                                color = NehaGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (hideBalance) "LKR ••••••" else "LKR %,.2f".format(wallet?.availableLkr ?: 0.0),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ready for bank payout",
                                fontSize = 10.sp,
                                color = Color(0xFFDDD9F3)
                            )
                        }
                    }

                    // Pending Balance
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("affiliate_pending_balance_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, NehaGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = AppStrings.pendingBalance(isSinhala),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (hideBalance) "LKR ••••••" else "LKR %,.2f".format(wallet?.pendingLkr ?: 0.0),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalPurpleDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Awaiting Admin Release",
                                fontSize = 10.sp,
                                color = NehaGoldDark
                            )
                        }
                    }
                }
            }

            // Your Hunted Shops List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Hunted Shops (${shops.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "Realtime Sync",
                        fontSize = 11.sp,
                        color = StatusApproved,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (shops.isEmpty()) {
                item {
                    LuxuryCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = NehaGold, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No shops hunted yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPurpleDark
                            )
                            Text(
                                text = "Tap 'Hunt Shop' below to onboard a retail shop, verify owner OTP, and earn commission!",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(shops) { shop ->
                    ShopItemCard(shop = shop)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                NehaFooter(isSinhala = isSinhala)
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showAddShopDialog) {
        AddShopDialog(
            viewModel = viewModel,
            onDismiss = { showAddShopDialog = false }
        )
    }
}

@Composable
fun ShopItemCard(shop: Shop) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(shop.createdAt))
    val statusColor = when (shop.status) {
        ItemStatus.APPROVED -> StatusApproved
        ItemStatus.REJECTED -> StatusRejected
        else -> StatusPending
    }

    LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.shopName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "Owner: ${shop.ownerName} | ${shop.ownerPhone}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Email: ${shop.ownerEmail}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = if (shop.status == ItemStatus.PENDING) "PENDING APPROVAL" else shop.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Package Escrow: LKR %,.0f".format(shop.escrowAmountLkr),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                val commissionLkr = shop.escrowAmountLkr * shop.commissionRate
                Text(
                    text = "Commission: LKR %,.0f (%.0f%%)".format(commissionLkr, shop.commissionRate * 100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPurple
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Submitted: $dateStr",
                fontSize = 10.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun AddShopDialog(
    viewModel: NehaViewModel,
    onDismiss: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }

    var step by remember { mutableStateOf(1) } // 1: Info Form, 2: Owner OTP verification
    var generatedOwnerOtp by remember { mutableStateOf("") }
    var enteredOwnerOtp by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Hunt & Register Shop" else "Verify Shop Owner OTP",
                fontWeight = FontWeight.Bold,
                color = RoyalPurple
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 1) {
                    Text(
                        text = "Enter shop and owner details. A 6-digit OTP will be sent to the owner's email to confirm.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Name") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = { Text("Owner Phone (07XXXXXXXX)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = { Text("Owner Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Owner Email: $ownerEmail\nEnter the 6-digit OTP code received by shop owner:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFBF4E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Email Subject: Your NEHA Code: $generatedOwnerOtp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NehaGoldDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    SixDigitOtpInput(
                        otpValue = enteredOwnerOtp,
                        onOtpChange = { enteredOwnerOtp = it }
                    )
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (shopName.isBlank() || ownerName.isBlank() || ownerPhone.isBlank() || ownerEmail.isBlank()) {
                            errorMsg = "Please fill in all fields"
                        } else {
                            errorMsg = null
                            val code = (100000..999999).random().toString()
                            generatedOwnerOtp = code
                            step = 2
                        }
                    } else {
                        if (enteredOwnerOtp != generatedOwnerOtp) {
                            errorMsg = "Invalid OTP code. Please enter the 6-digit code shown in email subject."
                        } else {
                            isSubmitting = true
                            errorMsg = null
                            viewModel.registerShop(
                                shopName = shopName,
                                ownerName = ownerName,
                                ownerPhone = ownerPhone,
                                ownerEmail = ownerEmail,
                                onSuccess = {
                                    isSubmitting = false
                                    onDismiss()
                                },
                                onError = { err ->
                                    isSubmitting = false
                                    errorMsg = err
                                }
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = NehaGold, modifier = Modifier.size(18.dp))
                } else {
                    Text(if (step == 1) "Send Owner OTP" else "Verify & Save Shop", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoyalPurple)
            }
        }
    )
}
