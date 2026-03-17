package com.example.tfg.ui.screen.user


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.viewModel.HuertosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHuertoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    huertoId: String // 🔌 ¡Aquí recibimos el ID del huerto seleccionado!
) {
    // Nota: Más adelante usaremos este huertoId para decirle al ViewModel:
    // "Descarga solo los cultivos que pertenezcan a este ID"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Huerto", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)),
                // 🔌 Flecha para volver a la pantalla anterior
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 🔌 Aquí navegaremos al formulario de "Crear Cultivo" pasando el huertoId
                    // navController.navigate("crear_cultivo/$huertoId")
                },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Cultivo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ==========================================
            // 1. ZONA DE INFORMACIÓN Y CLIMA
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Un verde muy clarito
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "ID del Huerto:", fontSize = 14.sp, color = Color.Gray)
                    Text(text = huertoId, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "El clima y los detalles irán aquí pronto 🌤️", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 2. ZONA DE CULTIVOS (La lista)
            // ==========================================
            Text(
                text = "Mis Cultivos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Box temporal hasta que tengamos la lista real de la base de datos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no has plantado nada.\n¡Usa el botón + para empezar!",
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            /* 🔌 EL FUTURO LAZYCOLUMN IRÁ AQUÍ
            LazyColumn {
                items(cultivos) { cultivo ->
                    ItemCultivo(cultivo)
                }
            }
            */
        }
    }
}