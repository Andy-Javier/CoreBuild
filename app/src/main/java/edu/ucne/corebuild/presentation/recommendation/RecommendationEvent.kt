package edu.ucne.corebuild.presentation.recommendation

import edu.ucne.corebuild.domain.model.Component

sealed class RecommendationEvent {
    data class OnBudgetChange(val budget: String) : RecommendationEvent()
    data class OnBaseComponentSelect(val component: Component?) : RecommendationEvent()
    object OnGenerateBuild : RecommendationEvent()
}
