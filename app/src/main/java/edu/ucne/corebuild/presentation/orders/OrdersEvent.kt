package edu.ucne.corebuild.presentation.orders

sealed class OrdersEvent {
    object OnLoadOrders : OrdersEvent()
    object OnCreateOrder : OrdersEvent()
}
