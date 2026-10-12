package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.NehaGold
import com.example.ui.theme.NehaGoldDark
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleDark
import com.example.ui.util.AppStrings

@Composable
fun NehaTopBar(
    subtitle: String = "",
    isSinhala: Boolean,
    onToggleLanguage: () -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = R.drawable.neha_brand_logo,
                        contentDescription = "NEHA BRAND Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NehaGold, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = AppStrings.brandName(isSinhala),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RoyalPurple,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFFBF4E2),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, NehaGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Owner",
                                        tint = NehaGoldDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Owner: Sathsara D",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoyalPurpleDark
                                    )
                                }
                            }
                        }

                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sinhala / English Toggle
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF3F0FA),
                        border = BorderStroke(1.dp, NehaGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { onToggleLanguage() }
                            .testTag("language_toggle_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = RoyalPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSinhala) "සිංහල" else "EN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPurple
                            )
                        }
                    }

                    if (onLogout != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = RoyalPurpleDark
                            )
                        }
                    }
                }
            }

            // Luxury gold line separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(RoyalPurple, NehaGold, RoyalPurple)
                        )
                    )
            )
        }
    }
}

@Composable
fun NehaFooter(
    isSinhala: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(1.dp)
                .background(NehaGold.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = AppStrings.footer(isSinhala),
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke = BorderStroke(1.dp, NehaGold),
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun SixDigitOtpInput(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otpValue,
        onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (index in 0 until 6) {
                    val char = if (index < otpValue.length) otpValue[index].toString() else ""
                    val isFocused = index == otpValue.length

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) RoyalPurple else NehaGold,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurpleDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun LivePayoutFeedTicker(
    modifier: Modifier = Modifier
) {
    val samplePayouts = listOf(
        "Owner Sathsara D paid LKR 8,500 to Commercial Bank (Kandy)",
        "Affiliate Commission LKR 4,200 released to BOC (Galle)",
        "Brand Crew Task Payout LKR 3,200 paid to Sampath Bank (Colombo)",
        "Owner Sathsara D transferred LKR 12,000 to People's Bank (Kurunegala)",
        "Retail Escrow Commission LKR 5,000 approved & released"
    )

    var currentIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            currentIndex = (currentIndex + 1) % samplePayouts.size
        }
    }

    Surface(
        color = Color(0xFFF7F5FC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NehaGold),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF10B981),
                shape = CircleShape,
                modifier = Modifier.size(8.dp)
            ) {}

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "LIVE PAYOUT FEED:",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RoyalPurpleDark
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = samplePayouts[currentIndex],
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

