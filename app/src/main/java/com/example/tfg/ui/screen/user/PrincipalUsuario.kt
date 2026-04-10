package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.viewModel.HuertosViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
val verdeEco = Color(0xFF4CAF50)
val VerdeFondo = Color(0xFFE8F5E9)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalUser(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    selectedItem: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var usuario by remember { mutableStateOf<com.example.tfg.data.model.Usuario?>(null) }
    val items = listOf("Mis Huertos", "Mapa", "Perfil")
    val icons = listOf(Icons.Filled.Eco, Icons.Filled.Map, Icons.Filled.Person)

    val context = LocalContext.current
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorFondoSitio = MaterialTheme.colorScheme.background
    val colorSuperficie = MaterialTheme.colorScheme.surface

    LaunchedEffect(Unit) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("DEBUG_PERFIL", "UID de Firebase: $uid")

        if (uid != null) {
            try {
                val apiService = com.example.tfg.data.network.RetrofitClient.getApiService(context)
                val response = apiService.obtenerUsuarioPorId(uid)

                if (response.isSuccessful) {
                    usuario = response.body()
                    android.util.Log.d("DEBUG_PERFIL", "Usuario cargado: ${usuario?.nombre}")
                } else {
                    android.util.Log.e(
                        "DEBUG_PERFIL",
                        "Error API: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_PERFIL", "Error de conexión: ${e.message}")
            }
        } else {
            android.util.Log.e("DEBUG_PERFIL", "El UID de Firebase es NULL")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Drop", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = verdeEco, // 🚩 AQUÍ: La barra de arriba ahora es verde
                    titleContentColor = Color.White // Texto en blanco para que resalte
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = colorSuperficie) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { onTabChange(index) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorPrimario,
                            selectedTextColor = colorPrimario,
                            indicatorColor = colorPrimario.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_huerto") },
                    containerColor = colorPrimario,
                    contentColor = Color.White
                ) { Icon(Icons.Filled.Add, "Añadir") }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when (selectedItem) {
                0 -> MisHuertosScreen(navController, viewModel)
                1 -> MapaHuertosScreen(viewModel.huertos.value)
                2 -> PerfilScreen(
                    usuario = usuario,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = onLogout
                )
            }
        }
    }
}
