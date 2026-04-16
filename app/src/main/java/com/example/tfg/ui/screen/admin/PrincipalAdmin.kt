package com.example.tfg.ui.screen.admin

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.RolData
import com.example.tfg.viewModel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalAdmin(
    navController: NavHostController,
    viewModel: UsuarioViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit // 🚩 Pasamos el logout desde la MainActivity como en la UserScreen
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    // Observamos los estados del ViewModel
    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listaUsuarios by viewModel.listaUsuarios.collectAsState()
    val miIdActual = usuarioLogueado?.id ?: ""

    // 🚩 OPTIMIZACIÓN 1: Carga de datos inteligente
    // Solo cargamos si la lista está vacía para evitar peticiones innecesarias al cambiar de pestaña
    LaunchedEffect(selectedItem) {
        when (selectedItem) {
            0 -> viewModel.cargarEstadisticas()
            1 -> if (listaUsuarios.isEmpty()) viewModel.listarUsuarios()
        }
    }

    Scaffold(
        topBar = {
            // Unificamos la TopBar para que respete el tema
            TopAppBar(
                title = {
                    Text(
                        text = when(selectedItem) {
                            0 -> "Estadísticas Globales"
                            1 -> "Gestión de Usuarios"
                            2 -> "Radar de Proximidad"
                            else -> "Mi Perfil"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf("Métricas", "Usuarios", "Mapa", "Perfil")
                val icons = listOf(Icons.Default.Dashboard, Icons.Default.People, Icons.Default.Radar, Icons.Default.Person)

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // 🚩 OPTIMIZACIÓN 2: Eliminamos Box innecesarios
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedItem) {
                0 -> DashboardAdminContent(viewModel)
                1 -> GestionUsuariosAdminScreen(
                    listaUsuarios = listaUsuarios,
                    miIdActual = miIdActual,
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
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun DashboardAdminContent(viewModel: UsuarioViewModel) {
    val stats by viewModel.stats.collectAsState()

    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Resumen del Sistema",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EstadisticaCard(
                        titulo = "Usuarios",
                        valor = "${stats!!.totalUsuarios}",
                        icono = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    EstadisticaCard(
                        titulo = "Huertos",
                        valor = "${stats!!.totalHuertos}",
                        icono = Icons.Default.Eco,
                        color = Color(0xFFEF6C00), // Naranja para huertos
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Distribución por Roles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val datosGrafico = stats!!.usuariosPorRol.map { (rol, cantidad) ->
                            RolData(
                                nombre = rol,
                                cantidad = cantidad.toInt(),
                                color = when (rol) {
                                    "ADMIN" -> MaterialTheme.colorScheme.error
                                    "MOD" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        GraficoDistribucionRoles(datos = datosGrafico)
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Text(text = titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GraficoDistribucionRoles(datos: List<RolData>) {
    val maxValor = datos.maxOfOrNull { it.cantidad }?.toFloat() ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        datos.forEach { item ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("${item.cantidad}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.cantidad / maxValor },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                    color = item.color,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}