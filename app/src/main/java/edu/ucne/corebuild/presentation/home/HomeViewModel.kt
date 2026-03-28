package edu.ucne.corebuild.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val _debouncedQuery = _searchQuery.debounce(300)

    val uiState: StateFlow<HomeUiState> = combine(
        getComponentsUseCase(),
        _searchQuery,
        _debouncedQuery,
        _isLoading
    ) { components, immediateQuery, debouncedQuery, loading ->
        val filtered = if (debouncedQuery.isBlank()) {
            components
        } else {
            components.filter {
                it.name.contains(debouncedQuery, ignoreCase = true) ||
                it.category.contains(debouncedQuery, ignoreCase = true)
            }
        }
        HomeUiState(
            components = components,
            filteredComponents = filtered,
            searchQuery = immediateQuery,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            HomeEvent.LoadComponents -> {
                // El flujo reactivo de getComponentsUseCase ya maneja la carga
            }
        }
    }
}
