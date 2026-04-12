package edu.ucne.corebuild.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.User
import edu.ucne.corebuild.domain.repository.OrderRepository
import edu.ucne.corebuild.domain.repository.UserRepository
import edu.ucne.corebuild.domain.auth.AuthManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeLoggedUser()
        observeOrders()
    }

    private fun observeLoggedUser() {
        viewModelScope.launch {
            userRepository.getLoggedUser().collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isLogged = user != null,
                        isAdmin = if (user != null)
                            authManager.isAdmin(user.email) else false,
                        isCheckingSession = false
                    )
                }
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                val totalSpent = orders.sumOf { it.totalPrice }
                _uiState.update {
                    it.copy(
                        totalOrdersCount = orders.size,
                        totalSpent = totalSpent
                    )
                }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnLogin -> login(event.email, event.pass)
            is AuthEvent.OnRegister -> register(event.name, event.email, event.pass)
            is AuthEvent.OnLogout -> logout()
            is AuthEvent.OnUpdateProfilePicture -> updateProfilePicture(event.imageUrl)
            is AuthEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Paso 1: Verificar si es admin con AuthManager
            if (authManager.validateAdminCredentials(email, pass)) {
                val adminUser = User(
                    name = if (email.contains("andernunez")) "Anderson Nuñez" else "Andy Javier",
                    email = email.trim().lowercase()
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = adminUser,
                        isLogged = true,
                        isAdmin = true
                    )
                }
                return@launch
            }

            // Paso 2: Si no es admin, proceder con login normal en Room/API
            val result = userRepository.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isLogged = true,
                            isAdmin = authManager.isAdmin(user.email)
                        )
                    }
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

    private fun updateProfilePicture(imageUrl: String) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user
            if (currentUser != null && currentUser.id != null) {
                userRepository.updateProfilePicture(currentUser.id, imageUrl)
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.update { it.copy(user = null, isLogged = false, isAdmin = false, isCheckingSession = false) }
        }
    }
}
