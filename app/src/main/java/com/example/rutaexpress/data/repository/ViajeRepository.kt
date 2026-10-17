package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Asiento
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.model.UsuarioConductor
import com.example.rutaexpress.data.model.Viaje
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión de Viajes y la consulta de Conductores desde Firestore.
 */
class ViajeRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val viajesCollection by lazy { firestore.collection("viajes") }
    private val asientosCollection by lazy { firestore.collection("asientos") }
    private val usersCollection by lazy { firestore.collection("users") }

    /**
     * Escucha en tiempo real la colección de viajes.
     */
    fun getViajesFlow(): Flow<List<Viaje>> = callbackFlow {
        val listener = viajesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Viaje::class.java)
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Consulta en tiempo real la colección "users" filtrando únicamente usuarios con rol "conductor".
     */
    fun getConductoresFlow(): Flow<List<UsuarioConductor>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("role", "conductor")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val conductor = doc.toObject(UsuarioConductor::class.java)
                        conductor?.apply {
                            if (uid.isBlank()) {
                                uid = doc.id
                            }
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Registra un nuevo Viaje y genera automáticamente en un WriteBatch N documentos de asientos.
     */
    suspend fun registrarViajeConAsientos(viaje: Viaje, capacidad: Int): Result<String> {
        return runCatching {
            val batch = firestore.batch()

            val numAsientos = if (capacidad > 0) capacidad else 15
            viaje.asientosTotales = numAsientos
            viaje.asientosDisponibles = numAsientos
            viaje.asientosOcupados = 0

            // 1. Asignar ID e insertar documento del viaje
            val viajeRef = viajesCollection.document()
            viaje.id = viajeRef.id
            batch.set(viajeRef, viaje)

            // 2. Generar N asientos (del 1 al número de capacidad)
            for (i in 1..numAsientos) {
                val asientoRef = asientosCollection.document()
                val asiento = Asiento(
                    id = asientoRef.id,
                    idViaje = viaje.id,
                    numero = i,
                    estado = "disponible",
                )
                batch.set(asientoRef, asiento)
            }

            // 3. Ejecutar atómicamente el batch en Firestore
            batch.commit().await()

            viaje.id
        }
    }

    suspend fun actualizarViaje(viaje: Viaje): Result<Unit> {
        return runCatching {
            require(viaje.id.isNotBlank()) { "ID de viaje inválido" }
            viajesCollection.document(viaje.id).set(viaje).await()
            Unit
        }
    }

    suspend fun actualizarEstadoViaje(viajeId: String, nuevoEstado: String): Result<Unit> {
        return runCatching {
            require(viajeId.isNotBlank()) { "ID de viaje inválido" }
            viajesCollection.document(viajeId).update("estado", nuevoEstado).await()
            Unit
        }
    }

    suspend fun eliminarViaje(id: String): Result<Unit> {
        return runCatching {
            require(id.isNotBlank()) { "ID de viaje inválido" }
            viajesCollection.document(id).delete().await()
            Unit
        }
    }

    /**
     * Registra una Reserva Temporal en Firestore (HU18) asociando:
     * - ID del Viaje y asientos seleccionados
     * - Lista de pasajeros (DNI y Nombres)
     * - Código único REX-XXXXX
     * - Estado "RESERVADO" y marca de tiempo de expiración de EXACTAMENTE 5 MINUTOS
     * - Monto total
     */
    suspend fun crearReservaTemporal(reserva: Reserva, asientosIds: List<String>): Result<Reserva> {
        return runCatching {
            val batch = firestore.batch()

            // 1. Crear documento de la reserva
            val reservaRef = firestore.collection("reservas").document()
            reserva.id = reservaRef.id
            if (reserva.codigoReserva.isBlank()) {
                val aleatorio = (10000..99999).random()
                reserva.codigoReserva = "REX-$aleatorio"
            }
            val ahora = System.currentTimeMillis()
            reserva.createdAt = ahora
            reserva.expiresAt = ahora + (5 * 60 * 1000) // 5 minutos exactos

            batch.set(reservaRef, reserva)

            // 2. Actualizar estado de los asientos seleccionados
            asientosIds.forEach { asientoId ->
                val asientoRef = asientosCollection.document(asientoId)
                batch.update(
                    asientoRef, mapOf(
                        "estado" to "reservado",
                        "reservaExpiraAt" to reserva.expiresAt,
                        "idReserva" to reserva.id
                    )
                )
            }

            // 3. Actualizar conteo de asientos en el viaje si existe
            if (reserva.idViaje.isNotBlank()) {
                val viajeRef = viajesCollection.document(reserva.idViaje)
                val snapshot = viajeRef.get().await()
                if (snapshot.exists()) {
                    val ocupados = snapshot.getLong("asientosOcupados")?.toInt() ?: 0
                    val totales = snapshot.getLong("asientosTotales")?.toInt() ?: 15
                    val nuevosOcupados = ocupados + asientosIds.size
                    val nuevosDisponibles = (totales - nuevosOcupados).coerceAtLeast(0)
                    batch.update(
                        viajeRef, mapOf(
                            "asientosOcupados" to nuevosOcupados,
                            "asientosDisponibles" to nuevosDisponibles
                        )
                    )
                }
            }

            batch.commit().await()
            reserva
        }
    }
}
