package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartTotal(): Flow<Double>
    fun getCartItemCount(): Flow<Int>
    suspend fun addComponent(component: Component)
    suspend fun removeComponent(componentId: Int)
    suspend fun updateQuantity(componentId: Int, quantity: Int)
    suspend fun clearCart()
}
