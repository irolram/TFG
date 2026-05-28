package com.example.tfg.ui.screen.user

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.TipoTicket
import com.example.tfg.data.model.Usuario
import com.example.tfg.viewModel.TicketViewModel

// Pantalla de soporte para enviar un ticket.
// El usuario elige el tipo, escribe asunto/descripción y se envía al ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarTicketScreen(
    navController: NavHostController,
    viewModel: TicketViewModel,
    usuario: Usuario?
) {
    // rememberSaveable conserva los campos si el móvil gira.
    var asunto by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var tipoSeleccionado by rememberSaveable { mutableStateOf(TipoTicket.SUGERENCIA) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Soporte y Ayuda",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 16.dp)
        ) {
            FormularioTicket(
                asunto = asunto,
                descripcion = descripcion,
                tipoSeleccionado = tipoSeleccionado,
                usuario = usuario,
                isLandscape = isLandscape,
                onAsuntoChange = { asunto = it },
                onDescripcionChange = { descripcion = it },
                onTipoChange = { tipoSeleccionado = it },
                onEnviar = {
                    viewModel.enviarTicket(asunto, descripcion, usuario, tipoSeleccionado)
                    navController.popBackStack()
                }
            )
        }
    }
}

// Formulario interno del ticket.
// Está separado para mantener limpia la pantalla principal y centralizar validación/interfaz.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioTicket(
    asunto: String,
    descripcion: String,
    tipoSeleccionado: TipoTicket,
    usuario: Usuario?,
    isLandscape: Boolean,
    onAsuntoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTipoChange: (TipoTicket) -> Unit,
    onEnviar: () -> Unit
) {
    Text(
        text = "¿En qué podemos ayudarte?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    // Chips para clasificar el ticket antes de enviarlo.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipoTicket.entries.forEach { tipo ->
            FilterChip(
                selected = tipoSeleccionado == tipo,
                onClick = { onTipoChange(tipo) },
                label = { Text(tipo.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }

    OutlinedTextField(
        value = asunto,
        onValueChange = onAsuntoChange,
        label = { Text("Asunto del problema") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = descripcion,
        onValueChange = onDescripcionChange,
        label = { Text("Descripción detallada") },
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLandscape) 110.dp else 180.dp),
        shape = RoundedCornerShape(12.dp)
    )

    Button(
        onClick = onEnviar,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = asunto.isNotBlank() && descripcion.isNotBlank() && usuario != null,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (usuario == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                strokeWidth = 2.dp
            )

            Spacer(Modifier.width(12.dp))

            Text("Cargando perfil...")
        } else {
            Text("ENVIAR TICKET", fontWeight = FontWeight.ExtraBold)
        }
    }
}