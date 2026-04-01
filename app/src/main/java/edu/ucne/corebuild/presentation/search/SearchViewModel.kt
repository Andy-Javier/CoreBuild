package edu.ucne.corebuild.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val componentRepository: ComponentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedBrands = MutableStateFlow<Set<String>>(emptySet())
    private val _priceRange = MutableStateFlow<Pair<Double?, Double?>>(null to null)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        val componentsFlow = componentRepository.getComponents()

        combine(
            _query.debounce(300),
            _selectedCategories,
            _selectedBrands,
            _priceRange,
            componentsFlow
        ) { query, categories, brands, priceRange, components ->
            val filtered = components.filter { component ->
                val matchesQuery = query.isBlank() || component.name.contains(query, ignoreCase = true)
                val matchesCategory = categories.isEmpty() || categories.contains(component.category)
                
                val componentBrand = when(component) {
                    is Component.CPU -> component.brand
                    is Component.GPU -> component.brand
                    is Component.Motherboard -> component.brand
                    is Component.RAM -> component.brand
                    is Component.PSU -> component.brand
                }
                val matchesBrand = brands.isEmpty() || brands.contains(componentBrand)
                
                val (min, max) = priceRange
                val matchesPrice = (min == null || component.price >= min) && 
                                 (max == null || component.price <= max)

                matchesQuery && matchesCategory && matchesBrand && matchesPrice
            }

            val availableCategories = components.map { it.category }.distinct()
            val availableBrands = components.map { component ->
                when(component) {
                    is Component.CPU -> component.brand
                    is Component.GPU -> component.brand
                    is Component.Motherboard -> component.brand
                    is Component.RAM -> component.brand
                    is Component.PSU -> component.brand
                }
            }.distinct()

            SearchUiState(
                query = query,
                components = components,
                filteredComponents = filtered,
                selectedCategories = categories,
                selectedBrands = brands,
                minPrice = priceRange.first,
                maxPrice = priceRange.second,
                availableCategories = availableCategories,
                availableBrands = availableBrands
            )
        }.onEach { state ->
            _uiState.update { state }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> _query.value = event.query
            is SearchEvent.OnCategorySelect -> {
                val current = _selectedCategories.value
                _selectedCategories.value = if (current.contains(event.category)) {
                    current - event.category
                } else {
                    current + event.category
                }
            }
            is SearchEvent.OnBrandSelect -> {
                val current = _selectedBrands.value
                _selectedBrands.value = if (current.contains(event.brand)) {
                    current - event.brand
                } else {
                    current + event.brand
                }
            }
            is SearchEvent.OnPriceRangeChange -> {
                _priceRange.value = event.min to event.max
            }
            SearchEvent.OnClearFilters -> {
                _selectedCategories.value = emptySet()
                _selectedBrands.value = emptySet()
                _priceRange.value = null to null
            }
        }
    }
}
