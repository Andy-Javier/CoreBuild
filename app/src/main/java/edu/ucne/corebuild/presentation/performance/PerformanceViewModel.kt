package edu.ucne.corebuild.presentation.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.performance.PerformanceCalculator
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState = _uiState.asStateFlow()

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

    fun onEvent(event: PerformanceEvent) {
        when (event) {
            is PerformanceEvent.SelectCpu -> {
                _uiState.update { it.copy(selectedCpu = event.cpu) }
                calculatePerformance()
            }
            is PerformanceEvent.SelectGpu -> {
                _uiState.update { it.copy(selectedGpu = event.gpu) }
                calculatePerformance()
            }
            is PerformanceEvent.SelectGame -> {
                _uiState.update { it.copy(selectedGame = event.game) }
                calculatePerformance()
            }
            is PerformanceEvent.SelectResolution -> {
                _uiState.update { it.copy(selectedResolution = event.resolution) }
                calculatePerformance()
            }
        }
    }

    private fun calculatePerformance() {
        val state = _uiState.value
        val cpu = state.selectedCpu
        val gpu = state.selectedGpu
        
        if (cpu != null && gpu != null) {
            val result = PerformanceCalculator.estimateFps(
                cpu = cpu,
                gpu = gpu,
                game = state.selectedGame,
                resolution = state.selectedResolution
            )
            _uiState.update { it.copy(fpsResult = result) }
        }
    }
}
