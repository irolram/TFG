package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // 🔌 Añadido para el contexto
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.ui.screen.user.VerdeFondo
import com.example.tfg.ui.screen.user.VerdePrenda
import com.example.tfg.viewModel.HuertosViewModel // 🔌 Añadido para limpiar los datos
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Colores Admin (usamos el mismo verde pero con toques más "serios")
val VerdeAdmin = Color(0xFF388E3C)
val GrisFondo = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalAdmin(
    navController: NavHostController,
    viewModel: HuertosViewModel // 🔌 Añadimos el ViewModel aquí
) {
    // 🔌 AÑADIDOS: El contexto y el scope para que el botón de salir no dé error
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado para la barra de navegación
    var selectedItem by remember { mutableIntStateOf(0) } // Optimizado a IntState
    val items = listOf("Dashboard", "Usuarios", "Mapa", "Ajustes")
    val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.People, Icons.Filled.Map, Icons.Filled.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop - Panel Admin", color = Color.White) }, // Le he añadido "Admin" para que lo distingas bien
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrenda),
                actions = {
                    IconButton(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            scope.launch {
                                val tokenManager = TokenManager(context)
                                tokenManager.clearAuth()
                                viewModel.limpiarDatos()

                                // 🔌 Navegamos DESPUÉS de limpiar los datos para evitar fallos
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
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
        }, // 🔌 CORREGIDO: Sobraba una llave aquí que rompía el código
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerdeAdmin,
                            selectedTextColor = VerdeAdmin,
                            indicatorColor = VerdeFondo
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Contenido principal con fondo gris claro para diferenciar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GrisFondo)
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen del Sistema",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeAdmin,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Fila de tarjetas de estadísticas rápidas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadisticaCard(titulo = "Usuarios Totales", valor = "1,234", icono = Icons.Filled.People, modifier = Modifier.weight(1f))
                EstadisticaCard(titulo = "Huertos Activos", valor = "567", icono = Icons.Filled.Eco, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de actividad reciente
            Text(text = "Actividad Reciente", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(3) { index ->
                    ActividadItem(descripcion = "Usuario #${index + 100} registró un nuevo huerto.")
                }
                item {
                    ActividadItem(descripcion = "Alerta de sistema: Servidor de Railway con carga alta.", esAlerta = true)
                }
            }
        }
    }
}

@Composable
fun EstadisticaCard(titulo: String, valor: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = VerdeAdmin, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo, fontSize = 12.sp, color = Color.Gray)
            }
            Text(text = valor, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VerdeAdmin, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ActividadItem(descripcion: String, esAlerta: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (esAlerta) Color(0xFFFFEBEE) else Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (esAlerta) Icons.Filled.Analytics else Icons.Filled.Dashboard,
            contentDescription = null,
            tint = if (esAlerta) Color.Red else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = descripcion, fontSize = 14.sp, color = if (esAlerta) Color.Red else Color.Black)
    }
}