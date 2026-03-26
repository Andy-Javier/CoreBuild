package edu.ucne.corebuild.presentation.bottleneck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class BottleneckViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BottleneckUiState())
    val uiState: StateFlow<BottleneckUiState> = _uiState.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getComponentsUseCase().collect { components ->
                _uiState.update {
                    it.copy(
                        cpus = components.filterIsInstance<Component.CPU>(),
                        gpus = components.filterIsInstance<Component.GPU>(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: BottleneckEvent) {
        when (event) {
            is BottleneckEvent.SelectCpu -> {
                _uiState.update { it.copy(selectedCpu = event.cpu) }
                calculateBottleneck()
            }
            is BottleneckEvent.SelectGpu -> {
                _uiState.update { it.copy(selectedGpu = event.gpu) }
                calculateBottleneck()
            }
            is BottleneckEvent.SelectResolution -> {
                _uiState.update { it.copy(selectedResolution = event.resolution) }
                calculateBottleneck()
            }
        }
    }

    private fun calculateBottleneck() {
        val cpu = _uiState.value.selectedCpu ?: return
        val gpu = _uiState.value.selectedGpu ?: return
        val res = _uiState.value.selectedResolution

        // Heuristic calculation based on performance scores (simplified)
        // Score = Price as a proxy for performance for this demo
        val cpuScore = cpu.price
        val gpuScore = gpu.price

        // Resolution factor: Higher resolution relies more on GPU
        val resFactor = when (res) {
            "1080p" -> 1.0
            "1440p" -> 0.8
            "4K" -> 0.5
            else -> 1.0
        }

        val balancedGpuScore = cpuScore * 2.5 * (1 / resFactor)
        val difference = abs(balancedGpuScore - gpuScore)
        val percentage = (difference / balancedGpuScore * 100).coerceIn(0.0, 100.0)

        val status = when {
            percentage < 10 -> "Equilibrado ✅"
            gpuScore < balancedGpuScore -> "Cuello de botella de GPU ⚠️"
            else -> "Cuello de botella de CPU ⚠️"
        }

        _uiState.update { 
            it.copy(
                bottleneckPercentage = percentage,
                status = status
            )
        }
    }
}
