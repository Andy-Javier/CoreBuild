package edu.ucne.corebuild.presentation.auth

import edu.ucne.corebuild.domain.model.User

data class AuthUiState(
    val user: User? = null,
    val isLogged: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
