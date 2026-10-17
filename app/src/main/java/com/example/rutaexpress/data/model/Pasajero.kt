package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para Pasajeros asociados a una reserva.
 */
data class Pasajero(
    @get:PropertyName("dni") @set:PropertyName("dni") var dni: String = "",
    @get:PropertyName("nombres") @set:PropertyName("nombres") var nombres: String = "",
    @get:PropertyName("asientoNumero") @set:PropertyName("asientoNumero") var asientoNumero: Int = 0
)
