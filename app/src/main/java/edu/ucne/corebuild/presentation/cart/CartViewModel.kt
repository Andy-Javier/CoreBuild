package edu.ucne.corebuild.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.buildscore.BuildScoreCalculator
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.OrderRepository
import edu.ucne.corebuild.presentation.notifications.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val compatibilityEngine: CompatibilityEngine,
    private val buildScoreCalculator: BuildScoreCalculator,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _showOrderConfirmation = MutableStateFlow(false)

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        cartRepository.getCartTotal(),
        _snackbarMessage,
        _showOrderConfirmation
    ) { items, total, message, showConfirmation ->
        val score = buildScoreCalculator.calculateScore(items)
        CartUiState(
            cartItems = items,
            total = total,
            warnings = compatibilityEngine.checkCompatibility(items),
            snackbarMessage = message,
            showOrderConfirmation = showConfirmation,
            buildScore = score.score,
            buildLabel = score.label,
            buildRecommendations = score.recommendations,
            isLoading = false
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
                CartEvent.OnCheckout -> {
                    val currentItems = uiState.value.cartItems
                    if (currentItems.isNotEmpty()) {
                        val order = Order(
                            components = currentItems.flatMap { item -> 
                                List(item.quantity) { item.component } 
                            },
                            totalPrice = uiState.value.total,
                            date = Date()
                        )
                        orderRepository.createOrder(order)
                        cartRepository.clearCart()
                        
                        // Flujo de simulación de entrega
                        _showOrderConfirmation.value = true
                        _snackbarMessage.value = "¡Pedido realizado con éxito!"
                        
                        viewModelScope.launch {
                            delay(5000) // Simular preparación y entrega
                            notificationHelper.sendOrderDeliveredNotification()
                            delay(2000)
                            _showOrderConfirmation.value = false
                        }
                    } else {
                        _snackbarMessage.value = "El carrito está vacío"
                    }
                }
                CartEvent.DismissSnackbar -> {
                    _snackbarMessage.value = null
                }
            }
        }
    }
}
