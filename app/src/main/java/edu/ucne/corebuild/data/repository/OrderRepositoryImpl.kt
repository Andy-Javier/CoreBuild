package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.OrderDao
import edu.ucne.corebuild.data.local.entity.OrderEntity
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.model.OrderStatus
import edu.ucne.corebuild.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { entity ->
                Order(
                    id = entity.id,
                    components = emptyList(), // Por ahora vacío, se requiere deserialización si se necesita en UI
                    totalPrice = entity.totalPrice,
                    date = Date(entity.date),
                    status = OrderStatus.valueOf(entity.status)
                )
            }
        }
    }

    override suspend fun createOrder(order: Order) {
        val entity = OrderEntity(
            id = order.id,
            componentsJson = "", // Se podría usar Json.encodeToString(order.components) con un serializer custom
            totalPrice = order.totalPrice,
            date = order.date.time,
            status = order.status.name
        )
        orderDao.insertOrder(entity)
    }
}
