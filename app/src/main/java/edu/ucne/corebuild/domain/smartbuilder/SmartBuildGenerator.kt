package edu.ucne.corebuild.domain.smartbuilder

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import kotlin.math.abs

class SmartBuildGenerator @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    suspend fun generateBuild(
        anchorCpu: Component.CPU?,
        anchorGpu: Component.GPU?,
        allComponents: List<Component>
    ): SmartBuild {
        if (anchorCpu == null && anchorGpu == null) {
            throw IllegalArgumentException("At least one anchor component must be selected")
        }

        val cpus = allComponents.filterIsInstance<Component.CPU>()
        val gpus = allComponents.filterIsInstance<Component.GPU>()
        val motherboards = allComponents.filterIsInstance<Component.Motherboard>()
        val rams = allComponents.filterIsInstance<Component.RAM>()
        val psus = allComponents.filterIsInstance<Component.PSU>()

        val suggested = mutableListOf<Component>()
        val warnings = mutableListOf<String>()

        // 1. Select CPU if not provided
        val selectedCpu = anchorCpu ?: selectBalancedCpu(anchorGpu!!, cpus)
        if (anchorCpu == null) suggested.add(selectedCpu)

        // 2. Select GPU if not provided
        val selectedGpu = anchorGpu ?: selectBalancedGpu(selectedCpu, gpus)
        if (anchorGpu == null) suggested.add(selectedGpu)

        // 3. Select Motherboard compatible with CPU
        val selectedMobo = selectCompatibleMotherboard(selectedCpu, motherboards)
        suggested.add(selectedMobo)

        // 4. Select RAM compatible with Motherboard
        val selectedRam = selectCompatibleRam(selectedMobo, rams)
        suggested.add(selectedRam)

        // 5. Select PSU sufficient for CPU + GPU
        val selectedPsu = selectSufficientPsu(selectedCpu, selectedGpu, psus)
        suggested.add(selectedPsu)

        // Bottleneck check
        checkBottleneck(selectedCpu, selectedGpu, warnings)

        return SmartBuild(
            anchorCpu = anchorCpu,
            anchorGpu = anchorGpu,
            suggested = suggested,
            warnings = warnings
        )
    }

    private fun selectBalancedCpu(gpu: Component.GPU, cpus: List<Component.CPU>): Component.CPU {
        // GPU price is roughly 40-50% of build, CPU 20-25%
        // Target CPU price ≈ GPU price * 0.5
        val targetPrice = gpu.price * 0.5
        return cpus.minByOrNull { abs(it.price - targetPrice) } ?: cpus.first()
    }

    private fun selectBalancedGpu(cpu: Component.CPU, gpus: List<Component.GPU>): Component.GPU {
        // Target GPU price ≈ CPU price * 2.0
        val targetPrice = cpu.price * 2.0
        return gpus.minByOrNull { abs(it.price - targetPrice) } ?: gpus.first()
    }

    private fun selectCompatibleMotherboard(cpu: Component.CPU, mobos: List<Component.Motherboard>): Component.Motherboard {
        val cleanSocket = cpu.socket.replace(" ", "").lowercase()
        return mobos.filter { it.socket.replace(" ", "").lowercase() == cleanSocket }
            .minByOrNull { abs(it.price - (cpu.price * 0.8)) } ?: mobos.first()
    }

    private fun selectCompatibleRam(mobo: Component.Motherboard, rams: List<Component.RAM>): Component.RAM {
        return rams.filter { it.type.contains(mobo.ramType, ignoreCase = true) || mobo.ramType.contains(it.type, ignoreCase = true) }
            .maxByOrNull { it.price } ?: rams.first()
    }

    private fun selectSufficientPsu(cpu: Component.CPU, gpu: Component.GPU, psus: List<Component.PSU>): Component.PSU {
        val gpuWatts = (gpu.recommendedPSU ?: gpu.consumptionWatts).filter { it.isDigit() }.toIntOrNull() ?: 600
        return psus.filter { it.wattage >= gpuWatts }
            .minByOrNull { it.wattage } ?: psus.maxByOrNull { it.wattage } ?: psus.first()
    }

    private fun checkBottleneck(cpu: Component.CPU, gpu: Component.GPU, warnings: MutableList<String>) {
        val ratio = gpu.price / cpu.price
        if (ratio > 4.0) {
            warnings.add("⚠️ El procesador puede limitar el rendimiento de esta tarjeta gráfica (Cuello de botella).")
        } else if (ratio < 1.0) {
            warnings.add("⚠️ La tarjeta gráfica es poco potente para el nivel de este procesador.")
        }
    }
}
