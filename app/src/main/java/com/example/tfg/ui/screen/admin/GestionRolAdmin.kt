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
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario

// 🚩 OPTIMIZACIÓN 1: Usar un estado sellado para los diálogos
// Evita tener 3 o 4 booleanos diferentes.
sealed class AdminAction {
    data object None : AdminAction()
    data class ChangeRole(val usuario: Usuario, val nuevoRol: Rol) : AdminAction()
    data class DeleteUser(val usuario: Usuario) : AdminAction()
}

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
    // Estado único para acciones
    var accionPendiente by remember { mutableStateOf<AdminAction>(AdminAction.None) }

    // 🚩 OPTIMIZACIÓN 2: Diálogos extraídos para no ensuciar el código principal
    when (val accion = accionPendiente) {
        is AdminAction.ChangeRole -> {
            AlertDialog(
                onDismissRequest = { accionPendiente = AdminAction.None },
                title = { Text("Confirmar Cambio") },
                text = { Text("¿Cambiar a ${accion.usuario.nombre} al rango ${accion.nuevoRol}?") },
                confirmButton = {
                    Button(onClick = {
                        onCambiarRol(accion.usuario.id, accion.nuevoRol)
                        accionPendiente = AdminAction.None
                    }) { Text("Confirmar") }
                },
                dismissButton = { TextButton(onClick = { accionPendiente = AdminAction.None }) { Text("Cancelar") } }
            )
        }
        is AdminAction.DeleteUser -> {
            AlertDialog(
                onDismissRequest = { accionPendiente = AdminAction.None },
                icon = { Icon(Icons.Default.Warning, null, tint = Color.Red) },
                title = { Text("Eliminar Usuario") },
                text = { Text("¿Seguro que quieres borrar a ${accion.usuario.nombre}? Esta acción es irreversible.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onEliminarUsuario(accion.usuario.id)
                            accionPendiente = AdminAction.None
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { accionPendiente = AdminAction.None }) { Text("Cancelar") } }
            )
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            // Cabecera optimizada con colores del tema
            Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding()) {
                    Text("GESTIÓN DE USUARIOS", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text("Panel de Control de Accesos", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            if (listaUsuarios.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay usuarios registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🚩 OPTIMIZACIÓN 3: Usar key para que LazyColumn sea más eficiente al mover/borrar
                    items(listaUsuarios, key = { it.id }) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            esPropio = usuario.id == miIdActual,
                            onPromote = { accionPendiente = AdminAction.ChangeRole(usuario, it) },
                            onDemote = { accionPendiente = AdminAction.ChangeRole(usuario, it) },
                            onDelete = { accionPendiente = AdminAction.DeleteUser(usuario) }
                        )
                    }
                }
            }
        }
    }
}

// 🚩 OPTIMIZACIÓN 4: Componente de tarjeta extraído
@Composable
fun UsuarioCard(
    usuario: Usuario,
    esPropio: Boolean,
    onPromote: (Rol) -> Unit,
    onDemote: (Rol) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(usuario.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(Modifier.height(8.dp))

                // Badge de rol dinámico
                val (color, label) = when (usuario.rol) {
                    Rol.ADMIN -> Color.Red to "ADMINISTRADOR"
                    Rol.MOD -> Color.Blue to "MODERADOR"
                    Rol.USER -> Color(0xFF4CAF50) to "USUARIO"
                }

                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            if (!esPropio) {
                Row {
                    IconButton(onClick = {
                        val next = if (usuario.rol == Rol.USER) Rol.MOD else Rol.ADMIN
                        onPromote(next)
                    }, enabled = usuario.rol != Rol.ADMIN) {
                        Icon(Icons.Default.ArrowUpward, null, tint = if (usuario.rol != Rol.ADMIN) Color(0xFF4CAF50) else Color.LightGray)
                    }
                    IconButton(onClick = {
                        val prev = if (usuario.rol == Rol.ADMIN) Rol.MOD else Rol.USER
                        onDemote(prev)
                    }, enabled = usuario.rol != Rol.USER) {
                        Icon(Icons.Default.ArrowDownward, null, tint = if (usuario.rol != Rol.USER) Color(0xFFEF6C00) else Color.LightGray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            } else {
                Text("(Tú)", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }
        }
    }
}