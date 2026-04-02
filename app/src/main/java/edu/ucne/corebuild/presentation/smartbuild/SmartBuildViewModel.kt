package edu.ucne.corebuild.presentation.smartbuild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.domain.smartbuilder.SmartBuild
import edu.ucne.corebuild.domain.smartbuilder.SmartBuildGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SmartBuildNavigationEvent {
    data object NavigateToCart : SmartBuildNavigationEvent()
}

data class SmartBuildFormState(
    val cpuModeEnabled: Boolean = true,
    val gpuModeEnabled: Boolean = false,
    val selectedCpu: Component.CPU? = null,
    val selectedGpu: Component.GPU? = null,
    val cpus: List<Component.CPU> = emptyList(),
    val gpus: List<Component.GPU> = emptyList()
) {
    val isReadyToBuild: Boolean get() =
        (cpuModeEnabled && selectedCpu != null) ||
                (gpuModeEnabled && selectedGpu != null)
}

sealed class SmartBuildUiState {
    object Idle : SmartBuildUiState()
    object Loading : SmartBuildUiState()
    data class Success(val build: SmartBuild) : SmartBuildUiState()
    data class Error(val message: String) : SmartBuildUiState()
}

data class GroupedComponents<T : Component>(
    val brand: String,
    val items: List<T>
)

fun List<Component.CPU>.groupCpusByBrand(): List<GroupedComponents<Component.CPU>> {
    val map = linkedMapOf("Intel" to mutableListOf<Component.CPU>(), "Ryzen" to mutableListOf(), "Otro" to mutableListOf())
    forEach { cpu ->
        val n = cpu.name.lowercase()
        when {
            n.contains("intel") || n.contains("core") -> map["Intel"]!!.add(cpu)
            n.contains("ryzen") || n.contains("amd")  -> map["Ryzen"]!!.add(cpu)
            else                                        -> map["Otro"]!!.add(cpu)
        }
    }
    return map.entries.filter { it.value.isNotEmpty() }.map { GroupedComponents(it.key, it.value) }
}

fun List<Component.GPU>.groupGpusByBrand(): List<GroupedComponents<Component.GPU>> {
    val map = linkedMapOf("Nvidia" to mutableListOf<Component.GPU>(), "Radeon" to mutableListOf(), "Otro" to mutableListOf())
    forEach { gpu ->
        val n = gpu.name.lowercase()
        when {
            n.contains("nvidia") || n.contains("rtx") || n.contains("gtx") || n.contains("geforce") -> map["Nvidia"]!!.add(gpu)
            n.contains("radeon") || n.contains("rx ") || (n.contains("amd") && !n.contains("ryzen")) -> map["Radeon"]!!.add(gpu)
            else -> map["Otro"]!!.add(gpu)
        }
    }
    return map.entries.filter { it.value.isNotEmpty() }.map { GroupedComponents(it.key, it.value) }
}

@HiltViewModel
class SmartBuildViewModel @Inject constructor(
    private val componentRepository: ComponentRepository,
    private val smartBuildGenerator: SmartBuildGenerator,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(SmartBuildFormState())
    val formState: StateFlow<SmartBuildFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<SmartBuildUiState>(SmartBuildUiState.Idle)
    val uiState: StateFlow<SmartBuildUiState> = _uiState.asStateFlow()

    private val _cpuSearchQuery = MutableStateFlow("")
    val cpuSearchQuery: StateFlow<String> = _cpuSearchQuery.asStateFlow()

    private val _gpuSearchQuery = MutableStateFlow("")
    val gpuSearchQuery: StateFlow<String> = _gpuSearchQuery.asStateFlow()

    // true = mostrar el diálogo de confirmación de guardar
    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SmartBuildNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val filteredCpuGroups: StateFlow<List<GroupedComponents<Component.CPU>>> =
        combine(_formState, _cpuSearchQuery) { state, query ->
            val filtered = if (query.isBlank()) state.cpus
            else state.cpus.filter { it.name.contains(query, ignoreCase = true) }
            filtered.groupCpusByBrand()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredGpuGroups: StateFlow<List<GroupedComponents<Component.GPU>>> =
        combine(_formState, _gpuSearchQuery) { state, query ->
            val filtered = if (query.isBlank()) state.gpus
            else state.gpus.filter { it.name.contains(query, ignoreCase = true) }
            filtered.groupGpusByBrand()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { loadComponents() }

    private fun loadComponents() {
        viewModelScope.launch {
            componentRepository.getComponents().collect { components ->
                _formState.update { state ->
                    state.copy(
                        cpus = components.filterIsInstance<Component.CPU>(),
                        gpus = components.filterIsInstance<Component.GPU>()
                    )
                }
            }
        }
    }

    fun onCpuSearch(query: String) { _cpuSearchQuery.value = query }
    fun onGpuSearch(query: String) { _gpuSearchQuery.value = query }

    fun toggleCpuMode() {
        if (!_formState.value.gpuModeEnabled && _formState.value.cpuModeEnabled) return
        _formState.update { it.copy(cpuModeEnabled = !it.cpuModeEnabled, selectedCpu = null) }
        _cpuSearchQuery.value = ""
    }

    fun toggleGpuMode() {
        if (!_formState.value.cpuModeEnabled && _formState.value.gpuModeEnabled) return
        _formState.update { it.copy(gpuModeEnabled = !it.gpuModeEnabled, selectedGpu = null) }
        _gpuSearchQuery.value = ""
    }

    fun selectCpu(component: Component.CPU) { _formState.update { it.copy(selectedCpu = component) } }
    fun selectGpu(component: Component.GPU) { _formState.update { it.copy(selectedGpu = component) } }

    fun buildNow() {
        val state = _formState.value
        if (!state.isReadyToBuild) return
        viewModelScope.launch {
            _uiState.value = SmartBuildUiState.Loading
            try {
                val allComponents = componentRepository.getComponents().first()
                val result = smartBuildGenerator.generateBuild(
                    anchorCpu = if (state.cpuModeEnabled) state.selectedCpu else null,
                    anchorGpu = if (state.gpuModeEnabled) state.selectedGpu else null,
                    allComponents = allComponents
                )
                _uiState.value = SmartBuildUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = SmartBuildUiState.Error(e.message ?: "Ocurrió un error desconocido")
            }
        }
    }

    fun requestSaveBuild() { _showSaveDialog.value = true }
    fun dismissSaveDialog() { _showSaveDialog.value = false }

    fun confirmSaveBuild() {
        val success = _uiState.value as? SmartBuildUiState.Success ?: return
        _showSaveDialog.value = false
        viewModelScope.launch {
            val build = success.build
            val allComponents = buildList {
                build.anchorCpu?.let { add(it as Component) }
                build.anchorGpu?.let { add(it as Component) }
                addAll(build.suggested)
            }
            allComponents.forEach { cartRepository.addComponent(it, 1) }
            _navigationEvent.emit(SmartBuildNavigationEvent.NavigateToCart)
        }
    }

    fun resetToForm() { _uiState.value = SmartBuildUiState.Idle }
}