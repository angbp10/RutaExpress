package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Asiento
import com.example.rutaexpress.data.model.Viaje
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión de Viajes y la generación automática de Asientos con WriteBatch en Firestore.
 */
class ViajeRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val viajesCollection by lazy { firestore.collection("viajes") }
    private val asientosCollection by lazy { firestore.collection("asientos") }

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
     * Registra un nuevo Viaje y genera automáticamente en un WriteBatch N documentos de asientos.
     */
    suspend fun registrarViajeConAsientos(viaje: Viaje, capacidad: Int): Result<String> {
        return runCatching {
            val batch = firestore.batch()

            // 1. Asignar ID e insertar documento del viaje
            val viajeRef = viajesCollection.document()
            viaje.id = viajeRef.id
            batch.set(viajeRef, viaje)

            // 2. Generar N asientos (del 1 al número de capacidad)
            val numAsientos = if (capacidad > 0) capacidad else 15
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

    suspend fun eliminarViaje(id: String): Result<Unit> {
        return runCatching {
            require(id.isNotBlank()) { "ID de viaje inválido" }
            viajesCollection.document(id).delete().await()
            Unit
        }
    }
}
