package edu.ucne.corebuild.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.User
import edu.ucne.corebuild.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeLoggedUser()
    }

    private fun observeLoggedUser() {
        viewModelScope.launch {
            userRepository.getLoggedUser().collect { user ->
                _uiState.update { it.copy(user = user, isLogged = user != null) }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnLogin -> login(event.email, event.pass)
            is AuthEvent.OnRegister -> register(event.name, event.email, event.pass)
            is AuthEvent.OnLogout -> logout()
            is AuthEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user, isLogged = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.register(User(name = name, email = email), pass)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Usuario registrado correctamente") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.update { it.copy(user = null, isLogged = false) }
        }
    }
}
