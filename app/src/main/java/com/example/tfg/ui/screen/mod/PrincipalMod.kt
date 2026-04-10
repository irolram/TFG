package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.tfg.data.model.Rol
import com.example.tfg.ui.screen.admin.VerdeAdmin
import com.example.tfg.viewModel.UsuarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalMod(
    navController: NavHostController,
    viewModel: UsuarioViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    // 🚩 1. Conexión con el ViewModel y Firebase
    val miId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val listaUsuarios by viewModel.listaUsuarios.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // 🚩 2. Lógica de carga automática al cambiar de pestaña
    LaunchedEffect(selectedItem) {
        when (selectedItem) {
            1 -> viewModel.listarUsuarios() // Carga la lista para moderar
            2 -> if (usuarioLogueado == null) viewModel.cargarPerfilActual(miId) // Carga perfil
        }
    }

    // 🚩 3. Función de Logout centralizada
    val performLogout = {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        scope.launch {
            com.example.tfg.data.TokenManager(context).clearAuth()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            // Ocultamos la barra en el perfil para que luzca mejor tu diseño con franja verde
            if (selectedItem != 2) {
                TopAppBar(
                    title = { Text("Eco Drop - Moderación", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.tfg.ui.screen.user.verdeEco)
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("Dashboard", "Comunidad", "Perfil")
                val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.People, Icons.Filled.Person)

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.example.tfg.ui.screen.admin.VerdeAdmin,
                            indicatorColor = Color(0xFFE8F5E9)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedItem) {
                0 -> ResumenActividadContent() // Una sub-pantalla con estadísticas rápidas
                1 -> GestionUsuariosModScreen(
                    listaUsuarios = listaUsuarios,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.listarUsuarios() },
                    onPromocionar = { id -> viewModel.actualizarRol(id, Rol.MOD) }
                )
                2 -> PerfilModScreen(
                    usuario = usuarioLogueado,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = { performLogout() }
                )
            }
        }
    }
}