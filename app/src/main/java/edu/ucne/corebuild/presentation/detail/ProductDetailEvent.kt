package edu.ucne.corebuild.presentation.detail

import edu.ucne.corebuild.domain.model.Component

sealed class ProductDetailEvent {
    data class LoadComponent(val id: Int) : ProductDetailEvent()
    data class AddToCart(val component: Component, val quantity: Int) : ProductDetailEvent()
    data class OnBuyNow(val component: Component) : ProductDetailEvent()
    object DismissSnackbar : ProductDetailEvent()
}
