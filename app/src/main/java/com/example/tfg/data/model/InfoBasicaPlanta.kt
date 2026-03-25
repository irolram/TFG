package com.example.tfg.data.model
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class InfoBasicaPlanta(
    @SerializedName("id") val id: Int,

    @SerializedName("common_name") val nombreComun: String?,

    @SerializedName("scientific_name") val nombreCientifico: List<String>?,

    @SerializedName("watering") val riego: String?,

    @JsonAdapter(SunlightDeserializer::class)
    @SerializedName("sunlight") val luzSolar: List<String>?,

    @SerializedName("default_image") val imagenPredeterminada: ImagenPlanta?
)

class SunlightDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<String> {
        return when {
            json.isJsonArray -> json.asJsonArray.map { it.asString }
            json.isJsonPrimitive -> listOf(json.asString)
            else -> emptyList()
        }
    }
}