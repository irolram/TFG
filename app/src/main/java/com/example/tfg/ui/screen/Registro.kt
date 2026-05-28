package com.example.tfg.ui.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
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

// Pantalla de registro.
// Crea el usuario en Firebase, lo registra en el servidor, guarda el token y completa su perfil.
@Composable
fun RegisterScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val tokenManager = remember { TokenManager(context) }
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }

    // Campos del formulario. rememberSaveable evita perder los datos al girar el móvil.
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmarPassword by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isLandscape) 48.dp else 24.dp)
                    .padding(vertical = if (isLandscape) 20.dp else 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.Center
            ) {
                // Título principal del registro.
                Text(
                    text = "¡Crea tu Huerto!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = if (isLandscape) 28.sp else 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Regístrate para empezar a cultivar",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = if (isLandscape) 14.sp else 16.sp,
                    modifier = Modifier.padding(bottom = if (isLandscape) 20.dp else 32.dp)
                )

                // Formulario de datos personales y credenciales.
                HuertoTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorMsg = null
                    },
                    placeholder = "Nombre",
                    icon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

                HuertoTextField(
                    value = apellidos,
                    onValueChange = {
                        apellidos = it
                        errorMsg = null
                    },
                    placeholder = "Apellidos",
                    icon = Icons.Default.Badge
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

                HuertoTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMsg = null
                    },
                    placeholder = "Email de cultivador",
                    icon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

                HuertoTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMsg = null
                    },
                    placeholder = "Contraseña",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

                HuertoTextField(
                    value = confirmarPassword,
                    onValueChange = {
                        confirmarPassword = it
                        errorMsg = null
                    },
                    placeholder = "Confirmar contraseña",
                    icon = Icons.Default.VerifiedUser,
                    isPassword = true
                )

                // Mensaje de error de validación, Firebase o servidor.
                errorMsg?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 48.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                if (nombre.isBlank() || email.isBlank()) {
                                    errorMsg = "Rellena los campos obligatorios"
                                    return@launch
                                }

                                if (password != confirmarPassword) {
                                    errorMsg = "Las contraseñas no coinciden"
                                    return@launch
                                }

                                isLoading = true
                                errorMsg = null

                                try {
                                    // 1. Crea el usuario en Firebase.
                                    val authResult = auth
                                        .createUserWithEmailAndPassword(email, password)
                                        .await()

                                    val firebaseUser = authResult.user

                                    if (firebaseUser != null) {
                                        val uid = firebaseUser.uid
                                        val correo = firebaseUser.email ?: email

                                        // 2. Login contra el servidor para auto-registrar y recibir JWT.
                                        val loginRequest = LoginRequest(userId = uid, email = correo)
                                        val loginResponse = RetrofitClient
                                            .getApiService(context)
                                            .loginConServidor(loginRequest)

                                        if (loginResponse.isSuccessful) {
                                            val authData = loginResponse.body()

                                            if (authData != null) {
                                                // 3. Guarda token y userId.
                                                tokenManager.saveToken(
                                                    authData.accessToken,
                                                    authData.userId
                                                )

                                                // 4. Completa el perfil del usuario en el backend.
                                                val usuarioActualizado = Usuario(
                                                    id = uid,
                                                    nombre = nombre,
                                                    apellidos = apellidos,
                                                    email = correo,
                                                    rol = Rol.USER
                                                )

                                                RetrofitClient
                                                    .getApiService(context)
                                                    .actualizarUsuario(uid, usuarioActualizado)

                                                // 5. Entra al menú principal y limpia el historial.
                                                navController.navigate("main_menuUser") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        } else {
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
                            .height(if (isLandscape) 54.dp else 64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = "Empezar a Cultivar",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = if (isLandscape) 18.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 32.dp))

                // Enlace para volver al login si ya tiene cuenta.
                Row {
                    Text(
                        text = "¿Ya tienes un huerto? ",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Inicia sesión",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}