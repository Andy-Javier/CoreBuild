package edu.ucne.corebuild.presentation.detail

import edu.ucne.corebuild.domain.model.Component

data class ProductDetailUiState(
    val component: Component? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
