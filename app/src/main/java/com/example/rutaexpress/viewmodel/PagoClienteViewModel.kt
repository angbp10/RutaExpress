package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PagoClienteUiState(
    val reservaId: String = "",
    val reserva: Reserva? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val metodoSeleccionado: String = "Yape",
    val numeroOperacion: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

class PagoClienteViewModel(
    private val reservaRepository: ReservaRepository = ReservaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PagoClienteUiState())
    val uiState: StateFlow<PagoClienteUiState> = _uiState.asStateFlow()

    fun cargarReserva(reservaId: String) {
        if (reservaId.isBlank()) return
        _uiState.update { it.copy(reservaId = reservaId, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = reservaRepository.obtenerReservaPorId(reservaId)
            result.onSuccess { reserva ->
                _uiState.update { it.copy(reserva = reserva, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar la reserva: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun onMetodoPagoChanged(metodo: String) {
        _uiState.update { it.copy(metodoSeleccionado = metodo) }
    }

    fun onNumeroOperacionChanged(numero: String) {
        val clean = numero.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(numeroOperacion = clean, errorMessage = null) }
    }

    fun enviarPagoAValidacion() {
        val current = _uiState.value
        val numOp = current.numeroOperacion.trim()

        if (numOp.length !in 6..8) {
            _uiState.update { it.copy(errorMessage = "El N° de Operación debe tener entre 6 y 8 dígitos numéricos.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = reservaRepository.enviarPagoAValidacion(
                reservaId = current.reservaId,
                numeroOperacion = numOp,
                metodoPago = current.metodoSeleccionado,
            )

            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Error al registrar pago: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
