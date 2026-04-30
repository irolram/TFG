package com.example.tfg.ui.components

import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.Huerto

/**
 * OPTIMIZACIÓN 1: Eliminamos el Card innecesario.
 * OutlinedTextField ya tiene soporte para fondo y forma. Menos capas = más rápido.
 */
@Composable
fun HuertoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}



@Composable
fun FichaTecnicaSimple(planta: CatalogoDePlantas) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // Usamos el color secundario del tema suavizado
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📏 Especificaciones de siembra",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de items reutilizando el componente InfoItem
            val specs = listOf(
                Triple("Riego", planta.riego?.name ?: "N/A", Icons.Default.WaterDrop),
                Triple("Luz", planta.luzSolar?.name ?: "N/A", Icons.Default.LightMode),
                Triple("Profundidad", planta.profundidadSiembra ?: "N/A", Icons.Default.Straighten),
                Triple("Crecimiento", "${planta.diasCrecimiento ?: "--"} días", Icons.Default.Timer),
                Triple("Separación entre plantas", "${planta.distanciaEntrePlantas ?: "--"} ", Icons.Default.WaterDrop),
                Triple("Estación ideal", planta.temporadaIdeal ?: "N/A", Icons.Default.CalendarMonth)
            )

            specs.forEach { (label, valor, icon) ->
                InfoItem(label = label, valor = valor, icon = icon)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * OPTIMIZACIÓN 3: Refactorización de ItemHuerto.
 * Simplificamos el SwipeToDismiss para que sea más legible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHuerto(huerto: Huerto, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // Solo permitimos borrar deslizando a la izquierda
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
                    .background(color, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp).size(28.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = huerto.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (huerto.descripcion.isNotEmpty()) {
                        Text(
                            text = huerto.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // El widget de clima se mantiene porque es funcional
                    WidgetClima(latitud = huerto.latitud, longitud = huerto.longitud)
                }
            }
        }
    )
}

fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "Sin fecha"

    return try {
        // Definimos el formato: dd/MM/yyyy
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Creamos el objeto fecha directamente con el Long
        val date = Date(timestamp)
        sdf.format(date)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}