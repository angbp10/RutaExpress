package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.remote.DniData
import com.example.rutaexpress.data.remote.RetrofitClient

/**
 * Repositorio encargado de la consulta de DNI mediante PeruAPI.
 */
class DniRepository {
    suspend fun consultarDni(dni: String): Result<DniData> {
        return runCatching {
            val response = RetrofitClient.dniApiService.consultarDni(dni)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.data != null) {
                        body.data
                    } else if (!body.nombres.isNullOrBlank() || !body.numero.isNullOrBlank()) {
                        DniData(
                            numero = body.numero ?: dni,
                            nombres = body.nombres,
                            apellidoPaterno = body.apellidoPaterno,
                            apellidoMaterno = body.apellidoMaterno
                        )
                    } else {
                        throw Exception(body.message ?: "No se encontraron datos para el DNI ingresado.")
                    }
                } else {
                    throw Exception("Respuesta vacía del servidor.")
                }
            } else {
                throw Exception("Error al consultar DNI (${response.code()}): ${response.message()}")
            }
        }
    }
}
