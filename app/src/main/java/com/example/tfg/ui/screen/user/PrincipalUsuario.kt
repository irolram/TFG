package com.example.tfg.ui.screen.user

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
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel


val VerdePrenda = Color(0xFF4CAF50)
val VerdeFondo = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalUser(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Inicio", "Mis Huertos", "Perfil")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Eco, Icons.Filled.Person)
    val context = LocalContext.current
    val apiService = RetrofitClient.getApiService(context)
    val viewModel: HuertosViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.obtenerTodosLosHuertos(apiService)
    }

    val listaHuertos = viewModel.huertos.value
    val estaCargando = viewModel.cargando.value
    val mensajeError = viewModel.error.value

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
                onClick = { navController.navigate("crear_huerto") },
                containerColor = VerdePrenda,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Huerto")
            }
        }
    ) { paddingValues ->
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

            when {
                estaCargando -> {
                    // Muestra una ruedita girando mientras espera a Railway
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdePrenda)
                    }
                }
                mensajeError != null -> {
                    Text(text = "Ups: $mensajeError", color = Color.Red)
                }
                listaHuertos.isEmpty() -> {
                    Text(text = "Aún no tienes huertos. ¡Añade el primero!", color = Color.Gray)
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listaHuertos) { huerto ->
                            HuertoCard(huerto = huerto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HuertoCard(huerto: Huerto) {
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
            Icon(
                Icons.Filled.Eco,
                contentDescription = null,
                tint = VerdePrenda,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                // Mostramos el nombre
                Text(
                    text = huerto.nombre.ifEmpty { "Huerto sin nombre" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // Mostramos la descripción (o un texto por defecto si viene vacía)
                Text(
                    text = huerto.descripcion.ifEmpty { "Sin descripción disponible" },
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}