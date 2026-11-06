package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Repositorio para la consulta y gestión de reservas en Firestore (HU11 / HU18).
 */
class ReservaRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val reservasCollection by lazy { firestore.collection("reservas") }
    private val asientosCollection by lazy { firestore.collection("asientos") }

    companion object {
        /**
         * Parsea la fecha y hora de salida a un objeto LocalDateTime.
         */
        fun parseSalidaDateTime(fechaViaje: String, horaSalida: String): LocalDateTime? {
            return try {
                val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.forLanguageTag("es-ES"))
                val date = LocalDate.parse(fechaViaje.trim(), dateFormatter)

                val cleanHora = horaSalida.trim()
                    .replace("a. m.", "AM", ignoreCase = true)
                    .replace("p. m.", "PM", ignoreCase = true)
                    .replace("a.m.", "AM", ignoreCase = true)
                    .replace("p.m.", "PM", ignoreCase = true)
                    .replace(".", "")

                val time: LocalTime = if (cleanHora.contains("AM", ignoreCase = true) || cleanHora.contains("PM", ignoreCase = true)) {
                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                    LocalTime.parse(cleanHora.uppercase(), timeFormatter)
                } else {
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    LocalTime.parse(cleanHora, timeFormatter)
                }

                LocalDateTime.of(date, time)
            } catch (_: Exception) {
                null
            }
        }

        /**
         * Función de utilidad que compara LocalDateTime.now() con la fecha y hora combinada de la salida.
         * Calcula la diferencia en minutos: ChronoUnit.MINUTES.between(LocalDateTime.now(), horaSalida).
         */
        fun calcularMinutosRestantesSalida(fechaViaje: String, horaSalida: String): Long? {
            return try {
                val salidaDateTime = parseSalidaDateTime(fechaViaje, horaSalida) ?: return null
                ChronoUnit.MINUTES.between(LocalDateTime.now(), salidaDateTime)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Verifica si una reserva en estado "RESERVADO" cumple con el límite de 5 minutos previos a la salida.
     * Si faltan 5 minutos o menos (o si la hora ya pasó), marca la reserva como "EXPIRADO" en Firestore
     * y restablece el estado de los asientos a "disponible".
     */
    suspend fun verificarYExpirarReservaSiEsNecesario(reserva: Reserva): Reserva {
        if (!reserva.estado.equals("RESERVADO", ignoreCase = true)) {
            return reserva
        }

        val minutosRestantes = calcularMinutosRestantesSalida(reserva.fechaViaje, reserva.horaSalida)
        if (minutosRestantes != null && minutosRestantes <= 5) {
            try {
                val batch = firestore.batch()
                val reservaRef = reservasCollection.document(reserva.id)

                // 1. Marcar reserva como expirada
                batch.update(reservaRef, "estado", "EXPIRADO")

                // 2. Restablecer asientos en la colección del viaje a disponible
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
                                    "estado" to "disponible",
                                    "reservaExpiraAt" to 0L,
                                    "idReserva" to ""
                                )
                            )
                        }
                    }

                    // 3. Actualizar conteo de asientos en el documento del viaje
                    val viajeRef = firestore.collection("viajes").document(reserva.idViaje)
                    val viajeSnap = viajeRef.get().await()
                    if (viajeSnap.exists()) {
                        val ocupados = viajeSnap.getLong("asientosOcupados")?.toInt() ?: 0
                        val totales = viajeSnap.getLong("asientosTotales")?.toInt() ?: 15
                        val nuevosOcupados = (ocupados - reserva.asientos.size).coerceAtLeast(0)
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
                return reserva.copy(estado = "EXPIRADO")
            } catch (_: Exception) {
                // Si falla la red, mantener estado en memoria
            }
        }
        return reserva
    }

    /**
     * Busca una reserva en Firestore por Código (ej: REX-50168) o por DNI de pasajero.
     */
    suspend fun buscarReservaPorCodigoODni(query: String): Result<Reserva?> {
        return runCatching {
            val qTrim = query.trim()
            if (qTrim.isBlank()) return@runCatching null

            var reservaEncontrada: Reserva? = null

            // 1. Intentar búsqueda exacta por Código de Reserva
            val snapshotCodigo = reservasCollection
                .whereEqualTo("codigoReserva", qTrim.uppercase())
                .get()
                .await()

            if (!snapshotCodigo.isEmpty) {
                reservaEncontrada = snapshotCodigo.documents.firstNotNullOfOrNull { it.toObject(Reserva::class.java) }
            }

            // 2. Si es 5 dígitos o código sin guion, intentar con prefijo "REX-"
            if (reservaEncontrada == null && !qTrim.startsWith("REX-", ignoreCase = true) && qTrim.length == 5) {
                val snapshotRex = reservasCollection
                    .whereEqualTo("codigoReserva", "REX-$qTrim")
                    .get()
                    .await()
                if (!snapshotRex.isEmpty) {
                    reservaEncontrada = snapshotRex.documents.firstNotNullOfOrNull { it.toObject(Reserva::class.java) }
                }
            }

            // 3. Buscar por DNI de pasajero si aún no se encuentra
            if (reservaEncontrada == null) {
                val snapshotTodas = reservasCollection
                    .get()
                    .await()

                reservaEncontrada = snapshotTodas.documents
                    .mapNotNull { doc -> doc.toObject(Reserva::class.java) }
                    .find { reserva ->
                        reserva.pasajeros.any { it.dni.equals(qTrim, ignoreCase = true) }
                    }
            }

            // Verificar expiración por tiempo de salida si se encontró
            if (reservaEncontrada != null) {
                reservaEncontrada = verificarYExpirarReservaSiEsNecesario(reservaEncontrada)
            }

            reservaEncontrada
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

            val reservaVerificada = verificarYExpirarReservaSiEsNecesario(reserva)
            if (reservaVerificada.estado.equals("EXPIRADO", ignoreCase = true)) {
                throw IllegalStateException("No se puede confirmar el pago: la reserva ha expirado porque faltan menos de 5 minutos para la salida del vehículo.")
            }

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

    /**
     * Obtiene una reserva específica por su ID de Firestore.
     */
    suspend fun obtenerReservaPorId(reservaId: String): Result<Reserva?> {
        return runCatching {
            require(reservaId.isNotBlank()) { "ID de reserva no válido" }
            val doc = reservasCollection.document(reservaId).get().await()
            doc.toObject(Reserva::class.java)
        }
    }

    /**
     * Envía la información de pago de la reserva para validación por recepción (HU17):
     * Actualiza numeroOperacion, metodoPago y estadoPago = "EN_REVISION".
     */
    suspend fun enviarPagoAValidacion(
        reservaId: String,
        numeroOperacion: String,
        metodoPago: String
    ): Result<Unit> {
        return runCatching {
            require(reservaId.isNotBlank()) { "ID de reserva no válido" }
            require(numeroOperacion.isNotBlank()) { "El número de operación no puede estar vacío" }
            val cleanOp = numeroOperacion.trim()
            require(cleanOp.length in 6..8 && cleanOp.all { it.isDigit() }) {
                "El número de operación debe tener entre 6 y 8 dígitos numéricos"
            }

            reservasCollection.document(reservaId).update(
                mapOf(
                    "numeroOperacion" to cleanOp,
                    "metodoPago" to metodoPago,
                    "medioPago" to metodoPago,
                    "estadoPago" to "EN_REVISION"
                )
            ).await()
            Unit
        }
    }

    /**
     * Escucha en tiempo real todas las reservas que se encuentren en estadoPago == "EN_REVISION" (HU10).
     */
    fun obtenerReservasEnRevisionFlow(): Flow<List<Reserva>> = callbackFlow {
        val listener = reservasCollection
            .whereEqualTo("estadoPago", "EN_REVISION")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reservas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Reserva::class.java)?.apply {
                        if (id.isBlank()) id = doc.id
                    }
                } ?: emptyList()

                // Ordenar por más recientes primero
                trySend(reservas.sortedByDescending { it.createdAt })
            }

        awaitClose { listener.remove() }
    }

    /**
     * Aprueba el pago por transferencia de la reserva (HU10):
     * Actualiza estadoPago = "PAGADO", estado = "PAGADO" y marca asientos como ocupados en Firestore.
     */
    suspend fun aprobarPagoReserva(reservaId: String): Result<Unit> {
        return runCatching {
            require(reservaId.isNotBlank()) { "ID de reserva no válido" }
            val batch = firestore.batch()
            val reservaRef = reservasCollection.document(reservaId)
            val reservaSnap = reservaRef.get().await()
            require(reservaSnap.exists()) { "Reserva no encontrada" }

            val reserva = reservaSnap.toObject(Reserva::class.java)
                ?: throw IllegalStateException("Error al leer datos de la reserva")

            val ahora = System.currentTimeMillis()

            batch.update(
                reservaRef, mapOf(
                    "estadoPago" to "PAGADO",
                    "estado" to "PAGADO",
                    "fechaPago" to ahora
                )
            )

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

    /**
     * Rechaza el pago por transferencia de la reserva (HU10):
     * Actualiza estadoPago = "RECHAZADO".
     */
    suspend fun rechazarPagoReserva(reservaId: String): Result<Unit> {
        return runCatching {
            require(reservaId.isNotBlank()) { "ID de reserva no válido" }
            reservasCollection.document(reservaId).update(
                mapOf(
                    "estadoPago" to "RECHAZADO"
                )
            ).await()
            Unit
        }
    }

    /**
     * Valida de manera estricta el boleto escaneado por el personal y registra el abordaje (HU29).
     *
     * Flujo de validaciones:
     * 1. ¿Existe el documento en Firestore?
     * 2. ¿Pertenece al idViaje del turno seleccionado?
     * 3. ¿El estadoPago es "PAGADO"?
     * 4. ¿El estadoAbordaje ya es "ABORDADO"?
     * 5. Si pasa todo: Actualiza estadoAbordaje = "ABORDADO" en Firestore.
     */
    suspend fun validarYMarcarAbordaje(
        idBoletoEscaneado: String,
        idViajeTurnoActual: String = ""
    ): Result<Reserva> {
        return runCatching {
            val cleanId = idBoletoEscaneado.trim()
            require(cleanId.isNotBlank()) { "El código o ID escaneado está vacío." }

            // 1. ¿Existe en Firestore?
            val docRef = reservasCollection.document(cleanId)
            var docSnap = docRef.get().await()

            // Si se escaneó por códigoReserva (ej: REX-12345), buscar el documento por codigoReserva
            if (!docSnap.exists()) {
                val querySnap = reservasCollection
                    .whereEqualTo("codigoReserva", cleanId.uppercase())
                    .get()
                    .await()

                if (!querySnap.isEmpty) {
                    docSnap = querySnap.documents.first()
                }
            }

            if (!docSnap.exists()) {
                throw Exception("Boleto no encontrado / inválido.")
            }

            val reserva = docSnap.toObject(Reserva::class.java)?.apply {
                if (id.isBlank()) id = docSnap.id
            } ?: throw Exception("Error al decodificar la información del boleto.")

            // 2. ¿Es del turno correcto?
            if (idViajeTurnoActual.isNotBlank() && reserva.idViaje.isNotBlank()) {
                if (reserva.idViaje != idViajeTurnoActual) {
                    throw Exception("Este boleto pertenece a otro turno o ruta.")
                }
            }

            // 3. ¿Está pagado?
            if (!reserva.estadoPago.equals("PAGADO", ignoreCase = true) && !reserva.estado.equals("PAGADO", ignoreCase = true)) {
                throw Exception("El boleto no ha sido pagado (Estado: ${reserva.estadoPago.ifBlank { reserva.estado }}).")
            }

            // 4. ¿Ya se usó?
            if (reserva.estadoAbordaje.equals("ABORDADO", ignoreCase = true)) {
                throw Exception("¡Alerta! Este boleto ya fue escaneado y utilizado previamente.")
            }

            // 5. ¡ÉXITO! Marcar estadoAbordaje = "ABORDADO" en Firestore
            docSnap.reference.update("estadoAbordaje", "ABORDADO").await()
            reserva.copy(estadoAbordaje = "ABORDADO")
        }
    }
}
