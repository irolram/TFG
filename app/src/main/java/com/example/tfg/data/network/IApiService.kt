package com.example.tfg.data.network


import com.example.tfg.data.model.Usuario
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.RespuestaAuth
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.RespuestaBusquedaPlantas
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.StatsDashboard
import com.example.tfg.data.model.Ticket
import retrofit2.Response
import retrofit2.http.*


interface IApiService {

    // ==========================================
    // 🔐 AUTENTICACIÓN
    // ==========================================

    @POST("api/auth/login-app")
    suspend fun loginConServidor(@Body request: LoginRequest): Response<RespuestaAuth>


    // ==========================================
    // 👤 GESTIÓN DE USUARIOS (Perfil Propio y Admin)
    // ==========================================

    // El usuario ve su propio perfil / El Admin ve el perfil de cualquiera
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: String): Response<Usuario>

    // El usuario actualiza sus propios datos (nombre, apellidos...)
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: String, @Body usuario: Usuario): Response<Usuario>

    // SOLO ADMIN/MOD: Listar todos los usuarios del sistema
    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<Usuario>>

    // SOLO ADMIN/MOD: Cambiar el rango de un usuario
        @PUT("/api/usuarios/{id}/rol")
        suspend fun actualizarRol(
        @Path("id") id: String,
        @Query("nuevoRol") nuevoRol: Rol
        ): Response<Usuario>


    // SOLO ADMIN: Eliminar un usuario del sistema
    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: String): Response<Unit>


    // ==========================================
    // 🏡 GESTIÓN DE HUERTOS Y CULTIVOS
    // ==========================================

    @GET("api/huertos")
    suspend fun obtenerHuertos(): Response<List<Huerto>>

    @POST("api/huertos")
    suspend fun crearHuerto(@Body huerto: Huerto): Response<Huerto>

    @DELETE("api/huertos/{id}")
    suspend fun borrarHuerto(@Path("id") id: String): Response<Unit>

    @GET("api/huertos/{id}/cultivos")
    suspend fun obtenerCultivosDelHuerto(@Path("id") huertoId: String): Response<List<Cultivo>>

    @POST("api/huertos/{id}/cultivos")
    suspend fun aniadirCultivo(@Path("id") huertoId: String, @Body cultivo: Cultivo): Response<Unit>

    @DELETE("api/huertos/{huertoId}/cultivos/{cultivoId}")
    suspend fun eliminarCultivo(
        @Header("Authorization") token: String,
        @Path("huertoId") huertoId: String,
        @Path("cultivoId") cultivoId: String
    ): Response<Unit>


    // ==========================================
    // 🌿 CATÁLOGO DE PLANTAS
    // ==========================================

    @GET("api/catalogo")
    suspend fun obtenerTodoElCatalogo(): Response<List<CatalogoDePlantas>>

    @GET("api/catalogo/buscar")
    suspend fun buscarEnCatalogo(@Query("nombre") nombre: String): Response<List<CatalogoDePlantas>>

    // Endpoint externo (Perenual API u otra)
    @GET("species-list")
    suspend fun buscarPlantasExternas(
        @Query("key") apiKey: String,
        @Query("q") consulta: String
    ): RespuestaBusquedaPlantas


    @GET("api/admin/stats")
    suspend fun obtenerEstadisticas(): Response<StatsDashboard>

    @GET("api/admin/stats/proximidad")
    suspend fun obtenerConteoProximidad(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radio") radio: Double
    ): Response<Long>

    // ==========================================
    // GESTOR DE TICKETS
    // ==========================================

    @GET("api/tickets")
    suspend fun listarTickets(): Response<List<Ticket>>

    @PATCH("api/tickets/{id}/resolver")
    suspend fun resolverTicket(@Path("id") id: String): Response<Ticket>

    @POST("api/tickets")
    suspend fun crearTicket(@Body ticket: Ticket): Response<Ticket>


}