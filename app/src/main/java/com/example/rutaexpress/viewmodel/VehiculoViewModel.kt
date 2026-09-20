package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Vehiculo
import com.example.rutaexpress.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehiculoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel encargado del CRUD de la Flotas de Vehículos en tiempo real.
 */
class VehiculoViewModel(
    private val repository: VehiculoRepository = VehiculoRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoUiState())
    val uiState: StateFlow<VehiculoUiState> = _uiState.asStateFlow()

    // Flujo en tiempo real de vehículos proveniente de Firestore
    val vehiculosState: StateFlow<List<Vehiculo>> = repository.getVehiculosFlow()
        .catch { ex ->
            _uiState.update { it.copy(errorMessage = "Error en vehículos: ${ex.localizedMessage}") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun registrarVehiculo(vehiculo: Vehiculo) {
        if (vehiculo.placa.isBlank() || vehiculo.modelo.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La placa y el modelo son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.registrarVehiculo(vehiculo)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Vehículo registrado con éxito.") }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Error al registrar vehículo.") }
            }
        }
    }

    fun actualizarVehiculo(id: String, vehiculo: Vehiculo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.actualizarVehiculo(id, vehiculo)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Vehículo actualizado con éxito.") }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Error al actualizar vehículo.") }
            }
        }
    }

    fun eliminarVehiculo(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.eliminarVehiculo(id)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Vehículo eliminado con éxito.") }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Error al eliminar vehículo.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
