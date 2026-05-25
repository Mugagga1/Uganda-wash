package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SparkleGold,
    secondary = LiquidCyan,
    tertiary = AccentYellow,
    background = DeepObsidian,
    surface = CardDark,
    onPrimary = DeepObsidian,
    onSecondary = DeepObsidian,
    onTertiary = DeepObsidian,
    onBackground = WhiteClean,
    onSurface = WhiteClean,
    error = AccentRed,
    surfaceVariant = DividerDark,
    onSurfaceVariant = SlateGray
)

private val LightColorScheme = lightColorScheme(
    primary = SparkleGold,
    secondary = LiquidCyan,
    tertiary = AccentYellow,
    background = DeepObsidian, // Maintain premium look even in light theme, or close to it
    surface = CardDark,
    onPrimary = DeepObsidian,
    onSecondary = DeepObsidian,
    onBackground = WhiteClean,
    onSurface = WhiteClean,
    error = AccentRed,
    surfaceVariant = DividerDark,
    onSurfaceVariant = SlateGray
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark mode for beautiful gloss contrast
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
