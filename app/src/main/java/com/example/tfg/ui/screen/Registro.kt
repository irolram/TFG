package com.example.tfg.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.HuertoTextField
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Paleta de colores temáticos para el huerto
val VerdeFondoInicio = Color(0xFFE8F5E9)
val VerdeFondoFin = Color(0xFFC8E6C9)
val VerdePrimario = Color(0xFF2E7D32)
val VerdeSecundario = Color(0xFF43A047)
val ColorTierra = Color(0xFF795548)

@Composable
fun RegisterScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Estados para los campos
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VerdeFondoInicio, VerdeFondoFin)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Título temático
                Text(
                    "¡Crea tu Huerto!",
                    color = VerdePrimario,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    "Regístrate para empezar a cultivar",
                    color = ColorTierra,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Campos de registro con iconos y estilo Material Design 3
                HuertoTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = "Nombre",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(16.dp))

                HuertoTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    placeholder = "Apellidos",
                    icon = Icons.Default.Badge
                )
                Spacer(modifier = Modifier.height(16.dp))

                HuertoTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email de cultivador",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))

                HuertoTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Contraseña",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                HuertoTextField(
                    value = confirmarPassword,
                    onValueChange = { confirmarPassword = it },
                    placeholder = "Confirmar contraseña",
                    icon = Icons.Default.VerifiedUser,
                    isPassword = true
                )

                // Mensaje de error estilizado
                errorMsg?.let {
                    Text(it, color = Color.Red, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp))
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Botón principal estilizado
                if (isLoading) {
                    CircularProgressIndicator(color = VerdePrimario)
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                // Validación básica de contraseñas
                                if (password != confirmarPassword) {
                                    errorMsg = "Las contraseñas no coinciden"
                                    return@launch
                                }
                                if (nombre.isBlank() || email.isBlank()) {
                                    errorMsg = "Rellena los campos obligatorios"
                                    return@launch
                                }

                                isLoading = true
                                errorMsg = null

                                try {
                                    // 1. REGISTRO EN FIREBASE
                                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                                    val firebaseUser = authResult.user

                                    if (firebaseUser != null) {
                                        val uid = firebaseUser.uid
                                        val correo = firebaseUser.email ?: email

                                        // 2. LOGIN EN RAILWAY (Provoca el auto-registro y nos da el JWT)
                                        val loginRequest = LoginRequest(userId = uid, email = correo)
                                        val loginResponse = RetrofitClient.getApiService(context).loginConServidor(loginRequest)

                                        if (loginResponse.isSuccessful) {
                                            val authData = loginResponse.body()
                                            if (authData != null) {
                                                // 3. GUARDAMOS EL TOKEN
                                                tokenManager.saveToken(authData.accessToken, authData.userId)

                                                // 4. ACTUALIZAMOS EL PERFIL (Le ponemos el nombre real en vez del genérico)
                                                val usuarioActualizado = Usuario(
                                                    id = uid,
                                                    nombre = nombre,
                                                    apellidos = apellidos,
                                                    email = correo,
                                                    rol = Rol.USER // O el rol que devuelva authData
                                                )
                                                // Esta llamada ya lleva el token gracias al AuthInterceptor
                                                RetrofitClient.getApiService(context).actualizarUsuario(uid, usuarioActualizado)

                                                navController.navigate("main_menuUser") {
                                                    // Limpiamos el historial para que no pueda volver atrás al registro
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            // Rollback: Si falla nuestra API, borramos de Firebase para no tener "usuarios fantasma"
                                            firebaseUser.delete().await()
                                            errorMsg = "Error al conectar con el servidor EcoDrop."
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMsg = "Error: ${e.localizedMessage}"
                                    Log.e("REGISTRO", "Fallo: ", e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Empezar a Cultivar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Texto de login estilizado
                Row {
                    Text("¿Ya tienes un huerto? ", color = ColorTierra, fontSize = 14.sp)
                    Text(
                        "Inicia sesión",
                        color = VerdeSecundario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            }
        }
    }
}