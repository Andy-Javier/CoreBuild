package edu.ucne.corebuild.presentation.favorites

import edu.ucne.corebuild.domain.model.Component

data class FavoritesUiState(
    val favorites: List<Component> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
