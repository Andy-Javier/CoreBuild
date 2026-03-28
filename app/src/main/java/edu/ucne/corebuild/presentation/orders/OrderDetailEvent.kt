package edu.ucne.corebuild.presentation.orders

sealed interface OrderDetailEvent {
    data class LoadOrder(val id: Int) : OrderDetailEvent
}
