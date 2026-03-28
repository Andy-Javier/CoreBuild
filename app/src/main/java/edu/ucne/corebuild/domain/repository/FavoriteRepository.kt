package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Component
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteComponents(): Flow<List<Component>>
    fun isFavorite(componentId: Int): Flow<Boolean>
    suspend fun toggleFavorite(componentId: Int)
}
