package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElyzarethColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealHeaderMid,
    onPrimaryContainer = Color.White,
    secondary = TealAccentCyan,
    onSecondary = TextDarkSlate,
    secondaryContainer = AiBannerGradientStart,
    onSecondaryContainer = TextDarkSlate,
    tertiary = GoldVersionBadge,
    onTertiary = TextDarkSlate,
    background = DashboardBackground,
    onBackground = TextDarkSlate,
    surface = CardSurfaceWhite,
    onSurface = TextDarkSlate,
    surfaceVariant = Color(0xFFDEE8EE),
    onSurfaceVariant = TextMutedSlate,
    outline = CardBorderLight
)

@Composable
fun ElyzarethTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElyzarethColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ElyzarethTheme(content = content)
}
