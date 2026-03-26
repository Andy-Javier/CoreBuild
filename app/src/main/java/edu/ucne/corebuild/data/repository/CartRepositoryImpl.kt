package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems

    override fun getCartTotal(): Flow<Double> = _cartItems.map { items ->
        items.sumOf { it.total }
    }

    override fun getCartItemCount(): Flow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }

    override suspend fun addComponent(component: Component, quantity: Int) {
        _cartItems.update { items ->
            val limit = compatibilityEngine.getLimitForCategory(component)
            val existingItem = items.find { it.component.id == component.id }
            
            if (existingItem != null) {
                val newQuantity = (existingItem.quantity + quantity).coerceAtMost(limit)
                items.map {
                    if (it.component.id == component.id) it.copy(quantity = newQuantity)
                    else it
                }
            } else {
                val initialQuantity = quantity.coerceAtMost(limit)
                items + CartItem(component, initialQuantity)
            }
        }
    }

    override suspend fun removeComponent(componentId: Int) {
        _cartItems.update { items ->
            items.filterNot { it.component.id == componentId }
        }
    }

    override suspend fun updateQuantity(componentId: Int, quantity: Int) {
        _cartItems.update { items ->
            if (quantity <= 0) {
                items.filterNot { it.component.id == componentId }
            } else {
                items.map {
                    if (it.component.id == componentId) {
                        val limit = compatibilityEngine.getLimitForCategory(it.component)
                        it.copy(quantity = quantity.coerceAtMost(limit))
                    } else it
                }
            }
        }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }
}
