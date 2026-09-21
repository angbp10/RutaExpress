package com.example.rutaexpress.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de datos para representar una Encomienda en el sistema.
 */
data class Encomienda(
    var id: String = "",
    val guia: String = "",
    val remitente: String = "",
    val dniRemitente: String = "",
    val celularRemitente: String = "",
    val destinatario: String = "",
    val dniDestinatario: String = "",
    val celularDestinatario: String = "",
    val pesoKg: Double = 0.0,
    val tipoCarga: String = "Estándar", // "Estándar", "Frágil", "Express"
    val tarifaTotal: Double = 0.0,
    val estado: String = "EN_ALMACEN",
    @ServerTimestamp
    val fechaEmision: Date? = null,
    @ServerTimestamp
    val fechaActualizacion: Date? = null
)
