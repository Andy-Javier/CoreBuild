package edu.ucne.corebuild.data.local.dao

import androidx.room.*
import edu.ucne.corebuild.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Int): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}
