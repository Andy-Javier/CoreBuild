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
import kotlin.random.Random

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
    private val _navigateToCart = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val _debouncedQuery = _searchQuery.debounce(300)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<HomeUiState> = combine(
        getComponentsUseCase(),
        statsRepository.getRecentlyViewed(),
        statsRepository.getTopRated(),
        _searchQuery,
        _debouncedQuery,
        _selectedCategory,
        _isLoading,
        _featuredBuild,
        _showBuildDialog,
        _navigateToCart
    ) { flows ->
        val components = flows[0] as List<Component>
        val recentlyViewed = flows[1] as List<Component>
        val topRated = flows[2] as List<Component>
        val immediateQuery = flows[3] as String
        val debouncedQuery = flows[4] as String
        val selectedCat = flows[5] as String?
        val loading = flows[6] as Boolean
        val featured = flows[7] as PredefinedBuild?
        val showDialog = flows[8] as Boolean
        val navCart = flows[9] as Boolean

        if (featured == null && components.isNotEmpty()) {
            generateRandomBuild(components)
        }

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
            showBuildDialog = showDialog,
            navigateToCart = navCart
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

        val builds = listOf(
            createBuild("Master Race Ultra", "Lo mejor de lo mejor para 4K", cpus, gpus, mobos, rams, psus, budget = 4000.0),
            createBuild("Gaming Pro Balanced", "Equilibrio perfecto precio/rendimiento", cpus, gpus, mobos, rams, psus, budget = 2000.0),
            createBuild("Budget Warrior", "Excelente para 1080p competitivo", cpus, gpus, mobos, rams, psus, budget = 1000.0),
            createBuild("Streamer Entry", "Multitarea fluida para creadores", cpus, gpus, mobos, rams, psus, budget = 1500.0),
            createBuild("Compact Beast", "Potencia masiva en formato pequeño", cpus, gpus, mobos, rams, psus, budget = 2500.0),
            createBuild("Workstation Ready", "Ideal para renderizado y diseño", cpus, gpus, mobos, rams, psus, budget = 3500.0)
        )

        _featuredBuild.value = builds.random()
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
    ): PredefinedBuild {
        // Selection with compatibility logic
        val cpu = cpus.filter { it.price <= budget * 0.3 }.maxByOrNull { it.price } ?: cpus.first()
        val gpu = gpus.filter { it.price <= budget * 0.4 }.maxByOrNull { it.price } ?: gpus.first()
        
        // Exact socket match
        val mobo = mobos.filter { it.socket.trim().equals(cpu.socket.trim(), ignoreCase = true) }
            .minByOrNull { Math.abs(it.price - (budget * 0.15)) } ?: mobos.first()
            
        val ram = rams.filter { it.price <= budget * 0.1 }.maxByOrNull { it.price } ?: rams.first()
        
        // PSU Wattage logic: GPU rec + buffer
        val gpuRecWatts = gpu.recommendedWattage.filter { it.isDigit() }.toIntOrNull() ?: 600
        val psu = psus.filter { it.wattage >= gpuRecWatts }
            .minByOrNull { it.wattage } ?: psus.maxByOrNull { it.wattage } ?: psus.first()

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
                    _navigateToCart.value = true
                }
                HomeEvent.ResetNavigation -> _navigateToCart.value = false
                HomeEvent.LoadComponents -> { }
            }
        }
    }
}
