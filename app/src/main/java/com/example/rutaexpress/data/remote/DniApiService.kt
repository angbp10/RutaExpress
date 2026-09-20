package com.example.rutaexpress.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * DTO para la solicitud de consulta de DNI.
 */
data class DniRequest(
    val dni: String,
)

/**
 * DTO para la respuesta de la API de DNI.
 */
data class DniResponse(
    val success: Boolean? = false,
    val data: DniData? = null,
    val message: String? = null,
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
 * Interfaz de Retrofit para consultar datos de DNI mediante la API json.pe.
 */
interface DniApiService {
    @Headers(
        "Authorization: Bearer 4f6b4b487171804a46683cf5b6ac096402e22b0409444e4fff9b475962d7",
        "Content-Type: application/json",
    )
    @POST("api/dni")
    suspend fun consultarDni(
        @Body request: DniRequest,
    ): Response<DniResponse>
}
