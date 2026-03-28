package edu.ucne.corebuild.presentation.recommendation

sealed class RecommendationEvent {
    data class OnBudgetChange(val budget: String) : RecommendationEvent()
    data class OnPriorityChange(val priority: String?) : RecommendationEvent()
    data object OnGenerateBuild : RecommendationEvent()
}
