package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.LuxuryCard
import com.example.ui.components.NehaFooter
import com.example.ui.components.NehaTopBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.affiliate.AffiliateHuntScreen
import com.example.ui.screens.auth.ChooseCrewScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OtpVerifyScreen
import com.example.ui.screens.brandcrew.BrandCrewScreen
import com.example.ui.theme.LuxuryBackground
import com.example.ui.theme.NehaBrandTheme
import com.example.ui.theme.NehaGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NehaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NehaBrandTheme {
                val viewModel: NehaViewModel = viewModel()
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: NehaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isSinhala by viewModel.isSinhala.collectAsState()

    // Back handling
    BackHandler(enabled = currentScreen !is AppScreen.Login) {
        when (currentScreen) {
            is AppScreen.OtpVerify, is AppScreen.ChooseCrew -> viewModel.navigateTo(AppScreen.Login)
            is AppScreen.BrandCrewBankDetails -> viewModel.navigateTo(AppScreen.BrandCrew)
            is AppScreen.AccessDenied -> {
                user?.let {
                    when (it.role) {
                        UserRole.ADMIN -> viewModel.navigateTo(AppScreen.AdminDashboard)
                        UserRole.AFFILIATE_CREW -> viewModel.navigateTo(AppScreen.AffiliateHunt)
                        UserRole.BRAND_CREW -> viewModel.navigateTo(AppScreen.BrandCrew)
                    }
                } ?: viewModel.navigateTo(AppScreen.Login)
            }
            else -> {
                // If on main crew screen, logout confirmation or stay
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Active Screen
        when (val screen = currentScreen) {
            is AppScreen.Login -> LoginScreen(viewModel = viewModel)
            is AppScreen.OtpVerify -> OtpVerifyScreen(
                email = screen.email,
                simulatedCode = screen.simulatedCode,
                viewModel = viewModel
            )
            is AppScreen.ChooseCrew -> ChooseCrewScreen(
                email = screen.email,
                viewModel = viewModel
            )
            is AppScreen.AffiliateHunt -> AffiliateHuntScreen(viewModel = viewModel)
            is AppScreen.BrandCrew -> BrandCrewScreen(viewModel = viewModel)
            is AppScreen.BrandCrewBankDetails -> com.example.ui.screens.brandcrew.BankDetailsScreen(viewModel = viewModel)
            is AppScreen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
            is AppScreen.AccessDenied -> AccessDeniedScreen(viewModel = viewModel)
        }

        // Realtime Toast / Notification Banner Overlay
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                color = RoyalPurpleDark,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, NehaGold),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NehaGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = notification ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Button(
                            onClick = { viewModel.clearNotification() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("OK", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccessDeniedScreen(viewModel: NehaViewModel) {
    val isSinhala by viewModel.isSinhala.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            NehaTopBar(
                subtitle = "Security Guard",
                isSinhala = isSinhala,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onLogout = { viewModel.logout() }
            )
        },
        containerColor = LuxuryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Surface(
                color = Color(0xFFFEE2E2),
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(44.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Access Denied (Protected Route)",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RoyalPurpleDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your current role (${user?.role?.name}) does not have permission to access this screen. Affiliate Crew and Brand Crew features are strictly isolated.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    user?.let {
                        when (it.role) {
                            UserRole.ADMIN -> viewModel.navigateTo(AppScreen.AdminDashboard)
                            UserRole.AFFILIATE_CREW -> viewModel.navigateTo(AppScreen.AffiliateHunt)
                            UserRole.BRAND_CREW -> viewModel.navigateTo(AppScreen.BrandCrew)
                        }
                    } ?: viewModel.navigateTo(AppScreen.Login)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to My Assigned Dashboard", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
            NehaFooter(isSinhala = isSinhala)
        }
    }
}
