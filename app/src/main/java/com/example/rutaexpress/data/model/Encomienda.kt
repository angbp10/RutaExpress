package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de datos para representar una Encomienda en el sistema (HU09).
 */
data class Encomienda(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("guia") @set:PropertyName("guia") var guia: String = "",
    @get:PropertyName("remitente") @set:PropertyName("remitente") var remitente: String = "",
    @get:PropertyName("dniRemitente") @set:PropertyName("dniRemitente") var dniRemitente: String = "",
    @get:PropertyName("celularRemitente") @set:PropertyName("celularRemitente") var celularRemitente: String = "",
    @get:PropertyName("destinatario") @set:PropertyName("destinatario") var destinatario: String = "",
    @get:PropertyName("dniDestinatario") @set:PropertyName("dniDestinatario") var dniDestinatario: String = "",
    @get:PropertyName("celularDestinatario") @set:PropertyName("celularDestinatario") var celularDestinatario: String = "",
    @get:PropertyName("pesoKg") @set:PropertyName("pesoKg") var pesoKg: Double = 0.0,
    @get:PropertyName("tipoCarga") @set:PropertyName("tipoCarga") var tipoCarga: String = "Estándar",
    @get:PropertyName("tarifaTotal") @set:PropertyName("tarifaTotal") var tarifaTotal: Double = 0.0,
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String = "En almacén",
    @get:PropertyName("estadoActual") @set:PropertyName("estadoActual") var estadoActual: String = "En almacén",
    @get:PropertyName("ultimaActualizacion") @set:PropertyName("ultimaActualizacion") var ultimaActualizacion: Long = System.currentTimeMillis(),
    @get:PropertyName("historialMovimientos") @set:PropertyName("historialMovimientos") var historialMovimientos: List<Movimiento> = emptyList(),
    @ServerTimestamp
    var fechaEmision: Date? = null,
    @ServerTimestamp
    var fechaActualizacion: Date? = null,
)
