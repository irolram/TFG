package com.example.tfg.ui.components

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
import androidx.compose.ui.unit.sp
import com.example.tfg.ui.screens.VerdePrimario
import com.example.tfg.ui.screens.VerdeSecundario
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.Huerto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = VerdePrimario) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeSecundario,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            )
        )
    }
}

// Función para mostrar la ficha técnica de la planta
@Composable
fun FichaTecnicaSimple(planta: CatalogoDePlantas) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📏 Especificaciones de siembra",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(12.dp))


            InfoItem(label = "Riego:", valor = planta.riego ?: "N/A", icon = Icons.Default.WaterDrop)
            InfoItem(label = "Luz:", valor = planta.luzSolar ?: "N/A", icon = Icons.Default.LightMode)
            InfoItem(label = "Profundidad:", valor = planta.profundidadSiembra ?: "N/A", icon = Icons.Default.Straighten)
            InfoItem(label = "Crecimiento:", valor = "${planta.diasCrecimiento ?: "--"} días", icon = Icons.Default.Timer)
        }
    }
}
// Función para personalizar las instrucciones de cada cultivo
@Composable
fun SeccionInstrucciones(instrucciones: String?) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, null, tint = Color(0xFF1B5E20), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Manual de cultivo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1B5E20)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = instrucciones ?: "Cargando consejos de expertos para esta planta...",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

// Función para mostrar la información de cada cultivo
@Composable
fun InfoItem(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF4CAF50))
        Spacer(Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(text = valor, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    }
}

// Función para mostrar la información de cada cultivo menos detallado
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHuerto(huerto: Huerto, onClick: () -> Unit, onDeleteClick: () -> Unit) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
                    .background(Color.Red, shape = CardDefaults.shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar Huerto",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = huerto.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = huerto.descripcion,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WidgetClima(latitud = huerto.latitud, longitud = huerto.longitud)
                }
            }
        }
    )
}

