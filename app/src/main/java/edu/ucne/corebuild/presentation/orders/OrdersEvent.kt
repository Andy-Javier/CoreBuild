package edu.ucne.corebuild.presentation.orders

import edu.ucne.corebuild.domain.model.Order

sealed interface OrdersEvent {
    data object OnLoadOrders : OrdersEvent
    data class OnCreateOrder(val order: Order) : OrdersEvent
}
