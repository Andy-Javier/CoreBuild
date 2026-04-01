package edu.ucne.corebuild.presentation.search

import edu.ucne.corebuild.domain.model.Component

data class SearchUiState(
    val query: String = "",
    val components: List<Component> = emptyList(),
    val filteredComponents: List<Component> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filtros seleccionados
    val selectedCategories: Set<String> = emptySet(),
    val selectedBrands: Set<String> = emptySet(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    
    // Opciones de filtros dinámicos (extraídos de la data)
    val availableCategories: List<String> = emptyList(),
    val availableBrands: List<String> = emptyList()
)
