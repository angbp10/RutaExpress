package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para las Rutas y Horarios de Turismo Azañero.
 */
data class RutaHorario(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("origen") @set:PropertyName("origen") var origen: String = "",
    @get:PropertyName("destino") @set:PropertyName("destino") var destino: String = "",
    @get:PropertyName("fecha") @set:PropertyName("fecha") var fecha: String = "",
    @get:PropertyName("horaSalida") @set:PropertyName("horaSalida") var horaSalida: String = "",
    @get:PropertyName("flotaAsignada") @set:PropertyName("flotaAsignada") var flotaAsignada: String = "",
    @get:PropertyName("conductorId") @set:PropertyName("conductorId") var conductorId: String = "",
    @get:PropertyName("conductorNombre") @set:PropertyName("conductorNombre") var conductorNombre: String = "",
    @get:PropertyName("costoPasaje") @set:PropertyName("costoPasaje") var costoPasaje: Double = 0.0,
    @get:PropertyName("asientosTotales") @set:PropertyName("asientosTotales") var asientosTotales: Int = 15,
    @get:PropertyName("asientosDisponibles") @set:PropertyName("asientosDisponibles") var asientosDisponibles: Int = 15,
)
