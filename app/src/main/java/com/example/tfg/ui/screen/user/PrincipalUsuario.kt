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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.viewModel.HuertosViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

val VerdePrenda = Color(0xFF4CAF50)
val VerdeFondo = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalUser(navController: NavHostController, viewModel: HuertosViewModel) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Mis Huertos","Mapa","Perfil")
    val icons = listOf(Icons.Filled.Eco, Icons.Filled.Map, Icons.Filled.Person)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop - Panel", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrenda),
                actions = {
                    IconButton(
                        // Sale de la sesión y borra el token
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            scope.launch {
                                val tokenManager = TokenManager(context)
                                tokenManager.clearAuth()
                                viewModel.limpiarDatos()
                            }
                            // Se dirige al login
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerdePrenda,
                            selectedTextColor = VerdePrenda,
                            indicatorColor = VerdeFondo
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Solo mostramos el botón de añadir si estamos en la pestaña de "Mis Huertos"
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = VerdePrenda,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir Huerto")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VerdeFondo)
        ) {
            when (selectedItem) {
                0 -> {
                    MisHuertosScreen(navController = navController, viewModel = viewModel)
                }
                1 -> {
                    val huertosList = viewModel.huertos.value
                    MapaHuertosScreen(huertos = huertosList)
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Configuración de Perfil en construcción...", fontSize = 18.sp, color = VerdePrenda)
                    }
                }
            }
        }
    }
}