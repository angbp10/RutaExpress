package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.RutaHorario
import com.example.rutaexpress.data.repository.RutaHorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para las pantallas de Recepcionista, Cliente y Conductor.
 */
data class RutaHorarioUiState(
    val rutas: List<RutaHorario> = emptyList(),
    val searchOrigen: String = "",
    val searchDestino: String = "",
    val searchFecha: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel encargado del CRUD de Rutas/Horarios y filtrado por Rol.
 */
class RutaHorarioViewModel(
    private val repository: RutaHorarioRepository = RutaHorarioRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(RutaHorarioUiState())
    val uiState: StateFlow<RutaHorarioUiState> = _uiState.asStateFlow()

    init {
        loadAllRutas()
    }

    /**
     * Carga la lista completa de rutas disponibles.
     */
    fun loadAllRutas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getRutas()
            result.onSuccess { list ->
                _uiState.update { it.copy(rutas = list, isLoading = false, hasSearched = false) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar rutas: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Búsqueda/Filtrado para el cliente según Origen, Destino y Fecha.
     */
    fun searchPasajes(origen: String, destino: String, fecha: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    searchOrigen = origen,
                    searchDestino = destino,
                    searchFecha = fecha,
                    errorMessage = null,
                )
            }
            val result = repository.searchRutas(origen, destino, fecha)
            result.onSuccess { list ->
                _uiState.update {
                    it.copy(rutas = list, isLoading = false, hasSearched = true)
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error en la búsqueda: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Filtrado para conductor: Carga únicamente las rutas asignadas.
     */
    fun loadConductorRutas(conductorNombreOrId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getRutasByConductor(conductorNombreOrId)
            result.onSuccess { list ->
                _uiState.update { it.copy(rutas = list, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar rutas del conductor: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Funcionalidad CRUD para Recepcionista: Crear Ruta/Horario.
     */
    fun createRuta(
        origen: String,
        destino: String,
        fecha: String,
        horaSalida: String,
        flotaAsignada: String,
        conductorNombre: String,
        costoPasaje: Double,
    ) {
        if (origen.isBlank() || destino.isBlank() || fecha.isBlank() || horaSalida.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa origen, destino, fecha y hora.") }
            return
        }

        val nuevaRuta = RutaHorario(
            origen = origen,
            destino = destino,
            fecha = fecha,
            horaSalida = horaSalida,
            flotaAsignada = flotaAsignada.ifBlank { "Combi Azañero" },
            conductorNombre = conductorNombre.ifBlank { "Sin asignar" },
            costoPasaje = costoPasaje,
            asientosTotales = 15,
            asientosDisponibles = 15,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.createRuta(nuevaRuta)
            result.onSuccess {
                _uiState.update { it.copy(successMessage = "Ruta creada con éxito.") }
                loadAllRutas()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear la ruta: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Funcionalidad CRUD para Recepcionista: Editar Ruta/Horario.
     */
    fun updateRuta(rutaActualizada: RutaHorario) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateRuta(rutaActualizada)
            result.onSuccess {
                _uiState.update { it.copy(successMessage = "Ruta actualizada correctamente.") }
                loadAllRutas()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al actualizar ruta: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Funcionalidad CRUD para Recepcionista: Eliminar Ruta/Horario.
     */
    fun deleteRuta(rutaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteRuta(rutaId)
            result.onSuccess {
                _uiState.update { it.copy(successMessage = "Ruta eliminada correctamente.") }
                loadAllRutas()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al eliminar ruta: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
