package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.data.repository.ReservaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val AGENCIAS_DISPONIBLES = listOf("Cajamarca", "Celendín", "Balsas", "Leymebamba", "Chachapoyas")

data class RutaPromocional(
    val tituloRuta: String,
    val descripcion: String,
    val mensajeConfianza: String,
)

val RUTAS_PROMOCIONALES = listOf(
    RutaPromocional(
        tituloRuta = "Cajamarca ➔ Celendín",
        descripcion = "Ruta Directa por Encañada y Polloc",
        mensajeConfianza = "Servicio Seguro y Puntual"
    ),
    RutaPromocional(
        tituloRuta = "Celendín ➔ Chachapoyas",
        descripcion = "Confort en cada kilómetro",
        mensajeConfianza = "Viaja tranquilo con Turismo Azañero"
    ),
    RutaPromocional(
        tituloRuta = "Celendín ➔ Leymebamba",
        descripcion = "Disfruta el paisaje con seguridad",
        mensajeConfianza = "Flota moderna y monitoreada"
    ),
    RutaPromocional(
        tituloRuta = "Celendín ➔ Balsas",
        descripcion = "Conexión rápida y directa",
        mensajeConfianza = "Servicio Interprovincial Directo"
    )
)

/**
 * Estado UI reactivo para la pantalla de Búsqueda de Cliente.
 */
data class BusquedaClienteUiState(
    val origen: String = "Cajamarca",
    val destino: String = "Celendín",
    val fechaSeleccionada: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val resultadosBusqueda: List<Viaje> = emptyList(),
    val haBuscado: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel que gestiona los filtros de origen, destino y fecha para la consulta de viajes disponibles.
 */
class BusquedaClienteViewModel : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow(BusquedaClienteUiState())
    val uiState: StateFlow<BusquedaClienteUiState> = _uiState.asStateFlow()

    fun onOrigenChanged(nuevoOrigen: String) {
        _uiState.update { it.copy(origen = nuevoOrigen) }
    }

    fun onDestinoChanged(nuevoDestino: String) {
        _uiState.update { it.copy(destino = nuevoDestino) }
    }

    private fun esFechaPasada(fechaStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                isLenient = false
            }
            val fechaSeleccionada = sdf.parse(fechaStr) ?: return false

            val calHoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calSeleccionada = Calendar.getInstance().apply {
                time = fechaSeleccionada
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            calSeleccionada.before(calHoy)
        } catch (e: Exception) {
            false
        }
    }

    fun onFechaChanged(nuevaFecha: String) {
        if (esFechaPasada(nuevaFecha)) {
            _uiState.update {
                it.copy(
                    errorMessage = "No puedes consultar salidas para fechas pasadas."
                )
            }
            return
        }
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha, errorMessage = null) }
    }

    /**
     * Intercambia los valores actuales de origen y destino.
     */
    fun intercambiarDestinos() {
        _uiState.update {
            it.copy(
                origen = it.destino,
                destino = it.origen,
            )
        }
    }

    fun seleccionarFechaHoy() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        _uiState.update { it.copy(fechaSeleccionada = sdf.format(Date())) }
    }

    fun seleccionarFechaManana() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        _uiState.update { it.copy(fechaSeleccionada = sdf.format(calendar.time)) }
    }

    /**
     * Realiza la consulta a la colección "viajes" en Firestore filtrando por origen, destino, fecha y estado EN VENTA.
     */
    fun buscarSalidas() {
        val current = _uiState.value
        if (esFechaPasada(current.fechaSeleccionada)) {
            _uiState.update {
                it.copy(
                    errorMessage = "No puedes consultar salidas para fechas pasadas.",
                    resultadosBusqueda = emptyList(),
                    haBuscado = true
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, haBuscado = true, errorMessage = null) }

        firestore.collection("viajes")
            .whereEqualTo("origen", current.origen)
            .whereEqualTo("destino", current.destino)
            .whereEqualTo("fecha", current.fechaSeleccionada)
            .whereEqualTo("estado", "EN VENTA")
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.toObjects(Viaje::class.java)
                _uiState.update {
                    it.copy(
                        resultadosBusqueda = lista,
                        isLoading = false,
                    )
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(
                        resultadosBusqueda = emptyList(),
                        isLoading = false,
                        errorMessage = "Error al buscar salidas: ${ex.localizedMessage}",
                    )
                }
            }
    }

    fun buscarTurnosDisponibles() {
        buscarSalidas()
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Compara LocalDateTime.now() con la fecha y hora de salida del viaje.
     * Retorna true si la salida está a 5 minutos o menos (o si ya pasó la hora).
     */
    fun esReservaBloqueada(viaje: Viaje): Boolean {
        val minutosRestantes = ReservaRepository.calcularMinutosRestantesSalida(viaje.fecha, viaje.horaSalida)
        return minutosRestantes != null && minutosRestantes <= 5
    }
}
