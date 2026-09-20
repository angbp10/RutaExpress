package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.RutaHorario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión CRUD de Rutas y Horarios en Firestore.
 */
class RutaHorarioRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val collectionRef by lazy { firestore.collection("rutas_horarios") }

    /**
     * Obtiene todas las rutas creadas. Si la base de datos está vacía, registra datos iniciales.
     */
    suspend fun getRutas(): Result<List<RutaHorario>> {
        return runCatching {
            val snapshot = collectionRef.get().await()
            val list = snapshot.toObjects(RutaHorario::class.java)

            if (list.isEmpty()) {
                val seedList = getInitialSeedData()
                seedList.forEach { ruta ->
                    val docRef = collectionRef.document()
                    ruta.id = docRef.id
                    docRef.set(ruta).await()
                }
                seedList
            } else {
                list
            }
        }
    }

    /**
     * Filtra las rutas por Origen, Destino y opcionalmente Fecha.
     */
    suspend fun searchRutas(origen: String, destino: String, fecha: String): Result<List<RutaHorario>> {
        return runCatching {
            val allRutas = getRutas().getOrDefault(emptyList())
            allRutas.filter { ruta ->
                val matchOrigen = origen.isBlank() || ruta.origen.equals(origen, ignoreCase = true)
                val matchDestino = destino.isBlank() || ruta.destino.equals(destino, ignoreCase = true)
                val matchFecha = fecha.isBlank() || ruta.fecha.equals(fecha, ignoreCase = true)
                matchOrigen && matchDestino && matchFecha
            }
        }
    }

    /**
     * Obtiene únicamente las rutas asignadas al conductor especificado.
     */
    suspend fun getRutasByConductor(conductorQuery: String): Result<List<RutaHorario>> {
        return runCatching {
            val allRutas = getRutas().getOrDefault(emptyList())
            allRutas.filter { ruta ->
                ruta.conductorId.equals(conductorQuery, ignoreCase = true) ||
                        ruta.conductorNombre.contains(conductorQuery, ignoreCase = true)
            }
        }
    }

    /**
     * Registra una nueva ruta/horario en Firestore.
     */
    suspend fun createRuta(ruta: RutaHorario): Result<Unit> {
        return runCatching {
            val docRef = collectionRef.document()
            ruta.id = docRef.id
            docRef.set(ruta).await()
            Unit
        }
    }

    /**
     * Actualiza una ruta existente en Firestore.
     */
    suspend fun updateRuta(ruta: RutaHorario): Result<Unit> {
        return runCatching {
            require(ruta.id.isNotBlank()) { "ID de la ruta no válido" }
            collectionRef.document(ruta.id).set(ruta).await()
            Unit
        }
    }

    /**
     * Elimina una ruta en Firestore según su ID.
     */
    suspend fun deleteRuta(rutaId: String): Result<Unit> {
        return runCatching {
            require(rutaId.isNotBlank()) { "ID de la ruta no válido" }
            collectionRef.document(rutaId).delete().await()
            Unit
        }
    }

    /**
     * Datos semilla regionales para Turismo Azañero en Cajamarca y región.
     */
    private fun getInitialSeedData(): List<RutaHorario> {
        return listOf(
            RutaHorario(
                origen = "Cajamarca",
                destino = "Celendín",
                fecha = "20/05/2025",
                horaSalida = "06:00 AM",
                flotaAsignada = "Combi Azañero #01",
                conductorId = "cond_01",
                conductorNombre = "Juan Pérez",
                costoPasaje = 20.0,
                asientosTotales = 15,
                asientosDisponibles = 10,
            ),
            RutaHorario(
                origen = "Cajamarca",
                destino = "Chachapoyas",
                fecha = "20/05/2025",
                horaSalida = "08:30 AM",
                flotaAsignada = "Bus Expreso #04",
                conductorId = "cond_02",
                conductorNombre = "Carlos Mendoza",
                costoPasaje = 45.0,
                asientosTotales = 20,
                asientosDisponibles = 12,
            ),
            RutaHorario(
                origen = "Celendín",
                destino = "Chachapoyas",
                fecha = "21/05/2025",
                horaSalida = "10:00 AM",
                flotaAsignada = "Combi Azañero #02",
                conductorId = "cond_01",
                conductorNombre = "Juan Pérez",
                costoPasaje = 30.0,
                asientosTotales = 15,
                asientosDisponibles = 8,
            )
        )
    }
}
