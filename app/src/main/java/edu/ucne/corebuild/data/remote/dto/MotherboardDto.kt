package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class MotherboardDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val socket: String? = null,
    val chipset: String? = null,
    val formato: String? = null,
    val tipoRam: String? = null,
    val velocidadRamMax: String? = null,
    val slotsRam: Int? = null,
    val capacidadRamMax: String? = null,
    val almacenamiento: String? = null, // Can contain M.2 info
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun MotherboardDto.toDomain(idOverride: Int? = null): Component.Motherboard {
    return Component.Motherboard(
        id = idOverride ?: id,
        name = nombre ?: "Placa Base Desconocida",
        brand = marca ?: "",
        socket = socket ?: "",
        chipset = chipset ?: "",
        format = formato ?: "",
        ramType = tipoRam ?: "",
        maxRamSpeed = velocidadRamMax,
        ramSlots = slotsRam,
        maxRamCapacity = capacidadRamMax,
        slotsM2 = if (almacenamiento?.contains("M.2", ignoreCase = true) == true) 1 else 0,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
