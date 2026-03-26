package edu.ucne.corebuild.presentation.home

sealed interface HomeEvent {
    data object LoadComponents : HomeEvent
    data class OnSearchQueryChange(val query: String) : HomeEvent
}
