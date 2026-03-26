package edu.ucne.corebuild.presentation.cart

sealed interface CartEvent {
    data class RemoveFromCart(val componentId: Int) : CartEvent
    data class UpdateQuantity(val componentId: Int, val quantity: Int) : CartEvent
    data object ClearCart : CartEvent
    data object DismissSnackbar : CartEvent
}
