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

data class BoletoDigitalUiState(
    val reservaId: String = "",
    val reserva: Reserva? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class BoletoDigitalViewModel(
    private val reservaRepository: ReservaRepository = ReservaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoletoDigitalUiState())
    val uiState: StateFlow<BoletoDigitalUiState> = _uiState.asStateFlow()

    fun cargarBoleto(reservaId: String) {
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
                        errorMessage = "Error al cargar boleto: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
