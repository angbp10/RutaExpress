package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.model.Asiento
import com.example.rutaexpress.data.model.Viaje
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI reactivo para la pantalla de Selección de Asientos.
 */
data class SeleccionAsientosUiState(
    val viaje: Viaje? = null,
    val asientosList: List<Asiento> = emptyList(),
    val asientosSeleccionadosIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel encargado del flujo de Selección de Asientos en tiempo real desde Firestore.
 */
class SeleccionAsientosViewModel : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow(SeleccionAsientosUiState())
    val uiState: StateFlow<SeleccionAsientosUiState> = _uiState.asStateFlow()

    private var currentViajeId: String = ""

    /**
     * Carga el viaje y escucha en tiempo real la colección/subcolección de asientos.
     */
    fun cargarViajeYAsientos(viajeId: String) {
        if (viajeId.isBlank()) return
        currentViajeId = viajeId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // 1. Escuchar viaje en tiempo real
        firestore.collection("viajes").document(viajeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar viaje: ${error.localizedMessage}"
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val viajeObj = snapshot.toObject(Viaje::class.java)
                    _uiState.update { it.copy(viaje = viajeObj) }
                }
            }

        // 2. Escuchar asientos en tiempo real
        firestore.collection("asientos")
            .whereEqualTo("idViaje", viajeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar asientos: ${error.localizedMessage}"
                        )
                    }
                    return@addSnapshotListener
                }

                val asientosRecuperados = snapshot?.toObjects(Asiento::class.java).orEmpty()

                _uiState.update { state ->
                    val viajeObj = state.viaje
                    val totales = viajeObj?.asientosTotales ?: 15

                    // Generar lista final completando asientos si Firestore no los tiene aún
                    val listaFinal = if (asientosRecuperados.isNotEmpty()) {
                        asientosRecuperados.sortedBy { it.numero }
                    } else {
                        (1..totales).map { num ->
                            Asiento(
                                id = "asiento_$num",
                                idViaje = viajeId,
                                numero = num,
                                estado = "disponible"
                            )
                        }
                    }

                    // Limpiar seleccionados que hayan cambiado a ocupado
                    val idsOcupados = listaFinal
                        .filter { it.estado.lowercase() != "disponible" }
                        .map { it.id }
                        .toSet()

                    val seleccionadosLimpios = state.asientosSeleccionadosIds
                        .filterNot { idsOcupados.contains(it) }
                        .toSet()

                    state.copy(
                        asientosList = listaFinal,
                        asientosSeleccionadosIds = seleccionadosLimpios,
                        isLoading = false
                    )
                }
            }
    }

    /**
     * Alterna la selección de un asiento disponible.
     */
    fun toggleSeleccionAsiento(asiento: Asiento) {
        if (asiento.estado.lowercase() != "disponible") return

        _uiState.update { state ->
            val actuales = state.asientosSeleccionadosIds.toMutableSet()
            if (actuales.contains(asiento.id)) {
                actuales.remove(asiento.id)
            } else {
                actuales.add(asiento.id)
            }
            state.copy(asientosSeleccionadosIds = actuales)
        }
    }

    /**
     * Confirma la reserva de los asientos elegidos y actualiza la ocupación en Firestore.
     */
    fun confirmarReserva(onSuccess: () -> Unit) {
        val current = _uiState.value
        val seleccionados = current.asientosSeleccionadosIds
        if (seleccionados.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                seleccionados.forEach { asientoId ->
                    val ref = firestore.collection("asientos").document(asientoId)
                    batch.set(
                        ref, mapOf(
                            "id" to asientoId,
                            "idViaje" to currentViajeId,
                            "estado" to "ocupado"
                        ), SetOptions.merge()
                    )
                }

                val viaje = current.viaje
                if (viaje != null) {
                    val viajeRef = firestore.collection("viajes").document(viaje.id)
                    val nuevosOcupados = viaje.asientosOcupados + seleccionados.size
                    val nuevosDisponibles = (viaje.asientosTotales - nuevosOcupados).coerceAtLeast(0)
                    batch.update(
                        viajeRef, mapOf(
                            "asientosOcupados" to nuevosOcupados,
                            "asientosDisponibles" to nuevosDisponibles
                        )
                    )
                }

                batch.commit().addOnSuccessListener {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "¡Asientos reservados con éxito!",
                            asientosSeleccionadosIds = emptySet()
                        )
                    }
                    onSuccess()
                }.addOnFailureListener { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al reservar asientos: ${ex.localizedMessage}"
                        )
                    }
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error en el proceso de reserva: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
