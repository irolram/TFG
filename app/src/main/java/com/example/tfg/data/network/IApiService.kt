package com.example.tfg.data.network

import com.example.tfg.data.model.Usuario
import com.example.tfg.data.model.Huerto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IApiService {

    // Ruta para registrar al usuario (coincide con app.post('/usuarios', ...) en tu API)
    @POST("usuarios")
    suspend fun registrarUsuario(@Body usuario: Usuario): Response<Unit>

    // Ruta para crear un nuevo huerto (coincide con app.post('/huertos', ...) en tu API)
    @POST("huertos")
    suspend fun crearHuerto(@Body huerto: Huerto): Response<Unit>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: String): Response<Usuario>


}