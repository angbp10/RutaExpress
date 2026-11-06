package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.data.repository.ReservaRepository
import com.example.rutaexpress.data.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EscanerAbordajeUiState(
    val viajesDisponibles: List<Viaje> = emptyList(),
    val viajeSeleccionado: Viaje? = null,
    val isLoadingViajes: Boolean = true,
    val isValidating: Boolean = false,
    val resultadoExitoso: Reserva? = null,
    val errorRechazo: String? = null,
    val mostrarDialogoResultado: Boolean = false,
    val totalAbordadosTurno: Int = 0,
)

class EscanerAbordajeViewModel(
    private val reservaRepository: ReservaRepository = ReservaRepository(),
    private val viajeRepository: ViajeRepository = ViajeRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(EscanerAbordajeUiState())
    val uiState: StateFlow<EscanerAbordajeUiState> = _uiState.asStateFlow()

    init {
        cargarViajes()
    }

    fun cargarViajes() {
        _uiState.update { it.copy(isLoadingViajes = true) }
        viewModelScope.launch {
            viajeRepository.getViajesFlow().collect { lista ->
                _uiState.update {
                    it.copy(
                        viajesDisponibles = lista,
                        viajeSeleccionado = it.viajeSeleccionado ?: lista.firstOrNull(),
                        isLoadingViajes = false,
                    )
                }
            }
        }
    }

    fun seleccionarViaje(viaje: Viaje) {
        _uiState.update { it.copy(viajeSeleccionado = viaje) }
    }

    fun procesarBoletoEscaneado(idBoleto: String) {
        val viajeActual = _uiState.value.viajeSeleccionado
        if (idBoleto.isBlank()) return

        _uiState.update {
            it.copy(
                isValidating = true,
                resultadoExitoso = null,
                errorRechazo = null,
                mostrarDialogoResultado = false,
            )
        }

        viewModelScope.launch {
            val idTurno = viajeActual?.id.orEmpty()
            val result = reservaRepository.validarYMarcarAbordaje(
                idBoletoEscaneado = idBoleto,
                idViajeTurnoActual = idTurno,
            )

            result.onSuccess { reservaAbordada ->
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        resultadoExitoso = reservaAbordada,
                        errorRechazo = null,
                        mostrarDialogoResultado = true,
                        totalAbordadosTurno = it.totalAbordadosTurno + 1,
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        resultadoExitoso = null,
                        errorRechazo = exception.message ?: "Ocurrió un error al validar el boleto.",
                        mostrarDialogoResultado = true,
                    )
                }
            }
        }
    }

    fun cerrarDialogoResultado() {
        _uiState.update {
            it.copy(
                mostrarDialogoResultado = false,
                resultadoExitoso = null,
                errorRechazo = null,
            )
        }
    }
}
