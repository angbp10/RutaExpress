package com.example.rutaexpress.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.repository.AuthRepository
import com.example.rutaexpress.data.repository.DniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI reactivo para el proceso de Registro.
 */
data class SignUpUiState(
    val dni: String = "",
    val nombres: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val isDniLoading: Boolean = false,
    val isSignUpLoading: Boolean = false,
    val isSignUpSuccess: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel encargado del flujo de Registro de usuarios y autocompletado de DNI.
 */
class SignUpViewModel(
    private val dniRepository: DniRepository = DniRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private var lastQueriedDni: String = ""

    /**
     * Actualiza el DNI y si llega a exactamente 8 dígitos ejecuta la consulta automática.
     */
    fun onDniChanged(newDni: String) {
        val cleanDni = newDni.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(dni = cleanDni, errorMessage = null) }

        // Consulta automática al tener 8 dígitos
        if ((cleanDni.length == 8) && (cleanDni != lastQueriedDni)) {
            lastQueriedDni = cleanDni
            consultarDni(cleanDni)
        }
    }

    fun onNombresChanged(value: String) {
        _uiState.update { it.copy(nombres = value) }
    }

    fun onApellidoPaternoChanged(value: String) {
        _uiState.update { it.copy(apellidoPaterno = value) }
    }

    fun onApellidoMaternoChanged(value: String) {
        _uiState.update { it.copy(apellidoMaterno = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onTelefonoChanged(value: String) {
        val cleanTel = value.filter { it.isDigit() }.take(9)
        _uiState.update { it.copy(telefono = cleanTel) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    /**
     * Consulta el DNI en la API REST de json.pe y autocompleta nombres y apellidos.
     */
    private fun consultarDni(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDniLoading = true, errorMessage = null) }
            val result = dniRepository.consultarDni(dni)
            result.onSuccess { data ->
                _uiState.update { state ->
                    state.copy(
                        nombres = data.nombres.orEmpty(),
                        apellidoPaterno = data.apellidoPaterno.orEmpty(),
                        apellidoMaterno = data.apellidoMaterno.orEmpty(),
                        isDniLoading = false,
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isDniLoading = false,
                        errorMessage = "Consulta DNI: ${exception.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Valida los datos e inicia el proceso de registro con Firebase Authentication.
     */
    fun signUp() {
        val currentState = _uiState.value

        // Validaciones
        if (currentState.dni.length < 8) {
            _uiState.update { it.copy(errorMessage = "El DNI debe tener al menos 8 dígitos.") }
            return
        }
        if (currentState.nombres.isBlank() || currentState.apellidoPaterno.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, ingresa nombres y apellido paterno.") }
            return
        }
        if (currentState.email.isBlank() || !currentState.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo electrónico válido.") }
            return
        }
        if (currentState.telefono.length < 9) {
            _uiState.update { it.copy(errorMessage = "El teléfono debe tener 9 dígitos.") }
            return
        }
        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSignUpLoading = true, errorMessage = null) }
            val result = authRepository.signUp(
                dni = currentState.dni,
                nombres = currentState.nombres,
                apellidoPaterno = currentState.apellidoPaterno,
                apellidoMaterno = currentState.apellidoMaterno,
                email = currentState.email,
                telefono = currentState.telefono,
                password = currentState.password,
            )

            result.onSuccess {
                _uiState.update { it.copy(isSignUpLoading = false, isSignUpSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isSignUpLoading = false,
                        errorMessage = exception.localizedMessage ?: "Error al registrar usuario.",
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
