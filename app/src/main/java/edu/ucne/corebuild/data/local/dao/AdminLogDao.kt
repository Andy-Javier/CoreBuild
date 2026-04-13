package edu.ucne.corebuild.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.ucne.corebuild.data.local.entity.AdminLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminLogDao {
    @Insert
    suspend fun insert(log: AdminLogEntity)

    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AdminLogEntity>>
}
