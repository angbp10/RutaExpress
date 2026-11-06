package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ValidacionPagosUiState(
    val reservasEnRevision: List<Reserva> = emptyList(),
    val isLoading: Boolean = true,
    val isProcessingId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ValidacionPagosViewModel(
    private val reservaRepository: ReservaRepository = ReservaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValidacionPagosUiState())
    val uiState: StateFlow<ValidacionPagosUiState> = _uiState.asStateFlow()

    init {
        escucharPagosEnRevision()
    }

    private fun escucharPagosEnRevision() {
        viewModelScope.launch {
            reservaRepository.obtenerReservasEnRevisionFlow()
                .catch { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al escuchar pagos en revisión: ${ex.localizedMessage}",
                        )
                    }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(
                            reservasEnRevision = lista,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun aprobarPago(reservaId: String) {
        if (reservaId.isBlank()) return
        _uiState.update { it.copy(isProcessingId = reservaId, errorMessage = null) }
        viewModelScope.launch {
            val result = reservaRepository.aprobarPagoReserva(reservaId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isProcessingId = null,
                        successMessage = "¡Pago aprobado y reserva confirmada con éxito!",
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isProcessingId = null,
                        errorMessage = "Error al aprobar pago: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun rechazarPago(reservaId: String) {
        if (reservaId.isBlank()) return
        _uiState.update { it.copy(isProcessingId = reservaId, errorMessage = null) }
        viewModelScope.launch {
            val result = reservaRepository.rechazarPagoReserva(reservaId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isProcessingId = null,
                        successMessage = "Pago rechazado correctamente.",
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isProcessingId = null,
                        errorMessage = "Error al rechazar pago: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
