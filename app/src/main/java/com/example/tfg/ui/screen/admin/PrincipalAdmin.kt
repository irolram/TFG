package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.tfg.ui.screen.user.verdeEco
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
    viewModel: UsuarioViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    // Observamos los datos del ViewModel
    val listaUsuarios by viewModel.listaUsuarios.collectAsState()

    // Cargamos usuarios al iniciar o al volver a la pestaña de usuarios
    LaunchedEffect(selectedItem) {
        if (selectedItem == 1) {
            viewModel.listarUsuarios()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop - Panel Admin", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = verdeEco,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        scope.launch {
                            TokenManager(context).clearAuth()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("Dashboard", "Usuarios", "Mapa", "Ajustes")
                val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.People, Icons.Filled.Map, Icons.Filled.Settings)

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
            val isRefreshing by viewModel.isRefreshing.collectAsState()
            when (selectedItem) {
                0 -> DashboardAdminContent()
                1 -> GestionUsuariosAdminScreen(listaUsuarios = listaUsuarios,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.listarUsuarios() },
                    onCambiarRol = { id, nuevoRol -> viewModel.actualizarRol(id, nuevoRol) },
                    onEliminarUsuario = { id -> viewModel.eliminarUsuario(id) }
                )
                2 -> SeccionEnConstruccion("Mapa Global de Huertos")
                3 -> SeccionEnConstruccion("Ajustes del Sistema")
            }
        }
    }
}

// --- SUB-PANTALLA: DASHBOARD ---
@Composable
fun DashboardAdminContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen del Sistema",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EstadisticaCard("Usuarios", "1,234", Icons.Filled.People, Modifier.weight(1f))
            EstadisticaCard("Huertos", "567", Icons.Filled.Eco, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Actividad Reciente",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(5) { index ->
                ActividadItem(
                    descripcion = "Usuario ${100 + index} ha creado un nuevo huerto.",
                    esAlerta = index == 2
                )
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun EstadisticaCard(titulo: String, valor: String, icono: ImageVector, modifier: Modifier = Modifier) {
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