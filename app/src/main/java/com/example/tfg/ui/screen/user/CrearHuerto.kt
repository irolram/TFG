package com.example.tfg.ui.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.* // 🚩 IMPORTANTE: Aquí están 'getValue' y 'setValue'
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearHuertoScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    // 🚩 OPTIMIZACIÓN: Acceso limpio al estado del ViewModel
    val state by viewModel.uiState

    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados locales del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var latitud by remember { mutableDoubleStateOf(0.0) }
    var longitud by remember { mutableDoubleStateOf(0.0) }
    var ubicacionCapturada by remember { mutableStateOf(false) }
    var modoGpsActivo by remember { mutableStateOf(false) }

    // Lanzador de permisos de ubicación
    val permisoLanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Toast.makeText(context, "¡Permiso concedido! Pulsa el botón de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita el GPS para situar el huerto.", Toast.LENGTH_LONG).show()
        }
    }

    // Navegación automática si se guarda bien
    LaunchedEffect(state.operacionExitosa) {
        if (state.operacionExitosa) {
            navController.popBackStack()
            viewModel.resetEstado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Huerto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- BLOQUE 1: DATOS ---
            Text("Detalles del Huerto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del huerto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Breve descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            HorizontalDivider()

            // --- BLOQUE 2: GPS ---
            Text("Localización", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (tienePermiso) {
                        @SuppressLint("MissingPermission")
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                latitud = location.latitude
                                longitud = location.longitude
                                ubicacionCapturada = true
                                modoGpsActivo = true
                            } else {
                                Toast.makeText(context, "Activa el GPS en los ajustes.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        permisoLanzador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (modoGpsActivo) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (modoGpsActivo) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(if (modoGpsActivo) Icons.Default.GpsFixed else Icons.Default.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text(if (modoGpsActivo) "📍 Ubicación fijada" else "📍 Usar mi posición GPS")
            }

            // Aquí va tu componente de Mapa
            Box(modifier = Modifier.height(240.dp).fillMaxWidth()) {
                MapaSelectorUbicacion(latitud, longitud, modoGpsActivo) { lat, lon ->
                    latitud = lat
                    longitud = lon
                    ubicacionCapturada = true
                    modoGpsActivo = false
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BLOQUE 3: GUARDAR ---
            Button(
                onClick = {
                    // 🚩 Llamamos a la función optimizada del ViewModel
                    viewModel.crearNuevoHuerto(apiService, nombre, descripcion, latitud, longitud)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.cargando && nombre.isNotBlank() && ubicacionCapturada,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("GUARDAR HUERTO", fontWeight = FontWeight.ExtraBold)
                }
            }

            // Muestra error si lo hay en el estado
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}