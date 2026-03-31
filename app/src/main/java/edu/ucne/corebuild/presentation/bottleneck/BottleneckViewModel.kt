package edu.ucne.corebuild.presentation.bottleneck

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
import kotlin.math.abs

@HiltViewModel
class BottleneckViewModel @Inject constructor(
    private val getComponentsUseCase: GetComponentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BottleneckUiState())
    val uiState: StateFlow<BottleneckUiState> = _uiState.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getComponentsUseCase().collect { components ->
                _uiState.update {
                    it.copy(
                        cpus = components.filterIsInstance<Component.CPU>(),
                        gpus = components.filterIsInstance<Component.GPU>(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: BottleneckEvent) {
        when (event) {
            is BottleneckEvent.SelectCpu -> {
                _uiState.update { it.copy(selectedCpu = event.cpu) }
                calculateBottleneck(_uiState.value)
            }
            is BottleneckEvent.SelectGpu -> {
                _uiState.update { it.copy(selectedGpu = event.gpu) }
                calculateBottleneck(_uiState.value)
            }
            is BottleneckEvent.SelectResolution -> {
                _uiState.update { it.copy(selectedResolution = event.resolution) }
                calculateBottleneck(_uiState.value)
            }
        }
    }

    private fun calculateBottleneck(state: BottleneckUiState) {
        val cpu = state.selectedCpu ?: return
        val gpu = state.selectedGpu ?: return
        val res = state.selectedResolution

        // Paso 1: Detección de Arquitectura
        val isArrowLake = cpu.name.contains("Ultra", ignoreCase = true)
        val isHybridIntel = !isArrowLake && 
            (cpu.name.contains("-12") || cpu.name.contains("-13") || cpu.name.contains("-14"))
        
        // En arquitecturas híbridas Intel, solo los P-cores (estimados como hilos/2) dictan el rendimiento gaming
        val gamingCores = if (isHybridIntel) cpu.threads / 2 else cpu.cores

        // Paso 2: Multiplicador de Generación (IPC Real)
        val name = cpu.name.uppercase()
        val genMultiplier = when {
            // Zen 5 (9000 Series)
            name.contains("9800") || name.contains("9700") || 
            name.contains("9600") || name.contains("9950") -> 1.25
            // Zen 4 X3D
            name.contains("7950X3D") || name.contains("7800X3D") -> 1.20
            // Zen 4 Estándar
            name.contains("7900") || name.contains("7700") || 
            name.contains("7600") || name.contains("7500") -> 1.15
            // Zen 3 X3D
            name.contains("5800X3D") || name.contains("5600X3D") -> 1.10
            // Zen 3 Estándar
            name.contains("5900") || name.contains("5950") || 
            name.contains("5700") || name.contains("5600") || 
            name.contains("5500") -> 1.10 // Ajustado de 1.05 a 1.10 por benchmarks reales
            // Zen 2
            name.contains("3600") || name.contains("3700") || 
            name.contains("3900") || name.contains("4100") || 
            name.contains("4500") -> 1.0
            // Intel Arrow Lake
            name.contains("ULTRA 9") -> 1.18
            name.contains("ULTRA 7") -> 1.14
            name.contains("ULTRA 5") -> 1.10
            // Intel Híbrido por Generación
            name.contains("-14") -> 1.08
            name.contains("-13") -> 1.06
            name.contains("-12") -> 1.03
            // Intel Antiguo
            name.contains("-11") -> 1.02
            name.contains("-10") -> 1.0
            else -> 1.0
        }

        // Paso 3: Score Base CPU (Gaming Cores * Boost Ghz)
        val boostGhz = Regex("""[\d.]+""").find(cpu.boostClock)
            ?.value?.toDoubleOrNull() ?: 4.0
        var cpuScore = gamingCores.toDouble() * boostGhz * genMultiplier

        // Paso 4: Multiplicador X3D (Ventaja masiva en Gaming)
        val x3dMultiplier = when {
            name.contains("9800X3D") -> 1.70
            name.contains("7800X3D") -> 1.58
            name.contains("7950X3D") -> 1.50
            name.contains("5800X3D") -> 1.38
            name.contains("5600X3D") -> 1.32
            else -> 1.0
        }
        cpuScore *= x3dMultiplier

        // Paso 5: Bonus Caché L3 (Cada 10MB suma 1 punto)
        val cacheMb = Regex("""\d+""").find(cpu.cache ?: "16")
            ?.value?.toDoubleOrNull() ?: 16.0
        cpuScore += cacheMb / 10.0

        // Paso 6: Score GPU (watts / 4.0 para balancear con CPU)
        val watts = Regex("""\d+""").find(gpu.consumptionWatts)
            ?.value?.toDoubleOrNull() ?: 200.0
        val gpuScore = watts / 4.0

        // Paso 7: Normalización (Basada en topes del sistema para comparación justa)
        val CPU_MAX = 120.0
        val GPU_MAX = 200.0
        val normalizedCpu = (cpuScore / CPU_MAX).coerceIn(0.0, 1.0)
        val normalizedGpu = (gpuScore / GPU_MAX).coerceIn(0.0, 1.0)

        // Paso 8: Ratio Ideal por Resolución (Elimina el bug de dirección inversa)
        val idealRatio = when (res) {
            "1080p" -> 1.10 // CPU-bound: CPU debe ser un 10% mayor que GPU
            "1440p" -> 1.00 // Equilibrio total
            "4K"    -> 0.85 // GPU-bound: La GPU asume la carga dominante
            else    -> 1.00
        }

        // Paso 9: Cálculo Final del Ratio y Diferencia
        // Comparamos el ratio real del par contra el ratio ideal de la resolución
        val actualRatio = if (normalizedGpu > 0) normalizedCpu / normalizedGpu else 1.0
        val ratioDiff = abs(actualRatio - idealRatio)
        val percentage = (ratioDiff / idealRatio * 100).coerceIn(0.0, 100.0)

        // Dirección: Si actualRatio > idealRatio -> CPU sobra -> GPU es el cuello
        // Si actualRatio < idealRatio -> GPU sobra -> CPU es el cuello
        val limitingComponent = if (actualRatio > idealRatio) "GPU" else "CPU"

        // Paso 10: Status Labels con umbrales profesionales actualizados
        val status = when {
            percentage < 10 -> "Equilibrado ✅"
            percentage < 35 -> "Desbalance leve: $limitingComponent podría ser mejor ⚠️"
            else -> "Cuello de botella severo: $limitingComponent limita el sistema 🔴"
        }

        _uiState.update { 
            it.copy(
                bottleneckPercentage = percentage,
                status = status
            )
        }
    }
}
