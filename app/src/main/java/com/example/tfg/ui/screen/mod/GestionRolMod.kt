package com.example.tfg.ui.screen.mod
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario


@Composable
fun GestionUsuariosModScreen(
    listaUsuarios: List<Usuario>,
    onPromocionar: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Cabecera más amigable
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF4CAF50)).padding(20.dp)) {
            Text("COMUNIDAD ECO DROP", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Filtramos para que el MOD solo vea a los USER normales
        val soloUsuarios = listaUsuarios.filter { it.rol == Rol.USER }

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(soloUsuarios) { usuario ->
                ListItem(
                    headlineContent = { Text(usuario.nombre) },
                    supportingContent = { Text("Usuario activo") },
                    trailingContent = {
                        Button(onClick = { onPromocionar(usuario.id) }) {
                            Text("Hacer Moderador")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}