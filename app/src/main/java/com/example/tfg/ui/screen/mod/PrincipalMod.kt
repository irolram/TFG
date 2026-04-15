package com.example.tfg.ui.screen.mod
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.Rol
import com.example.tfg.ui.screen.admin.VerdeAdmin
import com.example.tfg.ui.screen.user.MapaHuertosScreen
import com.example.tfg.ui.screen.user.MisHuertosScreen
import com.example.tfg.ui.theme.VerdeEco
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.TicketViewModel
import com.example.tfg.viewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalMod(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel,
    ticketViewModel: TicketViewModel,
    huertosViewModel: HuertosViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }
    val miId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val apiService = remember { com.example.tfg.data.network.RetrofitClient.getApiService(context) }

    // --- ESTADOS DE LOS VIEWMODELS ---
    val usuarioLogueado by usuarioViewModel.usuarioLogueado.collectAsState()
    val listaUsuarios by usuarioViewModel.listaUsuarios.collectAsState()
    val isRefreshingUsuarios by usuarioViewModel.isRefreshing.collectAsState()
    val listaTickets by ticketViewModel.listaTickets.collectAsState()
    val isRefreshingTickets by ticketViewModel.isRefreshing.collectAsState()

    // 🚩 1. LÓGICA DE CARGA: Aquí solo llamamos a funciones del ViewModel
    LaunchedEffect(selectedItem) {
        when (selectedItem) {
            0, 1 -> huertosViewModel.obtenerTodosLosHuertos(apiService) // Carga datos para lista y mapa
            2 -> ticketViewModel.listarTickets()
            3 -> usuarioViewModel.listarUsuarios()
            4 -> if (usuarioLogueado == null) usuarioViewModel.cargarPerfilActual(miId)
        }
    }

    val performLogout = {
        FirebaseAuth.getInstance().signOut()

        usuarioViewModel.limpiarSesion()

        scope.launch {
            TokenManager(context).clearAuth()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }


    Scaffold(
        topBar = {
            if (selectedItem != 4) {
                TopAppBar(
                    title = {
                        Text(
                            text = when(selectedItem) {
                                0 -> "Mis Huertos"
                                1 -> "Mapa de Huertos"
                                2 -> "Gestión de Soporte"
                                3 -> "Moderación de Comunidad"
                                else -> "Eco Drop"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if(selectedItem <= 1) VerdeEco else Color(0xFF00796B)
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("Huertos", "Mapa", "Tickets", "Comunidad", "Perfil")
                val icons = listOf(
                    Icons.Filled.Home,
                    Icons.Filled.LocationOn,
                    Icons.Filled.NotificationsActive,
                    Icons.Filled.People,
                    Icons.Filled.Person
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if(index <= 1) VerdeEco else VerdeAdmin,
                            indicatorColor = Color(0xFFE8F5E9)
                        )
                    )
                }
            }
        },

        floatingActionButton = {
            // Solo mostramos el botón si estamos en la pestaña "Huertos" (índice 0)
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = VerdeEco,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Huerto")
                }
            }
        }


    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val huertos by huertosViewModel.huertos

            when (selectedItem) {
                // 🚩 2. PINTAR LAS PANTALLAS: Aquí es donde llamas a los Composables
                0 -> MisHuertosScreen(navController, huertosViewModel)
                1 -> MapaHuertosScreen(huertos = huertos)
                2 -> GestionTicketsScreen(
                    listaTickets = listaTickets,
                    isRefreshing = isRefreshingTickets,
                    onRefresh = { ticketViewModel.listarTickets() },
                    onResolverTicket = { id -> ticketViewModel.resolverTicket(id) }
                )
                3 -> GestionUsuariosModScreen(
                    listaUsuarios = listaUsuarios,
                    isRefreshing = isRefreshingUsuarios,
                    onRefresh = { usuarioViewModel.listarUsuarios() },
                    onPromocionarAMod = { id -> usuarioViewModel.actualizarRol(id, Rol.MOD) }
                )
                4 -> PerfilModScreen(
                    usuario = usuarioLogueado,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = { performLogout() }
                )
            }
        }
    }
}