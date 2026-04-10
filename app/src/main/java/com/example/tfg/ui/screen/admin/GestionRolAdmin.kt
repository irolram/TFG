package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario
import com.example.tfg.viewModel.UsuarioViewModel

@Composable
fun GestionUsuariosAdminScreen(
    listaUsuarios: List<Usuario>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCambiarRol: (String, Rol) -> Unit,
    onEliminarUsuario: (String) -> Unit,
) {


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ){
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F4F4))) {
            // Cabecera más "potente"
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(20.dp)) {
                Text("PANEL DE CONTROL: ADMIN", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(listaUsuarios) { usuario ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${usuario.nombre} (${usuario.rol})", fontWeight = FontWeight.Bold)
                                Text(usuario.email, style = MaterialTheme.typography.bodySmall)
                            }

                            // Acciones completas
                            IconButton(onClick = { onCambiarRol(usuario.id, Rol.USER) }) {
                                Icon(Icons.Default.ArrowDownward, "Bajar", tint = Color.Red)
                            }
                            IconButton(onClick = { onCambiarRol(usuario.id, Rol.MOD) }) {
                                Icon(Icons.Default.ArrowUpward, "Subir", tint = Color(0xFF4CAF50))
                            }
                            IconButton(onClick = { onEliminarUsuario(usuario.id) }) {
                                Icon(Icons.Default.Delete, "Borrar", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}