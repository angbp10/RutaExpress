package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.remote.DniData
import com.example.rutaexpress.data.remote.DniRequest
import com.example.rutaexpress.data.remote.RetrofitClient

/**
 * Repositorio encargado de la consulta de DNI.
 */
class DniRepository {
    suspend fun consultarDni(dni: String): Result<DniData> {
        return runCatching {
            val response = RetrofitClient.dniApiService.consultarDni(DniRequest(dni))
            if (response.isSuccessful) {
                val body = response.body()
                if ((body?.success == true) && (body.data != null)) {
                    body.data
                } else {
                    throw Exception(body?.message ?: "No se encontraron datos para el DNI ingresado.")
                }
            } else {
                throw Exception("Error al consultar DNI (${response.code()}): ${response.message()}")
            }
        }
    }
}
