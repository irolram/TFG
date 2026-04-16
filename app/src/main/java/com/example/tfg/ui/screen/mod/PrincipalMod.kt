package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Rol
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.screen.user.MapaHuertosScreen
import com.example.tfg.ui.screen.user.MisHuertosScreen
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.TicketViewModel
import com.example.tfg.viewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalMod(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel,
    ticketViewModel: TicketViewModel,
    huertosViewModel: HuertosViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedItem by remember { mutableIntStateOf(0) }
    val miId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val apiService = remember { RetrofitClient.getApiService(context) }

    // --- OBSERVACIÓN DE ESTADOS ---
    val usuarioLogueado by usuarioViewModel.usuarioLogueado.collectAsState()
    val listaUsuarios by usuarioViewModel.listaUsuarios.collectAsState()
    val listaHuertos = huertosViewModel.uiState.value.lista

    val listaTickets by ticketViewModel.listaTickets.collectAsState()
    val isRefreshingTickets by ticketViewModel.isRefreshing.collectAsState()
    val isRefreshingUsuarios by usuarioViewModel.isRefreshing.collectAsState()

    // 🚩 SOLUCIÓN: Carga inmediata al entrar
    LaunchedEffect(Unit) {
        if (usuarioLogueado == null && miId.isNotEmpty()) {
            usuarioViewModel.cargarPerfilActual(miId)
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
                                1 -> "Mapa Global"
                                2 -> "Soporte Técnico"
                                3 -> "Comunidad"
                                else -> "Panel Mod"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf("Huertos", "Mapa", "Tickets", "Comunidad", "Perfil")
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.LocationOn,
                    Icons.Default.NotificationsActive,
                    Icons.Default.People,
                    Icons.Default.Person
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Solo permitimos crear huertos desde la pestaña 0
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Huerto")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedItem) {
                0 -> MisHuertosScreen(navController, huertosViewModel)
                1 -> MapaHuertosScreen(huertos = listaHuertos)
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
                    onLogout = onLogout
                )
            }
        }
    }
}