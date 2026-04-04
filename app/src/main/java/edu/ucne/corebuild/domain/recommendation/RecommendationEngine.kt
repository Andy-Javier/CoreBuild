package edu.ucne.corebuild.domain.recommendation

import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationEngine @Inject constructor() {

    fun recommend(
        allComponents: List<Component>,
        recentlyViewed: List<Component>,   // weight 1
        cartItems: List<Component>,        // weight 3
        favorites: List<Component>,        // weight 2
        searchQuery: String                
    ): List<Component> {
        if (allComponents.isEmpty()) return emptyList()

        val scores = mutableMapOf<Int, Double>()

        recentlyViewed.forEach { scores[it.id] = (scores[it.id] ?: 0.0) + 1.0 }
        favorites.forEach    { scores[it.id] = (scores[it.id] ?: 0.0) + 2.0 }
        cartItems.forEach    { scores[it.id] = (scores[it.id] ?: 0.0) + 3.0 }

        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            allComponents.forEach { c ->
                if (c.name.contains(query, ignoreCase = true) ||
                    c.category.contains(query, ignoreCase = true)) {
                    scores[c.id] = (scores[c.id] ?: 0.0) + 1.5
                }
            }
        }

        val cartIds = cartItems.map { it.id }.toSet()
        return allComponents
            .filter { scores.getOrDefault(it.id, 0.0) > 0.0 && it.id !in cartIds }
            .sortedByDescending { scores[it.id] ?: 0.0 }
            .take(10)
    }
}
