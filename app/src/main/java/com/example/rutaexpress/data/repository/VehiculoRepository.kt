package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Vehiculo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión CRUD y sincronización en tiempo real de Vehículos (Flota) en Firestore.
 */
class VehiculoRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val collectionRef by lazy { firestore.collection("vehiculos") }

    /**
     * Escucha en tiempo real la colección "vehiculos".
     */
    fun getVehiculosFlow(): Flow<List<Vehiculo>> = callbackFlow {
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Vehiculo::class.java)
                // Si la colección está vacía, sembrar datos iniciales
                if (list.isEmpty()) {
                    seedVehiculos()
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    private fun seedVehiculos() {
        val initialList = listOf(
            Vehiculo(placa = "M4D-960", marca = "Toyota", modelo = "HiAce", anio = 2022, capacidad = 15, estado = "activo"),
            Vehiculo(placa = "B2C-400", marca = "Mercedes-Benz", modelo = "Sprinter", anio = 2023, capacidad = 20, estado = "activo"),
            Vehiculo(placa = "C7X-120", marca = "Hyundai", modelo = "H350", anio = 2021, capacidad = 15, estado = "mantenimiento"),
        )
        initialList.forEach { vehiculo ->
            val docRef = collectionRef.document()
            vehiculo.id = docRef.id
            docRef.set(vehiculo)
        }
    }

    suspend fun registrarVehiculo(vehiculo: Vehiculo): Result<Unit> {
        return runCatching {
            val docRef = collectionRef.document()
            vehiculo.id = docRef.id
            docRef.set(vehiculo).await()
            Unit
        }
    }

    suspend fun actualizarVehiculo(id: String, vehiculo: Vehiculo): Result<Unit> {
        return runCatching {
            require(id.isNotBlank()) { "ID de vehículo inválido" }
            vehiculo.id = id
            collectionRef.document(id).set(vehiculo).await()
            Unit
        }
    }

    suspend fun eliminarVehiculo(id: String): Result<Unit> {
        return runCatching {
            require(id.isNotBlank()) { "ID de vehículo inválido" }
            collectionRef.document(id).delete().await()
            Unit
        }
    }
}
