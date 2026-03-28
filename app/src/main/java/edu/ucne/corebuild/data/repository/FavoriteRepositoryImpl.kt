package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.FavoriteDao
import edu.ucne.corebuild.data.local.entity.FavoriteEntity
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavoriteComponents(): Flow<List<Component>> {
        return favoriteDao.getFavoriteComponents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavorite(componentId: Int): Flow<Boolean> {
        return favoriteDao.isFavorite(componentId)
    }

    override suspend fun toggleFavorite(componentId: Int) {
        val isFav = favoriteDao.isFavorite(componentId).first()
        if (isFav) {
            favoriteDao.removeFavorite(FavoriteEntity(componentId))
        } else {
            favoriteDao.addFavorite(FavoriteEntity(componentId))
        }
    }
}
