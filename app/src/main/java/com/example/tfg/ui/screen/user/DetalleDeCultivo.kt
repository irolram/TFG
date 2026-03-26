package com.example.tfg.ui.screen.user
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg.ui.components.FichaTecnicaSimple
import com.example.tfg.viewModel.HuertosViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCultivoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    cultivoId: String
) {
    // 🚩 Usamos el nombre intuitivo que acordamos
    val miCultivo = viewModel.cultivosDelHuerto.value.find { it.id == cultivoId }

    // 💡 Creamos una referencia corta a la info del catálogo para no escribir tanto
    val detalleEspecie = miCultivo?.infoCatalogo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(miCultivo?.nombre?.replaceFirstChar { it.uppercase() } ?: "Detalle del Cultivo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (miCultivo == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontró el cultivo en tu huerto")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // --- CABECERA ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        // 🚩 CAMBIO: Ahora el icono viene del catálogo
                        model = detalleEspecie?.icono?.trim(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8E9))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(miCultivo.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = miCultivo.estado,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- FICHA TÉCNICA ---
                Text("📋 Guía técnica de la especie", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                if (detalleEspecie != null) {
                    FichaTecnicaSimple(planta = detalleEspecie)
                }

                Spacer(Modifier.height(24.dp))

                // --- INSTRUCCIONES PERSONALIZADAS ---
                Text("🌱 Consejos de cultivo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = detalleEspecie?.instrucciones
                                ?: "Estamos preparando los mejores consejos para tu ${miCultivo.nombre}...",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}


