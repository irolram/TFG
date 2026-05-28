package com.example.tfg.ui.screen.admin

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.AdminAction
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario

// Funcion que gestiona la gestion de usuarios
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
    var accionPendiente by remember { mutableStateOf<AdminAction>(AdminAction.None) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    when (val accion = accionPendiente) {
        is AdminAction.ChangeRole -> {
            AlertDialog(
                onDismissRequest = { accionPendiente = AdminAction.None },
                title = { Text("Confirmar Cambio") },
                text = { Text("¿Cambiar a ${accion.usuario.nombre} al rango ${accion.nuevoRol}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onCambiarRol(accion.usuario.id, accion.nuevoRol)
                            accionPendiente = AdminAction.None
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accionPendiente = AdminAction.None }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        is AdminAction.DeleteUser -> {
            AlertDialog(
                onDismissRequest = { accionPendiente = AdminAction.None },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red
                    )
                },
                title = { Text("Eliminar Usuario") },
                text = { Text("¿Seguro que quieres borrar a ${accion.usuario.nombre}? Esta acción es irreversible.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onEliminarUsuario(accion.usuario.id)
                            accionPendiente = AdminAction.None
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accionPendiente = AdminAction.None }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        else -> Unit
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (isLandscape) 10.dp else 20.dp
                    )
                    .statusBarsPadding()
            ) {
                Text(
                    text = "GESTIÓN DE USUARIOS",
                    style = if (isLandscape) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                if (!isLandscape) {
                    Text(
                        text = "Panel de Control de Accesos",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (listaUsuarios.isEmpty() && !isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay usuarios registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = if (isLandscape) 8.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (isLandscape) 8.dp else 12.dp
                    )
                ) {
                    items(listaUsuarios, key = { it.id }) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            esPropio = usuario.id == miIdActual,
                            compacta = isLandscape,
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
// Funcion que gestiona la tarjeta de usuario
@Composable
fun UsuarioCard(
    usuario: Usuario,
    esPropio: Boolean,
    compacta: Boolean = false,
    onPromote: (Rol) -> Unit,
    onDemote: (Rol) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = if (compacta) 10.dp else 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(Modifier.height(if (compacta) 4.dp else 8.dp))

                val (color, label) = when (usuario.rol) {
                    Rol.ADMIN -> Color.Red to "ADMINISTRADOR"
                    Rol.MOD -> Color.Blue to "MODERADOR"
                    Rol.USER -> Color(0xFF4CAF50) to "USUARIO"
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!esPropio) {
                Row {
                    IconButton(
                        onClick = {
                            if (usuario.rol == Rol.USER) {
                                onPromote(Rol.MOD)
                            }
                        },
                        enabled = usuario.rol == Rol.USER
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Ascender",
                            tint = if (usuario.rol == Rol.USER) {
                                Color(0xFF4CAF50)
                            } else {
                                Color.LightGray
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            val rolAnterior = if (usuario.rol == Rol.ADMIN) {
                                Rol.MOD
                            } else {
                                Rol.USER
                            }

                            onDemote(rolAnterior)
                        },
                        enabled = usuario.rol != Rol.USER
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Degradar",
                            tint = if (usuario.rol != Rol.USER) {
                                Color(0xFFEF6C00)
                            } else {
                                Color.LightGray
                            }
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            } else {
                Text(
                    text = "(Tú)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }
    }
}