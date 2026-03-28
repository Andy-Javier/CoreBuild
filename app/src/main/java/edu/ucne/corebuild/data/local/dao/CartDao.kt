package edu.ucne.corebuild.data.local.dao

import androidx.room.*
import edu.ucne.corebuild.data.local.entity.CartEntity
import edu.ucne.corebuild.data.local.entity.CartItemWithComponent
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Transaction
    @Query("SELECT * FROM cart_items")
    fun getCartItemsWithComponents(): Flow<List<CartItemWithComponent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartEntity)

    @Delete
    suspend fun delete(cartItem: CartEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE componentId = :componentId")
    suspend fun updateQuantity(componentId: Int, quantity: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM cart_items WHERE componentId = :componentId")
    suspend fun getCartItemById(componentId: Int): CartEntity?
}
