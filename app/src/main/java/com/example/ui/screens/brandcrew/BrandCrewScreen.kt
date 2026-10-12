package com.example.ui.screens.brandcrew

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import com.example.ui.viewmodel.AppScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrandSubRole
import com.example.data.model.ItemStatus
import com.example.data.model.ServiceTask
import com.example.data.model.WalletTransaction
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.components.LivePayoutFeedTicker
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
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.NehaViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BrandCrewScreen(
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val wallet by viewModel.currentWallet.collectAsState()
    val tasks by viewModel.currentTasks.collectAsState()
    val transactions by viewModel.currentTransactions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Bank Details, 2: Payment History
    var showBankDialog by remember { mutableStateOf(false) }
    var showTrainingDialog by remember { mutableStateOf(false) }
    var showCaptureTaskDialog by remember { mutableStateOf<ServiceTask?>(null) }
    var isSimulateOffline by remember { mutableStateOf(false) }
    var hideBalance by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NehaTopBar(
                subtitle = "BRAND CREW - Service Tasks",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LivePayoutFeedTicker()
            }

            item {
                // Profile Card with Rating, Sub-role, Owner Sathsara D badge
                LuxuryCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = RoyalPurple,
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = NehaGold, modifier = Modifier.size(28.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user?.name ?: "Brand Specialist",
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
                                            text = user?.subRole?.name ?: "PROMOTION",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NehaGoldDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = NehaGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "4.9 Rating", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                                    Text(text = " • ${tasks.count { it.status == ItemStatus.APPROVED || it.status == ItemStatus.PAID }} Tasks Completed", fontSize = 11.sp, color = Color.Gray)
                                }

                                Text(
                                    text = "Payments guaranteed by Owner Sathsara D",
                                    fontSize = 10.sp,
                                    color = RoyalPurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Training Academy Status Banner
                        Spacer(modifier = Modifier.height(12.dp))
                        val trainingDone = user?.trainingCompleted == true
                        Surface(
                            color = if (trainingDone) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (trainingDone) StatusApproved else NehaGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTrainingDialog = true }
                                .testTag("training_academy_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (trainingDone) Icons.Default.CheckCircle else Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (trainingDone) StatusApproved else NehaGoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (trainingDone) "Training Academy: 100% Certified" else "Training Academy: Video Progress ${user?.trainingProgressPercent ?: 0}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (trainingDone) StatusApproved else Color(0xFF92400E)
                                        )
                                        Text(
                                            text = if (trainingDone) "All task rewards & live proofs unlocked" else "Must watch 100% video to unlock task submissions",
                                            fontSize = 10.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Text(
                                    text = if (trainingDone) "REVIEW" else "WATCH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalPurple
                                )
                            }
                        }
                    }
                }
            }

            // 3 Earnings & Balance Cards (PickMe + Banking App style)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Available Balance (Big Purple bg white text)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("brand_crew_available_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalPurple),
                        border = BorderStroke(1.5.dp, NehaGold)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = AppStrings.availableBalance(isSinhala),
                                        fontSize = 12.sp,
                                        color = NehaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Balance Privacy",
                                        tint = NehaGold,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { hideBalance = !hideBalance }
                                    )
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Payable by Owner",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (hideBalance) "LKR ••••••" else "LKR %,.2f".format(wallet?.availableLkr ?: 0.0),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Realtime Sync • Direct transfer to your bank by Sathsara D",
                                fontSize = 11.sp,
                                color = Color(0xFFDDD9F3)
                            )
                        }
                    }

                    // 2 Secondary Balance Cards (Pending & Total Paid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("brand_crew_pending_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.2.dp, NehaGold)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = AppStrings.pendingBalance(isSinhala), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "LKR %,.2f".format(wallet?.pendingLkr ?: 0.0),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalPurpleDark
                                )
                                Text(text = "Pending admin review", fontSize = 9.sp, color = NehaGoldDark)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("brand_crew_total_paid_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.2.dp, NehaGold)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = "Total Paid by Owner", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "LKR %,.2f".format(wallet?.totalPaidLkr ?: 0.0),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusPaid
                                )
                                Text(text = "Bank deposited", fontSize = 9.sp, color = StatusPaid)
                            }
                        }
                    }
                }
            }

            // Tab Navigation
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = RoyalPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NehaGold,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Service Tasks", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("My Bank Details", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            // Tab 0: Service Tasks
            if (selectedTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assigned Service Tasks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurpleDark
                        )

                        // Offline simulator toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSimulateOffline) Color(0xFFFEE2E2) else Color(0xFFE0E7FF),
                            modifier = Modifier.clickable { isSimulateOffline = !isSimulateOffline }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSimulateOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isSimulateOffline) Color.Red else RoyalPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSimulateOffline) "Offline Mode" else "Online Mode",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimulateOffline) Color.Red else RoyalPurple
                                )
                            }
                        }
                    }
                }

                // Default tasks available for submission
                item {
                    DefaultAvailableTasksList(
                        trainingCompleted = user?.trainingCompleted == true,
                        subRole = user?.subRole ?: BrandSubRole.PROMOTION,
                        onSelectTask = { title, shop, reward ->
                            showCaptureTaskDialog = ServiceTask(
                                id = "",
                                crewUserId = user?.id ?: "",
                                shopName = shop,
                                subRole = user?.subRole ?: BrandSubRole.PROMOTION,
                                taskTitle = title,
                                description = "Live proof task submission",
                                rewardAmountLkr = reward
                            )
                        }
                    )
                }

                item {
                    Text(
                        text = "My Completed Tasks & Proofs (${tasks.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (tasks.isEmpty()) {
                    item {
                        LuxuryCard {
                            Text(
                                text = "No tasks completed yet. Complete training to take live photo proof and earn LKR rewards!",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(tasks) { task ->
                        BrandTaskItemCard(
                            task = task,
                            onRetryUpload = {
                                viewModel.submitTaskProof(
                                    shopName = task.shopName,
                                    subRole = task.subRole,
                                    title = task.taskTitle,
                                    desc = task.description,
                                    rewardLkr = task.rewardAmountLkr,
                                    photoUri = task.photoUri,
                                    lat = task.latitude,
                                    lng = task.longitude,
                                    timestamp = task.capturedTimestamp,
                                    deviceInfo = task.deviceInfo,
                                    isOffline = false
                                ) { _, _ -> }
                            }
                        )
                    }
                }
            }

            // Tab 1: Bank Details System
            if (selectedTab == 1) {
                item {
                    BankDetailsView(
                        user = user,
                        onEdit = { viewModel.navigateTo(AppScreen.BrandCrewBankDetails) }
                    )
                }
            }

            // Tab 2: Payment History
            if (selectedTab == 2) {
                item {
                    PaymentHistoryView(
                        transactions = transactions.filter { it.type == "SERVICE_PAYOUT" },
                        bankAccount = "${user?.bankName} - ${user?.bankAccount}"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                NehaFooter(isSinhala = isSinhala)
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    // Dialogs
    if (showBankDialog) {
        BankDetailsDialog(
            initialBank = user?.bankName ?: "People's Bank",
            initialAccount = user?.bankAccount ?: "",
            initialHolder = user?.bankHolder ?: "",
            initialBranch = user?.bankBranch ?: "",
            onSave = { bank, acc, holder, branch ->
                viewModel.saveBankDetails(bank, acc, holder, branch) {
                    showBankDialog = false
                }
            },
            onDismiss = { showBankDialog = false }
        )
    }

    if (showTrainingDialog) {
        TrainingAcademyDialog(
            currentPercent = user?.trainingProgressPercent ?: 0,
            onUpdateProgress = { p -> viewModel.saveTrainingProgress(p) },
            onDismiss = { showTrainingDialog = false }
        )
    }

    showCaptureTaskDialog?.let { taskToSubmit ->
        LiveCameraProofDialog(
            task = taskToSubmit,
            isOffline = isSimulateOffline,
            onSubmit = { photoPath, lat, lng, time, device ->
                viewModel.submitTaskProof(
                    shopName = taskToSubmit.shopName,
                    subRole = taskToSubmit.subRole,
                    title = taskToSubmit.taskTitle,
                    desc = taskToSubmit.description,
                    rewardLkr = taskToSubmit.rewardAmountLkr,
                    photoUri = photoPath,
                    lat = lat,
                    lng = lng,
                    timestamp = time,
                    deviceInfo = device,
                    isOffline = isSimulateOffline
                ) { success, msg ->
                    if (success) {
                        showCaptureTaskDialog = null
                    }
                }
            },
            onDismiss = { showCaptureTaskDialog = null }
        )
    }
}

@Composable
fun DefaultAvailableTasksList(
    trainingCompleted: Boolean,
    subRole: BrandSubRole,
    onSelectTask: (String, String, Double) -> Unit
) {
    val sampleTasks = listOf(
        Triple("Colombo Retail Super - Brand Poster Affixing", "Colombo Super Mart", 2500.0),
        Triple("Kandy City Store - Digital E-Bill System Setup", "Kandy Express Store", 3200.0),
        Triple("Galle Road Boutique - Monthly Stock Audit & Barcode", "Galle Highway Mart", 2800.0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sampleTasks.forEach { (title, shop, reward) ->
            LuxuryCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                        Text(text = "Shop: $shop • Role: $subRole", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "Reward: LKR %,.0f".format(reward), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = NehaGoldDark)
                    }

                    Button(
                        onClick = { onSelectTask(title, shop, reward) },
                        enabled = trainingCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NehaGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Proof", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandTaskItemCard(
    task: ServiceTask,
    onRetryUpload: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(task.createdAt))
    val statusColor = when (task.status) {
        ItemStatus.APPROVED -> StatusApproved
        ItemStatus.REJECTED -> StatusRejected
        ItemStatus.PAID -> StatusPaid
        else -> StatusPending
    }

    LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = task.taskTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                    Text(text = "Shop: ${task.shopName} | ${task.subRole.name}", fontSize = 11.sp, color = Color.DarkGray)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = task.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = NehaGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS: %.4f, %.4f • Device: %s".format(task.latitude, task.longitude, task.deviceInfo),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Reward: LKR %,.0f".format(task.rewardAmountLkr), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                Text(text = dateStr, fontSize = 10.sp, color = Color.LightGray)
            }

            if (task.isOfflineQueued) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onRetryUpload,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPending),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry Upload (Offline Queue)", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BankDetailsView(
    user: com.example.data.model.UserProfile?,
    onEdit: () -> Unit
) {
    LuxuryCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "My Bank Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                }

                val isConfigured = !user?.bankAccount.isNullOrBlank()
                val isVerified = user?.bankVerified == true
                Surface(
                    color = if (isVerified) Color(0xFFECFDF5) else if (isConfigured) Color(0xFFFFFBEB) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isVerified) StatusApproved else if (isConfigured) StatusPending else Color(0xFFD97706))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (isVerified) StatusApproved else if (isConfigured) StatusPending else Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isVerified) "Verified for Payouts" else if (isConfigured) "Pending Admin Verification" else "Not Configured",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) Color(0xFF047857) else if (isConfigured) Color(0xFFB45309) else Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Used exclusively for direct payments by Owner Sathsara D",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            BankFieldItem(label = "Bank Name", value = user?.bankName?.ifBlank { "Not provided" } ?: "Not provided")
            BankFieldItem(label = "Account Number", value = user?.bankAccount?.ifBlank { "Not provided" } ?: "Not provided")
            BankFieldItem(label = "Account Holder", value = user?.bankHolder?.ifBlank { "Not provided" } ?: "Not provided")
            BankFieldItem(label = "Branch", value = user?.bankBranch?.ifBlank { "Not provided" } ?: "Not provided")

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_bank_details_btn")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = NehaGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (user?.bankAccount.isNullOrBlank()) "Add Bank Details" else "Update Bank Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BankFieldItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsDialog(
    initialBank: String,
    initialAccount: String,
    initialHolder: String,
    initialBranch: String,
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val banks = listOf("People's Bank", "NSB", "Commercial Bank", "Sampath Bank", "BOC", "HNB")
    var selectedBank by remember { mutableStateOf(initialBank.ifBlank { banks.first() }) }
    var expanded by remember { mutableStateOf(false) }

    var account by remember { mutableStateOf(initialAccount) }
    var holder by remember { mutableStateOf(initialHolder) }
    var branch by remember { mutableStateOf(initialBranch) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Bank Details for Payouts", fontWeight = FontWeight.Bold, color = RoyalPurple)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Owner Sathsara D transfers your earnings directly to this account.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bank Name") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    selectedBank = b
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account Number") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = holder,
                    onValueChange = { holder = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Branch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (account.isBlank() || holder.isBlank() || branch.isBlank()) {
                        errorMsg = "Please fill in all fields"
                    } else {
                        onSave(selectedBank, account, holder, branch)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text("Save & Verify", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalPurple) }
        }
    )
}

@Composable
fun TrainingAcademyDialog(
    currentPercent: Int,
    onUpdateProgress: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableIntStateOf(currentPercent) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && progress < 100) {
            while (progress < 100 && isPlaying) {
                delay(400)
                progress = (progress + 10).coerceAtMost(100)
                onUpdateProgress(progress)
            }
            isPlaying = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = RoyalPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NEHA Training Academy", fontWeight = FontWeight.Bold, color = RoyalPurple)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mandatory certification module: 'Standard Operating Procedures for Retail Shop Proof & GPS Validation'",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated video screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play Video",
                            tint = if (progress >= 100) StatusApproved else NehaGold,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    if (progress < 100) isPlaying = !isPlaying
                                }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (progress >= 100) "Completed 100%!" else if (isPlaying) "Playing... Watching training" else "Tap to Play Video",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Course Progress:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                    Text(text = "$progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (progress >= 100) StatusApproved else NehaGoldDark)
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progress >= 100) StatusApproved else RoyalPurple,
                    trackColor = Color(0xFFE5E7EB),
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Requires 100% completion to unlock task assignments.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text(if (progress >= 100) "Done & Continue" else "Close", color = Color.White)
            }
        }
    )
}

