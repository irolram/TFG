package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearHuertoScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    // Variables para guardar lo que el usuario escribe
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val context = LocalContext.current
    val apiService = RetrofitClient.getApiService(context)

    // Leemos si el ViewModel está ocupado guardando
    val estaGuardando = viewModel.guardando.value
    val error = viewModel.errorGuardar.value
    val guardadoExitoso = viewModel.guardadoExitoso.value
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            navController.popBackStack()
            viewModel.resetEstado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Huerto", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrenda)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Añade los detalles de tu nuevo huerto:")

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del huerto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (ej. Tomates y lechugas)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (error != null) {
                Text(text = error, color = Color.Red)
            }

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        viewModel.crearNuevoHuerto(apiService, nombre, descripcion)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Se desactiva si está guardando o vacío
                enabled = !estaGuardando && nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrenda)
            ) {
                if (estaGuardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Huerto")
                }
            }
        }
    }
}