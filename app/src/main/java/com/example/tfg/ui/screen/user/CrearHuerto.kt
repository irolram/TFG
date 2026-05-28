package com.example.tfg.ui.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
import com.google.android.gms.location.LocationServices

// Pantalla para crear un nuevo huerto.
// Permite introducir nombre, descripción y escoger la ubicación con GPS o tocando el mapa.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearHuertoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel
) {
    val state by viewModel.uiState

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val apiService = remember { RetrofitClient.getApiService(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Campos del formulario. rememberSaveable evita perderlos al girar la pantalla.
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var latitud by rememberSaveable { mutableDoubleStateOf(0.0) }
    var longitud by rememberSaveable { mutableDoubleStateOf(0.0) }
    var ubicacionCapturada by rememberSaveable { mutableStateOf(false) }
    var modoGpsActivo by rememberSaveable { mutableStateOf(false) }

    // Launcher para pedir permisos de ubicación al usuario.
    val permisoLanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Toast.makeText(context, "Permiso concedido. Pulsa el botón de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita el GPS para situar el huerto.", Toast.LENGTH_LONG).show()
        }
    }

    // Cuando el ViewModel confirma que el huerto se creó, vuelve atrás y limpia estado.
    LaunchedEffect(state.operacionExitosa) {
        if (state.operacionExitosa) {
            navController.popBackStack()
            viewModel.resetEstado()
        }
    }

    // Función local para pedir/usar la ubicación GPS actual.
    fun capturarUbicacionGps() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

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
            permisoLanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Huerto",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormularioHuerto(
                        nombre = nombre,
                        descripcion = descripcion,
                        modoGpsActivo = modoGpsActivo,
                        stateCargando = state.cargando,
                        error = state.error,
                        ubicacionCapturada = ubicacionCapturada,
                        onNombreChange = { nombre = it },
                        onDescripcionChange = { descripcion = it },
                        onCapturarGps = { capturarUbicacionGps() },
                        onGuardar = {
                            viewModel.crearNuevoHuerto(
                                apiService,
                                nombre,
                                descripcion,
                                latitud,
                                longitud
                            )
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    MapaSelectorUbicacion(
                        latitudActual = latitud,
                        longitudActual = longitud,
                        modoGpsActivo = modoGpsActivo
                    ) { lat, lon ->
                        latitud = lat
                        longitud = lon
                        ubicacionCapturada = true
                        modoGpsActivo = false
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormularioHuerto(
                    nombre = nombre,
                    descripcion = descripcion,
                    modoGpsActivo = modoGpsActivo,
                    stateCargando = state.cargando,
                    error = state.error,
                    ubicacionCapturada = ubicacionCapturada,
                    onNombreChange = { nombre = it },
                    onDescripcionChange = { descripcion = it },
                    onCapturarGps = { capturarUbicacionGps() },
                    onGuardar = {
                        viewModel.crearNuevoHuerto(
                            apiService,
                            nombre,
                            descripcion,
                            latitud,
                            longitud
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                ) {
                    MapaSelectorUbicacion(
                        latitudActual = latitud,
                        longitudActual = longitud,
                        modoGpsActivo = modoGpsActivo
                    ) { lat, lon ->
                        latitud = lat
                        longitud = lon
                        ubicacionCapturada = true
                        modoGpsActivo = false
                    }
                }
            }
        }
    }
}

// Formulario con los campos y acciones para crear el huerto.
// Está separado para poder reutilizarlo tanto en vertical como en horizontal.
@Composable
private fun FormularioHuerto(
    nombre: String,
    descripcion: String,
    modoGpsActivo: Boolean,
    stateCargando: Boolean,
    error: String?,
    ubicacionCapturada: Boolean,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCapturarGps: () -> Unit,
    onGuardar: () -> Unit
) {
    Text(
        text = "Detalles del Huerto",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    OutlinedTextField(
        value = nombre,
        onValueChange = onNombreChange,
        label = { Text("Nombre del huerto") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    OutlinedTextField(
        value = descripcion,
        onValueChange = onDescripcionChange,
        label = { Text("Breve descripción") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 3
    )

    HorizontalDivider()

    Text(
        text = "Localización",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    Button(
        onClick = onCapturarGps,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (modoGpsActivo) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (modoGpsActivo) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    ) {
        Icon(
            imageVector = if (modoGpsActivo) Icons.Default.GpsFixed else Icons.Default.LocationOn,
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(if (modoGpsActivo) "Ubicación fijada" else "Usar mi posición GPS")
    }

    Button(
        onClick = onGuardar,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !stateCargando && nombre.isNotBlank() && ubicacionCapturada,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (stateCargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("GUARDAR HUERTO", fontWeight = FontWeight.ExtraBold)
        }
    }

    error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
        )
    }
}