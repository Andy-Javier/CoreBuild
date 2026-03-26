package edu.ucne.corebuild.presentation.cart

import edu.ucne.corebuild.domain.model.CartItem

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val warnings: List<String> = emptyList(),
    val isLoading: Boolean = false
)
