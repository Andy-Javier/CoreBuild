package edu.ucne.corebuild.data.local.dao

import androidx.room.*
import edu.ucne.corebuild.data.local.entity.ComponentEntity
import edu.ucne.corebuild.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getFavoriteIds(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE componentId = :id)")
    fun isFavorite(id: Int): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM components WHERE id IN (SELECT componentId FROM favorites)")
    fun getFavoriteComponents(): Flow<List<ComponentEntity>>
}
