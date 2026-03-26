package edu.ucne.corebuild.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.use_case.GetComponentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getComponentUseCase: GetComponentUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.LoadComponent -> loadComponent(event.id)
            is ProductDetailEvent.AddToCart -> {
                viewModelScope.launch {
                    cartRepository.addComponent(event.component)
                }
            }
        }
    }

    private fun loadComponent(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getComponentUseCase(id).collect { component ->
                _uiState.update {
                    it.copy(
                        component = component,
                        isLoading = false
                    )
                }
            }
        }
    }
}
