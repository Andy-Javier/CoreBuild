package edu.ucne.corebuild.domain.model

import java.util.Date

data class Order(
    val id: Int = 0,
    val components: List<Component>,
    val totalPrice: Double,
    val date: Date,
    val status: OrderStatus = OrderMode.CREATED
)

enum class OrderMode {
    CREATED, ENVIADO, ENTREGADO
}

typealias OrderStatus = OrderMode
