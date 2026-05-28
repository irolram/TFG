package com.example.tfg.ui.screen.mod

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.ModAction
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario

// Pantalla de gestión del moderador.
// Muestra solo usuarios con rol USER para que el moderador pueda proponerlos/ascenderlos a MOD.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosModScreen(
    listaUsuarios: List<Usuario>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPromocionarAMod: (String) -> Unit
) {
    // Guarda la acción pendiente para saber si hay que mostrar un diálogo de confirmación.
    var accionPendiente by remember { mutableStateOf<ModAction>(ModAction.None) }

    // Detecta si el móvil está en horizontal para compactar la cabecera y aprovechar mejor la pantalla.
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Filtra la lista y deja únicamente usuarios normales, que son los candidatos a moderador.
    val candidatos = remember(listaUsuarios) {
        listaUsuarios.filter { it.rol == Rol.USER }
    }

    // Si hay una promoción pendiente, se muestra un diálogo antes de ejecutar la acción.
    if (accionPendiente is ModAction.ConfirmPromote) {
        val usuario = (accionPendiente as ModAction.ConfirmPromote).usuario

        AlertDialog(
            onDismissRequest = { accionPendiente = ModAction.None },
            title = { Text("Ascenso de Rango") },
            text = { Text("¿Deseas otorgar permisos de moderación a ${usuario.nombre}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onPromocionarAMod(usuario.id)
                        accionPendiente = ModAction.None
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { accionPendiente = ModAction.None }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera de la pantalla. En horizontal se reduce para dejar más espacio a la lista.
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
                    text = "RECLUTAMIENTO",
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
                        text = "Usuarios candidatos a moderación",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Contenedor con gesto de arrastrar hacia abajo para refrescar la lista.
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (candidatos.isEmpty() && !isRefreshing) {
                // Mensaje vacío cuando no hay usuarios USER disponibles para ascender.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay candidatos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                // Lista con scroll de candidatos. LazyColumn ya gestiona el scroll vertical.
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
                    items(candidatos, key = { it.id }) { usuario ->
                        CandidatoItem(
                            usuario = usuario,
                            compacta = isLandscape,
                            onPromoteClick = {
                                accionPendiente = ModAction.ConfirmPromote(usuario)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta individual de candidato.
// Enseña nombre, email y un botón para iniciar la promoción a moderador.
@Composable
fun CandidatoItem(
    usuario: Usuario,
    compacta: Boolean = false,
    onPromoteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            // Datos principales del usuario candidato.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // Botón que abre el diálogo de confirmación antes de ascender al usuario.
            Button(
                onClick = onPromoteClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "HACER MOD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}