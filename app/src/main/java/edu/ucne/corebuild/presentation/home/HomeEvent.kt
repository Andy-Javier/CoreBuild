package edu.ucne.corebuild.presentation.home

sealed interface HomeEvent {
    data object LoadComponents : HomeEvent
    data class OnSearchQueryChange(val query: String) : HomeEvent
    data class OnCategoryChange(val category: String?) : HomeEvent
    data object OnToggleBuildDialog : HomeEvent
    data object OnAddFeaturedToCart : HomeEvent
    data object ResetNavigation : HomeEvent
}
