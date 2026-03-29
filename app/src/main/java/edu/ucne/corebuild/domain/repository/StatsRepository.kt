package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Component
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    suspend fun recordView(componentId: Int)
    suspend fun recordAddedToCart(componentId: Int)
    suspend fun recordPurchase(componentId: Int)
    fun getRecentlyViewed(): Flow<List<Component>>
    fun getTopRated(): Flow<List<Component>>
}
