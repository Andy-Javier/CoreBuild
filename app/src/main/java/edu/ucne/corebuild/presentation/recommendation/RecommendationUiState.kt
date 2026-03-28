package edu.ucne.corebuild.presentation.recommendation

import edu.ucne.corebuild.domain.model.Component

data class RecommendationUiState(
    val recommendedComponents: List<Component> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val budget: String = "",
    val priority: String? = null, // "CPU" or "GPU"
    val allComponents: List<Component> = emptyList()
) {
    val totalPrice: Double get() = recommendedComponents.sumOf { it.price }
}
