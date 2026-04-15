package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.RolData
import com.example.tfg.ui.theme.VerdeEco
import com.example.tfg.viewModel.UsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Colores específicos para Admin
val VerdeAdmin = Color(0xFF2E7D32)
val GrisFondo = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalAdmin(
    navController: NavHostController,
    viewModel: UsuarioViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    val miId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listaUsuarios by viewModel.listaUsuarios.collectAsState()

    LaunchedEffect(selectedItem) {
        when (selectedItem) {
            1 -> viewModel.listarUsuarios()
            3 -> if (usuarioLogueado == null) viewModel.cargarPerfilActual(miId)
        }
    }

    val performLogout = {
        FirebaseAuth.getInstance().signOut()
        scope.launch {
            TokenManager(context).clearAuth()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            // Solo mostramos la TopBar si NO estamos en el perfil (opcional, por estética)
            if (selectedItem != 3) {
                TopAppBar(
                    title = { Text("Eco Drop - Panel Admin", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdeEco,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("Estadísticas", "Usuarios", "Mapa", "Perfil")
                val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.People, Icons.Filled.Map, Icons.Filled.Person)

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerdeAdmin,
                            selectedTextColor = VerdeAdmin,
                            indicatorColor = Color(0xFFE8F5E9)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GrisFondo)
        ) {
            when (selectedItem) {
                0 -> DashboardAdminContent(viewModel)
                1 -> GestionUsuariosAdminScreen(
                    listaUsuarios = listaUsuarios,
                    miIdActual = miId, // Para que no se pueda borrar a sí mismo
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.listarUsuarios() },
                    onCambiarRol = { id, nuevoRol -> viewModel.actualizarRol(id, nuevoRol) },
                    onEliminarUsuario = { id -> viewModel.eliminarUsuario(id) }
                )
                2 -> MapaAdminScreen(viewModel)
                3 -> PerfilAdminScreen(
                    usuario = usuarioLogueado,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = { performLogout() }
                )
            }
        }
    }
}

@Composable
fun DashboardAdminContent(viewModel: UsuarioViewModel) {
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VerdeEco)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("Panel de Control", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EstadisticaCard("Usuarios", "${stats!!.totalUsuarios}", Icons.Default.People,Modifier.weight(1f),VerdeAdmin)
                    EstadisticaCard("Huertos", "${stats!!.totalHuertos}", Icons.Default.Eco, Modifier.weight(1f),Color(0xFFEF6C00))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Distribución por Roles (Actualizado)", fontWeight = FontWeight.Bold)

                        // Transformamos el Map del servidor en la lista para el gráfico
                        val datosGrafico = stats!!.usuariosPorRol.map { (rol, cantidad) ->
                            RolData(
                                nombre = rol,
                                cantidad = cantidad.toInt(),
                                color = when (rol) {
                                    "ADMIN" -> Color(0xFFD32F2F)
                                    "MOD" -> Color(0xFF1976D2)
                                    else -> VerdeAdmin
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        GraficoDistribucionRoles(datos = datosGrafico)
                    }
                }
            }
        }
    }
}


@Composable
fun GraficoDistribucionRoles(datos: List<RolData>) {
    val maxValor = datos.maxOf { it.cantidad }.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        datos.forEach { item ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.nombre, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("${item.cantidad}", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                // Barra de progreso personalizada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFEEEEEE), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (maxValor > 0) item.cantidad / maxValor else 0f)
                            .fillMaxHeight()
                            .background(item.color, CircleShape)
                    )
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun EstadisticaCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    VerdeAdmin1: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null, tint = VerdeAdmin, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = valor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = VerdeAdmin)
            Text(text = titulo, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ActividadItem(descripcion: String, esAlerta: Boolean = false) {
    val colorBase = if (esAlerta) Color(0xFFFFEBEE) else Color.White
    val colorTexto = if (esAlerta) Color(0xFFC62828) else Color.Black
    val colorIcono = if (esAlerta) Color(0xFFE53935) else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorBase, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (esAlerta) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = descripcion, fontSize = 14.sp, color = colorTexto)
    }
}

@Composable
fun SeccionEnConstruccion(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Settings, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Text(titulo, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Próximamente", color = Color.LightGray)
        }
    }
}