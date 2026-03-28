package edu.ucne.corebuild.domain.model

import java.util.Date

data class Order(
    val id: Int? = null,
    val components: List<Component>,
    val totalPrice: Double,
    val date: Date,
    val status: OrderStatus = OrderStatus.CREATED
)

enum class OrderStatus {
    CREATED,
    DELIVERED
}
