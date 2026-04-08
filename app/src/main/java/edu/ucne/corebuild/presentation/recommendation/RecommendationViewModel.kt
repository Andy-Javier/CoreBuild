package edu.ucne.corebuild.presentation.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.recommendation.BuildRecommender
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase,
    private val buildRecommender: BuildRecommender
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            getComponentsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(isLoading = false, allComponents = result.data ?: emptyList()) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(isLoading = false, error = result.message) 
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: RecommendationEvent) {
        when (event) {
            is RecommendationEvent.OnBudgetChange -> {
                _uiState.update { it.copy(budget = event.budget) }
            }
            is RecommendationEvent.OnPriorityChange -> {
                _uiState.update { it.copy(priority = event.priority) }
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
            priority = _uiState.value.priority,
            allComponents = _uiState.value.allComponents
        )

        if (recommended.isEmpty()) {
            _uiState.update { it.copy(
                isLoading = false,
                recommendedComponents = emptyList(),
                error = "El presupuesto es insuficiente para armar una PC completa con los componentes actuales."
            ) }
        } else {
            _uiState.update { it.copy(
                isLoading = false,
                recommendedComponents = recommended,
                error = null
            ) }
        }
    }
}
