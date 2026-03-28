package edu.ucne.corebuild.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        onEvent(OrdersEvent.OnLoadOrders)
    }

    fun onEvent(event: OrdersEvent) {
        when (event) {
            is OrdersEvent.OnLoadOrders -> loadOrders()
            is OrdersEvent.OnCreateOrder -> createOrder()
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.getAllOrders()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { orders ->
                    _uiState.update { it.copy(isLoading = false, orders = orders) }
                }
        }
    }

    private fun createOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cartItems = cartRepository.getCartItems().first()
                val total = cartRepository.getCartTotal().first()
                
                if (cartItems.isNotEmpty()) {
                    val order = Order(
                        components = cartItems.map { it.component },
                        totalPrice = total,
                        date = Date()
                    )
                    orderRepository.createOrder(order)
                    cartRepository.clearCart()
                    loadOrders()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "El carrito está vacío") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
