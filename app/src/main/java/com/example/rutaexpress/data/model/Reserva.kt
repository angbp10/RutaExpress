package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para Reservas Temporales de Pago en Agencia (HU18).
 */
data class Reserva(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("codigoReserva") @set:PropertyName("codigoReserva") var codigoReserva: String = "",
    @get:PropertyName("idViaje") @set:PropertyName("idViaje") var idViaje: String = "",
    @get:PropertyName("origen") @set:PropertyName("origen") var origen: String = "",
    @get:PropertyName("destino") @set:PropertyName("destino") var destino: String = "",
    @get:PropertyName("fechaViaje") @set:PropertyName("fechaViaje") var fechaViaje: String = "",
    @get:PropertyName("horaSalida") @set:PropertyName("horaSalida") var horaSalida: String = "",
    @get:PropertyName("asientos") @set:PropertyName("asientos") var asientos: List<Int> = emptyList(),
    @get:PropertyName("pasajeros") @set:PropertyName("pasajeros") var pasajeros: List<Pasajero> = emptyList(),
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String = "RESERVADO",
    @get:PropertyName("montoTotal") @set:PropertyName("montoTotal") var montoTotal: Double = 0.0,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("expiresAt") @set:PropertyName("expiresAt") var expiresAt: Long = System.currentTimeMillis() + (5 * 60 * 1000)
)
