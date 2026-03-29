package edu.ucne.corebuild.data.local.dao

import androidx.room.*
import edu.ucne.corebuild.data.local.entity.StatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Query("SELECT * FROM component_stats WHERE componentId = :id")
    suspend fun getStatsForComponent(id: Int): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: StatsEntity)

    @Query("SELECT * FROM component_stats ORDER BY lastViewed DESC LIMIT 10")
    fun getRecentlyViewed(): Flow<List<StatsEntity>>

    @Query("SELECT * FROM component_stats ORDER BY purchases DESC, addedToCart DESC, views DESC LIMIT 20")
    fun getTopRated(): Flow<List<StatsEntity>>
}
