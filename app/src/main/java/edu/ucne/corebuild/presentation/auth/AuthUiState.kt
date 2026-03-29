package edu.ucne.corebuild.presentation.auth

import edu.ucne.corebuild.domain.model.User

data class AuthUiState(
    val user: User? = null,
    val isLogged: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val totalOrdersCount: Int = 0,
    val totalSpent: Double = 0.0
)
