package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Asiento
import com.example.rutaexpress.data.model.Pasajero
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.data.repository.DniRepository
import com.example.rutaexpress.data.repository.ReservaRepository
import com.example.rutaexpress.data.repository.ViajeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Estado del formulario de entrada de un pasajero.
 */
data class PasajeroInputState(
    val asientoNumero: Int = 0,
    val dni: String = "",
    val nombres: String = "",
    val isLoadingDni: Boolean = false,
    val errorDni: String? = null,
)

/**
 * Estado UI reactivo para la pantalla de Selección de Asientos.
 */
data class SeleccionAsientosUiState(
    val viaje: Viaje? = null,
    val asientosList: List<Asiento> = emptyList(),
    val asientosSeleccionadosIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isReservaBloqueada: Boolean = false,
    val bloqueoMensaje: String = "",
    // HU18 state
    val mostrarDialogoPasajeros: Boolean = false,
    val pasajerosInputs: List<PasajeroInputState> = emptyList(),
    val reservaCreada: Reserva? = null,
    val mostrarDialogoResumen: Boolean = false,
)

/**
 * ViewModel encargado del flujo de Selección de Asientos en tiempo real desde Firestore.
 */
class SeleccionAsientosViewModel : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val dniRepository by lazy { DniRepository() }
    private val viajeRepository by lazy { ViajeRepository() }

    private val _uiState = MutableStateFlow(SeleccionAsientosUiState())
    val uiState: StateFlow<SeleccionAsientosUiState> = _uiState.asStateFlow()

    private var currentViajeId: String = ""
    private var isRecepcionistaMode: Boolean = false

    private fun evaluarBloqueo(viajeObj: Viaje?, esRecepcionista: Boolean): Pair<Boolean, String> {
        if (viajeObj == null) return Pair(false, "")
        return if (esRecepcionista) {
            // Recepcionista: Solo se bloquea si la hora actual es estrictamente posterior a la hora de salida del bus
            val salidaDt = ReservaRepository.parseSalidaDateTime(viajeObj.fecha, viajeObj.horaSalida)
            val passed = salidaDt != null && LocalDateTime.now().isAfter(salidaDt)
            val mensaje = "Este bus ya ha cumplido su hora de salida programada. No es posible asignar o vender nuevos pasajes para esta ruta."
            Pair(passed, if (passed) mensaje else "")
        } else {
            // Cliente: Se bloquea si faltan 5 minutos o menos o ya pasó
            val mins = ReservaRepository.calcularMinutosRestantesSalida(viajeObj.fecha, viajeObj.horaSalida)
            val blocked = mins != null && mins <= 5
            val mensaje = "Las reservas en línea cierran 5 minutos antes de la partida. Acércate a taquilla."
            Pair(blocked, if (blocked) mensaje else "")
        }
    }

    /**
     * Carga el viaje y escucha en tiempo real la colección de asientos.
     * Evalúa reglas diferenciadas por rol (Cliente vs Recepcionista).
     */
    fun cargarViajeYAsientos(viajeId: String, esRecepcionista: Boolean = false) {
        if (viajeId.isBlank()) return
        currentViajeId = viajeId
        isRecepcionistaMode = esRecepcionista
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // 1. Escuchar viaje en tiempo real
        firestore.collection("viajes").document(viajeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar viaje: ${error.localizedMessage}"
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val viajeObj = snapshot.toObject(Viaje::class.java)
                    val (bloqueado, mensaje) = evaluarBloqueo(viajeObj, isRecepcionistaMode)
                    _uiState.update {
                        it.copy(
                            viaje = viajeObj,
                            isReservaBloqueada = bloqueado,
                            bloqueoMensaje = mensaje
                        )
                    }
                }
            }

        // 2. Escuchar asientos en tiempo real
        firestore.collection("asientos")
            .whereEqualTo("idViaje", viajeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar asientos: ${error.localizedMessage}"
                        )
                    }
                    return@addSnapshotListener
                }

                val asientosRecuperados = snapshot?.toObjects(Asiento::class.java).orEmpty()
                val ahora = System.currentTimeMillis()

                _uiState.update { state ->
                    val viajeObj = state.viaje
                    val totales = viajeObj?.asientosTotales ?: 15

                    val (bloqueado, mensaje) = evaluarBloqueo(viajeObj, isRecepcionistaMode)

                    // Evaluar expiración de 5 min: si está reservado pero expiró, considerar disponible
                    val asientosProcesados = asientosRecuperados.map { asiento ->
                        if (asiento.estado.equals("reservado", ignoreCase = true) &&
                            asiento.reservaExpiraAt > 0 && ahora > asiento.reservaExpiraAt
                        ) {
                            asiento.copy(estado = "disponible", reservaExpiraAt = 0L, idReserva = "")
                        } else {
                            asiento
                        }
                    }

                    // Generar lista final
                    val listaFinal = if (asientosProcesados.isNotEmpty()) {
                        asientosProcesados.sortedBy { it.numero }
                    } else {
                        (1..totales).map { num ->
                            Asiento(
                                id = "asiento_$num",
                                idViaje = viajeId,
                                numero = num,
                                estado = "disponible"
                            )
                        }
                    }

                    // Limpiar seleccionados que hayan cambiado a ocupado/reservado activo
                    val idsOcupados = listaFinal
                        .filter { asiento ->
                            val esOcupado = asiento.estado.lowercase() == "ocupado"
                            val esReservadoActivo = asiento.estado.lowercase() == "reservado" &&
                                    (asiento.reservaExpiraAt == 0L || ahora <= asiento.reservaExpiraAt)
                            esOcupado || esReservadoActivo
                        }
                        .map { it.id }
                        .toSet()

                    val seleccionadosLimpios = if (bloqueado) emptySet() else state.asientosSeleccionadosIds
                        .filterNot { idsOcupados.contains(it) }
                        .toSet()

                    state.copy(
                        asientosList = listaFinal,
                        asientosSeleccionadosIds = seleccionadosLimpios,
                        isReservaBloqueada = bloqueado,
                        bloqueoMensaje = mensaje,
                        isLoading = false
                    )
                }
            }
    }

    /**
     * Alterna la selección de un asiento disponible.
     */
    fun toggleSeleccionAsiento(asiento: Asiento) {
        val state = _uiState.value
        if (state.isReservaBloqueada) return

        val ahora = System.currentTimeMillis()
        val esDisponible = asiento.estado.lowercase() == "disponible" ||
                (asiento.estado.lowercase() == "reservado" && asiento.reservaExpiraAt > 0 && ahora > asiento.reservaExpiraAt)

        if (!esDisponible) return

        _uiState.update { st ->
            val actuales = st.asientosSeleccionadosIds.toMutableSet()
            if (actuales.contains(asiento.id)) {
                actuales.remove(asiento.id)
            } else {
                actuales.add(asiento.id)
            }
            st.copy(asientosSeleccionadosIds = actuales)
        }
    }

    /**
     * Prepara e inicia el flujo de HU18 (Ingreso de pasajeros para reserva temporal).
     */
    fun abrirRegistroPasajeros() {
        val state = _uiState.value
        if (state.isReservaBloqueada || state.asientosSeleccionadosIds.isEmpty()) return

        val asientosSeleccionados = state.asientosList
            .filter { state.asientosSeleccionadosIds.contains(it.id) }
            .sortedBy { it.numero }

        val inputs = asientosSeleccionados.map { asiento ->
            PasajeroInputState(asientoNumero = asiento.numero)
        }

        _uiState.update {
            it.copy(
                mostrarDialogoPasajeros = true,
                pasajerosInputs = inputs
            )
        }
    }

    fun cerrarRegistroPasajeros() {
        _uiState.update { it.copy(mostrarDialogoPasajeros = false) }
    }

    fun onPasajeroDniChanged(index: Int, nuevoDni: String) {
        if (nuevoDni.length > 8) return
        _uiState.update { state ->
            val nuevaLista = state.pasajerosInputs.toMutableList()
            if (index in nuevaLista.indices) {
                nuevaLista[index] = nuevaLista[index].copy(dni = nuevoDni, errorDni = null)
            }
            state.copy(pasajerosInputs = nuevaLista)
        }
    }

    fun onPasajeroNombresChanged(index: Int, nuevosNombres: String) {
        _uiState.update { state ->
            val nuevaLista = state.pasajerosInputs.toMutableList()
            if (index in nuevaLista.indices) {
                nuevaLista[index] = nuevaLista[index].copy(nombres = nuevosNombres)
            }
            state.copy(pasajerosInputs = nuevaLista)
        }
    }

    /**
     * Consulta el DNI de un pasajero mediante Retrofit / DniRepository.
     */
    fun consultarDniPasajero(index: Int) {
        val state = _uiState.value
        if (index !in state.pasajerosInputs.indices) return
        val input = state.pasajerosInputs[index]
        if (input.dni.length != 8) {
            _uiState.update { st ->
                val lista = st.pasajerosInputs.toMutableList()
                lista[index] = lista[index].copy(errorDni = "DNI debe tener 8 dígitos")
                st.copy(pasajerosInputs = lista)
            }
            return
        }

        _uiState.update { st ->
            val lista = st.pasajerosInputs.toMutableList()
            lista[index] = lista[index].copy(isLoadingDni = true, errorDni = null)
            st.copy(pasajerosInputs = lista)
        }

        viewModelScope.launch {
            val result = dniRepository.consultarDni(input.dni)
            result.onSuccess { data ->
                val nombreCompleto = listOfNotNull(
                    data.nombres,
                    data.apellidoPaterno,
                    data.apellidoMaterno
                ).joinToString(" ").trim()

                _uiState.update { st ->
                    val lista = st.pasajerosInputs.toMutableList()
                    if (index in lista.indices) {
                        lista[index] = lista[index].copy(
                            nombres = if (nombreCompleto.isNotBlank()) nombreCompleto else lista[index].nombres,
                            isLoadingDni = false
                        )
                    }
                    st.copy(pasajerosInputs = lista)
                }
            }.onFailure { ex ->
                _uiState.update { st ->
                    val lista = st.pasajerosInputs.toMutableList()
                    if (index in lista.indices) {
                        lista[index] = lista[index].copy(
                            isLoadingDni = false,
                            errorDni = ex.localizedMessage ?: "Error al consultar DNI"
                        )
                    }
                    st.copy(pasajerosInputs = lista)
                }
            }
        }
    }

    /**
     * Procesa y registra la Reserva Temporal de 5 minutos en Firestore (HU18).
     */
    fun procesarReservaTemporal() {
        val state = _uiState.value
        if (state.isReservaBloqueada) {
            _uiState.update {
                it.copy(
                    mostrarDialogoPasajeros = false,
                    errorMessage = state.bloqueoMensaje.ifBlank { "Este viaje no se encuentra disponible." }
                )
            }
            return
        }
        val viaje = state.viaje ?: return
        val seleccionadosIds = state.asientosSeleccionadosIds
        if (seleccionadosIds.isEmpty()) return

        // Validar que todos los campos de pasajeros estén completos
        val incompletos = state.pasajerosInputs.any { it.dni.length != 8 || it.nombres.isBlank() }
        if (incompletos) {
            _uiState.update {
                it.copy(errorMessage = "Por favor, ingresa DNI (8 dígitos) y Nombres para cada pasajero.")
            }
            return
        }

        val pasajeros = state.pasajerosInputs.map {
            Pasajero(
                dni = it.dni.trim(),
                nombres = it.nombres.trim(),
                asientoNumero = it.asientoNumero
            )
        }

        val asientosNumeros = state.asientosList
            .filter { seleccionadosIds.contains(it.id) }
            .map { it.numero }
            .sorted()

        val montoTotal = viaje.precio * seleccionadosIds.size

        val reserva = Reserva(
            idViaje = viaje.id,
            origen = viaje.origen,
            destino = viaje.destino,
            fechaViaje = viaje.fecha,
            horaSalida = viaje.horaSalida,
            asientos = asientosNumeros,
            pasajeros = pasajeros,
            estado = "RESERVADO",
            montoTotal = montoTotal
        )

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = viajeRepository.crearReservaTemporal(reserva, seleccionadosIds.toList())
            result.onSuccess { reservaCreada ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mostrarDialogoPasajeros = false,
                        mostrarDialogoResumen = true,
                        reservaCreada = reservaCreada,
                        asientosSeleccionadosIds = emptySet(),
                        successMessage = "¡Reserva temporal creada con éxito!"
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear reserva: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    fun cerrarDialogoResumen() {
        _uiState.update { it.copy(mostrarDialogoResumen = false, reservaCreada = null) }
    }

    /**
     * Confirma la reserva de los asientos elegidos directamente (método anterior mantenido por compatibilidad).
     */
    fun confirmarReserva(onSuccess: () -> Unit) {
        val current = _uiState.value
        val seleccionados = current.asientosSeleccionadosIds
        if (seleccionados.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                seleccionados.forEach { asientoId ->
                    val ref = firestore.collection("asientos").document(asientoId)
                    batch.set(
                        ref, mapOf(
                            "id" to asientoId,
                            "idViaje" to currentViajeId,
                            "estado" to "ocupado"
                        ), SetOptions.merge()
                    )
                }

                val viaje = current.viaje
                if (viaje != null) {
                    val viajeRef = firestore.collection("viajes").document(viaje.id)
                    val nuevosOcupados = viaje.asientosOcupados + seleccionados.size
                    val nuevosDisponibles = (viaje.asientosTotales - nuevosOcupados).coerceAtLeast(0)
                    batch.update(
                        viajeRef, mapOf(
                            "asientosOcupados" to nuevosOcupados,
                            "asientosDisponibles" to nuevosDisponibles
                        )
                    )
                }

                batch.commit().addOnSuccessListener {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "¡Asientos reservados con éxito!",
                            asientosSeleccionadosIds = emptySet()
                        )
                    }
                    onSuccess()
                }.addOnFailureListener { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al reservar asientos: ${ex.localizedMessage}"
                        )
                    }
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error en el proceso de reserva: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
