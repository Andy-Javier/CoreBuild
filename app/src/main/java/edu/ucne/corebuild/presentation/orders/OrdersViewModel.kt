package edu.ucne.corebuild.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState(isLoading = true))
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        orderRepository.getAllOrders()
            .onEach { orders ->
                _uiState.update { it.copy(orders = orders, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: OrdersEvent) {
        when (event) {
            is OrdersEvent.OnLoadOrders -> {
                loadOrders()
            }
            is OrdersEvent.OnCreateOrder -> {
                viewModelScope.launch {
                    orderRepository.createOrder(event.order)
                }
            }
        }
    }
}
