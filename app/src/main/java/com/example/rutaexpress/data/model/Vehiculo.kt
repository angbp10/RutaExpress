package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para los Vehículos (Flota) de Turismo Azañero.
 */
data class Vehiculo(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("placa") @set:PropertyName("placa") var placa: String = "",
    @get:PropertyName("marca") @set:PropertyName("marca") var marca: String = "",
    @get:PropertyName("modelo") @set:PropertyName("modelo") var modelo: String = "",
    @get:PropertyName("anio") @set:PropertyName("anio") var anio: Int = 0,
    @get:PropertyName("capacidad") @set:PropertyName("capacidad") var capacidad: Int = 0,
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String = "activo", // "activo", "mantenimiento", "inactivo"
    @get:PropertyName("ultimaRevision") @set:PropertyName("ultimaRevision") var ultimaRevision: Long = System.currentTimeMillis(),
)
