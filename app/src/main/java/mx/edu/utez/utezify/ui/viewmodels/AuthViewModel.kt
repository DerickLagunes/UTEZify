package mx.edu.utez.utezify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.utezify.data.models.UiState
import mx.edu.utez.utezify.data.models.User
import mx.edu.utez.utezify.data.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>>(UiState.Empty)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val currentUser: StateFlow<User?> = authRepository.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun login(matricula: String, password: String) {
        val cleanMatricula = matricula.trim()
        if (cleanMatricula.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("La matrícula y contraseña son requeridas")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.login(cleanMatricula, password)
            result.onSuccess { user ->
                _authState.value = UiState.Success(user)
            }.onFailure { error ->
                _authState.value = UiState.Error(error.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun register(fullName: String, matricula: String, password: String, confirmPassword: String) {
        val cleanFullName = fullName.trim()
        val cleanMatricula = matricula.trim()

        if (cleanFullName.isBlank() || cleanMatricula.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("Todos los campos son obligatorios")
            return
        }

        if (password != confirmPassword) {
            _authState.value = UiState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.register(cleanFullName, cleanMatricula, password)
            result.onSuccess { user ->
                _authState.value = UiState.Success(user)
            }.onFailure { error ->
                _authState.value = UiState.Error(error.localizedMessage ?: "Error al registrar usuario")
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = UiState.Empty
            onLoggedOut()
        }
    }

    fun resetState() {
        _authState.value = UiState.Empty
    }
}
