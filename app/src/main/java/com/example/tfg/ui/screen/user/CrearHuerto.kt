package com.example.tfg.ui.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearHuertoScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // 🔌 Aquí guardaremos las coordenadas del GPS real
    var latitud by remember { mutableDoubleStateOf(0.0) }
    var longitud by remember { mutableDoubleStateOf(0.0) }
    var ubicacionCapturada by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val apiService = RetrofitClient.getApiService(context)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val estaGuardando = viewModel.guardando.value
    val error = viewModel.errorGuardar.value
    val guardadoExitoso = viewModel.guardadoExitoso.value

    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            navController.popBackStack()
            viewModel.resetEstado()
        }
    }

    // 🔌 El lanzador que pide permiso de ubicación al usuario
    val permisoLanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {

            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitud = location.latitude
                    longitud = location.longitude
                    ubicacionCapturada = true
                }
            }
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
                maxLines = 2
            )

            // 📍 BOTÓN DEL GPS (Sustituye al mapa)
            Button(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        @SuppressLint("MissingPermission")
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                latitud = location.latitude
                                longitud = location.longitude
                                ubicacionCapturada = true
                            }
                        }
                    } else {
                        permisoLanzador.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ubicacionCapturada) Color(0xFF388E3C) else Color.Gray
                )
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (ubicacionCapturada) "📍 ¡Ubicación capturada!" else "📍 Obtener mi ubicación actual")
            }

            if (error != null) {
                Text(text = error, color = Color.Red)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && ubicacionCapturada) {
                        viewModel.crearNuevoHuerto(
                            apiService = apiService,
                            nombre = nombre,
                            descripcion = descripcion,
                            latitud = latitud,
                            longitud = longitud
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estaGuardando && nombre.isNotBlank() && ubicacionCapturada,
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