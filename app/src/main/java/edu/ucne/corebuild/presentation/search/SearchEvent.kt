package edu.ucne.corebuild.presentation.search

sealed interface SearchEvent {
    data class OnQueryChange(val query: String) : SearchEvent
    data class OnCategorySelect(val category: String) : SearchEvent
    data class OnBrandSelect(val brand: String) : SearchEvent
    data class OnPriceRangeChange(val min: Double?, val max: Double?) : SearchEvent
    data object OnClearFilters : SearchEvent
}
