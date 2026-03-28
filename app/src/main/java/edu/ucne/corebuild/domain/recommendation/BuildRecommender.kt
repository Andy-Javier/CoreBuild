package edu.ucne.corebuild.domain.recommendation

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject

class BuildRecommender @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    fun recommendBuild(
        budget: Double,
        priority: String?, // "CPU" or "GPU"
        allComponents: List<Component>
    ): List<Component> {
        val result = mutableListOf<Component>()
        var currentBudget = budget

        val cpus = allComponents.filterIsInstance<Component.CPU>().sortedByDescending { it.price }
        val gpus = allComponents.filterIsInstance<Component.GPU>().sortedByDescending { it.price }
        val mobos = allComponents.filterIsInstance<Component.Motherboard>().sortedBy { it.price } // Cheapest first for stability
        val rams = allComponents.filterIsInstance<Component.RAM>().sortedBy { it.price }
        val psus = allComponents.filterIsInstance<Component.PSU>().sortedBy { it.price } // Selection: cheapest that fits requirements

        // 1. Allocate Budget based on Priority
        val cpuBudget: Double
        val gpuBudget: Double
        
        when (priority) {
            "CPU" -> {
                cpuBudget = budget * 0.45
                gpuBudget = budget * 0.25
            }
            "GPU" -> {
                cpuBudget = budget * 0.25
                gpuBudget = budget * 0.45
            }
            else -> {
                cpuBudget = budget * 0.35
                gpuBudget = budget * 0.35
            }
        }

        // 2. Select CPU
        val selectedCpu = cpus.find { it.price <= cpuBudget }
        if (selectedCpu != null) {
            result.add(selectedCpu)
            currentBudget -= selectedCpu.price
        }

        // 3. Select GPU (Optional if budget too low, but recommended)
        val selectedGpu = gpus.find { it.price <= gpuBudget }
        if (selectedGpu != null) {
            result.add(selectedGpu)
            currentBudget -= selectedGpu.price
        }

        // 4. Select compatible Motherboard (Cheapest compatible)
        if (selectedCpu != null) {
            val compatibleMobo = allComponents.filterIsInstance<Component.Motherboard>()
                .filter { it.socket == selectedCpu.socket }
                .minByOrNull { it.price }
            
            if (compatibleMobo != null && currentBudget >= compatibleMobo.price) {
                result.add(compatibleMobo)
                currentBudget -= compatibleMobo.price
            }
        }

        // 5. Select compatible RAM (Cheapest compatible)
        val mobo = result.filterIsInstance<Component.Motherboard>().firstOrNull()
        if (mobo != null) {
            val compatibleRam = allComponents.filterIsInstance<Component.RAM>()
                .filter { it.type == mobo.ramType }
                .minByOrNull { it.price }
            
            if (compatibleRam != null && currentBudget >= compatibleRam.price) {
                result.add(compatibleRam)
                currentBudget -= compatibleRam.price
            }
        }

        // 6. Select PSU (Cheapest that meets wattage)
        val gpu = result.filterIsInstance<Component.GPU>().firstOrNull()
        val requiredWattage = if (gpu != null) {
            gpu.recommendedWattage.filter { it.isDigit() }.toIntOrNull() ?: 500
        } else 400

        val selectedPsu = psus.find { it.wattage >= requiredWattage && it.price <= currentBudget }
        if (selectedPsu != null) {
            result.add(selectedPsu)
            currentBudget -= selectedPsu.price
        }

        // Validation: Must have at least CPU, Mobo, RAM, and PSU to be a "PC"
        val hasEssentials = result.any { it is Component.CPU } && 
                           result.any { it is Component.Motherboard } && 
                           result.any { it is Component.RAM } && 
                           result.any { it is Component.PSU }

        return if (hasEssentials) result else emptyList()
    }
}
