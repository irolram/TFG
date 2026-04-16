package com.example.tfg.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.tfg.data.model.Rol

// --- 1. DEFINICIÓN DE COLORES POR RANGO ---

// USER: Verde Naturaleza (Fresco y accesible)
private val UserPrimary = Color(0xFF4CAF50)
private val UserContainer = Color(0xFFC8E6C9)

// MODERADOR: Verde Teal (Profesional y equilibrado)
private val ModPrimary = Color(0xFF00796B)
private val ModContainer = Color(0xFFB2DFDB)

// ADMIN: Verde Bosque (Autoridad y profundidad)
private val AdminPrimary = Color(0xFF1B5E20)
private val AdminContainer = Color(0xFFA5D6A7)

// --- 2. GENERADOR DE SCHEMES ---

@Composable
fun TFGTheme(
    rol: Rol?,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val rangoActual = rol ?: Rol.USER

    val colorScheme = when (rangoActual) {
        Rol.ADMIN -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF81C784),
                onPrimary = Color.Black,
                primaryContainer = AdminPrimary,
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E)
            )
        } else {
            lightColorScheme(
                primary = AdminPrimary,
                onPrimary = Color.White,
                primaryContainer = AdminContainer,
                background = Color(0xFFF8F9FA)
            )
        }

        Rol.MOD -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF4DB6AC),
                onPrimary = Color.Black,
                primaryContainer = ModPrimary,
                background = Color(0xFF121212)
            )
        } else {
            lightColorScheme(
                primary = ModPrimary,
                onPrimary = Color.White,
                primaryContainer = ModContainer,
                background = Color(0xFFF0F4F4)
            )
        }

        Rol.USER -> if (darkTheme) {
            darkColorScheme(
                primary = UserPrimary,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF2E7D32),
                background = Color(0xFF121212)
            )
        } else {
            lightColorScheme(
                primary = UserPrimary,
                onPrimary = Color.White,
                primaryContainer = UserContainer,
                background = Color(0xFFF4F7F6),
                surface = Color.White
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}