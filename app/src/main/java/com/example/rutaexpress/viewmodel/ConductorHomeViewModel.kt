package com.example.rutaexpress.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rutaexpress.data.model.Viaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado UI reactivo para la pantalla del Panel del Conductor.
 */
data class ConductorHomeUiState(
    val viajes: List<Viaje> = emptyList(),
    val isLoading: Boolean = true,
    val conductorNombre: String = "",
    val errorMessage: String? = null,
)

/**
 * ViewModel encargado de la lógica del Panel del Conductor.
 * Conecta un listener en tiempo real a la colección "viajes" de Firestore
 * filtrando únicamente por idConductor == UID del usuario autenticado.
 */
class ConductorHomeViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow(ConductorHomeUiState())
    val uiState: StateFlow<ConductorHomeUiState> = _uiState.asStateFlow()

    private val _viajes = MutableStateFlow<List<Viaje>>(emptyList())
    val viajes: StateFlow<List<Viaje>> = _viajes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarRutasConductor()
    }

    /**
     * Obtiene el UID del conductor autenticado y escucha en tiempo real sus rutas asignadas.
     */
    private fun cargarRutasConductor() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No se encontró sesión de conductor activa."
                )
            }
            _isLoading.value = false
            return
        }

        // 1. Obtener nombre completo del conductor desde la colección "users"
        firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val nombres = snapshot.getString("nombres").orEmpty()
                    val apPaterno = snapshot.getString("apellido_paterno").orEmpty()
                    val apMaterno = snapshot.getString("apellido_materno").orEmpty()
                    val nombreCompleto = listOf(nombres, apPaterno, apMaterno)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")

                    if (nombreCompleto.isNotBlank()) {
                        _uiState.update { it.copy(conductorNombre = nombreCompleto) }
                    }
                }
            }

        // 2. Consulta a Firestore en tiempo real filtrando por whereEqualTo("idConductor", uid)
        firestore.collection("viajes")
            .whereEqualTo("idConductor", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar rutas: ${error.localizedMessage}"
                        )
                    }
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val rutasList = snapshot.toObjects(Viaje::class.java)
                    _viajes.value = rutasList
                    _isLoading.value = false
                    _uiState.update {
                        it.copy(
                            viajes = rutasList,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _viajes.value = emptyList()
                    _isLoading.value = false
                    _uiState.update {
                        it.copy(
                            viajes = emptyList(),
                            isLoading = false
                        )
                    }
                }
            }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
