package edu.ucne.corebuild.domain.model

sealed class Component {
    abstract val id: Int
    abstract val name: String
    abstract val description: String
    abstract val price: Double
    abstract val category: String
    abstract val imageUrl: String?

    fun withImageUrl(url: String?): Component {
        return when (this) {
            is CPU -> this.copy(imageUrl = url)
            is GPU -> this.copy(imageUrl = url)
            is Motherboard -> this.copy(imageUrl = url)
            is RAM -> this.copy(imageUrl = url)
            is PSU -> this.copy(imageUrl = url)
        }
    }

    data class CPU(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val socket: String,
        val generation: String,
        val cores: Int,
        val threads: Int,
        val baseClock: String,
        val boostClock: String,
        val tdp: String,
        val cache: String? = null,
        val integratedGraphics: String? = null,
        val ramSupport: String? = null,
        override val category: String = "Procesador",
        override val imageUrl: String? = null
    ) : Component()

    data class GPU(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val chipset: String,
        val vram: String,
        val vramType: String,
        val memoryBus: String? = null,
        val baseClock: String? = null,
        val boostClock: String? = null,
        val consumptionWatts: String,
        val recommendedPSU: String? = null,
        val pcieInterface: String? = null,
        val length: String? = null,
        override val category: String = "Tarjeta Gráfica",
        override val imageUrl: String? = null
    ) : Component()

    data class Motherboard(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val socket: String,
        val chipset: String,
        val format: String,
        val ramType: String,
        val maxRamSpeed: String? = null,
        val ramSlots: Int? = null,
        val maxRamCapacity: String? = null,
        val slotsM2: Int? = null,
        override val category: String = "Placa Base",
        override val imageUrl: String? = null
    ) : Component()

    data class RAM(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val type: String,
        val capacity: String,
        val configuration: String, // Nueva propiedad para "2x8GB", "2x16GB", etc.
        val speed: String,
        val latency: String,
        val voltage: String? = null,
        val hasRGB: Boolean? = null,
        override val category: String = "Memoria RAM",
        override val imageUrl: String? = null
    ) : Component()

    data class PSU(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val wattage: Int,
        val certification: String,
        val modularity: String,
        val fanSize: String? = null,
        val protection: String? = null,
        override val category: String = "Fuente de Poder",
        override val imageUrl: String? = null
    ) : Component()
}
