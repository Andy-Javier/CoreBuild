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
    private val cartRepository: CartRepository,
    private val compatibilityEngine: CompatibilityEngine,
    private val buildScoreCalculator: BuildScoreCalculator
) : ViewModel() {

    val uiState: StateFlow<BuildOverviewUiState> = cartRepository.getCartItems()
        .map { items ->
            val components = items.map { it.component }
            val cpu = components.filterIsInstance<Component.CPU>().firstOrNull()
            val gpu = components.filterIsInstance<Component.GPU>().firstOrNull()
            val psu = components.filterIsInstance<Component.PSU>().firstOrNull()

            // 1. Build Score
            val scoreResult = buildScoreCalculator.calculateScore(items)
            val scoreValue = scoreResult.score / 100f
            val scoreLabel = when {
                scoreValue >= 0.75f -> "Excelente"
                scoreValue >= 0.45f -> "Balanceado"
                else -> "Mejorable"
            }

            // 2. Rendimiento (FPS) - Usando GTA V @ 1080p como base para el resumen general
            val fpsResult = if (cpu != null && gpu != null) {
                PerformanceCalculator.estimateFps(cpu, gpu, GamePreset.GTA_V, "1080p")
            } else null
            
            val estimatedFps = fpsResult?.fps ?: 0
            val fpsBarValue = (estimatedFps / 144f).coerceIn(0f, 1f)

            // 3. Bottleneck (Estimación inline basada en price ratio si no existe método en engine)
            var bPercent = 0f
            var bLabel = "Sin problema"
            if (cpu != null && gpu != null) {
                val ratio = gpu.price / cpu.price
                when {
                    ratio > 4.0 -> { bPercent = 0.8f; bLabel = "CPU limitado" }
                    ratio < 1.0 -> { bPercent = 0.8f; bLabel = "GPU limitado" }
                    ratio >= 3.5 -> { bPercent = 0.4f; bLabel = "CPU limitado" }
                    ratio <= 1.2 -> { bPercent = 0.4f; bLabel = "GPU limitado" }
                }
            }

            // 4. Consumo
            val estimatedWatts = components.sumOf { 
                when (it) {
                    is Component.CPU -> it.tdp?.filter { char -> char.isDigit() }?.toIntOrNull() ?: 65
                    is Component.GPU -> it.consumptionWatts.filter { char -> char.isDigit() }.toIntOrNull() ?: 200
                    else -> 10 // Margen para otros componentes
                }
            }
            val psuWatts = psu?.wattage ?: 0
            val powerBarValue = if (psuWatts > 0) (estimatedWatts / psuWatts.toFloat()).coerceIn(0f, 1f) else 0f

            // 5. Compatibilidad
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BuildOverviewUiState()
        )
}
