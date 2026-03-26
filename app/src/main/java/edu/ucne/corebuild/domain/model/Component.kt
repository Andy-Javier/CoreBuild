package edu.ucne.corebuild.domain.model

sealed class Component {
    abstract val id: Int
    abstract val name: String
    abstract val description: String
    abstract val price: Double
    abstract val category: String

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
        override val category: String = "Procesador"
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
        val recommendedWattage: String,
        override val category: String = "Tarjeta Gráfica"
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
        override val category: String = "Placa Base"
    ) : Component()

    data class RAM(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val type: String,
        val capacity: String,
        val speed: String,
        val latency: String,
        override val category: String = "Memoria RAM"
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
        override val category: String = "Fuente de Poder"
    ) : Component()
}
