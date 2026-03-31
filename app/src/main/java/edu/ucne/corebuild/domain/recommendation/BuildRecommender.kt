package edu.ucne.corebuild.domain.recommendation

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import kotlin.math.ceil

class BuildRecommender @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    fun recommendBuild(
        budget: Double,
        priority: String?, // "CPU" or "GPU"
        allComponents: List<Component>
    ): List<Component> {
        if (allComponents.isEmpty()) return emptyList()

        val cpus = allComponents.filterIsInstance<Component.CPU>().sortedBy { it.price }
        val gpus = allComponents.filterIsInstance<Component.GPU>().sortedBy { it.price }
        val mobos = allComponents.filterIsInstance<Component.Motherboard>().sortedBy { it.price }
        val rams = allComponents.filterIsInstance<Component.RAM>().sortedBy { it.price }
        val psus = allComponents.filterIsInstance<Component.PSU>().sortedBy { it.price }

        if (cpus.isEmpty() || mobos.isEmpty() || rams.isEmpty() || psus.isEmpty()) return emptyList()

        // 1. Encontrar el build más barato posible (cheapestViableBuild)
        var cheapestViableBuild: List<Component>? = null
        var absoluteMinPrice = Double.MAX_VALUE

        for (cpu in cpus) {
            val mobo = mobos.find { isSocketCompatible(cpu.socket, it.socket) } ?: continue
            val ram = rams.find { isRamCompatible(mobo.ramType, it.type) } ?: continue
            val psu = psus.firstOrNull() ?: continue
            
            val total = cpu.price + mobo.price + ram.price + psu.price
            if (total < absoluteMinPrice) {
                absoluteMinPrice = total
                cheapestViableBuild = listOf(cpu, mobo, ram, psu)
            }
        }

        // Si ni el build más barato entra en el presupuesto, no podemos recomendar nada
        if (cheapestViableBuild == null || absoluteMinPrice > budget) return emptyList()

        var recommendedBuild: List<Component>? = null

        // 2. Lógica de recomendación según prioridad
        if (priority == "GPU" && gpus.isNotEmpty()) {
            for (gpu in gpus.reversed()) {
                val remaining = budget - gpu.price
                if (remaining < 0) continue
                
                // Iterar CPUs de mejor a peor para encontrar compatibilidad
                for (cpu in cpus.reversed()) {
                    val essentialsBudget = remaining - cpu.price
                    val essentials = findBestEssentialsForCpu(essentialsBudget, cpu, mobos, rams, psus, gpu)
                    if (essentials.isNotEmpty()) {
                        val build = listOf(gpu, cpu) + essentials
                        if (build.sumOf { it.price } <= budget) {
                            recommendedBuild = build
                            break
                        }
                    }
                }
                if (recommendedBuild != null) break
            }
        } else {
            val gpuRatio = if (priority == "CPU") 0.25 else 0.35
            val cpuLimit = budget * 0.5
            val cpusToTry = cpus.filter { it.price <= cpuLimit }.sortedByDescending { it.price }
            
            for (cpu in cpusToTry) {
                val remainingForOthers = budget - cpu.price
                val compatibleGpus = gpus.filter { it.price <= remainingForOthers * 0.8 }.sortedByDescending { it.price }
                
                if (compatibleGpus.isNotEmpty()) {
                    val targetGpuPrice = remainingForOthers * gpuRatio
                    val selectedGpu = compatibleGpus.find { it.price <= targetGpuPrice } ?: compatibleGpus.last()
                    
                    val finalBudget = remainingForOthers - selectedGpu.price
                    val essentials = findBestEssentialsForCpu(finalBudget, cpu, mobos, rams, psus, selectedGpu)
                    
                    if (essentials.isNotEmpty()) {
                        val build = listOf(cpu, selectedGpu) + essentials
                        if (build.sumOf { it.price } <= budget) {
                            recommendedBuild = build
                            break
                        }
                    }
                }
            }
        }

        // Fallback final: Si nada funcionó, devolver el build más barato garantizado
        val finalResult = recommendedBuild ?: cheapestViableBuild
        return if (finalResult.sumOf { it.price } <= budget) finalResult else cheapestViableBuild
    }

    private fun findBestEssentialsForCpu(
        budget: Double,
        cpu: Component.CPU,
        mobos: List<Component.Motherboard>,
        rams: List<Component.RAM>,
        psus: List<Component.PSU>,
        gpu: Component.GPU?
    ): List<Component> {
        val minWatts = if (gpu != null) {
            val manufacturerRec = Regex("""\d+""").find(gpu.recommendedPSU ?: "")?.value?.toIntOrNull()
            if (manufacturerRec != null) {
                manufacturerRec
            } else {
                val cpuWatts = Regex("""\d+""").find(cpu.tdp ?: "")?.value?.toIntOrNull() ?: 65
                val gpuCons = Regex("""\d+""").find(gpu.consumptionWatts ?: "")?.value?.toIntOrNull() ?: 200
                (ceil((cpuWatts + gpuCons) * 1.2 / 50.0) * 50).toInt()
            }
        } else {
            450
        }

        val compatibleMobos = mobos.filter { 
            isSocketCompatible(cpu.socket, it.socket) && it.price <= budget * 0.5 
        }.sortedByDescending { it.price }
        
        for (mobo in compatibleMobos) {
            val remaining = budget - mobo.price
            if (remaining < 0) continue
            
            val compatibleRams = rams.filter { isRamCompatible(mobo.ramType, it.type) && it.price <= remaining * 0.7 }
            val selectedRam = compatibleRams.maxByOrNull { it.price } ?: rams.lastOrNull() ?: continue
            
            val psuBudget = remaining - selectedRam.price
            if (psuBudget < 0) continue
            
            // PSU Estricta: Solo si cumple Watts Y presupuesto
            val selectedPsu = psus.filter { it.wattage >= minWatts && it.price <= psuBudget }.maxByOrNull { it.price }
            
            if (selectedPsu != null) {
                return listOf(mobo, selectedRam, selectedPsu)
            }
        }
        return emptyList()
    }

    private fun isSocketCompatible(cpuSocket: String, moboSocket: String): Boolean {
        val s1 = cpuSocket.replace(" ", "").uppercase()
        val s2 = moboSocket.replace(" ", "").uppercase()
        return s1 == s2 || s1.contains(s2) || s2.contains(s1)
    }

    private fun isRamCompatible(moboRam: String, ramType: String): Boolean {
        val r1 = moboRam.uppercase()
        val r2 = ramType.uppercase()
        return r1.contains(r2) || r2.contains(r1)
    }
}
