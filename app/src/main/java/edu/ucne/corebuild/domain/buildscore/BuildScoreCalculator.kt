package edu.ucne.corebuild.domain.buildscore

import edu.ucne.corebuild.domain.compatibility.CompatibilityEngine
import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject

data class BuildScore(
    val score: Int,
    val label: String,
    val recommendations: List<String>
)

class BuildScoreCalculator @Inject constructor(
    private val compatibilityEngine: CompatibilityEngine
) {
    fun calculateScore(items: List<CartItem>): BuildScore {
        if (items.isEmpty()) return BuildScore(0, "❌ Build Incompleto", listOf("Agrega componentes para evaluar tu build."))

        val components = items.map { it.component }
        val cpu = components.filterIsInstance<Component.CPU>().firstOrNull()
        val gpu = components.filterIsInstance<Component.GPU>().firstOrNull()
        val ram = components.filterIsInstance<Component.RAM>().firstOrNull()
        val psu = components.filterIsInstance<Component.PSU>().firstOrNull()
        val mobo = components.filterIsInstance<Component.Motherboard>().firstOrNull()

        var baseScore = 0.0
        val recs = mutableListOf<String>()

        cpu?.let {
            baseScore += when {
                it.price > 350 -> 25
                it.price >= 200 -> 18
                it.price >= 100 -> 12
                else -> 5
            }
        }

        gpu?.let {
            baseScore += when {
                it.price > 700 -> 25
                it.price >= 400 -> 18
                it.price >= 200 -> 12
                else -> 5
            }
        }

        ram?.let {
            val name = it.name.uppercase()
            var ramPts = when {
                name.contains("64GB") || name.contains("128GB") -> 15
                name.contains("32GB") -> 12
                name.contains("16GB") -> 8
                else -> 3
            }

            val speedRegex = Regex("""(\d{4,5})\s*MHZ|DDR[45]-(\d{4,5})""")
            val speedMatch = speedRegex.find(name)
            val speed = speedMatch?.groupValues
                ?.drop(1)
                ?.firstOrNull { group -> group.isNotEmpty() }
                ?.toIntOrNull() ?: 0

            if ((name.contains("DDR4") && speed > 3600) || (name.contains("DDR5") && speed > 5600)) {
                ramPts += 3
            }
            baseScore += ramPts.coerceAtMost(15)
        }

        psu?.let {
            val name = it.name.uppercase()
            baseScore += when {
                name.contains("PLATINUM") || name.contains("TITANIUM") -> 10
                name.contains("GOLD") -> 7
                else -> 4
            }
        }

        mobo?.let {
            val name = it.name.uppercase()
            baseScore += when {
                listOf("X570", "Z690", "Z790", "X670E", "Z890", "X870E", "X870").any { chipset -> name.contains(chipset) } -> 5
                listOf("B450", "B550", "B650", "B760").any { chipset -> name.contains(chipset) } -> 3
                else -> 2
            }
        }

        var synergyBonus = 0.0

        if (cpu != null && gpu != null && mobo != null && ram != null && psu != null) {
            synergyBonus += 10
        }

        if (ram?.name?.contains("DDR5", true) == true) {
            synergyBonus += 5
        }

        val warnings = compatibilityEngine.checkCompatibility(items)
        if (cpu != null && mobo != null && warnings.none { it.contains("Socket", true) }) {
            synergyBonus += 5
        }

        if (cpu != null && gpu != null && psu != null) {
            val cpuWatts = Regex("""\d+""").find(cpu.tdp)?.value?.toIntOrNull() ?: 65
            val gpuWatts = Regex("""\d+""").find(gpu.consumptionWatts)?.value?.toIntOrNull() ?: 200
            if (psu.wattage >= (cpuWatts + gpuWatts) * 1.2) {
                synergyBonus += 5
            }
        }

        var totalScore = ((baseScore + synergyBonus) / 105.0) * 100.0

        if (warnings.isNotEmpty()) {
            totalScore -= 30
            recs.add("⚠️ Errores de compatibilidad detectados.")
        }

        if (cpu == null) { totalScore -= 10; recs.add("Falta CPU (-10)") }
        if (mobo == null) { totalScore -= 10; recs.add("Falta Placa Base (-10)") }
        if (ram == null) { totalScore -= 10; recs.add("Falta RAM (-10)") }
        if (psu == null) { totalScore -= 10; recs.add("Falta Fuente de Poder (-10)") }
        if (gpu == null) { totalScore -= 5; recs.add("Falta GPU (-5)") }

        if (gpu != null && psu != null) {
            val recPSU = Regex("""\d+""").find(gpu.recommendedPSU ?: gpu.consumptionWatts)?.value?.toIntOrNull() ?: 600
            if (psu.wattage < recPSU) {
                totalScore -= 15
                recs.add("⚠️ PSU insuficiente para la GPU: se sugieren ${recPSU}W.")
            }
        }

        if (cpu != null && gpu != null) {
            val ratio = gpu.price / cpu.price
            when {
                ratio > 4.0 -> { totalScore -= 10; recs.add("Cuello de botella severo: CPU muy débil para esta GPU.") }
                ratio < 1.0 -> { totalScore -= 10; recs.add("Cuello de botella severo: GPU muy débil para este CPU.") }
                ratio >= 3.5 -> { totalScore -= 5; recs.add("Desbalance moderado: El CPU podría limitar la GPU.") }
                ratio <= 1.2 -> { totalScore -= 5; recs.add("Desbalance moderado: La GPU limita el potencial del CPU.") }
            }
        }

        val finalScore = totalScore.toInt().coerceIn(0, 100)
        val label = when {
            finalScore >= 90 -> "🏆 Build de Ensueño"
            finalScore >= 75 -> "✅ Excelente"
            finalScore >= 55 -> "⚖️ Balanceado"
            finalScore >= 35 -> "⚠️ Mejorable"
            else -> "❌ Build Incompleto"
        }

        if (recs.isEmpty()) recs.add("¡Excelente elección de componentes!")

        return BuildScore(finalScore, label, recs)
    }
}
