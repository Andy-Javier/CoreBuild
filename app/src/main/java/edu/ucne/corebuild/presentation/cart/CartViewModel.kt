package edu.ucne.corebuild.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.buildscore.BuildScoreCalculator
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.model.OrderMode
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.OrderRepository
import edu.ucne.corebuild.domain.repository.UserRepository
import edu.ucne.corebuild.presentation.notifications.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

sealed class CartNavigationEvent {
    data object NavigateToLogin : CartNavigationEvent()
    data object NavigateToThanks : CartNavigationEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val compatibilityEngine: CompatibilityEngine,
    private val buildScoreCalculator: BuildScoreCalculator,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<CartNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _showOrderConfirmation = MutableStateFlow(false)

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        cartRepository.getCartTotal(),
        userRepository.getLoggedUser(),
        _showOrderConfirmation
    ) { items, total, user, showConfirmation ->
        val score = buildScoreCalculator.calculateScore(items)
        CartUiState(
            cartItems = items,
            total = total,
            warnings = compatibilityEngine.checkCompatibility(items),
            showOrderConfirmation = showConfirmation,
            buildScore = score.score,
            buildLabel = score.label,
            buildRecommendations = score.recommendations,
            isLoading = false,
            isLogged = user != null
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
                    _snackbarEvent.emit("Producto eliminado")
                }
                is CartEvent.UpdateQuantity -> {
                    val item = uiState.value.cartItems.find { it.component.id == event.componentId }
                    if (item != null) {
                        val limit = compatibilityEngine.getLimitForCategory(item.component)
                        if (event.quantity > limit) {
                            _snackbarEvent.emit("Límite alcanzado: Máximo $limit unidades")
                        } else {
                            cartRepository.updateQuantity(event.componentId, event.quantity)
                        }
                    }
                }
                CartEvent.ClearCart -> {
                    cartRepository.clearCart()
                    _snackbarEvent.emit("Carrito vaciado")
                }
                CartEvent.OnCheckout -> {
                    if (!uiState.value.isLogged) {
                        _navigationEvent.emit(CartNavigationEvent.NavigateToLogin)
                        return@launch
                    }

                    val currentItems = uiState.value.cartItems
                    if (currentItems.isNotEmpty()) {
                        val totalPrice = uiState.value.total
                        val orderComponents = currentItems.flatMap { item -> 
                            List(item.quantity) { item.component } 
                        }
                        
                        val order = Order(
                            components = orderComponents,
                            totalPrice = totalPrice,
                            date = Date(),
                            status = OrderMode.CREATED
                        )
                        orderRepository.createOrder(order)
                        cartRepository.clearCart()
                        
                        _showOrderConfirmation.value = true
                        _snackbarEvent.emit("¡Pedido realizado con éxito!")
                        
                        viewModelScope.launch {
                            delay(5000)
                            val orders = orderRepository.getAllOrders().first()
                            val lastOrder = orders.maxByOrNull { it.id }
                            if (lastOrder != null) {
                                orderRepository.updateOrder(lastOrder.copy(status = OrderMode.ENVIADO))
                            }
                            
                            notificationHelper.sendOrderDeliveredNotification()
                            _showOrderConfirmation.value = false
                        }
                    } else {
                        _snackbarEvent.emit("El carrito está vacío")
                    }
                }
                CartEvent.DismissSnackbar -> { /* Managed by SharedFlow now */ }
                CartEvent.ResetNavigation -> { /* Managed by SharedFlow now */ }
            }
        }
    }
}
