package com.example.tfg.data.network


import com.example.tfg.data.model.Usuario
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.AuthResponse
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.RespuestaBusquedaPlantas
import retrofit2.Response
import retrofit2.http.*

interface IApiService {

    // Este es el endpoint clave: Envías el UID de Firebase y recibes el JWT
    @POST("api/auth/login-app")
    suspend fun loginConServidor(@Body request: LoginRequest): Response<AuthResponse>

    // Obtener perfil del usuario actual (protegido por JWT)
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: String): Response<Usuario>

    // Actualizar datos del usuario
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: String, @Body usuario: Usuario): Response<Usuario>


    // Obtener todos los huertos del usuario logueado
    @GET("api/huertos")
    suspend fun obtenerHuertos(): Response<List<Huerto>>

    @POST("api/huertos")
    suspend fun crearHuerto(@Body huerto: Huerto): Response<Huerto>

    @DELETE("api/huertos/{id}")
    suspend fun borrarHuerto(@Path("id") id: String): Response<Unit>

     @GET("species-list")
        suspend fun buscarPlantas(
            @Query("key") apiKey: String,
            @Query("q") consulta: String
        ): RespuestaBusquedaPlantas

    @POST("api/huertos/{id}/cultivos")
    suspend fun aniadirCultivo(@Path("id") huertoId: String, @Body cultivo: Cultivo): Response<Unit>

    @GET("api/huertos/{id}/cultivos")
    suspend fun obtenerCultivosDelHuerto(@Path("id") huertoId: String): Response<List<Cultivo>>

    @GET("api/catalogo/buscar")
    suspend fun buscarEnCatalogo(@Query("nombre") nombre: String): List<CatalogoDePlantas>

    @DELETE("api/huertos/{huertoId}/cultivos/{cultivoId}")
    suspend fun eliminarCultivo(
        @Header("Authorization") token: String,
        @Path("huertoId") huertoId: String,
        @Path("cultivoId") cultivoId: String
    ): Response<Unit>

    }



