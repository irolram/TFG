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
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario

// 🚩 OPTIMIZACIÓN 1: Estado sellado para acciones de moderación
sealed class ModAction {
    data object None : ModAction()
    data class ConfirmPromote(val usuario: Usuario) : ModAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosModScreen(
    listaUsuarios: List<Usuario>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPromocionarAMod: (String) -> Unit
) {
    // Estado de acción único
    var accionPendiente by remember { mutableStateOf<ModAction>(ModAction.None) }

    // Filtrado eficiente de candidatos (Solo USERs)
    val candidatos = remember(listaUsuarios) {
        listaUsuarios.filter { it.rol == Rol.USER }
    }

    // 🚩 OPTIMIZACIÓN 2: Diálogo centralizado
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { accionPendiente = ModAction.None }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            // Cabecera que usa el color Teal del tema de Moderador automáticamente
            Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding()) {
                    Text("RECLUTAMIENTO", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text("Usuarios candidatos a moderación", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            if (candidatos.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay candidatos disponibles", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🚩 OPTIMIZACIÓN 3: Claves únicas para mejor rendimiento en listas
                    items(candidatos, key = { it.id }) { usuario ->
                        CandidatoItem(
                            usuario = usuario,
                            onPromoteClick = { accionPendiente = ModAction.ConfirmPromote(usuario) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CandidatoItem(usuario: Usuario, onPromoteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(usuario.email, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }

            Button(
                onClick = onPromoteClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Shield, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("HACER MOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}