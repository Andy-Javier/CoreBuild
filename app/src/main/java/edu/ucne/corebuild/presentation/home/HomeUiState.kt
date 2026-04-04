package edu.ucne.corebuild.presentation.home

import edu.ucne.corebuild.domain.model.Component

data class HomeUiState(
    val components: List<Component> = emptyList(),
    val filteredComponents: List<Component> = emptyList(),
    val recentlyViewed: List<Component> = emptyList(),
    val topRated: List<Component> = emptyList(),
    val intelComponents: List<Component> = emptyList(),
    val amdCpuComponents: List<Component> = emptyList(),
    val nvidiaComponents: List<Component> = emptyList(),
    val radeonComponents: List<Component> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val featuredBuild: PredefinedBuild? = null,
    val showBuildDialog: Boolean = false,
    val navigateToCart: Boolean = false,
    val error: String? = null,
    val smartRecommendations: List<Component> = emptyList()
)

data class PredefinedBuild(
    val name: String,
    val description: String,
    val components: List<Component>,
    val totalPrice: Double,
    val imageUrl: String = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774798413/imagen_2026-03-29_113332415_rorwm4.png"
)
