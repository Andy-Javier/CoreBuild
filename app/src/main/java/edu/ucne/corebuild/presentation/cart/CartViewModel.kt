package edu.ucne.corebuild.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.buildscore.BuildScoreCalculator
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val compatibilityEngine: CompatibilityEngine,
    private val buildScoreCalculator: BuildScoreCalculator
) : ViewModel() {

    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        cartRepository.getCartTotal(),
        _snackbarMessage
    ) { items, total, message ->
        val score = buildScoreCalculator.calculateScore(items)
        CartUiState(
            cartItems = items,
            total = total,
            warnings = compatibilityEngine.checkCompatibility(items),
            snackbarMessage = message,
            buildScore = score.score,
            buildLabel = score.label,
            buildRecommendations = score.recommendations
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
                    _snackbarMessage.value = "Producto eliminado"
                }
                is CartEvent.UpdateQuantity -> {
                    val item = uiState.value.cartItems.find { it.component.id == event.componentId }
                    if (item != null) {
                        val limit = compatibilityEngine.getLimitForCategory(item.component)
                        if (event.quantity > limit) {
                            _snackbarMessage.value = "Límite alcanzado: Máximo $limit unidades"
                        } else {
                            cartRepository.updateQuantity(event.componentId, event.quantity)
                        }
                    }
                }
                CartEvent.ClearCart -> {
                    cartRepository.clearCart()
                    _snackbarMessage.value = "Carrito vaciado"
                }
                CartEvent.DismissSnackbar -> {
                    _snackbarMessage.value = null
                }
            }
        }
    }
}
