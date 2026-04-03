package edu.ucne.corebuild.presentation.performance

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.performance.FpsResult
import edu.ucne.corebuild.domain.performance.GamePreset

val RESOLUTIONS = listOf("1080p", "1440p", "4K")

data class PerformanceUiState(
    val cpus: List<Component.CPU> = emptyList(),
    val gpus: List<Component.GPU> = emptyList(),
    val selectedCpu: Component.CPU? = null,
    val selectedGpu: Component.GPU? = null,
    val selectedGame: GamePreset = GamePreset.GTA_V,
    val selectedResolution: String = "1080p",
    val fpsResult: FpsResult? = null,
    val isLoading: Boolean = false
)

sealed interface PerformanceEvent {
    data class SelectCpu(val cpu: Component.CPU) : PerformanceEvent
    data class SelectGpu(val gpu: Component.GPU) : PerformanceEvent
    data class SelectGame(val game: GamePreset) : PerformanceEvent
    data class SelectResolution(val resolution: String) : PerformanceEvent
}
