package edu.ucne.corebuild.domain.recommendation

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject

class BuildRecommender @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    fun recommendBuild(
        budget: Double,
        baseComponent: Component?,
        allComponents: List<Component>
    ): List<Component> {
        val result = mutableListOf<Component>()
        var currentBudget = budget
        
        // Si hay un componente base, añadirlo primero
        baseComponent?.let { 
            result.add(it)
            currentBudget -= it.price
        }

        val cpus = allComponents.filterIsInstance<Component.CPU>().sortedByDescending { it.price }
        val gpus = allComponents.filterIsInstance<Component.GPU>().sortedByDescending { it.price }
        val mobos = allComponents.filterIsInstance<Component.Motherboard>().sortedByDescending { it.price }
        val rams = allComponents.filterIsInstance<Component.RAM>().sortedByDescending { it.price }
        val psus = allComponents.filterIsInstance<Component.PSU>().sortedByDescending { it.price }

        // Paso 1: Asegurar componentes esenciales
        
        // 1. CPU (si no es base)
        if (result.none { it is Component.CPU }) {
            cpus.find { it.price <= currentBudget * 0.3 }?.let { 
                result.add(it)
                currentBudget -= it.price
            }
        }

        // 2. Motherboard compatible con el CPU
        val cpu = result.filterIsInstance<Component.CPU>().firstOrNull()
        if (cpu != null && result.none { it is Component.Motherboard }) {
            mobos.find { it.socket == cpu.socket && it.price <= currentBudget * 0.2 }?.let {
                result.add(it)
                currentBudget -= it.price
            }
        }

        // 3. GPU (si no es base)
        if (result.none { it is Component.GPU }) {
            gpus.find { it.price <= currentBudget * 0.4 }?.let {
                result.add(it)
                currentBudget -= it.price
            }
        }

        // 4. RAM compatible con Motherboard
        val mobo = result.filterIsInstance<Component.Motherboard>().firstOrNull()
        if (mobo != null && result.none { it is Component.RAM }) {
            rams.find { it.type == mobo.ramType && it.price <= currentBudget * 0.15 }?.let {
                result.add(it)
                currentBudget -= it.price
            }
        }

        // 5. PSU potente para la GPU
        val gpu = result.filterIsInstance<Component.GPU>().firstOrNull()
        if (gpu != null && result.none { it is Component.PSU }) {
            val recWattage = gpu.recommendedWattage.filter { it.isDigit() }.toIntOrNull() ?: 500
            psus.find { it.wattage >= recWattage && it.price <= currentBudget }?.let {
                result.add(it)
                currentBudget -= it.price
            }
        }

        return result
    }
}
