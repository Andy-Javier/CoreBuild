package edu.ucne.corebuild.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.buildscore.BuildScoreCalculator
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.performance.GamePreset
import edu.ucne.corebuild.domain.performance.PerformanceCalculator
import edu.ucne.corebuild.domain.repository.CartRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BuildOverviewViewModel @Inject constructor(
    cartRepository: CartRepository,
    private val compatibilityEngine: CompatibilityEngine,
    private val buildScoreCalculator: BuildScoreCalculator
) : ViewModel() {

    val uiState: StateFlow<BuildOverviewUiState> = cartRepository.getCartItems()
        .map { items ->
            if (items.isEmpty()) return@map BuildOverviewUiState(isLoading = false)

            val components = items.map { it.component }
            val cpu = components.filterIsInstance<Component.CPU>().firstOrNull()
            val gpu = components.filterIsInstance<Component.GPU>().firstOrNull()
            val psu = components.filterIsInstance<Component.PSU>().firstOrNull()

            val scoreResult = buildScoreCalculator.calculateScore(items)
            val scoreValue = scoreResult.score / 100f
            val scoreLabel = scoreResult.label

            val fpsResult = if (cpu != null && gpu != null) {
                PerformanceCalculator.estimateFps(cpu, gpu, GamePreset.GTA_V, "1080p")
            } else null
            
            val estimatedFps = fpsResult?.fps ?: 0
            val fpsBarValue = (estimatedFps / 144f).coerceIn(0f, 1f)

            var bPercent = 0f
            var bLabel = "Sin problema"
            if (cpu != null && gpu != null) {
                val cPrice = cpu.price.coerceAtLeast(1.0)
                val ratio = gpu.price / cPrice
                when {
                    ratio > 4.0 -> { bPercent = 0.8f; bLabel = "CPU limitado" }
                    ratio < 1.0 -> { bPercent = 0.8f; bLabel = "GPU limitado" }
                    ratio >= 3.5 -> { bPercent = 0.4f; bLabel = "CPU limitado" }
                    ratio <= 1.2 -> { bPercent = 0.4f; bLabel = "GPU limitado" }
                }
            }

            val estimatedWatts = components.sumOf { component ->
                try {
                    when (component) {
                        is Component.CPU -> {
                            val tdpStr = component.tdp
                            tdpStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 65
                        }
                        is Component.GPU -> {
                            val consStr = component.consumptionWatts
                            consStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 200
                        }
                        else -> 10
                    }
                } catch (_: Exception) { 10 }
            }
            val psuWatts = psu?.wattage ?: 0
            val powerBarValue = if (psuWatts > 0) (estimatedWatts / psuWatts.toFloat()).coerceIn(0f, 1f) else 0f

            val warnings = compatibilityEngine.checkCompatibility(items)

            BuildOverviewUiState(
                isLoading = false,
                components = items,
                scoreValue = scoreValue,
                scoreLabel = scoreLabel,
                estimatedFps = estimatedFps,
                fpsBarValue = fpsBarValue,
                bottleneckValue = bPercent,
                bottleneckLabel = bLabel,
                estimatedWatts = estimatedWatts,
                psuWatts = psuWatts,
                powerBarValue = powerBarValue,
                compatibilityOk = warnings.isEmpty(),
                warnings = warnings
            )
        }
        .catch { emit(BuildOverviewUiState(isLoading = false)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BuildOverviewUiState()
        )
}
