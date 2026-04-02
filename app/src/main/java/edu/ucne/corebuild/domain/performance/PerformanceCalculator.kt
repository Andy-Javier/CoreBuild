package edu.ucne.corebuild.domain.performance

import edu.ucne.corebuild.domain.model.Component

enum class GamePreset(
    val displayName: String,
    val fpsAt100pct: Int,   // FPS máximo con el mejor hardware @ 1080p
    val fpsFloor: Int       // FPS mínimo absoluto con hardware muy débil
) {
    GTA_V(            "GTA V",                  fpsAt100pct = 165, fpsFloor = 18),
    CYBERPUNK_2077(   "Cyberpunk 2077",          fpsAt100pct = 120, fpsFloor = 8),
    VALORANT(         "Valorant",                fpsAt100pct = 300, fpsFloor = 40),
    MINECRAFT(        "Minecraft",               fpsAt100pct = 240, fpsFloor = 30),
    ELDEN_RING(       "Elden Ring",              fpsAt100pct = 60,  fpsFloor = 10),
    CS2(              "CS2",                     fpsAt100pct = 300, fpsFloor = 45),
    FORTNITE(         "Fortnite",                fpsAt100pct = 144, fpsFloor = 20),
    RED_DEAD_2(       "Red Dead 2",              fpsAt100pct = 90,  fpsFloor = 10),
    HOGWARTS_LEGACY(  "Hogwarts Legacy",         fpsAt100pct = 100, fpsFloor = 8),
    STARFIELD(        "Starfield",               fpsAt100pct = 90,  fpsFloor = 10)
}

data class FpsResult(
    val fps: Int,
    val label: String,      // "Muy fluido" | "Fluido" | "Jugable" | "Limitado" | "No recomendado"
    val fraction: Float,    // 0f..1f normalizado a 120fps
    val limitedBy: String,  // "CPU" | "GPU" | "Equilibrado"
    val cpuContribution: Int,   // porcentaje 0..100 para debug/UI opcional
    val gpuContribution: Int    // porcentaje 0..100 para debug/UI opcional
)

object PerformanceCalculator {

    private const val CPU_MAX = 65.0
    private const val GPU_MAX = 575.0

    fun estimateFps(
        cpu: Component.CPU,
        gpu: Component.GPU,
        game: GamePreset,
        resolution: String
    ): FpsResult {
        val enrichedCpu = enrichedCpuScore(cpu)
        val normCpu = (enrichedCpu / CPU_MAX).coerceIn(0.05, 1.0)
        
        val gpuWatts = gpu.consumptionWatts.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 200.0
        val normGpu = (gpuWatts / GPU_MAX).coerceIn(0.05, 1.0)

        val gpuWeight = when (resolution) {
            "1080p" -> 0.55
            "1440p" -> 0.65
            "4K"    -> 0.80
            else    -> 0.55
        }
        val cpuWeight = 1.0 - gpuWeight

        val systemScore = (normCpu * cpuWeight + normGpu * gpuWeight).coerceIn(0.05, 1.0)

        val resFactor = when (resolution) {
            "1080p" -> 1.00
            "1440p" -> 0.65
            "4K"    -> 0.35
            else    -> 1.00
        }

        val adjustedMax = (game.fpsAt100pct * resFactor).toInt().coerceAtLeast(game.fpsFloor + 1)
        val fps = (game.fpsFloor + (adjustedMax - game.fpsFloor) * systemScore).toInt()
            .coerceIn(game.fpsFloor, adjustedMax)

        val limitedBy = when {
            normCpu < normGpu * 0.70 -> "CPU"
            normGpu < normCpu * 0.70 -> "GPU"
            else                     -> "Equilibrado"
        }

        val label = when {
            fps >= 90 -> "Muy fluido"
            fps >= 60 -> "Fluido"
            fps >= 40 -> "Jugable"
            fps >= 24 -> "Limitado"
            else      -> "No recomendado"
        }

        val fraction = (fps / 120f).coerceIn(0f, 1f)
        
        val cpuCont = (normCpu * cpuWeight / systemScore * 100).toInt()
        val gpuCont = 100 - cpuCont

        return FpsResult(
            fps = fps,
            label = label,
            fraction = fraction,
            limitedBy = limitedBy,
            cpuContribution = cpuCont,
            gpuContribution = gpuCont
        )
    }

    private fun enrichedCpuScore(cpu: Component.CPU): Double {
        val boostGhz = cpu.boostClock.replace("GHz", "").trim().toDoubleOrNull() ?: 3.0
        val baseScore = cpu.cores * boostGhz

        val cacheMb = parseCacheMb(cpu.cache ?: "")
        val cacheMultiplier = when {
            cacheMb >= 128 -> 1.38
            cacheMb >= 96  -> 1.30
            cacheMb >= 64  -> 1.08
            cacheMb >= 32  -> 1.00
            cacheMb >= 20  -> 0.96
            cacheMb >= 16  -> 0.95
            cacheMb >= 12  -> 0.93
            else           -> 0.90
        }

        val name = cpu.name
        val ipcMultiplier = when {
            "9600" in name || "9700" in name || "9800" in name || "9900" in name || "9950" in name -> 1.16
            "7600" in name || "7700" in name || "7800" in name || "7900" in name || "7950" in name || "7500" in name -> 1.13
            "5600" in name || "5500" in name || "5700" in name || "5800" in name || "5900" in name || "5950" in name -> 1.10
            "3600" in name || "3700" in name || "3800" in name || "3900" in name || "4100" in name || "4500" in name -> 1.00
            "245K" in name || "265K" in name || "285K" in name -> 1.12
            "14900" in name || "14700" in name || "14600" in name || "14400" in name || "14100" in name -> 1.09
            "13900" in name || "13700" in name || "13600" in name || "13400" in name || "13100" in name -> 1.08
            "12900" in name || "12700" in name || "12600" in name || "12400" in name || "12100" in name -> 1.06
            "11700" in name || "11400" in name -> 1.04
            "10900" in name || "10700" in name || "10400" in name || "10100" in name -> 1.00
            else -> 1.00
        }

        val eCoresPenalty = when {
            ("12900" in name || "13900" in name || "14900" in name) && cpu.cores >= 24 -> 0.72
            ("12700" in name || "13700" in name || "14700" in name) && cpu.cores >= 16 -> 0.78
            ("12600" in name || "13600" in name || "14600" in name) && cpu.cores >= 14 -> 0.82
            ("12400" in name || "13400" in name || "14400" in name) && cpu.cores >= 10 -> 0.88
            ("265K" in name || "285K" in name) -> 0.85
            else -> 1.00
        }

        return baseScore * cacheMultiplier * ipcMultiplier * eCoresPenalty
    }

    private fun parseCacheMb(cache: String): Int {
        return cache.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 8
    }
}
