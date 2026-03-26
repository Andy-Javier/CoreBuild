package edu.ucne.corebuild.presentation.comparator

import edu.ucne.corebuild.domain.model.Component

data class ComparatorUiState(
    val components: List<Component> = emptyList(),
    val filteredComponents: List<Component> = emptyList(),
    val selectedType: String = "CPU", // "CPU" or "GPU"
    val selectedComponent1: Component? = null,
    val selectedComponent2: Component? = null,
    val isLoading: Boolean = false
)
