package edu.ucne.corebuild.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.CartRepository
import edu.ucne.corebuild.domain.repository.StatsRepository
import edu.ucne.corebuild.domain.use_case.GetComponentsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeNavigationEvent {
    data object NavigateToCart : HomeNavigationEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase,
    private val statsRepository: StatsRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _showBuildDialog = MutableStateFlow(false)
    private val _featuredBuild = MutableStateFlow<PredefinedBuild?>(null)

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val _debouncedQuery = _searchQuery.debounce(300)

    init {
        viewModelScope.launch {
            getComponentsUseCase()
                .filter { it.isNotEmpty() }
                .firstOrNull()
                .let { components ->
                    if (components != null) {
                        generateRandomBuild(components)
                    }
                }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            getComponentsUseCase(),
            statsRepository.getRecentlyViewed(),
            statsRepository.getTopRated()
        ) { c, rv, tr -> Triple(c, rv, tr) },
        combine(
            _searchQuery,
            _debouncedQuery,
            _selectedCategory
        ) { q, dq, cat -> Triple(q, dq, cat) },
        combine(
            _isLoading,
            _featuredBuild,
            _showBuildDialog
        ) { l, f, s -> Triple(l, f, s) }
    ) { data, inputs, ui ->
        val (components, recentlyViewed, topRated) = data
        val (immediateQuery, debouncedQuery, selectedCat) = inputs
        val (loading, featured, showDialog) = ui

        val filtered = components.filter { component ->
            val matchesQuery = if (debouncedQuery.isBlank()) true 
                               else component.name.contains(debouncedQuery, ignoreCase = true)
            
            val matchesCategory = when (selectedCat) {
                null -> true
                "CPU" -> component.category == "Procesador"
                "GPU" -> component.category == "Tarjeta Gráfica"
                "RAM" -> component.category == "Memoria RAM"
                "Motherboard" -> component.category == "Placa Base"
                "PSU" -> component.category == "Fuente de Poder"
                else -> component.category.equals(selectedCat, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }

        val intel = components.filter { it is Component.CPU && it.brand.contains("Intel", ignoreCase = true) }
        val amdCpu = components.filter { it is Component.CPU && it.brand.contains("AMD", ignoreCase = true) }
        val nvidia = components.filter { it is Component.GPU && (it.brand.contains("NVIDIA", ignoreCase = true) || it.name.contains("RTX", ignoreCase = true) || it.name.contains("GTX", ignoreCase = true)) }
        val radeon = components.filter { it is Component.GPU && (it.brand.contains("AMD", ignoreCase = true) || it.brand.contains("Radeon", ignoreCase = true) || it.name.contains("RX ", ignoreCase = true)) }

        HomeUiState(
            components = components,
            filteredComponents = filtered,
            recentlyViewed = recentlyViewed,
            topRated = topRated,
            intelComponents = intel,
            amdCpuComponents = amdCpu,
            nvidiaComponents = nvidia,
            radeonComponents = radeon,
            searchQuery = immediateQuery,
            selectedCategory = selectedCat,
            isLoading = loading,
            featuredBuild = featured,
            showBuildDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun generateRandomBuild(allComponents: List<Component>) {
        val cpus = allComponents.filterIsInstance<Component.CPU>()
        val gpus = allComponents.filterIsInstance<Component.GPU>()
        val mobos = allComponents.filterIsInstance<Component.Motherboard>()
        val rams = allComponents.filterIsInstance<Component.RAM>()
        val psus = allComponents.filterIsInstance<Component.PSU>()

        if (cpus.isEmpty() || gpus.isEmpty() || mobos.isEmpty() || rams.isEmpty() || psus.isEmpty()) return

        val builds = listOfNotNull(
            createBuild("Master Race Ultra", "Lo mejor de lo mejor para 4K", cpus, gpus, mobos, rams, psus, budget = 4000.0),
            createBuild("Gaming Pro Balanced", "Equilibrio perfecto precio/rendimiento", cpus, gpus, mobos, rams, psus, budget = 2000.0),
            createBuild("Budget Warrior", "Excelente para 1080p competitivo", cpus, gpus, mobos, rams, psus, budget = 1000.0),
            createBuild("Streamer Entry", "Multitarea fluida para creadores", cpus, gpus, mobos, rams, psus, budget = 1500.0),
            createBuild("Compact Beast", "Potencia masiva en formato pequeño", cpus, gpus, mobos, rams, psus, budget = 2500.0),
            createBuild("Workstation Ready", "Ideal para renderizado y diseño", cpus, gpus, mobos, rams, psus, budget = 3500.0)
        )

        if (builds.isNotEmpty()) {
            _featuredBuild.value = builds.random()
        }
    }

    private fun createBuild(
        name: String, 
        desc: String,
        cpus: List<Component.CPU>,
        gpus: List<Component.GPU>,
        mobos: List<Component.Motherboard>,
        rams: List<Component.RAM>,
        psus: List<Component.PSU>,
        budget: Double
    ): PredefinedBuild? {
        val cpu = cpus.filter { it.price <= budget * 0.3 }.maxByOrNull { it.price } ?: cpus.firstOrNull() ?: return null
        val gpu = gpus.filter { it.price <= budget * 0.4 }.maxByOrNull { it.price } ?: gpus.firstOrNull() ?: return null
        
        // Limpiamos el socket para una comparación más robusta (quitamos espacios y pasamos a minúsculas)
        val cleanCpuSocket = cpu.socket.replace(" ", "").lowercase()
        
        val mobo = mobos.filter { 
            it.socket.replace(" ", "").lowercase() == cleanCpuSocket 
        }.minByOrNull { Math.abs(it.price - (budget * 0.15)) } 
        
        // Si no hay motherboard compatible con el socket, cancelamos esta build para no mostrar datos erróneos
        if (mobo == null) return null
            
        val ram = rams.filter { it.price <= budget * 0.1 }.maxByOrNull { it.price } ?: rams.firstOrNull() ?: return null

        val gpuRecWatts = (gpu.recommendedPSU ?: gpu.consumptionWatts).filter { it.isDigit() }.toIntOrNull() ?: 600
        // Añadimos un pequeño buffer de seguridad a la fuente
        val psu = psus.filter { it.wattage >= gpuRecWatts }
            .minByOrNull { it.wattage } 
            
        if (psu == null) return null

        val buildList = listOf(cpu, gpu, mobo, ram, psu)
        return PredefinedBuild(
            name = name,
            description = desc,
            components = buildList,
            totalPrice = buildList.sumOf { it.price }
        )
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeEvent.OnSearchQueryChange -> _searchQuery.value = event.query
                is HomeEvent.OnCategoryChange -> _selectedCategory.value = event.category
                HomeEvent.OnToggleBuildDialog -> _showBuildDialog.value = !_showBuildDialog.value
                HomeEvent.OnAddFeaturedToCart -> {
                    _featuredBuild.value?.components?.forEach { component ->
                        cartRepository.addComponent(component)
                    }
                    _showBuildDialog.value = false
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToCart)
                }
                HomeEvent.ResetNavigation -> { /* Managed by SharedFlow */ }
                HomeEvent.LoadComponents -> { }
            }
        }
    }
}
