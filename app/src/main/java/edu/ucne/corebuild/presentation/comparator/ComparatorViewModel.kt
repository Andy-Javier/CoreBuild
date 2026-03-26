package edu.ucne.corebuild.presentation.comparator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComparatorViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComparatorUiState())
    val uiState: StateFlow<ComparatorUiState> = _uiState.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getComponentsUseCase().collect { components ->
                _uiState.update { 
                    it.copy(
                        components = components,
                        isLoading = false
                    )
                }
                updateFilteredComponents(_uiState.value.selectedType)
            }
        }
    }

    fun onEvent(event: ComparatorEvent) {
        when (event) {
            is ComparatorEvent.SelectType -> {
                _uiState.update { 
                    it.copy(
                        selectedType = event.type,
                        selectedComponent1 = null,
                        selectedComponent2 = null
                    ) 
                }
                updateFilteredComponents(event.type)
            }
            is ComparatorEvent.SelectComponent1 -> {
                _uiState.update { it.copy(selectedComponent1 = event.component) }
            }
            is ComparatorEvent.SelectComponent2 -> {
                _uiState.update { it.copy(selectedComponent2 = event.component) }
            }
        }
    }

    private fun updateFilteredComponents(type: String) {
        val filtered = _uiState.value.components.filter { component ->
            when (type) {
                "CPU" -> component is Component.CPU
                "GPU" -> component is Component.GPU
                else -> false
            }
        }
        _uiState.update { it.copy(filteredComponents = filtered) }
    }
}
