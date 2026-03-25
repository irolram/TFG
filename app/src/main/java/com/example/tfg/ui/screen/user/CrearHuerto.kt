package com.example.tfg.ui.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var latitud by remember { mutableDoubleStateOf(0.0) }
    var longitud by remember { mutableDoubleStateOf(0.0) }
    var ubicacionCapturada by remember { mutableStateOf(false) }
    var modoGpsActivo by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val apiService = RetrofitClient.getApiService(context)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val estaGuardando = viewModel.guardando.value
    val guardadoExitoso = viewModel.guardadoExitoso.value

    // Lanzador de permisos mejorado
    val permisoLanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Log.d("MI_TFG_GPS", "Permiso concedido tras solicitud")
            Toast.makeText(context, "¡Permiso aceptado! Pulsa el botón de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("MI_TFG_GPS", "Permiso denegado")
            Toast.makeText(context, "Se necesita el GPS para situar el huerto.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            navController.popBackStack()
            viewModel.resetEstado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Huerto", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    Log.d("MI_TFG_GPS", "Botón pulsado")
                    val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        Log.d("MI_TFG_GPS", "Tiene permisos. Pidiendo ubicación...")
                        @SuppressLint("MissingPermission")
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                Log.d("MI_TFG_GPS", "¡Ubicación OK!: ${location.latitude}")
                                latitud = location.latitude
                                longitud = location.longitude
                                ubicacionCapturada = true
                                modoGpsActivo = true
                            } else {
                                Log.e("MI_TFG_GPS", "Ubicación NULL. Activa el GPS en los ajustes o emulador.")
                                Toast.makeText(context, "GPS no detectado aún.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.w("MI_TFG_GPS", "No tiene permisos. Lanzando diálogo...")
                        permisoLanzador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (modoGpsActivo) Color(0xFF2E7D32) else Color(0xFF4CAF50))
            ) {
                Icon(Icons.Filled.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text(if (modoGpsActivo) "📍 GPS Activo" else "📍 Usar mi GPS actual")
            }

            MapaSelectorUbicacion(latitud, longitud, modoGpsActivo) { lat, lon ->
                latitud = lat
                longitud = lon
                ubicacionCapturada = true
                modoGpsActivo = false
            }

            Button(
                onClick = { viewModel.crearNuevoHuerto(apiService, nombre, descripcion, latitud, longitud) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !estaGuardando && nombre.isNotBlank() && ubicacionCapturada,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (estaGuardando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Guardar Huerto", fontWeight = FontWeight.Bold)
            }
        }
    }
}