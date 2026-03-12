package com.example.tfg

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.screen.LoginEcoDropScreen
import com.example.tfg.ui.screen.PantallaPrincipalAdmin
import com.example.tfg.ui.screen.PantallaPrincipalUser
import com.example.tfg.ui.screens.RegisterScreen
import com.example.tfg.ui.theme.TFGTheme
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TFGTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginEcoDropScreen(navController)
                    }
                    composable("register") {
                        RegisterScreen(navController)
                    }

                    composable("main_menuUser") {
                        PantallaPrincipalUser(navController)
                    }
                    composable("main_menuAdmin") {
                        PantallaPrincipalAdmin(navController)
                    }

                }

                // 2. Lógica de sincronización con MySQL
                // Esto solo ocurre si detectamos al usuario al arrancar
                if (currentUser != null) {
                    val miUsuarioReal = Usuario(
                        id = currentUser.uid,
                        nombre = currentUser.displayName ?: "Usuario Nuevo",
                        apellidos = "",
                        email = currentUser.email ?: "",
                        rol = Rol.USUARIO
                    )

                    LaunchedEffect(currentUser.uid) {
                        try {
                            val response = RetrofitClient.instance.registrarUsuario(miUsuarioReal)
                            if (response.isSuccessful) {
                                Log.d("RETROFIT", "¡Usuario real ${miUsuarioReal.nombre} sincronizado!")
                            }
                        } catch (e: Exception) {
                            Log.e("RETROFIT", "Error de red: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}