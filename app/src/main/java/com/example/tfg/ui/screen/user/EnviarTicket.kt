package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.TipoTicket
import com.example.tfg.ui.theme.VerdeEco
import com.example.tfg.viewModel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarTicketScreen(
    navController: NavHostController,
    viewModel: TicketViewModel,
    usuario: com.example.tfg.data.model.Usuario?
) {
    var asunto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf(TipoTicket.SUGERENCIA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soporte y Ayuda", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeEco)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("¿Qué ocurre?", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoTicket.values().forEach { tipo ->
                    FilterChip(
                        selected = tipoSeleccionado == tipo,
                        onClick = { tipoSeleccionado = tipo },
                        label = { Text(tipo.name) }
                    )
                }
            }

            OutlinedTextField(
                value = asunto,
                onValueChange = { asunto = it },
                label = { Text("Asunto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Describe el problema detalladamente") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.enviarTicket(asunto, descripcion, usuario, tipoSeleccionado)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = asunto.isNotBlank() && descripcion.isNotBlank() && usuario != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (usuario == null) Color.Gray else VerdeEco
                )
            ) {
                if (usuario == null) {
                    Text("Cargando datos de usuario...")
                } else {
                    Text("ENVIAR TICKET", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}