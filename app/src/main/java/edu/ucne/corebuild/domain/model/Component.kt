package edu.ucne.corebuild.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("component_class")
sealed class Component {
    abstract val id: Int
    abstract val name: String
    abstract val description: String
    abstract val price: Double
    abstract val category: String
    abstract val imageUrl: String?

    @Serializable
    @SerialName("cpu")
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
        override val category: String = "Procesador",
        override val imageUrl: String? = null
    ) : Component()

    @Serializable
    @SerialName("gpu")
    data class GPU(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        val chipset: String,
        val vram: String,
        val vramType: String,
        val consumptionWatts: String,
        val recommendedPSU: String? = null,
        val pcieInterface: String? = null,
        val length: String? = null,
        override val category: String = "Tarjeta Gráfica",
        override val imageUrl: String? = null
    ) : Component()

    @Serializable
    @SerialName("motherboard")
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
        val maxRamCapacity: String? = null,
        val slotsM2: Int? = null,
        override val category: String = "Placa Base",
        override val imageUrl: String? = null
    ) : Component()

    @Serializable
    @SerialName("ram")
    data class RAM(
        override val id: Int,
        override val name: String,
        override val description: String,
        override val price: Double,
        val brand: String,
        @SerialName("ram_type_internal")
        val type: String,
        val capacity: String,
        val speed: String,
        val latency: String,
        val voltage: String? = null,
        val hasRGB: Boolean? = null,
        override val category: String = "Memoria RAM",
        override val imageUrl: String? = null
    ) : Component()

    @Serializable
    @SerialName("psu")
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
