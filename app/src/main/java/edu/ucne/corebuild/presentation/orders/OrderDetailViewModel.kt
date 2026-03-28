package edu.ucne.corebuild.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: OrderDetailEvent) {
        when (event) {
            is OrderDetailEvent.LoadOrder -> loadOrder(event.id)
        }
    }

    private fun loadOrder(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.getOrderById(id)
                .onEach { order ->
                    _uiState.update { it.copy(order = order, isLoading = false) }
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .launchIn(viewModelScope)
        }
    }
}
