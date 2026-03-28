package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.CartDao
import edu.ucne.corebuild.data.local.entity.CartEntity
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val compatibilityEngine: CompatibilityEngine
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> = 
        cartDao.getCartItemsWithComponents().map { list ->
            list.map { itemWithComp ->
                CartItem(
                    component = itemWithComp.component.toDomain(),
                    quantity = itemWithComp.cart.quantity
                )
            }
        }

    override fun getCartTotal(): Flow<Double> = getCartItems().map { items ->
        items.sumOf { it.total }
    }

    override fun getCartItemCount(): Flow<Int> = getCartItems().map { items ->
        items.sumOf { it.quantity }
    }

    override suspend fun addComponent(component: Component, quantity: Int) {
        val existing = cartDao.getCartItemById(component.id)
        val limit = compatibilityEngine.getLimitForCategory(component)
        
        if (existing != null) {
            val newQuantity = (existing.quantity + quantity).coerceAtMost(limit)
            cartDao.updateQuantity(component.id, newQuantity)
        } else {
            val initialQuantity = quantity.coerceAtMost(limit)
            cartDao.insert(CartEntity(component.id, initialQuantity))
        }
    }

    override suspend fun removeComponent(componentId: Int) {
        val existing = cartDao.getCartItemById(componentId)
        if (existing != null) {
            cartDao.delete(existing)
        }
    }

    override suspend fun updateQuantity(componentId: Int, quantity: Int) {
        if (quantity <= 0) {
            val existing = cartDao.getCartItemById(componentId)
            if (existing != null) cartDao.delete(existing)
        } else {
            cartDao.updateQuantity(componentId, quantity)
        }
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
