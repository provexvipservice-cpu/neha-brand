package com.example.ui.screens.brandcrew

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.components.LuxuryCard
import com.example.ui.components.NehaFooter
import com.example.ui.components.NehaTopBar
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.NehaGold
import com.example.ui.theme.NehaGoldDark
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NehaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val sriLankanBanks = listOf(
        "Commercial Bank of Ceylon",
        "People's Bank (මහජන බැංකුව)",
        "Bank of Ceylon (BOC - ලංකා බැංකුව)",
        "Sampath Bank",
        "Hatton National Bank (HNB)",
        "Nations Trust Bank (NTB)",
        "National Savings Bank (NSB)",
        "Seylan Bank",
        "DFCC Bank",
        "Pan Asia Bank",
        "Union Bank of Colombo",
        "Cargills Bank"
    )

    var selectedBank by remember(user?.bankName) {
        mutableStateOf(user?.bankName?.ifBlank { sriLankanBanks.first() } ?: sriLankanBanks.first())
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var accountNumber by remember(user?.bankAccount) {
        mutableStateOf(user?.bankAccount ?: "")
    }
    var accountHolder by remember(user?.bankHolder) {
        mutableStateOf(user?.bankHolder?.ifBlank { user?.name ?: "" } ?: "")
    }
    var branchName by remember(user?.bankBranch) {
        mutableStateOf(user?.bankBranch ?: "")
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isConfigured = !user?.bankAccount.isNullOrBlank()
    val isVerified = user?.bankVerified == true

    Scaffold(
        topBar = {
            NehaTopBar(
                subtitle = "Bank Details Management",
                isSinhala = isSinhala,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onLogout = { viewModel.logout() }
            )
        },
        containerColor = LuxuryBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Back button and screen title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.BrandCrew) },
                    modifier = Modifier.testTag("back_to_brand_crew_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RoyalPurple
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Text(
                        text = if (isSinhala) "බැංකු ගිණුම් කළමනාකරණය" else "Bank Details Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "Direct Payouts by Owner Sathsara D",
                        fontSize = 11.sp,
                        color = NehaGoldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DYNAMIC STATUS BADGE (Key user requirement)
            LuxuryCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verification Status:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        // Status Badge
                        if (!isConfigured) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD97706)),
                                modifier = Modifier.testTag("bank_status_badge_not_configured")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "NOT CONFIGURED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        } else if (!isVerified) {
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, StatusPending),
                                modifier = Modifier.testTag("bank_status_badge_pending")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = StatusPending, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PENDING ADMIN VERIFICATION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, StatusApproved),
                                modifier = Modifier.testTag("bank_status_badge_verified")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = StatusApproved, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VERIFIED FOR PAYOUTS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanatory badge text
                    val explanationText = when {
                        !isConfigured -> "Add your Sri Lankan bank account below so Owner Sathsara D can transfer your task earnings directly."
                        !isVerified -> "Your bank details have been submitted. Owner Sathsara D will verify your account in the Admin Panel before executing bank transfer payouts."
                        else -> "✓ Verified by Owner Sathsara D. All service task earnings will be transferred directly to this verified account."
                    }

                    Text(
                        text = explanationText,
                        fontSize = 12.sp,
                        color = if (isVerified) Color(0xFF047857) else Color.DarkGray,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FORM FIELDS CARD
            LuxuryCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Bank & Account Credentials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "Select your Sri Lankan bank from the official dropdown list.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Sri Lankan Bank Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Local Sri Lankan Bank") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = RoyalPurple)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalPurple,
                                unfocusedBorderColor = NehaGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("bank_dropdown_field")
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            sriLankanBanks.forEach { bank ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = bank,
                                            fontWeight = if (bank == selectedBank) FontWeight.Bold else FontWeight.Normal,
                                            color = if (bank == selectedBank) RoyalPurple else Color.DarkGray
                                        )
                                    },
                                    onClick = {
                                        selectedBank = bank
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Account Number Input
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = {
                            if (it.length <= 20 && it.all { c -> c.isDigit() || c == '-' }) {
                                accountNumber = it
                            }
                        },
                        label = { Text("Account Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalPurple)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_number_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Account Holder Name
                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        label = { Text("Account Holder Name (As per bank)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_holder_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Branch Name & City
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Branch Name / City (e.g. Colombo Super Branch)") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = RoyalPurple)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("branch_name_input")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = successMessage ?: "",
                            color = Color(0xFF059669),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Notice if editing verified account
                    if (isVerified) {
                        Surface(
                            color = Color(0xFFFBF4E2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NehaGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Notice: Updating existing verified bank details will reset the badge to 'PENDING ADMIN VERIFICATION' until re-approved by Admin.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (accountNumber.isBlank() || accountNumber.length < 6) {
                                errorMessage = "Please enter a valid account number (minimum 6 digits)"
                                successMessage = null
                            } else if (accountHolder.isBlank()) {
                                errorMessage = "Please enter the account holder name"
                                successMessage = null
                            } else if (branchName.isBlank()) {
                                errorMessage = "Please enter your bank branch name"
                                successMessage = null
                            } else {
                                errorMessage = null
                                isSubmitting = true
                                viewModel.saveBankDetails(
                                    bankName = selectedBank,
                                    account = accountNumber.trim(),
                                    holder = accountHolder.trim(),
                                    branch = branchName.trim()
                                ) {
                                    isSubmitting = false
                                    successMessage = "Bank details submitted successfully! Awaiting Admin verification."
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_bank_details_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = NehaGold, modifier = Modifier.size(22.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = NehaGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isConfigured) "Update Bank Details" else "Save & Submit for Verification",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECURITY NOTICE
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NehaGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Banking Security: NEHA BRAND uses these credentials exclusively to disburse task commissions directly to you. Never share your password or OTP code.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            NehaFooter(isSinhala = isSinhala)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
