package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario

// Colores específicos para diferenciar del Admin
val ColorMod = Color(0xFF00796B)
val GrisFondoMod = Color(0xFFF4F7F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosModScreen(
    listaUsuarios: List<Usuario>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPromocionarAMod: (String) -> Unit
) {
    val candidatos = remember(listaUsuarios) {
        listaUsuarios.filter { it.rol == Rol.USER }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    // Diálogo de confirmación
    if (showConfirmDialog && usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Ascender a Moderador") },
            text = { Text("¿Quieres darle permisos de moderación a ${usuarioSeleccionado!!.nombre}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onPromocionarAMod(usuarioSeleccionado!!.id)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorMod)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(GrisFondoMod)) {
        // Cabecera
        Box(modifier = Modifier.fillMaxWidth().background(ColorMod).padding(20.dp)) {
            Column {
                Text("RECLUTAMIENTO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Solo se muestran usuarios candidatos", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (candidatos.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay usuarios pendientes de ascender", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(candidatos) { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = usuario.email, color = Color.Gray, fontSize = 12.sp)
                                }

                                // 🚩 CLAVE 2: El botón de acción
                                Button(
                                    onClick = {
                                        usuarioSeleccionado = usuario
                                        showConfirmDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorMod),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("HACER MOD", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}