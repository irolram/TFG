package com.example.tfg.ui.screen.mod

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

@RequiresApi(Build.VERSION_CODES.O)
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
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    val miId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val apiService = remember { RetrofitClient.getApiService(context) }

    val usuarioLogueado by usuarioViewModel.usuarioLogueado.collectAsState()
    val listaUsuarios by usuarioViewModel.listaUsuarios.collectAsState()
    val stateHuertos by huertosViewModel.uiState
    val listaTickets by ticketViewModel.listaTickets.collectAsState()
    val isRefreshingTickets by ticketViewModel.isRefreshing.collectAsState()
    val isRefreshingUsuarios by usuarioViewModel.isRefreshing.collectAsState()

    val items = listOf("Huertos", "Mapa", "Tickets", "Comunidad", "Perfil")
    val icons = listOf(Icons.Default.Home, Icons.Default.LocationOn, Icons.Default.NotificationsActive, Icons.Default.People, Icons.Default.Person)

    val tituloPantalla = when (selectedItem) {
        0 -> "Mis Huertos"
        1 -> "Mapa Global"
        2 -> "Soporte Técnico"
        3 -> "Comunidad"
        4 -> "Mi Perfil"
        else -> "Panel Mod"
    }

    LaunchedEffect(Unit) {
        if (usuarioLogueado == null && miId.isNotEmpty()) {
            usuarioViewModel.cargarPerfilActual(miId)
        }
        huertosViewModel.obtenerTodosLosHuertos(apiService)
    }

    LaunchedEffect(selectedItem) {
        when (selectedItem) {
            2 -> if (listaTickets.isEmpty()) ticketViewModel.listarTickets()
            3 -> if (listaUsuarios.isEmpty()) usuarioViewModel.listarUsuarios()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = tituloPantalla, fontWeight = FontWeight.ExtraBold, maxLines = 1) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // ALTURA ELIMINADA: Deja que Material3 calcule la altura según el sistema
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(text = item, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(onClick = { navController.navigate("crear_huerto") }) {
                    Icon(Icons.Default.Add, "Nuevo")
                }
            }
        }
    ) { paddingValues ->
        // BOX: Es vital aplicar el padding del Scaffold aquí para que el contenido no quede oculto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedItem) {
                0 -> MisHuertosScreen(navController, huertosViewModel)
                1 -> MapaHuertosScreen(huertos = stateHuertos.lista)
                2 -> GestionTicketsScreen(listaTickets, isRefreshingTickets, { ticketViewModel.listarTickets() }, { id -> ticketViewModel.resolverTicket(id) })
                3 -> GestionUsuariosModScreen(listaUsuarios, isRefreshingUsuarios, { usuarioViewModel.listarUsuarios() }, { id -> usuarioViewModel.actualizarRol(id, Rol.MOD) })
                4 -> PerfilModScreen(usuarioLogueado, isDarkMode, onDarkModeChange, onLogout)
            }
        }
    }
}