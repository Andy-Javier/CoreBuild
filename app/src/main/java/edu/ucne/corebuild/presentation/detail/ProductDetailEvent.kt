package edu.ucne.corebuild.presentation.detail

sealed interface ProductDetailEvent {
    data class LoadComponent(val id: Int) : ProductDetailEvent
}
