package edu.ucne.corebuild.presentation.favorites

sealed interface FavoritesEvent {
    data class OnToggleFavorite(val componentId: Int) : FavoritesEvent
}
