package edu.ucne.corebuild.domain.buildscore

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import kotlin.math.abs

data class BuildScore(
    val score: Int,
    val label: String,
    val recommendations: List<String>
)

class BuildScoreCalculator @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    fun calculateScore(items: List<CartItem>): BuildScore {
        if (items.isEmpty()) return BuildScore(0, "Vaciado", listOf("Agrega componentes para evaluar tu build."))

        var baseScore = 100
        val recs = mutableListOf<String>()

        // 1. Penalización por Incompatibilidad (Grave)
        val warnings = compatibilityEngine.checkCompatibility(items)
        if (warnings.isNotEmpty()) {
            baseScore -= 40
            recs.add("Corrige las incompatibilidades críticas primero.")
        }

        // 2. Penalización por Faltantes (Esenciales)
        val components = items.map { it.component }
        val hasCpu = components.any { it is Component.CPU }
        val hasGpu = components.any { it is Component.GPU }
        val hasMobo = components.any { it is Component.Motherboard }
        val hasRam = components.any { it is Component.RAM }
        val hasPsu = components.any { it is Component.PSU }

        if (!hasCpu) { baseScore -= 10; recs.add("Falta un procesador.") }
        if (!hasGpu) { baseScore -= 5; recs.add("Considera una GPU para mejor rendimiento.") }
        if (!hasMobo) { baseScore -= 10; recs.add("Falta una placa base.") }
        if (!hasRam) { baseScore -= 10; recs.add("Falta memoria RAM.") }
        if (!hasPsu) { baseScore -= 10; recs.add("Falta una fuente de poder.") }

        // 3. Balance CPU vs GPU (Proxy por precio para rendimiento)
        if (hasCpu && hasGpu) {
            val cpu = components.filterIsInstance<Component.CPU>().first()
            val gpu = components.filterIsInstance<Component.GPU>().first()
            
            val cpuPrice = cpu.price
            val gpuPrice = gpu.price
            
            // Ratio ideal: GPU cuesta entre 1.5x y 2.5x el precio del CPU
            val ratio = gpuPrice / cpuPrice
            if (ratio < 1.2) {
                baseScore -= 15
                recs.add("Tu GPU es débil para ese CPU. Podrías tener cuello de botella.")
            } else if (ratio > 3.5) {
                baseScore -= 15
                recs.add("Tu CPU es débil para esa GPU. No aprovecharás todo su potencial.")
            }
        }

        val finalScore = baseScore.coerceIn(0, 100)
        val label = when {
            finalScore >= 85 -> "Excelente"
            finalScore >= 60 -> "Balanceado"
            else -> "Mejorable"
        }

        return BuildScore(finalScore, label, recs)
    }
}
