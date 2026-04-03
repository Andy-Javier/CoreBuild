package edu.ucne.corebuild.presentation.overview

import edu.ucne.corebuild.domain.model.CartItem

data class BuildOverviewUiState(
    val isLoading: Boolean = true,
    val components: List<CartItem> = emptyList(),

    // Build Score
    val scoreValue: Float = 0f,      // 0f..1f
    val scoreLabel: String = "",     // Excelente / Balanceado / Mejorable

    // Rendimiento
    val estimatedFps: Int = 0,
    val fpsBarValue: Float = 0f,     // normalizado 0f..1f (cap 144fps)

    // Bottleneck
    val bottleneckValue: Float = 0f, // 0f..1f (0=sin problema)
    val bottleneckLabel: String = "", // CPU limitado / GPU limitado / Sin problema

    // Consumo
    val estimatedWatts: Int = 0,
    val psuWatts: Int = 0,           // de Component.PSU si existe, si no 0
    val powerBarValue: Float = 0f,   // estimatedWatts / psuWatts, cap 1f

    // Compatibilidad
    val compatibilityOk: Boolean = true,
    val warnings: List<String> = emptyList()
)
