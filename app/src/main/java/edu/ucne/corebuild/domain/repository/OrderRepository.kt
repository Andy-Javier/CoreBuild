package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    suspend fun createOrder(order: Order)
}
