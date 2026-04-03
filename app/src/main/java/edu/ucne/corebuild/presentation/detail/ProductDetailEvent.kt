package edu.ucne.corebuild.presentation.detail

import edu.ucne.corebuild.domain.model.Component

sealed interface ProductDetailEvent {
    data class LoadComponent(val id: Int) : ProductDetailEvent
    data class AddToCart(val component: Component, val quantity: Int) : ProductDetailEvent
    data class OnBuyNow(val component: Component, val quantity: Int) : ProductDetailEvent
    data object OnToggleFavorite : ProductDetailEvent
    data object DismissSnackbar : ProductDetailEvent
}
