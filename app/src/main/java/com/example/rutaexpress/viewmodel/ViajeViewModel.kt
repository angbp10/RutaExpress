package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.data.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViajeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel para la gestión de Viajes/Rutas y generación automática de asientos.
 */
class ViajeViewModel(
    private val repository: ViajeRepository = ViajeRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViajeUiState())
    val uiState: StateFlow<ViajeUiState> = _uiState.asStateFlow()

    val viajesState: StateFlow<List<Viaje>> = repository.getViajesFlow()
        .catch { ex ->
            _uiState.update { it.copy(errorMessage = "Error en viajes: ${ex.localizedMessage}") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun crearViajeConAsientos(viaje: Viaje, capacidadVehiculo: Int) {
        if (viaje.origen.isBlank() || viaje.destino.isBlank() || viaje.fecha.isBlank() || viaje.horaSalida.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa los datos obligatorios del viaje.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.registrarViajeConAsientos(viaje, capacidadVehiculo)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Ruta creada correctamente y asientos generados automáticamente.",
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.localizedMessage ?: "Error al crear la ruta.",
                    )
                }
            }
        }
    }

    fun actualizarViaje(viajeActualizado: Viaje) {
        if (viajeActualizado.origen.isBlank() || viajeActualizado.destino.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Origen y destino son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.actualizarViaje(viajeActualizado)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Ruta/Viaje actualizado con éxito.",
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.localizedMessage ?: "Error al actualizar la ruta.",
                    )
                }
            }
        }
    }

    fun eliminarViaje(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.eliminarViaje(id)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Viaje eliminado.") }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
