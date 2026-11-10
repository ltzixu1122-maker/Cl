package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CouponLoColorScheme = darkColorScheme(
    primary = CouponRed,
    onPrimary = PureWhite,
    primaryContainer = CouponRedDark,
    onPrimaryContainer = PureWhite,
    secondary = CouponGreen,
    onSecondary = PureWhite,
    tertiary = CouponAmber,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = PureWhite,
    surface = DarkPanel,
    onSurface = PureWhite,
    surfaceVariant = DarkPanel2,
    onSurfaceVariant = DarkMuted,
    outline = DarkLine
)

@Composable
fun CouponLoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CouponLoColorScheme,
        typography = Typography,
        content = content
    )
}
