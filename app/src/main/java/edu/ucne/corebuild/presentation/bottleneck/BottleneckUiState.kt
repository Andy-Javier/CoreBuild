package edu.ucne.corebuild.presentation.bottleneck

import edu.ucne.corebuild.domain.model.Component

data class BottleneckUiState(
    val cpus: List<Component.CPU> = emptyList(),
    val gpus: List<Component.GPU> = emptyList(),
    val selectedCpu: Component.CPU? = null,
    val selectedGpu: Component.GPU? = null,
    val selectedResolution: String = "1080p", // "1080p", "1440p", "4K"
    val bottleneckPercentage: Double = 0.0,
    val status: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