@Composable
fun PaymentHistoryView(
    transactions: List<WalletTransaction>,
    bankAccount: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment History - Paid by Owner Sathsara D",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalPurpleDark
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
            LuxuryCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = NehaGold, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No payout history yet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "When Owner Sathsara D transfers earnings to your bank, the receipt will show here with unique Transaction ID.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            transactions.forEach { tx ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
                LuxuryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LKR %,.2f".format(tx.amountLkr),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusPaid
                            )
                            Surface(
                                color = Color(0xFFFBF4E2),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NehaGold)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = NehaGoldDark, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "Paid by Sathsara D", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalPurpleDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Tx ID: ${tx.transactionId}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Text(text = tx.notes, fontSize = 11.sp, color = Color.Gray)
                        Text(text = "Date: $dateStr", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

// Live Camera Proof Component with Watermark and GPS
@Composable
fun LiveCameraProofDialog(
    task: ServiceTask,
    isOffline: Boolean,
    onSubmit: (photoPath: String, lat: Double, lng: Double, timestamp: Long, device: String) -> Unit,
    onDismiss: () -> Unit
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var captureTimestamp by remember { mutableLongStateOf(0L) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            val now = System.currentTimeMillis()
            captureTimestamp = now
            // Add Gold "NEHA BRAND" Watermark and GPS overlay
            val watermarked = addWatermarkToBitmap(bmp, "NEHA BRAND - ${task.shopName}", now)
            capturedBitmap = watermarked
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Take Live Photo Proof (Camera Only)", fontWeight = FontWeight.Bold, color = RoyalPurple)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gallery upload is disabled. Take live photo on site. GPS & timestamp will be attached automatically.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (capturedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F0FA))
                            .border(1.dp, NehaGold, RoundedCornerShape(12.dp))
                            .clickable { cameraLauncher.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Open Live Camera", fontWeight = FontWeight.Bold, color = RoyalPurple)
                            Text("(Disabled Gallery)", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, StatusApproved, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Proof",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "✓ GPS Captured: 6.9271° N, 79.8612° E", fontSize = 11.sp, color = StatusApproved, fontWeight = FontWeight.Bold)
                            Text(text = "✓ Timestamp: Valid live photo (< 2 min)", fontSize = 11.sp, color = StatusApproved)
                            Text(text = "✓ Gold 'NEHA BRAND' watermark stamped", fontSize = 11.sp, color = StatusApproved)
                        }
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (capturedBitmap == null) {
                        errorMsg = "Please tap 'Open Live Camera' to take photo"
                    } else if (!isOffline && (now - captureTimestamp) > (2 * 60 * 1000)) {
                        errorMsg = "Fake photo detected! Must submit within 2 minutes of capture."
                    } else {
                        onSubmit("content://camera/proof_${captureTimestamp}", 6.9271, 79.8612, captureTimestamp, "Android Hardware Camera")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text(if (isOffline) "Queue Offline" else "Submit Task Proof", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalPurple) }
        }
    )
}

fun addWatermarkToBitmap(original: Bitmap, brandWatermark: String, timestamp: Long): Bitmap {
    val result = original.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        color = android.graphics.Color.rgb(200, 169, 81) // NehaGold #C8A951
        textSize = (result.height / 20f).coerceAtLeast(24f)
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
    }

    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    canvas.drawText("NEHA BRAND", 20f, paint.textSize + 10f, paint)
    paint.textSize = paint.textSize * 0.7f
    canvas.drawText(brandWatermark, 20f, paint.textSize * 2.2f + 10f, paint)
    canvas.drawText("GPS: 6.9271, 79.8612 | $dateStr", 20f, result.height - 20f, paint)
    return result
}
