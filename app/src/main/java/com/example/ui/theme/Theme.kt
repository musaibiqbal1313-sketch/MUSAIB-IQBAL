package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KisanGreenDarkPrimary,
    onPrimary = Color(0xFF003825),
    primaryContainer = KisanGreenDarkContainer,
    onPrimaryContainer = Color(0xFF90F5C7),
    secondary = HarvestGoldDark,
    onSecondary = Color(0xFF452B00),
    background = FarmBackgroundDark,
    surface = FarmSurfaceDark,
    surfaceVariant = FarmSurfaceVariantDark,
    onBackground = Color(0xFFE2E4DE),
    onSurface = Color(0xFFE2E4DE)
)

private val LightColorScheme = lightColorScheme(
    primary = KisanGreenPrimary,
    onPrimary = KisanGreenOnPrimary,
    primaryContainer = KisanGreenContainer,
    onPrimaryContainer = KisanGreenOnContainer,
    secondary = HarvestGold,
    onSecondary = Color.White,
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = HarvestGoldOnContainer,
    tertiary = CanalBlue,
    onTertiary = Color.White,
    tertiaryContainer = CanalBlueContainer,
    onTertiaryContainer = CanalBlueOnContainer,
    background = FarmBackgroundLight,
    surface = FarmSurfaceLight,
    surfaceVariant = FarmSurfaceVariantLight,
    outline = FarmOutlineLight,
    onBackground = Color(0xFF191C19),
    onSurface = Color(0xFF191C19),
    onSurfaceVariant = Color(0xFF424941)
)

@Composable
fun KisanWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand agricultural theme consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = KisanWeatherTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
