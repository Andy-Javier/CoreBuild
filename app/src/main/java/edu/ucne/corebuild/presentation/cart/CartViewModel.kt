package edu.ucne.corebuild.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val compatibilityEngine: CompatibilityEngine
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        cartRepository.getCartTotal()
    ) { items, total ->
        CartUiState(
            cartItems = items,
            total = total,
            warnings = compatibilityEngine.checkCompatibility(items)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartUiState(isLoading = true)
    )

    fun onEvent(event: CartEvent) {
        viewModelScope.launch {
            when (event) {
                is CartEvent.RemoveFromCart -> {
                    cartRepository.removeComponent(event.componentId)
                }
                is CartEvent.UpdateQuantity -> {
                    cartRepository.updateQuantity(event.componentId, event.quantity)
                }
                CartEvent.ClearCart -> {
                    cartRepository.clearCart()
                }
            }
        }
    }
}
