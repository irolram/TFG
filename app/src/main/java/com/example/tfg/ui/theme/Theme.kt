package com.example.tfg.ui.theme

import android.app.Activity
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
import com.example.tfg.data.model.Rol

// ui/theme/Color.kt
val VerdeEco = Color(0xFF4CAF50)
val VerdeEcoLight = Color(0xFFE8F5E9)
val TealMod = Color(0xFF00796B)
val TealModLight = Color(0xFFB2DFDB)
val GrisAdmin = Color(0xFF263238)
val VerdeAdminAccento = Color(0xFF81C784)

// Colores de estado (Comunes)
val TicketError = Color(0xFFD32F2F)
val TicketSugerencia = Color(0xFF388E3C)
val TicketOtro = Color(0xFFF9A825)

// ui/theme/Theme.kt

@Composable
fun EcoDropTheme(
    rol: Rol?, // Pasamos el rol que tenemos en el ViewModel
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (rol) {
        Rol.ADMIN -> darkColorScheme(
            primary = VerdeAdminAccento,
            surface = GrisAdmin,
            background = Color(0xFF121212)
        )
        Rol.MOD -> lightColorScheme(
            primary = TealMod,
            secondary = TealModLight,
            surface = Color.White
        )
        else -> lightColorScheme( // Por defecto el de USER
            primary = VerdeEco,
            secondary = VerdeEcoLight,
            background = Color(0xFFF4F7F6),
            surface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TFGTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    rol: Rol?,
    dynamicColor: Boolean = true,
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