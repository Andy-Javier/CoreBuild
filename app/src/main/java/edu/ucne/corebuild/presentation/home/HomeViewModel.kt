package edu.ucne.corebuild.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        onEvent(HomeEvent.LoadComponents)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadComponents -> loadComponents()
        }
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
            }
        }
    }
}
