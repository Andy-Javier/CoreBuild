package edu.ucne.corebuild.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoriteRepository.getFavoriteComponents()
        .map { favorites ->
            FavoritesUiState(favorites = favorites, isLoading = false)
        }
        .onStart { emit(FavoritesUiState(isLoading = true)) }
        .catch { e -> emit(FavoritesUiState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState(isLoading = true)
        )

    fun onEvent(event: FavoritesEvent) {
        when (event) {
            is FavoritesEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    favoriteRepository.toggleFavorite(event.componentId)
                }
            }
        }
    }
}
