package edu.ucne.corebuild.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.FavoriteRepository
import edu.ucne.corebuild.domain.repository.OrderRepository
import edu.ucne.corebuild.domain.repository.StatsRepository
import edu.ucne.corebuild.domain.use_case.GetComponentUseCase
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getComponentUseCase: GetComponentUseCase,
    private val getComponentsUseCase: GetComponentsUseCase,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val favoriteRepository: FavoriteRepository,
    private val statsRepository: StatsRepository,
    private val compatibilityEngine: CompatibilityEngine
) : ViewModel() {

    private val _componentId = MutableStateFlow<Int?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _orderCompleted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _component = _componentId.filterNotNull().flatMapLatest { id ->
        getComponentUseCase(id).onEach { component ->
            component?.let { statsRepository.recordView(it.id) }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _isFavorite = _componentId.filterNotNull().flatMapLatest { id ->
        favoriteRepository.isFavorite(id)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _variants = _component.filterNotNull().flatMapLatest { component ->
        if (component is Component.RAM) {
            val baseName = extractBaseRamName(component.name)
            getComponentsUseCase().map { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.filterIsInstance<Component.RAM>()?.filter { 
                            extractBaseRamName(it.name) == baseName 
                        } ?: emptyList()
                    }
                    else -> emptyList()
                }
            }
        } else {
            flowOf(emptyList<Component>())
        }
    }

    private val _operationState = combine(_isLoading, _snackbarMessage, _orderCompleted, _error) { loading, msg, completed, error ->
        loading to (msg to (completed to error))
    }

    val uiState: StateFlow<ProductDetailUiState> = combine(
        _component,
        _isFavorite,
        _operationState,
        cartRepository.getCartItems(),
        _variants
    ) { component, isFav, op, cartItems, variants ->
        val (loading, nested) = op
        val (message, last) = nested
        val (completed, error) = last
        
        val limit = component?.let { compatibilityEngine.getLimitForCategory(it) } ?: 3
        val currentInCart = cartItems.find { it.component.id == component?.id }?.quantity ?: 0
        
        ProductDetailUiState(
            component = component,
            variants = variants,
            isFavorite = isFav,
            isLoading = loading,
            snackbarMessage = message,
            error = error,
            quantityLimit = limit,
            currentInCart = currentInCart,
            orderCompleted = completed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductDetailUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            getComponentsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _isLoading.value = true
                    is Resource.Success -> _isLoading.value = false
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                    }
                }
            }
        }
    }

    private fun extractBaseRamName(name: String): String {
        val configPatterns = listOf(
            Regex("""\s\d+x\d+GB.*""", RegexOption.IGNORE_CASE),
            Regex("""\s\d+GB.*""", RegexOption.IGNORE_CASE)
        )
        var baseName = name
        for (pattern in configPatterns) {
            val match = pattern.find(baseName)
            if (match != null) {
                baseName = baseName.substring(0, match.range.first).trim()
                break
            }
        }
        return baseName
    }

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.LoadComponent -> {
                _componentId.value = event.id
            }
            is ProductDetailEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    _componentId.value?.let { id ->
                        favoriteRepository.toggleFavorite(id)
                    }
                }
            }
            is ProductDetailEvent.AddToCart -> {
                viewModelScope.launch {
                    val state = uiState.value
                    val limit = state.quantityLimit
                    val totalAfterAdd = state.currentInCart + event.quantity
                    
                    if (totalAfterAdd > limit) {
                         val availableToAdd = limit - state.currentInCart
                         if (availableToAdd > 0) {
                             cartRepository.addComponent(event.component, availableToAdd)
                             statsRepository.recordAddedToCart(event.component.id)
                             _snackbarMessage.value = "Límite alcanzado. Solo se agregaron $availableToAdd unidades."
                         } else {
                             _snackbarMessage.value = "No se puede agregar más. Límite de $limit unidades ya alcanzado."
                         }
                    } else {
                        cartRepository.addComponent(event.component, event.quantity)
                        statsRepository.recordAddedToCart(event.component.id)
                        _snackbarMessage.value = "Agregado al carrito"
                    }
                }
            }
            is ProductDetailEvent.OnBuyNow -> {
                viewModelScope.launch {
                    try {
                        val order = Order(
                            components = List(event.quantity) { event.component },
                            totalPrice = event.component.price * event.quantity,
                            date = Date()
                        )
                        orderRepository.createOrder(order)
                        statsRepository.recordPurchase(event.component.id)
                        _orderCompleted.value = true
                        _snackbarMessage.value = "¡Compra realizada con éxito!"
                    } catch (e: Exception) {
                        _snackbarMessage.value = "Error al procesar la compra: ${e.message}"
                    }
                }
            }
            is ProductDetailEvent.DismissSnackbar -> {
                _snackbarMessage.value = null
            }
        }
    }
}
