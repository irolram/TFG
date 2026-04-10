package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Rol
import com.example.tfg.ui.screen.admin.VerdeAdmin
import com.example.tfg.viewModel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalMod(
    navController: NavHostController,
    viewModel: UsuarioViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    val listaUsuarios by viewModel.listaUsuarios.collectAsState()
    val items = listOf("Dashboard", "Comunidad", "Ajustes")
    val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.People, Icons.Filled.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop - Moderación", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeAdmin) // Verde normal
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItem) {
                0 -> Text("Resumen de actividad de usuarios")
                1 -> GestionUsuariosModScreen(
                    listaUsuarios = listaUsuarios,
                    onPromocionar = { id -> viewModel.actualizarRol(id, Rol.MOD) }
                )
                2 -> Text("Ajustes de perfil de Moderador")
            }
        }
    }
}