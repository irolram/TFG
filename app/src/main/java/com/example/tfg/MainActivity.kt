package com.example.tfg

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.screen.LoginEcoDropScreen
import com.example.tfg.ui.screen.admin.PantallaPrincipalAdmin
import com.example.tfg.ui.screen.user.PantallaPrincipalUser
import com.example.tfg.ui.screens.RegisterScreen
import com.example.tfg.ui.theme.TFGTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.ui.screen.user.CrearHuertoScreen
import com.example.tfg.ui.screen.user.DetalleHuertoScreen
import com.example.tfg.ui.screen.user.MisHuertosScreen
import com.example.tfg.ui.screen.user.VerdePrenda
import com.example.tfg.viewModel.HuertosViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TFGTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                val viewModel = viewModel<HuertosViewModel>()

                // Instanciamos el TokenManager para guardar el JWT
                val tokenManager = remember { TokenManager(context) }

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "splash"
                ) {
                    // Pantalla de carga mientras sincronizamos con la API
                    composable("splash") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VerdePrenda) // Muestra la ruedita verde
                        }
                    }

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
                    composable("crear_huerto") {
                        CrearHuertoScreen(navController, viewModel)
                    }

                    composable(
                        route = "detalle_huerto/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->

                        // 🔌 ¡Cazamos el ID que nos ha pasado la tarjeta!
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""

                        // Y abrimos la nueva pantalla pasándole el ID
                        DetalleHuertoScreen(navController = navController, viewModel = viewModel, huertoId = huertoId)
                    }
                }

                // Lógica de Autenticación con tu API de Spring Boot
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        try {
                            val loginRequest = LoginRequest(
                                userId = currentUser.uid,
                                email = currentUser.email ?: "",
                            )

                            // 1. Pedimos el Token a nuestra API (Railway) usando el nuevo método con context
                            val response = RetrofitClient.getApiService(context).loginConServidor(loginRequest)

                            if (response.isSuccessful) {
                                val authData = response.body() // Aquí se crea el objeto con los datos del servidor

                                if (authData != null) {
                                    // 2. Ahora sí: le pasamos los datos que vienen dentro de authData
                                    tokenManager.saveToken(authData.accessToken, authData.userId)

                                    Log.d("API_AUTH", "¡Éxito! Token guardado. Rol: ${authData.rol}")

                                    // 3. Redirigimos según el ROL
                                    if (authData.rol == "ADMIN") {
                                        navController.navigate("main_menuAdmin") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("main_menuUser") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            } else {
                                Log.e("API_AUTH", "Error en el servidor: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("API_AUTH", "Fallo de red: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}