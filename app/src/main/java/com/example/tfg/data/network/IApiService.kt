package com.example.tfg.data.network


import com.example.tfg.data.model.Usuario
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.RespuestaAuth
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.RespuestaBusquedaPlantas
import com.example.tfg.data.model.Rol
import retrofit2.Response
import retrofit2.http.*

// Esta interfaz sirve para definir los endpoints de la API
interface IApiService {

    // EndPoint dirigido a la autenticación, devuelve un token de acceso (JWT)
    @POST("api/auth/login-app")
    suspend fun loginConServidor(@Body request: LoginRequest): Response<RespuestaAuth>

    // Obtiene el Id del usuario logueado
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: String): Response<Usuario>

    // Actualiza los datos del usuario
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: String, @Body usuario: Usuario): Response<Usuario>


    // Obtener todos los huertos del usuario logueado
    @GET("api/huertos")
    suspend fun obtenerHuertos(): Response<List<Huerto>>

    // Crear un nuevo huerto
    @POST("api/huertos")
    suspend fun crearHuerto(@Body huerto: Huerto): Response<Huerto>

    //Borra un huerto
    @DELETE("api/huertos/{id}")
    suspend fun borrarHuerto(@Path("id") id: String): Response<Unit>

    // Busca plantas en el catálogo
     @GET("species-list")
        suspend fun buscarPlantas(
            @Query("key") apiKey: String,
            @Query("q") consulta: String
        ): RespuestaBusquedaPlantas

     // Añade cultivos
    @POST("api/huertos/{id}/cultivos")
    suspend fun aniadirCultivo(@Path("id") huertoId: String, @Body cultivo: Cultivo): Response<Unit>

    // Elimina cultivos
    @DELETE("api/huertos/{huertoId}/cultivos/{cultivoId}")
    suspend fun eliminarCultivo(
        @Header("Authorization") token: String,
        @Path("huertoId") huertoId: String,
        @Path("cultivoId") cultivoId: String
    ): Response<Unit>

    // Obtiene los cultivos de un huerto
    @GET("api/huertos/{id}/cultivos")
    suspend fun obtenerCultivosDelHuerto(@Path("id") huertoId: String): Response<List<Cultivo>>

    // Busca plantas en el catálogo
    @GET("api/catalogo/buscar")
    suspend fun buscarEnCatalogo(@Query("nombre") nombre: String): List<CatalogoDePlantas>

    @GET("api/catalogo")
    suspend fun obtenerTodoElCatalogo(): List<CatalogoDePlantas>

    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") idObjetivo: String,
        @Query("nuevoRol") nuevoRol: String
    ): Response<String>

        @GET("api/usuarios")
        suspend fun listarUsuarios(): Response<List<Usuario>>
        @DELETE("api/usuarios/{id}")
        suspend fun eliminarUsuario(@Path("id") id: String): Response<Unit>

        @PUT("api/usuarios/{id}/rol")
        suspend fun actualizarRol(
            @Path("id") idObjetivo: String,   // El ID que va en la URL {id}
            @Query("nuevoRol") nuevoRol: Rol // El parámetro que va después del ? (ej: ?nuevoRol=MOD)
        ): Response<String>
}





