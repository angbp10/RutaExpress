package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para mapear los usuarios con rol "conductor" de Firestore.
 */
data class UsuarioConductor(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("nombres") @set:PropertyName("nombres") var nombres: String = "",
    @get:PropertyName("apellido_paterno") @set:PropertyName("apellido_paterno") var apellido_paterno: String = "",
    @get:PropertyName("apellido_materno") @set:PropertyName("apellido_materno") var apellido_materno: String = "",
) {
    val nombreCompleto: String
        get() = listOf(nombres, apellido_paterno, apellido_materno)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}
