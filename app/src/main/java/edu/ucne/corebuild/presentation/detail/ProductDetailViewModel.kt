package edu.ucne.corebuild.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.use_case.GetComponentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getComponentUseCase: GetComponentUseCase,
    private val cartRepository: CartRepository,
    private val compatibilityEngine: CompatibilityEngine
) : ViewModel() {

    private val _componentState = MutableStateFlow<Component?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductDetailUiState> = combine(
        _componentState,
        _isLoading,
        _snackbarMessage,
        cartRepository.getCartItems()
    ) { component, loading, message, cartItems ->
        val limit = component?.let { compatibilityEngine.getLimitForCategory(it) } ?: 1
        val currentInCart = cartItems.find { it.component.id == component?.id }?.quantity ?: 0
        
        ProductDetailUiState(
            component = component,
            isLoading = loading,
            snackbarMessage = message,
            quantityLimit = limit,
            currentInCart = currentInCart
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductDetailUiState(isLoading = true)
    )

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.LoadComponent -> loadComponent(event.id)
            is ProductDetailEvent.AddToCart -> {
                viewModelScope.launch {
                    val state = uiState.value
                    val limit = state.quantityLimit
                    val totalAfterAdd = state.currentInCart + event.quantity
                    
                    if (totalAfterAdd > limit) {
                         val availableToAdd = limit - state.currentInCart
                         if (availableToAdd > 0) {
                             cartRepository.addComponent(event.component, availableToAdd)
                             _snackbarMessage.value = "Límite alcanzado. Solo se agregaron $availableToAdd unidades."
                         } else {
                             _snackbarMessage.value = "No se puede agregar más. Límite de $limit unidades ya alcanzado."
                         }
                    } else {
                        cartRepository.addComponent(event.component, event.quantity)
                        _snackbarMessage.value = "Agregado al carrito"
                    }
                }
            }
            is ProductDetailEvent.DismissSnackbar -> {
                _snackbarMessage.value = null
            }
        }
    }

    private fun loadComponent(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            getComponentUseCase(id).collect { component ->
                _componentState.value = component
                _isLoading.value = false
            }
        }
    }
    
    fun showLimitReachedMessage() {
        val limit = uiState.value.quantityLimit
        _snackbarMessage.value = "Límite alcanzado: Máximo $limit unidades en total"
    }
}
