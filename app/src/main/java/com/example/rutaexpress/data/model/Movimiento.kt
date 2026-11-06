package com.example.rutaexpress.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para registrar cada cambio de estado en el historial del tracking de la encomienda (HU09).
 */
data class Movimiento(
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String = "En almacén",
    @get:PropertyName("fechaHora") @set:PropertyName("fechaHora") var fechaHora: Long = System.currentTimeMillis(),
    @get:PropertyName("nombreUsuario") @set:PropertyName("nombreUsuario") var nombreUsuario: String = "Personal de Agencia",
    @get:PropertyName("idUsuario") @set:PropertyName("idUsuario") var idUsuario: String = "SISTEMA",
)
