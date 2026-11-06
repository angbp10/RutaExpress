package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.remote.DniData
import com.example.rutaexpress.data.remote.RetrofitClient

/**
 * Repositorio encargado de la consulta de DNI mediante json.pe.
 */
class DniRepository {
    suspend fun consultarDni(dni: String): Result<DniData> {
        return runCatching {
            val response = RetrofitClient.dniApiService.consultarDni(dni)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val dataObj = body.data
                    val nombres = dataObj?.nombres ?: dataObj?.nombre ?: body.nombres ?: body.nombre
                    val paterno = dataObj?.apellidoPaterno ?: dataObj?.paterno ?: body.apellidoPaterno ?: body.paterno
                    val materno = dataObj?.apellidoMaterno ?: dataObj?.materno ?: body.apellidoMaterno ?: body.materno
                    val numero = dataObj?.numero ?: dataObj?.dni ?: body.numero ?: body.dni ?: dni

                    if (!nombres.isNullOrBlank() || !paterno.isNullOrBlank()) {
                        DniData(
                            numero = numero,
                            nombres = nombres,
                            apellidoPaterno = paterno,
                            apellidoMaterno = materno,
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
