package com.example.tfg.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Define tus colores "Eco" aquí si no los tienes globales
val VerdePrenda = Color(0xFF4CAF50)
val VerdeFondo = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalUser(navController: NavHostController) {
    // Estado para la barra de navegación (0: Inicio, 1: Mis Huertos, 2: Perfil)
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Inicio", "Mis Huertos", "Perfil")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Eco, Icons.Filled.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop - Mis Huertos", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrenda)
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
            FloatingActionButton(
                onClick = { /* Acción para añadir nuevo huerto */ },
                containerColor = VerdePrenda,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Huerto")
            }
        }
    ) { paddingValues ->
        // Contenido principal con fondo verde claro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VerdeFondo)
                .padding(16.dp)
        ) {
            Text(
                text = "¡Bienvenido de nuevo, Jardinero!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VerdePrenda,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista simple de huertos (puedes conectar esto a tu base de datos)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(5) { index ->
                    HuertoCard(nombre = "Huerto Urbano #${index + 1}", tareas = index + 1)
                }
            }
        }
    }
}

@Composable
fun HuertoCard(nombre: String, tareas: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Eco, contentDescription = null, tint = VerdePrenda, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "$tareas tareas pendientes hoy", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}