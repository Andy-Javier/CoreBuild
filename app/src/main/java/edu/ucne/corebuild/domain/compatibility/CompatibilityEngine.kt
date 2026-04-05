package edu.ucne.corebuild.domain.compatibility

import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject

class CompatibilityEngine @Inject constructor() {

    fun checkCompatibility(items: List<CartItem>): List<String> {
        val warnings = mutableListOf<String>()
        
        val cpus = items.map { it.component }.filterIsInstance<Component.CPU>()
        val motherboards = items.map { it.component }.filterIsInstance<Component.Motherboard>()
        val gpus = items.map { it.component }.filterIsInstance<Component.GPU>()
        val psus = items.map { it.component }.filterIsInstance<Component.PSU>()

        if (cpus.isNotEmpty() && motherboards.isNotEmpty()) {
            cpus.forEach { cpu ->
                motherboards.forEach { mobo ->
                    if (cpu.socket != mobo.socket) {
                        warnings.add("⚠️ Incompatibilidad de Socket: El procesador ${cpu.name} (${cpu.socket}) no es compatible con la placa base ${mobo.name} (${mobo.socket}).")
                    }
                }
            }
        }

        if (gpus.isNotEmpty() && psus.isNotEmpty()) {
            val totalConsumptionWatts = gpus.sumOf { gpu ->
                gpu.consumptionWatts.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            val maxPsuWattage = psus.maxOf { it.wattage }
            
            if (maxPsuWattage < totalConsumptionWatts) {
                warnings.add("⚠️ Potencia Insuficiente: La fuente de poder ($maxPsuWattage W) podría no ser suficiente para la tarjeta gráfica (Consumo: $totalConsumptionWatts W).")
            }
        }

        items.forEach { item ->
            val limit = getLimitForCategory(item.component)
            if (item.quantity > limit) {
                warnings.add("⚠️ Límite excedido: Solo se permiten $limit unidad(es) de ${item.component.name} por equipo.")
            }
        }

        return warnings
    }

    fun getLimitForCategory(component: Component): Int {
        return when (component) {
            is Component.CPU -> 3
            is Component.Motherboard -> 1
            is Component.GPU -> 2
            is Component.RAM -> 4
            is Component.PSU -> 1
        }
    }
}
