package com.example.rutaexpress.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * DTO para la respuesta de la API de DNI en PeruAPI.
 */
data class DniResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val data: DniData? = null,
    val numero: String? = null,
    val nombres: String? = null,
    @SerializedName("apellido_paterno")
    val apellidoPaterno: String? = null,
    @SerializedName("apellido_materno")
    val apellidoMaterno: String? = null,
)

/**
 * Datos del ciudadano devueltos por la API.
 */
data class DniData(
    val numero: String? = null,
    val nombres: String? = null,
    @SerializedName("apellido_paterno")
    val apellidoPaterno: String? = null,
    @SerializedName("apellido_materno")
    val apellidoMaterno: String? = null,
)

/**
 * Interfaz de Retrofit para consultar datos de DNI mediante PeruAPI.
 */
interface DniApiService {
    @Headers("X-API-KEY: da9dcefba1797bd30b1471b90d4217d4")
    @GET("api/dni/{dni}")
    suspend fun consultarDni(
        @Path("dni") dni: String,
    ): Response<DniResponse>
}
