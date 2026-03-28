package edu.ucne.corebuild.presentation.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.recommendation.BuildRecommender
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val componentRepository: ComponentRepository,
    private val buildRecommender: BuildRecommender
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            componentRepository.getComponents().collect { components ->
                _uiState.update { it.copy(allComponents = components) }
            }
        }
    }

    fun onEvent(event: RecommendationEvent) {
        when (event) {
            is RecommendationEvent.OnBudgetChange -> {
                _uiState.update { it.copy(budget = event.budget) }
            }
            is RecommendationEvent.OnBaseComponentSelect -> {
                _uiState.update { it.copy(baseComponent = event.component) }
            }
            is RecommendationEvent.OnGenerateBuild -> generateBuild()
        }
    }

    private fun generateBuild() {
        val budget = _uiState.value.budget.toDoubleOrNull() ?: 0.0
        if (budget <= 0) {
            _uiState.update { it.copy(error = "Por favor ingrese un presupuesto válido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val recommended = buildRecommender.recommendBuild(
            budget = budget,
            baseComponent = _uiState.value.baseComponent,
            allComponents = _uiState.value.allComponents
        )

        _uiState.update { it.copy(
            isLoading = false,
            recommendedComponents = recommended
        ) }
    }
}
