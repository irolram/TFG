package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.navigation.NavHostController
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.UsuarioViewModel


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
    // 🚩 Observamos el usuario directamente del ViewModel, sin re-peticiones a la API
    val usuario by usuariosViewModel.usuarioLogueado.collectAsState()

    val items = listOf("Mis Huertos", "Mapa", "Perfil")
    val icons = listOf(Icons.Filled.Eco, Icons.Filled.Map, Icons.Filled.Person)

    // Usamos el esquema de colores del tema
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorOnPrimario = MaterialTheme.colorScheme.onPrimary
    val colorSuperficie = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorPrimario, // 🚩 Ahora es dinámico (Admin/User/Mod)
                    titleContentColor = colorOnPrimario
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = colorSuperficie) {
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
            // Solo mostramos el botón de añadir si estamos en "Mis Huertos"
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = colorPrimario,
                    contentColor = colorOnPrimario
                ) { Icon(Icons.Filled.Add, "Añadir") }
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
                1 -> MapaHuertosScreen(huertosViewModel.huertos.value)
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
