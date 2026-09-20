package com.example.rutaexpress.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutaexpress.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados UI para el proceso de Iniciar Sesión con gestión de roles.
 */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val role: String, val user: FirebaseUser? = null) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

/**
 * ViewModel que gestiona la autenticación y la lectura obligatoria del rol de usuario en Firestore.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Realiza la autenticación y NO navega hasta obtener el rol exacto desde Firestore.
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor ingresa tu correo y contraseña.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.loginAndGetRole(email.trim(), password)
            result.onSuccess { (user, role) ->
                Log.d("AUTH_ROLE", "Rol obtenido de Firestore: $role")
                // Emitir estado Success únicamente con el rol obtenido de Firestore
                _uiState.value = LoginUiState.Success(role = role, user = user)
            }.onFailure { exception ->
                Log.e("AUTH_ROLE", "Error durante autenticación/consulta de rol: ${exception.localizedMessage}")
                _uiState.value = LoginUiState.Error(
                    exception.localizedMessage ?: "Credenciales incorrectas o error de conexión.",
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
