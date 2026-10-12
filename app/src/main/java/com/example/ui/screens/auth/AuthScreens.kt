package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BrandSubRole
import com.example.data.model.UserRole
import com.example.ui.components.LuxuryCard
import com.example.ui.components.NehaFooter
import com.example.ui.components.NehaTopBar
import com.example.ui.components.SixDigitOtpInput
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.NehaGold
import com.example.ui.theme.NehaGoldDark
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NehaViewModel

@Composable
fun LoginScreen(
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Member OTP, 1: Owner/Admin Login

    var emailInput by remember { mutableStateOf("") }
    var adminEmailInput by remember { mutableStateOf("sathsara.admin@nehaworkhub.com") }
    var adminPasswordInput by remember { mutableStateOf("NehaAdmin@2026") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NehaTopBar(
            subtitle = AppStrings.tagline(isSinhala),
            isSinhala = isSinhala,
            onToggleLanguage = { viewModel.toggleLanguage() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Center Logo 200px Big
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.5.dp, NehaGold, CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = R.drawable.neha_brand_logo,
                contentDescription = "NEHA BRAND 200px Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "NEHA BRAND" in #2E1A8B bold 28px
        Text(
            text = "NEHA BRAND",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RoyalPurple,
            letterSpacing = 1.sp
        )

        // "Grow Your Shop Smart" in gold #C8A951
        Text(
            text = "Grow Your Shop Smart",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NehaGoldDark,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Small "Owner: Sathsara D" in purple
        Text(
            text = "Owner: Sathsara D",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = RoyalPurpleDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Luxury Login Card
        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF7F5FC),
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
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = {
                            Text(
                                text = if (isSinhala) "කණ්ඩායම් පිවිසුම (OTP)" else "Crew Login (OTP)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) RoyalPurple else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = {
                            Text(
                                text = if (isSinhala) "හිමිකරු / ADMIN" else "Owner / Admin",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) RoyalPurple else Color.Gray
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (selectedTab == 0) {
                    // Crew OTP login
                    Text(
                        text = if (isSinhala)
                            "ඔබේ ඊමේල් ලිපිනය ඇතුලත් කරන්න. අපි ඉලක්කම් 6ක කේතයක් එවන්නෙමු."
                        else
                            "Enter your Email. A 6-digit OTP code will be sent to your email subject.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Your Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = RoyalPurple)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            viewModel.requestOtp(emailInput) { success, msg ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_otp_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = NehaGold, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSinhala) "කේතය ලබාගන්න (Send 6-Digit Code)" else "Send 6-Digit Code",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Admin Direct Login
                    Text(
                        text = if (isSinhala)
                            "හිමිකරු සත්සර ඩී - නිල පරිපාලක පද්ධතිය"
                        else
                            "Owner Sathsara D - Official Admin Portal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalPurpleDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = adminEmailInput,
                        onValueChange = { adminEmailInput = it },
                        label = { Text("Admin Email") },
                        leadingIcon = {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoyalPurple)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Admin Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalPurple)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = NehaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            viewModel.adminDirectLogin(adminEmailInput, adminPasswordInput) { success, msg ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = NehaGold, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSinhala) "පරිපාලක ලෙස පිවිසෙන්න" else "Sign In to Admin Dashboard",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NehaFooter(isSinhala = isSinhala)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OtpVerifyScreen(
    email: String,
    simulatedCode: String,
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    var enteredOtp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NehaTopBar(
            subtitle = "Email Verification",
            isSinhala = isSinhala,
            onToggleLanguage = { viewModel.toggleLanguage() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFFF3F0FA),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = RoyalPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isSinhala) "ඉලක්කම් 6ක OTP කේතය" else "Enter 6-Digit OTP Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPurpleDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Code sent to: $email\nCheck email subject: \"Your NEHA Code: $simulatedCode\"",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6 boxes input
                SixDigitOtpInput(
                    otpValue = enteredOtp,
                    onOtpChange = {
                        enteredOtp = it
                        if (it.length == 6) {
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.testTag("otp_boxes_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        viewModel.submitOtp(email, enteredOtp) { success, msg ->
                            isLoading = false
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = enteredOtp.length == 6 && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_otp_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = NehaGold, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSinhala) "කේතය තහවුරු කරන්න (Verify)" else "Verify OTP Code",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.Login) },
                    border = BorderStroke(1.dp, NehaGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Email / Back", color = RoyalPurple)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NehaFooter(isSinhala = isSinhala)
    }
}

@Composable
fun ChooseCrewScreen(
    email: String,
    viewModel: NehaViewModel,
    modifier: Modifier = Modifier
) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var selectedSubRole by remember { mutableStateOf(BrandSubRole.PROMOTION) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NehaTopBar(
            subtitle = "New Member Registration",
            isSinhala = isSinhala,
            onToggleLanguage = { viewModel.toggleLanguage() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isSinhala) "ඔබේ කණ්ඩායම තෝරන්න" else "Choose Your Crew",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RoyalPurpleDark
        )

        Text(
            text = if (isSinhala) "නේහා බ්‍රෑන්ඩ් ජාලයට සාදරයෙන් පිළිගනිමු" else "Welcome to NEHA BRAND Network",
            fontSize = 13.sp,
            color = NehaGoldDark,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile input details
        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Personal Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPurple
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPurple,
                        unfocusedBorderColor = NehaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_user_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number (e.g. 0771234567)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPurple,
                        unfocusedBorderColor = NehaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_user_phone_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option 1: AFFILIATE CREW
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { selectedRole = UserRole.AFFILIATE_CREW }
                .testTag("select_affiliate_crew_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == UserRole.AFFILIATE_CREW) Color(0xFFFBF4E2) else Color.White
            ),
            border = BorderStroke(
                width = if (selectedRole == UserRole.AFFILIATE_CREW) 2.dp else 1.dp,
                color = if (selectedRole == UserRole.AFFILIATE_CREW) RoyalPurple else NehaGold
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = RoyalPurple,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = NehaGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AFFILIATE CREW",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurpleDark
                    )
                    Text(
                        text = "Hunt Shops & Earn 5-7%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NehaGoldDark
                    )
                    Text(
                        text = "Onboard retail stores, get owner OTP, and receive instant commission per verified store.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (selectedRole == UserRole.AFFILIATE_CREW) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = RoyalPurple,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: BRAND CREW
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { selectedRole = UserRole.BRAND_CREW }
                .testTag("select_brand_crew_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == UserRole.BRAND_CREW) Color(0xFFECE8F9) else Color.White
            ),
            border = BorderStroke(
                width = if (selectedRole == UserRole.BRAND_CREW) 2.dp else 1.dp,
                color = if (selectedRole == UserRole.BRAND_CREW) RoyalPurple else NehaGold
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = RoyalPurple,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BRAND CREW",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurpleDark
                        )
                        Text(
                            text = "Service Shops (Promotion, Ebill, Stock)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NehaGoldDark
                        )
                        Text(
                            text = "Execute live shop tasks with live camera proof & GPS. Direct payouts from Owner Sathsara D.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    if (selectedRole == UserRole.BRAND_CREW) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = RoyalPurple,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Sub-role selection for BRAND_CREW
                AnimatedVisibility(visible = selectedRole == UserRole.BRAND_CREW) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Text(
                            text = "Select Sub-Role:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrandSubRole.values().forEach { sub ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedSubRole == sub) RoyalPurple else Color.White,
                                    border = BorderStroke(1.dp, if (selectedSubRole == sub) RoyalPurple else NehaGold),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSubRole = sub }
                                ) {
                                    Text(
                                        text = sub.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedSubRole == sub) Color.White else RoyalPurpleDark,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMsg ?: "",
                color = Color(0xFFDC2626),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nameInput.isBlank()) {
                    errorMsg = "Please enter your name"
                } else if (phoneInput.isBlank()) {
                    errorMsg = "Please enter your phone number"
                } else if (selectedRole == null) {
                    errorMsg = "Please select either Affiliate Crew or Brand Crew"
                } else {
                    errorMsg = null
                    viewModel.selectCrewRole(
                        email = email,
                        name = nameInput,
                        phone = phoneInput,
                        role = selectedRole!!,
                        subRole = selectedSubRole
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .testTag("confirm_crew_selection_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Confirm & Enter Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = NehaGold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NehaFooter(isSinhala = isSinhala)
    }
}
