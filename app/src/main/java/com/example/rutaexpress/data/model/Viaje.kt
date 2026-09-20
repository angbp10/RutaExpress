package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para los Viajes / Rutas creados.
 */
data class Viaje(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("origen") @set:PropertyName("origen") var origen: String = "",
    @get:PropertyName("destino") @set:PropertyName("destino") var destino: String = "",
    @get:PropertyName("idVehiculo") @set:PropertyName("idVehiculo") var idVehiculo: String = "",
    @get:PropertyName("vehiculoDescripcion") @set:PropertyName("vehiculoDescripcion") var vehiculoDescripcion: String = "",
    @get:PropertyName("conductor") @set:PropertyName("conductor") var conductor: String = "",
    @get:PropertyName("fecha") @set:PropertyName("fecha") var fecha: String = "",
    @get:PropertyName("horaSalida") @set:PropertyName("horaSalida") var horaSalida: String = "",
    @get:PropertyName("precio") @set:PropertyName("precio") var precio: Double = 0.0,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
)
