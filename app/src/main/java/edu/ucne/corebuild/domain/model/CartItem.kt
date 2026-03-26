package edu.ucne.corebuild.domain.model

data class CartItem(
    val component: Component,
    val quantity: Int = 1
) {
    val total: Double get() = component.price * quantity
}
