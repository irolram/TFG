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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.screen.LoginEcoDropScreen
import com.example.tfg.ui.screen.admin.PantallaPrincipalAdmin
import com.example.tfg.ui.screen.user.CrearHuertoScreen
import com.example.tfg.ui.screen.user.DetalleHuertoScreen
import com.example.tfg.ui.screen.user.MisHuertosScreen
import com.example.tfg.ui.screen.user.PantallaPrincipalUser
import com.example.tfg.ui.screen.user.VerdePrenda
import com.example.tfg.ui.screens.RegisterScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.viewModel.HuertosViewModel
import com.google.firebase.auth.FirebaseAuth

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

                // Instanciamos el ViewModel de los huertos (que se compartirá entre pantallas)
                val viewModel = viewModel<HuertosViewModel>()

                // Instanciamos el TokenManager para guardar el JWT
                val tokenManager = remember { TokenManager(context) }

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "splash"
                ) {
                    // 1. Pantalla de carga mientras sincronizamos con la API
                    composable("splash") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VerdePrenda)
                        }
                    }

                    // 2. Pantalla de Login
                    composable("login") {
                        LoginEcoDropScreen(navController)
                    }

                    // 3. Pantalla de Registro
                    composable("register") {
                        RegisterScreen(navController)
                    }

                    // 4. Pantalla Principal Usuario
                    composable("main_menuUser") {
                        PantallaPrincipalUser(navController = navController, viewModel = viewModel)
                    }

                    // 5. Pantalla Principal Admin
                    composable("main_menuAdmin") {
                        PantallaPrincipalAdmin(navController, viewModel)
                    }

                    // 6. Pantalla Crear Huerto
                    composable("crear_huerto") {
                        CrearHuertoScreen(navController = navController, viewModel = viewModel)
                    }

                    // 7. Pantalla Detalle Huerto (Con paso de parámetros)
                    composable(
                        route = "detalle_huerto/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""
                        DetalleHuertoScreen(
                            navController = navController,
                            viewModel = viewModel,
                            huertoId = huertoId
                        )
                    }
                }

                // Lógica de Autenticación Automática (Si ya hay sesión en Firebase)
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        try {
                            val loginRequest = LoginRequest(
                                userId = currentUser.uid,
                                email = currentUser.email ?: "",
                            )

                            // Pedimos el Token a nuestra API (Railway)
                            val response = RetrofitClient.getApiService(context).loginConServidor(loginRequest)

                            if (response.isSuccessful) {
                                val authData = response.body()

                                if (authData != null) {
                                    // Guardamos el token
                                    tokenManager.saveToken(authData.accessToken, authData.userId)
                                    Log.d("API_AUTH", "¡Éxito! Token guardado. Rol: ${authData.rol}")

                                    // Redirigimos según el ROL
                                    if (authData.rol == "ADMIN") {
                                        navController.navigate("main_menuAdmin") {
                                            popUpTo("splash") { inclusive = true }
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("main_menuUser") {
                                            popUpTo("splash") { inclusive = true }
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            } else {
                                Log.e("API_AUTH", "Error en el servidor: ${response.code()} - ${response.errorBody()?.string()}")
                                // Si falla el backend, mandamos al login por seguridad
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("API_AUTH", "Fallo de red: ${e.message}")
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}