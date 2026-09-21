package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Encomienda
import com.example.rutaexpress.data.repository.DniRepository
import com.example.rutaexpress.data.repository.EncomiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EncomiendaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val guideGenerated: String? = null,
    val currentEncomienda: Encomienda? = null,
    val searchQuery: String = ""
)

class EncomiendaViewModel(
    private val repository: EncomiendaRepository = EncomiendaRepository(),
    private val dniRepository: DniRepository = DniRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EncomiendaUiState())
    val uiState: StateFlow<EncomiendaUiState> = _uiState.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    val encomiendasState: StateFlow<List<Encomienda>> = repository.getEncomiendasFlow()
        .combine(_searchText) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.guia.contains(query, ignoreCase = true) ||
                            it.dniRemitente.contains(query, ignoreCase = true) ||
                            it.dniDestinatario.contains(query, ignoreCase = true)
                }
            }
        }
        .catch { ex ->
            _uiState.update { it.copy(errorMessage = "Error al cargar encomiendas: ${ex.localizedMessage}") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Factores de tarifa
    private val factorEstandar = 5.0
    private val factorFragil = 8.0
    private val factorExpress = 12.0

    /**
     * Calcula la tarifa total basada en el peso y tipo de carga.
     */
    fun calcularTarifa(peso: Double, tipoCarga: String): Double {
        val factor = when (tipoCarga) {
            "Estándar" -> factorEstandar
            "Frágil" -> factorFragil
            "Express" -> factorExpress
            else -> factorEstandar
        }
        return peso * factor
    }

    /**
     * Consulta el DNI del remitente o destinatario.
     */
    fun buscarDni(dni: String, isRemitente: Boolean, onResult: (String) -> Unit) {
        if (dni.length != 8) return

        viewModelScope.launch {
            val result = dniRepository.consultarDni(dni)
            result.onSuccess { data ->
                val nombreCompleto = "${data.nombres} ${data.apellidoPaterno} ${data.apellidoMaterno}"
                onResult(nombreCompleto.trim())
            }.onFailure { ex ->
                _uiState.update { it.copy(errorMessage = "DNI no encontrado: ${ex.message}") }
            }
        }
    }

    /**
     * Emite una nueva guía de encomienda.
     */
    fun emitirGuia(
        remitente: String,
        dniRemitente: String,
        celularRemitente: String,
        destinatario: String,
        dniDestinatario: String,
        celularDestinatario: String,
        peso: Double,
        tipoCarga: String
    ) {
        if (remitente.isBlank() || dniRemitente.length != 8 || destinatario.isBlank() || dniDestinatario.length != 8 || peso <= 0) {
            _uiState.update { it.copy(errorMessage = "Por favor, complete todos los campos obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Generar número de guía secuencial
            val ultimoNumero = repository.getUltimoNumeroGuia()
            val nuevoNumero = ultimoNumero + 1
            val guiaFormateada = "e-${nuevoNumero.toString().padStart(7, '0')}"
            
            val tarifa = calcularTarifa(peso, tipoCarga)
            
            val encomienda = Encomienda(
                guia = guiaFormateada,
                remitente = remitente,
                dniRemitente = dniRemitente,
                celularRemitente = celularRemitente,
                destinatario = destinatario,
                dniDestinatario = dniDestinatario,
                celularDestinatario = celularDestinatario,
                pesoKg = peso,
                tipoCarga = tipoCarga,
                tarifaTotal = tarifa,
                estado = "EN_ALMACEN"
            )

            val result = repository.registrarEncomienda(encomienda)
            result.onSuccess { guia ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Guía $guia emitida con éxito.",
                        guideGenerated = guia
                    )
                }
            }.onFailure { ex ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al emitir guía: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchText.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Busca una encomienda por su número de guía o DNI.
     * (Mantenemos esta función por si se requiere búsqueda explícita,
     * pero la UI usará el filtrado dinámico de encomiendasState)
     */
    fun buscarEncomienda(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese guía o DNI para buscar.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentEncomienda = null) }
            val result = repository.buscarEncomienda(query)
            result.onSuccess { encomienda ->
                if (encomienda != null) {
                    _uiState.update { it.copy(isLoading = false, currentEncomienda = encomienda) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró ningún registro.") }
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage) }
            }
        }
    }

    /**
     * Actualiza el estado de una encomienda existente.
     */
    fun actualizarEstado(id: String, nuevoEstado: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.actualizarEstado(id, nuevoEstado)
            result.onSuccess {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        successMessage = "Estado actualizado a $nuevoEstado",
                        currentEncomienda = it.currentEncomienda?.copy(estado = nuevoEstado)
                    )
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.localizedMessage) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, guideGenerated = null) }
    }
}
