package edu.ucne.corebuild.presentation.orders

import edu.ucne.corebuild.domain.model.Order

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showConfirmation: Boolean = false,
    val notificationSent: Boolean = false
)
