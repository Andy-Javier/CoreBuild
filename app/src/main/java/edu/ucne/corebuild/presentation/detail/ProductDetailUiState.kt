package edu.ucne.corebuild.presentation.detail

import edu.ucne.corebuild.domain.model.Component

data class ProductDetailUiState(
    val component: Component? = null,
    val variants: List<Component> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val navigateToCart: Boolean = false,
    val quantityLimit: Int = 1,
    val currentInCart: Int = 0,
    val orderCompleted: Boolean = false,
    val isAdmin: Boolean = false
)
