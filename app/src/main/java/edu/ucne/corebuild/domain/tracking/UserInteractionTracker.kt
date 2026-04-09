package edu.ucne.corebuild.domain.tracking

import edu.ucne.corebuild.domain.repository.StatsRepository
import javax.inject.Inject

class UserInteractionTracker @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend fun trackView(componentId: Int) {
        statsRepository.recordView(componentId)
    }

    suspend fun trackAddToCart(componentId: Int) {
        statsRepository.recordView(componentId)
        statsRepository.recordAddedToCart(componentId)
    }

    fun trackSearch(query: String) {
    }
}