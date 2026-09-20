package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para los Asientos de un viaje en Turismo Azañero.
 */
data class Asiento(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("idViaje") @set:PropertyName("idViaje") var idViaje: String = "",
    @get:PropertyName("numero") @set:PropertyName("numero") var numero: Int = 1,
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String = "disponible", // "disponible", "seleccionado", "reservado"
)
