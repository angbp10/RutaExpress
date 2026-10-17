package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la consulta y gestión de reservas en Firestore (HU11 / HU18).
 */
class ReservaRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val reservasCollection by lazy { firestore.collection("reservas") }
    private val asientosCollection by lazy { firestore.collection("asientos") }

    /**
     * Busca una reserva en Firestore por Código (ej: REX-50168) o por DNI de pasajero.
     */
    suspend fun buscarReservaPorCodigoODni(query: String): Result<Reserva?> {
        return runCatching {
            val qTrim = query.trim()
            if (qTrim.isBlank()) return@runCatching null

            // 1. Intentar búsqueda exacta por Código de Reserva
            val snapshotCodigo = reservasCollection
                .whereEqualTo("codigoReserva", qTrim.uppercase())
                .get()
                .await()

            if (!snapshotCodigo.isEmpty) {
                return@runCatching snapshotCodigo.documents.firstNotNullOfOrNull { it.toObject(Reserva::class.java) }
            }

            // 2. Si es 5 dígitos o código sin guion, intentar con prefijo "REX-"
            if (!qTrim.startsWith("REX-", ignoreCase = true) && qTrim.length == 5) {
                val snapshotRex = reservasCollection
                    .whereEqualTo("codigoReserva", "REX-$qTrim")
                    .get()
                    .await()
                if (!snapshotRex.isEmpty) {
                    return@runCatching snapshotRex.documents.firstNotNullOfOrNull { it.toObject(Reserva::class.java) }
                }
            }

            // 3. Buscar por DNI de pasajero
            val snapshotTodas = reservasCollection
                .get()
                .await()

            val reservaDni = snapshotTodas.documents
                .mapNotNull { doc -> doc.toObject(Reserva::class.java) }
                .find { reserva ->
                    reserva.pasajeros.any { it.dni.equals(qTrim, ignoreCase = true) }
                }

            reservaDni
        }
    }

    /**
     * Confirma el pago de la reserva en taquilla (HU11):
     * - Cambia estado a "PAGADO"
     * - Registra medio de pago ("Efectivo" o "Yape") y fechaPago
     * - Actualiza los asientos en Firestore a estado "ocupado" permanente
     */
    suspend fun confirmarPagoReserva(reservaId: String, medioPago: String): Result<Unit> {
        return runCatching {
            require(reservaId.isNotBlank()) { "ID de reserva inválido" }
            val batch = firestore.batch()

            val reservaRef = reservasCollection.document(reservaId)
            val reservaSnapshot = reservaRef.get().await()
            require(reservaSnapshot.exists()) { "Reserva no encontrada en Firestore" }

            val reserva = reservaSnapshot.toObject(Reserva::class.java)
                ?: throw IllegalStateException("Error al leer la reserva")

            val ahora = System.currentTimeMillis()

            // 1. Actualizar reserva
            batch.update(
                reservaRef, mapOf(
                    "estado" to "PAGADO",
                    "medioPago" to medioPago,
                    "fechaPago" to ahora
                )
            )

            // 2. Buscar y actualizar asientos a "ocupado" permanente
            if (reserva.idViaje.isNotBlank() && reserva.asientos.isNotEmpty()) {
                val asientosSnapshot = asientosCollection
                    .whereEqualTo("idViaje", reserva.idViaje)
                    .get()
                    .await()

                asientosSnapshot.documents.forEach { doc ->
                    val num = doc.getLong("numero")?.toInt() ?: -1
                    if (reserva.asientos.contains(num)) {
                        batch.update(
                            doc.reference, mapOf(
                                "estado" to "ocupado",
                                "reservaExpiraAt" to 0L,
                                "idReserva" to reserva.id
                            )
                        )
                    }
                }
            }

            batch.commit().await()
            Unit
        }
    }
}
