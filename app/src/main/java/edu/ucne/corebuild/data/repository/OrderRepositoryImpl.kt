package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.OrderDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOrderById(id: Int): Flow<Order?> {
        return orderDao.getOrderById(id).map { it?.toDomain() }
    }

    override suspend fun createOrder(order: Order) {
        orderDao.insertOrder(order.toEntity())
    }
}
