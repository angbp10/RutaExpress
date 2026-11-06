package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Encomienda
import com.example.rutaexpress.data.repository.EncomiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RastreoUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searched: Boolean = false,
    val encomiendas: List<Encomienda> = emptyList(),
    val errorMessage: String? = null,
)

class RastreoViewModel(
    private val repository: EncomiendaRepository = EncomiendaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(RastreoUiState())
    val uiState: StateFlow<RastreoUiState> = _uiState.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, errorMessage = null) }
    }

    fun buscarEncomienda(queryToSearch: String = _uiState.value.query) {
        val qTrim = queryToSearch.trim()
        if (qTrim.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingresa un número de guía o DNI.") }
            return
        }

        _uiState.update {
            it.copy(
                query = qTrim,
                isLoading = true,
                searched = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val result = repository.buscarEncomiendasPublico(qTrim)
            result.onSuccess { lista ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        encomiendas = lista,
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        encomiendas = emptyList(),
                        errorMessage = "Error en la búsqueda: ${ex.localizedMessage}",
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
