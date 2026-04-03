package edu.ucne.corebuild.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.ucne.corebuild.data.local.entity.ComponentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComponentDao {
    @Query("SELECT * FROM components")
    fun getComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE id = :id")
    fun getComponentById(id: Int): Flow<ComponentEntity?>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getComponentByIdSync(id: Int): ComponentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(components: List<ComponentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ComponentEntity>)

    @Query("SELECT COUNT(*) FROM components")
    suspend fun getCount(): Int

    @Query("SELECT * FROM components ORDER BY id ASC")
    fun getComponentsPaged(): PagingSource<Int, ComponentEntity>
}
