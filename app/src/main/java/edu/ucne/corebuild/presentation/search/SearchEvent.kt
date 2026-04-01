package edu.ucne.corebuild.presentation.search

sealed class SearchEvent {
    data class OnQueryChange(val query: String) : SearchEvent()
    data class OnCategorySelect(val category: String) : SearchEvent()
    data class OnBrandSelect(val brand: String) : SearchEvent()
    data class OnPriceRangeChange(val min: Double?, val max: Double?) : SearchEvent()
    object OnClearFilters : SearchEvent()
}
