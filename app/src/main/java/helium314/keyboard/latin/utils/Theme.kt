// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// grouped list style: grey page background, cards in surfaceContainerLowest, accent in the beBoard orange
private val lightColors = lightColorScheme(
    primary = Color(0xFFE8690B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE6D0),
    onPrimaryContainer = Color(0xFF4A1F00),
    secondary = Color(0xFF6E6E73),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color(0xFF1C1C1E),
    tertiary = Color(0xFF34C759),
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color.Black,
    surface = Color(0xFFF2F2F7),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF6E6E73),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFE5E5EA),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFD8D8DC),
    error = Color(0xFFFF3B30),
    onError = Color.White,
)

private val darkColors = darkColorScheme(
    primary = Color(0xFFFF9F43),
    onPrimary = Color(0xFF1C1C1E),
    primaryContainer = Color(0xFF5A2A00),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFF98989D),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3A3A3C),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF30D158),
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF98989D),
    surfaceContainerLowest = Color(0xFF1C1C1E),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3A3A3C),
    outline = Color(0xFF38383A),
    outlineVariant = Color(0xFF38383A),
    error = Color(0xFFFF453A),
    onError = Color.White,
)

@Composable
fun Theme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val material3 = Typography()
    MaterialTheme(
        colorScheme = if (dark) darkColors else lightColors,
        typography = Typography(
            // large page titles
            headlineMedium = material3.headlineMedium.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
            // collapsed page titles and dialog titles
            titleLarge = material3.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
            titleMedium = material3.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            titleSmall = material3.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = material3.bodyLarge.copy(fontSize = 17.sp, lineHeight = 22.sp),
            bodyMedium = material3.bodyMedium.copy(fontSize = 14.sp, lineHeight = 19.sp),
            bodySmall = material3.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp),
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(22.dp),
        ),
        content = content
    )
}

const val previewDark = true
