package com.example.rutaexpress.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * DTO para la respuesta de la API de DNI en json.pe.
 */
data class DniResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val data: DniData? = null,
    val numero: String? = null,
    @SerializedName("dni")
    val dni: String? = null,
    val nombres: String? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("apellido_paterno")
    val apellidoPaterno: String? = null,
    @SerializedName("paterno")
    val paterno: String? = null,
    @SerializedName("apellido_materno")
    val apellidoMaterno: String? = null,
    @SerializedName("materno")
    val materno: String? = null,
)

/**
 * Datos del ciudadano devueltos por la API.
 */
data class DniData(
    val numero: String? = null,
    @SerializedName("dni")
    val dni: String? = null,
    val nombres: String? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("apellido_paterno")
    val apellidoPaterno: String? = null,
    @SerializedName("paterno")
    val paterno: String? = null,
    @SerializedName("apellido_materno")
    val apellidoMaterno: String? = null,
    @SerializedName("materno")
    val materno: String? = null,
)

/**
 * Interfaz de Retrofit para consultar datos de DNI mediante json.pe.
 */
interface DniApiService {
    @Headers(
        "Authorization: Bearer 42ae355cdaec3023e02fbedfd7d43ebfd939c6867578bf5e0759ec2a5506",
        "X-API-KEY: 42ae355cdaec3023e02fbedfd7d43ebfd939c6867578bf5e0759ec2a5506",
    )
    @GET("api/dni/{dni}")
    suspend fun consultarDni(
        @Path("dni") dni: String,
        @Query("token") token: String = "42ae355cdaec3023e02fbedfd7d43ebfd939c6867578bf5e0759ec2a5506",
    ): Response<DniResponse>
}
