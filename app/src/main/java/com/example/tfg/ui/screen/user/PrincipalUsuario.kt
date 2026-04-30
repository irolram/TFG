package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalUser(
    navController: NavHostController,
    huertosViewModel: HuertosViewModel,
    usuariosViewModel: UsuarioViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    selectedItem: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }
    val miId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Observación de estados
    val state by huertosViewModel.uiState
    val usuario by usuariosViewModel.usuarioLogueado.collectAsState()

    val items = listOf("Mis Huertos", "Mapa", "Perfil")
    val icons = listOf(Icons.Default.Eco, Icons.Default.Map, Icons.Default.Person)

    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorOnPrimario = MaterialTheme.colorScheme.onPrimary

    // 🚩 CARGA PROACTIVA: Perfil y Huertos (para tener pings en el mapa)
    LaunchedEffect(Unit) {
        if (miId.isNotEmpty()) {
            if (usuario == null) usuariosViewModel.cargarPerfilActual(miId)
            huertosViewModel.obtenerTodosLosHuertos(apiService)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = when(selectedItem) {
                        0 -> "Mis Huertos"
                        1 -> "Mapa de Huertos"
                        2 -> "Mi Perfil"
                        else -> "Eco Drop"
                    },
                    fontWeight = FontWeight.ExtraBold
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorPrimario,
                    titleContentColor = colorOnPrimario
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { onTabChange(index) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorPrimario,
                            selectedTextColor = colorPrimario,
                            indicatorColor = colorPrimario.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = colorPrimario,
                    contentColor = colorOnPrimario,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir Huerto")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> MisHuertosScreen(navController, huertosViewModel)
                1 -> MapaHuertosScreen(huertos = state.lista)
                2 -> PerfilScreen(
                    usuario = usuario,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = onLogout,
                    onNavigateToSupport = { navController.navigate("enviar_ticket") }
                )
            }
        }
    }
}