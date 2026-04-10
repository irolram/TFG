package com.example.tfg

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.RiegoWorker
import com.example.tfg.ui.screen.LoginEcoDropScreen
import com.example.tfg.ui.screen.admin.PantallaPrincipalAdmin
import com.example.tfg.ui.screen.mod.PantallaPrincipalMod
import com.example.tfg.ui.screen.user.CrearHuertoScreen
import com.example.tfg.ui.screen.user.DetalleHuertoScreen
import com.example.tfg.ui.screen.user.PantallaPrincipalUser
import com.example.tfg.ui.screen.user.BuscarCultivoScreen
import com.example.tfg.ui.screen.user.DetalleCultivoScreen
import com.example.tfg.ui.screen.user.verdeEco
import com.example.tfg.ui.screens.RegisterScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.PlantaViewModel
import com.example.tfg.viewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        // --- GESTIÓN DE PERMISOS NOTIFICACIONES ---
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean -> }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // --- CONFIGURACIÓN DE WORKER (RIEGO) ---
        val request = PeriodicWorkRequestBuilder<RiegoWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CheckRiego",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            var selectedTab by rememberSaveable { mutableIntStateOf(0) }

            val scope = rememberCoroutineScope()
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            TFGTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val tokenManager = remember { TokenManager(context) }
                val apiService = remember { RetrofitClient.getApiService(context) }

                // --- FACTORIES PARA VIEWMODELS CON PARÁMETROS ---
                val plantsFactory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        PlantaViewModel(apiService) as T
                }

                val usersFactory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        UsuarioViewModel(apiService) as T
                }

                val plantasViewModel: PlantaViewModel = viewModel(factory = plantsFactory)
                val huertosViewModel: HuertosViewModel = viewModel()
                val usuariosViewModel: UsuarioViewModel = viewModel(factory = usersFactory)

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "splash"
                ) {
                    // Pantalla de carga inicial
                    composable("splash") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = verdeEco)
                        }
                    }

                    composable("login") { LoginEcoDropScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // --- 1. RUTA USUARIO ESTÁNDAR ---
                    composable("main_menuUser") {
                        PantallaPrincipalUser(
                            navController = navController,
                            viewModel = huertosViewModel,
                            isDarkMode = darkTheme,
                            onDarkModeChange = { darkTheme = it },
                            selectedItem = selectedTab,
                            onTabChange = { selectedTab = it },
                            onLogout = { cerrarSesionCompleta(auth, tokenManager, huertosViewModel, navController, scope) }
                        )
                    }

                    // --- 2. RUTA ADMINISTRADOR ---
                    composable("main_menuAdmin") {
                        PantallaPrincipalAdmin(
                            navController = navController,
                            viewModel = usuariosViewModel,
                            isDarkMode = darkTheme,
                            onDarkModeChange = { darkTheme = it }
                        )
                    }

                    // --- 3. RUTA MODERADOR ---
                    composable("main_menuMod") {
                        PantallaPrincipalMod(
                            navController = navController,
                            viewModel = usuariosViewModel,
                            isDarkMode = darkTheme,
                            onDarkModeChange = { darkTheme = it }
                        )
                    }

                    // --- OTRAS PANTALLAS (GESTIÓN HUERTOS) ---
                    composable("crear_huerto") {
                        CrearHuertoScreen(navController, huertosViewModel)
                    }

                    composable(
                        route = "detalle_huerto/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""
                        DetalleHuertoScreen(navController, huertosViewModel, tokenManager,huertoId)
                    }

                    composable(
                        route = "buscar_cultivo/{huertoId}",
                        arguments = listOf(navArgument("huertoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val huertoId = backStackEntry.arguments?.getString("huertoId") ?: ""
                        BuscarCultivoScreen(
                            huertoId = huertoId,
                            viewModel = plantasViewModel,
                            onCultivoGuardado = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "detalle_planta/{cultivoId}",
                        arguments = listOf(navArgument("cultivoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val cultivoId = backStackEntry.arguments?.getString("cultivoId") ?: ""
                        DetalleCultivoScreen(navController, huertosViewModel, cultivoId)
                    }
                }

                // --- LÓGICA DE REDIRECCIÓN POR ROL (SPLASH) ---
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        try {
                            val loginRequest = LoginRequest(
                                userId = currentUser.uid,
                                email = currentUser.email ?: "",
                            )

                            val response = apiService.loginConServidor(loginRequest)

                            if (response.isSuccessful) {
                                val authData = response.body()
                                if (authData != null) {
                                    tokenManager.saveToken(authData.accessToken, authData.userId)

                                    val destino = when (authData.rol) {
                                        "ADMIN" -> "main_menuAdmin"
                                        "MOD" -> "main_menuMod"
                                        else -> "main_menuUser"
                                    }

                                    navController.navigate(destino) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                logoutYLogin(auth, tokenManager, navController)
                            }
                        } catch (e: Exception) {
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }
                    } else if (navController.currentDestination?.route == "splash") {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                }
            }
        }
    }

    private suspend fun logoutYLogin(auth: FirebaseAuth, tm: TokenManager, nv: NavHostController) {
        auth.signOut()
        tm.clearAuth()
        nv.navigate("login") { popUpTo(0) { inclusive = true } }
    }

    private fun cerrarSesionCompleta(
        auth: FirebaseAuth,
        tm: TokenManager,
        vm: HuertosViewModel,
        nv: NavHostController,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        auth.signOut()
        scope.launch {
            tm.clearAuth()
            vm.limpiarDatos()
            nv.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }
}