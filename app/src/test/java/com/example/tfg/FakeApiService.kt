package com.example.tfg

import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.RespuestaAuth
import com.example.tfg.data.model.RespuestaBusquedaPlantas
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.StatsDashboard
import com.example.tfg.data.model.Ticket
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.IApiService
import retrofit2.Response

class FakeApiService : IApiService {
    var huertosResponse: Response<List<Huerto>> = Response.success(emptyList())
    var crearHuertoResponse: Response<Huerto> = Response.success(
        Huerto(id = "nuevo", nombre = "Nuevo", descripcion = "Creado")
    )
    var borrarHuertoResponse: Response<Unit> = Response.success(Unit)
    var cultivosResponse: Response<List<Cultivo>> = Response.success(emptyList())
    var eliminarCultivoResponse: Response<Unit> = Response.success(Unit)
    var registrarRiegoResponse: Response<Cultivo> = Response.success(cultivo())
    var catalogoResponse: Response<List<CatalogoDePlantas>> = Response.success(emptyList())
    var buscarCatalogoResponse: Response<List<CatalogoDePlantas>> = Response.success(emptyList())
    var ticketsResponse: Response<List<Ticket>> = Response.success(emptyList())
    var resolverTicketResponse: Response<Ticket> = Response.success(ticket())
    var crearTicketResponse: Response<Ticket> = Response.success(ticket(id = "ticket-creado"))

    var obtenerHuertosException: Exception? = null
    var buscarCatalogoException: Exception? = null
    var registrarRiegoException: Exception? = null

    val huertosCreados = mutableListOf<Huerto>()
    val cultivosAniadidos = mutableListOf<Cultivo>()
    val ticketsCreados = mutableListOf<Ticket>()
    val tokensEliminarCultivo = mutableListOf<String>()
    val idsHuertoRecargados = mutableListOf<String>()
    var listarTicketsCalls = 0
    var resolverTicketCalls = 0

    override suspend fun loginConServidor(request: LoginRequest): Response<RespuestaAuth> {
        error("No usado en estos tests")
    }

    override suspend fun obtenerUsuarioPorId(id: String): Response<Usuario> {
        error("No usado en estos tests")
    }

    override suspend fun actualizarUsuario(id: String, usuario: Usuario): Response<Usuario> {
        error("No usado en estos tests")
    }

    override suspend fun listarUsuarios(): Response<List<Usuario>> {
        error("No usado en estos tests")
    }

    override suspend fun actualizarRol(id: String, nuevoRol: Rol): Response<Usuario> {
        error("No usado en estos tests")
    }

    override suspend fun eliminarUsuario(id: String): Response<Unit> {
        error("No usado en estos tests")
    }

    override suspend fun obtenerHuertos(): Response<List<Huerto>> {
        obtenerHuertosException?.let { throw it }
        return huertosResponse
    }

    override suspend fun crearHuerto(huerto: Huerto): Response<Huerto> {
        huertosCreados += huerto
        return crearHuertoResponse
    }

    override suspend fun borrarHuerto(id: String): Response<Unit> = borrarHuertoResponse

    override suspend fun obtenerCultivosDelHuerto(huertoId: String): Response<List<Cultivo>> {
        idsHuertoRecargados += huertoId
        return cultivosResponse
    }

    override suspend fun aniadirCultivo(huertoId: String, cultivo: Cultivo): Response<Unit> {
        cultivosAniadidos += cultivo
        return Response.success(Unit)
    }

    override suspend fun eliminarCultivo(
        token: String,
        huertoId: String,
        cultivoId: String
    ): Response<Unit> {
        tokensEliminarCultivo += token
        return eliminarCultivoResponse
    }

    override suspend fun registrarRiego(cultivoId: String): Response<Cultivo> {
        registrarRiegoException?.let { throw it }
        return registrarRiegoResponse
    }

    override suspend fun obtenerTodoElCatalogo(): Response<List<CatalogoDePlantas>> {
        return catalogoResponse
    }

    override suspend fun buscarEnCatalogo(nombre: String): Response<List<CatalogoDePlantas>> {
        buscarCatalogoException?.let { throw it }
        return buscarCatalogoResponse
    }

    override suspend fun buscarPlantasExternas(
        apiKey: String,
        consulta: String
    ): RespuestaBusquedaPlantas {
        error("No usado en estos tests")
    }

    override suspend fun obtenerEstadisticas(): Response<StatsDashboard> {
        error("No usado en estos tests")
    }

    override suspend fun obtenerConteoProximidad(
        lat: Double,
        lng: Double,
        radio: Double
    ): Response<Long> {
        error("No usado en estos tests")
    }

    override suspend fun listarTickets(): Response<List<Ticket>> {
        listarTicketsCalls++
        return ticketsResponse
    }

    override suspend fun resolverTicket(id: String): Response<Ticket> {
        resolverTicketCalls++
        return resolverTicketResponse
    }

    override suspend fun crearTicket(ticket: Ticket): Response<Ticket> {
        ticketsCreados += ticket
        return crearTicketResponse
    }
}

fun planta(nombre: String = "Tomate") = CatalogoDePlantas(
    id = 1,
    nombre = nombre,
    nombreCientifico = "Solanum lycopersicum",
    riego = null,
    luzSolar = null,
    icono = null,
    instrucciones = null,
    diasCrecimiento = 80,
    temporadaIdeal = "Primavera",
    profundidadSiembra = "1 cm",
    distanciaEntrePlantas = "40 cm"
)

fun huerto(id: String = "huerto-1", nombre: String = "Terraza") = Huerto(
    id = id,
    nombre = nombre,
    descripcion = "Huerto de prueba",
    latitud = 40.0,
    longitud = -3.0
)

fun cultivo(id: String = "cultivo-1", nombre: String = "Tomate") = Cultivo(
    id = id,
    nombre = nombre,
    estado = "PLANTADO",
    fechaPlantacion = 1_700_000_000_000,
    huertoId = "huerto-1",
    apodo = nombre,
    infoCatalogo = planta(nombre)
)

fun ticket(id: String? = "ticket-1") = Ticket(
    id = id,
    usuarioNombre = "Ana Verde",
    usuarioId = "user-1",
    asunto = "Problema",
    descripcion = "No carga el mapa",
    tipo = com.example.tfg.data.model.TipoTicket.ERROR
)
