package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.ItemStatus
import com.example.data.model.ServiceTask
import com.example.data.model.Shop
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.LuxuryCard
import com.example.ui.components.NehaFooter
import com.example.ui.components.NehaTopBar
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.NehaGold
import com.example.ui.theme.NehaGoldDark
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected
import com.example.ui.viewmodel.NehaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    val shops by viewModel.allShops.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val profiles by viewModel.allProfiles.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "Approve Shops",
        "Approve Tasks",
        "Service Crew Payments",
        "Escrow Release",
        "Users & Roles",
        "Audit Logs",
        "QR Generator"
    )

    Scaffold(
        topBar = {
            NehaTopBar(
                subtitle = "Admin Panel - Owner: Sathsara D",
                isSinhala = isSinhala,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onLogout = { viewModel.logout() }
            )
        },
        containerColor = LuxuryBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Stats Overview Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricCard(
                        title = "Pending Shops",
                        value = "${shops.count { it.status == ItemStatus.PENDING }}",
                        accentColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Pending Tasks",
                        value = "${tasks.count { it.status == ItemStatus.PENDING }}",
                        accentColor = RoyalPurple,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Total Escrow",
                        value = "LKR %,.0fk".format(shops.sumOf { it.escrowAmountLkr } / 1000),
                        accentColor = NehaGoldDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Scrollable Navigation Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = RoyalPurple,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NehaGold,
                            height = 3.dp
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) RoyalPurple else Color.Gray
                                )
                            }
                        )
                    }
                }
            }

            // Tab 0: Approve Shops
            if (selectedTab == 0) {
                val pendingShops = shops.filter { it.status == ItemStatus.PENDING }
                item {
                    Text(
                        text = "Pending Shop Approvals (${pendingShops.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                }
                if (pendingShops.isEmpty()) {
                    item {
                        EmptyStateCard("No shops awaiting approval.")
                    }
                } else {
                    items(pendingShops) { shop ->
                        AdminShopApprovalCard(
                            shop = shop,
                            onApprove = { viewModel.adminApproveShop(shop) },
                            onReject = { viewModel.adminRejectShop(shop) }
                        )
                    }
                }
            }

            // Tab 1: Approve Tasks
            if (selectedTab == 1) {
                val pendingTasks = tasks.filter { it.status == ItemStatus.PENDING }
                item {
                    Text(
                        text = "Pending Task Proofs with Live GPS (${pendingTasks.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                }
                if (pendingTasks.isEmpty()) {
                    item {
                        EmptyStateCard("No service tasks awaiting approval.")
                    }
                } else {
                    items(pendingTasks) { task ->
                        AdminTaskApprovalCard(
                            task = task,
                            onApprove = { viewModel.adminApproveTask(task) },
                            onReject = { viewModel.adminRejectTask(task) }
                        )
                    }
                }
            }

            // Tab 2: Service Crew Payments (CRITICAL FOR OWNER SATHSARA D)
            if (selectedTab == 2) {
                val crewUsers = profiles.filter { it.role == UserRole.BRAND_CREW }
                item {
                    Column {
                        Text(
                            text = "Service Crew Bank Payments (Owner Direct Transfer)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurpleDark
                        )
                        Text(
                            text = "Transfer to user's registered bank account and click 'Mark as Paid' to zero balance.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (crewUsers.isEmpty()) {
                    item {
                        EmptyStateCard("No Brand Crew members registered yet.")
                    }
                } else {
                    items(crewUsers) { crew ->
                        AdminCrewPaymentCard(
                            crew = crew,
                            viewModel = viewModel,
                            onPayConfirm = {
                                viewModel.adminMarkServicePaid(crew.id)
                            }
                        )
                    }
                }
            }

            // Tab 3: Escrow Release
            if (selectedTab == 3) {
                val approvedShops = shops.filter { it.status == ItemStatus.APPROVED && !it.escrowReleased }
                item {
                    Text(
                        text = "Pending Escrow Releases (${approvedShops.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                }
                if (approvedShops.isEmpty()) {
                    item {
                        EmptyStateCard("All approved shops have escrow released to affiliate wallets.")
                    }
                } else {
                    items(approvedShops) { shop ->
                        AdminEscrowReleaseCard(
                            shop = shop,
                            onRelease = { viewModel.adminReleaseEscrow(shop) }
                        )
                    }
                }
            }

            // Tab 4: Users & Roles
            if (selectedTab == 4) {
                item {
                    AdminUsersManagementTab(
                        profiles = profiles,
                        onChangeRole = { uid, r -> viewModel.adminChangeUserRole(uid, r) }
                    )
                }
            }

            // Tab 5: Audit Logs
            if (selectedTab == 5) {
                item {
                    Text(
                        text = "Realtime Financial & Action Audit Trail (${auditLogs.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                }
                items(auditLogs) { log ->
                    AdminAuditLogCard(log = log)
                }
            }

            // Tab 6: QR Generator
            if (selectedTab == 6) {
                item {
                    AdminQrGeneratorTab(shops = shops.filter { it.status == ItemStatus.APPROVED })
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                NehaFooter(isSinhala = isSinhala)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NehaGold)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
        }
    }
}

@Composable
fun EmptyStateCard(text: String) {
    LuxuryCard {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Composable
fun AdminShopApprovalCard(
    shop: Shop,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    LuxuryCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = shop.shopName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
            Text(text = "Owner: ${shop.ownerName} • Phone: ${shop.ownerPhone}", fontSize = 12.sp, color = Color.DarkGray)
            Text(text = "Email: ${shop.ownerEmail} • Verified by OTP", fontSize = 11.sp, color = Color.Gray)
            Text(text = "Escrow: LKR %,.0f • Rate: %.0f%%".format(shop.escrowAmountLkr, shop.commissionRate * 100), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun AdminTaskApprovalCard(
    task: ServiceTask,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(task.capturedTimestamp))
    LuxuryCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = task.taskTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
            Text(text = "Shop: ${task.shopName} • Sub-role: ${task.subRole.name}", fontSize = 12.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFFF3F0FA),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NehaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Live Proof Photo with NEHA BRAND Watermark", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                    Text(text = "GPS Coordinates: %.4f, %.4f (Sri Lanka)".format(task.latitude, task.longitude), fontSize = 11.sp, color = Color.DarkGray)
                    Text(text = "Device: ${task.deviceInfo} • Time: $dateStr", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "Reward: LKR %,.0f".format(task.rewardAmountLkr), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = NehaGoldDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve Task")
                }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject Task")
                }
            }
        }
    }
}

@Composable
fun AdminCrewPaymentCard(
    crew: UserProfile,
    viewModel: NehaViewModel,
    onPayConfirm: () -> Unit
) {
    var showConfirmModal by remember { mutableStateOf(false) }

    // Read wallet for this crew member directly from repo flow
    val walletState by viewModel.repository.getWalletFlow(crew.id).collectAsState(initial = null)
    val available = walletState?.availableLkr ?: 0.0

    LuxuryCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = crew.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                    Text(text = "Email: ${crew.email} | Phone: ${crew.phone}", fontSize = 11.sp, color = Color.Gray)
                }

                Surface(
                    color = Color(0xFFFBF4E2),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NehaGold)
                ) {
                    Text(
                        text = "Sub-role: ${crew.subRole.name}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bank details box
            Surface(
                color = Color(0xFFF7F5FC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Registered Bank Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                        if (crew.bankVerified) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, StatusApproved)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusApproved,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (!crew.bankAccount.isNullOrBlank()) {
                            Button(
                                onClick = { viewModel.adminVerifyBank(crew.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Verify Account", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(text = "NO BANK RECORD", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = "Bank: ${crew.bankName.ifBlank { "Not provided" }}", fontSize = 11.sp)
                    Text(text = "Account: ${crew.bankAccount.ifBlank { "Not provided" }}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Holder: ${crew.bankHolder.ifBlank { "Not provided" }} • Branch: ${crew.bankBranch.ifBlank { "N/A" }}", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Available Balance: LKR %,.2f".format(available), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (available > 0) RoyalPurple else Color.Gray)
                    Text(text = "Total Paid: LKR %,.2f".format(walletState?.totalPaidLkr ?: 0.0), fontSize = 11.sp, color = StatusPaid)
                }

                Button(
                    onClick = { showConfirmModal = true },
                    enabled = available > 0.0,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Mark as Paid", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmModal) {
        AlertDialog(
            onDismissRequest = { showConfirmModal = false },
            title = {
                Text(text = "Confirm Bank Transfer by Owner Sathsara D", fontWeight = FontWeight.Bold, color = RoyalPurple)
            },
            text = {
                Text(
                    text = "Confirm you transferred LKR %,.2f to %s - %s (%s, %s)?\n\nThis will zero the available balance and record a formal audit transaction with ID NEHA-PAY-...".format(
                        available,
                        crew.bankHolder.ifBlank { crew.name },
                        crew.bankAccount,
                        crew.bankName,
                        crew.bankBranch
                    ),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPayConfirm()
                        showConfirmModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved)
                ) {
                    Text("Yes, Transferred & Mark Paid", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmModal = false }) {
                    Text("Cancel", color = RoyalPurple)
                }
            }
        )
    }
}

@Composable
fun AdminEscrowReleaseCard(
    shop: Shop,
    onRelease: () -> Unit
) {
    LuxuryCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = shop.shopName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
            Text(text = "Package Escrow: LKR %,.0f".format(shop.escrowAmountLkr), fontSize = 12.sp, color = Color.DarkGray)
            val commissionLkr = shop.escrowAmountLkr * shop.commissionRate
            Text(text = "Affiliate Commission: LKR %,.0f (Pending -> Available)".format(commissionLkr), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRelease,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = NehaGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Release Escrow Commission to Affiliate", color = Color.White)
            }
        }
    }
}

@Composable
fun AdminUsersManagementTab(
    profiles: List<UserProfile>,
    onChangeRole: (String, UserRole) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = profiles.filter {
        it.email.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search User by Email or Name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalPurple) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        filtered.forEach { user ->
            LuxuryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                            Text(text = user.email, fontSize = 12.sp, color = Color.Gray)
                        }

                        Surface(
                            color = Color(0xFFF3F0FA),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NehaGold)
                        ) {
                            Text(
                                text = user.role.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (user.role != UserRole.ADMIN) {
                            OutlinedButton(
                                onClick = { onChangeRole(user.id, UserRole.ADMIN) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Make Admin", fontSize = 10.sp, color = RoyalPurple)
                            }
                        }
                        if (user.role != UserRole.AFFILIATE_CREW) {
                            OutlinedButton(
                                onClick = { onChangeRole(user.id, UserRole.AFFILIATE_CREW) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Set Affiliate", fontSize = 10.sp, color = RoyalPurple)
                            }
                        }
                        if (user.role != UserRole.BRAND_CREW) {
                            OutlinedButton(
                                onClick = { onChangeRole(user.id, UserRole.BRAND_CREW) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Set Brand Crew", fontSize = 10.sp, color = RoyalPurple)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogCard(log: AuditLog) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = log.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                Text(text = dateStr, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Actor: ${log.actor} • Device: ${log.device}", fontSize = 11.sp, color = Color.DarkGray)
            Text(text = log.details, fontSize = 12.sp, color = Color.DarkGray)
            if (log.amountLkr > 0.0) {
                Text(text = "Amount: LKR %,.2f".format(log.amountLkr), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NehaGoldDark)
            }
        }
    }
}

@Composable
fun AdminQrGeneratorTab(shops: List<Shop>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Verified Retail QR Badges with NEHA BRAND Crown Emblem",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalPurpleDark
        )
        Text(
            text = "Generate and export official QR stands for approved retail partners.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (shops.isEmpty()) {
            EmptyStateCard("No approved shops yet. Approve shops in the 'Approve Shops' tab to generate QR badges.")
        } else {
            shops.forEach { shop ->
                LuxuryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = RoyalPurple,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = NehaGold, modifier = Modifier.size(80.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "NEHA BRAND VERIFIED MERCHANT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = NehaGoldDark)
                        Text(text = shop.shopName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                        Text(text = "Owner: ${shop.ownerName} • Phone: ${shop.ownerPhone}", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "Merchant ID: NEHA-${shop.id.take(8).uppercase()}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFFF7F5FC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NehaGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Scan to Pay & Record Service Proof • Powered by NEHA BRAND",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPurple,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
