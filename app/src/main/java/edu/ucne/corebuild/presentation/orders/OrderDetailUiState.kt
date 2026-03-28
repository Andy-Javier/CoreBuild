package edu.ucne.corebuild.presentation.orders

import edu.ucne.corebuild.domain.model.Order

data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
