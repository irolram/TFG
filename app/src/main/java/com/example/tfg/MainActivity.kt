package com.example.tfg

import android.os.Bundle
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
import com.example.tfg.ui.screen.user.PantallaPrincipalUser
import com.example.tfg.ui.screen.user.VerdePrenda
import com.example.tfg.ui.screen.user.BuscarCultivoScreen // 🚩 Importamos la nueva pantalla
import com.example.tfg.ui.screens.RegisterScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.PlantaViewModel // 🚩 Importamos el nuevo ViewModel
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

                // ViewModels
                val huertosViewModel = viewModel<HuertosViewModel>()
                val plantasViewModel = viewModel<PlantaViewModel>()

                val tokenManager = remember { TokenManager(context) }

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "splash"
                ) {
                    // 1. Pantalla de carga
                    composable("splash") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VerdePrenda)
                        }
                    }

                    // 2. Login y Registro
                    composable("login") { LoginEcoDropScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // 3. Menús Principales
                    composable("main_menuUser") {
                        PantallaPrincipalUser(navController = navController, viewModel = huertosViewModel)
                    }
                    composable("main_menuAdmin") {
                        PantallaPrincipalAdmin(navController, huertosViewModel)
                    }

                    // 4. Gestión de Huertos
                    composable("crear_huerto") {
                        CrearHuertoScreen(navController = navController, viewModel = huertosViewModel)
                    }

                    composable(
                        route = "detalle_huerto/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""
                        DetalleHuertoScreen(
                            navController = navController,
                            viewModel = huertosViewModel,
                            huertoId = huertoId,
                            tokenManager = tokenManager
                        )
                    }

                    composable(
                        route = "buscar_cultivo/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""

                        BuscarCultivoScreen(
                            huertoId = huertoId,
                            onCultivoGuardado = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    auth.signOut()
                    tokenManager.clearAuth()
                }
                // Lógica de Autenticación Automática
                LaunchedEffect(currentUser) {

                    if (currentUser != null) {
                        try {
                            val loginRequest = LoginRequest(
                                userId = currentUser.uid,
                                email = currentUser.email ?: "",
                            )

                            val response = RetrofitClient.getApiService(context).loginConServidor(loginRequest)

                            if (response.isSuccessful) {
                                val authData = response.body()
                                if (authData != null) {
                                    tokenManager.saveToken(authData.accessToken, authData.userId)

                                    val destino = if (authData.rol == "ADMIN") "main_menuAdmin" else "main_menuUser"
                                    navController.navigate(destino) {
                                        popUpTo("splash") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } else {
                                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                            }
                        } catch (e: Exception) {
                            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                        }
                    }
                }
            }
        }
    }
}