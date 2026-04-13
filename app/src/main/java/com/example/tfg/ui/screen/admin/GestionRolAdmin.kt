package com.example.tfg.ui.screen.admin

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosAdminScreen(
    listaUsuarios: List<Usuario>,
    miIdActual: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCambiarRol: (String, Rol) -> Unit,
    onEliminarUsuario: (String) -> Unit
) {
    // 🚩 ESTADOS PARA DIÁLOGOS
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var nuevoRolSeleccionado by remember { mutableStateOf<Rol?>(null) }

    // 1️⃣ DIÁLOGO: CAMBIO DE ROL
    if (showRoleDialog && usuarioSeleccionado != null && nuevoRolSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Confirmar Cambio de Rango") },
            text = { Text("¿Deseas cambiar el rol de ${usuarioSeleccionado!!.nombre} a ${nuevoRolSeleccionado!!.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onCambiarRol(usuarioSeleccionado!!.id, nuevoRolSeleccionado!!)
                        showRoleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeAdmin)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // 2️⃣ DIÁLOGO: ELIMINAR USUARIO
    if (showDeleteDialog && usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("¿Eliminar usuario?") },
            text = { Text("Esta acción es permanente. Se borrarán todos los huertos y datos asociados a ${usuarioSeleccionado!!.nombre}.") },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminarUsuario(usuarioSeleccionado!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // --- ESTRUCTURA DE LA PANTALLA ---
    Column(modifier = Modifier.fillMaxSize().background(GrisFondo)) {

        // Cabecera Estilo Admin
        Box(modifier = Modifier.fillMaxWidth().background(VerdeAdmin).padding(20.dp)) {
            Column {
                Text("GESTIÓN DE USUARIOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Administra los niveles de acceso del sistema", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (listaUsuarios.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay usuarios registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaUsuarios) { usuario ->
                        // No mostrar opciones para el propio Admin logueado (seguridad)
                        val esPropioUsuario = usuario.id == miIdActual

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

                                    Spacer(Modifier.height(8.dp))

                                    // Badge del Rol actual
                                    Surface(
                                        color = when(usuario.rol) {
                                            Rol.ADMIN -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                                            Rol.MOD -> Color(0xFF1976D2).copy(alpha = 0.1f)
                                            else -> VerdeAdmin.copy(alpha = 0.1f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = usuario.rol.name,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when(usuario.rol) {
                                                Rol.ADMIN -> Color(0xFFD32F2F)
                                                Rol.MOD -> Color(0xFF1976D2)
                                                else -> VerdeAdmin
                                            }
                                        )
                                    }
                                }

                                // Botones de Acción (Solo si no es el admin actual)
                                // Botones de Acción (Solo si no es el admin actual)
                                if (!esPropioUsuario) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        // 1️⃣ BOTÓN: SUBIR RANGO (Promote)
                                        IconButton(
                                            onClick = {
                                                usuarioSeleccionado = usuario
                                                nuevoRolSeleccionado = when(usuario.rol) {
                                                    Rol.USER -> Rol.MOD
                                                    Rol.MOD -> Rol.ADMIN
                                                    else -> Rol.ADMIN
                                                }
                                                showRoleDialog = true
                                            },
                                            enabled = usuario.rol != Rol.ADMIN // Bloqueado si ya es Admin
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Subir Rango",
                                                tint = if (usuario.rol != Rol.ADMIN) VerdeAdmin else Color.LightGray
                                            )
                                        }

                                        // 2️⃣ BOTÓN: BAJAR RANGO (Demote)
                                        IconButton(
                                            onClick = {
                                                usuarioSeleccionado = usuario
                                                nuevoRolSeleccionado = when(usuario.rol) {
                                                    Rol.ADMIN -> Rol.MOD
                                                    Rol.MOD -> Rol.USER
                                                    else -> Rol.USER
                                                }
                                                showRoleDialog = true
                                            },
                                            enabled = usuario.rol != Rol.USER // Bloqueado si ya es User
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Bajar Rango",
                                                tint = if (usuario.rol != Rol.USER) Color(0xFFEF6C00) else Color.LightGray // Naranja para avisar del "downgrade"
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // 3️⃣ BOTÓN: ELIMINAR
                                        IconButton(onClick = {
                                            usuarioSeleccionado = usuario
                                            showDeleteDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                } else {
                                    Text("(Tú)", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}