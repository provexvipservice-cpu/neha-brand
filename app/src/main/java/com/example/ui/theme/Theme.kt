package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NehaGold,
    onPrimary = RoyalPurpleDark,
    primaryContainer = RoyalPurple,
    onPrimaryContainer = Color.White,
    secondary = NehaGoldLight,
    onSecondary = Color.Black,
    secondaryContainer = RoyalPurpleLight,
    onSecondaryContainer = Color.White,
    background = Color(0xFF13111C),
    onBackground = Color(0xFFF0EFF5),
    surface = Color(0xFF1E1A2B),
    onSurface = Color(0xFFF0EFF5),
    outline = NehaGoldDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    primaryContainer = RoyalPurpleContainer,
    onPrimaryContainer = OnRoyalPurpleContainer,
    secondary = NehaGold,
    onSecondary = Color.White,
    secondaryContainer = NehaGoldContainer,
    onSecondaryContainer = NehaGoldDark,
    background = LuxuryBackground,
    onBackground = TextPrimary,
    surface = LuxurySurface,
    onSurface = TextPrimary,
    outline = NehaGold,
  )

@Composable
fun NehaBrandTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NehaBrandTheme(darkTheme = darkTheme, content = content)
}
